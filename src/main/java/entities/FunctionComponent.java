package entities;

import jakarta.persistence.*;

@Entity
@Table(name = "function_components", uniqueConstraints = @UniqueConstraint(columnNames = { "composite_id",
        "component_id" }))
@IdClass(FunctionComponentId.class)
public class FunctionComponent {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "composite_id", nullable = false)
    private MathFunction composite;

    @Id
    @Column(name = "position", nullable = false)
    private Short position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private MathFunction component;

    public FunctionComponent() {
    }

    public FunctionComponent(MathFunction composite, MathFunction component, Short position) {
        this.composite = composite;
        this.component = component;
        this.position = position;
    }

    public MathFunction getComposite() {
        return composite;
    }

    public void setComposite(MathFunction composite) {
        this.composite = composite;
    }

    public Short getPosition() {
        return position;
    }

    public void setPosition(Short position) {
        this.position = position;
    }

    public MathFunction getComponent() {
        return component;
    }

    public void setComponent(MathFunction component) {
        this.component = component;
    }
}
