package mathproj.controllers;

import mathproj.api.ApiService;
import mathproj.dto.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final ApiService apiService;

    public RoleController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        List<mathproj.entities.Role> entities = apiService.getAllRoles();
        List<Role> dtos = entities.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{code}")
    public ResponseEntity<Role> getRoleByCode(@PathVariable String code) {
        Optional<mathproj.entities.Role> entity = apiService.getRoleByCode(code);
        return entity.map(this::toDto).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role dto) {
        mathproj.entities.Role entity = toEntity(dto);
        mathproj.entities.Role saved = apiService.saveRole(entity);
        return ResponseEntity.status(201).body(toDto(saved));
    }

    @PutMapping("/{code}")
    public ResponseEntity<Role> updateRole(@PathVariable String code, @RequestBody Role dto) {
        if (!code.equals(dto.getCode())) {
            return ResponseEntity.badRequest().build();
        }
        mathproj.entities.Role entity = toEntity(dto);
        mathproj.entities.Role saved = apiService.saveRole(entity);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable String code) {
        apiService.deleteRole(code);
    }

    private Role toDto(mathproj.entities.Role e) {
        return e == null ? null : new Role(e.getCode(), e.getDescription());
    }

    private mathproj.entities.Role toEntity(Role dto) {
        return dto == null ? null : new mathproj.entities.Role(dto.getCode(), dto.getDescription());
    }
}

