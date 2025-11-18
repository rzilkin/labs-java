package dao;

import dto.MathFunction;

import java.util.List;
import java.util.Optional;

public interface MathFunctionDao {
    MathFunction create(MathFunction function);

    Optional<MathFunction> findById(Long id);

    List<MathFunction> findByOwner(Long ownerId);

    List<MathFunction> findAll();

    boolean update(MathFunction function);

    boolean delete(Long id);
}