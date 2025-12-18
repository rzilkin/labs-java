package dao;

import db.DatabaseConnectionManager;
import dto.PerformanceMetrics;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class JdbcPerformanceMetricDao implements PerformanceMetricDao {
    private static final String BASE_SELECT_SQL = "SELECT id, engine, operation, records_processed, elapsed_ms FROM performance_metrics";
    private static final String SELECT_ORDER_BY_ELAPSED = BASE_SELECT_SQL + " ORDER BY elapsed_ms";
    private static final String SELECT_ORDER_BY_RECORDS_DESC = BASE_SELECT_SQL + " ORDER BY records_processed DESC";
    private static final String INSERT_SQL = "INSERT INTO performance_metrics (engine, operation, records_processed, elapsed_ms, recorded_at) "
            +
            "VALUES (?, ?, ?, ?, ?) RETURNING id";

    private final DatabaseConnectionManager connectionManager;

    public JdbcPerformanceMetricDao(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public PerformanceMetrics create(PerformanceMetrics metric) {
        if (metric.getRecordedAt() == null) {
            metric.setRecordedAt(Instant.now());
        }
        try (Connection connection = connectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, metric.getEngine());
            statement.setString(2, metric.getOperation());
            statement.setInt(3, metric.getRecordsProcessed() != null ? metric.getRecordsProcessed() : 0);
            statement.setInt(4, metric.getElapsedMs() != null ? metric.getElapsedMs() : 0);
            statement.setTimestamp(5, Timestamp.from(metric.getRecordedAt()));
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    metric.setId(rs.getLong("id"));
                    return metric;
                }
                throw new DaoException("Сервер не вернул идентификатор метрики производительности");
            }
        } catch (SQLException e) {
            // Check if it's a column not found error (recorded_at missing)
            if (e.getMessage() != null && e.getMessage().contains("recorded_at")) {
                // Try without recorded_at
                return createWithoutRecordedAt(metric);
            }
            throw new DaoException("Ошибка сохранения метрики производительности: " + e.getMessage(), e);
        }
    }

    private PerformanceMetrics createWithoutRecordedAt(PerformanceMetrics metric) {
        String insertWithoutTimestamp = "INSERT INTO performance_metrics (engine, operation, records_processed, elapsed_ms) "
                + "VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection connection = connectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(insertWithoutTimestamp)) {
            statement.setString(1, metric.getEngine());
            statement.setString(2, metric.getOperation());
            statement.setInt(3, metric.getRecordsProcessed() != null ? metric.getRecordsProcessed() : 0);
            statement.setInt(4, metric.getElapsedMs() != null ? metric.getElapsedMs() : 0);
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
        try {
            Timestamp ts = rs.getTimestamp("recorded_at");
            metric.setRecordedAt(ts != null ? ts.toInstant() : null);
        } catch (SQLException e) {
            metric.setRecordedAt(null);
        }
        return metric;
    }
}
