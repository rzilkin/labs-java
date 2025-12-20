package mathproj.functions.factory;

import mathproj.functions.ArrayTabulatedFunction;
import mathproj.functions.MathFunction;
import mathproj.functions.TabulatedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArrayTabulatedFunctionFactory implements TabulatedFunctionFactory {
    private static final Logger logger = LoggerFactory.getLogger(ArrayTabulatedFunctionFactory.class);

    @Override
    public ArrayTabulatedFunction create(double[] xValues, double[] yValues) {
        logger.debug("Создаём табулированную функцию на массивах через фабрику с количеством точек {}", xValues.length);
        return new ArrayTabulatedFunction(xValues, yValues);
    }

    @Override
    public TabulatedFunction create(MathFunction f, double xFrom, double xTo, int count) {
        logger.debug("Создаём ArrayTabulatedFunction из MathFunction на [{}, {}] с count={}", xFrom, xTo, count);
        return new ArrayTabulatedFunction(f, xFrom, xTo, count);
    }
}
