package mathproj.operations;

import mathproj.functions.ConstantFunction;
import mathproj.functions.SqrFunction;
import mathproj.functions.MathFunction;
import mathproj.operations.LeftSteppingDifferentialOperator;
import mathproj.operations.MiddleSteppingDifferentialOperator;
import mathproj.operations.RightSteppingDifferentialOperator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SteppingDifferentialOperatorTest {

    private static final double EPS = 1e-11;

    @Test
    void testMiddleDerivativeExactForSqr() {
        double h = 1e-3;
        MiddleSteppingDifferentialOperator op = new MiddleSteppingDifferentialOperator(h);
        MathFunction df = op.derive(new SqrFunction());

        double[] xs = {0.0, 1.0, -3.5, 12.345};
        for (double x : xs) {
            double expected = 2.0 * x;
            double actual = df.apply(x);
            assertEquals(expected, actual, EPS);
        }
    }

    @Test
    void testLeftAndRightDerivativeForSqr() {
        double h = 0.01;
        LeftSteppingDifferentialOperator leftOp = new LeftSteppingDifferentialOperator(h);
        RightSteppingDifferentialOperator rightOp = new RightSteppingDifferentialOperator(h);

        MathFunction leftDf = leftOp.derive(new SqrFunction());
        MathFunction rightDf = rightOp.derive(new SqrFunction());

        double[] xs = {0.0, 2.0, -4.25, 7.7};
        for (double x : xs) {
            double expectedLeft = 2.0 * x - h;
            double expectedRight = 2.0 * x + h; // right diff = (f(x+h)-f(x))/h = 2x + h

            assertEquals(expectedLeft, leftDf.apply(x), 1e-12);
            assertEquals(expectedRight, rightDf.apply(x), 1e-12);
        }
    }

    @Test
    void testDerivativeOfConstantIsZero() {
        double h = 0.5;
        ConstantFunction c = new ConstantFunction(7.123);
        MathFunction leftDf = new LeftSteppingDifferentialOperator(h).derive(c);
        MathFunction rightDf = new RightSteppingDifferentialOperator(h).derive(c);
        MathFunction middleDf = new MiddleSteppingDifferentialOperator(h).derive(c);

        double[] xs = {-10.0, 0.0, 3.1415};
        for (double x : xs) {
            assertEquals(0.0, leftDf.apply(x), EPS);
            assertEquals(0.0, rightDf.apply(x), EPS);
            assertEquals(0.0, middleDf.apply(x), EPS);
        }
    }

    @Test
    void testInvalidStepThrowsInConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new LeftSteppingDifferentialOperator(0.0));
        assertThrows(IllegalArgumentException.class, () -> new RightSteppingDifferentialOperator(-1e-3));
        assertThrows(IllegalArgumentException.class, () -> new MiddleSteppingDifferentialOperator(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> new MiddleSteppingDifferentialOperator(Double.POSITIVE_INFINITY));
    }

    @Test
    void testSetStepValidation() {
        MiddleSteppingDifferentialOperator op = new MiddleSteppingDifferentialOperator(0.1);

        op.setStep(1e-3);
        assertEquals(1e-3, op.getStep(), 0.0);

        assertThrows(IllegalArgumentException.class, () -> op.setStep(0.0));
        assertThrows(IllegalArgumentException.class, () -> op.setStep(-0.5));
        assertThrows(IllegalArgumentException.class, () -> op.setStep(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> op.setStep(Double.POSITIVE_INFINITY));
    }
}
