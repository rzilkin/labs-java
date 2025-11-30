package mathproj.controllers;

import mathproj.api.ApiService;
import mathproj.dto.MathFunction;
import mathproj.entities.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/functions")
public class MathFunctionController {

    private final ApiService apiService;

    public MathFunctionController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping
    public ResponseEntity<List<MathFunction>> getAllFunctions() {
        List<mathproj.entities.MathFunction> entities = apiService.getAllFunctions();
        List<MathFunction> dtos = entities.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MathFunction> getFunctionById(@PathVariable Long id) {
        Optional<mathproj.entities.MathFunction> entity = apiService.getFunctionById(id);
        return entity.map(this::toDto).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MathFunction> createFunction(@RequestBody MathFunction dto) {
        mathproj.entities.MathFunction entity = toEntity(dto);
        mathproj.entities.MathFunction saved = apiService.saveFunction(entity);
        return ResponseEntity.status(201).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MathFunction> updateFunction(@PathVariable Long id, @RequestBody MathFunction dto) {
        if (!id.equals(dto.getId())) {
            return ResponseEntity.badRequest().build();
        }
        mathproj.entities.MathFunction entity = toEntity(dto);
        mathproj.entities.MathFunction saved = apiService.saveFunction(entity);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFunction(@PathVariable Long id) {
        apiService.deleteFunction(id);
    }

    private MathFunction toDto(mathproj.entities.MathFunction e) {
        if (e == null) return null;
        return new MathFunction(
                e.getId(),
                e.getOwner() != null ? e.getOwner().getId() : null,
                e.getName(),
                e.getFunctionType() != null ? e.getFunctionType().name() : null,
                e.getDefinitionBody()
        );
    }

    private mathproj.entities.MathFunction toEntity(MathFunction dto) {
        if (dto == null) return null;
        mathproj.entities.MathFunction e = new mathproj.entities.MathFunction();
        e.setId(dto.getId());
        if (dto.getOwnerId() != null) {
            User owner = new User();
            owner.setId(dto.getOwnerId());
            e.setOwner(owner);
        }
        e.setName(dto.getName());
        if (dto.getFunctionType() != null) {
            try {
                e.setFunctionType(mathproj.entities.MathFunction.FunctionType.valueOf(dto.getFunctionType()));
            } catch (IllegalArgumentException ex) {
                e.setFunctionType(mathproj.entities.MathFunction.FunctionType.ANALYTIC);
            }
        }
        e.setDefinitionBody(dto.getDefinitionBody());
        return e;
    }
}

