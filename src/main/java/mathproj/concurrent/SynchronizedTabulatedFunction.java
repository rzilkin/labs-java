package mathproj.concurrent;

import mathproj.functions.TabulatedFunction;
import mathproj.functions.Point;
import mathproj.operations.TabulatedFunctionOperationService;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SynchronizedTabulatedFunction implements TabulatedFunction {
    private static final Logger log = LoggerFactory.getLogger(SynchronizedTabulatedFunction.class);

    private final TabulatedFunction func;

    public interface Operation<T> {
        T apply(SynchronizedTabulatedFunction sync);
    }

    public SynchronizedTabulatedFunction(TabulatedFunction func) {
        log.debug("Создание SynchronizedTabulatedFunction для: {}", func.getClass().getSimpleName());
        this.func = func;
    }

    public synchronized  <T> T doSynchronously(Operation<? extends T> op) {
        log.debug("Выполнение синхронной операции в потоке: {}", Thread.currentThread().getName());
        T res = op.apply(this);
        log.debug("Синхронная операция завершена");
        return res;
    }

    @Override
    public synchronized int getCount() {
        int count = func.getCount();
        log.trace("Получение количества точек: {}", count);
        return count;
    }

    @Override
    public synchronized double getX(int index) {
        log.trace("Получение x по индексу: {}", index);
        return func.getX(index);
    }

    @Override
    public synchronized double getY(int index) {
        log.trace("Получение y по индексу: {}", index);
        return func.getY(index);
    }

    @Override
    public synchronized void setY(int index, double value) {
        log.debug("Установка y[{}] = {}", index, value);
        func.setY(index, value);
    }

    @Override
    public synchronized int indexOfX(double x) {
        log.debug("Поиск индекса по x = {}", x);
        return func.indexOfX(x);
    }

    @Override
    public synchronized int indexOfY(double y) {
        log.debug("Поиск индекса по y = {}", y);
        return func.indexOfY(y);
    }

    @Override
    public synchronized double leftBound() {
        double leftBound = func.leftBound();
        log.trace("Левая граница: {}", leftBound);
        return leftBound;
    }

    @Override
    public synchronized double rightBound() {
        double rightBound = func.rightBound();
        log.trace("Правая граница: {}", rightBound);
        return rightBound;
    }

    @Override
    public synchronized Iterator<Point> iterator() {
        log.debug("Создание итератора для синхронизированной функции");
        Point[] points = TabulatedFunctionOperationService.asPoints(func);
        log.trace("Создан массив из {} точек для итератора", points.length);
        return new Iterator<Point>() {
            private int curIndex = 0;

            @Override
            public boolean hasNext() {
                boolean hasNext = curIndex < points.length;
                log.trace("Проверка hasNext: {}", hasNext);
                return hasNext;
            }

            @Override
            public Point next() {
                Point point = points[curIndex++];
                log.trace("Итератор вернул точку: {}", point);
                return point;
            }
        };
    }

    @Override
    public synchronized double apply(double x) {
        double res = func.apply(x);
        log.trace("Результат применения функции: {}, для x = {}", res, x);
        return res;
    }
}
