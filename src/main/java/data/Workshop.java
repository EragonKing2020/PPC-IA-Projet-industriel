package data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

public class Workshop {
    private final String id;

    private final Shift[] shifts;

    private final Station[] stations;

    private final Worker[] workers;

    private final Furniture[] furnitures;
    
    private Model model;
    
    private Solver solver;

    @JsonCreator
    public Workshop(
            @JsonProperty("id") String id,
            @JsonProperty("shifts") Shift[] shifts,
            @JsonProperty("stations") Station[] stations,
            @JsonProperty("workers") Worker[] workers,
            @JsonProperty("furnitures") Furniture[] furnitures
    ) {
        this.id = id;
        this.shifts = shifts;
        this.stations = stations;
        this.workers = workers;
        this.furnitures = furnitures;
        this.createVariables();
        this.postConstraints();
        solver.solve();
        System.out.println(this);
        for (Furniture furniture : this.furnitures) {
        	System.out.println(furniture.solToString());
        	for (Activity activity : furniture.getActivities())
        		System.out.println(activity.solToString());
        }
    }
    
    private void createVariables() {
    	this.model = new Model();
		this.solver = this.model.getSolver();	
		for (Activity act : this.getActivities()) {
			LinkedList<Station> activitiesStations = getStationsFromActivityType(act.getType());
			HashSet<Worker> activitiesWorkers = new HashSet<Worker>();
			for(Station station : activitiesStations) {
				for(Worker worker : getWorkersFromStation(station))
					activitiesWorkers.add(worker);
			}
			int[] stationsNumbers = new int[activitiesStations.size()];
			for(int i = 0;i<activitiesStations.size();i++) {
				stationsNumbers[i] = activitiesStations.get(i).getNumberId();
			}
			int[] workersNumbers = new int[activitiesWorkers.size()];
			Object[] workers = activitiesWorkers.toArray();
			for(int i = 0;i<activitiesWorkers.size();i++) {
				workersNumbers[i] = ((Worker)workers[i]).getNumberId();
			}
			act.setVariables(model, shifts, workersNumbers, stationsNumbers);
		}
    }

    public void postConstraints(){
    	for(Activity activity : getActivities()) {
            this.postTimeLimits(activity); 
    	}
        
    	for(Furniture furniture : this.furnitures) {
            //sets the precedence between the types
            this.postPrecedence(furniture);
            // The activities of a given task must go in the following order Transformation->Painting->Sticker->Check
            this.postOrderByType(furniture);
            // Furniture cumulative constraint and precedence/sequence
            this.postCumulativeFurniture(model, furniture);
    	}
    	for(Worker worker : this.getWorkers()) {
    		// Worker cumulative constraint
    		this.postCumulativeWorkers(worker);
    	}
    	for(Station station : this.getStations()) {
    		// Station cumulative constraint
            this.postCumulativeStations(station);
    	}
        
    }
    
    private void postCumulativeFurniture(Model model,Furniture furniture) {
    		LinkedList<Task> tasksList = furniture.getTasks(model);
            Task[] tasks = new Task[tasksList.size()];
            for(int i = 0;i<tasksList.size();i++) {
            	tasks[i] = tasksList.get(i);
            }
            IntVar[] heights = new IntVar[tasks.length];
            Arrays.fill(heights, model.intVar(1));
            IntVar capacity = model.intVar(1);
            model.cumulative(tasks, heights, capacity).post();
    }
    
    private void postPrecedence(Furniture furniture) {
    	// Precedence constraint
        for(Activity[] precedence : furniture.getPrecedence()) {
        	for(int i = 0;i<precedence.length-1;i++) {
        		model.arithm(precedence[i].gettFin(),"<=", precedence[i+1].gettDebut()).post();
        	}
        }
    }
    
