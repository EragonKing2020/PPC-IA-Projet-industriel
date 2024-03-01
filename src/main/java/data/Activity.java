package data;

import java.time.Duration;
import java.time.LocalDateTime;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Activity {
    private final String id;

    private final ActivityType type;

    private final int duration;
    
    private IntVar tDebut;
    private IntVar tFin;
    private IntVar durationVar;
    private IntVar worker;
    private IntVar station;
    private Task task;
    

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
    
    public IntVar gettDebut() {
		return tDebut;
	}

	public IntVar gettFin() {
		return tFin;
	}

	public IntVar getDurationVar() {
		return durationVar;
	}

	public IntVar getWorker() {
		return worker;
	}

	public IntVar getStation() {
		return station;
	}

	public Task getTask() {
		return task;
	}

	public void setVariables(Model model, Shift[] shifts, int[] workers, int[] stations) {
    	this.worker = model.intVar(workers);
    	this.station = model.intVar(stations);
    	LocalDateTime start = shifts[0].getStart();
    	LocalDateTime end = shifts[shifts.length-1].getEnd();
    	this.tDebut = model.intVar(0, (int)Duration.between(start, end).toMinutes());
		this.tFin = model.intVar(duration, (int)Duration.between(start, end).toMinutes());
		this.durationVar = model.intVar(duration, (int)Duration.between(start, end).toMinutes());
		this.task = model.taskVar(tDebut, durationVar, tFin);
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
