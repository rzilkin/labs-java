package mathproj.functions.factory;

import mathproj.functions.TabulatedFunction;
import mathproj.functions.MathFunction;

public interface TabulatedFunctionFactory {
    TabulatedFunction create(double[] xValues, double[] yValues);
    TabulatedFunction create(MathFunction f, double xFrom, double xTo, int count);
}
