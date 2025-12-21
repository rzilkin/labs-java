package mathproj.ui.controllers;

import mathproj.functions.Insertable;
import mathproj.functions.MathFunction;
import mathproj.functions.Removable;
import mathproj.functions.TabulatedFunction;
import mathproj.ui.dto.*;
import mathproj.ui.registry.MathFunctionRegistry;
import mathproj.ui.service.CurrentFunctionService;
import mathproj.ui.service.FunctionStoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ui")
public class TabulatedFunctionUiController {

    private final CurrentFunctionService current;
    private final FunctionStoreService store;

    public TabulatedFunctionUiController(CurrentFunctionService current, FunctionStoreService store) {
        this.current = current;
        this.store = store;
    }

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

        TabulatedFunction f = current.createFromPoints(x, y);
        String id = store.add(req.name, f);
        return ResponseEntity.ok(new CreateResultDto(id, req.name, f.getCount()));
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
        TabulatedFunction f = current.createFromMathFunction(base, req.xFrom, req.xTo, req.count);
        String id = store.add(req.name, f);
        return ResponseEntity.ok(new CreateResultDto(id, req.name, f.getCount()));
    }

    @GetMapping("/tabulated/current")
    public TabulatedFunctionDto getCurrent() {
        TabulatedFunction f = current.getOrThrow();

        List<PointDto> pts = new ArrayList<>();
        for (int i = 0; i < f.getCount(); i++) {
            pts.add(new PointDto(f.getX(i), f.getY(i)));
        }

        boolean insertable = f instanceof Insertable;
        boolean removable = f instanceof Removable;

        return new TabulatedFunctionDto("current", pts, insertable, removable);
    }

    @GetMapping("/tabulated/current/apply")
    public double apply(@RequestParam("x") double x) {
        TabulatedFunction f = current.getOrThrow();
        return f.apply(x);
    }

    @GetMapping("/tabulated/current/serialize")
    public String serialize() {
        return current.serializeToBase64();
    }

    @PostMapping("/tabulated/current/deserialize")
    public TabulatedFunctionDto deserialize(@RequestBody DeserializeRequest req) {
        if (req.base64 == null || req.base64.isBlank()) {
            throw new IllegalArgumentException("base64 пустой");
        }

        TabulatedFunction f = current.deserializeFromBase64(req.base64);

        List<PointDto> pts = new ArrayList<>();
        for (int i = 0; i < f.getCount(); i++) {
            pts.add(new PointDto(f.getX(i), f.getY(i)));
        }

        boolean insertable = f instanceof Insertable;
        boolean removable = f instanceof Removable;

        return new TabulatedFunctionDto("current", pts, insertable, removable);
    }

    @PostMapping("/tabulated/current/insert")
    public TabulatedFunctionDto insert(@RequestParam("x") double x, @RequestParam("y") double y) {
        TabulatedFunction f = current.getOrThrow();
        if (!(f instanceof Insertable ins)) {
            throw new IllegalStateException("Функция не поддерживает вставку (Insertable).");
        }

        ins.insert(x, y);
        return getCurrent();
    }

    @DeleteMapping("/tabulated/current/remove")
    public TabulatedFunctionDto remove(@RequestParam("index") int index) {
        TabulatedFunction f = current.getOrThrow();
        if (!(f instanceof Removable rem)) {
            throw new IllegalStateException("Функция не поддерживает удаление (Removable).");
        }

        rem.remove(index);
        return getCurrent();
    }
}





