package mathproj.entities;

import java.io.Serializable;
import java.util.Objects;

public class DatasetPointId implements Serializable {
    private Long dataset;
    private Integer pointIndex;

    public DatasetPointId() {
    }

    public DatasetPointId(Long dataset, Integer pointIndex) {
        this.dataset = dataset;
        this.pointIndex = pointIndex;
    }

    public Long getDataset() {
        return dataset;
    }

    public void setDataset(Long dataset) {
        this.dataset = dataset;
    }

    public Integer getPointIndex() {
        return pointIndex;
    }

    public void setPointIndex(Integer pointIndex) {
        this.pointIndex = pointIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DatasetPointId that = (DatasetPointId) o;
        return Objects.equals(dataset, that.dataset) && Objects.equals(pointIndex, that.pointIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataset, pointIndex);
    }
}
