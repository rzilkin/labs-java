package functions;

// Базовый абстрактный класс для табулированных функций.
public abstract class AbstractTabulatedFunction implements TabulatedFunction {

    // Количество точек в таблице.
    private int count = 0;

    // Установить количество точек (вызывать из подкласса при инициализации).
    protected void setCount(int count) {
        if (count < 0) throw new IllegalArgumentException("count должен быть положительным");
        this.count = count;
    }

    @Override
    public int getCount() {
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
        if (getCount() == 0) {
            throw new IllegalStateException("Табулированная функция не содержит точек");
        }
        if (getCount() == 1) {
            // Вырожденный случай: всегда возвращаем единственную y
            return getY(0);
        }

        // Сначала проверить границы
        double left = leftBound();
        double right = rightBound();

        if (Double.isNaN(x)) return Double.NaN;

        if (x < left) {
            return extrapolateLeft(x);
        }
        if (x > right) {
            return extrapolateRight(x);
        }

        // Если x совпадает с узлом таблицы — вернуть соответствующее y
        int idx = indexOfX(x);
        if (idx != -1) {
            return getY(idx);
        }

        // Иначе интерполируем внутри интервала
        int floorIndex = floorIndexOfX(x);
        // Защита: если floorIndex == getCount() (вне правой границы), вернём extrapolateRight
        if (floorIndex >= getCount()) {
            return extrapolateRight(x);
        }
        // Если floorIndex == 0 и x <= leftBound(), мы уже обработали x < left выше,
        // но на всякий случай защитимся:
        if (floorIndex <= 0 && x <= left) {
            return extrapolateLeft(x);
        }

        return interpolate(x, floorIndex);
    }
}
