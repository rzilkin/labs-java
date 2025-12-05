package mathproj.controllers;

import mathproj.dto.PerformanceMetrics;
import mathproj.api.SaveMetricRequest;
import mathproj.security.AuthCurrent;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/v1/metrics")
public class MetricsController {
    private static final Logger log = LoggerFactory.getLogger(MetricsController.class);

    private final List<PerformanceMetrics> metrics = new CopyOnWriteArrayList<>();
    private final AtomicLong seq = new AtomicLong(1);

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PerformanceMetrics> all(Authentication auth) {
        log.info("Получение метрик: инициатор={} -> всего записей={}", auth.getName(), metrics.size());
        return List.copyOf(metrics);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PerformanceMetrics> save(Authentication auth, @RequestBody SaveMetricRequest body) {
        long start = System.currentTimeMillis();
        long userId = AuthCurrent.userId(auth);

        if (body == null || body.operation() == null || body.recordsProcessed() == null || body.elapsedMs() == null) {
            throw new IllegalArgumentException("operation, recordsProcessed, elapsedMs обязательны");
        }

        PerformanceMetrics saved = new PerformanceMetrics(
                seq.getAndIncrement(),
                "FRAMEWORK_ORM",
                body.operation(),
                body.recordsProcessed(),
                body.elapsedMs().intValue()
        );
        metrics.add(saved);

        log.info("Сохранение метрики: инициатор={} uid={} -> создана id={} ({} мс)",
                auth.getName(), userId, saved.getId(), System.currentTimeMillis() - start);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}





