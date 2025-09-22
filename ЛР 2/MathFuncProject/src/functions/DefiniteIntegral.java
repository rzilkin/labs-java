package functions;
/* Утилитный класс для численного вычисления опредённого интеграла
* методом трапеций с фиксированным шагом.
* Метод calc(double a, double b, MathFunction function) вычисляет приближённое значение
* интеграла ∫[a,b] f(x) dx
*/


public class DefiniteIntegral implements MathFunction {
    public static final double INCREMENT = 1E-4;
    private double a;
    private double b;
    private final MathFunction function;

    public DefiniteIntegral(MathFunction function) {
        this.function = function;
    }

    public void setLower(double a) {
        this.a = a;
    }

    public void setUpper(double b) {
        this.b = b;
    }

    private double compute(){
        // Обработка частных случаев
        if (Double.isNaN(a) || Double.isNaN(b)) return Double.NaN;
        if (a == b) return 0.0;

        double area = 0;
        double modifier = 1.0;

        // Пределы в обратном порядке - меняем пределы местами и знак
        if (a > b){
            double tempA = a;
            a = b;
            b = tempA;
            modifier = -1.0;
        }

        // Вычислим количество полных шагов (нулевая часть нужна для остатка ниже)
        int nFullSteps = (int) ((b - a) / INCREMENT);

        // Основной цикл: проходим по узлам с шагом INCREMENT и складываем площади трапеций.
        for(double i = a + INCREMENT; i < b; i += INCREMENT ){
            double dFromA = i - a; // смещение текущего правого узла от начала
            double left = function.apply(a + dFromA - INCREMENT);
            double right = function.apply(a + dFromA);
            area += (INCREMENT / 2.0) * (left + right);
        }

        // Обработка остаточного (короткого) интервала, если (b-a) не кратно INCREMENT
        double remainder = (b - a)- nFullSteps * INCREMENT;
        if (remainder > 1e-12){ // Защита от небольших погрешностей

            // Площадь последнецй трапеции
            area += 0.5 * (function.apply(a + nFullSteps * INCREMENT) + function.apply(b)) * remainder;
        }

        return area * modifier;
    }

    @Override
    public double apply(double x) {
        return compute();
    }
}
