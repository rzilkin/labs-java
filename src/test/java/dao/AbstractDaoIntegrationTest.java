package dao;

import db.DatabaseConfig;
import db.DatabaseConnectionManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractDaoIntegrationTest {

    protected DatabaseConnectionManager connectionManager;

    @BeforeAll
    void initRealPostgres() throws SQLException, IOException {
        DatabaseConfig config = new DatabaseConfig(
                "jdbc:postgresql://localhost:5432/mathfunc",
                "postgres",
                "postgres"
        );
        connectionManager = new DatabaseConnectionManager(config);

        runSchemaScripts();
    }

    @BeforeEach
    void cleanTables() throws SQLException {
        try (Connection connection = connectionManager.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(
                    "TRUNCATE TABLE dataset_points, " +
                            "tabulated_datasets, " +
                            "math_functions, " +
                            "users " +
                            "RESTART IDENTITY CASCADE"
            );
        }
    }

    @AfterAll
    void shutdown() {
        // если в DatabaseConnectionManager есть close() — можно вызвать тут
        // connectionManager.close();
    }

    private void runSchemaScripts() throws IOException, SQLException {
        // скрипты должны лежать в src/main/resources/scripts/...
        List<String> scripts = List.of(
                "scripts/01_create_users.sql",
                "scripts/03_create_math_functions.sql",
                "scripts/04_create_tabulated_datasets.sql",
                "scripts/05_create_dataset_points.sql"
        );

        try (Connection connection = connectionManager.getConnection();
             Statement statement = connection.createStatement()) {

            ClassLoader cl = getClass().getClassLoader();

            for (String scriptPath : scripts) {
                try (InputStream in = cl.getResourceAsStream(scriptPath)) {
                    if (in == null) {
                        throw new IllegalStateException("Не найден SQL-скрипт в classpath: " + scriptPath);
                    }
                    String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);

                    for (String chunk : sql.split(";")) {
                        String trimmed = chunk.trim();
                        if (!trimmed.isEmpty()) {
                            statement.execute(trimmed);
                        }
                    }
                }
            }
        }
    }
}
