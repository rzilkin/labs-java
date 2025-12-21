package mathproj.ui.service;

import mathproj.functions.TabulatedFunction;
import mathproj.ui.dto.FunctionInfoDto;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FunctionStoreService {

    private final Map<String, Stored> store = new ConcurrentHashMap<>();

    public String add(String name, TabulatedFunction f) {
        String id = UUID.randomUUID().toString();
        store.put(id, new Stored(id, name, f));
        return id;
    }

    public List<FunctionInfoDto> list() {
        return store.values().stream()
                .sorted(Comparator.comparing(s -> s.name.toLowerCase()))
                .map(s -> new FunctionInfoDto(s.id, s.name, s.f.getCount()))
                .toList();
    }

    public Stored getOrThrow(String id) {
        Stored s = store.get(id);
        if (s == null) throw new IllegalArgumentException("Функция не найдена: " + id);
        return s;
    }

    public void delete(String id) {
        store.remove(id);
    }

    public record Stored(String id, String name, TabulatedFunction f) {}
}

