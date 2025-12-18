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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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

    private String extractExpressionFromJson(String definitionBody) {
        if (definitionBody == null || definitionBody.isBlank()) {
            return null;
        }
        try {
            JsonElement element = JsonParser.parseString(definitionBody);
            if (element.isJsonObject()) {
                JsonObject json = element.getAsJsonObject();
                if (json.has("expression")) {
                    return json.get("expression").getAsString();
                }
            }
        } catch (Exception e) {
            logger.debug("Definition body is not JSON, using as-is: {}", definitionBody);
        }
        return definitionBody;
    }

    private List<Long> extractComponentsFromJson(String definitionBody) {
        if (definitionBody == null || definitionBody.isBlank()) {
            return Collections.emptyList();
        }
        try {
            JsonElement element = JsonParser.parseString(definitionBody);
            if (element.isJsonObject()) {
                JsonObject json = element.getAsJsonObject();
                if (json.has("components") && json.get("components").isJsonArray()) {
                    List<Long> components = new ArrayList<>();
                    json.get("components").getAsJsonArray().forEach(elem -> {
                        try {
                            components.add(elem.getAsLong());
                        } catch (Exception ignored) {
                        }
                    });
                    return components;
                }
            }
        } catch (Exception e) {
            logger.debug("Definition body is not JSON, trying old format: {}", definitionBody);
        }
        String normalized = definitionBody.replace("[", "").replace("]", "");
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
                logger.warn("Не удалось разобрать компонент '{}'", part);
            }
        }
        return parsed;
    }

    private String formatDefinitionBodyAsJson(String functionType, String rawValue, List<Long> components) {
        if ("ANALYTIC".equalsIgnoreCase(functionType)) {
            String escaped = rawValue.replace("\\", "\\\\").replace("\"", "\\\"");
            return "{\"expression\":\"" + escaped + "\"}";
        } else if ("TABULATED".equalsIgnoreCase(functionType)) {
            return "{\"sourceType\":\"" + rawValue + "\"}";
        } else if ("COMPOSITE".equalsIgnoreCase(functionType)) {
            if (components != null && !components.isEmpty()) {
                String componentsJson = components.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",", "[", "]"));
                return "{\"components\":" + componentsJson + "}";
            } else {
                if (rawValue != null && rawValue.startsWith("[") && rawValue.endsWith("]")) {
                    return "{\"components\":" + rawValue + "}";
                }
                return "{\"components\":[]}";
            }
        }
        return "{\"value\":\"" + (rawValue != null ? rawValue.replace("\\", "\\\\").replace("\"", "\\\"") : "") + "\"}";
    }

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
        function.setDefinitionBody(formatDefinitionBodyAsJson("ANALYTIC", expression, null));

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
        function.setDefinitionBody(formatDefinitionBodyAsJson("TABULATED", "MANUAL", null));

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
                    BigDecimal.valueOf(point.getY())));
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
        function.setDefinitionBody(formatDefinitionBodyAsJson("TABULATED", "GENERATED_FROM:" + source.getId(), null));

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
                    BigDecimal.valueOf(y)));
        }
        logger.info("Создана табулированная функция {} на основе {} с {} точками", createdFunction.getId(),
                source.getId(), count);
        return toFullDto(createdFunction, createdDataset, generatedPoints, Collections.emptyList(), null);
    }

    public FunctionFullDto createComposite(Long ownerId, String name, List<Long> componentIds) {
        validateOwner(ownerId);
        validateName(name);
        if (componentIds == null || componentIds.isEmpty()) {
            throw new IllegalArgumentException("componentIds are required and cannot be empty");
        }
        List<Long> nonNullIds = componentIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (nonNullIds.isEmpty()) {
            throw new IllegalArgumentException("componentIds cannot contain only null values");
        }
        List<Long> validatedComponents = new ArrayList<>();
        for (Long id : nonNullIds) {
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
        function.setDefinitionBody(formatDefinitionBodyAsJson("COMPOSITE", null, validatedComponents));

        MathFunction created = mathFunctionDao.create(function);
        logger.info("Создана составная функция {} с {} компонентами", created.getId(), validatedComponents.size());
        return toFullDto(created, null, Collections.emptyList(), validatedComponents, null);
    }

    public List<FunctionSummaryDto> findAllByOwner(Long ownerId, String typeFilter, String search) {
        return findAllByOwner(ownerId, typeFilter, search, null, null);
    }

    public List<FunctionSummaryDto> findAllByOwner(Long ownerId, String typeFilter, String search, Integer page,
            Integer size) {
        validateOwner(ownerId);
        List<MathFunction> functions = mathFunctionDao.findByOwner(ownerId);
        String normalizedType = typeFilter == null ? null : typeFilter.trim().toUpperCase(Locale.ROOT);
        String normalizedSearch = search == null ? null : search.trim().toLowerCase(Locale.ROOT);

        List<FunctionSummaryDto> result = functions.stream()
                .filter(f -> normalizedType == null || normalizedType.equalsIgnoreCase(f.getFunctionType()))
                .filter(f -> normalizedSearch == null
                        || f.getName().toLowerCase(Locale.ROOT).contains(normalizedSearch))
                .sorted(Comparator.comparing(MathFunction::getId))
                .map(this::toSummaryDto)
                .collect(Collectors.toList());

        if (page != null && size != null && size > 0) {
            int pageNum = Math.max(0, page);
            int fromIndex = pageNum * size;
            if (fromIndex >= result.size()) {
                return Collections.emptyList();
            }
            int toIndex = Math.min(fromIndex + size, result.size());
            return result.subList(fromIndex, toIndex);
        }
        return result;
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
        Optional<TabulatedDataset> datasetOpt = tabulatedDatasetDao.findByFunctionId(function.getId()).stream()
                .findFirst();
        TabulatedDataset dataset = datasetOpt.orElse(null);
        List<PointDto> points = dataset == null
                ? Collections.emptyList()
                : datasetPointDao.findByDatasetIdOrderByPointIndex(dataset.getId()).stream()
                        .map(p -> new PointDto(p.getXValue().doubleValue(), p.getYValue().doubleValue()))
                        .collect(Collectors.toList());
        List<Long> components = function.getFunctionType().equalsIgnoreCase("COMPOSITE")
                ? loadCompositeComponents(function)
                : Collections.emptyList();
        String analyticExpression = "ANALYTIC".equalsIgnoreCase(function.getFunctionType())
                ? extractExpressionFromJson(function.getDefinitionBody())
                : null;
        return toFullDto(function, dataset, points, components, analyticExpression);
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
        function.setDefinitionBody(formatDefinitionBodyAsJson("COMPOSITE", null, validatedComponents));
        if (!mathFunctionDao.update(function)) {
            throw new IllegalStateException("Failed to update components");
        }
        logger.info("Обновлены компоненты составной функции {}", id);
        return buildFullDetails(function);
    }

    public List<Long> getComponents(Long id, Long ownerId) {
        validateOwner(ownerId);
        MathFunction function = mathFunctionDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Function not found"));
        if (!Objects.equals(function.getOwnerId(), ownerId)) {
            throw new IllegalArgumentException("Access denied");
        }
        if (!"COMPOSITE".equalsIgnoreCase(function.getFunctionType())) {
            throw new IllegalArgumentException("Function is not composite");
        }
        return loadCompositeComponents(function);
    }

    public void addComponent(Long functionId, Long ownerId, Long componentId, Integer position) {
        validateOwner(ownerId);
        if (componentId == null) {
            throw new IllegalArgumentException("componentId is required");
        }
        MathFunction function = mathFunctionDao.findById(functionId)
                .orElseThrow(() -> new IllegalArgumentException("Function not found"));
        if (!Objects.equals(function.getOwnerId(), ownerId)) {
            throw new IllegalArgumentException("Access denied");
        }
        if (!"COMPOSITE".equalsIgnoreCase(function.getFunctionType())) {
            throw new IllegalArgumentException("Function is not composite");
        }
        MathFunction component = mathFunctionDao.findById(componentId)
                .orElseThrow(() -> new IllegalArgumentException("Component not found: " + componentId));
        if (!Objects.equals(component.getOwnerId(), ownerId)) {
            throw new IllegalArgumentException("Access denied to component: " + componentId);
        }

        List<Long> components = new ArrayList<>(loadCompositeComponents(function));
        if (position != null && position >= 0 && position <= components.size()) {
            components.add(position, componentId);
        } else {
            components.add(componentId);
        }
        function.setDefinitionBody(formatDefinitionBodyAsJson("COMPOSITE", null, components));
        if (!mathFunctionDao.update(function)) {
            throw new IllegalStateException("Failed to add component");
        }
        logger.info("Добавлен компонент {} в функцию {}", componentId, functionId);
    }

    public void removeComponent(Long functionId, Long ownerId, Long componentId) {
        validateOwner(ownerId);
        if (componentId == null) {
            throw new IllegalArgumentException("componentId is required");
        }
        MathFunction function = mathFunctionDao.findById(functionId)
                .orElseThrow(() -> new IllegalArgumentException("Function not found"));
        if (!Objects.equals(function.getOwnerId(), ownerId)) {
            throw new IllegalArgumentException("Access denied");
        }
        if (!"COMPOSITE".equalsIgnoreCase(function.getFunctionType())) {
            throw new IllegalArgumentException("Function is not composite");
        }

        List<Long> components = new ArrayList<>(loadCompositeComponents(function));
        if (!components.remove(componentId)) {
            throw new IllegalArgumentException("Component not found in function: " + componentId);
        }
        function.setDefinitionBody(formatDefinitionBodyAsJson("COMPOSITE", null, components));
        if (!mathFunctionDao.update(function)) {
            throw new IllegalStateException("Failed to remove component");
        }
        logger.info("Удалён компонент {} из функции {}", componentId, functionId);
    }

    public FunctionFullDto updateAnalyticExpression(Long id, Long ownerId, String expression) {
        validateOwner(ownerId);
        validateExpression(expression);
        MathFunction function = mathFunctionDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Function not found"));
        if (!Objects.equals(function.getOwnerId(), ownerId)) {
            throw new IllegalArgumentException("Access denied");
        }
        if (!"ANALYTIC".equalsIgnoreCase(function.getFunctionType())) {
            throw new IllegalArgumentException("Function is not analytic");
        }
        function.setDefinitionBody(formatDefinitionBodyAsJson("ANALYTIC", expression, null));
        if (!mathFunctionDao.update(function)) {
            throw new IllegalStateException("Failed to update expression");
        }
        logger.info("Обновлено выражение аналитической функции {}", id);
        return buildFullDetails(function);
    }

    public FunctionFullDto exportFunction(Long id, Long ownerId) {
        validateOwner(ownerId);
        MathFunction function = mathFunctionDao.findById(id)
                .filter(f -> Objects.equals(f.getOwnerId(), ownerId))
                .orElseThrow(() -> new IllegalArgumentException("Function not found"));
        return buildFullDetails(function);
    }

    public FunctionFullDto importFunction(Long ownerId, FunctionFullDto importData) {
        validateOwner(ownerId);
        if (importData == null) {
            throw new IllegalArgumentException("Import data is required");
        }

        String type = importData.getType();
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Function type is required");
        }

        String name = importData.getName();
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Function name is required");
        }

        switch (type.toUpperCase(Locale.ROOT)) {
            case "ANALYTIC":
                String expression = importData.getAnalyticExpression();
                if (expression == null || expression.isBlank()) {
                    throw new IllegalArgumentException("Analytic expression is required");
                }
                return createAnalytic(ownerId, name, expression);
            case "TABULATED":
                List<PointDto> points = importData.getPoints();
                if (points == null || points.size() < 2) {
                    throw new IllegalArgumentException("At least two points are required");
                }
                return createTabulatedManual(ownerId, name, points);
            case "COMPOSITE":
                List<Long> components = importData.getComponents();
                if (components == null || components.isEmpty()) {
                    throw new IllegalArgumentException("Components are required for composite function");
                }
                return createComposite(ownerId, name, components);
            default:
                throw new IllegalArgumentException("Unknown function type: " + type);
        }
    }

    private List<Long> loadCompositeComponents(MathFunction function) {
        List<Long> persisted = functionComponentDao.findByCompositeIdOrderByPosition(function.getId()).stream()
                .map(FunctionComponents::getComponentId)
                .collect(Collectors.toList());
        if (!persisted.isEmpty()) {
            return persisted;
        }
        return extractComponentsFromJson(function.getDefinitionBody());
    }

    private FunctionSummaryDto toSummaryDto(MathFunction function) {
        Optional<TabulatedDataset> dataset = tabulatedDatasetDao.findByFunctionId(function.getId()).stream()
                .findFirst();
        Long datasetId = dataset.map(TabulatedDataset::getId).orElse(null);
        String sourceType = dataset.map(TabulatedDataset::getSourceType).orElse(null);
        return new FunctionSummaryDto(
                function.getId(),
                function.getName(),
                function.getFunctionType(),
                function.getOwnerId(),
                datasetId,
                sourceType,
                Instant.now());
    }

    private FunctionFullDto toFullDto(MathFunction function,
            TabulatedDataset dataset,
            List<PointDto> points,
            List<Long> components,
            String analyticExpression) {
        FunctionFullDto fullDto = new FunctionFullDto();
        fullDto.setId(function.getId());
        fullDto.setName(function.getName());
        fullDto.setType(function.getFunctionType());
        fullDto.setOwnerId(function.getOwnerId());
        fullDto.setDatasetId(dataset == null ? null : dataset.getId());
        fullDto.setSourceType(dataset == null ? null : dataset.getSourceType());
        fullDto.setCreatedAt(Instant.now());
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