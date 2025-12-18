package dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class FunctionFullDto {
    private Long id;
    private String name;
    private String type;
    private Long ownerId;
    private Instant createdAt;
    private Long datasetId;
    private String sourceType;

    private List<PointDto> points;
    private List<Long> components;
    private String analyticExpression;

    public FunctionFullDto() {
        this.points = new ArrayList<>();
        this.components = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Long getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public List<PointDto> getPoints() {
        return points;
    }

    public void setPoints(List<PointDto> points) {
        this.points = points;
    }

    public List<Long> getComponents() {
        return components;
    }

    public void setComponents(List<Long> components) {
        this.components = components;
    }

    public String getAnalyticExpression() {
        return analyticExpression;
    }

    public void setAnalyticExpression(String analyticExpression) {
        this.analyticExpression = analyticExpression;
    }

    public FunctionSummaryDto toSummary() {
        return new FunctionSummaryDto(id, name, type, ownerId, datasetId, sourceType, createdAt);
    }

    public void fromSummary(FunctionSummaryDto summary) {
        if (summary != null) {
            this.id = summary.getId();
            this.name = summary.getName();
            this.type = summary.getType();
            this.ownerId = summary.getOwnerId();
            this.datasetId = summary.getDatasetId();
            this.sourceType = summary.getSourceType();
            this.createdAt = summary.getCreatedAt();
        }
    }
}