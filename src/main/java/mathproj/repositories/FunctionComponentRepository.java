package mathproj.repositories;

import mathproj.entities.FunctionComponent;
import mathproj.entities.FunctionComponentId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FunctionComponentRepository extends JpaRepository<FunctionComponent, FunctionComponentId> {
}