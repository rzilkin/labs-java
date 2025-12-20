package mathproj.ui.controllers;

import mathproj.functions.MathFunction;
import mathproj.functions.TabulatedFunction;
import mathproj.functions.factory.ArrayTabulatedFunctionFactory;
import mathproj.functions.factory.TabulatedFunctionFactory;
import mathproj.ui.dto.CreateFromMathFunctionRequest;
import mathproj.ui.dto.CreateFromPointsRequest;
import mathproj.ui.dto.CreateResultDto;
import mathproj.ui.registry.MathFunctionRegistry;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ui")
public class TabulatedFunctionUiController {
    private final TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();

    @GetMapping("/math-functions")
    public List<String> mathFunctions() {
        return MathFunctionRegistry.functions().keySet().stream().toList();
    }

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

        double[] x = new double[req.points.size()];
        double[] y = new double[req.points.size()];
        for (int i = 0; i < req.points.size(); i++) {
            x[i] = req.points.get(i).getX();
            y[i] = req.points.get(i).getY();
        }
        TabulatedFunction f = factory.create(x, y);
        return ResponseEntity.ok(new CreateResultDto(req.name, f.getCount()));
    }

    @PostMapping("/tabulated/from-math-function")
    public ResponseEntity<CreateResultDto> createFromMath(@RequestBody CreateFromMathFunctionRequest req) {

        if (req.name == null || req.name.isBlank()) {
            throw new IllegalArgumentException("Имя функции не должно быть пустым.");
        }
        if (req.count < 2) {
            throw new IllegalArgumentException("Количество точек должно быть >= 2.");
        }
        if (req.count > 5000) {
            throw new IllegalArgumentException("Слишком много точек. Максимум: 5000.");
        }
        if (!(req.xFrom < req.xTo)) {
            throw new IllegalArgumentException("xFrom должен быть меньше xTo.");
        }

        var map = MathFunctionRegistry.functions();
        var supplier = map.get(req.mathFunctionKey);
        if (supplier == null) {
            throw new IllegalArgumentException("Неизвестная функция: " + req.mathFunctionKey);
        }

        MathFunction base = supplier.get();
        TabulatedFunction f = factory.create(base, req.xFrom, req.xTo, req.count);

        return ResponseEntity.ok(new CreateResultDto(req.name, f.getCount()));
    }
}



