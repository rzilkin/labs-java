package service;

import dao.UserRoleDao;
import dto.User;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class AuthHelper {
    private static final Logger logger = LoggerFactory.getLogger(AuthHelper.class);
    private static final String USER_ROLES_ATTRIBUTE = "userRoles";

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
        if (user == null) {
            return null;
        }

        // Load and cache user roles in request attribute
        loadUserRoles(req, user.getId());
        return user.getId();
    }

    public static Set<String> getUserRoles(HttpServletRequest req) {
        @SuppressWarnings("unchecked")
        Set<String> roles = (Set<String>) req.getAttribute(USER_ROLES_ATTRIBUTE);
        if (roles == null) {
            Long userId = getCurrentUserId(req);
            if (userId != null) {
                roles = loadUserRoles(req, userId);
            } else {
                roles = new HashSet<>();
            }
        }
        return roles;
    }

    private static Set<String> loadUserRoles(HttpServletRequest req, Long userId) {
        @SuppressWarnings("unchecked")
        Set<String> cached = (Set<String>) req.getAttribute(USER_ROLES_ATTRIBUTE);
        if (cached != null) {
            return cached;
        }

        try {
            UserRoleDao userRoleDao = ServiceLocator.getInstance().getUserRoleDao();
            List<String> roleList = userRoleDao.findRolesByUserId(userId);
            Set<String> roles = new HashSet<>(roleList);
            req.setAttribute(USER_ROLES_ATTRIBUTE, roles);
            logger.debug("Загружены роли пользователя {}: {}", userId, roles);
            return roles;
        } catch (Exception e) {
            logger.warn("Ошибка загрузки ролей пользователя {}", userId, e);
            Set<String> emptyRoles = new HashSet<>();
            req.setAttribute(USER_ROLES_ATTRIBUTE, emptyRoles);
            return emptyRoles;
        }
    }
}