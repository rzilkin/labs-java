package mathproj.ui.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UiPingController {
    @GetMapping("/api/ui/ping")
    public String ping() {
        return "UI backend is alive";
    }

    @GetMapping("/api/ui/crash")
    public String crash() {
        throw new IllegalArgumentException("Пример ошибки: неправильно введены данные");
    }
}

