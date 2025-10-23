package concurrent;

import functions.TabulatedFunction;
import functions.Point;
import operations.TabulatedFunctionOperationService;
import java.util.Iterator;

public class SynchronizedTabulatedFunction implements TabulatedFunction {
    private final TabulatedFunction func;
    private final Object mutex;

    public SynchronizedTabulatedFunction(TabulatedFunction func) {
        this.func = func;
        this.mutex = this;
    }

    @Override
    public int getCount() {
        synchronized (mutex) {
            return func.getCount();
        }
    }

    @Override
    public double getX(int index) {
        synchronized (mutex) {
            return func.getX(index);
        }
    }

    @Override
    public double getY(int index) {
        synchronized (mutex) {
            return func.getY(index);
        }
    }

    @Override
    public void setY(int index, double value) {
        synchronized (mutex) {
            func.setY(index, value);
        }
    }

    @Override
    public int indexOfX(double x) {
        synchronized (mutex) {
            return func.indexOfX(x);
        }
    }

    @Override
    public int indexOfY(double y) {
        synchronized (mutex) {
            return func.indexOfY(y);
        }
    }

    @Override
    public double leftBound() {
        synchronized (mutex) {
            return func.leftBound();
        }
    }

    @Override
    public double rightBound() {
        synchronized (mutex) {
            return func.rightBound();
        }
    }

    @Override
    public Iterator<Point> iterator() {
        synchronized (mutex) {
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
    }

    @Override
    public double apply(double x) {
        synchronized (mutex) {
            return func.apply(x);
        }
    }
}
