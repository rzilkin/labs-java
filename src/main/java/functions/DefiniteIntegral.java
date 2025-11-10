package functions;
/* Утилитный класс для численного вычисления опредённого интеграла
 * методом трапеций с фиксированным шагом.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefiniteIntegral implements MathFunction {
    public static final double INCREMENT = 1E-4;

    private static final Logger logger = LoggerFactory.getLogger(DefiniteIntegral.class);

    private double a;
    private final MathFunction function;

    public DefiniteIntegral(MathFunction function, double a) {
        this.function = function;
        this.a = a;
        logger.info("Создан определённый интеграл с нижним пределом {} для функции {}", a, function);
    }

    public void setLower(double a) {
        logger.debug("Обновляем нижний предел с {} на {}", this.a, a);
        this.a = a;
    }

    private double compute(double start, double end){
        logger.debug("Вычисляем интеграл от {} до {}", start, end);
        // Обработка частных случаев
        if (Double.isNaN(start) || Double.isNaN(end)) {
            logger.warn("Границы интегрирования содержат NaN: начало={}, конец={}", start, end);
            return Double.NaN;
        }
        if (start == end) {
            logger.trace("Границы интегрирования совпадают, возвращаем ноль");
            return 0.0;
        }

        double modifier = 1.0;

        // Пределы в обратном порядке - меняем пределы местами и знак
        if (start > end){
            logger.debug("Меняем пределы интегрирования местами: {} и {}", start, end);
            double temp = start;
            start = end;
            end = temp;
            modifier = -1.0;
        }
        double width = end - start;
        // Вычислим количество полных шагов (нулевая часть нужна для остатка ниже)
        int nFullSteps = (int) (width / INCREMENT);

        double area = 0.0;
        // Основной цикл: проходим по узлам с шагом INCREMENT и складываем площади трапеций.
        for (int k = 1; k <= nFullSteps; k++) {
            double xPrev = start + (k - 1) * INCREMENT;
            double xCurr = start + k * INCREMENT;
            double left = function.apply(xPrev);
            double right = function.apply(xCurr);
            area += 0.5 * (left + right) * INCREMENT;
        }

        // Обработка остаточного (короткого) интервала, если (end-start) не кратно INCREMENT
        double remainder = width- nFullSteps * INCREMENT;
        if (remainder > 1e-12) { // Защита от небольших погрешностей
            double xPrev = start + nFullSteps * INCREMENT;
            double xCurr = end;
            // Площадь последней трапеции
            area += 0.5 * (function.apply(xPrev) + function.apply(xCurr)) * remainder;
        }

        double result = area * modifier;
        logger.debug("Вычисленное значение интеграла: {}", result);
        return result;
    }

    @Override
    public double apply(double x) {
        logger.debug("Вычисляем интеграл при верхнем пределе {}", x);
        return compute(a, x);
    }
}