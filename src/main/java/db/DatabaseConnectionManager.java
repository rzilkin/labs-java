package db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionManager.class);
    private final DatabaseConfig config;

    public DatabaseConnectionManager(DatabaseConfig config) {
        this.config = config;
        loadDriver();
    }

    private void loadDriver() {
        try {
            Class.forName("org.postgresql.Driver");
            logger.debug("Драйвер PostgreSQL успешно загружен");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Драйвер PostgreSQL не найден в classpath", e);
        }
    }

    public Connection getConnection() throws SQLException {
        logger.trace("Открытие подключения к БД {}", config.getUrl());
        return DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
    }
}