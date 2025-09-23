package functions;

public interface Interpolator {
    double interpolate(double x, double l_x, double r_x, double l_y, double r_y);
    double extrapolate(double x, double l_x, double r_x, double l_y, double r_y);
}
