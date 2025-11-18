package mathproj.repositories;

import mathproj.entities.TabulatedDataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface TabulatedDatasetRepository extends JpaRepository<TabulatedDataset, Long> {
    List<TabulatedDataset> findAllByOrderByIdAsc();

    List<TabulatedDataset> findBySourceTypeOrderByIdAsc(TabulatedDataset.SourceType sourceType);

    @Query("SELECT d FROM TabulatedDataset d WHERE d.function.id = :functionId ORDER BY d.id")
    List<TabulatedDataset> findByFunctionIdOrderByIdAsc(Long functionId);

    @Query("SELECT d FROM TabulatedDataset d WHERE d.function.name LIKE %:functionName% ORDER BY d.id")
    List<TabulatedDataset> findByFunctionNameContaining(String functionName);

    @Query("SELECT d FROM TabulatedDataset d WHERE d.sourceType IN :sourceTypes ORDER BY d.id")
    List<TabulatedDataset> findBySourceTypeInOrderById(List<TabulatedDataset.SourceType> sourceTypes);

    @Query("SELECT d, COUNT(dp) as pointCount FROM TabulatedDataset d LEFT JOIN d.datasetPoints dp GROUP BY d ORDER BY pointCount DESC")
    List<Object[]> findDatasetsWithPointCount();

    List<TabulatedDataset> findByIdIn(List<Long> ids);
}