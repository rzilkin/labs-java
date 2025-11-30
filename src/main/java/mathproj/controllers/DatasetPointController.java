package mathproj.controllers;

import mathproj.api.ApiService;
import mathproj.dto.DatasetPoint;
import mathproj.entities.TabulatedDataset;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/dataset-points")
public class DatasetPointController {

    private final ApiService apiService;

    public DatasetPointController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping
    public ResponseEntity<List<DatasetPoint>> getAllPoints() {
        List<mathproj.entities.DatasetPoint> entities = apiService.getAllDatasetPoints();
        List<DatasetPoint> dtos = entities.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{datasetId}/{pointIndex}")
    public ResponseEntity<DatasetPoint> getPoint(
            @PathVariable Long datasetId,
            @PathVariable Integer pointIndex) {
        Optional<mathproj.entities.DatasetPoint> entity = apiService.getDatasetPoint(datasetId, pointIndex);
        return entity.map(this::toDto).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DatasetPoint> createPoint(@RequestBody DatasetPoint dto) {
        if (dto.getDatasetId() == null || dto.getPointIndex() == null ||
                dto.getXValue() == null || dto.getYValue() == null) {
            return ResponseEntity.badRequest().build();
        }

        mathproj.entities.DatasetPoint entity = new mathproj.entities.DatasetPoint();
        TabulatedDataset dataset = new TabulatedDataset();
        dataset.setId(dto.getDatasetId());
        entity.setDataset(dataset);
        entity.setPointIndex(dto.getPointIndex());
        entity.setXValue(dto.getXValue());
        entity.setYValue(dto.getYValue());

        mathproj.entities.DatasetPoint saved = apiService.saveDatasetPoint(entity);
        return ResponseEntity.status(201).body(toDto(saved));
    }

    @DeleteMapping("/{datasetId}/{pointIndex}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePoint(@PathVariable Long datasetId, @PathVariable Integer pointIndex) {
        mathproj.entities.DatasetPointId id = new mathproj.entities.DatasetPointId(datasetId, pointIndex);
        apiService.deleteDatasetPoint(id);
    }

    private DatasetPoint toDto(mathproj.entities.DatasetPoint e) {
        if (e == null) return null;
        return new DatasetPoint(
                e.getDataset() != null ? e.getDataset().getId() : null,
                e.getPointIndex(),
                e.getXValue(),
                e.getYValue()
        );
    }
}


