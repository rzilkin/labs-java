package dao;

import dto.DatasetPoint;

import java.util.List;

public interface DatasetPointDao {
    void upsert(DatasetPoint point);

    List<DatasetPoint> findByDatasetId(Long datasetId);

    boolean deletePoint(Long datasetId, int pointIndex);

    int deleteAllByDataset(Long datasetId);

    long countByDataset(Long datasetId);
}