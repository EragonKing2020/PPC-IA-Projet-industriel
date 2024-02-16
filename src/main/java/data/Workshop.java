package data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class Workshop {
    private final String id;

    private final Shift[] shifts;

    private final Station[] stations;

    private final Worker[] workers;

    private final Furniture[] furnitures;

    @JsonCreator
    public Workshop(
            @JsonProperty("id") String id,
            @JsonProperty("shifts") Shift[] shifts,
            @JsonProperty("stations") Station[] stations,
            @JsonProperty("workers") Worker[] workers,
            @JsonProperty("furnitures") Furniture[] furnitures
    ) {
        this.id = id;
        this.shifts = shifts;
        this.stations = stations;
        this.workers = workers;
        this.furnitures = furnitures;
    }

    public String getId() {
        return id;
    }

    public Shift[] getShifts() {
        return shifts;
    }

    public Station[] getStations() {
        return stations;
    }

    public Worker[] getWorkers() {
        return workers;
    }

    public Furniture[] getFurnitures() {
        return furnitures;
    }

    @Override
    public String toString() {
        return "Workshop{" +
                "id='" + id + '\'' +
                ", shifts=" + Arrays.toString(shifts) +
                ", stations=" + Arrays.toString(stations) +
                ", workers=" + Arrays.toString(workers) +
                ", furnitures=" + Arrays.toString(furnitures) +
                '}';
    }
}
