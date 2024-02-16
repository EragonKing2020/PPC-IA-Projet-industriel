package data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class Shift {
    private final String id;

    private final LocalDateTime start;

    private final LocalDateTime end;

    @JsonCreator
    public Shift(
            @JsonProperty("id") String id,
            @JsonProperty("start") LocalDateTime start,
            @JsonProperty("end") LocalDateTime end
    ) {
        this.id = id;
        this.start = start;
        this.end = end;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "Shift{" +
                "id='" + id + '\'' +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
