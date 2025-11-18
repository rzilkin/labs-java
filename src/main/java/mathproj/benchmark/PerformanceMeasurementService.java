package mathproj.benchmark;

import mathproj.entities.PerformanceMetric;
import mathproj.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

@Service
public class PerformanceMeasurementService {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMeasurementService.class);

    private final PerformanceMetricRepository performanceMetricRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final MathFunctionRepository mathFunctionRepository;
    private final TabulatedDatasetRepository tabulatedDatasetRepository;
    private final DatasetPointRepository datasetPointRepository;
    private final FunctionComponentRepository functionComponentRepository;

    public PerformanceMeasurementService(PerformanceMetricRepository performanceMetricRepository,
                                         UserRepository userRepository,
                                         RoleRepository roleRepository,
                                         UserRoleRepository userRoleRepository,
                                         MathFunctionRepository mathFunctionRepository,
                                         TabulatedDatasetRepository tabulatedDatasetRepository,
                                         DatasetPointRepository datasetPointRepository,
                                         FunctionComponentRepository functionComponentRepository) {
        this.performanceMetricRepository = performanceMetricRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.mathFunctionRepository = mathFunctionRepository;
        this.tabulatedDatasetRepository = tabulatedDatasetRepository;
        this.datasetPointRepository = datasetPointRepository;
        this.functionComponentRepository = functionComponentRepository;
    }

    @Transactional
    public void runBenchmarks() {
        performanceMetricRepository.deleteAllInBatch();
        logger.info("Начато измерение скорости выполнения запросов");
        measureCollectionOperation("users#findAll", () -> userRepository.findAll());
        measureCollectionOperation("roles#findAll", () -> roleRepository.findAll());
        measureCollectionOperation("user_roles#findAll", () -> userRoleRepository.findAll());
        measureCollectionOperation("math_functions#findAll", () -> mathFunctionRepository.findAll());
        measureCollectionOperation("tabulated_datasets#findAll", () -> tabulatedDatasetRepository.findAll());
        measureCollectionOperation("dataset_points#findAll", () -> datasetPointRepository.findAll());
        measureCollectionOperation("function_components#findAll", () -> functionComponentRepository.findAll());
        measureCountOperation("dataset_points#count", () -> datasetPointRepository.count());
        logger.info("Измерение выполнено, результаты сохранены в performance_metrics");
    }

    private <T> void measureCollectionOperation(String operationName, Supplier<List<T>> supplier) {
        long start = System.nanoTime();
        List<T> result = supplier.get();
        long elapsed = System.nanoTime() - start;
        saveMetric(operationName, result.size(), elapsed);
    }

    private void measureCountOperation(String operationName, LongSupplier supplier) {
        long start = System.nanoTime();
        long count = supplier.getAsLong();
        long elapsed = System.nanoTime() - start;
        saveMetric(operationName, (int) count, elapsed);
    }

    private void saveMetric(String operationName, int recordsProcessed, long elapsedNanos) {
        int elapsedMs = (int) TimeUnit.NANOSECONDS.toMillis(elapsedNanos);
        PerformanceMetric metric = new PerformanceMetric(
                PerformanceMetric.Engine.FRAMEWORK_ORM,
                operationName,
                recordsProcessed,
                elapsedMs
        );
        performanceMetricRepository.save(metric);
        logger.debug("{} -> {} записей за {} мс", operationName, recordsProcessed, elapsedMs);
    }
}