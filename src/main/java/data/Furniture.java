package data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class Furniture {
    private final String id;

    private final Activity[] activities;

    private final Activity[][] precedence;

    private final Activity[][] sequences;

    @JsonCreator
    public Furniture(
            @JsonProperty("id") String id,
            @JsonProperty("activities") Activity[] activities,
            @JsonProperty("precedence") Activity[][] precedence,
            @JsonProperty("sequences") Activity[][] sequences
    ) {
        this.id = id;
        this.activities = activities;
        this.precedence = precedence;
        this.sequences = sequences;
    }

    public String getId() {
        return id;
    }

    public Activity[] getActivities() {
        return activities;
    }

    public Activity[][] getPrecedence() {
        return precedence;
    }

    public Activity[][] getSequences() {
        return sequences;
    }

    @JsonIgnore
    public int getTotalDuration() {
        return Arrays.stream(activities).mapToInt(Activity::getDuration).sum();
    }

    @Override
    public String toString() {
        return "Furniture{" +
                "id='" + id + '\'' +
                ", activities=" + Arrays.toString(activities) +
                ", precedence=" + Arrays.deepToString(precedence) +
                ", sequences=" + Arrays.deepToString(sequences) +
                '}';
    }
}
