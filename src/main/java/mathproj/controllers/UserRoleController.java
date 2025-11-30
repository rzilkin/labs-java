package mathproj.controllers;

import mathproj.api.ApiService;
import mathproj.dto.UserRole;
import mathproj.entities.User;
import mathproj.entities.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/user-roles")
public class UserRoleController {

    private final ApiService apiService;

    public UserRoleController(ApiService apiService) {
        this.apiService = apiService;
    }

    @GetMapping
    public ResponseEntity<List<UserRole>> getAllUserRoles() {
        List<mathproj.entities.UserRole> entities = apiService.getAllUserRoles();
        List<UserRole> dtos = entities.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<UserRole> assignRole(@RequestBody UserRole dto) {
        if (dto.getUserId() == null || dto.getRoleCode() == null) {
            return ResponseEntity.badRequest().build();
        }
        if (apiService.existsUserRole(dto.getUserId(), dto.getRoleCode())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        mathproj.entities.UserRole entity = new mathproj.entities.UserRole();
        User userRef = new User();
        userRef.setId(dto.getUserId());
        entity.setUser(userRef);
        Role roleRef = new Role();
        roleRef.setCode(dto.getRoleCode());
        entity.setRole(roleRef);

        mathproj.entities.UserRole saved = apiService.saveUserRole(entity);
        return ResponseEntity.status(201).body(toDto(saved));
    }

    @DeleteMapping("/{userId}/{roleCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUserRole(@PathVariable Long userId, @PathVariable String roleCode) {
        mathproj.entities.UserRoleId id = new mathproj.entities.UserRoleId(userId, roleCode);
        apiService.deleteUserRole(id);
    }

    private UserRole toDto(mathproj.entities.UserRole e) {
        if (e == null || e.getUser() == null || e.getRole() == null) return null;
        return new UserRole(e.getUser().getId(), e.getRole().getCode());
    }
}

