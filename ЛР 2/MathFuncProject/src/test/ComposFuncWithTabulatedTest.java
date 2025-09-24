package test;

import functions.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ComposFuncWithTabulatedTest {

    @Test
    void testComposWithLinkedListTabulatedFunc() {
        double[] xValues = {3.0, 7.0, 13.0, 24.0, 25.0};
        double[] yValues = {7.0, 8.0, 12.0, 16.0, 25.0};
        LinkedListTabulatedFunction linkedListFunc = new LinkedListTabulatedFunction(xValues, yValues);

        MathFunction sqrFunction = new SqrFunction();
        MathFunction constantFunction = new ConstantFunction(2.0);

        MathFunction compos1 = new CompositeFunction(linkedListFunc, sqrFunction);
        MathFunction compos2 = new CompositeFunction(constantFunction, linkedListFunc);

        assertEquals(49.0, compos1.apply(3.0), 1e-6);
        assertEquals(64.0, compos1.apply(7.0), 1e-6);

        // Тестируем composite2: linkedListFunc(constant(x))
        assertEquals(6.75, compos2.apply(10.0), 1e-6);
        assertEquals(6.75, compos2.apply(-5.0), 1e-6);
    }

}
