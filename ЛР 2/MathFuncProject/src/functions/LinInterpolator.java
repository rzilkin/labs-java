package functions;

public class LinInterpolator implements Interpolator {
    @Override
    public double interpolate(double x, double l_x, double r_x, double l_y, double r_y) {
        return (l_y + (r_y - l_y) * (x - l_x) / (r_x - l_x));
    }
    @Override
    public double extrapolate(double x, double l_x, double r_x, double l_y, double r_y) {
        return interpolate(x, l_x, r_x, l_y, r_y);
    }
}
