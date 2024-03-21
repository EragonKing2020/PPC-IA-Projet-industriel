package data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
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
        System.out.println("Shifts equal : " + shiftsEqual());
        this.createVariables();
        this.postConstraints();
        solver.setSearch(Search.activityBasedSearch(this.getDecisionVariables()));
        //solver.setSearch(Search.conflictHistorySearch(this.getDecisionVariables()));
        // System.out.println("Initialisation done");
        System.out.println(solver.solve());
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
		for (Furniture furniture : this.getFurnitures()) {
			for (Activity act : furniture.getActivities()) {
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
				act.setVariables(model, shifts, furniture, workersNumbers, stationsNumbers);
				act.createStationsHeights(model, this.getStations().length);
				act.createWorkersHeights(model, this.getWorkers().length);
				Shift shift = getShiftByString(this.getWorkers()[0].getShift());
				int ub = getDuration(shift.getStart(), shift.getEnd());
				act.createBreaks(model, this.getWorkers().length, this.getWorkers()[0].getBreaks().length, ub);
			}
    	}
		for (Worker worker : this.getWorkers()) {
			worker.setVariables(model, this.getActivitiesFromWorker(worker));
		}
    }
    
    public IntVar[] getDecisionVariables() {
    	LinkedList<Activity> activities = this.getActivities();
    	IntVar[] vars = new IntVar[3 * activities.size()];
    	int i = 0;
    	for (Activity activity : activities) {
    		vars[i] = activity.getWorker();
    		vars[i + 1] = activity.getStation();
    		vars[i + 2] = activity.gettDebut();
    		i += 3;
    	}
    	return vars;
    }

    public void postConstraints(){
    	for(Activity activity : getActivities()) {
//            this.postTimeLimits(activity); 
    		this.postOneHeightPerActivity(activity);
    		this.postLinkHeightToStationAndWorker(activity);
    		this.postPauses(activity);
    		this.postSetDuration(activity);
    	}
        
    	for(Furniture furniture : this.furnitures) {
            //sets the precedence between the types
            this.postPrecedence(furniture);
            // The activities of a given task must go in the following order Transformation->Painting->Sticker->Check
            this.postOrderByType(furniture);
            // Furniture cumulative constraint and precedence/sequence
            this.postCumulativeFurniture(furniture);
    	}
    	for(Worker worker : this.getWorkers()) {
    		// Worker cumulative constraint
    		this.postCumulativeWorkers(worker);
    	}
    	for(Station station : this.getStations()) {
    		// Station cumulative constraint
            this.postCumulativeStations(station);
    	}

    	this.postDefTFin();
    	this.postTDebutNotInBreak();
    }
    
    private void postOneHeightPerActivity(Activity activity) {
    	model.sum(activity.getStationsHeights(), "=",1).post();
    	model.sum(activity.getWorkersHeights(), "=",1).post();
    }
    
    private void postLinkHeightToStationAndWorker(Activity activity) {
    	model.element(model.intVar(1), activity.getStationsHeights(), activity.getStation(),0).post();
    	model.element(model.intVar(1), activity.getWorkersHeights(), activity.getWorker(),0).post();
    }
    
    private void postCumulativeFurniture(Furniture furniture) {
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
    	if(furniture.getPrecedence()!=null) {
    		for(Activity[] precedence : furniture.getPrecedence()) {
            	for(int i = 0;i<precedence.length-1;i++) {
            		model.arithm(precedence[i].gettFin(),"<=", precedence[i+1].gettDebut()).post();
            	}
            }
    	}
    }
    
    private void postPauses(Activity activity) {
    	LocalDateTime startDay = this.getShifts()[0].getStart();
    	for(Worker worker : this.getWorkers()) {
    		for(int i = 0; i<worker.getBreaks().length;i++)
    			model.ifThenElse(
    				model.and(
    					model.arithm(activity.gettDebut(), "<", getDuration(startDay, worker.getBreaks()[i][1])),
    					model.arithm(activity.gettFin(), ">", getDuration(startDay, worker.getBreaks()[i][0]))
    					), 
    				model.arithm(activity.getBreaks()[worker.getNumberId()][i], "=", getDuration(worker.getBreaks()[i][0],worker.getBreaks()[i][1])), 
    				model.arithm(activity.getBreaks()[worker.getNumberId()][i], "=", 0));
    	}
    }
    
    private void postSetDuration(Activity activity) {
    	IntVar[][] breaks = new IntVar[activity.getBreaks().length][activity.getBreaks()[0].length+1];
    	for(int i = 0;i<breaks.length;i++) {
    		for(int j = 0;j<breaks[0].length-1;j++) {
    			breaks[i][j] = activity.getBreaks()[i][j];
    		}
    		breaks[i][breaks[0].length-1] = model.intVar(activity.getDuration());
    	}
    	for(int w = 0;w<this.getWorkers().length;w++) {
    		model.ifThen(
    				model.arithm(activity.getWorker(), "=", w), 
    				model.sum(breaks[w], "=", activity.getDurationVar())
    				);
    	}
    }
    
