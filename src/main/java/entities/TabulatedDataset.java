package entities;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tabulated_datasets")
public class TabulatedDataset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id", nullable = false)
    private MathFunction function;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 16)
    private SourceType sourceType;

    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DatasetPoint> datasetPoints = new HashSet<>();

    public enum SourceType {
        MANUAL,
        GENERATED,
        DIFFERENTIATED,
        INTEGRATED
    }

    public TabulatedDataset() {
    }

    public TabulatedDataset(MathFunction function, SourceType sourceType) {
        this.function = function;
        this.sourceType = sourceType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MathFunction getFunction() {
        return function;
    }

    public void setFunction(MathFunction function) {
        this.function = function;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public Set<DatasetPoint> getDatasetPoints() {
        return datasetPoints;
    }

    public void setDatasetPoints(Set<DatasetPoint> datasetPoints) {
        this.datasetPoints = datasetPoints;
    }
}

