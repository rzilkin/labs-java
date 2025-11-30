package mathproj.controllers;

import mathproj.api.ApiService;
import mathproj.dto.FunctionComponents;
import mathproj.entities.MathFunction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/function-components")
public class FunctionComponentController {

    private final ApiService apiService;

    public FunctionComponentController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping
    public ResponseEntity<List<FunctionComponents>> getAllComponents() {
        List<mathproj.entities.FunctionComponent> entities = apiService.getAllFunctionComponents();
        List<FunctionComponents> dtos = entities.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/by-composite/{compositeId}")
    public ResponseEntity<List<FunctionComponents>> getComponentsByComposite(@PathVariable Long compositeId) {
        List<mathproj.entities.FunctionComponent> entities = apiService.getComponentsByCompositeId(compositeId);
        List<FunctionComponents> dtos = entities.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<FunctionComponents> createComponent(@RequestBody FunctionComponents dto) {
        if (dto.getCompositeId() == null || dto.getComponentId() == null || dto.getPosition() == null) {
            return ResponseEntity.badRequest().build();
        }

        mathproj.entities.FunctionComponent entity = new mathproj.entities.FunctionComponent();
        MathFunction composite = new MathFunction();
        composite.setId(dto.getCompositeId());
        entity.setComposite(composite);
        MathFunction component = new MathFunction();
        component.setId(dto.getComponentId());
        entity.setComponent(component);
        entity.setPosition(dto.getPosition());

        mathproj.entities.FunctionComponent saved = apiService.saveFunctionComponent(entity);
        return ResponseEntity.status(201).body(toDto(saved));
    }

    @DeleteMapping("/{compositeId}/{position}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComponent(@PathVariable Long compositeId, @PathVariable Short position) {
        mathproj.entities.FunctionComponentId id = new mathproj.entities.FunctionComponentId(compositeId, position);
        apiService.deleteFunctionComponent(id);
    }

    private FunctionComponents toDto(mathproj.entities.FunctionComponent e) {
        if (e == null) return null;
        return new FunctionComponents(
                e.getComposite() != null ? e.getComposite().getId() : null,
                e.getComponent() != null ? e.getComponent().getId() : null,
                e.getPosition()
        );
    }
}

