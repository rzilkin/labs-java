package mathproj.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "math_functions", uniqueConstraints = @UniqueConstraint(columnNames = { "owner_id", "name" }))
public class MathFunction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "function_type", nullable = false, length = 16)
    private FunctionType functionType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "definition_body", nullable = false, columnDefinition = "jsonb")
    private String definitionBody;

    @OneToMany(mappedBy = "function", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TabulatedDataset> tabulatedDatasets = new HashSet<>();

    @OneToMany(mappedBy = "composite", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FunctionComponent> compositeComponents = new HashSet<>();

    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FunctionComponent> componentOf = new HashSet<>();

    public enum FunctionType {
        ANALYTIC,
        TABULATED,
        COMPOSITE
    }

    public MathFunction() {
    }

    public MathFunction(User owner, String name, FunctionType functionType, String definitionBody) {
        this.owner = owner;
        this.name = name;
        this.functionType = functionType;
        this.definitionBody = definitionBody;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FunctionType getFunctionType() {
        return functionType;
    }

    public void setFunctionType(FunctionType functionType) {
        this.functionType = functionType;
    }

    public String getDefinitionBody() {
        return definitionBody;
    }

    public void setDefinitionBody(String definitionBody) {
        this.definitionBody = definitionBody;
    }

    public Set<TabulatedDataset> getTabulatedDatasets() {
        return tabulatedDatasets;
    }

    public void setTabulatedDatasets(Set<TabulatedDataset> tabulatedDatasets) {
        this.tabulatedDatasets = tabulatedDatasets;
    }

    public Set<FunctionComponent> getCompositeComponents() {
        return compositeComponents;
    }

    public void setCompositeComponents(Set<FunctionComponent> compositeComponents) {
        this.compositeComponents = compositeComponents;
    }

    public Set<FunctionComponent> getComponentOf() {
        return componentOf;
    }

    public void setComponentOf(Set<FunctionComponent> componentOf) {
        this.componentOf = componentOf;
    }
}
