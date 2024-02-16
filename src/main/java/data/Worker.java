package data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Arrays;

public class Worker {
    private final String id;

    private final String shift;

    private final LocalDateTime[][] breaks;

    private final String[] stationsNames;

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

    public String getShift() {
        return shift;
    }

    public LocalDateTime[][] getBreaks() {
        return breaks;
    }

    public String[] getStations() {
        return stationsNames;
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
