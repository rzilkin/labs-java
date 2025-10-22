package functions;

import functions.DefiniteIntegral;
import functions.MathFunction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefiniteIntegralTest {

    // Допустимая дельта для сравнения численных результатов
    private static final double DELTA = 1e-4;

    @Test
    void testConstantFunctionPositiveInterval() {
        // f(x) = 5, a = 2, integral from 2 to 3 = 5 * (3-2) = 5
        MathFunction constFive = x -> 5.0;
        DefiniteIntegral integrator = new DefiniteIntegral(constFive, 2.0);
        double result = integrator.apply(3.0);
        assertEquals(5.0, result, 1e-6);
    }

    @Test
    void testSquareFunctionAnalyticalComparison() {
        // f(x) = x^2, a = 1, integral from 1 to 2 = (2^3 - 1^3)/3 = 7/3
        MathFunction square = x -> x * x;
        DefiniteIntegral integrator = new DefiniteIntegral(square, 1.0);
        double expected = (Math.pow(2.0, 3) - Math.pow(1.0, 3)) / 3.0; // 7/3
        double result = integrator.apply(2.0);
        assertEquals(expected, result, DELTA);
    }

    @Test
    void testZeroWidthInterval() {
        // a == x => integral = 0
        MathFunction any = x -> x * x + 1;
        DefiniteIntegral integrator = new DefiniteIntegral(any, 5.0);
        assertEquals(0.0, integrator.apply(5.0), 1e-12);
    }

    @Test
    void testReversedIntervalProducesNegative() {
        // f(x) = 5, a = 2, x = 1 => integral should be -5
        MathFunction constOne = x -> 5.0;
        DefiniteIntegral integrator = new DefiniteIntegral(constOne, 2.0);
        double result = integrator.apply(1.0); // x < a
        assertEquals(-5.0, result, 1e-6);
    }

    @Test
    void testFractionalRemainderExactForConstant() {
        // Проверим случай, когда (x-a) не кратно INCREMENT:
        // a = 0, x = 0.00015, f(x) = 1 => integral = 0.00015
        MathFunction constOne = x -> 1.0;
        DefiniteIntegral integrator = new DefiniteIntegral(constOne, 0.0);
        double result = integrator.apply(0.00015); // 1.5e-4
        assertEquals(0.00015, result, 1e-12);
    }

    @Test
    void testNaNLowerBoundProducesNaN() {
        // Если нижний предел NaN, результат должен быть NaN
        MathFunction any = x -> x;
        DefiniteIntegral integrator = new DefiniteIntegral(any, Double.NaN);
        double result = integrator.apply(1.0);
        assertTrue(Double.isNaN(result));
    }

    @Test
    void testFunctionReturningNaNProducesNaN() {
        // Если подынтегральная функция возвращает NaN на узлах, итог должен быть NaN
        MathFunction nanFunc = x -> Double.NaN;
        DefiniteIntegral integrator = new DefiniteIntegral(nanFunc, 0.0);
        double result = integrator.apply(1.0);
        assertTrue(Double.isNaN(result));
    }

    @Test
    void testFunctionReturningInfinityProducesInfiniteIntegral() {
        // Если функция возвращает +Infinity в любой точке, интеграл должен быть Infinite (+inf)
        MathFunction infFunc = x -> Double.POSITIVE_INFINITY;
        DefiniteIntegral integrator = new DefiniteIntegral(infFunc, 0.0);
        double result = integrator.apply(1.0);
        assertTrue(Double.isInfinite(result) && result > 0);
    }

    @Test
    void testPrecisionOnUnitIntervalForSquare() {
        // Проверяем точность для ∫_0^1 x^2 dx = 1/3.
        MathFunction square = x -> x * x;
        DefiniteIntegral integrator = new DefiniteIntegral(square, 0.0);
        double result = integrator.apply(1.0);
        double exact = 1.0 / 3.0;
        // ожидание: точность порядка INCREMENT, ставим допустимую дельту 1e-4
        assertEquals(exact, result, DELTA);
    }
}