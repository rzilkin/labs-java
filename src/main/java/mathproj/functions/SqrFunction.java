package mathproj.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Функция, возводящая х в квадрат
public class SqrFunction implements MathFunction {
    private static final Logger logger = LoggerFactory.getLogger(SqrFunction.class);

    @Override
    public double apply(double x) {
        double result = Math.pow(x, 2);
        logger.debug("Возводим значение {} в квадрат, получаем {}", x, result);
        return result;
    }
}