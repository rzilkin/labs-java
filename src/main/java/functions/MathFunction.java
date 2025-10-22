package functions;

// Интерфейс функции одной переменной
public interface MathFunction {
    double apply(double x);

    /* Возвращает сложную функцию g(f(x)), где
    *  f(x) - текущая функция (this),
    *  g(x) - функция afterFunction.
    */
    default CompositeFunction andThen(MathFunction afterFunction) {
        return new CompositeFunction(this, afterFunction);
    }
}
