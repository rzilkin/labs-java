package mathproj.store;

import mathproj.dto.FunctionFullDto;
import mathproj.dto.FunctionSummaryDto;
import mathproj.dto.PointDto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryFunctionStore {

    private final AtomicLong fnSeq = new AtomicLong(1);
    private final AtomicLong datasetSeq = new AtomicLong(1);

    private final Map<Long, FunctionFullDto> functions = new ConcurrentHashMap<>();

    public List<FunctionSummaryDto> list(long ownerId, String type, String search) {
        String t = norm(type);
        String s = norm(search);
        List<FunctionSummaryDto> out = new ArrayList<>();

        for (FunctionFullDto f : functions.values()) {
            FunctionSummaryDto sum = f.getSummary();
            if (sum == null) continue;
            if (!Objects.equals(sum.getOwnerId(), ownerId)) continue;
            if (t != null && !t.equalsIgnoreCase(sum.getType())) continue;
            if (s != null && (sum.getName() == null || !sum.getName().toLowerCase().contains(s))) continue;
            out.add(sum);
        }

        out.sort(Comparator.comparing(FunctionSummaryDto::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));
        return out;
    }

    public FunctionFullDto get(long ownerId, long id) {
        FunctionFullDto f = functions.get(id);
        if (f == null) throw new NoSuchElementException("Не найдено");
        if (!Objects.equals(f.getSummary().getOwnerId(), ownerId)) throw new SecurityException("Запрещено");
        return f;
    }

    public void delete(long ownerId, long id) {
        get(ownerId, id);
        functions.remove(id);
    }

    public FunctionSummaryDto rename(long ownerId, long id, String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Поле name обязательно");
        FunctionFullDto f = get(ownerId, id);
        f.getSummary().setName(name);
        return f.getSummary();
    }

    public FunctionFullDto createAnalytic(long ownerId, String name, String expression) {
        if (name == null || name.isBlank() || expression == null || expression.isBlank()) {
            throw new IllegalArgumentException("Поля name и expression обязательны");
        }
        long id = fnSeq.getAndIncrement();
        long datasetId = datasetSeq.getAndIncrement();

        FunctionSummaryDto summary = new FunctionSummaryDto(
                id, name, "ANALYTIC", ownerId, datasetId, "MANUAL", Instant.now()
        );

        FunctionFullDto full = new FunctionFullDto();
        full.setSummary(summary);
        full.setAnalyticExpression(expression);
        full.setPoints(new ArrayList<>());
        full.setComponents(new ArrayList<>());

        functions.put(id, full);
        return full;
    }

    public FunctionFullDto updateAnalytic(long ownerId, long id, String expression) {
        if (expression == null || expression.isBlank()) throw new IllegalArgumentException("Поле expression обязательно");
        FunctionFullDto f = get(ownerId, id);
        if (!"ANALYTIC".equalsIgnoreCase(f.getSummary().getType())) {
            throw new IllegalArgumentException("Функция не является аналитической");
        }
        f.setAnalyticExpression(expression);
        return f;
    }

    public FunctionFullDto createTabulatedManual(long ownerId, String name, List<PointDto> points) {
        if (name == null || name.isBlank() || points == null || points.size() < 2) {
            throw new IllegalArgumentException("Поле name и минимум две точки обязательны");
        }
        long id = fnSeq.getAndIncrement();
        long datasetId = datasetSeq.getAndIncrement();

        FunctionSummaryDto summary = new FunctionSummaryDto(
                id, name, "TABULATED", ownerId, datasetId, "MANUAL", Instant.now()
        );

        FunctionFullDto full = new FunctionFullDto();
        full.setSummary(summary);
        full.setPoints(new ArrayList<>(points));
        full.setComponents(new ArrayList<>());
        full.setAnalyticExpression(null);

        functions.put(id, full);
        return full;
    }

    public FunctionFullDto createTabulatedFromFunction(long ownerId, String name, long sourceFunctionId, int count, double from, double to) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Поле name обязательно");
        if (count < 2) throw new IllegalArgumentException("Количество точек должно быть >= 2");

        get(ownerId, sourceFunctionId);

        List<PointDto> points = new ArrayList<>();
        double step = (to - from) / (count - 1);
        for (int i = 0; i < count; i++) {
            double x = from + step * i;
            double y = 0.0;
            points.add(new PointDto(x, y));
        }

        long id = fnSeq.getAndIncrement();
        long datasetId = datasetSeq.getAndIncrement();
        FunctionSummaryDto summary = new FunctionSummaryDto(
                id, name, "TABULATED", ownerId, datasetId, "GENERATED", Instant.now()
        );

        FunctionFullDto full = new FunctionFullDto();
        full.setSummary(summary);
        full.setPoints(points);
        full.setComponents(new ArrayList<>());
        full.setAnalyticExpression(null);

        functions.put(id, full);
        return full;
    }

    public FunctionFullDto createComposite(long ownerId, String name, List<Long> componentIds) {
        if (name == null || name.isBlank() || componentIds == null || componentIds.isEmpty()) {
            throw new IllegalArgumentException("Поле name и список компонентов обязательны");
        }
        for (Long cid : componentIds) get(ownerId, cid);

        long id = fnSeq.getAndIncrement();
        long datasetId = datasetSeq.getAndIncrement();
        FunctionSummaryDto summary = new FunctionSummaryDto(
                id, name, "COMPOSITE", ownerId, datasetId, "MANUAL", Instant.now()
        );

        FunctionFullDto full = new FunctionFullDto();
        full.setSummary(summary);
        full.setComponents(new ArrayList<>(componentIds));
        full.setPoints(new ArrayList<>());
        full.setAnalyticExpression(null);

        functions.put(id, full);
        return full;
    }

    public List<Long> getComponents(long ownerId, long id) {
        return new ArrayList<>(get(ownerId, id).getComponents());
    }

    public void addComponent(long ownerId, long id, long componentId, Integer position) {
        FunctionFullDto f = get(ownerId, id);
        if (!"COMPOSITE".equalsIgnoreCase(f.getSummary().getType())) {
            throw new IllegalArgumentException("Функция не является составной (composite)");
        }
        get(ownerId, componentId);

        List<Long> comps = f.getComponents();
        if (position == null || position < 0 || position > comps.size()) {
            comps.add(componentId);
        } else {
            comps.add(position, componentId);
        }
    }

    public void removeComponent(long ownerId, long id, long componentId) {
        FunctionFullDto f = get(ownerId, id);
        f.getComponents().removeIf(x -> Objects.equals(x, componentId));
    }

    public FunctionFullDto cloneAsOperationResult(long ownerId, String name, String sourceType) {
        long id = fnSeq.getAndIncrement();
        long datasetId = datasetSeq.getAndIncrement();

        FunctionSummaryDto summary = new FunctionSummaryDto(
                id, name, "TABULATED", ownerId, datasetId, sourceType, Instant.now()
        );

        FunctionFullDto full = new FunctionFullDto();
        full.setSummary(summary);
        full.setPoints(new ArrayList<>());
        full.setComponents(new ArrayList<>());
        full.setAnalyticExpression(null);

        functions.put(id, full);
        return full;
    }

    private String norm(String v) {
        if (v == null) return null;
        String t = v.trim();
        if (t.isEmpty()) return null;
        return t.toLowerCase();
    }
}


