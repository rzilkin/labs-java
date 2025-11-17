package mathproj.repositories;

import mathproj.entities.TabulatedDataset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TabulatedDatasetRepository extends JpaRepository<TabulatedDataset, Long> {
}