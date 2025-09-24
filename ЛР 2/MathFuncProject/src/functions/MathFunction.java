package functions;

// Интерфейс функции одной переменной
public interface MathFunction {
    double apply(double x);

    default CompositeFunction andThen(MathFunction afterFunction) {
        return new CompositeFunction(this, afterFunction);
    }
}
