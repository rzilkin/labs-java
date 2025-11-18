package mathproj.search;

import mathproj.Main;
import mathproj.entities.DatasetPoint;
import mathproj.entities.MathFunction;
import mathproj.entities.PerformanceMetric;
import mathproj.entities.Role;
import mathproj.entities.TabulatedDataset;
import mathproj.entities.User;
import mathproj.repositories.DatasetPointRepository;
import mathproj.repositories.FunctionComponentRepository;
import mathproj.repositories.MathFunctionRepository;
import mathproj.repositories.PerformanceMetricRepository;
import mathproj.repositories.RoleRepository;
import mathproj.repositories.TabulatedDatasetRepository;
import mathproj.repositories.UserRepository;
import mathproj.repositories.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Main.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SearchServiceSortingTest {

    private static final Logger logger = LoggerFactory.getLogger(SearchServiceSortingTest.class);
    private static final int LARGE_COUNT = 10_500;
    private static final int ROLE_COUNT = 4_000;
    private static final int METRIC_COUNT = 11_000;

    @Autowired
    private SearchService searchService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private MathFunctionRepository mathFunctionRepository;
    @Autowired
    private TabulatedDatasetRepository tabulatedDatasetRepository;
    @Autowired
    private DatasetPointRepository datasetPointRepository;
    @Autowired
    private FunctionComponentRepository functionComponentRepository;
    @Autowired
    private PerformanceMetricRepository performanceMetricRepository;

    private final Random random = new Random(42);

    @BeforeEach
    void cleanDatabase() {
        datasetPointRepository.deleteAll();
        functionComponentRepository.deleteAll();
        tabulatedDatasetRepository.deleteAll();
        userRoleRepository.deleteAll();
        mathFunctionRepository.deleteAll();
        performanceMetricRepository.deleteAll();
        roleRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testFindUsersSortedByUsername() {
        generateUsers(LARGE_COUNT);
        searchService.findUsersSortedByUsername();

        MeasurementResult<User> result = measure(searchService::findUsersSortedByUsername);
        assertFalse(result.data().isEmpty(), "Expected users in repository");
        assertSorted(result.data(), User::getUsername, "Users should be sorted by username");

        recordMetric("users#sortedByUsername", result.data().size(), result.elapsedMs());
    }

    @Test
    void testFindFunctionsSortedByName() {
        User owner = userRepository.save(new User("function-owner", "hash"));
        generateMathFunctions(owner, LARGE_COUNT);
        searchService.findFunctionsSortedByName();

        MeasurementResult<MathFunction> result = measure(searchService::findFunctionsSortedByName);
        assertFalse(result.data().isEmpty(), "Expected functions in repository");
        assertSorted(result.data(), MathFunction::getName, "Functions should be sorted by name");

        recordMetric("math_functions#sortedByName", result.data().size(), result.elapsedMs());
    }

    @Test
    void testFindFunctionsSortedById() {
        User owner = userRepository.save(new User("function-owner-id", "hash"));
        generateMathFunctions(owner, LARGE_COUNT);
        searchService.findFunctionsSortedById();

        MeasurementResult<MathFunction> result = measure(searchService::findFunctionsSortedById);
        assertFalse(result.data().isEmpty(), "Expected functions in repository");
        assertSorted(result.data(), MathFunction::getId, "Functions should be sorted by ID");

        recordMetric("math_functions#sortedById", result.data().size(), result.elapsedMs());
    }

    @Test
    void testFindRolesSortedByCode() {
        generateRoles(ROLE_COUNT);
        searchService.findRolesSortedByCode();

        MeasurementResult<Role> result = measure(searchService::findRolesSortedByCode);
        assertFalse(result.data().isEmpty(), "Expected roles in repository");
        assertSorted(result.data(), Role::getCode, "Roles should be sorted by code");

        recordMetric("roles#sortedByCode", result.data().size(), result.elapsedMs());
    }

    @Test
    void testFindDatasetsSortedById() {
        generateDatasets(LARGE_COUNT);
        searchService.findDatasetsSortedById();

        MeasurementResult<TabulatedDataset> result = measure(searchService::findDatasetsSortedById);
        assertFalse(result.data().isEmpty(), "Expected datasets in repository");
        assertSorted(result.data(), TabulatedDataset::getId, "Datasets should be sorted by ID");

        recordMetric("tabulated_datasets#sortedById", result.data().size(), result.elapsedMs());
    }

    @Test
    void testFindDatasetPointsSortedByIndex() {
        TabulatedDataset dataset = generateDatasetWithPoints(LARGE_COUNT);
        searchService.findDatasetPointsSortedByIndex(dataset.getId());

        MeasurementResult<DatasetPoint> result = measure(() -> searchService.findDatasetPointsSortedByIndex(dataset.getId()));
        assertFalse(result.data().isEmpty(), "Expected dataset points in repository");
        assertSorted(result.data(), DatasetPoint::getPointIndex, "Dataset points should be sorted by index");

        recordMetric("dataset_points#sortedByIndex", result.data().size(), result.elapsedMs());
    }

    @Test
    void testFindDatasetPointsSortedByXValue() {
        TabulatedDataset dataset = generateDatasetWithPoints(LARGE_COUNT);
        searchService.findDatasetPointsSortedByXValue(dataset.getId());

        MeasurementResult<DatasetPoint> result = measure(() -> searchService.findDatasetPointsSortedByXValue(dataset.getId()));
        assertFalse(result.data().isEmpty(), "Expected dataset points in repository");
        assertSorted(result.data(), DatasetPoint::getXValue, "Dataset points should be sorted by X value");

        recordMetric("dataset_points#sortedByXValue", result.data().size(), result.elapsedMs());
    }

    @Test
    void testFindPerformanceMetricsSortedBySpeed() {
        generatePerformanceMetrics(METRIC_COUNT);
        searchService.findPerformanceMetricsSortedBySpeed();

        MeasurementResult<PerformanceMetric> result = measure(searchService::findPerformanceMetricsSortedBySpeed);
        assertFalse(result.data().isEmpty(), "Expected metrics in repository");
        assertSorted(result.data(), PerformanceMetric::getElapsedMs, "Metrics should be sorted by elapsed time");

        recordMetric("performance_metrics#sortedByElapsedMs", result.data().size(), result.elapsedMs());
    }

    @Test
    void testFindPerformanceMetricsSortedByRecords() {
        generatePerformanceMetrics(METRIC_COUNT);
        searchService.findPerformanceMetricsSortedByRecords();

        MeasurementResult<PerformanceMetric> result = measure(searchService::findPerformanceMetricsSortedByRecords);
        assertFalse(result.data().isEmpty(), "Expected metrics in repository");
        assertSortedDescending(result.data(), PerformanceMetric::getRecordsProcessed, "Metrics should be sorted by records processed");

        recordMetric("performance_metrics#sortedByRecords", result.data().size(), result.elapsedMs());
    }

    private List<User> generateUsers(int count) {
        List<User> users = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            users.add(new User("user_" + i, "pass_" + i));
        }
        return userRepository.saveAll(users);
    }

    private void generateRoles(int count) {
        List<Role> roles = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            roles.add(new Role("ROLE_" + i, "Role description " + i));
        }
        roleRepository.saveAll(roles);
    }

    private void generateMathFunctions(User owner, int count) {
        List<MathFunction> functions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            functions.add(new MathFunction(owner, "function_" + i, MathFunction.FunctionType.ANALYTIC, "{\"expr\":\"x\"}"));
        }
        mathFunctionRepository.saveAll(functions);
    }

    private void generateDatasets(int count) {
        User owner = userRepository.save(new User("dataset-owner", "hash"));
        MathFunction function = mathFunctionRepository.save(new MathFunction(owner, "dataset_function", MathFunction.FunctionType.TABULATED, "{\"expr\":\"x\"}"));
        List<TabulatedDataset> datasets = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            datasets.add(new TabulatedDataset(function, TabulatedDataset.SourceType.GENERATED));
        }
        tabulatedDatasetRepository.saveAll(datasets);
    }

    private TabulatedDataset generateDatasetWithPoints(int pointsCount) {
        User owner = userRepository.save(new User("dataset-points-owner", "hash"));
        MathFunction function = mathFunctionRepository.save(new MathFunction(owner, "dataset_points_function", MathFunction.FunctionType.TABULATED, "{\"expr\":\"x\"}"));
        TabulatedDataset dataset = tabulatedDatasetRepository.save(new TabulatedDataset(function, TabulatedDataset.SourceType.MANUAL));

        List<DatasetPoint> points = new ArrayList<>(pointsCount);
        for (int i = 0; i < pointsCount; i++) {
            BigDecimal xValue = BigDecimal.valueOf(random.nextDouble() * 10_000).setScale(2, RoundingMode.HALF_UP);
            BigDecimal yValue = xValue.multiply(BigDecimal.valueOf(2)).setScale(2, RoundingMode.HALF_UP);
            points.add(new DatasetPoint(dataset, i, xValue, yValue));
        }
        datasetPointRepository.saveAll(points);
        return dataset;
    }

    private void generatePerformanceMetrics(int count) {
        List<PerformanceMetric> metrics = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int elapsed = 10 + (i % 100);
            int records = 1_000 + (i % 5_000);
            String operation = "operation_" + (i % 10);
            metrics.add(new PerformanceMetric(PerformanceMetric.Engine.FRAMEWORK_ORM, operation, records, elapsed));
        }
        performanceMetricRepository.saveAll(metrics);
    }

    private void recordMetric(String operation, int processed, long elapsedMs) {
        int elapsed = (int) Math.min(elapsedMs, Integer.MAX_VALUE);
        PerformanceMetric metric = new PerformanceMetric(PerformanceMetric.Engine.FRAMEWORK_ORM, operation, processed, elapsed);
        performanceMetricRepository.save(metric);
        logger.info("{} took {} ms for {} records", operation, elapsed, processed);
    }

    private <T, U extends Comparable<U>> void assertSorted(List<T> items, Function<T, U> keyExtractor, String message) {
        for (int i = 1; i < items.size(); i++) {
            U previous = keyExtractor.apply(items.get(i - 1));
            U current = keyExtractor.apply(items.get(i));
            assertTrue(previous.compareTo(current) <= 0, message + " at position " + i);
        }
    }

    private <T, U extends Comparable<U>> void assertSortedDescending(List<T> items, Function<T, U> keyExtractor, String message) {
        for (int i = 1; i < items.size(); i++) {
            U previous = keyExtractor.apply(items.get(i - 1));
            U current = keyExtractor.apply(items.get(i));
            assertTrue(previous.compareTo(current) >= 0, message + " at position " + i);
        }
    }

    private <T> MeasurementResult<T> measure(Supplier<List<T>> supplier) {
        long start = System.nanoTime();
        List<T> data = supplier.get();
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        return new MeasurementResult<>(data, elapsedMs);
    }

    private record MeasurementResult<T>(List<T> data, long elapsedMs) {
    }
}