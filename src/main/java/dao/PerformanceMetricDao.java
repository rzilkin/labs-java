package dao;

import dto.PerformanceMetrics;

import java.util.List;

public interface PerformanceMetricDao {
    PerformanceMetrics create(PerformanceMetrics metric);

    List<PerformanceMetrics> findAll();

    List<PerformanceMetrics> findAllOrderByElapsedMsAsc();

    List<PerformanceMetrics> findAllOrderByRecordsProcessedDesc();
}