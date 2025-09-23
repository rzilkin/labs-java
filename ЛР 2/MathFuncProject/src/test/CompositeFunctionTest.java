package test;

import functions.CompositeFunction;
import functions.LinSplines;
import functions.MathFunction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CompositeFunctionTest {

    @Test
    void testSquareAndAddition() {
        MathFunction square = new MathFunction() {
            @Override
            public double apply(double x) {
                return x*x;
            }
        };

        MathFunction addition = new MathFunction() {
            @Override
            public double apply(double x) {
                return x+1;
            }
        };

        CompositeFunction func = new CompositeFunction(square, addition);
        assertEquals(36, func.apply(5), 1e-6);
        assertEquals(356.0769, func.apply(17.87), 1e-6);
    }

    @Test
    void testWithSplines() {
        MathFunction percent = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * 10;
            }
        };

        double[] xValues = {1.7, 2.8, 5.4, 8.0, 12.0, 13.34, 17};
        double[] yValues = {0.32, 4.2, 9.3, 11, 13.0, 20.5, 23};
        MathFunction bSpline = new LinSplines(xValues, yValues);
        CompositeFunction func = new CompositeFunction(percent, bSpline);

        assertEquals(85.153846, func.apply(5), 1e-6);
        assertEquals(-21.490909, func.apply(1), 1e-6);
        assertEquals(237.991803, func.apply(18.17), 1e-6);

    }

    @Test
    void testWithInfinite() {
        MathFunction first = new MathFunction() {
            @Override
            public double apply(double x) {
                return x + x + x;
            }
        };

        MathFunction second = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * x;
            }
        };

        CompositeFunction func = new CompositeFunction(first, second);
        assertEquals(Double.POSITIVE_INFINITY, func.apply(Double.POSITIVE_INFINITY));
        assertEquals(Double.POSITIVE_INFINITY, func.apply(Double.NEGATIVE_INFINITY));
    }
}
