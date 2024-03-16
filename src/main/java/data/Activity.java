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
    private IntVar worker;
    private IntVar workerIndex;
    private IntVar[] workerHeights;
    private int[] possibleWorkers;
    private IntVar station;
    private IntVar stationIndex;
    private IntVar[] stationHeights;
    private int[] possibleStations;
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
		return this.worker;
	}

	public IntVar getStation() {
		return this.station;
	}

	public Task getTask() {
		return task;
	}

	public void setVariables(Model model, Shift[] shifts, int[] workers, int[] stations) {
		this.possibleWorkers = workers;
		this.possibleStations = stations;
    	this.worker = model.intVar(workers);
    	this.station = model.intVar(stations);
    	LocalDateTime start = shifts[0].getStart();
    	LocalDateTime end = shifts[shifts.length-1].getEnd();
    	this.tDebut = model.intVar(0, (int)Duration.between(start, end).toMinutes() - duration);
		this.tFin = model.intVar(duration, (int)Duration.between(start, end).toMinutes());
		this.durationVar = model.intVar(duration, (int)Duration.between(start, end).toMinutes());
		this.task = model.taskVar(tDebut, durationVar, tFin);
    }
    
    
    public IntVar getWorkerIndex() {
		return workerIndex;
	}

	public void setWorkerIndex(Model model, int upperBound) {
		this.workerIndex = model.intVar(0, upperBound);
	}

	public IntVar getStationIndex() {
		return stationIndex;
	}

	public void setStationIndex(Model model, int upperBound) {
		this.stationIndex = model.intVar(0, upperBound);
	}

	public IntVar[] getWorkerHeights() {
		return workerHeights;
	}

	public void setWorkerHeights(Model model, int length) {
		this.workerHeights = model.intVarArray(length, 0, 1);
		model.sum(this.workerHeights,"=" ,1).post();
	}

	public IntVar[] getStationHeights() {
		return stationHeights;
	}

	public void setStationHeights(Model model, int length) {
		this.stationHeights = model.intVarArray(length, 0, 1);
		model.sum(this.stationHeights,"=", 1).post();
	}

	public int[] getPossibleWorkers() {
		return possibleWorkers;
	}
	
	public int[] getPossibleStations() {
		return possibleStations;
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
	
	public int getNumberId() {
    	return Integer.parseInt(id.substring(1, id.length())) ;
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
    
    public String solToString() {
    	String stringWH = "[";
    	for(IntVar height : this.getWorkerHeights()) {
    		stringWH += " " + height.getValue() + " ";
    	}
    	stringWH += "]";
    	return "Activity{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", duration=" + duration +
                "}\r" +
                "tDebut=" + this.gettDebut().getValue() +
                ", tFin=" + this.gettFin().getValue()+
                "\r"+
                "workerID = " + this.getWorker().getValue() + "\r"+
                "stationID = " + this.getStation().getValue() +"\r"+
                "workerHeights = " + stringWH +
                "\n";
    }
}
