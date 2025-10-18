package functions;

import functions.ConstantFunction;
import functions.MathFunction;
import functions.UnitFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnitFunctionTest {

    private static final double EPS = 1e-12;

    @Test
    void alwaysReturnsOne() {
        UnitFunction u = new UnitFunction();
        assertEquals(1.0, u.apply(0.0), EPS);
        assertEquals(1.0, u.apply(9999.9), EPS);
        assertEquals(1.0, u.apply(-0.5), EPS);
    }

    @Test
    void isASpecializedConstantFunction() {
        UnitFunction u = new UnitFunction();
        assertTrue(u instanceof ConstantFunction);
        assertTrue(u instanceof MathFunction);
    }
}
