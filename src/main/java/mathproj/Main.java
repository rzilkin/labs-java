package mathproj;

import db.DatabaseConfig;
import db.DatabaseConfigLoader;
import db.DatabaseConnectionManager;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final int DEFAULT_PORT = 8081;
    private static final String DEFAULT_CONTEXT_PATH = "";
    private static final String DEFAULT_DOC_BASE = "src/main/webapp";

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

        try {
            Tomcat tomcat = startTomcat();
            logger.info("Tomcat запущен и ожидает запросы");
            tomcat.getServer().await();
        } catch (LifecycleException | IOException e) {
            logger.error("Не удалось запустить Tomcat", e);
        }

        logger.info("Приложение завершило инициализацию");
    }

    private static Tomcat startTomcat() throws LifecycleException, IOException {
        int port = resolvePort();
        File docBase = new File(DEFAULT_DOC_BASE);
        if (!docBase.exists() && !docBase.mkdirs()) {
            throw new IOException("Не удалось создать директорию webapp: " + docBase.getAbsolutePath());
        }

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector();

        Context context = tomcat.addWebapp(DEFAULT_CONTEXT_PATH, docBase.getAbsolutePath());
        StandardRoot resources = new StandardRoot(context);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", "target/classes", "/"));
        context.setResources(resources);

        tomcat.start();
        logger.info("Tomcat запущен на порту {}", port);
        return tomcat;
    }

    private static int resolvePort() {
        String portValue = System.getProperty("server.port");
        if (portValue == null || portValue.isBlank()) {
            portValue = System.getenv("SERVER_PORT");
        }
        if (portValue == null || portValue.isBlank()) {
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(portValue.trim());
        } catch (NumberFormatException e) {
            logger.warn("Некорректный порт '{}', используется порт {}", portValue, DEFAULT_PORT);
            return DEFAULT_PORT;
        }
    }
}
