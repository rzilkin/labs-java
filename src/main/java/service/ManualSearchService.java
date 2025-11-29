package service;

import dao.DatasetPointDao;
import dao.FunctionComponentDao;
import dao.MathFunctionDao;
import dao.PerformanceMetricDao;
import dao.TabulatedDatasetDao;
import dao.UserDao;
import dto.DatasetPoint;
import dto.FunctionComponents;
import dto.MathFunction;
import dto.PerformanceMetrics;
import dto.TabulatedDataset;
import dto.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.HashSet;

public class ManualSearchService {
    private static final Logger logger = LoggerFactory.getLogger(ManualSearchService.class);

    private final UserDao userDao;
    private final MathFunctionDao mathFunctionDao;
    private final TabulatedDatasetDao tabulatedDatasetDao;
    private final DatasetPointDao datasetPointDao;
    private final PerformanceMetricDao performanceMetricDao;
    private final FunctionComponentDao functionComponentDao;

    public ManualSearchService(UserDao userDao,
                               MathFunctionDao mathFunctionDao,
                               TabulatedDatasetDao tabulatedDatasetDao,
                               DatasetPointDao datasetPointDao,
                               PerformanceMetricDao performanceMetricDao,
                               FunctionComponentDao functionComponentDao) {
        this.userDao = userDao;
        this.mathFunctionDao = mathFunctionDao;
        this.tabulatedDatasetDao = tabulatedDatasetDao;
        this.datasetPointDao = datasetPointDao;
        this.performanceMetricDao = performanceMetricDao;
        this.functionComponentDao = functionComponentDao;
    }

    // --- Одиночный поиск ---
    public Optional<User> findUserById(Long id) {
        logger.info("Поиск пользователя по id {}", id);
        Optional<User> user = userDao.findById(id);
        logOptionalResult("пользователь", user);
        return user;
    }

    public Optional<User> findUserByUsername(String username) {
        logger.info("Поиск пользователя по username {}", username);
        Optional<User> user = userDao.findByUsername(username);
        logOptionalResult("пользователь", user);
        return user;
    }

    public Optional<MathFunction> findFunctionById(Long id) {
        logger.info("Поиск функции по id {}", id);
        Optional<MathFunction> function = mathFunctionDao.findById(id);
        logOptionalResult("функция", function);
        return function;
    }

    public Optional<MathFunction> findFunctionByName(String name) {
        logger.info("Поиск функции по имени {}", name);
        Optional<MathFunction> function = mathFunctionDao.findByName(name);
        logOptionalResult("функция", function);
        return function;
    }

    public Optional<TabulatedDataset> findDatasetById(Long id) {
        logger.info("Поиск табличного набора данных по id {}", id);
        Optional<TabulatedDataset> dataset = tabulatedDatasetDao.findById(id);
        logOptionalResult("набор данных", dataset);
        return dataset;
    }

    // --- Множественный поиск ---
    public List<User> findAllUsers() {
        logger.info("Загрузка всех пользователей");
        List<User> users = userDao.findAll();
        logger.info("Загружено {} пользователей", users.size());
        return users;
    }

    public List<User> findAllUsersOrderByUsername() {
        logger.info("Загрузка всех пользователей в алфавитном порядке");
        List<User> users = userDao.findAllOrderByUsernameAsc();
        logger.info("Загружено {} пользователей", users.size());
        return users;
    }

    public List<MathFunction> findAllFunctions() {
        logger.info("Загрузка всех функций");
        List<MathFunction> functions = mathFunctionDao.findAll();
        logger.info("Загружено {} функций", functions.size());
        return functions;
    }

    public List<MathFunction> findAllFunctionsOrderByName() {
        logger.info("Загрузка всех функций по имени (ASC)");
        List<MathFunction> functions = mathFunctionDao.findAllOrderByNameAsc();
        logger.info("Загружено {} функций", functions.size());
        return functions;
    }

    public List<TabulatedDataset> findAllDatasets() {
        logger.info("Загрузка всех наборов данных");
        List<TabulatedDataset> datasets = tabulatedDatasetDao.findAll();
        logger.info("Загружено {} наборов данных", datasets.size());
        return datasets;
    }

    public List<TabulatedDataset> findAllDatasetsOrderById() {
        logger.info("Загрузка всех наборов данных по id (ASC)");
        List<TabulatedDataset> datasets = tabulatedDatasetDao.findAllOrderByIdAsc();
        logger.info("Загружено {} наборов данных", datasets.size());
        return datasets;
    }

    public List<TabulatedDataset> findDatasetsByFunctionId(Long functionId) {
        logger.info("Загрузка наборов данных для функции {}", functionId);
        List<TabulatedDataset> datasets = tabulatedDatasetDao.findByFunctionId(functionId);
        logger.info("Найдено {} наборов данных для функции {}", datasets.size(), functionId);
        return datasets;
    }

    public List<DatasetPoint> findDatasetPoints(Long datasetId) {
        logger.info("Загрузка точек набора данных {}", datasetId);
        List<DatasetPoint> points = datasetPointDao.findByDatasetId(datasetId);
        logger.info("Найдено {} точек для набора {}", points.size(), datasetId);
        return points;
    }

