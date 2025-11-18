package dao;

import dto.FunctionComponents;

import java.util.List;

public interface FunctionComponentDao {
    List<FunctionComponents> findByCompositeId(Long compositeId);

    List<FunctionComponents> findByCompositeIdOrderByPosition(Long compositeId);

    List<Long> findCompositeIdsByComponentId(Long componentId);
}
