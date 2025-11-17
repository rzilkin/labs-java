package mathproj.repositories;

import mathproj.entities.MathFunction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MathFunctionRepository extends JpaRepository<MathFunction, Long> {
}