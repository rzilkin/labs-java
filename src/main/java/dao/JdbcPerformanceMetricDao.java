package dao;

import db.DatabaseConnectionManager;
import dto.PerformanceMetrics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcPerformanceMetricDao implements PerformanceMetricDao {
    private static final String BASE_SELECT_SQL =
            "SELECT id, engine, operation, records_processed, elapsed_ms FROM performance_metrics";
    private static final String SELECT_ORDER_BY_ELAPSED = BASE_SELECT_SQL + " ORDER BY elapsed_ms";
    private static final String SELECT_ORDER_BY_RECORDS_DESC = BASE_SELECT_SQL + " ORDER BY records_processed DESC";
    private static final String INSERT_SQL =
            "INSERT INTO performance_metrics (engine, operation, records_processed, elapsed_ms) " +
                    "VALUES (?, ?, ?, ?) RETURNING id";

    private final DatabaseConnectionManager connectionManager;

    public JdbcPerformanceMetricDao(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public PerformanceMetrics create(PerformanceMetrics metric) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, metric.getEngine());
            statement.setString(2, metric.getOperation());
            statement.setObject(3, metric.getRecordsProcessed());
            statement.setObject(4, metric.getElapsedMs());
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    metric.setId(rs.getLong("id"));
                    return metric;
                }
                throw new DaoException("Сервер не вернул идентификатор метрики производительности");
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка сохранения метрики производительности", e);
        }
    }

    @Override
    public List<PerformanceMetrics> findAll() {
        return executeListQuery(BASE_SELECT_SQL);
    }

    @Override
    public List<PerformanceMetrics> findAllOrderByElapsedMsAsc() {
        return executeListQuery(SELECT_ORDER_BY_ELAPSED);
    }

    @Override
    public List<PerformanceMetrics> findAllOrderByRecordsProcessedDesc() {
        return executeListQuery(SELECT_ORDER_BY_RECORDS_DESC);
    }

    private List<PerformanceMetrics> executeListQuery(String sql) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<PerformanceMetrics> metrics = new ArrayList<>();
            while (rs.next()) {
                metrics.add(mapRow(rs));
            }
            return metrics;
        } catch (SQLException e) {
            throw new DaoException("Ошибка загрузки метрик производительности", e);
        }
    }

    private PerformanceMetrics mapRow(ResultSet rs) throws SQLException {
        PerformanceMetrics metric = new PerformanceMetrics();
        metric.setId(rs.getLong("id"));
        metric.setEngine(rs.getString("engine"));
        metric.setOperation(rs.getString("operation"));
        metric.setRecordsProcessed((Integer) rs.getObject("records_processed"));
        metric.setElapsedMs((Integer) rs.getObject("elapsed_ms"));
        return metric;
    }
}