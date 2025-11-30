package service;

import dao.DatasetPointDao;
import dao.FunctionComponentDao;
import dao.JdbcDatasetPointDao;
import dao.JdbcFunctionComponentDao;
import dao.JdbcMathFunctionDao;
import dao.JdbcPerformanceMetricDao;
import dao.JdbcTabulatedDatasetDao;
import dao.JdbcUserDao;
import dao.MathFunctionDao;
import dao.PerformanceMetricDao;
import dao.TabulatedDatasetDao;
import dao.UserDao;
import db.DatabaseConfig;
import db.DatabaseConfigLoader;
import db.DatabaseConnectionManager;
import operations.TabulatedFunctionOperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServiceLocator {
    private static final Logger logger = LoggerFactory.getLogger(ServiceLocator.class);
    private static final ServiceLocator INSTANCE = new ServiceLocator();

    private final DatabaseConnectionManager connectionManager;
    private final UserDao userDao;
    private final MathFunctionDao mathFunctionDao;
    private final TabulatedDatasetDao tabulatedDatasetDao;
    private final DatasetPointDao datasetPointDao;
    private final PerformanceMetricDao performanceMetricDao;
    private final FunctionComponentDao functionComponentDao;

    private final AuthService authService;
    private final FunctionService functionService;
    private final OperationService operationService;
    private final MetricsService metricsService;

    private ServiceLocator() {
        DatabaseConfigLoader loader = new DatabaseConfigLoader();
        DatabaseConfig config = loader.load();
        logger.info("Инициализация ServiceLocator с конфигурацией БД {}", config.getUrl());

        this.connectionManager = new DatabaseConnectionManager(config);
        this.userDao = new JdbcUserDao(connectionManager);
        this.mathFunctionDao = new JdbcMathFunctionDao(connectionManager);
        this.tabulatedDatasetDao = new JdbcTabulatedDatasetDao(connectionManager);
        this.datasetPointDao = new JdbcDatasetPointDao(connectionManager);
        this.performanceMetricDao = new JdbcPerformanceMetricDao(connectionManager);
        this.functionComponentDao = new JdbcFunctionComponentDao(connectionManager);

        this.authService = new AuthService(userDao);
        this.functionService = new FunctionService(mathFunctionDao, tabulatedDatasetDao,
                datasetPointDao, functionComponentDao);
        this.operationService = new OperationService(mathFunctionDao, tabulatedDatasetDao,
                datasetPointDao, new TabulatedFunctionOperationService());
        this.metricsService = new MetricsService(performanceMetricDao);
    }

    public static ServiceLocator getInstance() {
        return INSTANCE;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public FunctionService getFunctionService() {
        return functionService;
    }

    public OperationService getOperationService() {
        return operationService;
    }

    public MetricsService getMetricsService() {
        return metricsService;
    }
}