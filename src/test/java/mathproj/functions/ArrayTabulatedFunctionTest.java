package mathproj.functions;

import mathproj.functions.ArrayTabulatedFunction;
import mathproj.functions.MathFunction;
import mathproj.functions.Point;
import mathproj.operations.TabulatedFunctionOperationService;
import mathproj.exceptions.ArrayIsNotSortedException;
import mathproj.exceptions.DifferentLengthOfArraysException;
import mathproj.exceptions.InterpolationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArrayTabulatedFunctionTest {

    private static final double EPS = 1e-9;

    static class TestableArray extends ArrayTabulatedFunction {
        public TestableArray(double[] x, double[] y) {
            super(x, y);
        }

        public TestableArray(MathFunction source, double xFrom, double xTo, int count) {
            super(source, xFrom, xTo, count);
        }

        public int pubFloorIndex(double x) {
            return super.floorIndexOfX(x);
        }

        public double pubInterpolate(double x, int idx) {
            return super.interpolate(x, idx);
        }

        public double pubExtrapolateLeft(double x) {
            return super.extrapolateLeft(x);
        }

        public double pubExtrapolateRight(double x) {
            return super.extrapolateRight(x);
        }
    }

    @Test
    void testConstructorAndGetters() {
        double[] xs = {0.0, 1.0, 2.0};
        double[] ys = {0.0, 2.0, 4.0};
        TestableArray t = new TestableArray(xs, ys);

        assertEquals(3, t.getCount());
        assertEquals(0.0, t.leftBound(), EPS);
        assertEquals(2.0, t.rightBound(), EPS);

        assertEquals(0.0, t.getX(0), EPS);
        assertEquals(4.0, t.getY(2), EPS);
    }

    @Test
    void testDefensiveCopy() {
        double[] xs = {0.0, 1.0, 2.0};
        double[] ys = {0.0, 1.0, 4.0};
        TestableArray t = new TestableArray(xs, ys);

        xs[0] = 999.0;
        ys[0] = 999.0;

        assertEquals(0.0, t.getX(0), EPS);
        assertEquals(0.0, t.getY(0), EPS);
    }

    @Test
    void testConstructorNullOrInvalidArgs() {
        double[] good = {0.0};
        assertThrows(NullPointerException.class, () -> new TestableArray(null, good));
        assertThrows(NullPointerException.class, () -> new TestableArray(good, null));

        double[] xs = {0.0, 1.0};
        double[] ys = {0.0};
        assertThrows(DifferentLengthOfArraysException.class, () -> new TestableArray(xs, ys));

        assertThrows(IllegalArgumentException.class, () -> new TestableArray(new double[]{}, new double[]{}));

        double[] badX = {0.0, 2.0, 1.0};
        double[] y = {0.0, 4.0, 1.0};
        assertThrows(ArrayIsNotSortedException.class, () -> new TestableArray(badX, y));
    }

    @Test
    void testConstructorFromFunctionBasic() {

        MathFunction square = x -> x * x;
        TestableArray t = new TestableArray(square, 0.0, 2.0, 3);

        assertEquals(3, t.getCount());
        assertEquals(0.0, t.getX(0), EPS);
        assertEquals(1.0, t.getX(1), EPS);
        assertEquals(2.0, t.getX(2), EPS);

        assertEquals(0.0, t.getY(0), EPS);
        assertEquals(1.0, t.getY(1), EPS);
        assertEquals(4.0, t.getY(2), EPS);

        assertEquals(2.0, t.rightBound(), EPS);
    }

    @Test
    void testConstructorFromFunctionSwapBoundsAndCountOne() {
        MathFunction id = x -> x;

        TestableArray t = new TestableArray(id, 2.0, 0.0, 3);
        assertEquals(3, t.getCount());
        assertEquals(0.0, t.getX(0), EPS);
        assertEquals(2.0, t.getX(2), EPS);

        assertThrows(IllegalArgumentException.class, () -> new TestableArray(id, 5.0, 10.0, 1));
    }

    @Test
    void testIndexOfXAndIndexOfYAndSetY() {
        double[] xs = {0.0, 1.0, 2.0};
        double[] ys = {0.0, 5.0, 10.0};
        TestableArray t = new TestableArray(xs, ys);

        assertEquals(1, t.indexOfX(1.0));
        assertEquals(-1, t.indexOfX(1.5));

        assertEquals(2, t.indexOfY(10.0));
        assertEquals(-1, t.indexOfY(-1.0));

        t.setY(1, 42.0);
        assertEquals(42.0, t.getY(1), EPS);
        assertEquals(1, t.indexOfY(42.0));
    }

    @Test
    void testInvalidIndexAccessors() {
        double[] xs = {0.0, 1.0};
        double[] ys = {0.0, 1.0};
        TestableArray t = new TestableArray(xs, ys);

        assertThrows(IllegalArgumentException.class, () -> t.getX(-1));
        assertThrows(IllegalArgumentException.class, () -> t.getX(2));
        assertThrows(IllegalArgumentException.class, () -> t.getY(2));
        assertThrows(IllegalArgumentException.class, () -> t.setY(2, 3.0));
    }

    @Test
    void testInterpolationAndExtrapolationViaApply_linearFunction() {
        double[] xs = {0.0, 1.0, 2.0};
        double[] ys = {0.0, 2.0, 4.0};
        TestableArray t = new TestableArray(xs, ys);

        assertEquals(1.0, t.apply(0.5), EPS);   // (0.5)*2 = 1.0
        assertEquals(3.0, t.apply(1.5), EPS);   // 1.5*2 = 3.0

        assertEquals(-2.0, t.apply(-1.0), EPS); // 2*(-1) = -2
        assertEquals(6.0, t.apply(3.0), EPS);   // 2*3 = 6
    }

    @Test
    void testProtectedFloorInterpolateExtrapolateDirectly() {
        double[] xs = {0.0, 1.0, 2.0, 4.0};
        double[] ys = {0.0, 1.0, 4.0, 16.0}; // some values
        TestableArray t = new TestableArray(xs, ys);

        assertThrows(IllegalArgumentException.class, () -> t.pubFloorIndex(-1.0));
        assertThrows(IllegalArgumentException.class, () -> t.pubFloorIndex(0.0));
        assertEquals(1, t.pubFloorIndex(1.5));
        assertEquals(4, t.pubFloorIndex(10.0));

        double interp = t.pubInterpolate(1.5, 1);
        assertEquals(2.5, interp, 1e-9); // linear between (1,1) and (2,4): 1 + 0.5*(4-1)/(1) = 2.5

        assertEquals(-0.25, t.pubExtrapolateLeft(-0.25), 1e-9);
        assertEquals(22.0, t.pubExtrapolateRight(5.0), 1e-9); // between (2,4) and (4,16) slope=6 -> at 5: 16 + 1*6 = 22? Wait compute carefully
    }

    @Test
    void testPubExtrapolateRightCorrectValue() {
        double[] xs = {2.0, 4.0};
        double[] ys = {4.0, 16.0}; // slope = 6
        TestableArray t = new TestableArray(xs, ys);

        assertEquals(22.0, t.pubExtrapolateRight(5.0), EPS); // 16 + (5-4)*6 = 22
        assertEquals(-2.0, t.pubExtrapolateLeft(1.0), EPS); // left extrapolation: 4 + (1-2)*6 = -2
    }

    @Test
    void testApplyExactNodeAndNaN() {
        double[] xs = {0.0, 1.0, 2.0};
        double[] ys = {0.0, 1.0, 4.0};
        TestableArray t = new TestableArray(xs, ys);

        assertEquals(1.0, t.apply(1.0), EPS); // exact match returns getY(index)

        assertTrue(Double.isNaN(t.apply(Double.NaN)));
    }

    @Test
    void testIndexOfXBinarySearchBehavior() {
        double[] xs = {0.0, 0.1, 0.2};
        double[] ys = {0.0, 1.0, 2.0};
        TestableArray t = new TestableArray(xs, ys);

        assertEquals(1, t.indexOfX(0.1));
        assertEquals(-1, t.indexOfX(0.15)); // not exact, binarySearch returns negative -> indexOfX returns -1
    }

    @Test
    void testDiscretizationLastXEqualsRight() {
        MathFunction id = x -> x;
        TestableArray t = new TestableArray(id, 0.0, 1.0, 5);
        assertEquals(1.0, t.getX(4), EPS);
        assertEquals(0.0, t.getX(0), EPS);
    }

    @Test
    void testIndexOfYMultipleEqualElements() {
        double[] xs = {0.0, 1.0, 2.0, 3.0};
        double[] ys = {1.0, 2.0, 1.0, 1.0};
        TestableArray t = new TestableArray(xs, ys);

        assertEquals(0, t.indexOfY(1.0));
        assertEquals(1, t.indexOfY(2.0));
    }

    @Test
    void testConstructorFromFunction_withSameBounds() {
        MathFunction f = x -> Math.sin(x);

        TestableArray t = new TestableArray(f, 1.2345, 1.2345, 4);
        assertEquals(4, t.getCount());
        for (int i = 0; i < t.getCount(); i++) {
            assertEquals(1.2345, t.getX(i), EPS);
            assertEquals(f.apply(1.2345), t.getY(i), EPS);
        }
    }

    @Test
    void testConstructorFromFunction_invalidCount() {
        MathFunction f = x -> x;
        assertThrows(IllegalArgumentException.class, () -> new TestableArray(f, 0.0, 1.0, 0));
        assertThrows(IllegalArgumentException.class, () -> new TestableArray(f, 0.0, 1.0, -5));
    }

    @Test
    void testInsert() {
        double[] xs = {0.0, 1.0, 2.0, 3.0};
        double[] ys = {1.0, 2.0, 1.0, 1.0};
        TestableArray t = new TestableArray(xs, ys);

        t.insert(1.5, 2.1);
        int res1 = t.indexOfX(1.5);
        assertEquals(2, res1, 1e-6);

        t.insert(-1.0, 1.99);
        int res2 = t.indexOfX(-1.0);
        assertEquals(0, res2, 1e-6);

        t.insert(3.7, 2.9);
        int res3 = t.indexOfX(3.7);
        assertEquals(6, res3, 1e-6);

        t.insert(1.0, 2.77);
        int res4 = t.indexOfY(2.77);
        assertEquals(2, res4, 1e-6);

    }

    @Test
    void removeHead() {
        double[] xs = {1.0, 2.0, 3.0};
        double[] ys = {10.0, 20.0, 30.0};
        ArrayTabulatedFunction arr = new ArrayTabulatedFunction(xs, ys);

        arr.remove(0);

        assertEquals(2, arr.getCount());
        assertEquals(2.0, arr.getX(0), EPS);   // теперь первый элемент — был второй
        assertEquals(3.0, arr.getX(1), EPS);
    }

    @Test
    void removeMiddle() {
        double[] xs = {0.0, 1.0, 2.0, 3.0};
        double[] ys = {0.0, 10.0, 20.0, 30.0};
        ArrayTabulatedFunction arr = new ArrayTabulatedFunction(xs, ys);

        arr.remove(2); // удалить x==2.0

        assertEquals(3, arr.getCount());
        assertEquals(0.0, arr.getX(0), EPS);
        assertEquals(1.0, arr.getX(1), EPS);
        assertEquals(3.0, arr.getX(2), EPS);
    }

    @Test
    void removeTail() {
        double[] xs = {1.0, 2.0, 5.0};
        double[] ys = {10.0, 20.0, 50.0};
        ArrayTabulatedFunction arr = new ArrayTabulatedFunction(xs, ys);

        arr.remove(arr.getCount() - 1); // удалить последний

        assertEquals(2, arr.getCount());
        assertEquals(2.0, arr.rightBound(), EPS); // правый предел обновлён
    }

    @Test
    void removeSingleElement() {
        double[] xs = {3.14};
        double[] ys = {42.0};
        assertThrows(IllegalArgumentException.class, () -> new ArrayTabulatedFunction(xs, ys));
    }

    @Test
    void removeInvalidIndices() {
        double[] xs = {0.0, 1.0};
        double[] ys = {0.0, 1.0};
        ArrayTabulatedFunction arr = new ArrayTabulatedFunction(xs, ys);

        assertThrows(IndexOutOfBoundsException.class, () -> arr.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> arr.remove(arr.getCount()));
    }

    @Test
    void consecutiveRemovals() {
        double[] xs = {0.0, 1.0, 2.0, 3.0};
        double[] ys = {0.0, 10.0, 20.0, 30.0};
        ArrayTabulatedFunction arr = new ArrayTabulatedFunction(xs, ys);

        arr.remove(0); // удалили 0.0
        assertEquals(3, arr.getCount());
        assertEquals(1.0, arr.getX(0), EPS);

        arr.remove(1); // теперь удаляем средний (первоначально 2.0)
        assertEquals(2, arr.getCount());
        assertEquals(1.0, arr.getX(0));
        assertEquals(3.0, arr.getX(1));

        arr.remove(1); // удаляем хвост
        assertEquals(1, arr.getCount());
        assertEquals(1.0, arr.getX(0));
    }

    @Test
    void throwsOnDifferentLength() {
        double[] x = {0, 1};
        double[] y = {10};
        assertThrows(DifferentLengthOfArraysException.class, () -> new ArrayTabulatedFunction(x, y));
    }

    @Test
    void throwsOnNotSorted() {
        double[] x = {0, 0}; // нестрого
        double[] y = {10, 20};
        assertThrows(ArrayIsNotSortedException.class, () -> new ArrayTabulatedFunction(x, y));
    }

    @Test
    void throwsWhenXOutsideInterval() {
        double[] x = {0, 1, 2};
        double[] y = {0, 1, 2};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x, y);

        assertThrows(InterpolationException.class, () -> {
            f.interpolate(-0.1, 0);
        });

        assertThrows(InterpolationException.class, () -> {
            f.interpolate(1.5, 0);
        });

        assertDoesNotThrow(() -> {
            double v = f.interpolate(0.25, 0);
            assertTrue(v >= 0 && v <= 1);
        });
    }

    @Test
    void iteratorWhile_loop_returnsAllPointsInOrder() {
        double[] x = {2.0, 3.0, 5.0};
        double[] y = {4.0, 9.0, 25.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x, y);

        Iterator<Point> it = f.iterator();
        List<Double> xs = new ArrayList<>();
        List<Double> ys = new ArrayList<>();

        while (it.hasNext()) {
            Point p = it.next();
            xs.add(p.x);
            ys.add(p.y);
        }

        assertArrayEquals(x, xs.stream().mapToDouble(Double::doubleValue).toArray(), 1e-12);
        assertArrayEquals(y, ys.stream().mapToDouble(Double::doubleValue).toArray(), 1e-12);
    }

    @Test
    void iteratorForEach_loop_returnsAllPointsInOrder() {
        double[] x = {-1.0, 0.0, 1.0, 2.0};
        double[] y = {1.0, 0.0, 1.0, 4.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x, y);

        int i = 0;
        for (Point p : f) {
            assertEquals(x[i], p.x, EPS);
            assertEquals(y[i], p.y, EPS);
            i++;
        }
        assertEquals(x.length, i);
    }

    @Test
    void returnsAllPointsInOrder_forArrayFunction() {
        double[] x = {0.0, 1.0, 2.0, 3.0};
        double[] y = {0.0, 1.0, 4.0, 9.0};
        ArrayTabulatedFunction f = new ArrayTabulatedFunction(x, y);

        Point[] pts = TabulatedFunctionOperationService.asPoints(f);

        assertEquals(x.length, pts.length);
        for (int i = 0; i < x.length; i++) {
            assertEquals(x[i], pts[i].x, 1e-12);
            assertEquals(y[i], pts[i].y, 1e-12);
        }
    }

    @Test
    void throwsOnNull() {
        assertThrows(NullPointerException.class,
                () -> TabulatedFunctionOperationService.asPoints(null));
    }
}
