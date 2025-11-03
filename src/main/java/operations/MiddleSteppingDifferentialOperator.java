package operations;

import functions.MathFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiddleSteppingDifferentialOperator extends SteppingDifferentialOperator {
    private static final Logger log = LoggerFactory.getLogger(MiddleSteppingDifferentialOperator.class);

    public MiddleSteppingDifferentialOperator(double step) {
        super(step);
        log.debug("Создание среднего разностного оператора с шагом = {}", step);
    }

    @Override
    public MathFunction derive(MathFunction function) {
        log.debug("Вычисление производной для функции {} с шагом {}", function.getClass().getSimpleName(), step);

        return new MathFunction() {
            @Override
            public double apply(double x) {
                double funcXPlusStep = function.apply(x + step);
                double funcXMinusStep = function.apply(x - step);
                double derivative = (funcXPlusStep - funcXMinusStep) / (2 * step);
                log.debug("Средняя производная в точке x = {}: f({}) = {}, f({}) = {}, производная = {}",
                        x, x + step, funcXPlusStep, x - step, funcXMinusStep, derivative);
                return derivative;
            }
        };
    }
}