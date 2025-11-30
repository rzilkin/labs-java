package dto;

import java.time.Instant;

public class FunctionSummaryDto {
    private Long id;
    private String name;
    private String type;
    private Long ownerId;
    private Long datasetId;
    private String sourceType;
    private Instant createdAt;

    public FunctionSummaryDto() {
    }

    public FunctionSummaryDto(Long id,
                              String name,
                              String type,
                              Long ownerId,
                              Long datasetId,
                              String sourceType,
                              Instant createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.ownerId = ownerId;
        this.datasetId = datasetId;
        this.sourceType = sourceType;
        this.createdAt = createdAt;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}