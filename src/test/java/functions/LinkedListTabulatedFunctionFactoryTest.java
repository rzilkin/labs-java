package functions;

import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import org.junit.jupiter.api.Test;
import functions.factory.TabulatedFunctionFactory;
import functions.factory.LinkedListTabulatedFunctionFactory;
import static org.junit.jupiter.api.Assertions.*;

class LinkedListTabulatedFunctionFactoryTest {

    @Test
    void testCreate() {
        TabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();

        double[] xValues = {0.5, 1.5, 2.5};
        double[] yValues = {5.0, 15.0, 25.0};

        TabulatedFunction function = factory.create(xValues, yValues);

        // Проверяем, что созданный объект является LinkedListTabulatedFunction
        assertTrue(function instanceof LinkedListTabulatedFunction,
                "Созданная функция должна быть экземпляром LinkedListTabulatedFunction");

        // Проверяем корректность данных
        assertEquals(3, function.getCount());
        assertEquals(0.5, function.getX(0));
        assertEquals(5.0, function.getY(0));
        assertEquals(2.5, function.getX(2));
        assertEquals(25.0, function.getY(2));
    }
}