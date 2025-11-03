package functions.factory;

import functions.LinkedListTabulatedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkedListTabulatedFunctionFactory  implements TabulatedFunctionFactory{
    private static final Logger log = LoggerFactory.getLogger(LinkedListTabulatedFunctionFactory.class);

    @Override
    public LinkedListTabulatedFunction create(double[] xValues, double[] yValues) {
        log.debug("Создание LinkedListTabulatedFunction с {} точками данных", xValues.length);
        return new LinkedListTabulatedFunction(xValues, yValues);
    }

}
