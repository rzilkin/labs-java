package functions;

import functions.AbstractTabulatedFunction;
import exceptions.ArrayIsNotSortedException;
import exceptions.DifferentLengthOfArraysException;
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
}
