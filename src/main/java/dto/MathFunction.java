package dto;

import java.util.Objects;

public class MathFunction {
    private Long id;
    private Long ownerId;
    private String name;
    private String functionType;
    private String definitionBody;

    public MathFunction() {}

    public MathFunction(Long id, Long ownerId, String name, String functionType, String definitionBody) {
        this.id = id;
        this.ownerId = ownerId;
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
    public Long getOwnerId() {
        return ownerId;
    }
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getFunctionType() {
        return functionType;
    }
    public void setFunctionType(String functionType) {
        this.functionType = functionType;
    }
    public String getDefinitionBody() {
        return definitionBody;
    }
    public void setDefinitionBody(String definitionBody) {
        this.definitionBody = definitionBody;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MathFunction that = (MathFunction) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(ownerId, that.ownerId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(functionType, that.functionType) &&
                Objects.equals(definitionBody, that.definitionBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ownerId, name, functionType, definitionBody);
    }

    @Override
    public String toString() {
        return "Математическая функция{id = " + id + ", id пользователя = " + ownerId +
                ", название = '" + name + "', тип функции = '" + functionType +
                "', описание = '" + definitionBody + "'}";
    }
}