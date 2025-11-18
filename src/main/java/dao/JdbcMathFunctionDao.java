package dao;

import db.DatabaseConnectionManager;
import dto.MathFunction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMathFunctionDao implements MathFunctionDao {
    private static final String INSERT_SQL =
            "INSERT INTO math_functions (owner_id, name, function_type, definition_body) " +
                    "VALUES (?, ?, ?, ?::jsonb) RETURNING id";

    private static final String SELECT_BY_ID_SQL =
            "SELECT id, owner_id, name, function_type, definition_body " +
                    "FROM math_functions WHERE id = ?";

    private static final String SELECT_BY_OWNER_SQL =
            "SELECT id, owner_id, name, function_type, definition_body " +
                    "FROM math_functions WHERE owner_id = ? ORDER BY id";

    private static final String SELECT_ALL_SQL =
            "SELECT id, owner_id, name, function_type, definition_body " +
                    "FROM math_functions ORDER BY id";

    private static final String UPDATE_SQL =
            "UPDATE math_functions " +
                    "SET owner_id = ?, name = ?, function_type = ?, definition_body = ?::jsonb " +
                    "WHERE id = ?";

    private static final String DELETE_SQL =
            "DELETE FROM math_functions WHERE id = ?";

    private final DatabaseConnectionManager connectionManager;

    public JdbcMathFunctionDao(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public MathFunction create(MathFunction function) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            statement.setObject(1, function.getOwnerId());
            statement.setString(2, function.getName());
            statement.setString(3, function.getFunctionType());
            statement.setString(4, function.getDefinitionBody()); // строка, но в SQL она приводится к jsonb

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    function.setId(rs.getLong("id"));
                    return function;
                }
                throw new DaoException("Сервер не вернул идентификатор новой функции");
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка сохранения математической функции", e);
        }
    }

    @Override
    public Optional<MathFunction> findById(Long id) {
        return executeSingleResultQuery(SELECT_BY_ID_SQL, id);
    }

    @Override
    public List<MathFunction> findByOwner(Long ownerId) {
        return executeListQuery(SELECT_BY_OWNER_SQL, ownerId);
    }

    @Override
    public List<MathFunction> findAll() {
        return executeListQuery(SELECT_ALL_SQL);
    }

    @Override
    public boolean update(MathFunction function) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {

            statement.setObject(1, function.getOwnerId());
            statement.setString(2, function.getName());
            statement.setString(3, function.getFunctionType());
            statement.setString(4, function.getDefinitionBody());
            statement.setObject(5, function.getId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Ошибка обновления математической функции", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setObject(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Ошибка удаления математической функции", e);
        }
    }

    private Optional<MathFunction> executeSingleResultQuery(String sql, Object parameter) {
        List<MathFunction> result = executeListQuery(sql, parameter);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    private List<MathFunction> executeListQuery(String sql, Object... parameters) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            try (ResultSet rs = statement.executeQuery()) {
                List<MathFunction> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка выполнения запроса математической функции", e);
        }
    }

    private MathFunction mapRow(ResultSet rs) throws SQLException {
        MathFunction function = new MathFunction();
        function.setId(rs.getLong("id"));
        function.setOwnerId((Long) rs.getObject("owner_id"));
        function.setName(rs.getString("name"));
        function.setFunctionType(rs.getString("function_type"));
        function.setDefinitionBody(rs.getString("definition_body"));
        return function;
    }
}