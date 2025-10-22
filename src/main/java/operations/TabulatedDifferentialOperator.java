package operations;

import functions.factory.TabulatedFunctionFactory;
import functions.Point;
import functions.TabulatedFunction;
import functions.factory.ArrayTabulatedFunctionFactory;

public class TabulatedDifferentialOperator implements DifferentialOperator<TabulatedFunction> {
    private TabulatedFunctionFactory factory;
    public TabulatedDifferentialOperator(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }
    public TabulatedDifferentialOperator() {
        this.factory = new ArrayTabulatedFunctionFactory();
    }
    public TabulatedFunctionFactory getFactory() {
        return factory;
    }
    public void setFactory(TabulatedFunctionFactory factory) {
        this.factory = factory;
    }

    @Override
    public TabulatedFunction derive(TabulatedFunction func) {
        Point[] points = TabulatedFunctionOperationService.asPoints(func);


        double[] xValues = new double[points.length];
        double[] yValues = new double[points.length];

        for(int i = 0; i < xValues.length; ++i) {
            xValues[i] = points[i].x;
        }

        if(xValues.length == 1) {
            yValues[0] = 0.0;
        }
        else {
            for (int i = 0; i < points.length - 1; i++) {
                double dx = points[i + 1].x - points[i].x;
                double dy = points[i + 1].y - points[i].y;
                yValues[i] = dy / dx;
            }
            // Последняя точка получает ту же производную, что предыдущая
            yValues[points.length - 1] = yValues[points.length - 2];
        }
        return factory.create(xValues, yValues);
    }
}
