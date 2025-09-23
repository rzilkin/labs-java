package functions;
/* Утилитный класс для численного вычисления опредённого интеграла
 * методом трапеций с фиксированным шагом.
 */


public class DefiniteIntegral implements MathFunction {
    public static final double INCREMENT = 1E-4;

    private double a;
    private final MathFunction function;

    public DefiniteIntegral(MathFunction function, double a) {
        this.function = function;
        this.a = a;
    }

    public void setLower(double a) {
        this.a = a;
    }

    private double compute(double start, double end){
        // Обработка частных случаев
        if (Double.isNaN(start) || Double.isNaN(end)) return Double.NaN;
        if (start == end) return 0.0;

        double modifier = 1.0;

        // Пределы в обратном порядке - меняем пределы местами и знак
        if (start > end){
            double temp = start;
            start = end;
            end = temp;
            modifier = -1.0;
        }
        double width = end - start;
        // Вычислим количество полных шагов (нулевая часть нужна для остатка ниже)
        int nFullSteps = (int) (width / INCREMENT);

        double area = 0.0;
        // Основной цикл: проходим по узлам с шагом INCREMENT и складываем площади трапеций.
        for (int k = 1; k <= nFullSteps; k++) {
            double xPrev = start + (k - 1) * INCREMENT;
            double xCurr = start + k * INCREMENT;
            double left = function.apply(xPrev);
            double right = function.apply(xCurr);
            area += 0.5 * (left + right) * INCREMENT;
        }

        // Обработка остаточного (короткого) интервала, если (end-start) не кратно INCREMENT
        double remainder = width- nFullSteps * INCREMENT;
        if (remainder > 1e-12) { // Защита от небольших погрешностей
            double xPrev = start + nFullSteps * INCREMENT;
            double xCurr = end;
            // Площадь последней трапеции
            area += 0.5 * (function.apply(xPrev) + function.apply(xCurr)) * remainder;
        }

        return area * modifier;
    }

    @Override
    public double apply(double x) {
        return compute(a, x);
    }
}