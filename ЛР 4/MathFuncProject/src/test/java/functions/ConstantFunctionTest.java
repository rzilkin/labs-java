package functions;

import functions.ConstantFunction;
import functions.MathFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConstantFunctionTest {

    private static final double EPS = 1e-12;

    @Test
    void returnsSameValueForDifferentArguments() {
        ConstantFunction f = new ConstantFunction(42.0);
        assertEquals(42.0, f.apply(0.0), EPS);
        assertEquals(42.0, f.apply(1.23), EPS);
        assertEquals(42.0, f.apply(-999.99), EPS);
    }

    @Test
    void getterReturnsConstructorValue() {
        ConstantFunction f = new ConstantFunction(3.14);
        assertEquals(3.14, f.getValue(), EPS);
    }

    @Test
    void implementsMathFunction() {
        ConstantFunction f = new ConstantFunction(-7.5);
        assertTrue(f instanceof MathFunction);
    }
}
