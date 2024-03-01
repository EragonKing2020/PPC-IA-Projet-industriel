package data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.util.Arrays;
import java.util.LinkedList;

public class Workshop {
    private final String id;

    private final Shift[] shifts;

    private final Station[] stations;

    private final Worker[] workers;

    private final Furniture[] furnitures;
    
    private Model model;
    
    private Solver solver;
    
    private int tMax;

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
        this.tMax = this.getTMax();
        this.createVariables();
    }
    
    private void createVariables() {
    	this.model = new Model();
		this.solver = this.model.getSolver();
		
    	for (Furniture furniture : this.getFurnitures()) {
    		for (Activity act : furniture.getActivities()) {
    			ActivityType type = act.getType();
    			LinkedList<Station> activitiesStations = getStationsFromActivityType(type);
    			int[] stationsNumbers = new int[activitiesStations.size()];
    			for(int i = 0;i<activitiesStations.size();i++) {
    				stationsNumbers[i] = activitiesStations.get(i).getNumberId();
    			}
    			LinkedList<Worker> activitiesWorker = getWorkersFromStation(type);
    			int[] workersNumbers = new int[activitiesWorker.size()];
    			for(int i = 0;i<activitiesWorker.size();i++) {
    				workersNumbers[i] = activitiesWorker.get(i).getNumberId();
    			}
//    			act.setVariables(model, shifts, workersNumbers, stationsNumbers);
    		}
    	}
    }

    public void postConstraints(Model model){
        // Furniture cumulative constraint
        for (Furniture furniture : this.getFurnitures()) {
            Task[] tasks = furniture.getTasks();
            IntVar[] heights = new IntVar[furniture.getActivities().length];
            Arrays.fill(heights, model.intVar(1));
            IntVar capacity = model.intVar(1);
            model.cumulative(tasks, heights, capacity);
        }
    }
    
    public int getTMax() {
    	int tMax = 0;
    	for (Furniture furniture : this.getFurnitures())
    		for (Activity act : furniture.getActivities())
    			tMax += act.getDuration();
    	return tMax;
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
    
    public LinkedList<Worker> getWorkersFromStation(ActivityType type) {
    	LinkedList<Station> activitiesStations = getStationsFromActivityType(type);
    	LinkedList<Worker> activitiesWorker = new LinkedList<Worker>();
    	for(Worker worker : this.workers) {
    		for(String stationName : worker.getStations()) {
    			for(Station station : activitiesStations) {
    				if(station.getId()==stationName) {
    					activitiesWorker.add(worker);
    				}
    			}
    		}
    	}
    	return activitiesWorker;
    }
    
    public LinkedList<Activity> getActivitiesFromActivityType(ActivityType type) {
    	LinkedList<Activity> act = new LinkedList<Activity>();
    	LinkedList<Activity> activities = new LinkedList<Activity>();
    	for(Furniture f : this.furnitures) {
    		for(Activity actf : f.getActivities())
    			act.add(actf);
    	}
    	for(Activity a : act) {
    		if(a.getType()==type)
    			activities.add(a);
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
