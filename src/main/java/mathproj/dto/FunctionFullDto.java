package mathproj.dto;

import java.util.ArrayList;
import java.util.List;

public class FunctionFullDto {
    private FunctionSummaryDto summary;
    private List<PointDto> points = new ArrayList<>();
    private List<Long> components = new ArrayList<>();
    private String analyticExpression;

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
