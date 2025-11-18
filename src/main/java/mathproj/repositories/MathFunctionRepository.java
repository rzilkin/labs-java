package mathproj.repositories;

import mathproj.entities.MathFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface MathFunctionRepository extends JpaRepository<MathFunction, Long> {
    List<MathFunction> findAllByOrderByNameAsc();
    List<MathFunction> findAllByOrderByIdAsc();

    List<MathFunction> findByFunctionTypeOrderByNameAsc(MathFunction.FunctionType functionType);

    @Query("SELECT f FROM MathFunction f WHERE f.name LIKE %:name%")
    List<MathFunction> findByNameContaining(String name);

    @Query("SELECT f FROM MathFunction f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY f.name")
    List<MathFunction> findByNameContainingIgnoreCaseOrderByName(String name);

    @Query("SELECT f FROM MathFunction f WHERE f.owner.id = :ownerId ORDER BY f.name")
    List<MathFunction> findByOwnerIdOrderByName(Long ownerId);

    @Query("SELECT f FROM MathFunction f WHERE f.functionType = 'COMPOSITE' ORDER BY f.name")
    List<MathFunction> findCompositeFunctions();

    @Query("SELECT f FROM MathFunction f WHERE f.functionType = 'ANALYTIC' ORDER BY f.name")
    List<MathFunction> findAnalyticFunctions();

    @Query("SELECT f FROM MathFunction f WHERE f.functionType = :type AND f.name LIKE %:name% ORDER BY f.name")
    List<MathFunction> findByTypeAndNameContaining(MathFunction.FunctionType type, String name);
}