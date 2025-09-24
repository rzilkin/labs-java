package test;

import functions.LinkedListTabulatedFunction;
import functions.MathFunction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LinkedListTabulatedFunctionTest {

    private LinkedListTabulatedFunction createFunctionWithArrays() {
        double[] xValues = {3.0, 5.0, 8.0, 12.0};
        double[] yValues = {1.0, 12.0, 15.0, 30.0};
        return new LinkedListTabulatedFunction(xValues, yValues);
    }

    private LinkedListTabulatedFunction createFunctionWithMathFunction() {
        MathFunction source = new MathFunction() {
            @Override
            public double apply(double x) {
                return x * x;
            }
        };
        return new LinkedListTabulatedFunction(source, 2, 10, 7);
    }

    @Test
    void testConstructorWithArrays() {
        LinkedListTabulatedFunction func = createFunctionWithArrays();
        assertEquals(4, func.getCount());
        assertEquals(3.0, func.leftBound());
        assertEquals(12.0, func.rightBound());
        assertEquals(12.0, func.getY(1));
    }

    @Test
    void testConstructorFromMathFunction() {
        LinkedListTabulatedFunction func = createFunctionWithMathFunction();
        assertEquals(7, func.getCount());
        assertEquals(2.0, func.leftBound());
        assertEquals(10.0, func.rightBound());
        assertEquals(4.0, func.getY(0));
        assertEquals(36.0, func.getY(4));
    }

    @Test
    void testGetCount() {
        LinkedListTabulatedFunction func = createFunctionWithArrays();
        assertEquals(4, func.getCount());
    }

    @Test
    void testLeftBound() {
        LinkedListTabulatedFunction func = createFunctionWithArrays();
        assertEquals(3.0, func.leftBound());
    }

    @Test
    void testRightBound() {
        LinkedListTabulatedFunction func = createFunctionWithArrays();
        assertEquals(12.0, func.rightBound());
    }

    @Test
    void testGetX() {
        LinkedListTabulatedFunction func = createFunctionWithArrays();
        assertEquals(8.0, func.getX(2));
        assertEquals(5.0, func.getX(1));
    }

    @Test
    void testGetY() {
        LinkedListTabulatedFunction func = createFunctionWithArrays();
        assertEquals(1.0, func.getY(0));
        assertEquals(30.0, func.getY(3));
    }

    @Test
    void testSetY() {
        LinkedListTabulatedFunction func = createFunctionWithArrays();
        func.setY(1, 234.0);
        assertEquals(234.0, func.getY(1));
        assertEquals(1.0, func.getY(0));
        assertEquals(15.0, func.getY(2));
        assertEquals(30.0, func.getY(3));
    }

    @Test
    void testIndexOfX() {
        LinkedListTabulatedFunction func = createFunctionWithArrays();
        assertEquals(3, func.indexOfX(12.0));
        assertEquals(-1, func.indexOfX(7.467));
    }

    @Test
    void testIndexOfY() {
        LinkedListTabulatedFunction func = createFunctionWithArrays();
        assertEquals(-1, func.indexOfY(1982.456));
        assertEquals(2, func.indexOfY(15.0));
    }

    void testFloorIndexOfX() {
        LinkedListTabulatedFunction func = createFunctionWithArrays();
        assertEquals(0, func.floorIndexOfX(3.78));
        assertEquals(1, func.floorIndexOfX(5.67));
        assertEquals(2, func.floorIndexOfX(8.0));
        assertEquals(0, func.floorIndexOfX(1.5));
        assertEquals(2, func.floorIndexOfX(12.3));
    }

    @Test
    void testInterpolate() {
        LinkedListTabulatedFunction func = createFunctionWithArrays();
        double res = func.interpolate(6.5, 1);
        assertEquals(13.5, res, 1e-6);
    }

    @Test
    void testExtrapolateLeft() {
        LinkedListTabulatedFunction func = createFunctionWithArrays();
        double res = func.extrapolateLeft(1.5);
        assertEquals(-7.25, res, 1e-6);
    }

    @Test
    void testExtrapolateRight() {
        LinkedListTabulatedFunction func = createFunctionWithArrays();
        double res = func.extrapolateRight(13.4);
        assertEquals(35.25, res, 1e-6);
    }

}
