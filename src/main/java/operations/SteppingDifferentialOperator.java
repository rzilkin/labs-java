package operations;

import functions.MathFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SteppingDifferentialOperator implements DifferentialOperator<MathFunction> {
    private static final Logger log = LoggerFactory.getLogger(SteppingDifferentialOperator.class);

    protected double step;

    public SteppingDifferentialOperator(double step){
        log.debug("Создание шагового дифференциального оператора с шагом: {}", step);
        if(step <= 0 || Double.isNaN(step) || Double.isInfinite(step)){
            log.error("Некорректный шаг: {}", step);
            throw new IllegalArgumentException("Недопустимое значение шага: " + step);
        }
        this.step = step;
        log.debug("Шаг успешно установлен: {}", step);
    }

    public void setStep(double step) {
        log.debug("Попытка установки шага: {}", step);
        if (step <= 0 || Double.isNaN(step) || Double.isInfinite(step)) {
            log.error("Некорректный шаг: {}", step);
            throw new IllegalArgumentException("Недопустимое значение шага: " + step);
        }
        this.step = step;
        log.debug("Шаг успешно обновлен: {}", step);
    }

    public double getStep() {
        log.trace("Получение текущего шага: {}", step);
        return step;
    }

}
