package mathproj.functions;
/* Функция, всегда возвращает одно и то же число,
 * независимо от аргумента x.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstantFunction implements MathFunction{
    private static final Logger log = LoggerFactory.getLogger(ConstantFunction.class);

    private final double value;

    public ConstantFunction(double value) {
        log.debug("Создание константной функции для value = {}", value);
        this.value = value;
    }

    public double getValue() { return value; }

    @Override
    public double apply(double x) {
        log.debug("Вызов константной функции для x = {}, возвращаемое значение: {}", x, value);
        return value;
    }
}
