package service;

import dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;

public class DTOTransformer {
    private static final Logger logger = LoggerFactory.getLogger(DTOTransformer.class);

    public Role transformToRole(Map<String, Object> data) {
        logger.debug("Преобразование данных в роль: {}", data);
        try {
            Role role = new Role();
            role.setCode((String) data.get("code"));
            role.setDescription((String) data.get("description"));
            logger.info("Успешное преобразование данных в роль: {}", role);
            return role;
        } catch (Exception e) {
            logger.error("Ошибка преобразования данных в роль: {}", data, e);
            throw new TransformationException("Преобразование данных в роль не удалось", e);
        }
    }

    public User transformToUser(Map<String, Object> data) {
        logger.debug("Преобразование данных в пользователя: {}", data);
        try {
            User user = new User();
            user.setId(getLongValue(data.get("id")));
            user.setUsername((String) data.get("username"));
            user.setPasswordHash((String) data.get("passwordHash"));
            logger.info("Успешное преобразование данных в пользователя: {}", user.getUsername());
            return user;
        } catch (Exception e) {
            logger.error("Ошибка преобразования данных в пользователя: {}", data, e);
            throw new TransformationException("Преобразование данных в пользователя не удалось", e);
        }
    }

    public MathFunction transformToMatlabFunction(Map<String, Object> data) {
        logger.debug("Преобразование данных в математическую функцию: {}", data);
        try {
            MathFunction function = new MathFunction();
            function.setId(getLongValue(data.get("id")));
            function.setOwnerId(getLongValue(data.get("ownerId")));
            function.setName((String) data.get("name"));
            function.setFunctionType((String) data.get("functionType"));
            function.setDefinitionBody((String) data.get("definitionBody"));
            logger.info("Успешное преобразование данных в математическую функцию: {}", function.getName());
            return function;
        } catch (Exception e) {
            logger.error("Ошибка преобразования данных в математическую функцию: {}", data, e);
            throw new TransformationException("Преобразование данных в математическую функцию не удалось", e);
        }
    }

    public FunctionComponents transformToFunctionComponent(Map<String, Object> data) {
        logger.debug("Преобразование данных в составную функцию: {}", data);
        try {
            FunctionComponents component = new FunctionComponents();
            component.setCompositeId(getLongValue(data.get("compositeId")));
            component.setComponentId(getLongValue(data.get("componentId")));
            component.setPosition(getShortValue(data.get("position")));
            logger.info("Успешное преобразование данных в составную функцию: {}->{}",
                    component.getCompositeId(), component.getComponentId());
            return component;
        } catch (Exception e) {
            logger.error("Ошибка преобразования данных в составную функцию: {}", data, e);
            throw new TransformationException("Преобразование данных в составную функцию не удалось", e);
        }
    }

    public TabulatedDataset transformToDataset(Map<String, Object> data) {
        logger.debug("Преобразование данных в набор данных: {}", data);
        try {
            TabulatedDataset dataset = new TabulatedDataset();
            dataset.setId(getLongValue(data.get("id")));
            dataset.setFunctionId(getLongValue(data.get("functionId")));
            dataset.setSourceType((String) data.get("sourceType"));
            logger.info("Успешное преобразование данных в набор данных: {}", dataset.getId());
            return dataset;
        } catch (Exception e) {
            logger.error("Ошибка преобразования данных в набор данных: {}", data, e);
            throw new TransformationException("Преобразование данных в набор данных не удалось", e);
        }
    }

    public DatasetPoint transformToDatasetPoint(Map<String, Object> data) {
        logger.debug("Преобразование данных в точку набора данных: {}", data);
        try {
            DatasetPoint point = new DatasetPoint();
            point.setDatasetId(getLongValue(data.get("datasetId")));
            point.setPointIndex(getIntegerValue(data.get("pointIndex")));
            point.setXValue(getBigDecimalValue(data.get("xValue")));
            point.setYValue(getBigDecimalValue(data.get("yValue")));
            logger.info("Успешное преобразование данных в точку набора данных: datasetId={}, index={}",
                    point.getDatasetId(), point.getPointIndex());
            return point;
        } catch (Exception e) {
            logger.error("Ошибка преобразования данных в точку набора данных: {}", data, e);
            throw new TransformationException("Преобразования данных в точку набора данных не удалось", e);
        }
    }

    public UserRole transformToUserRole(Map<String, Object> data) {
        logger.debug("Преобразование данных в роль пользователя: {}", data);
        try {
            UserRole userRole = new UserRole();
            userRole.setUserId(getLongValue(data.get("userId")));
            userRole.setRoleCode((String) data.get("roleCode"));
            logger.info("Успешное преобразование данных в роль пользователя: userId={}, roleCode={}",
                    userRole.getUserId(), userRole.getRoleCode());
            return userRole;
        } catch (Exception e) {
            logger.error("Ошибка преобразования данных в роль пользователя: {}", data, e);
            throw new TransformationException("Преобразование данных в роль пользователя не удалось", e);
        }
    }

    public PerfomanceMetrics transformToPerformanceMetrics(Map<String, Object> data) {
        logger.debug("Преобразование данных в проверку мощности: {}", data);
        try {
            PerfomanceMetrics metrics = new PerfomanceMetrics();
            metrics.setId(getLongValue(data.get("id")));
            metrics.setEngine((String) data.get("engine"));
            metrics.setOperation((String) data.get("operation"));
            metrics.setRecordsProcessed(getIntegerValue(data.get("recordsProcessed")));
            metrics.setElapsedMs(getIntegerValue(data.get("elapsedMs")));

            logger.info("Успешное преобразование данных в проверку мощности: id={}, operation={}",
                    metrics.getId(), metrics.getOperation());
            return metrics;
        } catch (Exception e) {
            logger.error("Ошибка преобразования данных в проверку мощности: {}", data, e);
            throw new TransformationException("Преобразование данных в проверку мощности не удалось", e);
        }
    }

    private Long getLongValue(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) return Long.parseLong((String) value);
        throw new IllegalArgumentException("Ошибка конвертирования данных в LongValue: " + value);
    }

    private Integer getIntegerValue(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof String) return Integer.parseInt((String) value);
        throw new IllegalArgumentException("Ошибка конвертирования данных в IntegerValue: " + value);
    }

    private Short getShortValue(Object value) {
        if (value == null) return null;
        if (value instanceof Short) return (Short) value;
        if (value instanceof Integer) return ((Integer) value).shortValue();
        if (value instanceof String) return Short.parseShort((String) value);
        throw new IllegalArgumentException("Ошибка конвертирования данных в ShortValue: " + value);
    }

    private BigDecimal getBigDecimalValue(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Double) return BigDecimal.valueOf((Double) value);
        if (value instanceof String) return new BigDecimal((String) value);
        throw new IllegalArgumentException("Ошибка конвертирования данных в BigDecimalValue: " + value);
    }
}