    private void postTimeLimits(Activity activity) {
		for(int workerId : activity.getPossibleWorkers()) {
			String shiftID = workers[workerId].getShift();
	        LocalDateTime start_time = getShiftByString(shiftID).getStart();
	        LocalDateTime end_time = getShiftByString(shiftID).getEnd();
	        int startToMinutes = (int)Duration.between(shifts[0].getStart(), start_time).toMinutes();
	        int endToMinutes = (int)Duration.between(shifts[0].getStart(), end_time).toMinutes();
            model.ifThen(
                    model.arithm(activity.getWorker(),"=",workerId),
                    model.and(
                            model.arithm(activity.gettDebut(),">=",startToMinutes),
                            model.arithm(activity.gettFin(),"<=",endToMinutes)
                            )
                        );
		}
    }
    
    private void postCumulativeWorkers(Worker worker) {
//    		heights[0] = this.model.intVar(1);
//    		heights[1] = this.model.intVar(1);
//    		LocalDateTime startDay = shifts[0].getStart();
//        	LocalDateTime endDay = shifts[shifts.length-1].getEnd();
//        	Shift shiftWorker = this.getShiftByString(worker.getShift());
//        	LocalDateTime startWorker = shiftWorker.getStart();
//        	LocalDateTime endWorker = shiftWorker.getEnd();
//        	tasks[0] = model.taskVar(model.intVar(0), (int)Duration.between(startDay, startWorker).toMinutes());
//    		tasks[1] = model.taskVar(model.intVar((int)Duration.between(startDay, endWorker).toMinutes()), (int)Duration.between(endWorker, endDay).toMinutes());
    		LinkedList<Activity> activities = this.getActivitiesFromWorker(worker);
    		Task[] tasks = new Task[activities.size()];
    		IntVar[] heights = model.intVarArray(activities.size(), 0, 1);
    		for(int i = 0;i<activities.size();i++) {
            	tasks[i] = activities.get(i).getTask();
            	model.ifOnlyIf(
            			model.arithm(activities.get(i).getStation(),"=", worker.getNumberId()), 
            			model.arithm(heights[i], "=", 1));
            }
            IntVar capacity = model.intVar(1);
            model.cumulative(tasks, heights, capacity).post();
    }
    
    private void postCumulativeStations(Station station) {    	
		LinkedList<Activity> activities = this.getActivitiesFromStation(station);
		Task[] tasks = new Task[activities.size()];
		IntVar[] heights = model.intVarArray(activities.size(), 0, 1);
		for(int i = 0;i<activities.size();i++) {
        	tasks[i] = activities.get(i).getTask();
        	model.ifOnlyIf(
        			model.arithm(activities.get(i).getStation(),"=", station.getNumberId()), 
        			model.arithm(heights[i], "=", 1));
        }
        IntVar capacity = model.intVar(1);
        model.cumulative(tasks, heights, capacity).post();
    }
    
    public void postOrderByType(Furniture furniture) {
		LinkedList<Activity> transformation = this.getActivitiesFromActivityType(ActivityType.TRANSFORMATION, furniture.getActivities());
		LinkedList<Activity> painting = this.getActivitiesFromActivityType(ActivityType.PAINT, furniture.getActivities());
		LinkedList<Activity> sticker = this.getActivitiesFromActivityType(ActivityType.STICKER, furniture.getActivities());
		LinkedList<Activity> check = this.getActivitiesFromActivityType(ActivityType.CHECK, furniture.getActivities());
		for(Activity t : transformation)
			for(Activity p : painting)
				for(Activity s : sticker)
					for(Activity c : check) {
						model.arithm(t.gettFin(), "<=", p.gettDebut()).post();
						model.arithm(p.gettFin(), "<=", s.gettDebut()).post();
						model.arithm(s.gettFin(), "<=", c.gettDebut()).post();
					}
    }
    
    public Shift getShiftByString(String shiftString) {
    	for (Shift shift : this.shifts)
    		if (shift.getId().equals(shiftString))
    			return shift;
    	throw new Error("shift introuvable");
    }
    
