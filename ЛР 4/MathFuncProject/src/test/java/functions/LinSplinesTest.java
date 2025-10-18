package functions;

import functions.LinSplines;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LinSplinesTest {

    private double[] xValues = {1.7, 2.8, 5.4, 8.0, 12.0, 13.34, 17};
    private double[] yValues = {0.32, 4.2, 9.3, 11, 13.0, 20.5, 23};

    LinSplines spline = new LinSplines(xValues, yValues);

    @Test
    void testExtrapolationLeft() {
        assertEquals(-1.443636, spline.apply(1.2), 1e-6);
    }

    @Test
    void testExtrapolationRight() {
        assertEquals(24.297814, spline.apply(18.9), 1e-6);
    }

    @Test
    void testInterpolation() {
        assertEquals(9.496153, spline.apply(5.7), 1e-6);
    }

    @Test
    void testBestInterpolation() {
        assertEquals(20.5, spline.apply(13.34), 1e-6);
    }

    @Test
    void testClearXValues() {
        double[] xClear = {};
        double[] yClear = {};

        LinSplines secondSpline = new LinSplines(xClear, yClear);

        assertEquals(0.0, secondSpline.apply(5.9), 1e-6);
        assertEquals(0.0, secondSpline.apply(123), 1e-6);
    }
}
