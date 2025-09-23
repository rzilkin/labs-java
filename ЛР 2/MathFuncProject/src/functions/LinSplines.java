package functions;

public class LinSplines implements MathFunction{

    private double[] xCoord = {};
    private double[] yCoord = {};
    private final Interpolator interpolator = new LinInterpolator();

    public LinSplines(double[] xCoord, double[] yCoord) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
    }

    @Override
    public double apply(double x) {
        if (xCoord == null || yCoord == null || xCoord.length < 2) {
            return 0;
        }
        if (x <= xCoord[0]) {
            return interpolator.extrapolate(x, xCoord[0], xCoord[1], yCoord[0], yCoord[1]);
        }

        if (x >= xCoord[xCoord.length - 1]) {
            int last = xCoord.length - 1;
            return interpolator.extrapolate(x, xCoord[last - 1], xCoord[last], yCoord[last - 1], yCoord[last]);
        }

        for(int i = 0; i < xCoord.length - 1; ++i) {
            if (x >= xCoord[i] && x <= xCoord[i+1]) {
                return interpolator.interpolate(x, xCoord[i], xCoord[i+1], yCoord[i], yCoord[i+1]);
            }
        }

        return yCoord[yCoord.length - 1];
    }

}
