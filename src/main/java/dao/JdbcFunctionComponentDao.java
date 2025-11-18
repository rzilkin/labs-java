package dao;

import db.DatabaseConnectionManager;
import dto.FunctionComponents;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcFunctionComponentDao implements FunctionComponentDao {
    private static final String BASE_SELECT_SQL =
            "SELECT composite_id, component_id, position FROM function_components";
    private static final String SELECT_BY_COMPOSITE = BASE_SELECT_SQL + " WHERE composite_id = ?";
    private static final String SELECT_BY_COMPOSITE_ORDER = SELECT_BY_COMPOSITE + " ORDER BY position";
    private static final String SELECT_COMPOSITE_IDS_BY_COMPONENT =
            "SELECT DISTINCT composite_id FROM function_components WHERE component_id = ? ORDER BY composite_id";

    private final DatabaseConnectionManager connectionManager;

    public JdbcFunctionComponentDao(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public List<FunctionComponents> findByCompositeId(Long compositeId) {
        return executeComponentQuery(SELECT_BY_COMPOSITE, compositeId);
    }

    @Override
    public List<FunctionComponents> findByCompositeIdOrderByPosition(Long compositeId) {
        return executeComponentQuery(SELECT_BY_COMPOSITE_ORDER, compositeId);
    }

    @Override
    public List<Long> findCompositeIdsByComponentId(Long componentId) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_COMPOSITE_IDS_BY_COMPONENT)) {
            statement.setObject(1, componentId);
            try (ResultSet rs = statement.executeQuery()) {
                List<Long> compositeIds = new ArrayList<>();
                while (rs.next()) {
                    compositeIds.add(rs.getLong("composite_id"));
                }
                return compositeIds;
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка чтения композитных функций", e);
        }
    }

    private List<FunctionComponents> executeComponentQuery(String sql, Long id) {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                List<FunctionComponents> components = new ArrayList<>();
                while (rs.next()) {
                    components.add(mapRow(rs));
                }
                return components;
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка чтения компонентов функции", e);
        }
    }

    private FunctionComponents mapRow(ResultSet rs) throws SQLException {
        FunctionComponents component = new FunctionComponents();
        component.setCompositeId((Long) rs.getObject("composite_id"));
        component.setComponentId((Long) rs.getObject("component_id"));
        component.setPosition((Short) rs.getObject("position"));
        return component;
    }
}