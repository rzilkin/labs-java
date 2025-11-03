package operations;

import functions.MathFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RightSteppingDifferentialOperator extends SteppingDifferentialOperator {
    private static final Logger log = LoggerFactory.getLogger(RightSteppingDifferentialOperator.class);

    public RightSteppingDifferentialOperator(double step) {
        super(step);
        log.debug("Создание правого разностного оператора с шагом: {}", step);
    }

    @Override
    public MathFunction derive(MathFunction function) {
        log.debug("Вычисление производной для функции {} с шагом {}", function.getClass().getSimpleName(), step);

        return new MathFunction() {
            @Override
            public double apply(double x) {
                double funcXPlusStep = function.apply(x + step);
                double funcX = function.apply(x);
                double derivative = (funcXPlusStep - funcX) / step;
                log.debug("Правая производная в точке x = {}: f({}) = {}, f({}) = {}, производная = {}",
                        x, x + step, funcXPlusStep, x, funcX, derivative);
                return derivative;
            }
        };
    }
}