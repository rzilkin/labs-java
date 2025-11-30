package mathproj.controllers;

import mathproj.api.ApiService;
import mathproj.dto.PerformanceMetrics;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/performance-metrics")
public class PerformanceMetricController {

    private final ApiService apiService;

    public PerformanceMetricController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping
    public ResponseEntity<List<PerformanceMetrics>> getAllMetrics() {
        List<mathproj.entities.PerformanceMetric> entities = apiService.getAllPerformanceMetrics();
        List<PerformanceMetrics> dtos = entities.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PerformanceMetrics> getMetricById(@PathVariable Long id) {
        Optional<mathproj.entities.PerformanceMetric> entity = apiService.getPerformanceMetricById(id);
        return entity.map(this::toDto).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PerformanceMetrics> createMetric(@RequestBody PerformanceMetrics dto) {
        mathproj.entities.PerformanceMetric entity = toEntity(dto);
        mathproj.entities.PerformanceMetric saved = apiService.savePerformanceMetric(entity);
        return ResponseEntity.status(201).body(toDto(saved));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMetric(@PathVariable Long id) {
        apiService.deletePerformanceMetric(id);
    }

    private PerformanceMetrics toDto(mathproj.entities.PerformanceMetric e) {
        if (e == null) return null;
        return new PerformanceMetrics(
                e.getId(),
                e.getEngine() != null ? e.getEngine().name() : null,
                e.getOperation(),
                e.getRecordsProcessed(),
                e.getElapsedMs()
        );
    }

    private mathproj.entities.PerformanceMetric toEntity(PerformanceMetrics dto) {
        if (dto == null) return null;
        mathproj.entities.PerformanceMetric e = new mathproj.entities.PerformanceMetric();
        e.setId(dto.getId());
        if (dto.getEngine() != null) {
            try {
                e.setEngine(mathproj.entities.PerformanceMetric.Engine.valueOf(dto.getEngine()));
            } catch (IllegalArgumentException ex) {
                e.setEngine(mathproj.entities.PerformanceMetric.Engine.MANUAL_JDBC);
            }
        }
        e.setOperation(dto.getOperation());
        e.setRecordsProcessed(dto.getRecordsProcessed());
        e.setElapsedMs(dto.getElapsedMs());
        return e;
    }
}

