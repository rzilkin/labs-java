package dao;

import dto.DatasetPoint;
import dto.MathFunction;
import dto.PerformanceMetrics;
import dto.TabulatedDataset;
import dto.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SortingAndPerformanceIntegrationTest extends AbstractDaoIntegrationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SortingAndPerformanceIntegrationTest.class);
    private static final int LARGE_SAMPLE_SIZE = 10_000;
    private static final String ENGINE = "MANUAL_JDBC";

    private JdbcUserDao userDao;
    private JdbcMathFunctionDao mathFunctionDao;
    private JdbcTabulatedDatasetDao tabulatedDatasetDao;
    private JdbcDatasetPointDao datasetPointDao;
    private JdbcPerformanceMetricDao performanceMetricDao;

    @BeforeEach
    void initDaos() {
        userDao = new JdbcUserDao(connectionManager);
        mathFunctionDao = new JdbcMathFunctionDao(connectionManager);
        tabulatedDatasetDao = new JdbcTabulatedDatasetDao(connectionManager);
        datasetPointDao = new JdbcDatasetPointDao(connectionManager);
        performanceMetricDao = new JdbcPerformanceMetricDao(connectionManager);
    }

    @Test
    void shouldSortUsersByUsernameAsc() {
        createUser("c_user");
        createUser("a_user");
        createUser("b_user");

        List<User> sorted = userDao.findAllOrderByUsernameAsc();
        assertEquals(3, sorted.size());
        assertSortedAscending(sorted.stream().map(User::getUsername).collect(Collectors.toList()));
    }

    @Test
    void shouldSortFunctionsByNameAsc() {
        User owner = createUser("function_owner");
        createFunction(owner.getId(), "gamma_function");
        createFunction(owner.getId(), "alpha_function");
        createFunction(owner.getId(), "beta_function");

        List<MathFunction> sorted = mathFunctionDao.findAllOrderByNameAsc();
        assertEquals(3, sorted.size());
        assertSortedAscending(sorted.stream().map(MathFunction::getName).collect(Collectors.toList()));
    }

    @Test
    void shouldSortDatasetsByIdAsc() {
        User owner = createUser("dataset_owner");
        MathFunction function = createFunction(owner.getId(), "dataset_function");
        createDataset(function.getId());
        createDataset(function.getId());
        createDataset(function.getId());

        List<TabulatedDataset> sorted = tabulatedDatasetDao.findAllOrderByIdAsc();
        assertEquals(3, sorted.size());
        assertSortedAscending(sorted.stream().map(TabulatedDataset::getId).collect(Collectors.toList()));
    }

    @Test
    void shouldSortDatasetPointsByXValueAsc() {
        User owner = createUser("points_owner");
        MathFunction function = createFunction(owner.getId(), "points_function");
        TabulatedDataset dataset = createDataset(function.getId());

        upsertPoint(dataset.getId(), 0, BigDecimal.valueOf(3), BigDecimal.valueOf(9));
        upsertPoint(dataset.getId(), 1, BigDecimal.valueOf(1), BigDecimal.valueOf(1));
        upsertPoint(dataset.getId(), 2, BigDecimal.valueOf(2), BigDecimal.valueOf(4));

        List<DatasetPoint> sorted = datasetPointDao.findByDatasetIdOrderByXValue(dataset.getId());
        assertEquals(3, sorted.size());
        assertSortedAscending(sorted.stream().map(DatasetPoint::getXValue).collect(Collectors.toList()));
    }

    @Test
    void measureUserSortingByUsernamePerformance() {
        createUsers(LARGE_SAMPLE_SIZE);

        long elapsedMs = measureUsersSort();
        recordMetric("USERS_SORT_BY_USERNAME", LARGE_SAMPLE_SIZE, elapsedMs);
        LOGGER.info("USERS_SORT_BY_USERNAME: {} ms для {} записей", elapsedMs, LARGE_SAMPLE_SIZE);
    }

    @Test
    void measureFunctionSortingByNamePerformance() {
        User owner = createUser("functions_bulk_owner");
        createFunctions(LARGE_SAMPLE_SIZE, owner.getId());

        long elapsedMs = measureFunctionsSort();
        recordMetric("MATHFUNC_SORT_BY_NAME", LARGE_SAMPLE_SIZE, elapsedMs);
        LOGGER.info("MATHFUNC_SORT_BY_NAME: {} ms для {} записей", elapsedMs, LARGE_SAMPLE_SIZE);
    }

    @Test
    void measureDatasetPointsSortingByXValuePerformance() {
        User owner = createUser("points_bulk_owner");
        MathFunction function = createFunction(owner.getId(), "bulk_points_function");
        List<TabulatedDataset> datasets = createDatasetsWithPoints(1, function.getId(), 0, LARGE_SAMPLE_SIZE);
        TabulatedDataset dataset = datasets.get(0);

        long elapsedMs = measureDatasetPointsSort(dataset.getId(), LARGE_SAMPLE_SIZE);
        recordMetric("POINTS_SORT_BY_X", LARGE_SAMPLE_SIZE, elapsedMs);
        LOGGER.info("POINTS_SORT_BY_X: {} ms для {} точек", elapsedMs, LARGE_SAMPLE_SIZE);
    }

    private long measureUsersSort() {
        long start = System.nanoTime();
        List<User> users = userDao.findAllOrderByUsernameAsc();
        long elapsed = System.nanoTime() - start;
        assertEquals(LARGE_SAMPLE_SIZE, users.size());
        assertSortedAscending(users.stream().map(User::getUsername).collect(Collectors.toList()));
        assertFalse(users.isEmpty());
        return TimeUnit.NANOSECONDS.toMillis(elapsed);
    }

    private long measureFunctionsSort() {
        long start = System.nanoTime();
        List<MathFunction> functions = mathFunctionDao.findAllOrderByNameAsc();
        long elapsed = System.nanoTime() - start;
        assertEquals(LARGE_SAMPLE_SIZE, functions.size());
        assertSortedAscending(functions.stream().map(MathFunction::getName).collect(Collectors.toList()));
        assertFalse(functions.isEmpty());
        return TimeUnit.NANOSECONDS.toMillis(elapsed);
    }

    private long measureDatasetsSort() {
        long start = System.nanoTime();
        List<TabulatedDataset> datasets = tabulatedDatasetDao.findAllOrderByIdAsc();
        long elapsed = System.nanoTime() - start;
        assertEquals(LARGE_SAMPLE_SIZE, datasets.size());
        assertSortedAscending(datasets.stream().map(TabulatedDataset::getId).collect(Collectors.toList()));
        assertFalse(datasets.isEmpty());
        return TimeUnit.NANOSECONDS.toMillis(elapsed);
    }

    private long measureDatasetPointsSort(Long datasetId, int expectedCount) {
        long start = System.nanoTime();
        List<DatasetPoint> points = datasetPointDao.findByDatasetIdOrderByXValue(datasetId);
        long elapsed = System.nanoTime() - start;
        assertEquals(expectedCount, points.size());
        assertSortedAscending(points.stream().map(DatasetPoint::getXValue).collect(Collectors.toList()));
        assertFalse(points.isEmpty());
        return TimeUnit.NANOSECONDS.toMillis(elapsed);
    }

    private void createUsers(int count) {
        for (int i = 0; i < count; i++) {
            String username = String.format("bulk_user_%05d_%s", i, UUID.randomUUID());
            createUser(username);
        }
    }


    private List<MathFunction> createFunctions(int count, Long ownerId) {
        List<MathFunction> functions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            functions.add(createFunction(ownerId, String.format("function_%05d_%s", i, UUID.randomUUID())));
        }
        return functions;
    }

    private List<TabulatedDataset> createDatasets(int count, Long functionId) {
        List<TabulatedDataset> datasets = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            datasets.add(createDataset(functionId));
        }
        return datasets;
    }

    private List<TabulatedDataset> createDatasetsWithPoints(int datasetCount, Long functionId, int datasetIndexForPoints, int pointsToGenerate) {
        List<TabulatedDataset> datasets = createDatasets(datasetCount, functionId);
        if (!datasets.isEmpty() && datasetIndexForPoints >= 0 && datasetIndexForPoints < datasets.size()) {
            TabulatedDataset target = datasets.get(datasetIndexForPoints);
            createDatasetPoints(target.getId(), pointsToGenerate);
        }
        return datasets;
    }

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash("pwd_" + username);
        return userDao.create(user);
    }

    private MathFunction createFunction(Long ownerId, String name) {
        MathFunction function = new MathFunction();
        function.setOwnerId(ownerId);
        function.setName(name);
        function.setFunctionType("ANALYTIC");
        function.setDefinitionBody(String.format("{\"expression\":\"x^2 + %s\"}", name));
        return mathFunctionDao.create(function);
    }

    private TabulatedDataset createDataset(Long functionId) {
        TabulatedDataset dataset = new TabulatedDataset();
        dataset.setFunctionId(functionId);
        dataset.setSourceType("GENERATED");
        return tabulatedDatasetDao.create(dataset);
    }

    private void createDatasetPoints(Long datasetId, int count) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < count; i++) {
            BigDecimal x = BigDecimal.valueOf(random.nextDouble(0.0, 10_000.0));
            BigDecimal y = x.add(BigDecimal.valueOf(i % 10));
            upsertPoint(datasetId, i, x, y);
        }
    }

    private void upsertPoint(Long datasetId, int pointIndex, BigDecimal xValue, BigDecimal yValue) {
        DatasetPoint point = new DatasetPoint();
        point.setDatasetId(datasetId);
        point.setPointIndex(pointIndex);
        point.setXValue(xValue);
        point.setYValue(yValue);
        datasetPointDao.upsert(point);
    }

    private void recordMetric(String operation, int recordsProcessed, long elapsedMs) {
        PerformanceMetrics metrics = new PerformanceMetrics();
        metrics.setEngine(ENGINE);
        metrics.setOperation(operation);
        metrics.setRecordsProcessed(recordsProcessed);
        metrics.setElapsedMs(safeToInt(elapsedMs));
        performanceMetricDao.create(metrics);
    }

    private int safeToInt(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) value;
    }

    private <T extends Comparable<T>> void assertSortedAscending(List<T> values) {
        for (int i = 1; i < values.size(); i++) {
            T previous = values.get(i - 1);
            T current = values.get(i);
            assertTrue(previous.compareTo(current) <= 0,
                    () -> String.format("Последовательность не отсортирована: %s > %s", previous, current));
        }
    }
}