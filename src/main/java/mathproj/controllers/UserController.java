package mathproj.controllers;

import mathproj.api.ApiService;
import mathproj.dto.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final ApiService apiService;

    public UserController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<mathproj.entities.User> entities = apiService.getAllUsers();
        List<User> dtos = entities.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<mathproj.entities.User> entity = apiService.getUserById(id);
        return entity.map(this::toDto).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User dto) {
        mathproj.entities.User entity = toEntity(dto);
        mathproj.entities.User saved = apiService.saveUser(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User dto) {
        if (!id.equals(dto.getId())) {
            return ResponseEntity.badRequest().build();
        }
        mathproj.entities.User entity = toEntity(dto);
        mathproj.entities.User saved = apiService.saveUser(entity);
        return ResponseEntity.ok(toDto(saved));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        apiService.deleteUser(id);
    }

    private User toDto(mathproj.entities.User e) {
        return e == null ? null : new User(e.getId(), e.getUsername(), e.getPasswordHash());
    }

    private mathproj.entities.User toEntity(User dto) {
        if (dto == null) return null;
        mathproj.entities.User e = new mathproj.entities.User();
        e.setId(dto.getId());
        e.setUsername(dto.getUsername());
        e.setPasswordHash(dto.getPasswordHash());
        return e;
    }
}

