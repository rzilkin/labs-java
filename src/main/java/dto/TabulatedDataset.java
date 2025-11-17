package dto;

import java.util.Objects;

public class TabulatedDataset {
    private Long id;
    private Long functionId;
    private String sourceType;

    public TabulatedDataset() {}

    public TabulatedDataset(Long id, Long functionId, String sourceType) {
        this.id = id;
        this.functionId = functionId;
        this.sourceType = sourceType;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getFunctionId() {
        return functionId;
    }
    public void setFunctionId(Long functionId) {
        this.functionId = functionId;
    }
    public String getSourceType() {
        return sourceType;
    }
    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TabulatedDataset dataset = (TabulatedDataset) o;
        return Objects.equals(id, dataset.id) &&
                Objects.equals(functionId, dataset.functionId) &&
                Objects.equals(sourceType, dataset.sourceType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, functionId, sourceType);
    }

    @Override
    public String toString() {
        return "Набор данных{id = " + id + ", id функции = " + functionId +
                ", тип данных = '" + sourceType + "'}";
    }
}