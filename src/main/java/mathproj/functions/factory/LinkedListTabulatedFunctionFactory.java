package mathproj.functions.factory;

import mathproj.functions.MathFunction;
import mathproj.functions.TabulatedFunction;
import mathproj.functions.LinkedListTabulatedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkedListTabulatedFunctionFactory  implements TabulatedFunctionFactory{
    private static final Logger log = LoggerFactory.getLogger(LinkedListTabulatedFunctionFactory.class);

    @Override
    public LinkedListTabulatedFunction create(double[] xValues, double[] yValues) {
        log.debug("Создание LinkedListTabulatedFunction с {} точками данных", xValues.length);
        return new LinkedListTabulatedFunction(xValues, yValues);
    }

    @Override
    public TabulatedFunction create(MathFunction f, double xFrom, double xTo, int count) {
        log.debug("Создание LinkedListTabulatedFunction из MathFunction на [{}, {}] с count={}", xFrom, xTo, count);
        return new LinkedListTabulatedFunction(f, xFrom, xTo, count);
    }
}
