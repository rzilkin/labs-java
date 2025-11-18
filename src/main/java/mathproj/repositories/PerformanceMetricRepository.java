package mathproj.repositories;

import mathproj.entities.PerformanceMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PerformanceMetricRepository extends JpaRepository<PerformanceMetric, Long> {
    List<PerformanceMetric> findAllByOrderByElapsedMsAsc();
    List<PerformanceMetric> findAllByOrderByElapsedMsDesc();

    List<PerformanceMetric> findAllByOrderByRecordsProcessedDesc();

    @Query("SELECT pm FROM PerformanceMetric pm WHERE pm.engine = :engine ORDER BY pm.elapsedMs ASC")
    List<PerformanceMetric> findByEngineOrderByElapsedMsAsc(PerformanceMetric.Engine engine);

    @Query("SELECT pm FROM PerformanceMetric pm WHERE pm.operation LIKE %:operation% ORDER BY pm.elapsedMs DESC")
    List<PerformanceMetric> findByOperationContainingOrderByElapsedMsDesc(String operation);

    @Query("SELECT pm FROM PerformanceMetric pm WHERE pm.elapsedMs > :threshold ORDER BY pm.elapsedMs DESC")
    List<PerformanceMetric> findSlowOperations(Integer threshold);

    @Query("SELECT pm FROM PerformanceMetric pm WHERE pm.elapsedMs < :threshold ORDER BY pm.elapsedMs ASC")
    List<PerformanceMetric> findFastOperations(Integer threshold);

    @Query("SELECT pm FROM PerformanceMetric pm WHERE pm.recordsProcessed BETWEEN :minRecords AND :maxRecords ORDER BY pm.recordsProcessed")
    List<PerformanceMetric> findByRecordsProcessedBetweenOrderByRecordsProcessed(Integer minRecords, Integer maxRecords);

    @Query("SELECT pm.engine, AVG(pm.elapsedMs), MAX(pm.elapsedMs), MIN(pm.elapsedMs), COUNT(pm) FROM PerformanceMetric pm GROUP BY pm.engine")
    List<Object[]> getEngineStatistics();

    @Query("SELECT pm.operation, AVG(pm.elapsedMs), COUNT(pm) FROM PerformanceMetric pm GROUP BY pm.operation HAVING COUNT(pm) > 1 ORDER BY AVG(pm.elapsedMs) DESC")
    List<Object[]> getOperationStatistics();

    @Query("SELECT pm FROM PerformanceMetric pm ORDER BY pm.id DESC LIMIT :limit")
    List<PerformanceMetric> findRecentMetrics(int limit);
}