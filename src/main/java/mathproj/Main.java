package mathproj;

import db.DatabaseConfig;
import db.DatabaseConfigLoader;
import db.DatabaseConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Приложение запущено");
        Runtime runtime = Runtime.getRuntime();
        logger.debug("Доступных процессоров: {}", runtime.availableProcessors());
        logger.debug("Максимальный объём памяти: {} байт", runtime.maxMemory());

        DatabaseConfigLoader loader = new DatabaseConfigLoader();
        DatabaseConfig config = loader.load();
        DatabaseConnectionManager connectionManager = new DatabaseConnectionManager(config);
        try (Connection connection = connectionManager.getConnection()) {
            logger.info("Успешно установлено соединение с БД {}", config.getUrl());
        } catch (SQLException e) {
            logger.error("Не удалось установить соединение с базой данных", e);
        }

        logger.info("Приложение завершило инициализацию");
    }
}