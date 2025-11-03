package operations;

import functions.Point;
import functions.TabulatedFunction;
import exceptions.InconsistentFunctionsException;
import functions.factory.TabulatedFunctionFactory;
import functions.factory.ArrayTabulatedFunctionFactory;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TabulatedFunctionOperationService {
    private static final Logger log = LoggerFactory.getLogger(TabulatedFunctionOperationService.class);

    private TabulatedFunctionFactory factory;

    public TabulatedFunctionOperationService(TabulatedFunctionFactory factory) {
        log.debug("Создание TabulatedFunctionOperationService с фабрикой: {}", factory.getClass().getSimpleName());
        this.factory = Objects.requireNonNull(factory, "factory == null");
    }
    public TabulatedFunctionOperationService() {
        log.debug("Создание TabulatedFunctionOperationService с фабрикой по умолчанию");
        this.factory = new ArrayTabulatedFunctionFactory();
    }

    public TabulatedFunctionFactory getFactory() {
        log.trace("Получение фабрики: {}", factory.getClass().getSimpleName());
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        log.debug("Установка новой фабрики: {}", factory.getClass().getSimpleName());
        this.factory = Objects.requireNonNull(factory, "factory == null");
    }

    public static Point[] asPoints(TabulatedFunction tabulatedFunction) {
        log.debug("Преобразование табличной функции в массив точек");
        if (tabulatedFunction == null) {
            log.error("Попытка преобразования null функции в точки");
            throw new NullPointerException("tabulatedFunction == null");
        }

        final int n = tabulatedFunction.getCount();
        Point[] result = new Point[n];
        log.debug("Создание массива из {} точек", n);

        int i = 0;
        for (Point p : tabulatedFunction) {   // именно for-each, как требует задание
            // создаём копию точки, чтобы массив не зависел от внутреннего контейнера
            result[i++] = new Point(p.x, p.y);
            log.trace("Добавлена точка: ({}, {})", p.x, p.y);
        }
        log.debug("Преобразование завершено, получено {} точек", result.length);
        return result;
    }

    private interface BiOperation {
        double apply(double u, double v);
    }

    TabulatedFunction doOperation(TabulatedFunction a, TabulatedFunction b, BiOperation operation){
        log.debug("Выполнение операции над табличными функциями: {} и {}", a.getClass().getSimpleName(), b.getClass().getSimpleName());

        Objects.requireNonNull(a, "a == null");
        Objects.requireNonNull(b, "b == null");
        Objects.requireNonNull(operation, "operation == null");

        int n = a.getCount();
        log.debug("Количество точек в функции a: {}", n);
        log.debug("Количество точек в функции b: {}", b.getCount());
        if (n != b.getCount()) {
            log.error("Размеры функций различны: a={}, b={}", n, b.getCount());
            throw new InconsistentFunctionsException("Разные размеры: a=" + n + ", b=" + b.getCount());
        }

        Point[] ap = asPoints(a);
        Point[] bp = asPoints(b);

        double[] xValues = new double[n];
        double[] yValues = new double[n];

        for (int i = 0; i < n; i++) {
            xValues[i] = ap[i].x;
            if (Double.compare(xValues[i], bp[i].x) != 0) {
                log.error("Другой x в индексе {}: {} vs {}", i, xValues[i], bp[i].x);
                throw new InconsistentFunctionsException(
                        "Другой x в индексе " + i + ": " + xValues[i] + " vs " + bp[i].x
                );
            }
            yValues[i] = operation.apply(ap[i].y, bp[i].y);
            log.trace("Операция в точке x={}: {} и {} -> {}", xValues[i], ap[i].y, bp[i].y, yValues[i]);
        }

        TabulatedFunction res = factory.create(xValues, yValues);
        log.debug("Создана новая функция с {} точками", res.getCount());
        return res;
    }

    public TabulatedFunction sum(TabulatedFunction a, TabulatedFunction b) {
        log.info("Сложение табличных функций: {} и {}", a.getClass().getSimpleName(), b.getClass().getSimpleName());
        return doOperation(a, b, (u, v) -> u + v);
    }

    public TabulatedFunction subtract(TabulatedFunction a, TabulatedFunction b) {
        log.info("Вычитание табличных функций: {} и {}", a.getClass().getSimpleName(), b.getClass().getSimpleName());
        return doOperation(a, b, (u, v) -> u - v);
    }

    public TabulatedFunction multiplication(TabulatedFunction a, TabulatedFunction b) {
        log.info("Произведение табличных функций: {} и {}", a.getClass().getSimpleName(), b.getClass().getSimpleName());
        return doOperation(a, b, (u, v) -> u * v);
    }

    public TabulatedFunction division(TabulatedFunction a, TabulatedFunction b) {
        log.info("Деление табличных функций: {} и {}", a.getClass().getSimpleName(), b.getClass().getSimpleName());
        return doOperation(a, b, (u, v) -> u / v);
    }
}
