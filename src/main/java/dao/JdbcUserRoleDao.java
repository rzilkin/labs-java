package dao;

import db.DatabaseConnectionManager;
import dto.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcUserRoleDao implements UserRoleDao {
    private static final Logger logger = LoggerFactory.getLogger(JdbcUserRoleDao.class);

    private static final String INSERT_SQL = "INSERT INTO user_roles (user_id, role_code) VALUES (?, ?)";
    private static final String SELECT_ROLES_BY_USER_ID_SQL = "SELECT role_code FROM user_roles WHERE user_id = ?";
    private static final String SELECT_BY_USER_ID_SQL = "SELECT user_id, role_code FROM user_roles WHERE user_id = ?";
    private static final String DELETE_SQL = "DELETE FROM user_roles WHERE user_id = ? AND role_code = ?";
    private static final String DELETE_ALL_BY_USER_ID_SQL = "DELETE FROM user_roles WHERE user_id = ?";
    private static final String EXISTS_SQL = "SELECT 1 FROM user_roles WHERE user_id = ? AND role_code = ?";

    private final DatabaseConnectionManager connectionManager;

    public JdbcUserRoleDao(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public UserRole create(UserRole userRole) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setLong(1, userRole.getUserId());
            statement.setString(2, userRole.getRoleCode());
            statement.executeUpdate();
            logger.info("Назначена роль {} пользователю {}", userRole.getRoleCode(), userRole.getUserId());
            return userRole;
        } catch (SQLException e) {
            throw new DaoException("Ошибка сохранения роли пользователя", e);
        }
    }

    @Override
    public List<String> findRolesByUserId(Long userId) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ROLES_BY_USER_ID_SQL)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                List<String> roles = new ArrayList<>();
                while (rs.next()) {
                    roles.add(rs.getString("role_code"));
                }
                return roles;
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка загрузки ролей пользователя", e);
        }
    }

    @Override
    public List<UserRole> findByUserId(Long userId) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USER_ID_SQL)) {
            statement.setLong(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                List<UserRole> userRoles = new ArrayList<>();
                while (rs.next()) {
                    UserRole userRole = new UserRole();
                    userRole.setUserId(rs.getLong("user_id"));
                    userRole.setRoleCode(rs.getString("role_code"));
                    userRoles.add(userRole);
                }
                return userRoles;
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка загрузки ролей пользователя", e);
        }
    }

    @Override
    public boolean delete(Long userId, String roleCode) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, userId);
            statement.setString(2, roleCode);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Удалена роль {} у пользователя {}", roleCode, userId);
            }
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DaoException("Ошибка удаления роли пользователя", e);
        }
    }

    @Override
    public boolean deleteAllByUserId(Long userId) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_ALL_BY_USER_ID_SQL)) {
            statement.setLong(1, userId);
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Удалены все роли у пользователя {}", userId);
            }
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DaoException("Ошибка удаления всех ролей пользователя", e);
        }
    }

    @Override
    public boolean exists(Long userId, String roleCode) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(EXISTS_SQL)) {
            statement.setLong(1, userId);
            statement.setString(2, roleCode);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка проверки существования роли пользователя", e);
        }
    }
}

