package mathproj.ui.registry;

import mathproj.functions.MathFunction;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.*;
import java.util.function.Supplier;

public class MathFunctionRegistry {

    public static Map<String, Supplier<MathFunction>> functions() {
        var provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(UiFunction.class));

        Map<String, Entry> tmp = new HashMap<>();

        for (var beanDef : provider.findCandidateComponents("mathproj.functions")) {
            try {
                Class<?> clazz = Class.forName(beanDef.getBeanClassName());
                if (!MathFunction.class.isAssignableFrom(clazz)) continue;

                UiFunction ann = clazz.getAnnotation(UiFunction.class);
                if (ann == null) continue;

                var ctor = clazz.getDeclaredConstructor();
                ctor.setAccessible(true);

                @SuppressWarnings("unchecked")
                Class<? extends MathFunction> mfClass = (Class<? extends MathFunction>) clazz;

                tmp.put(ann.name(), new Entry(
                        ann.name(),
                        ann.priority(),
                        () -> {
                            try {
                                return mfClass.getDeclaredConstructor().newInstance();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                ));
            } catch (NoSuchMethodException e) {
            } catch (Exception e) {
                throw new RuntimeException("Ошибка сканирования функций", e);
            }
        }

        List<Entry> list = new ArrayList<>(tmp.values());
        list.sort(Comparator
                .comparingInt(Entry::priority)
                .thenComparing(Entry::name, String::compareToIgnoreCase));

        Map<String, Supplier<MathFunction>> out = new LinkedHashMap<>();
        for (var e : list) out.put(e.name, e.supplier);
        return out;
    }

    private record Entry(String name, int priority, Supplier<MathFunction> supplier) {}
}



