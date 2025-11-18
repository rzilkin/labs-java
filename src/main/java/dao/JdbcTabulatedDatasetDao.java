package dao;

import db.DatabaseConnectionManager;
import dto.TabulatedDataset;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTabulatedDatasetDao implements TabulatedDatasetDao {
    private static final String INSERT_SQL = "INSERT INTO tabulated_datasets (function_id, source_type) VALUES (?, ?) RETURNING id";
    private static final String SELECT_BY_ID_SQL = "SELECT id, function_id, source_type FROM tabulated_datasets WHERE id = ?";
    private static final String SELECT_BY_FUNCTION_SQL = "SELECT id, function_id, source_type FROM tabulated_datasets WHERE function_id = ? ORDER BY id";
    private static final String UPDATE_SQL = "UPDATE tabulated_datasets SET function_id = ?, source_type = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM tabulated_datasets WHERE id = ?";

    private final DatabaseConnectionManager connectionManager;

    public JdbcTabulatedDatasetDao(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public TabulatedDataset create(TabulatedDataset dataset) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setObject(1, dataset.getFunctionId());
            statement.setString(2, dataset.getSourceType());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    dataset.setId(rs.getLong("id"));
                    return dataset;
                }
                throw new DaoException("Сервер не вернул идентификатор набора данных");
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка сохранения набора данных", e);
        }
    }

    @Override
    public Optional<TabulatedDataset> findById(Long id) {
        return executeSingleResultQuery(SELECT_BY_ID_SQL, id);
    }

    @Override
    public List<TabulatedDataset> findByFunctionId(Long functionId) {
        return executeListQuery(SELECT_BY_FUNCTION_SQL, functionId);
    }

    @Override
    public boolean update(TabulatedDataset dataset) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setObject(1, dataset.getFunctionId());
            statement.setString(2, dataset.getSourceType());
            statement.setObject(3, dataset.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Ошибка обновления набора данных", e);
        }
    }

    @Override
    public boolean delete(Long id) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setObject(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Ошибка удаления набора данных", e);
        }
    }

    private Optional<TabulatedDataset> executeSingleResultQuery(String sql, Object parameter) {
        List<TabulatedDataset> result = executeListQuery(sql, parameter);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    private List<TabulatedDataset> executeListQuery(String sql, Object... parameters) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < parameters.length; i++) {
                statement.setObject(i + 1, parameters[i]);
            }
            try (ResultSet rs = statement.executeQuery()) {
                List<TabulatedDataset> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка выполнения запроса набора данных", e);
        }
    }

    private TabulatedDataset mapRow(ResultSet rs) throws SQLException {
        TabulatedDataset dataset = new TabulatedDataset();
        dataset.setId(rs.getLong("id"));
        dataset.setFunctionId((Long) rs.getObject("function_id"));
        dataset.setSourceType(rs.getString("source_type"));
        return dataset;
    }
}