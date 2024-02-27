package data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class Station {
    private final String id;
    private final ActivityType[] activityTypes;
    private final int intId;
    private static int intIdStations = -1;

    @JsonCreator
    public Station(
            @JsonProperty("id") String id,
            @JsonProperty("activityTypes") ActivityType[] activityTypes
    ) {
        this.id = id;
        this.activityTypes = activityTypes;
        this.intId = Station.getNextId();
    }
    
    private static int getNextId() {
    	intIdStations += 1;
    	return intIdStations;
    }
    
    public int getIntId() {
    	return this.intId;
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
