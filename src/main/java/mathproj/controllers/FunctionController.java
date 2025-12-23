package mathproj.controllers;

import mathproj.dto.FunctionFullDto;
import mathproj.dto.FunctionSummaryDto;
import mathproj.dto.PointDto;
import mathproj.api.*;
import mathproj.security.AuthCurrent;
import mathproj.store.InMemoryFunctionStore;
import mathproj.service.AnalyticExpressionEvaluator;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/functions")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class FunctionController {
    private static final Logger log = LoggerFactory.getLogger(FunctionController.class);
    private final InMemoryFunctionStore store;
    private final AnalyticExpressionEvaluator evaluator;

    public FunctionController(InMemoryFunctionStore store, AnalyticExpressionEvaluator evaluator) {
        this.store = store;
        this.evaluator = evaluator;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FunctionSummaryDto> list(Authentication auth,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        long start = System.currentTimeMillis();
        long userId = AuthCurrent.userId(auth);

        List<FunctionSummaryDto> all = store.list(userId, type, search);
        int from = Math.max(0, page * size);
        int to = Math.min(all.size(), from + size);
        List<FunctionSummaryDto> out = from >= all.size() ? List.of() : all.subList(from, to);

        log.info(
                "Получение списка функций: инициатор={} uid={} тип={} поиск={} страница={} размер={} -> {} шт. ({} мс)",
                auth.getName(), userId, type, search, page, size, out.size(), System.currentTimeMillis() - start);
        return out;
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public FunctionFullDto get(Authentication auth,
            @PathVariable long id,
            @RequestParam(required = false) Double from,
            @RequestParam(required = false) Double to) {
        long start = System.currentTimeMillis();
        long userId = AuthCurrent.userId(auth);
        FunctionFullDto dto = store.get(userId, id);

        // Если функция ANALYTIC и указаны параметры from/to, генерируем точки
        if ("ANALYTIC".equalsIgnoreCase(dto.getSummary().getType())
                && from != null && to != null) {

            if (from >= to) {
                log.warn("Некорректный диапазон для функции id={}: from={} >= to={}", id, from, to);
                dto.setPoints(new ArrayList<>());
            } else {
                try {
                    List<PointDto> points = generatePointsForAnalytic(
                            dto.getAnalyticExpression(), from, to);
                    dto.setPoints(points);
                    log.info("Сгенерировано {} точек для аналитической функции id={} в диапазоне [{}, {}]",
                            points.size(), id, from, to);
                } catch (Exception e) {
                    log.error("Ошибка генерации точек для аналитической функции id={}: {}",
                            id, e.getMessage(), e);
                    dto.setPoints(new ArrayList<>());
                }
            }
        }

        log.info("Получение функции id={}: инициатор={} uid={} -> ok ({} мс)",
                id, auth.getName(), userId, System.currentTimeMillis() - start);
        return dto;
    }

    /**
     * Генерирует точки для аналитической функции в указанном диапазоне.
     * 
     * @param expression математическое выражение
     * @param from       начало диапазона
     * @param to         конец диапазона
     * @return список точек (200 точек равномерно распределенных)
     */
    private List<PointDto> generatePointsForAnalytic(String expression, double from, double to) {
        List<PointDto> points = new ArrayList<>();
        int count = evaluator.getPointCount();
        double step = (to - from) / (count - 1);

        for (int i = 0; i < count; i++) {
            double x = from + i * step;
            try {
                double y = evaluator.evaluate(expression, x);

                // Пропускаем точки с NaN или Infinity
                if (Double.isFinite(y)) {
                    points.add(new PointDto(x, y));
                } else {
                    log.debug("Пропущена точка x={} (результат: {})", x, y);
                }
            } catch (ScriptException e) {
                log.debug("Ошибка вычисления в точке x={}: {}", x, e.getMessage());
                // Пропускаем точку при ошибке
            } catch (Exception e) {
                log.debug("Неожиданная ошибка при вычислении x={}: {}", x, e.getMessage());
                // Пропускаем точку при ошибке
            }
        }

        return points;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable long id) {
        long start = System.currentTimeMillis();
        long userId = AuthCurrent.userId(auth);
        store.delete(userId, id);
        log.info("Удаление функции id={}: инициатор={} uid={} -> 204 ({} мс)",
                id, auth.getName(), userId, System.currentTimeMillis() - start);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/analytic", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FunctionFullDto> createAnalytic(Authentication auth,
            @RequestBody CreateAnalyticRequest body) {
        long start = System.currentTimeMillis();
        long userId = AuthCurrent.userId(auth);

        FunctionFullDto created = store.createAnalytic(userId, body.name(), body.expression());

        log.info("Создание аналитической функции: инициатор={} uid={} -> создана id={} ({} мс)",
                auth.getName(), userId, created.getSummary().getId(), System.currentTimeMillis() - start);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping(value = "/analytic/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public FunctionFullDto updateAnalytic(Authentication auth, @PathVariable long id,
            @RequestBody UpdateAnalyticRequest body) {
        long start = System.currentTimeMillis();
        long userId = AuthCurrent.userId(auth);

        FunctionFullDto updated = store.updateAnalytic(userId, id, body.expression());

        log.info("Обновление аналитической функции id={}: инициатор={} uid={} -> ok ({} мс)",
                id, auth.getName(), userId, System.currentTimeMillis() - start);
        return updated;
    }

    @PostMapping(value = "/tabulated/manual", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FunctionFullDto> createTabulatedManual(Authentication auth,
            @RequestBody CreateTabulatedManualRequest body) {
        long start = System.currentTimeMillis();
        long userId = AuthCurrent.userId(auth);

        FunctionFullDto created = store.createTabulatedManual(userId, body.name(), body.points());

        log.info("Создание табулированной функции (вручную): инициатор={} uid={} -> создана id={} ({} мс)",
                auth.getName(), userId, created.getSummary().getId(), System.currentTimeMillis() - start);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping(value = "/tabulated/from-function", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FunctionFullDto> createTabulatedFromFunction(Authentication auth,
            @RequestBody CreateTabulatedFromFunctionRequest body) {
        long start = System.currentTimeMillis();
        long userId = AuthCurrent.userId(auth);

        int count = body.count() == null ? 0 : body.count();
        FunctionFullDto created = store.createTabulatedFromFunction(
                userId, body.name(), body.sourceFunctionId(), count, body.from(), body.to());

        log.info(
                "Создание табулированной функции (из другой): инициатор={} uid={} источник={} -> создана id={} ({} мс)",
                auth.getName(), userId, body.sourceFunctionId(), created.getSummary().getId(),
                System.currentTimeMillis() - start);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping(value = "/composite", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FunctionFullDto> createComposite(Authentication auth,
            @RequestBody CreateCompositeRequest body) {
        long start = System.currentTimeMillis();
        long userId = AuthCurrent.userId(auth);

        FunctionFullDto created = store.createComposite(userId, body.name(), body.componentIds());

        log.info("Создание составной функции: инициатор={} uid={} -> создана id={} ({} мс)",
                auth.getName(), userId, created.getSummary().getId(), System.currentTimeMillis() - start);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    public record CreateCompositeRequest(String name, List<Long> componentIds) {
    }

    @PutMapping(value = "/{id}/name", produces = MediaType.APPLICATION_JSON_VALUE)
    public FunctionSummaryDto rename(Authentication auth, @PathVariable long id, @RequestBody RenameRequest body) {
        long start = System.currentTimeMillis();
        long userId = AuthCurrent.userId(auth);

        FunctionSummaryDto updated = store.rename(userId, id, body.name());

        log.info("Переименование функции id={}: инициатор={} uid={} -> ok ({} мс)",
                id, auth.getName(), userId, System.currentTimeMillis() - start);
        return updated;
    }

    @GetMapping(value = "/{id}/components", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Long> getComponents(Authentication auth, @PathVariable long id) {
        long start = System.currentTimeMillis();
        long userId = AuthCurrent.userId(auth);

        List<Long> comps = store.getComponents(userId, id);

        log.info("Получение компонентов функции id={}: инициатор={} uid={} -> {} шт. ({} мс)",
                id, auth.getName(), userId, comps.size(), System.currentTimeMillis() - start);
        return comps;
    }

    @PostMapping(value = "/{id}/components")
    public ResponseEntity<Void> addComponent(Authentication auth, @PathVariable long id,
            @RequestBody ComponentOperationRequest body) {
        long start = System.currentTimeMillis();
        long userId = AuthCurrent.userId(auth);

        if (body == null || body.componentId() == null) {
            throw new IllegalArgumentException("componentId обязателен");
        }
        store.addComponent(userId, id, body.componentId(), body.position());

        log.info("Добавление компонента: функция id={} инициатор={} uid={} компонент={} позиция={} -> 201 ({} мс)",
                id, auth.getName(), userId, body.componentId(), body.position(), System.currentTimeMillis() - start);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping(value = "/{id}/components")
    public ResponseEntity<Void> deleteComponent(Authentication auth, @PathVariable long id,
            @RequestBody DeleteComponentRequest body) {
        long start = System.currentTimeMillis();
        long userId = AuthCurrent.userId(auth);

        if (body == null || body.componentId() == null) {
            throw new IllegalArgumentException("componentId обязателен");
        }
        store.removeComponent(userId, id, body.componentId());

        log.info("Удаление компонента: функция id={} инициатор={} uid={} компонент={} -> 204 ({} мс)",
                id, auth.getName(), userId, body.componentId(), System.currentTimeMillis() - start);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{id}/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public FunctionFullDto exportFn(Authentication auth, @PathVariable long id) {
        long userId = AuthCurrent.userId(auth);
        log.info("Экспорт функции id={}: инициатор={} uid={}", id, auth.getName(), userId);
        return store.get(userId, id);
    }

    @PostMapping(value = "/import", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FunctionFullDto> importFn(Authentication auth, @RequestBody FunctionFullDto body) {
        long start = System.currentTimeMillis();
        long userId = AuthCurrent.userId(auth);

        if (body == null || body.getSummary() == null)
            throw new IllegalArgumentException("Неверное тело для импорта");
        String type = body.getSummary().getType();
        String name = body.getSummary().getName();

        FunctionFullDto created;
        if ("ANALYTIC".equalsIgnoreCase(type)) {
            created = store.createAnalytic(userId, name, body.getAnalyticExpression());
        } else if ("COMPOSITE".equalsIgnoreCase(type)) {
            created = store.createComposite(userId, name, body.getComponents());
        } else {
            created = store.createTabulatedManual(userId, name, body.getPoints());
        }

        log.info("Импорт функции: инициатор={} uid={} -> создана id={} ({} мс)",
                auth.getName(), userId, created.getSummary().getId(), System.currentTimeMillis() - start);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}