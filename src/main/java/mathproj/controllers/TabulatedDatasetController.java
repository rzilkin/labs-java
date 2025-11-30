package mathproj.controllers;

import mathproj.api.ApiService;
import mathproj.dto.TabulatedDataset;
import mathproj.entities.MathFunction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/datasets")
public class TabulatedDatasetController {

    private final ApiService apiService;

    public TabulatedDatasetController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping
    public ResponseEntity<List<TabulatedDataset>> getAllDatasets() {
        List<mathproj.entities.TabulatedDataset> entities = apiService.getAllDatasets();
        List<TabulatedDataset> dtos = entities.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TabulatedDataset> getDatasetById(@PathVariable Long id) {
        Optional<mathproj.entities.TabulatedDataset> entity = apiService.getDatasetById(id);
        return entity.map(this::toDto).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TabulatedDataset> createDataset(@RequestBody TabulatedDataset dto) {
        mathproj.entities.TabulatedDataset entity = toEntity(dto);
        mathproj.entities.TabulatedDataset saved = apiService.saveDataset(entity);
        return ResponseEntity.status(201).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TabulatedDataset> updateDataset(@PathVariable Long id, @RequestBody TabulatedDataset dto) {
        if (!id.equals(dto.getId())) {
            return ResponseEntity.badRequest().build();
        }
        mathproj.entities.TabulatedDataset entity = toEntity(dto);
        mathproj.entities.TabulatedDataset saved = apiService.saveDataset(entity);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDataset(@PathVariable Long id) {
        apiService.deleteDataset(id);
    }

    private TabulatedDataset toDto(mathproj.entities.TabulatedDataset e) {
        if (e == null) return null;
        return new TabulatedDataset(
                e.getId(),
                e.getFunction() != null ? e.getFunction().getId() : null,
                e.getSourceType() != null ? e.getSourceType().name() : null
        );
    }

    private mathproj.entities.TabulatedDataset toEntity(TabulatedDataset dto) {
        if (dto == null) return null;
        mathproj.entities.TabulatedDataset e = new mathproj.entities.TabulatedDataset();
        e.setId(dto.getId());
        if (dto.getFunctionId() != null) {
            MathFunction f = new MathFunction();
            f.setId(dto.getFunctionId());
            e.setFunction(f);
        }
        if (dto.getSourceType() != null) {
            try {
                e.setSourceType(mathproj.entities.TabulatedDataset.SourceType.valueOf(dto.getSourceType()));
            } catch (IllegalArgumentException ex) {
                e.setSourceType(mathproj.entities.TabulatedDataset.SourceType.MANUAL);
            }
        }
        return e;
    }
}

