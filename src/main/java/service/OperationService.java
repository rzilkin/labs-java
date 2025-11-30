package service;

import dao.DatasetPointDao;
import dao.MathFunctionDao;
import dao.TabulatedDatasetDao;
import dto.DatasetPoint;
import dto.MathFunction;
import dto.TabulatedDataset;
import functions.ArrayTabulatedFunction;
import functions.TabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import operations.TabulatedDifferentialOperator;
import operations.TabulatedFunctionOperationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OperationService {
    private static final Logger logger = LoggerFactory.getLogger(OperationService.class);

    private final MathFunctionDao mathFunctionDao;
    private final TabulatedDatasetDao tabulatedDatasetDao;
    private final DatasetPointDao datasetPointDao;
    private final TabulatedFunctionOperationService tabulatedFunctionOperationService;
    private final TabulatedDifferentialOperator differentialOperator;

    public OperationService(MathFunctionDao mathFunctionDao,
                            TabulatedDatasetDao tabulatedDatasetDao,
                            DatasetPointDao datasetPointDao,
                            TabulatedFunctionOperationService tabulatedFunctionOperationService) {
        this.mathFunctionDao = Objects.requireNonNull(mathFunctionDao, "mathFunctionDao");
        this.tabulatedDatasetDao = Objects.requireNonNull(tabulatedDatasetDao, "tabulatedDatasetDao");
        this.datasetPointDao = Objects.requireNonNull(datasetPointDao, "datasetPointDao");
        this.tabulatedFunctionOperationService = Objects.requireNonNull(tabulatedFunctionOperationService,
                "tabulatedFunctionOperationService");
        this.differentialOperator = new TabulatedDifferentialOperator(new ArrayTabulatedFunctionFactory());
        logger.info("OperationService инициализирован");
    }

    public MathFunction applyBinaryOperation(Long leftId, Long rightId, String op, Long ownerId) {
        validateOwner(ownerId);
        MathFunction left = loadOwnedFunction(leftId, ownerId);
        MathFunction right = loadOwnedFunction(rightId, ownerId);
        TabulatedFunction leftFunc = loadTabulatedFunction(left);
        TabulatedFunction rightFunc = loadTabulatedFunction(right);

        TabulatedFunction result;
        switch (normalizeOp(op)) {
            case "ADD":
                result = tabulatedFunctionOperationService.sum(leftFunc, rightFunc);
                break;
            case "SUBTRACT":
                result = tabulatedFunctionOperationService.subtract(leftFunc, rightFunc);
                break;
            case "MULTIPLY":
                result = tabulatedFunctionOperationService.multiplication(leftFunc, rightFunc);
                break;
            case "DIVIDE":
                result = tabulatedFunctionOperationService.division(leftFunc, rightFunc);
                break;
            default:
                throw new IllegalArgumentException("Unsupported operation: " + op);
        }

        return persistResult(ownerId,
                "(" + left.getName() + ") " + op + " (" + right.getName() + ")",
                "BINARY:" + op + ":" + left.getId() + "," + right.getId(),
                result);
    }

    public MathFunction differentiate(Long functionId, Long ownerId) {
        validateOwner(ownerId);
        MathFunction function = loadOwnedFunction(functionId, ownerId);
        TabulatedFunction tabulated = loadTabulatedFunction(function);
        TabulatedFunction derived = differentialOperator.derive(tabulated);
        return persistResult(ownerId,
                function.getName() + "'",
                "DERIVATIVE_OF:" + functionId,
                derived);
    }

    public double integrate(Long functionId, Integer threads, Long ownerId) {
        validateOwner(ownerId);
        MathFunction function = loadOwnedFunction(functionId, ownerId);
        TabulatedFunction tabulated = loadTabulatedFunction(function);
        double start = tabulated.getX(0);
        double end = tabulated.getX(tabulated.getCount() - 1);
        double integral = 0.0;
        for (int i = 0; i < tabulated.getCount() - 1; i++) {
            double x0 = tabulated.getX(i);
            double x1 = tabulated.getX(i + 1);
            double y0 = tabulated.getY(i);
            double y1 = tabulated.getY(i + 1);
            integral += 0.5 * (y0 + y1) * (x1 - x0);
        }
        logger.info("Вычислен интеграл функции {} на [{}, {}] за {} точек, threads={} -> {}",
                functionId, start, end, tabulated.getCount(), threads, integral);
        return integral;
    }

    private String normalizeOp(String op) {
        if (op == null) {
            return "";
        }
        String normalized = op.trim().toUpperCase();
        switch (normalized) {
            case "+":
            case "ADD":
            case "SUM":
            case "PLUS":
                return "ADD";
            case "-":
            case "SUB":
            case "SUBTRACT":
            case "MINUS":
                return "SUBTRACT";
            case "*":
            case "MUL":
            case "MULT":
            case "MULTIPLY":
                return "MULTIPLY";
            case "/":
            case "DIV":
            case "DIVIDE":
                return "DIVIDE";
            default:
                return normalized;
        }
    }

    private MathFunction persistResult(Long ownerId, String name, String definitionBody, TabulatedFunction result) {
        MathFunction created = new MathFunction();
        created.setOwnerId(ownerId);
        created.setName(name);
        created.setFunctionType("TABULATED");
        created.setDefinitionBody(definitionBody);
        MathFunction saved = mathFunctionDao.create(created);

        TabulatedDataset dataset = new TabulatedDataset();
        dataset.setFunctionId(saved.getId());
        dataset.setSourceType("GENERATED");
        TabulatedDataset savedDataset = tabulatedDatasetDao.create(dataset);

        int idx = 0;
        for (functions.Point p : result) {
            DatasetPoint point = new DatasetPoint();
            point.setDatasetId(savedDataset.getId());
            point.setPointIndex(idx++);
            point.setXValue(BigDecimal.valueOf(p.x));
            point.setYValue(BigDecimal.valueOf(p.y));
            datasetPointDao.upsert(point);
        }
        logger.info("Сохранён результат операции {} с {} точками", definitionBody, idx);
        saved.setDefinitionBody(definitionBody);
        return saved;
    }

    private MathFunction loadOwnedFunction(Long id, Long ownerId) {
        if (id == null) {
            throw new IllegalArgumentException("function id is required");
        }
        Optional<MathFunction> functionOpt = mathFunctionDao.findById(id);
        MathFunction function = functionOpt.orElseThrow(() ->
                new IllegalArgumentException("Function not found: " + id));
        if (!Objects.equals(function.getOwnerId(), ownerId)) {
            throw new IllegalArgumentException("Access denied for function: " + id);
        }
        return function;
    }

    private TabulatedFunction loadTabulatedFunction(MathFunction function) {
        if (!"TABULATED".equalsIgnoreCase(function.getFunctionType())) {
            throw new IllegalArgumentException("Function is not tabulated");
        }
        List<TabulatedDataset> datasets = tabulatedDatasetDao.findByFunctionId(function.getId());
        if (datasets.isEmpty()) {
            throw new IllegalStateException("Dataset not found for function " + function.getId());
        }
        TabulatedDataset dataset = datasets.get(0);
        List<DatasetPoint> points = new ArrayList<>(datasetPointDao.findByDatasetIdOrderByPointIndex(dataset.getId()));
        if (points.size() < 2) {
            throw new IllegalStateException("Not enough points for operation");
        }
        double[] x = new double[points.size()];
        double[] y = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            x[i] = points.get(i).getXValue().doubleValue();
            y[i] = points.get(i).getYValue().doubleValue();
        }
        return new ArrayTabulatedFunction(x, y);
    }

    private void validateOwner(Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("ownerId is required");
        }
    }

    public String getEngine() {
        return tabulatedFunctionOperationService.getFactory().getClass().getSimpleName();
    }

    public void setEngine(String engine) {
        if (engine == null) {
            throw new IllegalArgumentException("engine is required");
        }
        String normalized = engine.trim().toUpperCase();
        switch (normalized) {
            case "ARRAY":
                tabulatedFunctionOperationService.setFactory(new ArrayTabulatedFunctionFactory());
                break;
            case "LINKED":
            case "LINKEDLIST":
                tabulatedFunctionOperationService.setFactory(new LinkedListTabulatedFunctionFactory());
                break;
            default:
                throw new IllegalArgumentException("Unknown engine: " + engine);
        }
        logger.info("Установлен движок табулированных функций: {}", normalized);
    }
}