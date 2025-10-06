package operations;

import functions.Point;
import functions.TabulatedFunction;

public class TabulatedFunctionOperationService {
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
}
