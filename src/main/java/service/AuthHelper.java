package service;

import dto.User;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class AuthHelper {
    private static final Logger logger = LoggerFactory.getLogger(AuthHelper.class);

    private AuthHelper() {
    }

    public static Long getCurrentUserId(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            logger.debug("Заголовок Authorization отсутствует или не Basic");
            return null;
        }

        String base64Credentials = authHeader.substring("Basic ".length()).trim();
        byte[] decodedBytes;
        try {
            decodedBytes = Base64.getDecoder().decode(base64Credentials);
        } catch (IllegalArgumentException e) {
            logger.warn("Не удалось декодировать заголовок авторизации", e);
            return null;
        }

        String credentials = new String(decodedBytes, StandardCharsets.UTF_8);
        int colonIndex = credentials.indexOf(":");
        if (colonIndex < 0) {
            logger.warn("Неверный формат учетных данных Basic Auth");
            return null;
        }

        String username = credentials.substring(0, colonIndex);
        String password = credentials.substring(colonIndex + 1);
        logger.debug("Попытка аутентификации пользователя {} из заголовка", username);

        AuthService authService = ServiceLocator.getInstance().getAuthService();
        User user = authService.login(username, password);
        return user != null ? user.getId() : null;
    }
}