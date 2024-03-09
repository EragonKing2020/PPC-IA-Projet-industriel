package data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;

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
    private HashMap<Integer, Integer> iWorkers = new HashMap<Integer, Integer>();
    private IntVar[] workers;
    private HashMap<Integer, Integer> iStations = new HashMap<Integer, Integer>();
    private IntVar[] stations;
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

	public IntVar getWorker(int worker) {
		return this.workers[this.iWorkers.get(worker)];
	}

	public IntVar getStation(int station) {
		return this.stations[this.iStations.get(station)];
	}

	public Task getTask() {
		return task;
	}

	public void setVariables(Model model, Shift[] shifts, int[] workers, int[] stations) {
		for (int i = 0; i < workers.length; i ++)
			this.iWorkers.put(workers[i], i);
		for (int i = 0; i < stations.length; i ++)
			this.iStations.put(stations[i], i);
    	this.workers = model.intVarArray(workers.length, 0, 1);
    	this.stations = model.intVarArray(stations.length, 0, 1);
    	LocalDateTime start = shifts[0].getStart();
    	LocalDateTime end = shifts[shifts.length-1].getEnd();
    	this.tDebut = model.intVar(0, (int)Duration.between(start, end).toMinutes());
    	if (this.getId().equals("A66")) {
    		System.out.println(this.getId());
    		System.out.println(this.gettDebut());
    		System.out.println(this);
    	}
		this.tFin = model.intVar(duration, (int)Duration.between(start, end).toMinutes());
		this.durationVar = model.intVar(duration, (int)Duration.between(start, end).toMinutes());
		this.task = model.taskVar(tDebut, durationVar, tFin);
		if (this.getId().equals("A66")) {
    		System.out.println(this.getId());
    		System.out.println("\t"+this.task.toString());
    		System.out.println("\t"+this.gettDebut());
    		System.out.println("\t"+this);
    	}
    }
    
    
    public void settDebut(IntVar tDebut) {
		this.tDebut = tDebut;
	}

	public void settFin(IntVar tFin) {
		this.tFin = tFin;
	}

	public void setDurationVar(IntVar durationVar) {
		this.durationVar = durationVar;
	}

	public void setTask(Task task) {
		this.task = task;
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
