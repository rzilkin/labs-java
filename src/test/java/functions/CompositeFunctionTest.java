package functions;

import functions.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CompositeFunctionTest {
    private static final double EPS = 1e-9;

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

        CompositeFunction func = new CompositeFunction(addition, square);
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
        CompositeFunction func = new CompositeFunction(bSpline, percent);

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

    @Test
    void testAndThenSimpleComposition() {
        MathFunction f = x -> x + 1;   // f(x) = x + 1
        MathFunction g = x -> 2 * x;   // g(x) = 2x

        // f.andThen(g) => g(f(x)) = 2*(x+1)
        MathFunction comp = f.andThen(g);
        assertEquals(8.0, comp.apply(3.0), EPS); // 2*(3+1) = 8
        assertEquals(4.0, comp.apply(1.0), EPS); // 2*(1+1) = 4
    }

    @Test
    void testThreeFunctionChain() {
        MathFunction f = x -> x + 1;    // f(x)=x+1
        MathFunction g = x -> 2 * x;    // g(x)=2x
        MathFunction h = x -> x * x;    // h(x)=x^2

        // f.andThen(g).andThen(h) => h(g(f(x))) = (2*(x+1))^2
        MathFunction chain = f.andThen(g).andThen(h);

        assertEquals(Math.pow(2 * (3 + 1), 2), chain.apply(3.0), EPS); // 64
        assertEquals(Math.pow(2 * (0 + 1), 2), chain.apply(0.0), EPS); // 4
    }

    @Test
    void testSelfComposition() {
        MathFunction sqr = new SqrFunction(); // x^2

        // sqr.andThen(sqr) => sqr(sqr(x)) = (x^2)^2 = x^4
        MathFunction self = sqr.andThen(sqr);

        assertEquals(Math.pow(2.0, 4), self.apply(2.0), EPS); // 16
        assertEquals(Math.pow(-3.0, 4), self.apply(-3.0), EPS); // 81
    }

    @Test
    void testCompositeOfCompositeEquality() {
        MathFunction f = x -> x + 1;
        MathFunction g = x -> x * 3;
        MathFunction h = x -> x - 2;

        // Build via andThen chain
        MathFunction chain = f.andThen(g).andThen(h); // h(g(f(x)))

        // Build manually as nested CompositeFunction: new CompositeFunction(new CompositeFunction(f,g), h)
        MathFunction manual = new CompositeFunction(new CompositeFunction(f, g), h);

        double[] testPoints = { -2.5, 0.0, 1.0, 5.5 };
        for (double t : testPoints) {
            assertEquals(manual.apply(t), chain.apply(t), EPS);
        }
    }

    @Test
    void testAndThenWithZeroAndUnitFunctions() {
        MathFunction zero = new ZeroFunction();   // always 0
        MathFunction unit = new UnitFunction();   // always 1
        MathFunction plusTen = x -> x + 10;

        // zero.andThen(plusTen) => plusTen(zero(x)) = plusTen(0) = 10 (constant)
        MathFunction zThen = zero.andThen(plusTen);
        assertEquals(10.0, zThen.apply(0.0), EPS);
        assertEquals(10.0, zThen.apply(1234.5), EPS);

        // plusTen.andThen(zero) => zero(plusTen(x)) = 0 (constant)
        MathFunction thenZ = plusTen.andThen(zero);
        assertEquals(0.0, thenZ.apply(0.0), EPS);
        assertEquals(0.0, thenZ.apply(999.9), EPS);

        // unit.andThen(plusTen) => plusTen(1) = 11
        MathFunction uThen = unit.andThen(plusTen);
        assertEquals(11.0, uThen.apply(0.0), EPS);
    }

    @Test
    void testAndThenWithConstantFunction() {
        MathFunction constFive = new ConstantFunction(5.0);
        MathFunction sqr = new SqrFunction();

        // constFive.andThen(sqr) => sqr(5.0) = 25
        MathFunction comp = constFive.andThen(sqr);
        assertEquals(25.0, comp.apply(-100.0), EPS);
        assertEquals(25.0, comp.apply(0.0), EPS);
    }

    @Test
    void testAssociativityOfComposition() {
        MathFunction f = x -> x + 1;
        MathFunction g = x -> x * 2;
        MathFunction h = x -> x - 3;

        // (f.andThen(g)).andThen(h) and f.andThen(g.andThen(h)) should be equivalent:
        MathFunction left = f.andThen(g).andThen(h);              // h(g(f(x)))
        MathFunction right = f.andThen(g.andThen(h));            // (h ∘ g) ∘ f -> h(g(f(x)))

        double[] pts = { -1.0, 0.0, 2.5, 10.0 };
        for (double p : pts) {
            assertEquals(left.apply(p), right.apply(p), EPS);
        }
    }

    @Test
    void testPropagationOfNaNAndInfinity() {
        MathFunction identity = x -> x;
        MathFunction nanFunc = x -> Double.NaN;
        MathFunction infFunc = x -> Double.POSITIVE_INFINITY;

        // NaN propagation: any composition with a function that returns NaN should produce NaN
        assertTrue(Double.isNaN(identity.andThen(nanFunc).apply(1.0)));

        // Infinity propagation:
        double resPos = identity.andThen(infFunc).apply(2.0);
        assertTrue(Double.isInfinite(resPos) && resPos > 0);

        double resNeg = identity.andThen(infFunc).apply(Double.NEGATIVE_INFINITY);
        assertTrue(Double.isInfinite(resNeg));
    }
}