    public LinkedList<Station> getStationsFromActivityType(ActivityType type) {
    	LinkedList<Station> activitiesStations = new LinkedList<Station>();
    	for(Station station : this.stations) {
    		for(ActivityType actType : station.getActivityTypes()) {
    			if(actType==type){
    				activitiesStations.add(station);
    			}
    		}
    	}
    	return activitiesStations;
    }
    
    public LinkedList<Worker> getWorkersFromStation(Station station) {
    	LinkedList<Worker> activitiesWorker = new LinkedList<Worker>();
    	for(Worker worker : this.workers) {
    		for(String stationName : worker.getStations()) {
				if(station.getId().equals(stationName)) {
					activitiesWorker.add(worker);
				}
    		}
    	}
    	return activitiesWorker;
    }
    
    public LinkedList<ActivityType> getActivityTypesFromWorker(Worker worker){
    	HashSet<ActivityType> activities = new HashSet<ActivityType>();
    	for (String idStation : worker.getStations()) 
    		for (ActivityType actT : this.getStationFromId(idStation).getActivityTypes())
    			activities.add(actT);
    	
    	return new LinkedList<ActivityType>(activities);
    }
    
    public Station getStationFromId(String id) {
    	for (Station station : this.getStations())
    		if (station.getId().equals(id))
    			return station;
    	throw new Error("T'as mis n'imp comme id de station");
    }
    
    public LinkedList<Activity> getActivitiesFromActivityType(ActivityType type, Activity[] a) {
    	HashSet<Activity> activities = new HashSet<Activity>();
    	for(Activity activity : a) {
    		if (activity.getType() == type)
				activities.add(activity);
    	}
    	return new LinkedList<Activity>(activities);
    }
    
    public LinkedList<Activity> getActivitiesFromActivityTypes(LinkedList<ActivityType> types){
    	LinkedList<Activity> activities = new LinkedList<Activity>();
    	for(Furniture f : this.furnitures)
    		for(Activity actf : f.getActivities())
    			if (types.contains(actf.getType()))
    				activities.add(actf);
    	return activities;
    }
    
    public LinkedList<Activity> getActivitiesFromActivityTypes(ActivityType[] types) {
    	LinkedList<ActivityType> typesList = new LinkedList<ActivityType>();
    	for (ActivityType type : types)
    		typesList.add(type);
    	return this.getActivitiesFromActivityTypes(typesList);
    }
    
    public LinkedList<Activity> getActivitiesFromWorker(Worker worker){
    	return this.getActivitiesFromActivityTypes(this.getActivityTypesFromWorker(worker));
    }
    
    public LinkedList<Activity> getActivitiesFromStation(Station station){
    	LinkedList<Activity> stationActs = new LinkedList<Activity>();
    	for(Activity activity : this.getActivities()) {
    		for(ActivityType type : station.getActivityTypes()) {
    			if(type.equals(activity.getType())) {
    				stationActs.add(activity);
    			}
    		}
    	}
    	return stationActs;
    }
    
    public LinkedList<Activity> getActivities(){
    	LinkedList<Activity> activities = new LinkedList<Activity>();
    	for(Furniture furniture : this .getFurnitures()) {
    		for(Activity activity : furniture.getActivities()) {
    			activities.add(activity);
    		}
    	}
    	return activities;
    }
    
    public String getId() {
        return id;
    }

    public Shift[] getShifts() {
        return shifts;
    }

    public Station[] getStations() {
        return stations;
    }

    public Worker[] getWorkers() {
        return workers;
    }

    public Furniture[] getFurnitures() {
        return furnitures;
    }

    @Override
    public String toString() {
        return "Workshop{" +
                "id='" + id + '\'' +
                ", shifts=" + Arrays.toString(shifts) +
                ", stations=" + Arrays.toString(stations) +
                ", workers=" + Arrays.toString(workers) +
                ", furnitures=" + Arrays.toString(furnitures) +
                '}';
    }
    
}
