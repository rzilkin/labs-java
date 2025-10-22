package operations;

import functions.MathFunction;

public abstract class SteppingDifferentialOperator implements DifferentialOperator<MathFunction> {
    protected double step;

    public SteppingDifferentialOperator(double step){
        if(step <= 0 || Double.isNaN(step) || Double.isInfinite(step)){
            throw new IllegalArgumentException("Недопустимое значение шага: " + step);
        }
        this.step = step;
    }

    public void setStep(double step) {
        if (step <= 0 || Double.isNaN(step) || Double.isInfinite(step)) {
            throw new IllegalArgumentException("Недопустимое значение шага: " + step);
        }
        this.step = step;
    }

    public double getStep() {
        return step;
    }


}
