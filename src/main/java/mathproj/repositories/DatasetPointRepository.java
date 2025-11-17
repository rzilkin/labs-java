package mathproj.repositories;

import mathproj.entities.DatasetPoint;
import mathproj.entities.DatasetPointId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatasetPointRepository extends JpaRepository<DatasetPoint, DatasetPointId> {
}