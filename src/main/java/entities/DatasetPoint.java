package entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "dataset_points")
@IdClass(DatasetPointId.class)
public class DatasetPoint {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private TabulatedDataset dataset;

    @Id
    @Column(name = "point_index", nullable = false)
    private Integer pointIndex;

    @Column(name = "x_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal xValue;

    @Column(name = "y_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal yValue;

    public DatasetPoint() {
    }

    public DatasetPoint(TabulatedDataset dataset, Integer pointIndex, BigDecimal xValue, BigDecimal yValue) {
        this.dataset = dataset;
        this.pointIndex = pointIndex;
        this.xValue = xValue;
        this.yValue = yValue;
    }

    public TabulatedDataset getDataset() {
        return dataset;
    }

    public void setDataset(TabulatedDataset dataset) {
        this.dataset = dataset;
    }

    public Integer getPointIndex() {
        return pointIndex;
    }

    public void setPointIndex(Integer pointIndex) {
        this.pointIndex = pointIndex;
    }

    public BigDecimal getXValue() {
        return xValue;
    }

    public void setXValue(BigDecimal xValue) {
        this.xValue = xValue;
    }

    public BigDecimal getYValue() {
        return yValue;
    }

    public void setYValue(BigDecimal yValue) {
        this.yValue = yValue;
    }
}
