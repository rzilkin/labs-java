package entities;

import jakarta.persistence.*;

@Entity
@Table(name = "performance_metrics")
public class PerformanceMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "engine", nullable = false, length = 32)
    private Engine engine;

    @Column(name = "operation", nullable = false, length = 255)
    private String operation;

    @Column(name = "records_processed", nullable = false)
    private Integer recordsProcessed;

    @Column(name = "elapsed_ms", nullable = false)
    private Integer elapsedMs;

    public enum Engine {
        MANUAL_JDBC,
        FRAMEWORK_ORM
    }

    public PerformanceMetric() {
    }

    public PerformanceMetric(Engine engine, String operation, Integer recordsProcessed, Integer elapsedMs) {
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

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
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
}
