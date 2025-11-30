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
import java.util.Base64;
import java.util.Map;

@WebServlet(name = "AuthLoginServlet", urlPatterns = "/api/v1/auth/login")
public class AuthLoginServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(AuthLoginServlet.class);

    private final Gson gson = new Gson();
    private final AuthService authService = ServiceLocator.getInstance().getAuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Credentials credentials = extractCredentials(req);
        if (credentials == null) {
            sendUnauthorized(resp, "Credentials are missing or invalid");
            return;
        }

        User user = authService.login(credentials.username, credentials.password);
        if (user == null) {
            logger.warn("Неуспешная попытка входа пользователя {}", credentials.username);
            sendUnauthorized(resp, "Invalid username or password");
            return;
        }

        logger.info("Пользователь {} успешно вошёл", user.getUsername());
        resp.setStatus(HttpServletResponse.SC_OK);
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(gson.toJson(Map.of(
                    "id", user.getId(),
                    "username", user.getUsername()
            )));
        }
    }

    private Credentials extractCredentials(HttpServletRequest req) {
        Credentials credentials = extractFromAuthorizationHeader(req);
        if (credentials != null) {
            return credentials;
        }
        try {
            return gson.fromJson(req.getReader(), Credentials.class);
        } catch (JsonSyntaxException | IOException e) {
            logger.warn("Не удалось разобрать тело запроса для входа", e);
            return null;
        }
    }

    private Credentials extractFromAuthorizationHeader(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return null;
        }
        try {
            String base64Credentials = authHeader.substring("Basic ".length()).trim();
            byte[] decodedBytes = Base64.getDecoder().decode(base64Credentials);
            String decoded = new String(decodedBytes, StandardCharsets.UTF_8);
            int colonIndex = decoded.indexOf(":");
            if (colonIndex < 0) {
                return null;
            }
            String username = decoded.substring(0, colonIndex);
            String password = decoded.substring(colonIndex + 1);
            return new Credentials(username, password);
        } catch (IllegalArgumentException e) {
            logger.warn("Некорректный заголовок Authorization", e);
            return null;
        }
    }

    private void sendUnauthorized(HttpServletResponse resp, String message) throws IOException {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(gson.toJson(Map.of("error", message)));
        }
    }

    private static class Credentials {
        String username;
        String password;

        Credentials() {
        }

        Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}