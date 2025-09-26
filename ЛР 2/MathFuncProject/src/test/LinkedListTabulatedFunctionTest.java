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

    @Test
    void testInsert() {
        LinkedListTabulatedFunction func = createFunctionWithArrays();
        func.insert(4.0, 7.12);
        double res1 = func.indexOfY(7.12);
        assertEquals(1, res1, 1e-6);
        func.insert(13.0, 90);
        double res2 = func.indexOfY(90);
        assertEquals(5, res2, 1e-6);
        func.insert(1.5, 0.52);
        double res3 = func.indexOfY(0.52);
        assertEquals(0, res3, 1e-6);
    }

    @Test
    void testRemoveFirstElement() {
        // Создаём список из трёх элементов
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(x, y);

        // Удаляем первый элемент
        func.remove(0);

        // Теперь первый элемент должен быть (2.0, 20.0)
        assertEquals(2.0, func.getX(0));
        assertEquals(20.0, func.getY(0));
        assertEquals(2, func.getCount());
    }

    @Test
    void testRemoveLastElement() {
        // Создаём список из трёх элементов
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(x, y);

        // Удаляем последний элемент
        func.remove(2);

        // Последний элемент теперь (2.0, 20.0)
        assertEquals(2.0, func.getX(1));
        assertEquals(20.0, func.getY(1));
        assertEquals(2, func.getCount());
    }

    @Test
    void testRemoveMiddleElement() {
        // Создаём список из трёх элементов
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(x, y);

        // Удаляем средний элемент (индекс 1)
        func.remove(1);

        // Теперь элементы: (1.0, 10.0) и (3.0, 30.0)
        assertEquals(2, func.getCount());
        assertEquals(1.0, func.getX(0));
        assertEquals(10.0, func.getY(0));
        assertEquals(3.0, func.getX(1));
        assertEquals(30.0, func.getY(1));
    }

    @Test
    void testRemoveSingleElementList() {
        // Создаём список из одного элемента
        double[] x = {1.0};
        double[] y = {10.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(x, y);

        // Удаляем единственный элемент — список должен стать пустым
        func.remove(0);

        // Ожидаем, что количество стало нулевым
        assertEquals(0, func.getCount());
    }

    @Test
    void testRemoveInvalidIndex() {
        // Создаём список из двух элементов
        double[] x = {1.0, 2.0};
        double[] y = {10.0, 20.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(x, y);

        // Неверный индекс (отрицательный и слишком большой)
        assertThrows(IndexOutOfBoundsException.class, () -> func.remove(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> func.remove(5));
    }

}
