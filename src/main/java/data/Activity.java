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
    private HashMap<Integer, Integer> iWorkersInversed = new HashMap<Integer, Integer>();
    private IntVar[] workers;
    private HashMap<Integer, Integer> iStations = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer> iStationsInversed = new HashMap<Integer, Integer>();
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
	
	public IntVar[] getWorkers() {
		return this.workers;
	}

	public IntVar getStation(int station) {
		return this.stations[this.iStations.get(station)];
	}
	
	public IntVar[] getStations() {
		return this.stations;
	}

	public Task getTask() {
		return task;
	}

	public void setVariables(Model model, Shift[] shifts, int[] workers, int[] stations) {
		for (int i = 0; i < workers.length; i ++) {
			this.iWorkers.put(workers[i], i);
			this.iWorkersInversed.put(i, workers[i]);
		}
		for (int i = 0; i < stations.length; i ++) {
			this.iStations.put(stations[i], i);
			this.iStationsInversed.put(i, stations[i]);
		}
    	this.workers = model.intVarArray(workers.length, 0, 1);
    	this.stations = model.intVarArray(stations.length, 0, 1);
    	LocalDateTime start = shifts[0].getStart();
    	LocalDateTime end = shifts[shifts.length-1].getEnd();
    	this.tDebut = model.intVar(0, (int)Duration.between(start, end).toMinutes() - duration);
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
    
    public int getWorkerValue() {
    	int i;
    	for (i = 0; this.getWorker(i).getValue() == 0; i ++);
    	return this.iWorkersInversed.get(i);
    }
    
    public int getStationValue() {
    	int i;
    	for (i = 0; this.getStation(i).getValue() == 0; i ++);
    	return this.iStationsInversed.get(i);
    }
    
    public String solToString() {
    	return "Activity{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", duration=" + duration +
                "}\n\r" +
                "tDebut=" + this.gettDebut().getValue() +
                ", tFin=" + this.gettFin().getValue() +
                ", worker=" + this.getWorkerValue() +
                ", station" + this.getStationValue();
    }
}
