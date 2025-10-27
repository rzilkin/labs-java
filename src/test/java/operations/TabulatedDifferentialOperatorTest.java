package operations;

import functions.*;
import functions.factory.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import concurrent.SynchronizedTabulatedFunction;

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

    @Test
    void linearFunctionHasConstantDerivative_sync() {
        double[] x = {0, 1, 2, 3};
        double[] y = {1, 4, 7, 10};
        TabulatedFunction f = new ArrayTabulatedFunction(x, y);

        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();
        TabulatedFunction d = op.deriveSynchronously(f);

        assertEquals(f.getCount(), d.getCount());
        for (int i = 0; i < d.getCount(); i++) {
            assertEquals(x[i], d.getX(i), 1e-12);
            assertEquals(3.0, d.getY(i), 1e-12);
        }
    }

    @Test
    void quadraticMatchesForwardDifferences_sync() {
        double[] x = {0, 1, 2};
        double[] y = {0, 1, 4};
        TabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();
        TabulatedFunction d = op.deriveSynchronously(f);

        assertArrayEquals(x, new double[]{d.getX(0), d.getX(1), d.getX(2)}, 1e-12);
        assertEquals(1.0, d.getY(0), 1e-12);
        assertEquals(3.0, d.getY(1), 1e-12);
        assertEquals(3.0, d.getY(2), 1e-12);
    }

    @Test
    void equalsToNonSyncDeriveForArray() {
        double[] x = {0, 0.5, 2.0, 5.0};
        double[] y = {0, 1.0, 4.0, 25.0};
        TabulatedFunction f = new ArrayTabulatedFunction(x, y);

        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();
        TabulatedFunction d1 = op.derive(f);
        TabulatedFunction d2 = op.deriveSynchronously(f);

        assertEquals(d1.getCount(), d2.getCount());
        for (int i = 0; i < d1.getCount(); i++) {
            assertEquals(d1.getX(i), d2.getX(i), 1e-12);
            assertEquals(d1.getY(i), d2.getY(i), 1e-12);
        }
    }

    @Test
    void worksWhenInputAlreadySynchronized() {
        double[] x = {1, 2, 4, 7};
        double[] y = {2, 5, 13, 26};
        TabulatedFunction base = new ArrayTabulatedFunction(x, y);
        SynchronizedTabulatedFunction sync = new SynchronizedTabulatedFunction(base);

        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();
        TabulatedFunction d = op.deriveSynchronously(sync);

        TabulatedFunction ref = op.derive(base);
        assertEquals(ref.getCount(), d.getCount());
        for (int i = 0; i < d.getCount(); i++) {
            assertEquals(ref.getX(i), d.getX(i), 1e-12);
            assertEquals(ref.getY(i), d.getY(i), 1e-12);
        }
    }

    @Test
    void preservesXMonotonicity() {
        double[] x = {-3, -1, 0, 2};
        double[] y = {9, 1, 0, 4};
        TabulatedFunction f = new ArrayTabulatedFunction(x, y);

        TabulatedDifferentialOperator op = new TabulatedDifferentialOperator();
        TabulatedFunction d = op.deriveSynchronously(f);

        assertEquals(x.length, d.getCount());
        double prev = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < d.getCount(); i++) {
            double xi = d.getX(i);
            assertTrue(xi > prev);
            prev = xi;
        }
    }
}
