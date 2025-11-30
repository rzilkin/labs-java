package service;

import dao.PerformanceMetricDao;
import dto.PerformanceMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class MetricsService {
    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);

    private final PerformanceMetricDao performanceMetricDao;

    public MetricsService(PerformanceMetricDao performanceMetricDao) {
        this.performanceMetricDao = Objects.requireNonNull(performanceMetricDao, "performanceMetricDao");
        logger.info("MetricsService инициализирован");
    }

    public PerformanceMetrics saveMetric(PerformanceMetrics metrics) {
        logger.info("Сохранение метрики операции {}", metrics.getOperation());
        return performanceMetricDao.create(metrics);
    }

    public List<PerformanceMetrics> getAll() {
        logger.debug("Запрос списка всех метрик");
        return performanceMetricDao.findAll();
    }
}