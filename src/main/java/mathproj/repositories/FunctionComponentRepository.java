package mathproj.repositories;

import mathproj.entities.FunctionComponent;
import mathproj.entities.FunctionComponentId;
import mathproj.entities.MathFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface FunctionComponentRepository extends JpaRepository<FunctionComponent, FunctionComponentId> {
    @Query("SELECT fc FROM FunctionComponent fc WHERE fc.composite.id = :compositeId ORDER BY fc.position ASC")
    List<FunctionComponent> findByCompositeIdOrderByPositionAsc(Long compositeId);

    @Query("SELECT fc.component FROM FunctionComponent fc WHERE fc.composite.id = :compositeId ORDER BY fc.position")
    List<mathproj.entities.MathFunction> findComponentsByCompositeId(Long compositeId);

    @Query("SELECT fc.composite FROM FunctionComponent fc WHERE fc.component.id = :componentId ORDER BY fc.composite.name")
    List<mathproj.entities.MathFunction> findCompositesByComponentId(Long componentId);

    @Query("SELECT fc FROM FunctionComponent fc WHERE fc.composite.id = :compositeId AND fc.position = :position")
    Optional<FunctionComponent> findByCompositeIdAndPosition(Long compositeId, Short position);

    @Query("SELECT MAX(fc.position) FROM FunctionComponent fc WHERE fc.composite.id = :compositeId")
    Short findMaxPositionByCompositeId(Long compositeId);

    @Query("SELECT fc FROM FunctionComponent fc WHERE fc.component.functionType = :functionType ORDER BY fc.composite.name, fc.position")
    List<FunctionComponent> findByComponentFunctionTypeOrderByCompositeAndPosition(MathFunction.FunctionType functionType);

    @Query("SELECT fc FROM FunctionComponent fc ORDER BY fc.composite.name, fc.position")
    List<FunctionComponent> findAllOrderByCompositeAndPosition();
}