package mathproj.controllers;

import org.slf4j.*;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/engine")
public class EngineController {
    private static final Logger log = LoggerFactory.getLogger(EngineController.class);
    private String backend = "framework";

    @GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public String get(Authentication auth) {
        log.info("Получение текущего движка: инициатор={} -> {}", auth.getName(), backend);
        return backend;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String set(@RequestBody String value, Authentication auth) {
        if (!"manual".equals(value) && !"framework".equals(value)) {
            throw new IllegalArgumentException("Недопустимое значение движка");
        }
        backend = value;
        log.info("Смена движка: инициатор={} -> {}", auth.getName(), backend);
        return backend;
    }
}




