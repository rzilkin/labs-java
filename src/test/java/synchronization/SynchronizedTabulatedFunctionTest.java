package synchronization;

import concurrent.SynchronizedTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.Point;
import functions.ArrayTabulatedFunction;
import functions.TabulatedFunction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;

class SynchronizedTabulatedFunctionTest {

    @Test
    void testGetCount() {
        double[] xValues = {0, 1, 5};
        double[] yValues = {0, 1, 17};
        TabulatedFunction baseFunc = new LinkedListTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(baseFunc);

        assertEquals(3, syncFunc.getCount());
    }

    @Test
    void testGetXAndGetY() {
        double[] xValues = {0, 1, 5};
        double[] yValues = {0, 1, 17};
        TabulatedFunction baseFunc = new LinkedListTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(baseFunc);

        assertEquals(0, syncFunc.getX(0));
        assertEquals(1, syncFunc.getX(1));
        assertEquals(5, syncFunc.getX(2));

        assertEquals(0, syncFunc.getY(0));
        assertEquals(1, syncFunc.getY(1));
        assertEquals(17, syncFunc.getY(2));
    }

    @Test
    void testSetY() {
        double[] xValues = {0, 1, 5};
        double[] yValues = {0, 1, 17};
        TabulatedFunction baseFunc = new LinkedListTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(baseFunc);

        syncFunc.setY(1, 11);
        assertEquals(11, syncFunc.getY(1));

        syncFunc.setY(2, 227);
        assertEquals(227, syncFunc.getY(2));
    }

    @Test
    void testIndexOfXAndIndexOfY() {
        double[] xValues = {0, 1, 5};
        double[] yValues = {0, 1, 17};
        TabulatedFunction baseFunc = new LinkedListTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(baseFunc);

        assertEquals(1, syncFunc.indexOfX(1));
        assertEquals(0, syncFunc.indexOfX(0));
        assertEquals(2, syncFunc.indexOfX(5));
        assertEquals(-1, syncFunc.indexOfX(53));

        assertEquals(1, syncFunc.indexOfY(1));
        assertEquals(0, syncFunc.indexOfY(0));
        assertEquals(2, syncFunc.indexOfY(17));
        assertEquals(-1, syncFunc.indexOfY(5));
    }

    @Test
    void testLeftBoundAndRightBound() {
        double[] xValues = {1, 2, 5};
        double[] yValues = {1, 16, 18};
        TabulatedFunction baseFunc = new LinkedListTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(baseFunc);

        assertEquals(1, syncFunc.leftBound());
        assertEquals(5, syncFunc.rightBound());
    }

    @Test
    void testApply() {
        double[] xValues = {1, 2, 5};
        double[] yValues = {1, 16, 18};
        TabulatedFunction baseFunc = new LinkedListTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(baseFunc);

        assertEquals(1, syncFunc.apply(1));
        assertEquals(16, syncFunc.apply(2));
        assertEquals(18, syncFunc.apply(5));
    }

    @Test
    void testIterator() {
        double[] xValues = {0, 1, 2};
        double[] yValues = {0, 1, 4};
        TabulatedFunction baseFunc = new LinkedListTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(baseFunc);

        Iterator<Point> iterator = syncFunc.iterator();
        assertTrue(iterator.hasNext());

        Point point1 = iterator.next();
        assertEquals(0, point1.x);
        assertEquals(0, point1.y);

        Point point2 = iterator.next();
        assertEquals(1, point2.x);
        assertEquals(1, point2.y);

        Point point3 = iterator.next();
        assertEquals(2, point3.x);
        assertEquals(4, point3.y);

        assertFalse(iterator.hasNext());
    }

}
