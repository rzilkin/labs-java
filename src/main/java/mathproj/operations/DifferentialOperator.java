package mathproj.operations;

import mathproj.functions.MathFunction;

public interface DifferentialOperator<T extends MathFunction> {
    T derive (T function);
}
