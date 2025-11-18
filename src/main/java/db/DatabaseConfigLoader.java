package db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfigLoader.class);
    private static final String PROPERTIES_FILE = "database.properties";

    public DatabaseConfig load() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
                logger.debug("Конфигурация БД загружена из {}", PROPERTIES_FILE);
            } else {
                logger.warn("Файл {} не найден. Используются только переменные окружения", PROPERTIES_FILE);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось прочитать файл конфигурации базы данных", e);
        }

        String url = overrideFromEnv("DB_URL", properties.getProperty("db.url"));
        String username = overrideFromEnv("DB_USERNAME", properties.getProperty("db.username"));
        String password = overrideFromEnv("DB_PASSWORD", properties.getProperty("db.password"));

        if (url == null || username == null || password == null) {
            throw new IllegalStateException("Недостаточно параметров для подключения к базе данных");
        }

        return new DatabaseConfig(url, username, password);
    }

    private String overrideFromEnv(String envName, String defaultValue) {
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            logger.debug("Параметр {} переопределён через переменную окружения", envName);
            return envValue;
        }
        return defaultValue;
    }
}