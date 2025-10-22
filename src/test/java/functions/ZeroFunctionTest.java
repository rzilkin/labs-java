package functions;

import functions.ConstantFunction;
import functions.MathFunction;
import functions.ZeroFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZeroFunctionTest {

    private static final double EPS = 1e-12;

    @Test
    void alwaysReturnsZero() {
        ZeroFunction z = new ZeroFunction();
        assertEquals(0.0, z.apply(0.0), EPS);
        assertEquals(0.0, z.apply(123.456), EPS);
        assertEquals(0.0, z.apply(-3.14), EPS);
    }

    @Test
    void isASpecializedConstantFunction() {
        ZeroFunction z = new ZeroFunction();
        assertTrue(z instanceof ConstantFunction);
        assertTrue(z instanceof MathFunction);
    }
}
