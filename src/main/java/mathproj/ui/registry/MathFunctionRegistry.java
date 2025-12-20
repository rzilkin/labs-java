package mathproj.ui.registry;

import mathproj.functions.*;

import java.util.*;
import java.util.function.Supplier;

public class MathFunctionRegistry {

    public static Map<String, Supplier<MathFunction>> functions() {
        Map<String, Supplier<MathFunction>> map = new HashMap<>();

        map.put("Квадратичная функция", SqrFunction::new);
        map.put("Тождественная функция", IdentifyFunction::new);
        map.put("Нулевая функция", ZeroFunction::new);
        map.put("Единичная функция", UnitFunction::new);

        List<Map.Entry<String, Supplier<MathFunction>>> entries = new ArrayList<>(map.entrySet());
        entries.sort(Map.Entry.comparingByKey());

        Map<String, Supplier<MathFunction>> sorted = new LinkedHashMap<>();
        for (var e : entries) sorted.put(e.getKey(), e.getValue());

        return sorted;
    }
}


