package dao;

import dto.TabulatedDataset;

import java.util.List;
import java.util.Optional;

public interface TabulatedDatasetDao {
    TabulatedDataset create(TabulatedDataset dataset);

    Optional<TabulatedDataset> findById(Long id);

    List<TabulatedDataset> findByFunctionId(Long functionId);

    boolean update(TabulatedDataset dataset);

    boolean delete(Long id);
}