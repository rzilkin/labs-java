package mathproj.ui.dto;

import java.util.List;

public class TabulatedFunctionDto {
    public String name;
    public List<PointDto> points;
    public boolean insertable;
    public boolean removable;

    public TabulatedFunctionDto(String name, List<PointDto> points, boolean insertable, boolean removable) {
        this.name = name;
        this.points = points;
        this.insertable = insertable;
        this.removable = removable;
    }
}

