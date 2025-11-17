package mathproj.functions;

import mathproj.functions.LinkedListTabulatedFunction;
import mathproj.functions.Point;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.*;

public class LinkListTabulatedFuncIteratorTest {

    @Test
    void testIteratorWithWhile() {
        double[] xValues = {1.0, 2.0, 3.0, 4.0};
        double[] yValues = {10.0, 20.0, 30.0, 40.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        Iterator<Point> iterator = function.iterator();

        int pointCount = 0;
        double[] expectedX = {1.0, 2.0, 3.0, 4.0};
        double[] expectedY = {10.0, 20.0, 30.0, 40.0};

        while (iterator.hasNext()) {
            Point point = iterator.next();
            // Проверяем ДО увеличения pointCount
            assertEquals(expectedX[pointCount], point.x, 1e-6);
            assertEquals(expectedY[pointCount], point.y, 1e-6);
            pointCount++;  // увеличиваем после проверки
        }

        assertEquals(4, pointCount);
    }

    @Test
    void testIteratorWithForEach() {
        double[] xValues = {0.5, 1.5, 2.5};
        double[] yValues = {5.0, 15.0, 25.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        int pointCount = 0;
        double[] expectedX = {0.5, 1.5, 2.5};
        double[] expectedY = {5.0, 15.0, 25.0};

        for (Point point : function) {
            assertEquals(expectedX[pointCount], point.x, 1e-6);
            assertEquals(expectedY[pointCount], point.y, 1e-6);

            pointCount++;
        }

        assertEquals(3, pointCount);
        assertEquals(function.getCount(), pointCount);
    }

    @Test
    void testIteratorThrowsExceptionWhenNoElements() {
        double[] xValues = {1.0, 2.0};
        double[] yValues = {10.0, 20.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        Iterator<Point> iterator = function.iterator();

        iterator.next();
        iterator.next();

        assertFalse(iterator.hasNext());

        assertThrows(NoSuchElementException.class, () -> iterator.next());
    }

    @Test
    void testIteratorRemoveThrowsException() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction function = new LinkedListTabulatedFunction(xValues, yValues);

        Iterator<Point> iterator = function.iterator();
        iterator.next();

        assertThrows(UnsupportedOperationException.class, () -> iterator.remove());
    }
}
