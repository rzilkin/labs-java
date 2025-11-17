package mathproj.repositories;

import mathproj.entities.PerformanceMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceMetricRepository extends JpaRepository<PerformanceMetric, Long> {
}