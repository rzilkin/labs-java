package dto;

import java.util.Objects;

public class PerfomanceMetrics {
    private Long id;
    private String engine;
    private String operation;
    private Integer recordsProcessed;
    private Integer elapsedMs;

    public PerfomanceMetrics() {}

    public PerfomanceMetrics(Long id, String engine, String operation, Integer recordsProcessed, Integer elapsedMs) {
        this.id = id;
        this.engine = engine;
        this.operation = operation;
        this.recordsProcessed = recordsProcessed;
        this.elapsedMs = elapsedMs;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerfomanceMetrics that = (PerfomanceMetrics) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(engine, that.engine) &&
                Objects.equals(operation, that.operation) &&
                Objects.equals(recordsProcessed, that.recordsProcessed) &&
                Objects.equals(elapsedMs, that.elapsedMs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, engine, operation, recordsProcessed, elapsedMs);
    }

    @Override
    public String toString() {
        return "Проверка мощности{id = " + id + ", движок = '" + engine + "', операция = '" + operation +
                "', количество обработанных запросов = " + recordsProcessed + ", время = " + elapsedMs + '}';
    }
}