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
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
            return;
        }

        if (requestBody == null || isBlank(requestBody.username) || isBlank(requestBody.password)) {
            logger.warn("Запрос регистрации с пустыми полями");
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Username and password are required");
            return;
        }

        // Validate minimum lengths per OpenAPI spec
        if (requestBody.username.trim().length() < 3) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Username must be at least 3 characters");
            return;
        }
        if (requestBody.password.length() < 6) {
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Password must be at least 6 characters");
            return;
        }

        try {
            User created = authService.register(requestBody.username.trim(), requestBody.password);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(Map.of(
                        "id", created.getId(),
                        "username", created.getUsername())));
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка валидации при регистрации: {}", e.getMessage());
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            logger.warn("Попытка создать существующего пользователя {}", requestBody.username);
            sendErrorResponse(req, resp, HttpServletResponse.SC_CONFLICT, "User already exists");
        } catch (Exception e) {
            logger.error("Неожиданная ошибка регистрации", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static class RegisterRequest {
        String username;
        String password;
    }
}
