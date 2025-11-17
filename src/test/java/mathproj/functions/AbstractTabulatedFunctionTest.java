package mathproj.functions;

import mathproj.functions.AbstractTabulatedFunction;
import mathproj.exceptions.ArrayIsNotSortedException;
import mathproj.exceptions.DifferentLengthOfArraysException;
import mathproj.functions.TabulatedFunction;
import mathproj.functions.factory.LinkedListTabulatedFunctionFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AbstractTabulatedFunctionTest {

    @Test
    void checkLengthIsTheSame_throwsOnDifferent() {
        double[] x = {0, 1, 2};
        double[] y = {10, 20};
        assertThrows(DifferentLengthOfArraysException.class,
                () -> AbstractTabulatedFunction.checkLengthIsTheSame(x, y));
    }

    @Test
    void checkSorted_throwsOnNotStrictlyIncreasing() {
        double[] x = {0, 1, 1}; // нестрого
        assertThrows(ArrayIsNotSortedException.class,
                () -> AbstractTabulatedFunction.checkSorted(x));
    }

    @Test
    public void testToStringForLinkedListTabulatedFunction() {
        double[] xValues = {0.0, 0.5, 1.0};
        double[] yValues = {0.0, 0.25, 1.0};

        LinkedListTabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        TabulatedFunction func = factory.create(xValues, yValues);

        String expected = "LinkedListTabulatedFunction size = 3\n[0.0; 0.0]\n[0.5; 0.25]\n[1.0; 1.0]\n";
        assertEquals(expected, func.toString());
    }

    @Test
    public void testToStringForLinkedListTabulatedFunction2() {
        double[] xValues = {0.0, 0.567};
        double[] yValues = {0.0, 0.2545};

        LinkedListTabulatedFunctionFactory factory = new LinkedListTabulatedFunctionFactory();
        TabulatedFunction func = factory.create(xValues, yValues);

        String expected = "LinkedListTabulatedFunction size = 2\n[0.0; 0.0]\n[0.567; 0.2545]\n";
        assertEquals(expected, func.toString());
    }
}
