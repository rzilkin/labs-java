package mathproj.repositories;

import mathproj.entities.PerformanceMetric;
import org.junit.jupiter.api.Test;

import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

class PerformanceMetricRepositoryTest extends RepositoryIntegrationTestSupport {

    @Test
    void performanceMetricRepositoryCapturesEngineStatistics() {
        performanceMetricRepository.deleteAll();

        PerformanceMetric manual = performanceMetricRepository.save(newMetric(PerformanceMetric.Engine.MANUAL_JDBC, "load", 10, 100));
        performanceMetricRepository.saveAll(List.of(
                newMetric(PerformanceMetric.Engine.FRAMEWORK_ORM, "compute", 20, 200),
                newMetric(PerformanceMetric.Engine.FRAMEWORK_ORM, "aggregate", 30, 300)));

        assertEquals(3, performanceMetricRepository.count());
        assertEquals(3, performanceMetricRepository.findAll().size());

        List<PerformanceMetric> stored = performanceMetricRepository.findAll();
        long manualCount = stored.stream()
                .filter(metric -> metric.getEngine() == PerformanceMetric.Engine.MANUAL_JDBC)
                .count();
        assertEquals(1, manualCount);

        long loadCount = stored.stream()
                .filter(metric -> metric.getOperation().toLowerCase().contains("load"))
                .count();
        assertEquals(1, loadCount);

        performanceMetricRepository.delete(manual);
        assertEquals(2, performanceMetricRepository.count());

        performanceMetricRepository.deleteAll(performanceMetricRepository.findAll());
        assertEquals(0, performanceMetricRepository.count());
    }
}