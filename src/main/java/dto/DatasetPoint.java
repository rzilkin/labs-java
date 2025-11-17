package dto;

import java.math.BigDecimal;
import java.util.Objects;

public class DatasetPoint {
    private Long datasetId;
    private Integer pointIndex;
    private BigDecimal xValue;
    private BigDecimal yValue;

    public DatasetPoint() {}

    public DatasetPoint(Long datasetId, Integer pointIndex, BigDecimal xValue, BigDecimal yValue) {
        this.datasetId = datasetId;
        this.pointIndex = pointIndex;
        this.xValue = xValue;
        this.yValue = yValue;
    }

    public Long getDatasetId() {
        return datasetId;
    }
    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DatasetPoint that = (DatasetPoint) o;
        return Objects.equals(datasetId, that.datasetId) &&
                Objects.equals(pointIndex, that.pointIndex) &&
                Objects.equals(xValue, that.xValue) &&
                Objects.equals(yValue, that.yValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datasetId, pointIndex, xValue, yValue);
    }

    @Override
    public String toString() {
        return "Точка из набора{id набора данных = " + datasetId + ", индекс точки = " + pointIndex +
                ", значение по x = " + xValue + ", значение по y = " + yValue + "}";
    }
}