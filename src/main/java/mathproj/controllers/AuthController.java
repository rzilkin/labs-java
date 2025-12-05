package mathproj.controllers;

import mathproj.api.RegisterRequest;
import mathproj.security.AppRole;
import mathproj.service.InMemoryUserStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final InMemoryUserStore users;

    public AuthController(InMemoryUserStore users) {
        this.users = users;
    }

    @PostMapping(
            value = "/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        long start = System.currentTimeMillis();

        if (req == null || isBlank(req.username()) || isBlank(req.password())) {
            log.warn("Регистрация: невалидные данные");
            throw new IllegalArgumentException("Имя пользователя и пароль обязательны");
        }

        var created = users.registerUser(req.username().trim(), req.password());

        log.info("Регистрация: пользователь={} -> id={} ({} мс)",
                created.getUsername(), created.getId(), System.currentTimeMillis() - start);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("id", created.getId(), "username", created.getUsername()));
    }

    @PostMapping(value = "/login")
    public ResponseEntity<Void> login(Authentication auth) {
        log.info("Вход: пользователь={}", auth != null ? auth.getName() : "<нет данных>");
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/grant", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> grantRole(@RequestParam String username,
                                       @RequestParam String role,
                                       Authentication auth) {
        long start = System.currentTimeMillis();

        if (isBlank(username) || isBlank(role)) {
            log.warn("Выдача роли: невалидные параметры, инициатор={}", auth.getName());
            throw new IllegalArgumentException("Имя пользователя и роль обязательны");
        }

        AppRole r;
        try {
            r = AppRole.valueOf(role.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Выдача роли: неизвестная роль={}, инициатор={}", role, auth.getName());
            throw new IllegalArgumentException("Неизвестная роль: " + role);
        }

        var updated = users.grantRole(username.trim(), r);

        log.info("Выдача роли: роль={} пользователю={} от инициатора={} ({} мс)",
                r, updated.getUsername(), auth.getName(), System.currentTimeMillis() - start);

        return ResponseEntity.ok(Map.of(
                "username", updated.getUsername(),
                "roles", updated.getRoles().stream().map(Enum::name).toList()
        ));
    }

    private boolean isBlank(String v) {
        return v == null || v.isBlank();
    }
}




