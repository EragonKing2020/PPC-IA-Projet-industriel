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
    private IntVar[] workersHeights;
	private int[] possibleWorkers;
    private IntVar station;
    private IntVar[] stationsHeights;
    private IntVar[][] breaks;
    private int[] possibleStations;
    private Task task;
    private Furniture furniture;
    

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
	
	 public IntVar[] getWorkersHeights() {
			return workersHeights;
		}

		public IntVar[] getStationsHeights() {
			return stationsHeights;
		}

	public void setVariables(Model model, Shift[] shifts, Furniture furniture, int[] workers, int[] stations) {
		this.furniture = furniture;
		this.possibleWorkers = workers;
		this.possibleStations = stations;
    	this.worker = model.intVar(workers);
    	this.station = model.intVar(stations);
    	LocalDateTime start = shifts[0].getStart();
    	LocalDateTime end = shifts[shifts.length-1].getEnd();
    	int tMinDebut = this.furniture.getTimeMinBefore(this);
    	int tMaxFin = (int)Duration.between(start, end).toMinutes() - this.furniture.getTimeMinAfter(this);
    	this.tDebut = model.intVar(0, tMaxFin - duration);
		this.tFin = model.intVar(duration, tMaxFin);
		this.durationVar = model.intVar(duration, tMaxFin - tMinDebut);
		this.task = model.taskVar(tDebut, durationVar, tFin);
    }
	
	public void createWorkersHeights(Model model, int length) {
		this.workersHeights = model.intVarArray(length, 0, 1);
	}
	
	public void createStationsHeights(Model model, int length) {
		this.stationsHeights = model.intVarArray(length, 0, 1);
	}
	
	public void createBreaks(Model model, int length, int nb_breaks, int ub) {
		this.breaks = model.intVarMatrix(length, nb_breaks, 0, ub);
	}
	
	public IntVar[][] getBreaks() {
		return breaks;
	}

	public int[] getPossibleWorkers() {
		return possibleWorkers;
	}
	
	public int[] getPossibleStations() {
		return possibleStations;
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
    	return "Activity{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", duration=" + duration +
                "}\r" +
                "tDebut=" + this.gettDebut().getValue() +
                ", tFin=" + this.gettFin().getValue()+
                "\r"+
                "workerID = " + this.getWorker().getValue() + "\r"+
                "stationID = " + this.getStation().getValue() + "\r"+
                "worker heights = " + this.getWorkersHeights()+ "\r"+
                "station heights = " + this.getStationsHeights()+ "\r"+
                "\n";
    }
}
