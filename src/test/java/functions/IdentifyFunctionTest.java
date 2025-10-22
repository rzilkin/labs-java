package functions;

import functions.IdentifyFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdentifyFunctionTest {

    private final IdentifyFunction id_f = new IdentifyFunction();

    @Test
    void testPositiveNumber() {
        assertEquals(2.0, id_f.apply(2.0), 1e-6);
        assertEquals(1.0, id_f.apply(1.0), 1e-6);
    }

    @Test
    void testNegativeNumber() {
        assertEquals(-10.0, id_f.apply(-10.0), 1e-6);
        assertEquals(-37.0, id_f.apply(-37.0), 1e-6);
    }

    @Test
    void testZeroNumber() {
        assertEquals(0.0, id_f.apply(0.0), 1e-6);
    }

    @Test
    void testFloatNumber() {
        assertEquals(17.786, id_f.apply(17.786), 1e-6);
        assertEquals(0.032, id_f.apply(0.032), 1e-6);
    }

    @Test
    void testBigNumber() {
        double res = id_f.apply(10e134);
        assertTrue(Double.isFinite(res) || Double.isInfinite(res));
    }

    @Test
    void testInfinity() {
        assertEquals(Double.POSITIVE_INFINITY, id_f.apply(Double.POSITIVE_INFINITY));
        assertEquals(Double.NEGATIVE_INFINITY, id_f.apply(Double.NEGATIVE_INFINITY));
    }

    @Test
    void testNan() {
        assertTrue(Double.isNaN(id_f.apply(Double.NaN)));
    }

}