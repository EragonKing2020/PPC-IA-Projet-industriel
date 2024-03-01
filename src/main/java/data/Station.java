package data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class Station {
    private final String id;
    private final ActivityType[] activityTypes;

    @JsonCreator
    public Station(
            @JsonProperty("id") String id,
            @JsonProperty("activityTypes") ActivityType[] activityTypes
    ) {
        this.id = id;
        this.activityTypes = activityTypes;
    }
    
    public int getNumberId() {
    	return Integer.valueOf((String) id.subSequence(1, -1)) ;
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
