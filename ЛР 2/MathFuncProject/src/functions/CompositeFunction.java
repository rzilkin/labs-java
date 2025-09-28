package functions;

//класс сложной функции, реализующий MathFunction
public class CompositeFunction implements MathFunction {
    //первая и вторая функции
    private MathFunction firstFunction;
    private MathFunction secondFunction;
    //конструктор
    public CompositeFunction(MathFunction firstFunction, MathFunction secondFunction) {
        this.firstFunction = firstFunction;
        this.secondFunction = secondFunction;
    }

    @Override
    public double apply(double x) {
        double res = firstFunction.apply(x);    //первая функция действует на x
        return secondFunction.apply(res);       //вторая функция действует на первую
    }
}
