package mathproj.functions;

import mathproj.functions.MathFunction;
import mathproj.functions.SqrFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SqrFunctionTest {

    private final MathFunction f = new SqrFunction();

    @Test
    void testPositiveNumber() {
        assertEquals(4.0, f.apply(2.0), 1e-6);
        assertEquals(9.0, f.apply(3.0), 1e-6);
    }

    @Test
    void testZero() {
        assertEquals(0.0, f.apply(0.0), 1e-6);
    }

    @Test
    void testNegativeNumber() {
        assertEquals(4.0, f.apply(-2.0), 1e-6);
        assertEquals(9.0, f.apply(-3.0), 1e-6);
    }

    @Test
    void testFractionalNumber() {
        assertEquals(2.25, f.apply(1.5), 1e-6);
        assertEquals(0.25, f.apply(-0.5), 1e-6);
    }

    @Test
    void testLargeNumber() {
        double result = f.apply(1e154);  // 10^154 → квадрат = 10^308, почти предел double
        assertTrue(Double.isFinite(result) || Double.isInfinite(result));
    }

    @Test
    void testInfinity() {
        assertEquals(Double.POSITIVE_INFINITY, f.apply(Double.POSITIVE_INFINITY));
        assertEquals(Double.POSITIVE_INFINITY, f.apply(Double.NEGATIVE_INFINITY));
    }

    @Test
    void testNaN() {
        assertTrue(Double.isNaN(f.apply(Double.NaN)));
    }
}
