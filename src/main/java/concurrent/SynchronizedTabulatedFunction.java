package concurrent;

import functions.TabulatedFunction;
import functions.Point;
import operations.TabulatedFunctionOperationService;
import java.util.Iterator;

public class SynchronizedTabulatedFunction implements TabulatedFunction {
    private final TabulatedFunction func;

    public interface Operation<T> {
        T apply(SynchronizedTabulatedFunction sync);
    }

    public SynchronizedTabulatedFunction(TabulatedFunction func) {
        this.func = func;
    }

    public synchronized  <T> T doSynchronously(Operation<? extends T> op) {
        return op.apply(this);
    }

    @Override
    public synchronized int getCount() {
        return func.getCount();
    }

    @Override
    public synchronized double getX(int index) {
        return func.getX(index);
    }

    @Override
    public synchronized double getY(int index) {
        return func.getY(index);
    }

    @Override
    public synchronized void setY(int index, double value) {
        func.setY(index, value);
    }

    @Override
    public synchronized int indexOfX(double x) {
        return func.indexOfX(x);
    }

    @Override
    public synchronized int indexOfY(double y) {
        return func.indexOfY(y);
    }

    @Override
    public synchronized double leftBound() {
        return func.leftBound();
    }

    @Override
    public synchronized double rightBound() {
        return func.rightBound();
    }

    @Override
    public synchronized Iterator<Point> iterator() {
        Point[] points = TabulatedFunctionOperationService.asPoints(func);
        return new Iterator<Point>() {
            private int curIndex = 0;

            @Override
            public boolean hasNext() {
                return curIndex < points.length;
            }

            @Override
            public Point next() {
                return points[curIndex++];
            }
        };
    }

    @Override
    public synchronized double apply(double x) {
        return func.apply(x);
    }
}
