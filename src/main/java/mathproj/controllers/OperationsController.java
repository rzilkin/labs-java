package mathproj.controllers;

import mathproj.dto.FunctionFullDto;
import mathproj.dto.PointDto;
import mathproj.api.BinaryOperationRequest;
import mathproj.api.IntegrateRequest;
import mathproj.security.AuthCurrent;
import mathproj.store.InMemoryFunctionStore;
import mathproj.functions.ArrayTabulatedFunction;
import mathproj.functions.DefiniteIntegral;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/operations")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class OperationsController {
    private static final Logger log = LoggerFactory.getLogger(OperationsController.class);
    private final InMemoryFunctionStore store;

    public OperationsController(InMemoryFunctionStore store) {
        this.store = store;
    }

    @PostMapping(value = "/{op}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FunctionFullDto> binary(Authentication auth, @PathVariable String op,
            @RequestBody BinaryOperationRequest body) {
        long start = System.currentTimeMillis();
        long userId = AuthCurrent.userId(auth);

        if (body == null || body.leftId() == null || body.rightId() == null) {
            throw new IllegalArgumentException("leftId и rightId обязательны");
        }

        store.get(userId, body.leftId());
        store.get(userId, body.rightId());

        FunctionFullDto created = store.cloneAsOperationResult(
                userId,
                op + "(" + body.leftId() + "," + body.rightId() + ")",
                "INTEGRATED");

        log.info("Бинарная операция op={} инициатор={} uid={} left={} right={} -> создана функция id={} ({} мс)",
                op, auth.getName(), userId, body.leftId(), body.rightId(),
                created.getSummary().getId(), System.currentTimeMillis() - start);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping(value = "/differentiate/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FunctionFullDto> differentiate(Authentication auth, @PathVariable long id) {
        long start = System.currentTimeMillis();
        long userId = AuthCurrent.userId(auth);

        store.get(userId, id);
        FunctionFullDto created = store.cloneAsOperationResult(userId, "d/dx(" + id + ")", "DIFFERENTIATED");

        log.info("Дифференцирование функции id={} инициатор={} uid={} -> создана функция id={} ({} мс)",
                id, auth.getName(), userId, created.getSummary().getId(), System.currentTimeMillis() - start);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping(value = "/integrate/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ValueResponse> integrate(Authentication auth, @PathVariable long id,
            @RequestBody(required = false) IntegrateRequest body) {
        long start = System.currentTimeMillis();
        long userId = AuthCurrent.userId(auth);

        FunctionFullDto function = store.get(userId, id);
        int threads = body == null || body.threads() == null ? 8 : body.threads();

        // Проверяем, что функция табулированная
        if (!"TABULATED".equalsIgnoreCase(function.getSummary().getType())) {
            throw new IllegalArgumentException("Интегрирование возможно только для табулированных функций");
        }

        List<PointDto> points = function.getPoints();
        if (points == null || points.size() < 2) {
            throw new IllegalArgumentException("Табулированная функция должна содержать минимум 2 точки");
        }

        // Извлекаем массивы x и y из точек
        double[] xValues = new double[points.size()];
        double[] yValues = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            PointDto point = points.get(i);
            xValues[i] = point.getX();
            yValues[i] = point.getY();
        }

        // Создаем табулированную функцию
        ArrayTabulatedFunction tabulatedFunction = new ArrayTabulatedFunction(xValues, yValues);

        // Находим область определения (min и max x)
        double minX = xValues[0];
        double maxX = xValues[xValues.length - 1];

        // Создаем DefiniteIntegral с нижним пределом = minX
        DefiniteIntegral integral = new DefiniteIntegral(tabulatedFunction, minX);

        // Вычисляем интеграл по всей области определения
        // apply(x) вычисляет интеграл от нижнего предела (minX) до x (maxX)
        double value = integral.apply(maxX);

        log.info("Интегрирование функции id={} инициатор={} uid={} потоки={} диапазон=[{}, {}] -> значение={} ({} мс)",
                id, auth.getName(), userId, threads, minX, maxX, value, System.currentTimeMillis() - start);

        return ResponseEntity.ok(new ValueResponse(value));
    }

    public record ValueResponse(double value) {
    }
}
