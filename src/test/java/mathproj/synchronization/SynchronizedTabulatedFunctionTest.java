package mathproj.synchronization;

import mathproj.concurrent.SynchronizedTabulatedFunction;
import mathproj.functions.LinkedListTabulatedFunction;
import mathproj.functions.Point;
import mathproj.functions.ArrayTabulatedFunction;
import mathproj.functions.TabulatedFunction;
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

    @Test
    void testDoSynchronouslyWithReturnValue() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        TabulatedFunction baseFunc = new ArrayTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(baseFunc);

        SynchronizedTabulatedFunction.Operation<Double> sumOperation =
                function -> {
                    double sum = 0;
                    for (Point point : function) {
                        sum += point.y;
                    }
                    return sum;
                };

        Double result = syncFunc.doSynchronously(sumOperation);
        assertEquals(60.0, result, 1e-9);
    }

    @Test
    void testDoSynchronouslyWithVoid() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {10.0, 20.0, 30.0};
        TabulatedFunction baseFunc = new LinkedListTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(baseFunc);

        SynchronizedTabulatedFunction.Operation<Void> updateOperation =
                function -> {
                    for (int i = 0; i < function.getCount(); i++) {
                        function.setY(i, function.getY(i) * 2);
                    }
                    return null;
                };

        Void result = syncFunc.doSynchronously(updateOperation);
        assertNull(result);

        Double checkResult = syncFunc.doSynchronously(func -> func.getY(0));
        assertEquals(20.0, checkResult, 1e-9);
    }

    @Test
    void testDoSynchronouslyComplexOperation() {
        double[] xValues = {0.0, 1.0, 2.0};
        double[] yValues = {0.0, 1.0, 4.0};
        TabulatedFunction baseFunc = new ArrayTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(baseFunc);

        SynchronizedTabulatedFunction.Operation<String> complexOperation =
                function -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Count: ").append(function.getCount());
                    sb.append(", Left: ").append(function.leftBound());
                    sb.append(", Right: ").append(function.rightBound());

                    function.setY(1, 100.0);

                    return sb.toString();
                };

        String result = syncFunc.doSynchronously(complexOperation);
        assertEquals("Count: 3, Left: 0.0, Right: 2.0", result);

        Double y1 = syncFunc.doSynchronously(func -> func.getY(1));
        assertEquals(100.0, y1, 1e-9);
    }

    @Test
    void testDoSynchronouslyWithLambda() {
        double[] xValues = {1.0, 2.0, 3.0};
        double[] yValues = {5.0, 6.0, 7.0};
        TabulatedFunction baseFunc = new ArrayTabulatedFunction(xValues, yValues);
        SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(baseFunc);

        Double average = syncFunc.doSynchronously(func -> {
            double sum = 0;
            for (int i = 0; i < func.getCount(); i++) {
                sum += func.getY(i);
            }
            return sum / func.getCount();
        });

        assertEquals(6.0, average, 1e-9);
    }
}
