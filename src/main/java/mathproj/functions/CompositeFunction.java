package mathproj.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//класс сложной функции, реализующий MathFunction
public class CompositeFunction implements MathFunction {
    private static final Logger log = LoggerFactory.getLogger(CompositeFunction.class);

    //первая и вторая функции
    private MathFunction firstFunction;
    private MathFunction secondFunction;
    //конструктор
    public CompositeFunction(MathFunction firstFunction, MathFunction secondFunction) {
        log.debug("Создание композитной функции: {} andThen {}", firstFunction.getClass().getSimpleName(),
                secondFunction.getClass().getSimpleName());

        this.firstFunction = firstFunction;
        this.secondFunction = secondFunction;
    }

    @Override
    public double apply(double x) {
        log.debug("Вычисление композитной функции для x = {}", x);
        double res = firstFunction.apply(x);    //первая функция действует на x
        log.debug("Промежуточный результат действия первой функции: {}", res);
        double finalRes = secondFunction.apply(res);
        log.debug("Итоговый результат композитной функции: {}", finalRes);
        return finalRes;       //вторая функция действует на первую
    }
}
