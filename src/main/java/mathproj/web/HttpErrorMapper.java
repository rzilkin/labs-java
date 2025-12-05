package mathproj.web;

import org.springframework.http.HttpStatus;

public final class HttpErrorMapper {
    private HttpErrorMapper() {}

    public static String fromStatus(int status) {
        return switch (status) {
            case 400 -> "Неверный запрос";
            case 401 -> "Не авторизован";
            case 403 -> "Доступ запрещён";
            case 404 -> "Не найдено";
            case 409 -> "Конфликт";
            case 405 -> "Метод не поддерживается";
            case 500 -> "Внутренняя ошибка сервера";
            default -> "Ошибка";
        };
    }

    public static String fromStatus(HttpStatus status) {
        return fromStatus(status.value());
    }
}


