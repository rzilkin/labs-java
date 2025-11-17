package mathproj.functions;

import mathproj.functions.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ComposFuncWithTabulatedTest {
    private static final double EPS = 1e-6;

    @Test
    void testComposWithLinkedListTabulatedFunc() {
        double[] xValues = {3.0, 7.0, 13.0, 24.0, 25.0};
        double[] yValues = {7.0, 8.0, 12.0, 16.0, 25.0};
        LinkedListTabulatedFunction linkedListFunc = new LinkedListTabulatedFunction(xValues, yValues);

        MathFunction sqrFunction = new SqrFunction();
        MathFunction constantFunction = new ConstantFunction(2.0);

        MathFunction compos1 = new CompositeFunction(linkedListFunc, sqrFunction);
        MathFunction compos2 = new CompositeFunction(constantFunction, linkedListFunc);

        assertEquals(49.0, compos1.apply(3.0), 1e-6);
        assertEquals(64.0, compos1.apply(7.0), 1e-6);

        // Тестируем composite2: linkedListFunc(constant(x))
        assertEquals(6.75, compos2.apply(10.0), 1e-6);
        assertEquals(6.75, compos2.apply(-5.0), 1e-6);
    }

    // Проверяем композицию array -> sqr и constant -> array (constant сначала)
    @Test
    void testComposWithArrayTabulatedFunc() {
        double[] xValues = {3.0, 7.0, 13.0, 24.0, 25.0};
        double[] yValues = {7.0, 8.0, 12.0, 16.0, 25.0};
        ArrayTabulatedFunction arrayFunc = new ArrayTabulatedFunction(xValues, yValues);

        MathFunction sqrFunction = new SqrFunction();
        MathFunction constantFunction = new ConstantFunction(2.0);

        // arrayFunc -> sqr: sqr(arrayFunc(x))
        MathFunction compos1 = new CompositeFunction(arrayFunc, sqrFunction);
        assertEquals(49.0, compos1.apply(3.0), EPS);   // arrayFunc(3)=7 -> 7^2 = 49
        assertEquals(64.0, compos1.apply(7.0), EPS);   // arrayFunc(7)=8 -> 8^2 = 64

        // constant -> array: array(constant(x)) где constant(x)=2 -> array(2) экстраполяция слева
        MathFunction compos2 = new CompositeFunction(constantFunction, arrayFunc);
        assertEquals(6.75, compos2.apply(10.0), EPS);
        assertEquals(6.75, compos2.apply(-5.0), EPS);
    }

    // Комбинация двух табулированных функций: arrayA -> linkedB и linkedB -> arrayA.
    @Test
    void testArrayToLinkedAndLinkedToArrayCompositions() {
        // arrayA: xA: {1,2} -> yA: {2,3}
        double[] xA = {1.0, 2.0};
        double[] yA = {2.0, 3.0};
        ArrayTabulatedFunction arrayA = new ArrayTabulatedFunction(xA, yA);

        // linkedB: xB: {2,3} -> yB: {20,30}
        double[] xB = {2.0, 3.0};
        double[] yB = {20.0, 30.0};
        LinkedListTabulatedFunction linkedB = new LinkedListTabulatedFunction(xB, yB);

        // Composite: arrayA then linkedB => linkedB(arrayA(x))
        MathFunction arr_then_link = new CompositeFunction(arrayA, linkedB);
        // arrayA(1.0) = 2.0 -> linkedB(2.0) = 20.0
        assertEquals(20.0, arr_then_link.apply(1.0), EPS);
        // arrayA(2.0) = 3.0 -> linkedB(3.0) = 30.0
        assertEquals(30.0, arr_then_link.apply(2.0), EPS);

        // Composite: linkedB then arrayA => arrayA(linkedB(x))
        MathFunction link_then_arr = new CompositeFunction(linkedB, arrayA);
        // linkedB(2.0) = 20.0 -> arrayA(20.0) -> экстраполяция вправо:
        // slope of arrayA = (3-2)/(2-1) = 1 -> arrayA(20) = 3 + (20-2)*1 = 21
        assertEquals(21.0, link_then_arr.apply(2.0), EPS);

        // linkedB(2.5) = interpolate between 20 and 30 => 25.0
        // arrayA(25.0) extrapolate right -> 3 + (25-2)*1 = 26.0
        double intermediate = linkedB.apply(2.5);
        assertEquals(25.0, intermediate, EPS);
        assertEquals(26.0, link_then_arr.apply(2.5), EPS);
    }

    // Проверяем поведение цепочек: (array -> linked) -> sqr  и самокомпозиции с табулированной функцией
    @Test
    void testChainCompositionAndSelfCompositionWithTabulated() {
        double[] xs = {0.0, 1.0, 2.0};
        double[] ys = {0.0, 10.0, 20.0}; // array: linear slope = 10 per x
        ArrayTabulatedFunction arr = new ArrayTabulatedFunction(xs, ys);

        double[] xs2 = {0.0, 10.0};
        double[] ys2 = {0.0, 100.0}; // linked: value at 10 is 100 (slope = 10)
        LinkedListTabulatedFunction linked = new LinkedListTabulatedFunction(xs2, ys2);

        // chain: arr.andThen(linked).andThen(new SqrFunction()) -> sqr(linked(arr(x)))
        MathFunction chain = arr.andThen(linked).andThen(new SqrFunction());

        // arr(1.0) = 10.0 -> linked(10.0) = 100.0 -> sqr = 10000
        assertEquals(10000.0, chain.apply(1.0), EPS);

        // Self-composition: arr.andThen(arr) => arr(arr(x))
        MathFunction self = arr.andThen(arr);
        // arr(1.0)=10 -> arr(10) extrapolate right: slope=10 -> arr(10)=20 + (10-2)*10 = 100
        // wait: arr right bound at x=2 -> y=20 slope between 1->2 is 10 => arr(10)=20 + (10-2)*10 = 100
        assertEquals(100.0, self.apply(1.0), EPS);
    }

    /* Проверяем композицию с ConstantFunction в обоих направлениях:
     * - constant -> array  (array(constant(x)))
     * - array -> constant  (constant(array(x)) -> should return constant)
     */
    @Test
    void testConstantCompositions() {
        double[] x = {0.0, 5.0};
        double[] y = {0.0, 50.0};
        ArrayTabulatedFunction a = new ArrayTabulatedFunction(x, y);

        MathFunction c5 = new ConstantFunction(5.0);

        // constant -> array: array(5.0) should be 50.0 (node)
        MathFunction compA = new CompositeFunction(c5, a);
        assertEquals(50.0, compA.apply(123.0), EPS); // constant gives 5 -> a(5)=50

        // array -> constant: constant ignores input, so result always 5.0
        MathFunction compB = new CompositeFunction(a, c5);
        assertEquals(5.0, compB.apply(0.0), EPS);
        assertEquals(5.0, compB.apply(3.14), EPS);
    }
}
