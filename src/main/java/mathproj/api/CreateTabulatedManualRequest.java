package mathproj.api;

import mathproj.dto.PointDto;
import java.util.List;

public record CreateTabulatedManualRequest(String name, List<PointDto> points) {}
