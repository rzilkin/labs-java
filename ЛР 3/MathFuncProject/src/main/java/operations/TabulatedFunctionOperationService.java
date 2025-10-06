package operations;

import functions.Point;
import functions.TabulatedFunction;
import exceptions.InconsistentFunctionsException;
import functions.factory.TabulatedFunctionFactory;
import functions.factory.ArrayTabulatedFunctionFactory;
import java.util.Objects;

public class TabulatedFunctionOperationService {

    private TabulatedFunctionFactory factory;

    public TabulatedFunctionOperationService(TabulatedFunctionFactory factory) {
        this.factory = Objects.requireNonNull(factory, "factory == null");
    }
    public TabulatedFunctionOperationService() {
        this.factory = new ArrayTabulatedFunctionFactory();
    }

    public TabulatedFunctionFactory getFactory() {
        return factory;
    }

    public void setFactory(TabulatedFunctionFactory factory) {
        this.factory = Objects.requireNonNull(factory, "factory == null");
    }

    public static Point[] asPoints(TabulatedFunction tabulatedFunction) {
        if (tabulatedFunction == null) {
            throw new NullPointerException("tabulatedFunction == null");
        }

        final int n = tabulatedFunction.getCount();
        Point[] result = new Point[n];

        int i = 0;
        for (Point p : tabulatedFunction) {   // именно for-each, как требует задание
            // создаём копию точки, чтобы массив не зависел от внутреннего контейнера
            result[i++] = new Point(p.x, p.y);
        }
        return result;
    }

    private interface BiOperation {
        double apply(double u, double v);
    }

    TabulatedFunction doOperation(TabulatedFunction a, TabulatedFunction b, BiOperation operation){
        Objects.requireNonNull(a, "a == null");
        Objects.requireNonNull(b, "b == null");
        Objects.requireNonNull(operation, "operation == null");

        int n = a.getCount();
        if (n != b.getCount()) {
            throw new InconsistentFunctionsException("Разные размеры: a=" + n + ", b=" + b.getCount());
        }

        Point[] ap = asPoints(a);
        Point[] bp = asPoints(b);

        double[] xValues = new double[n];
        double[] yValues = new double[n];

        for (int i = 0; i < n; i++) {
            xValues[i] = ap[i].x;
            if (Double.compare(xValues[i], bp[i].x) != 0) {
                throw new InconsistentFunctionsException(
                        "Другой x в индексе " + i + ": " + xValues[i] + " vs " + bp[i].x
                );
            }
            yValues[i] = operation.apply(ap[i].y, bp[i].y);
        }

        return factory.create(xValues, yValues);
    }

    public TabulatedFunction sum(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> u + v);
    }

    public TabulatedFunction subtract(TabulatedFunction a, TabulatedFunction b) {
        return doOperation(a, b, (u, v) -> u - v);
    }
}
