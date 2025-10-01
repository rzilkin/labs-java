package functions;

//интерфейс интерполятора, созданного для применения к реализации линейных сплайнов
public interface Interpolator {
    double interpolate(double x, double l_x, double r_x, double l_y, double r_y);       //метод интерполяции
    double extrapolate(double x, double l_x, double r_x, double l_y, double r_y);       //метод экстраполяции
}
