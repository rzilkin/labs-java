package mathproj.operations;

import mathproj.functions.MathFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeftSteppingDifferentialOperator extends SteppingDifferentialOperator {
    private static final Logger log = LoggerFactory.getLogger(LeftSteppingDifferentialOperator.class);

    public LeftSteppingDifferentialOperator(double step) {
        super(step);
        log.debug("Создание левого разностного оператора с шагом: {}", step);
    }

    @Override
    public MathFunction derive(MathFunction function) {
        log.debug("Вычисление производной для функции {} с шагом {}", function.getClass().getSimpleName(), step);
        return new MathFunction() {
            @Override
            public double apply(double x) {
                double funcX = function.apply(x);
                double funcXMinusStep = function.apply(x - step);
                double derivative = (funcX - funcXMinusStep) / step;
                log.debug("Левая производная в точке x = {}: f({}) = {}, f({}) = {}, производная = {}",
                        x, x, funcX, x - step, funcXMinusStep, derivative);
                return derivative;
            }
        };
    }
}