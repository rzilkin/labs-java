package servlet;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dto.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.AuthService;
import service.ServiceLocator;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@WebServlet(name = "AuthRegisterServlet", urlPatterns = "/api/v1/auth/register")
public class AuthRegisterServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(AuthRegisterServlet.class);

    private final Gson gson = new Gson();
    private final AuthService authService = ServiceLocator.getInstance().getAuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        RegisterRequest requestBody;
        try {
            requestBody = gson.fromJson(req.getReader(), RegisterRequest.class);
        } catch (JsonSyntaxException e) {
            logger.warn("Невалидный JSON при регистрации", e);
            sendBadRequest(resp, "Invalid JSON format");
            return;
        }

        if (requestBody == null || isBlank(requestBody.username) || isBlank(requestBody.password)) {
            logger.warn("Запрос регистрации с пустыми полями");
            sendBadRequest(resp, "Username and password are required");
            return;
        }

        try {
            User created = authService.register(requestBody.username.trim(), requestBody.password);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(Map.of(
                        "id", created.getId(),
                        "username", created.getUsername()
                )));
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка валидации при регистрации: {}", e.getMessage());
            sendBadRequest(resp, e.getMessage());
        } catch (IllegalStateException e) {
            logger.warn("Попытка создать существующего пользователя {}", requestBody.username);
            sendConflict(resp, "User already exists");
        } catch (Exception e) {
            logger.error("Неожиданная ошибка регистрации", e);
            sendServerError(resp);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void sendBadRequest(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(gson.toJson(Map.of("error", message)));
        }
    }

    private void sendConflict(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_CONFLICT);
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(gson.toJson(Map.of("error", message)));
        }
    }

    private void sendServerError(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(gson.toJson(Map.of("error", "Internal server error")));
        }
    }

    private static class RegisterRequest {
        String username;
        String password;
    }
}