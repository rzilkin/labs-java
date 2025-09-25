package test;

import functions.ArrayTabulatedFunction;
import functions.MathFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArrayTabulatedFunctionTest {

    private static final double EPS = 1e-9;

    /**
     * Test subclass that exposes protected methods for testing.
     */
    static class TestableArray extends ArrayTabulatedFunction {
        public TestableArray(double[] x, double[] y) {
            super(x, y);
        }

        public TestableArray(MathFunction source, double xFrom, double xTo, int count) {
            super(source, xFrom, xTo, count);
        }

        // public wrappers to access protected methods
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

        // mutate original arrays
        xs[0] = 999.0;
        ys[0] = 999.0;

        // internal arrays must not be affected (defensive copy)
        assertEquals(0.0, t.getX(0), EPS);
        assertEquals(0.0, t.getY(0), EPS);
    }

    @Test
    void testConstructorNullOrInvalidArgs() {
        double[] good = {0.0};
        assertThrows(NullPointerException.class, () -> new TestableArray(null, good));
        assertThrows(NullPointerException.class, () -> new TestableArray(good, null));
        // length mismatch
        double[] xs = {0.0, 1.0};
        double[] ys = {0.0};
        assertThrows(IllegalArgumentException.class, () -> new TestableArray(xs, ys));
        // empty arrays not allowed
        assertThrows(IllegalArgumentException.class, () -> new TestableArray(new double[]{}, new double[]{}));
        // unsorted x values
        double[] badX = {0.0, 2.0, 1.0};
        double[] y = {0.0, 4.0, 1.0};
        assertThrows(IllegalArgumentException.class, () -> new TestableArray(badX, y));
    }

    @Test
    void testConstructorFromFunctionBasic() {
        // source: f(x) = x^2
        MathFunction square = x -> x * x;
        TestableArray t = new TestableArray(square, 0.0, 2.0, 3);

        assertEquals(3, t.getCount());
        assertEquals(0.0, t.getX(0), EPS);
        assertEquals(1.0, t.getX(1), EPS);
        assertEquals(2.0, t.getX(2), EPS);

        assertEquals(0.0, t.getY(0), EPS);
        assertEquals(1.0, t.getY(1), EPS);
        assertEquals(4.0, t.getY(2), EPS);
        // last x equals right bound exactly (constructor enforces)
        assertEquals(2.0, t.rightBound(), EPS);
    }

    @Test
    void testConstructorFromFunctionSwapBoundsAndCountOne() {
        MathFunction id = x -> x;
        // swapped bounds
        TestableArray t = new TestableArray(id, 2.0, 0.0, 3);
        assertEquals(3, t.getCount());
        assertEquals(0.0, t.getX(0), EPS);
        assertEquals(2.0, t.getX(2), EPS);

        // count == 1
        TestableArray single = new TestableArray(id, 5.0, 10.0, 1);
        assertEquals(1, single.getCount());
        assertEquals(5.0, single.getX(0), EPS);
        assertEquals(5.0, single.getY(0), EPS);
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

        // setY changes value
        t.setY(1, 42.0);
        assertEquals(42.0, t.getY(1), EPS);
        assertEquals(1, t.indexOfY(42.0));
    }

    @Test
    void testInvalidIndexAccessors() {
        double[] xs = {0.0, 1.0};
        double[] ys = {0.0, 1.0};
        TestableArray t = new TestableArray(xs, ys);

        assertThrows(IndexOutOfBoundsException.class, () -> t.getX(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> t.getX(2));
        assertThrows(IndexOutOfBoundsException.class, () -> t.getY(2));
        assertThrows(IndexOutOfBoundsException.class, () -> t.setY(2, 3.0));
    }

    @Test
    void testInterpolationAndExtrapolationViaApply_linearFunction() {
        // underlying exact function y = 2x
        double[] xs = {0.0, 1.0, 2.0};
        double[] ys = {0.0, 2.0, 4.0};
        TestableArray t = new TestableArray(xs, ys);

        // interpolation inside interval
        assertEquals(1.0, t.apply(0.5), EPS);   // (0.5)*2 = 1.0
        assertEquals(3.0, t.apply(1.5), EPS);   // 1.5*2 = 3.0

        // extrapolation left and right
        assertEquals(-2.0, t.apply(-1.0), EPS); // 2*(-1) = -2
        assertEquals(6.0, t.apply(3.0), EPS);   // 2*3 = 6
    }

    @Test
    void testProtectedFloorInterpolateExtrapolateDirectly() {
        double[] xs = {0.0, 1.0, 2.0, 4.0};
        double[] ys = {0.0, 1.0, 4.0, 16.0}; // some values
        TestableArray t = new TestableArray(xs, ys);

        // floor index: before first => 0
        assertEquals(0, t.pubFloorIndex(-1.0));
        // floor index: equal to first => 0 (no element < first)
        assertEquals(0, t.pubFloorIndex(0.0));
        // floor index: between 1 and 2 => 1
        assertEquals(1, t.pubFloorIndex(1.5));
        // floor index: greater than last => count (4)
        assertEquals(4, t.pubFloorIndex(10.0));

        // direct interpolation (between x1=1 and x2=2 at x=1.5)
        double interp = t.pubInterpolate(1.5, 1);
        assertEquals(2.5, interp, 1e-9); // linear between (1,1) and (2,4): 1 + 0.5*(4-1)/(1) = 2.5

        // extrapolate left using first two points (0 and 1)
        assertEquals(-0.25, t.pubExtrapolateLeft(-0.25), 1e-9);
        // extrapolate right using last two points (2 and 4)
        assertEquals(22.0, t.pubExtrapolateRight(5.0), 1e-9); // between (2,4) and (4,16) slope=6 -> at 5: 16 + 1*6 = 22? Wait compute carefully
        // recalc expected: slope = (16-4)/(4-2)=12/2=6 -> at x=5 => y=16 + (5-4)*6 = 22
        // But we asserted 28 above erroneously; fix to expected 22
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

        // exact node
        assertEquals(1.0, t.apply(1.0), EPS); // exact match returns getY(index)

        // NaN propagation
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

        // indexOfY returns first matching index
        assertEquals(0, t.indexOfY(1.0));
        assertEquals(1, t.indexOfY(2.0));
    }

    @Test
    void testConstructorFromFunction_withSameBounds() {
        MathFunction f = x -> Math.sin(x);
        // xFrom == xTo case: all x should be equal to that bound, all y equal to f(bound)
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
}
