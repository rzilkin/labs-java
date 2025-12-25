package dto;

import java.util.List;

public class FunctionResponse {
    private FunctionSummaryDto summary;
    private List<PointDto> points;
    private List<Long> components;
    private String analyticExpression;

    public FunctionResponse() {
    }

    public FunctionResponse(FunctionSummaryDto summary,
                            List<PointDto> points,
                            List<Long> components,
                            String analyticExpression) {
        this.summary = summary;
        this.points = points;
        this.components = components;
        this.analyticExpression = analyticExpression;
    }

    public static FunctionResponse fromFullDto(FunctionFullDto dto) {
        if (dto == null) {
            return null;
        }
        return new FunctionResponse(
                dto.toSummary(),
                dto.getPoints(),
                dto.getComponents(),
                dto.getAnalyticExpression()
        );
    }

    public FunctionFullDto toFullDto() {
        FunctionFullDto fullDto = new FunctionFullDto();
        fullDto.fromSummary(summary);
        fullDto.setPoints(points);
        fullDto.setComponents(components);
        fullDto.setAnalyticExpression(analyticExpression);
        return fullDto;
    }

    public FunctionSummaryDto getSummary() {
        return summary;
    }

    public void setSummary(FunctionSummaryDto summary) {
        this.summary = summary;
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
}
