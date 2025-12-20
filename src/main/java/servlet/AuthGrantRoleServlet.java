package servlet;

import com.google.gson.JsonSyntaxException;
import dao.UserDao;
import dao.UserRoleDao;
import dto.User;
import dto.UserRole;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ServiceLocator;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@WebServlet(name = "AuthGrantRoleServlet", urlPatterns = "/api/v1/auth/grant")
public class AuthGrantRoleServlet extends BaseApiServlet {
    private static final Logger logger = LoggerFactory.getLogger(AuthGrantRoleServlet.class);
    private static final String ADMIN_ROLE = "ADMIN";

    private final UserRoleDao userRoleDao = ServiceLocator.getInstance().getUserRoleDao();
    private final UserDao userDao = ServiceLocator.getInstance().getUserDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        Long currentUserId = getCurrentUserId(req);
        if (currentUserId == null) {
            requireAuth(req, resp);
            return;
        }

        requireRole(req, resp, ADMIN_ROLE);
        if (resp.isCommitted()) {
            return;
        }

        GrantRoleRequest requestBody;
        try {
            // Try to parse from JSON body first
            String bodyStr = readBody(req);
            if (bodyStr != null && !bodyStr.isBlank()) {
                try {
                    requestBody = gson.fromJson(bodyStr, GrantRoleRequest.class);
                } catch (JsonSyntaxException e) {
                    logger.warn("Некорректный JSON при назначении роли", e);
                    sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
                    return;
                }
            } else {
                // Fall back to query parameters
                String username = req.getParameter("username");
                String role = req.getParameter("role");
                if (username == null || role == null) {
                    sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "username and role are required");
                    return;
                }
                requestBody = new GrantRoleRequest();
                requestBody.username = username;
                requestBody.role = role;
            }
        } catch (Exception e) {
            logger.error("Ошибка чтения тела запроса", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid request");
            return;
        }

        if (requestBody == null || isBlank(requestBody.username) || isBlank(requestBody.role)) {
            logger.warn("Запрос назначения роли с пустыми полями");
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, "username and role are required");
            return;
        }

        String username = requestBody.username.trim();
        String roleCode = requestBody.role.trim().toUpperCase();

        try {
            User targetUser = userDao.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

            if (userRoleDao.exists(targetUser.getId(), roleCode)) {
                logger.info("Пользователь {} уже имеет роль {}", username, roleCode);
                resp.setStatus(HttpServletResponse.SC_OK);
                try (PrintWriter writer = resp.getWriter()) {
                    writer.write(gson.toJson(Map.of(
                            "userId", targetUser.getId(),
                            "username", username,
                            "role", roleCode,
                            "message", "Role already assigned")));
                }
                return;
            }

            UserRole userRole = new UserRole(targetUser.getId(), roleCode);
            userRoleDao.create(userRole);

            logger.info("Пользователь {} (id: {}) назначил роль {} пользователю {} (id: {})",
                    getCurrentUserId(req), currentUserId, roleCode, username, targetUser.getId());

            resp.setStatus(HttpServletResponse.SC_CREATED);
            try (PrintWriter writer = resp.getWriter()) {
                writer.write(gson.toJson(Map.of(
                        "userId", targetUser.getId(),
                        "username", username,
                        "role", roleCode)));
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Ошибка валидации при назначении роли: {}", e.getMessage());
            sendErrorResponse(req, resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Неожиданная ошибка назначения роли", e);
            sendErrorResponse(req, resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private String readBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static class GrantRoleRequest {
        String username;
        String role;
    }
}