//    private void postPauses() {
//    	LocalDateTime startDay = this.getShifts()[0].getStart();
//    	for (Worker worker : this.getWorkers()) {
//    		for (Activity activity : this.getActivitiesFromWorker(worker)) {
//    			int i = 0;
//    			for (LocalDateTime[] pause : worker.getBreaks()) {
//    				model.ifOnlyIf(model.and(model.arithm(activity.getWorker(), "=", worker.getNumberId()),
//    										model.arithm(activity.gettDebut(), "<=", (int)Duration.between(startDay, pause[1]).toMinutes()),
//    										model.arithm(activity.gettFin(),">", (int)Duration.between(startDay, pause[0]).toMinutes())),
//    							model.arithm(worker.getBoolPauseAct(i, activity), "=", 1));
//    				i ++;
//    			}
//    		}
//    		for (int i = 0; i < worker.getBreaks().length; i ++) {
//    			IntVar[] boolPause = worker.getBoolPause(i);
//    			if (boolPause.length >0) {
//	    			int[] scalars = new int[boolPause.length];
//	    			for (int j = 0; j < scalars.length; j ++)
//	    				scalars[j] = 1;
//	    			model.scalar(boolPause, scalars, "<=", 1).post();
//    			}
//    		}
//    	}
//    }
    
    private void postDefTFin() {
    	for (Furniture furniture : this.getFurnitures()) {
    		for (Activity activity : furniture.getActivities()) {
    			LinkedList<Worker> workers = this.getWorkersFromStations(this.getStationsFromActivityType(activity.getType()));
    			LinkedList<LocalDateTime[]> pauses = new LinkedList<LocalDateTime[]>();
    			for (Worker worker : workers) {
    				for (LocalDateTime[] pause : worker.getBreaks()) {
    					pauses.add(pause);
    				}
    			}
    			IntVar[] vars = new IntVar[pauses.size() + 3];
    			int[] scalars = new int[pauses.size() + 3];
    			vars[0] = activity.gettFin();
    			scalars[0] = -1;
    			vars[1] = activity.gettDebut();
    			scalars[1] = 1;
    			vars[2] = model.intVar(activity.getDuration());
    			scalars[2] = 1;
    			int i = 3;
    			for (Worker worker : workers) {
    				for (int j = 0; j < worker.getBreaks().length; j ++) {
    					vars[i] = worker.getBoolPauseAct(j, activity);
    					scalars[i] = worker.getDurationBreak(j);
						i ++;
    				}
    			}
    			model.scalar(vars, scalars, "=", 0).post();
    		}
    	}
    }
    
    private void postTDebutNotInBreak() {
    	LocalDateTime start = this.getShifts()[0].getStart();
    	for (Furniture furniture : this.getFurnitures()) {
    		for (Activity activity : furniture.getActivities()) {
    			for (Worker worker : this.getWorkersFromStations(this.getStationsFromActivityType(activity.getType()))) {
    				for (int i = 0; i < worker.getBreaks().length; i ++) {
    					model.ifThen(model.arithm(activity.gettDebut(), "=", worker.getNumberId()),
    								model.or(model.arithm(activity.gettDebut(), "<", (int)Duration.between(start, worker.getBreaks()[i][0]).toMinutes()),
    										 model.arithm(activity.gettDebut(), ">=", (int)Duration.between(start, worker.getBreaks()[i][1]).toMinutes())));
    				}
    			}
    		}
    	}
    }
    
    private void postCumulativeWorkers(Worker worker) {
    		LinkedList<Activity> activities = this.getActivitiesFromWorker(worker);
    		if(activities.size()==0) {
    			return;
    		}
    		Task[] tasks = new Task[activities.size() + 2];
    		IntVar[] heights = model.intVarArray(activities.size() + 2, 0, 1);
    		model.arithm(heights[0], "=", 1).post();
    		model.arithm(heights[1], "=", 1).post();
    		LocalDateTime startDay = shifts[0].getStart();
        	LocalDateTime endDay = shifts[shifts.length-1].getEnd();
        	Shift shiftWorker = this.getShiftByString(worker.getShift());
        	LocalDateTime startWorker = shiftWorker.getStart();
        	LocalDateTime endWorker = shiftWorker.getEnd();
        	tasks[0] = model.taskVar(model.intVar(0), (int)Duration.between(startDay, startWorker).toMinutes());
    		tasks[1] = model.taskVar(model.intVar((int)Duration.between(startDay, endWorker).toMinutes()), (int)Duration.between(endWorker, endDay).toMinutes());
    		
    		for(int i = 0;i<activities.size();i++) {
            	tasks[i + 2] = activities.get(i).getTask();
            	heights[i+2] = activities.get(i).getWorkersHeights()[worker.getNumberId()];
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
        	heights[i] = activities.get(i).getStationsHeights()[station.getNumberId()];
        }
        IntVar capacity = model.intVar(1);
        model.cumulative(tasks, heights, capacity).post();
    }
    
    public void postOrderByType(Furniture furniture) {
		LinkedList<Activity> transformation = furniture.getActivitiesFromType(ActivityType.TRANSFORMATION);
		LinkedList<Activity> painting = furniture.getActivitiesFromType(ActivityType.PAINT);
		LinkedList<Activity> sticker = furniture.getActivitiesFromType(ActivityType.STICKER);
		LinkedList<Activity> check = furniture.getActivitiesFromType(ActivityType.CHECK);
		@SuppressWarnings("unchecked")
		LinkedList<Activity>[] activitiesSorted = (LinkedList<Activity>[]) new LinkedList[]{transformation, painting, sticker, check};
		for (int i = 0; i < activitiesSorted.length - 1; i ++)
			for (int j = i + 1; j < activitiesSorted.length; j ++)
				for (Activity actBefore : activitiesSorted[i])
					for (Activity actAfter : activitiesSorted[j])
						model.arithm(actBefore.gettFin(), "<=", actAfter.gettDebut()).post();
    }
 
    public int getDuration(LocalDateTime t1, LocalDateTime t2) {
    	return (int)Duration.between(t1, t2).toMinutes();
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
    
    public LinkedList<Worker> getWorkersFromStations(LinkedList<Station> stations){
    	HashSet<Worker> workers = new HashSet<Worker>();
    	for(Worker worker : this.workers) {
    		for(String stationName : worker.getStations()) {
    			for (Station station : stations)
					if(station.getId().equals(stationName)) {
						workers.add(worker);
					}
    		}
    	}
    	return new LinkedList<Worker>(workers);
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
    
    public LinkedList<Activity> getActivitiesFromActivityTypes(LinkedList<ActivityType> types){
    	LinkedList<Activity> activities = new LinkedList<Activity>();
    	for(Furniture f : this.furnitures)
    		for(Activity actf : f.getActivities())
    			if (types.contains(actf.getType()))
    				activities.add(actf);
    	return activities;
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
    
    public int minimumInterval() {
    	LocalDateTime startDay = shifts[0].getStart();
    	LocalDateTime endDay = shifts[shifts.length-1].getEnd();
    	int min = (int)Duration.between(startDay, endDay).toMinutes();
    	for(Worker worker : this.getWorkers()) {
    		for(int i = 0;i<worker.getBreaks().length-1;i++) {
    			int candidat = (int)Duration.between(worker.getBreaks()[i][1], worker.getBreaks()[i+1][0]).toMinutes();
    			if(candidat<min) {
    				min = candidat;
    			}
    		}
    	}
    	return min;
    }
    
    public boolean equals(LocalDateTime[] break1, LocalDateTime[] break2) {
    	if(((int)Duration.between(break1[0] , break2[0]).toMinutes() == 0) && ((int)Duration.between(break1[1] , break2[1]).toMinutes() == 0))
    		return true;
    	return false;
    }
    
    public boolean shiftsEqual() {
    	for(int i = 0;i<this.getWorkers().length;i++) {
    		for(int j = i+1;j<this.getWorkers().length;j++) {
    			if(this.getWorkers()[i].getShift().equals(this.getWorkers()[j].getShift())) {
    				if(this.getWorkers()[i].getBreaks().length!=this.getWorkers()[j].getBreaks().length)
    					return false;
    				for(int k = 0;k<this.getWorkers()[i].getBreaks().length;k++) {
    					if(!equals(this.getWorkers()[i].getBreaks()[k],this.getWorkers()[j].getBreaks()[k]))
    						return false;
    				}
    			}
    		}
    	}
    	return true;
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
