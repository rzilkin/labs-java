package mathproj.functions;

import mathproj.functions.LinkedListTabulatedFunction;
import mathproj.functions.MathFunction;
import mathproj.exceptions.ArrayIsNotSortedException;
import mathproj.exceptions.DifferentLengthOfArraysException;
import mathproj.exceptions.InterpolationException;
import mathproj.functions.Point;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import mathproj.operations.TabulatedFunctionOperationService;

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
    void testConstructorArraysInvalidLength() {
        // Тест на массивы длиной менее 2
        assertThrows(IllegalArgumentException.class, () -> {
            double[] x = {1.0};
            double[] y = {2.0};
            new LinkedListTabulatedFunction(x, y);
        });

        assertThrows(DifferentLengthOfArraysException.class, () -> {
            double[] x = {1.0};
            double[] y = {2.0, 3.0}; // разная длина
            new LinkedListTabulatedFunction(x, y);
        });

        assertThrows(DifferentLengthOfArraysException.class, () -> {
            double[] x = {1.0, 2.0};
            double[] y = {2.0}; // разная длина
            new LinkedListTabulatedFunction(x, y);
        });

        // Тест на пустые массивы
        assertThrows(IllegalArgumentException.class, () -> {
            double[] x = {};
            double[] y = {};
            new LinkedListTabulatedFunction(x, y);
        });
    }

    @Test
    void testConstructorFromFunctionInvalidCount() {
        MathFunction source = x -> x * x;

        // Тест на count < 2
        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunction(source, 0.0, 1.0, 1);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunction(source, 0.0, 1.0, 0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunction(source, 0.0, 1.0, -5);
        });
    }

    @Test
    void testGetXInvalidIndex() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(x, y);

        assertThrows(IllegalArgumentException.class, () -> func.getX(-1));
        assertThrows(IllegalArgumentException.class, () -> func.getX(3));
        assertThrows(IllegalArgumentException.class, () -> func.getX(100));
    }

    @Test
    void testGetYInvalidIndex() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(x, y);

        assertThrows(IllegalArgumentException.class, () -> func.getY(-1));
        assertThrows(IllegalArgumentException.class, () -> func.getY(3));
        assertThrows(IllegalArgumentException.class, () -> func.getY(50));
    }

    @Test
    void testSetYInvalidIndex() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(x, y);

        assertThrows(IllegalArgumentException.class, () -> func.setY(-1, 15.0));
        assertThrows(IllegalArgumentException.class, () -> func.setY(3, 15.0));
        assertThrows(IllegalArgumentException.class, () -> func.setY(10, 15.0));
    }

    @Test
    void testFloorIndexOfXInvalidX() {
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {10.0, 20.0, 30.0};
        LinkedListTabulatedFunction func = new LinkedListTabulatedFunction(x, y);

        // Тест на x меньше левой границы
        assertThrows(IllegalArgumentException.class, () -> func.floorIndexOfX(0.5));
        assertThrows(IllegalArgumentException.class, () -> func.floorIndexOfX(-1.0));
        assertThrows(IllegalArgumentException.class, () -> func.floorIndexOfX(Double.NEGATIVE_INFINITY));
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
        assertThrows(IllegalArgumentException.class, () -> {
            new LinkedListTabulatedFunction(x, y);
        });
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

    @Test
    void throwsOnDifferentLength() {
        double[] x = {0, 1};
        double[] y = {42};
        assertThrows(DifferentLengthOfArraysException.class, () -> new LinkedListTabulatedFunction(x, y));
    }

    @Test
    void throwsOnNotSorted() {
        double[] x = {1, 1};
        double[] y = {10, 20};
        assertThrows(ArrayIsNotSortedException.class, () -> new LinkedListTabulatedFunction(x, y));
    }

    @Test
    void throwsWhenXOutsideInterval() {
        double[] x = {0, 1, 2};
        double[] y = {0, 1, 2};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        // Интервал [1,2], floorIndex=1 — x=0.5 снаружи
        assertThrows(InterpolationException.class, () -> {
            f.interpolate(0.5, 1);
        });

        assertThrows(InterpolationException.class, () -> f.interpolate(0.5, 1));
    }

    @Test
    void returnsAllPointsInOrder_forLinkedListFunction() {
        double[] x = {-2.0, -1.0, 0.0, 1.0};
        double[] y = { 4.0,  1.0, 0.0, 1.0};
        LinkedListTabulatedFunction f = new LinkedListTabulatedFunction(x, y);

        Point[] pts = TabulatedFunctionOperationService.asPoints(f);

        assertEquals(x.length, pts.length);
        for (int i = 0; i < x.length; i++) {
            assertEquals(x[i], pts[i].x, 1e-12);
            assertEquals(y[i], pts[i].y, 1e-12);
        }
    }
}
