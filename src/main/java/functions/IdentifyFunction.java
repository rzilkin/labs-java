package functions;

//класс, реализующий интерфейс MathFunction, который выполняет тождественное преобразование
public class IdentifyFunction implements MathFunction {
    @Override
    public double apply(double x) {
        return x;
    } 

}