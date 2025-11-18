package dao;

import db.DatabaseConnectionManager;
import dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserDao implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(JdbcUserDao.class);

    private static final String INSERT_SQL = "INSERT INTO users (username, password_hash) VALUES (?, ?) RETURNING id";
    private static final String SELECT_BY_ID_SQL = "SELECT id, username, password_hash FROM users WHERE id = ?";
    private static final String SELECT_BY_USERNAME_SQL = "SELECT id, username, password_hash FROM users WHERE username = ?";
    private static final String SELECT_ALL_SQL = "SELECT id, username, password_hash FROM users ORDER BY id";
    private static final String UPDATE_SQL = "UPDATE users SET username = ?, password_hash = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM users WHERE id = ?";

    private final DatabaseConnectionManager connectionManager;

    public JdbcUserDao(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public User create(User user) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getLong("id"));
                    logger.info("Создан пользователь {} с id {}", user.getUsername(), user.getId());
                    return user;
                }
                throw new DaoException("Сервер не вернул идентификатор новой записи");
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка сохранения пользователя", e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        return executeSingleResultQuery(SELECT_BY_ID_SQL, id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return executeSingleResultQuery(SELECT_BY_USERNAME_SQL, username);
    }

    @Override
    public List<User> findAll() {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = statement.executeQuery()) {
            List<User> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DaoException("Ошибка загрузки списка пользователей", e);
        }
    }

    @Override
    public boolean update(User user) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setLong(3, user.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Ошибка обновления пользователя", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Ошибка удаления пользователя", e);
        }
    }

    private Optional<User> executeSingleResultQuery(String sql, Object parameter) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, parameter);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка выполнения запроса пользователя", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        return user;
    }
}