package mathproj.ui.controllers;

import mathproj.ui.dto.CreateFromPointsRequest;
import mathproj.ui.dto.CreateResultDto;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ui")
public class TabulatedFunctionUiController {

    @PostMapping("/tabulated/from-points")
    public ResponseEntity<CreateResultDto> createFromPoints(@RequestBody CreateFromPointsRequest req) {

        if (req.name == null || req.name.isBlank()) {
            throw new IllegalArgumentException("Имя функции не должно быть пустым.");
        }
        if (req.points == null) {
            throw new IllegalArgumentException("Список точек отсутствует.");
        }
        if (req.points.size() < 2) {
            throw new IllegalArgumentException("Нужно минимум 2 точки.");
        }
        if (req.points.size() > 500) {
            throw new IllegalArgumentException("Слишком много точек. Максимум: 500.");
        }

        return ResponseEntity.ok(new CreateResultDto(req.name, req.points.size()));
    }
}

