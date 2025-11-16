package entities;

import java.io.Serializable;
import java.util.Objects;

public class FunctionComponentId implements Serializable {
    private Long composite;
    private Short position;

    public FunctionComponentId() {
    }

    public FunctionComponentId(Long composite, Short position) {
        this.composite = composite;
        this.position = position;
    }

    public Long getComposite() {
        return composite;
    }

    public void setComposite(Long composite) {
        this.composite = composite;
    }

    public Short getPosition() {
        return position;
    }

    public void setPosition(Short position) {
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FunctionComponentId that = (FunctionComponentId) o;
        return Objects.equals(composite, that.composite) && Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(composite, position);
    }
}
