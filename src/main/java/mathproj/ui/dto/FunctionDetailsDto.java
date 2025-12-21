package mathproj.ui.dto;

public class FunctionDetailsDto {
    public String id;
    public String name;
    public int pointsCount;

    public double xMin;
    public double xMax;
    public double yMin;
    public double yMax;

    public boolean insertable;
    public boolean removable;

    public FunctionDetailsDto(String id, String name, int pointsCount,
                              double xMin, double xMax,
                              double yMin, double yMax,
                              boolean insertable, boolean removable) {
        this.id = id;
        this.name = name;
        this.pointsCount = pointsCount;
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.insertable = insertable;
        this.removable = removable;
    }
}

