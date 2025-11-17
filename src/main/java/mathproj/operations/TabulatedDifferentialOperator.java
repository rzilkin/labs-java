package mathproj.operations;

import mathproj.functions.factory.TabulatedFunctionFactory;
import mathproj.functions.Point;
import mathproj.functions.TabulatedFunction;
import mathproj.functions.factory.ArrayTabulatedFunctionFactory;
import mathproj.concurrent.SynchronizedTabulatedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TabulatedDifferentialOperator implements DifferentialOperator<TabulatedFunction> {
    private static final Logger log = LoggerFactory.getLogger(TabulatedDifferentialOperator.class);

    private TabulatedFunctionFactory factory;
    public TabulatedDifferentialOperator(TabulatedFunctionFactory factory) {
        log.debug("Создание TabulatedDifferentialOperator с фабрикой: {}", factory.getClass().getSimpleName());
        this.factory = factory;
    }
    public TabulatedDifferentialOperator() {
        log.debug("Создание TabulatedDifferentialOperator с фабрикой по умолчанию");
        this.factory = new ArrayTabulatedFunctionFactory();
    }
    public TabulatedFunctionFactory getFactory() {
        log.trace("Получение фабрики: {}", factory.getClass().getSimpleName());
        return factory;
    }
    public void setFactory(TabulatedFunctionFactory factory) {
        log.debug("Установка новой фабрики: {}", factory.getClass().getSimpleName());
        this.factory = factory;
    }

    @Override
    public TabulatedFunction derive(TabulatedFunction func) {
        log.debug("Вычисление производной для табличной функции с {} точками", func.getCount());

        Point[] points = TabulatedFunctionOperationService.asPoints(func);
        log.debug("Преобразование функции в массив из {} точек", points.length);

        double[] xValues = new double[points.length];
        double[] yValues = new double[points.length];

        for(int i = 0; i < xValues.length; ++i) {
            xValues[i] = points[i].x;
        }

        if(xValues.length == 1) {
            log.debug("Функция состоит из одной точки, производная равна 0");
            yValues[0] = 0.0;
        }
        else {
            log.debug("Вычисление производных для {} сегментов", points.length - 1);
            for (int i = 0; i < points.length - 1; i++) {
                double dx = points[i + 1].x - points[i].x;
                double dy = points[i + 1].y - points[i].y;
                yValues[i] = dy / dx;
                log.trace("Производная в сегменте [{}, {}]: dy/dx = {}/{} = {}", points[i].x, points[i + 1].x, dy, dx, yValues[i]);
            }
            // Последняя точка получает ту же производную, что предыдущая
            yValues[points.length - 1] = yValues[points.length - 2];
            log.debug("Производная в последней точке установлена равной предыдущей: {}", yValues[points.length - 1]);
        }
        TabulatedFunction res = factory.create(xValues, yValues);
        log.debug("Создана производная функция с {} точками", res.getCount());
        return res;
    }

    public TabulatedFunction deriveSynchronously(TabulatedFunction func) {
        log.debug("Синхронное вычисление производной для функции: {}", func.getClass().getSimpleName());
        if (func instanceof SynchronizedTabulatedFunction) {
            log.debug("Функция уже синхронизирована, используем существующий объект");
            return ((SynchronizedTabulatedFunction) func).doSynchronously(this::derive);
        } else {
            log.debug("Создание синхронизированной обертки для функции");
            SynchronizedTabulatedFunction syncFunc = new SynchronizedTabulatedFunction(func);
            return syncFunc.doSynchronously(this::derive);
        }
    }
}