    public List<DatasetPoint> findDatasetPointsOrderByIndex(Long datasetId) {
        logger.info("Загрузка точек набора {} по индексу", datasetId);
        List<DatasetPoint> points = datasetPointDao.findByDatasetIdOrderByPointIndex(datasetId);
        logger.info("Найдено {} точек", points.size());
        return points;
    }

    public List<DatasetPoint> findDatasetPointsOrderByX(Long datasetId) {
        logger.info("Загрузка точек набора {} по значению X", datasetId);
        List<DatasetPoint> points = datasetPointDao.findByDatasetIdOrderByXValue(datasetId);
        logger.info("Найдено {} точек", points.size());
        return points;
    }

    public List<PerformanceMetrics> findAllPerformanceMetrics() {
        logger.info("Загрузка всех метрик производительности");
        List<PerformanceMetrics> metrics = performanceMetricDao.findAll();
        logger.info("Загружено {} метрик", metrics.size());
        return metrics;
    }

    public List<PerformanceMetrics> findPerformanceMetricsOrderByElapsed() {
        logger.info("Загрузка метрик производительности по времени выполнения");
        List<PerformanceMetrics> metrics = performanceMetricDao.findAllOrderByElapsedMsAsc();
        logger.info("Загружено {} метрик", metrics.size());
        return metrics;
    }

    public List<PerformanceMetrics> findPerformanceMetricsOrderByRecordsProcessed() {
        logger.info("Загрузка метрик производительности по количеству обработанных записей");
        List<PerformanceMetrics> metrics = performanceMetricDao.findAllOrderByRecordsProcessedDesc();
        logger.info("Загружено {} метрик", metrics.size());
        return metrics;
    }

    // --- Поиск по иерархии ---
    public List<MathFunction> depthFirstTraversal(Long rootFunctionId) {
        logger.info("DFS по иерархии функций от корня {}", rootFunctionId);
        List<MathFunction> result = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        dfs(rootFunctionId, visited, result);
        logger.info("DFS завершён. Обработано {} функций", result.size());
        return result;
    }

    public List<MathFunction> breadthFirstTraversal(Long rootFunctionId) {
        logger.info("BFS по иерархии функций от корня {}", rootFunctionId);
        List<MathFunction> result = new ArrayList<>();
        if (rootFunctionId == null) {
            logger.info("Корень не задан, возвращён пустой результат");
            return result;
        }
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new ArrayDeque<>();
        queue.offer(rootFunctionId);
        while (!queue.isEmpty()) {
            Long currentId = queue.poll();
            if (currentId == null || !visited.add(currentId)) {
                logger.debug("Функция {} уже посещена, пропускаем", currentId);
                continue;
            }
            addFunctionToResult(currentId, result);
            List<FunctionComponents> components = functionComponentDao.findByCompositeIdOrderByPosition(currentId);
            for (FunctionComponents component : components) {
                logger.debug("BFS добавляет компонент {} функции {} в очередь", component.getComponentId(), currentId);
                queue.offer(component.getComponentId());
            }
        }
        logger.info("BFS завершён. Обработано {} функций", result.size());
        return result;
    }

    public List<MathFunction> findCompositeFunctionsUsingComponent(Long componentId) {
        logger.info("Поиск композитных функций, использующих компонент {}", componentId);
        List<Long> compositeIds = functionComponentDao.findCompositeIdsByComponentId(componentId);
        LinkedHashSet<MathFunction> composites = new LinkedHashSet<>();
        for (Long compositeId : compositeIds) {
            logger.debug("Проверка композитной функции {}", compositeId);
            mathFunctionDao.findById(compositeId).ifPresent(function -> {
                logger.debug("Добавлена композитная функция {}", function.getId());
                composites.add(function);
            });
        }
        logger.info("Найдено {} композитных функций для компонента {}", composites.size(), componentId);
        return new ArrayList<>(composites);
    }

    private void dfs(Long currentId, Set<Long> visited, List<MathFunction> accumulator) {
        if (currentId == null || !visited.add(currentId)) {
            logger.debug("DFS пропускает функцию {} (null или уже посещена)", currentId);
            return;
        }
        addFunctionToResult(currentId, accumulator);
        List<FunctionComponents> components = functionComponentDao.findByCompositeIdOrderByPosition(currentId);
        for (FunctionComponents component : components) {
            logger.debug("DFS переходит от функции {} к компоненту {}", currentId, component.getComponentId());
            dfs(component.getComponentId(), visited, accumulator);
        }
    }

    private void addFunctionToResult(Long functionId, List<MathFunction> accumulator) {
        mathFunctionDao.findById(functionId).ifPresent(function -> {
            logger.debug("Добавлена функция {} ({})", function.getId(), function.getName());
            accumulator.add(function);
        });
    }

    private <T> void logOptionalResult(String entityName, Optional<T> result) {
        if (result.isPresent()) {
            logger.info("{} найден", entityName);
        } else {
            logger.info("{} не найден", entityName);
        }
    }
}
