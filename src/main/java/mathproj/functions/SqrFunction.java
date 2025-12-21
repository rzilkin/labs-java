package mathproj.functions;

import mathproj.ui.registry.UiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Функция, возводящая х в квадрат
@UiFunction(name = "Квадратичная функция", priority = 10)
public class SqrFunction implements MathFunction {
    private static final Logger logger = LoggerFactory.getLogger(SqrFunction.class);

    @Override
    public double apply(double x) {
        double result = Math.pow(x, 2);
        logger.debug("Возводим значение {} в квадрат, получаем {}", x, result);
        return result;
    }
}