package dto;

import java.time.Instant;
import java.util.Objects;

public class PerformanceMetrics {
    private Long id;
    private String engine;
    private String operation;
    private Integer recordsProcessed;
    private Integer elapsedMs;
    private Instant recordedAt;

    public PerformanceMetrics() {}

    public PerformanceMetrics(Long id, String engine, String operation, Integer recordsProcessed, Integer elapsedMs) {
        this.id = id;
        this.engine = engine;
        this.operation = operation;
        this.recordsProcessed = recordsProcessed;
        this.elapsedMs = elapsedMs;
        this.recordedAt = Instant.now();
    }

    public PerformanceMetrics(Long id, String engine, String operation, Integer recordsProcessed, Integer elapsedMs, Instant recordedAt) {
        this.id = id;
        this.engine = engine;
        this.operation = operation;
        this.recordsProcessed = recordsProcessed;
        this.elapsedMs = elapsedMs;
        this.recordedAt = recordedAt;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getEngine() {
        return engine;
    }
    public void setEngine(String engine) {
        this.engine = engine;
    }
    public String getOperation() {
        return operation;
    }
    public void setOperation(String operation) {
        this.operation = operation;
    }
    public Integer getRecordsProcessed() {
        return recordsProcessed;
    }
    public void setRecordsProcessed(Integer recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }
    public Integer getElapsedMs() {
        return elapsedMs;
    }
    public void setElapsedMs(Integer elapsedMs) {
        this.elapsedMs = elapsedMs;
    }
    public Instant getRecordedAt() {
        return recordedAt;
    }
    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerformanceMetrics that = (PerformanceMetrics) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(engine, that.engine) &&
                Objects.equals(operation, that.operation) &&
                Objects.equals(recordsProcessed, that.recordsProcessed) &&
                Objects.equals(elapsedMs, that.elapsedMs) &&
                Objects.equals(recordedAt, that.recordedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, engine, operation, recordsProcessed, elapsedMs, recordedAt);
    }

    @Override
    public String toString() {
        return "PerformanceMetrics{id=" + id + ", engine='" + engine + "', operation='" + operation +
                "', recordsProcessed=" + recordsProcessed + ", elapsedMs=" + elapsedMs +
                ", recordedAt=" + recordedAt + '}';
    }
}
