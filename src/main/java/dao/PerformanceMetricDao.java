package dao;

import dto.PerfomanceMetrics;

import java.util.List;

public interface PerformanceMetricDao {
    PerfomanceMetrics create(PerfomanceMetrics metric);

    List<PerfomanceMetrics> findAll();

    List<PerfomanceMetrics> findAllOrderByElapsedMsAsc();

    List<PerfomanceMetrics> findAllOrderByRecordsProcessedDesc();
}