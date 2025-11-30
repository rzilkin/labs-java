package service;

import dao.UserDao;
import dto.User;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = Objects.requireNonNull(userDao, "userDao");
    }

    public User register(String username, String password) {
        validateCredentials(username, password);
        logger.info("Регистрация пользователя {}", username);
        if (userDao.findByUsername(username).isPresent()) {
            logger.warn("Попытка повторной регистрации пользователя {}", username);
            throw new IllegalStateException("User already exists");
        }
        User user = new User(null, username, hashPassword(username, password));
        return userDao.create(user);
    }

    public User login(String username, String password) {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            logger.warn("Попытка входа с пустыми полями");
            return null;
        }
        logger.info("Попытка входа пользователя {}", username);
        return userDao.findByUsername(username)
                .filter(user -> hashPassword(username, password).equals(user.getPasswordHash()))
                .map(user -> {
                    logger.info("Пользователь {} успешно аутентифицирован", username);
                    return user;
                })
                .orElseGet(() -> {
                    logger.warn("Неуспешная попытка входа для пользователя {}", username);
                    return null;
                });
    }

    public Long getCurrentUserId(HttpServletRequest req) {
        return AuthHelper.getCurrentUserId(req);
    }

    private void validateCredentials(String username, String password) {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("Username and password must not be blank");
        }
    }

    private String hashPassword(String username, String password) {
        byte[] raw = (username + password).getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(raw);
    }
}