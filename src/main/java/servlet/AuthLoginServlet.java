package servlet;

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
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@WebServlet(name = "AuthLoginServlet", urlPatterns = "/api/v1/auth/login")
public class AuthLoginServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(AuthLoginServlet.class);

    private final AuthService authService = ServiceLocator.getInstance().getAuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Credentials credentials = extractCredentials(req);
        if (credentials == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        User user = authService.login(credentials.username, credentials.password);
        if (user == null) {
            logger.warn("Неуспешная попытка входа пользователя {}", credentials.username);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        logger.info("Пользователь {} успешно вошёл", user.getUsername());
        resp.setStatus(HttpServletResponse.SC_OK);
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
