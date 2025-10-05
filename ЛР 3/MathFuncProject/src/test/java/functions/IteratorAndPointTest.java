package functions;

import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.Point;
import org.junit.jupiter.api.Test;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IteratorAndPointTest {
    @Test
    void testArrayTabulatedFunctionIteratorThrowsException() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction func = new ArrayTabulatedFunction(xValues, yValues);

        assertThrows(UnsupportedOperationException.class, () -> {
            Iterator<Point> iterator = func.iterator();
        });
    }

    @Test
    void testLinkedListTabulatedFunctionIteratorThrowsException() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(xValues, yValues);
        Iterator<Point> iterator = func.iterator();

        assertThrows(UnsupportedOperationException.class, () -> {
            iterator.remove();
        });
    }

    @Test
    void testPointClass() {
        Point p = new Point(3.5, 14.5);
        assertEquals(3.5, p.x, 1e-6);
        assertEquals(14.5, p.y, 1e-6);
        assertEquals("|3.5, 14.5|", p.toString());
    }
}
