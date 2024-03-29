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
//    private IntVar[] durations;
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
	
	public IntVar getWorkerHeight(Worker worker) {
		return workersHeights[this.getIndiceWorker(worker)];
	}
	
	public IntVar getWorkerHeight(int worker) {
		return workersHeights[this.getIndiceWorker(worker)];
	}
	
	public IntVar[] getStationsHeights() {
		return stationsHeights;
	}
	
	public IntVar getStationHeight(Station station) {
		return stationsHeights[this.getIndiceStation(station)];
	}
	
	public IntVar getStationHeight(int station) {
		return stationsHeights[this.getIndiceStation(station)];
	}
	
	public IntVar getBreak(Worker worker, int iPause) {
		return breaks[this.getIndiceWorker(worker)][iPause];
	}
	
	public IntVar[] getBreaksWorker(Worker worker) {
		return breaks[this.getIndiceWorker(worker)];
	}
	
	public IntVar[] getBreaksWorker(int worker) {
		return breaks[this.getIndiceWorker(worker)];
	}

//	public IntVar[] getDurations() {
//		return durations;
//	}

	public void setVariables(Model model, Shift[] shifts, Furniture furniture, int[] workers, int[] stations, int[] durations) {
		this.furniture = furniture;
		this.possibleWorkers = workers;
		this.possibleStations = stations;
    	this.worker = model.intVar("Worker_" + this.getId(),workers);
    	this.station = model.intVar("Station_" + this.getId(),stations);
    	LocalDateTime start = shifts[0].getStart();
    	LocalDateTime end = shifts[shifts.length-1].getEnd();
    	int tMinDebut = this.furniture.getTimeMinBefore(this);
    	int tMaxFin = (int)Duration.between(start, end).toMinutes() - this.furniture.getTimeMinAfter(this);
    	this.tDebut = model.intVar("tDebut_" + this.getId(),tMinDebut, tMaxFin - duration);
		this.tFin = model.intVar("tFin_" + this.getId(),tMinDebut+duration, tMaxFin);
		this.durationVar = model.intVar("tDuration_" + this.getId(),this.duration, tMaxFin - tMinDebut);
		this.task = model.taskVar(tDebut, durationVar, tFin);
		this.workersHeights = model.intVarArray(workers.length, 0, 1);
		this.stationsHeights = model.intVarArray(stations.length, 0, 1);
    }
	
	public void createBreaks(Model model, int[][] breaksDurations) {
		this.breaks = new IntVar[this.possibleWorkers.length][breaksDurations[0].length+1];
		for(int i = 0; i < this.possibleWorkers.length; i ++) {
			for(int j = 0;j<breaks[0].length-1;j++) {
				breaks[i][j] = model.intVar("Break_" + this.getId() + "_" + this.possibleWorkers[i] + "," + j,new int[] {0,breaksDurations[this.possibleWorkers[i]][j]});
			}
			breaks[i][breaks[i].length-1] = model.intVar(getDuration());
		}
//		this.durations = new IntVar[breaks.length];
//		for(int i = 0;i<durations.length;i++) {
//			durations[i] = model.sum( id + "duration_"+i, breaks[i]);
//		}
	}
	
	public IntVar[][] getBreaks() {
		return breaks;
	}
	
	private int getIndiceWorker(Worker worker) {
		return getIndiceWorker(worker.getNumberId());
	}
	
	private int getIndiceWorker(int worker) {
		int i =0;
		while (i < this.possibleWorkers.length && worker != possibleWorkers[i])
			i ++;
		if (i < this.possibleWorkers.length)
			return i;
		throw new Error("Worker " + worker + " is not possible for the activity " + this.id);
	}
	
	private int getIndiceStation(Station station) {
		return this.getIndiceStation(station.getNumberId());
	}
	
	private int getIndiceStation(int station) {
		int i =0;
		while (i < this.possibleStations.length && station != possibleStations[i])
			i ++;
		if (i < this.possibleStations.length)
			return i;
		throw new Error("Station " + station + " is not possible for the activity " + this.id);
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
