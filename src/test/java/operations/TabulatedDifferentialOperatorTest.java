package operations;

import functions.*;
import functions.factory.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TabulatedDifferentialOperatorTest {

    @Test
    public void testLinearFunctionWithArrayFactory() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(factory);

        double[] xValues = {0, 1, 2, 3, 4};
        double[] yValues = {1, 3, 5, 7, 9};

        TabulatedFunction func = factory.create(xValues, yValues);
        TabulatedFunction derivative = operator.derive(func);

        for (int i = 0; i < derivative.getCount(); i++) {
            assertEquals(2.0, derivative.getY(i), 1e-10,
                    "Производная линейной функции должна быть равна 2");
        }
    }

    @Test
    public void testConstantFunction() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(factory);

        double[] xValues = {-2, -1, 0, 1, 2};
        double[] yValues = {5, 5, 5, 5, 5};

        TabulatedFunction func = factory.create(xValues, yValues);
        TabulatedFunction derivative = operator.derive(func);

        for (int i = 0; i < derivative.getCount(); i++) {
            assertEquals(0.0, derivative.getY(i), 1e-10,
                    "Производная константы должна быть равна 0");
        }
    }

    @Test
    public void testTwoPointsFunction() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(factory);

        double[] xValues = {1.0, 3.0};
        double[] yValues = {2.0, 8.0};

        TabulatedFunction func = factory.create(xValues, yValues);
        TabulatedFunction derivative = operator.derive(func);

        assertEquals(2, derivative.getCount());
        double expectedSlope = (8.0 - 2.0) / (3.0 - 1.0);

        assertEquals(expectedSlope, derivative.getY(0), 1e-10);
        assertEquals(expectedSlope, derivative.getY(1), 1e-10);
    }

    @Test
    public void testFactoryGetterSetter() {
        TabulatedFunctionFactory initialFactory = new ArrayTabulatedFunctionFactory();
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(initialFactory);

        assertEquals(initialFactory, operator.getFactory());

        TabulatedFunctionFactory newFactory = new LinkedListTabulatedFunctionFactory();
        operator.setFactory(newFactory);
        assertEquals(newFactory, operator.getFactory());
    }

    @Test
    public void testDefaultConstructor() {
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator();

        assertNotNull(operator.getFactory());
        assertTrue(operator.getFactory() instanceof ArrayTabulatedFunctionFactory);
    }

    @Test
    public void testTabulatedFunctionOperationServiceIntegration() {
        TabulatedFunctionFactory factory = new ArrayTabulatedFunctionFactory();
        TabulatedDifferentialOperator operator = new TabulatedDifferentialOperator(factory);

        double[] xValues = {0, 1, 2, 3};
        double[] yValues = {0, 2, 4, 6};

        TabulatedFunction func = factory.create(xValues, yValues);

        Point[] points = TabulatedFunctionOperationService.asPoints(func);
        assertEquals(4, points.length);

        for (int i = 0; i < points.length; i++) {
            assertEquals(xValues[i], points[i].x, 1e-10);
            assertEquals(yValues[i], points[i].y, 1e-10);
        }

        TabulatedFunction derivative = operator.derive(func);
        assertEquals(4, derivative.getCount());

        for (int i = 0; i < derivative.getCount(); i++) {
            assertEquals(2.0, derivative.getY(i), 1e-10);
        }
    }
}
