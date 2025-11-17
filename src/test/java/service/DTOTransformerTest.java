package service;

import dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DtoTransformerTest {
    private static final Logger logger = LoggerFactory.getLogger(DtoTransformerTest.class);
    private DTOTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new DTOTransformer();
        logger.info("Настройка тестового окружения завершена");
    }

    @Test
    void testTransformToRole() {
        logger.debug("Тестирование трансформации Role");
        Map<String, Object> data = new HashMap<>();
        data.put("code", "ADMIN");
        data.put("description", "Администратор");

        Role role = transformer.transformToRole(data);

        assertNotNull(role);
        assertEquals("ADMIN", role.getCode());
        assertEquals("Администратор", role.getDescription());
        logger.info("Тест трансформации Role пройден успешно");
    }

    @Test
    void testTransformToUser() {
        logger.debug("Тестирование трансформации User");
        Map<String, Object> data = new HashMap<>();
        data.put("id", 1L);
        data.put("username", "никита_захаров");
        data.put("passwordHash", "хэш_пароля_qwerty");

        User user = transformer.transformToUser(data);

        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("никита_захаров", user.getUsername());
        assertEquals("хэш_пароля_qwerty", user.getPasswordHash());
        logger.info("Тест трансформации User пройден успешно");
    }

    @Test
    void testTransformToMatlabFunction() {
        logger.debug("Тестирование трансформации MathFunction");
        Map<String, Object> data = new HashMap<>();
        data.put("id", 10L);
        data.put("ownerId", 1L);
        data.put("name", "синусоида");
        data.put("functionType", "ANALYTIC");
        data.put("definitionBody", "{\"выражение\": \"sin(x)\"}");

        MathFunction function = transformer.transformToMatlabFunction(data);

        assertNotNull(function);
        assertEquals(10L, function.getId());
        assertEquals(1L, function.getOwnerId());
        assertEquals("синусоида", function.getName());
        assertEquals("ANALYTIC", function.getFunctionType());
        assertEquals("{\"выражение\": \"sin(x)\"}", function.getDefinitionBody());
        logger.info("Тест трансформации MatlabFunction пройден успешно");
    }

    @Test
    void testTransformToFunctionComponent() {
        logger.debug("Тестирование трансформации FunctionComponent");
        Map<String, Object> data = new HashMap<>();
        data.put("compositeId", 100L);
        data.put("componentId", 200L);
        data.put("position", (short) 1);

        FunctionComponents component = transformer.transformToFunctionComponent(data);

        assertNotNull(component);
        assertEquals(100L, component.getCompositeId());
        assertEquals(200L, component.getComponentId());
        assertEquals((short) 1, component.getPosition());
        logger.info("Тест трансформации FunctionComponent пройден успешно");
    }

    @Test
    void testTransformToDataset() {
        logger.debug("Тестирование трансформации Dataset");
        Map<String, Object> data = new HashMap<>();
        data.put("id", 50L);
        data.put("functionId", 10L);
        data.put("sourceType", "GENERATED");

        TabulatedDataset dataset = transformer.transformToDataset(data);

        assertNotNull(dataset);
        assertEquals(50L, dataset.getId());
        assertEquals(10L, dataset.getFunctionId());
        assertEquals("GENERATED", dataset.getSourceType());
        logger.info("Тест трансформации Dataset пройден успешно");
    }

    @Test
    void testTransformToDatasetPoint() {
        logger.debug("Тестирование трансформации DatasetPoint");
        Map<String, Object> data = new HashMap<>();
        data.put("datasetId", 50L);
        data.put("pointIndex", 0);
        data.put("xValue", new BigDecimal("1.5"));
        data.put("yValue", new BigDecimal("2.7"));

        DatasetPoint point = transformer.transformToDatasetPoint(data);

        assertNotNull(point);
        assertEquals(50L, point.getDatasetId());
        assertEquals(0, point.getPointIndex());
        assertEquals(new BigDecimal("1.5"), point.getXValue());
        assertEquals(new BigDecimal("2.7"), point.getYValue());
        logger.info("Тест трансформации DatasetPoint пройден успешно");
    }

    @Test
    void testTransformToUserRole() {
        logger.debug("Тестирование трансформации UserRole");
        Map<String, Object> data = new HashMap<>();
        data.put("userId", 1L);
        data.put("roleCode", "ADMIN");

        UserRole userRole = transformer.transformToUserRole(data);

        assertNotNull(userRole);
        assertEquals(1L, userRole.getUserId());
        assertEquals("ADMIN", userRole.getRoleCode());
        logger.info("Тест трансформации UserRole пройден успешно");
    }

    @Test
    void testTransformToPerformanceMetrics() {
        logger.debug("Тестирование трансформации PerformanceMetrics");
        Map<String, Object> data = new HashMap<>();
        data.put("id", 100L);
        data.put("engine", "MANUAL_JDBC");
        data.put("operation", "ВСТАВКА_ДАННЫХ");
        data.put("recordsProcessed", 1000);
        data.put("elapsedMs", 150);

        PerfomanceMetrics metrics = transformer.transformToPerformanceMetrics(data);

        assertNotNull(metrics);
        assertEquals(100L, metrics.getId());
        assertEquals("MANUAL_JDBC", metrics.getEngine());
        assertEquals("ВСТАВКА_ДАННЫХ", metrics.getOperation());
        assertEquals(1000, metrics.getRecordsProcessed());
        assertEquals(150, metrics.getElapsedMs());
        logger.info("Тест трансформации PerformanceMetrics пройден успешно");
    }

    @Test
    void testTransformationExceptionForUser() {
        logger.debug("Тестирование обработки ошибок для User");
        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("id", "неверный_long");
        invalidData.put("username", "тестовый_пользователь");

        assertThrows(TransformationException.class, () -> {
            transformer.transformToUser(invalidData);
        });
        logger.info("Тест обработки ошибок User пройден успешно");
    }

    @Test
    void testTransformationExceptionForDatasetPoint() {
        logger.debug("Тестирование обработки ошибок для DatasetPoint");
        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("datasetId", "неверный_id");
        invalidData.put("pointIndex", "не_число");

        assertThrows(TransformationException.class, () -> {
            transformer.transformToDatasetPoint(invalidData);
        });
        logger.info("Тест обработки ошибок DatasetPoint пройден успешно");
    }

    @Test
    void testTransformationExceptionForPerformanceMetrics() {
        logger.debug("Тестирование обработки ошибок для PerformanceMetrics");
        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("id", 100L);
        invalidData.put("recordsProcessed", "не_число"); // Неверный тип

        assertThrows(TransformationException.class, () -> {
            transformer.transformToPerformanceMetrics(invalidData);
        });
        logger.info("Тест обработки ошибок PerformanceMetrics пройден успешно");
    }

    @Test
    void testNullSafety() {
        logger.debug("Тестирование обработки null значений");
        Map<String, Object> dataWithNulls = new HashMap<>();
        dataWithNulls.put("id", null);
        dataWithNulls.put("username", "пользователь_без_id");
        dataWithNulls.put("passwordHash", null);

        assertDoesNotThrow(() -> {
            User user = transformer.transformToUser(dataWithNulls);
            assertNull(user.getId());
            assertEquals("пользователь_без_id", user.getUsername());
            assertNull(user.getPasswordHash());
        });
        logger.info("Тест обработки null значений пройден успешно");
    }
}