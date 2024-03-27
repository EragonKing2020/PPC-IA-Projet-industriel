package data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

public class Station {
    private final String id;
    private final ActivityType[] activityTypes;
    
    private IntVar nbActivities;

    @JsonCreator
    public Station(
            @JsonProperty("id") String id,
            @JsonProperty("activityTypes") ActivityType[] activityTypes
    ) {
        this.id = id;
        this.activityTypes = activityTypes;
    }

	public void createVariables(Model model, int length) {
    	this.nbActivities = model.intVar(0,length);
    }
    
    public IntVar getNbActivities() {
		return nbActivities;
	}

	public int getNumberId() {
    	return Integer.parseInt(id.substring(1, id.length())) ;
    }
    
    public String getId() {
        return id;
    }

    public ActivityType[] getActivityTypes() {
        return activityTypes;
    }

    @Override
    public String toString() {
        return "Station{" +
                "id='" + id + '\'' +
                ", activityTypes=" + Arrays.toString(activityTypes) +
                '}';
    }
}
