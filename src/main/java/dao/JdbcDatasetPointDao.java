package dao;

import db.DatabaseConnectionManager;
import dto.DatasetPoint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcDatasetPointDao implements DatasetPointDao {
    private static final String UPSERT_SQL = "INSERT INTO dataset_points (dataset_id, point_index, x_value, y_value) " +
            "VALUES (?, ?, ?, ?) ON CONFLICT (dataset_id, point_index) DO UPDATE SET " +
            "x_value = EXCLUDED.x_value, y_value = EXCLUDED.y_value";
    private static final String SELECT_BY_DATASET_SQL = "SELECT dataset_id, point_index, x_value, y_value FROM dataset_points WHERE dataset_id = ? ORDER BY point_index";
    private static final String DELETE_POINT_SQL = "DELETE FROM dataset_points WHERE dataset_id = ? AND point_index = ?";
    private static final String DELETE_ALL_SQL = "DELETE FROM dataset_points WHERE dataset_id = ?";
    private static final String COUNT_SQL = "SELECT COUNT(*) FROM dataset_points WHERE dataset_id = ?";

    private final DatabaseConnectionManager connectionManager;

    public JdbcDatasetPointDao(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void upsert(DatasetPoint point) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPSERT_SQL)) {
            statement.setObject(1, point.getDatasetId());
            statement.setObject(2, point.getPointIndex());
            if (point.getXValue() != null) {
                statement.setBigDecimal(3, point.getXValue());
            } else {
                statement.setNull(3, java.sql.Types.NUMERIC);
            }
            if (point.getYValue() != null) {
                statement.setBigDecimal(4, point.getYValue());
            } else {
                statement.setNull(4, java.sql.Types.NUMERIC);
            }
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Ошибка сохранения точки набора данных", e);
        }
    }

    @Override
    public List<DatasetPoint> findByDatasetId(Long datasetId) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_DATASET_SQL)) {
            statement.setObject(1, datasetId);
            try (ResultSet rs = statement.executeQuery()) {
                List<DatasetPoint> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка чтения точек набора данных", e);
        }
    }

    @Override
    public boolean deletePoint(Long datasetId, int pointIndex) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_POINT_SQL)) {
            statement.setObject(1, datasetId);
            statement.setInt(2, pointIndex);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DaoException("Ошибка удаления точки набора данных", e);
        }
    }

    @Override
    public int deleteAllByDataset(Long datasetId) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_ALL_SQL)) {
            statement.setObject(1, datasetId);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Ошибка очистки точек набора данных", e);
        }
    }

    @Override
    public long countByDataset(Long datasetId) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_SQL)) {
            statement.setObject(1, datasetId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                return 0L;
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка подсчёта точек набора данных", e);
        }
    }

    private DatasetPoint mapRow(ResultSet rs) throws SQLException {
        DatasetPoint point = new DatasetPoint();
        point.setDatasetId((Long) rs.getObject("dataset_id"));
        point.setPointIndex(rs.getInt("point_index"));
        point.setXValue(rs.getBigDecimal("x_value"));
        point.setYValue(rs.getBigDecimal("y_value"));
        return point;
    }
}