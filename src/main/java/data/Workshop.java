package data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;

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
    			act.setVariables(model, tMax, this.getWorkersAct(act.getType()), this.getStationsAct(act.getType()));;
    		}
    	}
    }
    
    public int getTMax() {
    	int tMax = 0;
    	for (Furniture furniture : this.getFurnitures())
    		for (Activity act : furniture.getActivities())
    			tMax += act.getDuration();
    	return tMax;
    }
    
    public int[] getStationsAct(ActivityType type) {
    	int length = 0;
    	for
    }
    
    public int[] getWorkersAct(ActivityType type) {
    	
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
