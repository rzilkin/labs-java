package operations;

import exceptions.InconsistentFunctionsException;
import functions.ArrayTabulatedFunction;
import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import functions.factory.LinkedListTabulatedFunctionFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TabulatedFunctionOperationServiceTest {
    @Test
    void sum_arrayPlusArray_returnsArrayByDefaultFactory() {
        double[] x = {0, 1, 2};
        double[] y1 = {0, 1, 4};
        double[] y2 = {1, 1, 1};

        TabulatedFunction a = new ArrayTabulatedFunction(x, y1);
        TabulatedFunction b = new ArrayTabulatedFunction(x, y2);

        TabulatedFunctionOperationService svc = new TabulatedFunctionOperationService(); // Array фабрика по умолчанию
        TabulatedFunction c = svc.sum(a, b);

        assertTrue(c instanceof ArrayTabulatedFunction);
        assertEquals(3, c.getCount());
        assertEquals(0, c.getX(0));
        assertEquals(2, c.getX(2));
        assertEquals(1, c.getY(0), 1e-12); // 0+1
        assertEquals(2, c.getY(1), 1e-12); // 1+1
        assertEquals(5, c.getY(2), 1e-12); // 4+1
    }

    @Test
    void subtract_mixedTypes_returnsArrayByDefaultFactory() {
        double[] x = {-1, 0, 1};
        double[] y1 = {1, 0, 1};
        double[] y2 = {2, 2, 2};

        TabulatedFunction a = new LinkedListTabulatedFunction(x, y1);
        TabulatedFunction b = new ArrayTabulatedFunction(x, y2);

        TabulatedFunctionOperationService svc = new TabulatedFunctionOperationService(); // Array фабрика
        TabulatedFunction c = svc.subtract(a, b);

        assertTrue(c instanceof ArrayTabulatedFunction);
        assertEquals(-1, c.getX(0), 1e-12);
        assertEquals(1, c.getX(2), 1e-12);
        assertEquals(-1, c.getY(0), 1e-12); // 1-2
        assertEquals(-2, c.getY(1), 1e-12); // 0-2
        assertEquals(-1, c.getY(2), 1e-12); // 1-2
    }

    @Test
    void sum_arrayPlusLinkedList_returnsLinkedListByConfiguredFactory() {
        double[] x = {2, 3, 5, 8};
        double[] y1 = {1, 1, 2, 3};
        double[] y2 = {10, 20, 30, 40};

        TabulatedFunction a = new ArrayTabulatedFunction(x, y1);
        TabulatedFunction b = new LinkedListTabulatedFunction(x, y2);

        TabulatedFunctionOperationService svc =
                new TabulatedFunctionOperationService(new LinkedListTabulatedFunctionFactory());

        TabulatedFunction c = svc.sum(a, b);

        assertTrue(c instanceof LinkedListTabulatedFunction);
        assertEquals(4, c.getCount());
        assertEquals(11, c.getY(0), 1e-12);
        assertEquals(21, c.getY(1), 1e-12);
        assertEquals(32, c.getY(2), 1e-12);
        assertEquals(43, c.getY(3), 1e-12);
    }

    @Test
    void subtract_linkedListMinusArray_returnsLinkedListByConfiguredFactory() {
        double[] x = {0, 10};
        double[] y1 = {5, 7};
        double[] y2 = {2, 4};

        TabulatedFunction a = new LinkedListTabulatedFunction(x, y1);
        TabulatedFunction b = new ArrayTabulatedFunction(x, y2);

        TabulatedFunctionOperationService svc =
                new TabulatedFunctionOperationService(new LinkedListTabulatedFunctionFactory());

        TabulatedFunction c = svc.subtract(a, b);

        assertTrue(c instanceof LinkedListTabulatedFunction);
        assertEquals(2, c.getCount());
        assertEquals(3, c.getY(0), 1e-12); // 5-2
        assertEquals(3, c.getY(1), 1e-12); // 7-4
    }

    @Test
    void multiplication_arrayPlusArray_returnsArrayByDefaultFactory() {
        double[] x = {0, 1, 2};
        double[] y1 = {0, 1, 4};
        double[] y2 = {1, 1, 1};

        TabulatedFunction a = new ArrayTabulatedFunction(x, y1);
        TabulatedFunction b = new ArrayTabulatedFunction(x, y2);

        TabulatedFunctionOperationService svc = new TabulatedFunctionOperationService(); // Array фабрика по умолчанию
        TabulatedFunction c = svc.multiplication(a, b);

        assertTrue(c instanceof ArrayTabulatedFunction);
        assertEquals(3, c.getCount());
        assertEquals(0, c.getX(0));
        assertEquals(2, c.getX(2));
        assertEquals(0, c.getY(0), 1e-12);
        assertEquals(1, c.getY(1), 1e-12);
        assertEquals(4, c.getY(2), 1e-12);
    }

    @Test
    void division_mixedTypes_returnsArrayByDefaultFactory() {
        double[] x = {-1, 0, 1};
        double[] y1 = {1, 0, 1};
        double[] y2 = {2, 2, 2};

        TabulatedFunction a = new LinkedListTabulatedFunction(x, y1);
        TabulatedFunction b = new ArrayTabulatedFunction(x, y2);

        TabulatedFunctionOperationService svc = new TabulatedFunctionOperationService(); // Array фабрика
        TabulatedFunction c = svc.division(a, b);

        assertTrue(c instanceof ArrayTabulatedFunction);
        assertEquals(-1, c.getX(0), 1e-12);
        assertEquals(1, c.getX(2), 1e-12);
        assertEquals(0.5, c.getY(0), 1e-12); // 1 / 2
        assertEquals(0, c.getY(1), 1e-12); // 0 / 2
        assertEquals(0.5, c.getY(2), 1e-12); // 1 / 2
    }

    @Test
    void multiplication_arrayPlusLinkedList_returnsLinkedListByConfiguredFactory() {
        double[] x = {2, 3, 5, 8};
        double[] y1 = {1, 1, 2, 3};
        double[] y2 = {10, 20, 30, 40};

        TabulatedFunction a = new ArrayTabulatedFunction(x, y1);
        TabulatedFunction b = new LinkedListTabulatedFunction(x, y2);

        TabulatedFunctionOperationService svc =
                new TabulatedFunctionOperationService(new LinkedListTabulatedFunctionFactory());

        TabulatedFunction c = svc.multiplication(a, b);

        assertTrue(c instanceof LinkedListTabulatedFunction);
        assertEquals(4, c.getCount());
        assertEquals(10, c.getY(0), 1e-12);
        assertEquals(20, c.getY(1), 1e-12);
        assertEquals(60, c.getY(2), 1e-12);
        assertEquals(120, c.getY(3), 1e-12);
    }

    @Test
    void division_linkedListMinusArray_returnsLinkedListByConfiguredFactory() {
        double[] x = {0, 10};
        double[] y1 = {5, 7};
        double[] y2 = {2, 4};

        TabulatedFunction a = new LinkedListTabulatedFunction(x, y1);
        TabulatedFunction b = new ArrayTabulatedFunction(x, y2);

        TabulatedFunctionOperationService svc =
                new TabulatedFunctionOperationService(new LinkedListTabulatedFunctionFactory());

        TabulatedFunction c = svc.division(a, b);

        assertTrue(c instanceof LinkedListTabulatedFunction);
        assertEquals(2, c.getCount());
        assertEquals(2.5, c.getY(0), 1e-12); // 5 / 2
        assertEquals(1.75, c.getY(1), 1e-12); // 7 / 4
    }

    @Test
    void throws_whenDifferentSizes() {
        double[] x1 = {0, 1, 2};
        double[] y1 = {0, 1, 2};
        double[] x2 = {0, 1};      // другой размер
        double[] y2 = {10, 20};

        TabulatedFunction a = new ArrayTabulatedFunction(x1, y1);
        TabulatedFunction b = new LinkedListTabulatedFunction(x2, y2);

        TabulatedFunctionOperationService svc = new TabulatedFunctionOperationService();

        assertThrows(InconsistentFunctionsException.class, () -> svc.sum(a, b));
        assertThrows(InconsistentFunctionsException.class, () -> svc.subtract(a, b));
    }

    @Test
    void throws_whenDifferentXValues_sameSize() {
        double[] x1 = {0, 1, 2};
        double[] y1 = {0, 1, 4};
        double[] x2 = {0, 1.5, 2};   // несовпадает x[1]
        double[] y2 = {10, 20, 30};

        TabulatedFunction a = new ArrayTabulatedFunction(x1, y1);
        TabulatedFunction b = new ArrayTabulatedFunction(x2, y2);

        TabulatedFunctionOperationService svc = new TabulatedFunctionOperationService();

        assertThrows(InconsistentFunctionsException.class, () -> svc.sum(a, b));
        assertThrows(InconsistentFunctionsException.class, () -> svc.subtract(a, b));
    }
}