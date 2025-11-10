package functions.factory;

import functions.ArrayTabulatedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArrayTabulatedFunctionFactory implements TabulatedFunctionFactory {
    private static final Logger logger = LoggerFactory.getLogger(ArrayTabulatedFunctionFactory.class);

    @Override
    public ArrayTabulatedFunction create(double[] xValues, double[] yValues) {
        logger.debug("Создаём табулированную функцию на массивах через фабрику с количеством точек {}", xValues.length);
        return new ArrayTabulatedFunction(xValues, yValues);
    }
}
