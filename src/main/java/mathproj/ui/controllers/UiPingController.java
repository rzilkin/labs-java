package mathproj.ui.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UiPingController {
    @GetMapping("/api/v1/ui/ping")
    public String ping() {
        return "UI Бэк живой";
    }

    @GetMapping("/api/v1/ui/crash")
    public String crash() {
        throw new IllegalArgumentException("Пример ошибки: неправильно введены данные");
    }
}

