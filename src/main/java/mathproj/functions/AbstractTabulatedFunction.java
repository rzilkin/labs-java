package mathproj.functions;

import mathproj.exceptions.ArrayIsNotSortedException;
import mathproj.exceptions.DifferentLengthOfArraysException;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Базовый абстрактный класс для табулированных функций.
public abstract class AbstractTabulatedFunction implements TabulatedFunction, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTabulatedFunction.class);

    private static final long serialVersionUID = -499199225426405919L;
    // Количество точек в таблице.
    protected int count = 0;

    // Установить количество точек (вызывать из подкласса при инициализации).
    protected void setCount(int count) {
        logger.debug("Setting tabulated function size to {}", count);
        if (count < 0) {
            logger.error("Attempted to set negative count: {}", count);
            throw new IllegalArgumentException("count должен быть положительным");
        }
        this.count = count;
    }

    @Override
    public int getCount() {
        logger.trace("Getting tabulated function size: {}", count);
        return count;
    }

    //Возвращает индекс максимального x_i, который меньше заданного x.
    protected abstract int floorIndexOfX(double x);

    // Экстраполировать значение слева от левой границы таблицы.
    protected abstract double extrapolateLeft(double x);

    // Экстраполировать значение справа от правой границы таблицы.
    protected abstract double extrapolateRight(double x);

    /* Интерполяция внутри таблицы: вычислить значение функции для x,
     * зная индекс левой точки интервала (floorIndex).
     */
    protected abstract double interpolate(double x, int floorIndex);

    /* линейная интерполяция (или экстраполяция) по двум точкам:
     * y = leftY + (x - leftX) * (rightY - leftY) / (rightX - leftX)
     */

    protected double interpolate(double x, double leftX, double rightX, double leftY, double rightY) {
        if (Double.compare(rightX, leftX) == 0) {
            // Защита от деления на ноль — возвращаем leftY (можно обсуждать альтернативы)
            return leftY;
        }
        return leftY + (x - leftX) * (rightY - leftY) / (rightX - leftX);
    }

    public static void checkLengthIsTheSame(double[] xValues, double[] yValues){
        logger.debug("Checking array lengths: x={}, y={}",
                xValues == null ? null : xValues.length,
                yValues == null ? null : yValues.length);
        if (xValues == null || yValues == null) {
            logger.error("Null arrays passed to length check: xValues={}, yValues={}", xValues, yValues);
            throw new NullPointerException("xValues и yValues не должны быть пустыми");
        }

        if(xValues.length != yValues.length) {
            logger.error("Different array lengths detected: x={}, y={}", xValues.length, yValues.length);
            throw new DifferentLengthOfArraysException("Длины массивов не совпадают!\nx=" + xValues.length + ", y=" + yValues.length);
        }
    }

    public static void checkSorted(double[] xValues){
        logger.debug("Checking array sort order for {} elements", xValues == null ? null : xValues.length);
        if (xValues == null) {
            logger.error("Null array passed to sort check");
            throw new NullPointerException("xValues не должен быть пустым");
        }

        for (int i = 1; i < xValues.length; i++) {
            if (!(xValues[i] > xValues[i - 1])) {
                logger.error("Array is not sorted at indexes {} and {}", i - 1, i);
                throw new ArrayIsNotSortedException("xValues должен строго возрастать. Нарушение на парах индексов "
                        + (i - 1) + "," + i + " (" + xValues[i - 1] + " >= " + xValues[i] + ")"
                );
            }
        }

    }
    /* Логика получения значения функции:
     * - если таблица пуста (count == 0) — бросаем исключение (нет данных);
     * - если count == 1 — всегда возвращаем единственное значение getY(0);
     * - если x < leftBound() — используем extrapolateLeft;
     * - если x > rightBound() — используем extrapolateRight;
     * - иначе, если indexOfX(x) != -1 — возвращаем getY(index);
     * - иначе вычисляем floorIndexOfX(x) и возвращаем interpolate(x, floorIndex).
     */
    @Override
    public double apply(double x) {
        logger.debug("Applying tabulated function to x={}", x);
        if (getCount() == 0) {
            logger.error("Attempt to apply function with zero points");
            throw new IllegalStateException("Табулированная функция не содержит точек");
        }
        if (getCount() == 1) {
            logger.trace("Single point function; returning y0");
            // Вырожденный случай: всегда возвращаем единственную y
            return getY(0);
        }

        // Сначала проверить границы
        double left = leftBound();
        double right = rightBound();

        if (Double.isNaN(x)) return Double.NaN;

        if (x < left) {
            logger.info("Value {} is left of domain [{};{}]; extrapolating left", x, left, right);
            return extrapolateLeft(x);
        }
        if (x > right) {
            logger.info("Value {} is right of domain [{};{}]; extrapolating right", x, left, right);
            return extrapolateRight(x);
        }

        // Если x совпадает с узлом таблицы — вернуть соответствующее y
        int idx = indexOfX(x);
        if (idx != -1) {
            logger.trace("Value {} matches node index {}", x, idx);
            return getY(idx);
        }

        // Иначе интерполируем внутри интервала
        int floorIndex = floorIndexOfX(x);
        // Защита: если floorIndex == getCount() (вне правой границы), вернём extrapolateRight
        if (floorIndex >= getCount()) {
            logger.warn("Floor index {} is out of bounds for value {}; using right extrapolation", floorIndex, x);
            return extrapolateRight(x);
        }
        // Если floorIndex == 0 и x <= leftBound(), мы уже обработали x < left выше,
        // но на всякий случай защитимся:
        if (floorIndex <= 0 && x <= left) {
            logger.warn("Floor index {} implies left extrapolation for value {}", floorIndex, x);
            return extrapolateLeft(x);
        }

        logger.trace("Interpolating value {} using floor index {}", x, floorIndex);
        return interpolate(x, floorIndex);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append(getClass().getSimpleName()).append(" size = ").append(getCount()).append("\n");

        for (Point point : this) {
            str.append("[").append(point.x).append("; ").append(point.y).append("]\n");
        }

        return str.toString();
    }

}