package functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Класс доп. функции в виде сплайнов, который реализует MathFunction
public class LinSplines implements MathFunction{
    private static final Logger log = LoggerFactory.getLogger(LinSplines.class);

    private double[] xCoord = {};       //массив координат по x
    private double[] yCoord = {};       //массив координат по y
    private final Interpolator interpolator = new LinInterpolator();    //сам интерполятор

    public LinSplines(double[] xCoord, double[] yCoord) {       //конструктор сплайнов
        log.debug("Создание линейных сплайнов длиной: {}",
                xCoord.length);
        this.xCoord = xCoord;
        this.yCoord = yCoord;
    }

    @Override
    public double apply(double x) {     //метод, определяющий y между двумя x при помощи интерполяции и экстраполяции
        if (xCoord == null || yCoord == null || xCoord.length < 2) {        //если массив не инициализирован или только один элемент, то y не определён и вернётся 0
            log.warn("Массивы имеют нулевую длину или их размер меньше 2");
            return 0;
        }
        if (x <= xCoord[0]) {   //если x меньше самого первого значения, то вызываем левую экстраполяцию
            log.debug("Левая экстраполяция для x = {}", x);
            return interpolator.extrapolate(x, xCoord[0], xCoord[1], yCoord[0], yCoord[1]);
        }

        if (x >= xCoord[xCoord.length - 1]) {       //если x больше самого последнего значения, то вызываем правую экстраполяцию
            int last = xCoord.length - 1;       //для удобства заводим новую переменную
            log.debug("Правая экстраполяция для x = {}", x);
            return interpolator.extrapolate(x, xCoord[last - 1], xCoord[last], yCoord[last - 1], yCoord[last]);
        }

        for(int i = 0; i < xCoord.length - 1; ++i) {        //если значение между двумя x, то используем интерполяцию
            if (x >= xCoord[i] && x <= xCoord[i+1]) {       //находим между какими x расположено значение
                log.debug("Интерполяция для x = {}", x);
                return interpolator.interpolate(x, xCoord[i], xCoord[i+1], yCoord[i], yCoord[i+1]);
            }
        }

        return yCoord[yCoord.length - 1];       //иначе возвращаем последний y
    }

}
