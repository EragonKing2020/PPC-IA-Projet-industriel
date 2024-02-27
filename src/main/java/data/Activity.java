package data;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Activity {
    private final String id;

    private final ActivityType type;

    private final int duration;
    
    private IntVar tDebut;
    private IntVar tFin;
    private IntVar worker;
    private IntVar station;
    

    @JsonCreator
    public Activity(
            @JsonProperty("id") String id,
            @JsonProperty("type") ActivityType type,
            @JsonProperty("duration") int duration
    ) {
        this.id = id;
        this.type = type;
        this.duration = duration;
    }
    
    public void setVariables(Model model, int tMax, int[] workers, int[] stations) {
    	this.tDebut = model.intVar(0, tMax - this.duration);
    	this.tFin = model.intVar(this.duration, tMax);
    	this.worker = model.intVar(workers);
    	this.station = model.intVar(stations);
    }
    
    public String getId() {
        return id;
    }

    public ActivityType getType() {
        return type;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Activity) {
            return id.equals(((Activity) o).getId());
        }
        return false;
    }

    @Override
    public String toString() {
        return "Activity{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", duration=" + duration +
                '}';
    }
}
