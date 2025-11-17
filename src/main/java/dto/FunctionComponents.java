package dto;

import java.util.Objects;

public class FunctionComponents {
    private Long compositeId;
    private Long componentId;
    private Short position;

    public FunctionComponents() {}

    public FunctionComponents(Long compositeId, Long componentId, Short position) {
        this.compositeId = compositeId;
        this.componentId = componentId;
        this.position = position;
    }

    public Long getCompositeId() {
        return compositeId;
    }
    public void setCompositeId(Long compositeId) {
        this.compositeId = compositeId;
    }
    public Long getComponentId() {
        return componentId;
    }
    public void setComponentId(Long componentId) {
        this.componentId = componentId;
    }
    public Short getPosition() {
        return position;
    }
    public void setPosition(Short position) {
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionComponents that = (FunctionComponents) o;
        return Objects.equals(compositeId, that.compositeId) &&
                Objects.equals(componentId, that.componentId) &&
                Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(compositeId, componentId, position);
    }

    @Override
    public String toString() {
        return "Компоненты функции{compositeId = " + compositeId +
                ", componentId = " + componentId + ", место = " + position + "}";
    }
}