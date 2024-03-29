package data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

public class Worker {
    private final String id;

    private final String shift;

    private final LocalDateTime[][] breaks;

    private final String[] stationsNames;
    
    private IntVar[][] boolPauseAct;
    private HashMap<Activity, Integer> indiceActBoolPauseAct = new HashMap<Activity, Integer>();

    @JsonCreator
    public Worker(
            @JsonProperty("id") String id,
            @JsonProperty("shift") String shift,
            @JsonProperty("breaks") LocalDateTime[][] breaks,
            @JsonProperty("stations") String[] stationsNames
    ) {
        this.id = id;
        this.shift = shift;
        this.breaks = breaks;
        this.stationsNames = stationsNames;
    }

    public String getId() {
        return id;
    }
    public int getNumberId() {
    	return Integer.parseInt(id.substring(1, id.length())) ;
    }
    public String getShift() {
        return shift;
    }

    public LocalDateTime[][] getBreaks() {
        return breaks;
    }

    public String[] getStations() {
        return stationsNames;
    }
    
    public void setVariables(Model model, LinkedList<Activity> activities) {
    	this.boolPauseAct = model.intVarMatrix(this.getBreaks().length, activities.size(), 0, 1);
    	for (int i = 0; i < activities.size(); i ++)
    		this.indiceActBoolPauseAct.put(activities.get(i), i);
    }
    
    private IntVar getBoolPauseActInt(int pause, int activity) {
    	return this.boolPauseAct[pause][activity];
    }
    
    public IntVar getBoolPauseAct(int pause, Activity activity) {
    	return this.getBoolPauseActInt(pause, this.indiceActBoolPauseAct.get(activity));
    }
    
    public IntVar[] getBoolPause(int pause) {
    	return this.boolPauseAct[pause];
    }
    
    public IntVar[] getBoolAct(Activity activity) {
    	IntVar[] vars = new IntVar[this.boolPauseAct.length];
    	for (int i = 0; i < this.boolPauseAct.length; i ++)
    		vars[i] = this.getBoolPauseAct(i, activity);
    	return vars;
    }

    @Override
    public String toString() {
        return "Worker{" +
                "id='" + id + '\'' +
                ", shift=" + shift +
                ", breaks=" + Arrays.toString(breaks) +
                ", stations=" + Arrays.toString(stationsNames) +
                '}';
    }
}
