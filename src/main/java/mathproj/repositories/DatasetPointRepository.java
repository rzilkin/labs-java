package mathproj.repositories;

import mathproj.entities.DatasetPoint;
import mathproj.entities.DatasetPointId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DatasetPointRepository extends JpaRepository<DatasetPoint, DatasetPointId> {
    @Query("SELECT dp FROM DatasetPoint dp WHERE dp.dataset.id = :datasetId ORDER BY dp.pointIndex ASC")
    List<DatasetPoint> findByDatasetIdOrderByPointIndexAsc(Long datasetId);

    @Query("SELECT dp FROM DatasetPoint dp WHERE dp.dataset.id = :datasetId ORDER BY dp.xValue ASC")
    List<DatasetPoint> findByDatasetIdOrderByXValueAsc(Long datasetId);

    @Query("SELECT dp FROM DatasetPoint dp WHERE dp.dataset.id = :datasetId ORDER BY dp.yValue DESC")
    List<DatasetPoint> findByDatasetIdOrderByYValueDesc(Long datasetId);

    @Query("SELECT dp FROM DatasetPoint dp WHERE dp.dataset.id = :datasetId AND dp.xValue BETWEEN :minX AND :maxX")
    List<DatasetPoint> findByDatasetIdAndXValueBetween(Long datasetId, BigDecimal minX, BigDecimal maxX);

    @Query("SELECT dp FROM DatasetPoint dp WHERE dp.dataset.id = :datasetId AND dp.yValue > :minY ORDER BY dp.yValue DESC")
    List<DatasetPoint> findByDatasetIdAndYValueGreaterThanOrderByYValueDesc(Long datasetId, BigDecimal minY);

    @Query("SELECT dp FROM DatasetPoint dp WHERE dp.dataset.id = :datasetId AND dp.pointIndex = :pointIndex")
    Optional<DatasetPoint> findByDatasetIdAndPointIndex(Long datasetId, Integer pointIndex);

    @Query("SELECT dp FROM DatasetPoint dp WHERE dp.dataset.id IN :datasetIds ORDER BY dp.dataset.id, dp.pointIndex")
    List<DatasetPoint> findByDatasetIdsOrderByDatasetAndIndex(List<Long> datasetIds);

    @Query("SELECT COUNT(dp), MIN(dp.xValue), MAX(dp.xValue), AVG(dp.yValue) FROM DatasetPoint dp WHERE dp.dataset.id = :datasetId")
    Object[] getDatasetStats(Long datasetId);

    @Query("SELECT dp.dataset.id, COUNT(dp), AVG(dp.yValue) FROM DatasetPoint dp WHERE dp.dataset.id IN :datasetIds GROUP BY dp.dataset.id")
    List<Object[]> getMultipleDatasetStats(List<Long> datasetIds);

    @Query("SELECT dp FROM DatasetPoint dp WHERE dp.dataset.id = :datasetId AND dp.yValue = (SELECT MAX(dp2.yValue) FROM DatasetPoint dp2 WHERE dp2.dataset.id = :datasetId)")
    List<DatasetPoint> findMaxYValuePoints(Long datasetId);
}