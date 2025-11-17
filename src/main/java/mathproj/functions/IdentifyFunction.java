package mathproj.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//класс, реализующий интерфейс MathFunction, который выполняет тождественное преобразование
public class IdentifyFunction implements MathFunction {
    private static final Logger log = LoggerFactory.getLogger(IdentifyFunction.class);

    @Override
    public double apply(double x) {
        log.debug("Вызов тождественной функции для x = {}, возвращаемое значение = {}", x, x);
        return x;
    } 

}