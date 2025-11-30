package service;

import dao.DatasetPointDao;
import dao.FunctionComponentDao;
import dao.MathFunctionDao;
import dao.TabulatedDatasetDao;
import dto.DatasetPoint;
import dto.FunctionComponents;
import dto.FunctionFullDto;
import dto.FunctionSummaryDto;
import dto.MathFunction;
import dto.PointDto;
import dto.TabulatedDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class FunctionService {
    private static final Logger logger = LoggerFactory.getLogger(FunctionService.class);

    private final MathFunctionDao mathFunctionDao;
    private final TabulatedDatasetDao tabulatedDatasetDao;
    private final DatasetPointDao datasetPointDao;
    private final FunctionComponentDao functionComponentDao;

    public FunctionService(MathFunctionDao mathFunctionDao,
                           TabulatedDatasetDao tabulatedDatasetDao,
                           DatasetPointDao datasetPointDao,
                           FunctionComponentDao functionComponentDao) {
        this.mathFunctionDao = Objects.requireNonNull(mathFunctionDao, "mathFunctionDao");
        this.tabulatedDatasetDao = Objects.requireNonNull(tabulatedDatasetDao, "tabulatedDatasetDao");
        this.datasetPointDao = Objects.requireNonNull(datasetPointDao, "datasetPointDao");
        this.functionComponentDao = Objects.requireNonNull(functionComponentDao, "functionComponentDao");
    }

    public FunctionFullDto createAnalytic(Long ownerId, String name, String expression) {
        validateOwner(ownerId);
        validateName(name);
        validateExpression(expression);

        MathFunction function = new MathFunction();
        function.setOwnerId(ownerId);
        function.setName(name.trim());
        function.setFunctionType("ANALYTIC");
        function.setDefinitionBody(expression);

        MathFunction created = mathFunctionDao.create(function);
        logger.info("Создана аналитическая функция {} для пользователя {}", created.getId(), ownerId);
        return toFullDto(created, null, Collections.emptyList(), Collections.emptyList(), expression);
    }

    public FunctionFullDto createTabulatedManual(Long ownerId, String name, List<PointDto> points) {
        validateOwner(ownerId);
        validateName(name);
        validatePoints(points);

        MathFunction function = new MathFunction();
        function.setOwnerId(ownerId);
        function.setName(name.trim());
        function.setFunctionType("TABULATED");
        function.setDefinitionBody("MANUAL");

        MathFunction createdFunction = mathFunctionDao.create(function);
        TabulatedDataset dataset = new TabulatedDataset();
        dataset.setFunctionId(createdFunction.getId());
        dataset.setSourceType("MANUAL");
        TabulatedDataset createdDataset = tabulatedDatasetDao.create(dataset);

        int index = 0;
        for (PointDto point : points) {
            datasetPointDao.upsert(new DatasetPoint(
                    createdDataset.getId(),
                    index++,
                    BigDecimal.valueOf(point.getX()),
                    BigDecimal.valueOf(point.getY())
            ));
        }
        logger.info("Создана табулированная функция {} с {} точками", createdFunction.getId(), points.size());
        return toFullDto(createdFunction, createdDataset, points, Collections.emptyList(), null);
    }

    public FunctionFullDto createTabulatedFromFunction(Long ownerId,
                                                       String name,
                                                       Long sourceFunctionId,
                                                       int count,
                                                       double from,
                                                       double to) {
        validateOwner(ownerId);
        validateName(name);
        if (sourceFunctionId == null) {
            throw new IllegalArgumentException("sourceFunctionId is required");
        }
        if (count < 2) {
            throw new IllegalArgumentException("count must be >= 2");
        }
        if (Double.compare(from, to) >= 0) {
            throw new IllegalArgumentException("from must be less than to");
        }
        MathFunction source = mathFunctionDao.findById(sourceFunctionId)
                .filter(f -> Objects.equals(f.getOwnerId(), ownerId))
                .orElseThrow(() -> new IllegalArgumentException("Source function not found or access denied"));

        MathFunction function = new MathFunction();
        function.setOwnerId(ownerId);
        function.setName(name.trim());
        function.setFunctionType("TABULATED");
        function.setDefinitionBody("GENERATED_FROM:" + source.getId());

        MathFunction createdFunction = mathFunctionDao.create(function);
        TabulatedDataset dataset = new TabulatedDataset();
        dataset.setFunctionId(createdFunction.getId());
        dataset.setSourceType("GENERATED");
        TabulatedDataset createdDataset = tabulatedDatasetDao.create(dataset);

        List<PointDto> generatedPoints = new ArrayList<>();
        double step = (to - from) / (count - 1);
        for (int i = 0; i < count; i++) {
            double x = from + step * i;
            double y = 0.0d;
            generatedPoints.add(new PointDto(x, y));
            datasetPointDao.upsert(new DatasetPoint(
                    createdDataset.getId(),
                    i,
                    BigDecimal.valueOf(x),
                    BigDecimal.valueOf(y)
            ));
        }
        logger.info("Создана табулированная функция {} на основе {} с {} точками", createdFunction.getId(), source.getId(), count);
        return toFullDto(createdFunction, createdDataset, generatedPoints, Collections.emptyList(), null);
    }

    public FunctionFullDto createComposite(Long ownerId, String name, List<Long> componentIds) {
        validateOwner(ownerId);
        validateName(name);
        if (componentIds == null) {
            throw new IllegalArgumentException("componentIds are required");
        }
        List<Long> validatedComponents = new ArrayList<>();
        for (Long id : componentIds) {
            MathFunction component = mathFunctionDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Component not found: " + id));
            if (!Objects.equals(component.getOwnerId(), ownerId)) {
                throw new IllegalArgumentException("Access denied to component: " + id);
            }
            validatedComponents.add(id);
        }

        MathFunction function = new MathFunction();
        function.setOwnerId(ownerId);
        function.setName(name.trim());
        function.setFunctionType("COMPOSITE");
        function.setDefinitionBody(validatedComponents.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "[", "]")));

        MathFunction created = mathFunctionDao.create(function);
        logger.info("Создана составная функция {} с {} компонентами", created.getId(), validatedComponents.size());
        return toFullDto(created, null, Collections.emptyList(), validatedComponents, null);
    }

    public List<FunctionSummaryDto> findAllByOwner(Long ownerId, String typeFilter, String search) {
        validateOwner(ownerId);
        List<MathFunction> functions = mathFunctionDao.findByOwner(ownerId);
        String normalizedType = typeFilter == null ? null : typeFilter.trim().toUpperCase(Locale.ROOT);
        String normalizedSearch = search == null ? null : search.trim().toLowerCase(Locale.ROOT);

        return functions.stream()
                .filter(f -> normalizedType == null || normalizedType.equalsIgnoreCase(f.getFunctionType()))
                .filter(f -> normalizedSearch == null || f.getName().toLowerCase(Locale.ROOT).contains(normalizedSearch))
                .sorted(Comparator.comparing(MathFunction::getId))
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    public FunctionFullDto findByIdAndOwner(Long id, Long ownerId) {
        validateOwner(ownerId);
        MathFunction function = mathFunctionDao.findById(id)
                .filter(f -> Objects.equals(f.getOwnerId(), ownerId))
                .orElseThrow(() -> new IllegalArgumentException("Function not found"));
        return buildFullDetails(function);
    }

    public FunctionSummaryDto updateName(Long id, Long ownerId, String newName) {
        validateOwner(ownerId);
        validateName(newName);
        MathFunction function = mathFunctionDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Function not found"));
        if (!Objects.equals(function.getOwnerId(), ownerId)) {
            throw new IllegalArgumentException("Access denied");
        }
        function.setName(newName.trim());
        if (!mathFunctionDao.update(function)) {
            throw new IllegalStateException("Failed to update name");
        }
        logger.info("Обновлено имя функции {}", id);
        return toSummaryDto(function);
    }

    public boolean deleteById(Long id, Long ownerId) {
        validateOwner(ownerId);
        MathFunction function = mathFunctionDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Function not found"));
        if (!Objects.equals(function.getOwnerId(), ownerId)) {
            throw new IllegalArgumentException("Access denied");
        }

        for (TabulatedDataset dataset : tabulatedDatasetDao.findByFunctionId(id)) {
            datasetPointDao.deleteAllByDataset(dataset.getId());
            tabulatedDatasetDao.delete(dataset.getId());
        }

        boolean deleted = mathFunctionDao.delete(id);
        logger.info("Удаление функции {} завершено: {}", id, deleted);
        return deleted;
    }

    private FunctionFullDto buildFullDetails(MathFunction function) {
        Optional<TabulatedDataset> datasetOpt = tabulatedDatasetDao.findByFunctionId(function.getId()).stream().findFirst();
        TabulatedDataset dataset = datasetOpt.orElse(null);
        List<PointDto> points = dataset == null
                ? Collections.emptyList()
                : datasetPointDao.findByDatasetIdOrderByPointIndex(dataset.getId()).stream()
                .map(p -> new PointDto(p.getXValue().doubleValue(), p.getYValue().doubleValue()))
                .collect(Collectors.toList());
        List<Long> components = function.getFunctionType().equalsIgnoreCase("COMPOSITE")
                ? loadCompositeComponents(function)
                : Collections.emptyList();
        return toFullDto(function, dataset, points, components,
                "ANALYTIC".equalsIgnoreCase(function.getFunctionType()) ? function.getDefinitionBody() : null);
    }

    public FunctionFullDto updateCompositeComponents(Long id, Long ownerId, List<Long> componentIds) {
        validateOwner(ownerId);
        if (componentIds == null) {
            throw new IllegalArgumentException("componentIds are required");
        }
        MathFunction function = mathFunctionDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Function not found"));
        if (!Objects.equals(function.getOwnerId(), ownerId)) {
            throw new IllegalArgumentException("Access denied");
        }
        if (!"COMPOSITE".equalsIgnoreCase(function.getFunctionType())) {
            throw new IllegalArgumentException("Function is not composite");
        }
        List<Long> validatedComponents = new ArrayList<>();
        for (Long cid : componentIds) {
            MathFunction component = mathFunctionDao.findById(cid)
                    .orElseThrow(() -> new IllegalArgumentException("Component not found: " + cid));
            if (!Objects.equals(component.getOwnerId(), ownerId)) {
                throw new IllegalArgumentException("Access denied to component: " + cid);
            }
            validatedComponents.add(cid);
        }
        function.setDefinitionBody(validatedComponents.stream().map(String::valueOf)
                .collect(Collectors.joining(",", "[", "]")));
        if (!mathFunctionDao.update(function)) {
            throw new IllegalStateException("Failed to update components");
        }
        logger.info("Обновлены компоненты составной функции {}", id);
        return buildFullDetails(function);
    }

    private List<Long> loadCompositeComponents(MathFunction function) {
        List<Long> persisted = functionComponentDao.findByCompositeIdOrderByPosition(function.getId()).stream()
                .map(FunctionComponents::getComponentId)
                .collect(Collectors.toList());
        if (!persisted.isEmpty()) {
            return persisted;
        }
        String body = function.getDefinitionBody();
        if (body == null || body.isBlank()) {
            return Collections.emptyList();
        }
        String normalized = body.replace("[", "").replace("]", "");
        if (normalized.isBlank()) {
            return Collections.emptyList();
        }
        List<Long> parsed = new ArrayList<>();
        for (String part : normalized.split(",")) {
            if (part == null || part.isBlank()) {
                continue;
            }
            try {
                parsed.add(Long.parseLong(part.trim()));
            } catch (NumberFormatException ignored) {
                logger.warn("Не удалось разобрать компонент '{}' для функции {}", part, function.getId());
            }
        }
        return parsed;
    }

    private FunctionSummaryDto toSummaryDto(MathFunction function) {
        Optional<TabulatedDataset> dataset = tabulatedDatasetDao.findByFunctionId(function.getId()).stream().findFirst();
        Long datasetId = dataset.map(TabulatedDataset::getId).orElse(null);
        String sourceType = dataset.map(TabulatedDataset::getSourceType).orElse(null);
        return new FunctionSummaryDto(
                function.getId(),
                function.getName(),
                function.getFunctionType(),
                function.getOwnerId(),
                datasetId,
                sourceType,
                Instant.now()
        );
    }

    private FunctionFullDto toFullDto(MathFunction function,
                                      TabulatedDataset dataset,
                                      List<PointDto> points,
                                      List<Long> components,
                                      String analyticExpression) {
        FunctionSummaryDto summary = new FunctionSummaryDto(
                function.getId(),
                function.getName(),
                function.getFunctionType(),
                function.getOwnerId(),
                dataset == null ? null : dataset.getId(),
                dataset == null ? null : dataset.getSourceType(),
                Instant.now()
        );
        FunctionFullDto fullDto = new FunctionFullDto();
        fullDto.setSummary(summary);
        fullDto.setPoints(points == null ? Collections.emptyList() : new ArrayList<>(points));
        fullDto.setComponents(components == null ? Collections.emptyList() : new ArrayList<>(components));
        fullDto.setAnalyticExpression(analyticExpression);
        return fullDto;
    }

    private void validateOwner(Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("ownerId is required");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
    }

    private void validateExpression(String expression) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("expression is required");
        }
    }

    private void validatePoints(List<PointDto> points) {
        if (points == null || points.size() < 2) {
            throw new IllegalArgumentException("At least two points are required");
        }
    }
}