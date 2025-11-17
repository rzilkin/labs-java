package mathproj.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//класс, реализующий интерполятор
public class LinInterpolator implements Interpolator {
    private static final Logger log = LoggerFactory.getLogger(LinInterpolator.class);

    @Override
    public double interpolate(double x, double l_x, double r_x, double l_y, double r_y) {
        double res = calculateLinearValue(x, l_x, r_x, l_y, r_y);
        log.debug("Интерполяция: x = {} в диапазоне x[{}, {}] → y[{}, {}], результат = {}",
                x, l_x, r_x, l_y, r_y, res);
        return res;
    }

    @Override
    public double extrapolate(double x, double l_x, double r_x, double l_y, double r_y) {
        double res = calculateLinearValue(x, l_x, r_x, l_y, r_y);
        log.debug("Экстраполяция: x = {} за пределами x[{}, {}] → y[{}, {}], результат = {}",
                x, l_x, r_x, l_y, r_y, res);
        return res;
    }

    private double calculateLinearValue(double x, double l_x, double r_x, double l_y, double r_y) {
        return l_y + (r_y - l_y) * (x - l_x) / (r_x - l_x);
    }
}
