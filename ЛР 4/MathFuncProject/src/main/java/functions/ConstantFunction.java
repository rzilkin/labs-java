package functions;
/* Функция, всегда возвращает одно и то же число,
 * независимо от аргумента x.
 */
public class ConstantFunction implements MathFunction{
    private final double value;

    public ConstantFunction(double value){
        this.value = value;
    }

    public double getValue(){
        return value;
    }

    @Override
    public double apply(double x) {
        return value;
    }
}
