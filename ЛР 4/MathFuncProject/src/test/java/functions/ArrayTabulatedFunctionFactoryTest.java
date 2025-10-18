package functions;

import functions.ArrayTabulatedFunction;
import functions.TabulatedFunction;
import org.junit.jupiter.api.Test;
import functions.factory.ArrayTabulatedFunctionFactory;
import functions.factory.TabulatedFunctionFactory;
import static org.junit.jupiter.api.Assertions.*;

class ArrayTabulatedFunctionFactoryTest {
    @Test
    void testCreate() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();

        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};

        TabulatedFunction function = factory.create(xValues, yValues);

        assertTrue(function instanceof ArrayTabulatedFunction,
                "Созданная функция должна быть экземпляром ArrayTabulatedFunction");

        assertEquals(3, function.getCount());
        assertEquals(1.0, function.getX(0));
        assertEquals(10.0, function.getY(0));
        assertEquals(3.0, function.getX(2));
        assertEquals(30.0, function.getY(2));
    }
}