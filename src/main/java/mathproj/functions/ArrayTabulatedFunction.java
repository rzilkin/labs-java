package mathproj.functions;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.NoSuchElementException;
import java.util.Iterator;

import mathproj.exceptions.InterpolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Реализация TabulatedFunction на основе двух массивов xValues и yValues.
public class ArrayTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable, Serializable {
    private static final long serialVersionUID = -9143763512074794060L;

    private static final Logger logger = LoggerFactory.getLogger(ArrayTabulatedFunction.class);

    private double[] xValues;
    private double[] yValues;

    public ArrayTabulatedFunction(double[] xValues, double [] yValues){
        logger.info("Создаём табулированную функцию на массивах с количеством точек {}", xValues == null ? null : xValues.length);
        AbstractTabulatedFunction.checkLengthIsTheSame(xValues, yValues);

        if(xValues.length < 2)
            throw new IllegalArgumentException("Меньше минимальной длины");

        AbstractTabulatedFunction.checkSorted(xValues);

        this.xValues = Arrays.copyOf(xValues, xValues.length);
        this.yValues = Arrays.copyOf(yValues, yValues.length);

        setCount(this.xValues.length);
        logger.debug("Табулированная функция на массивах инициализирована с областью [{}; {}]", this.xValues[0], this.xValues[this.xValues.length - 1]);
    }

    public ArrayTabulatedFunction(MathFunction source, double xFrom, double xTo, int count){
        logger.info("Табулируем функцию {} на интервале от {} до {} с {} точками", source, xFrom, xTo, count);
        Objects.requireNonNull(source, "source не может быть нулевым");
        if (count < 2)
            throw new IllegalArgumentException("count должен быть >= 2");

        // если границы перепутаны - меняем местами
        double left = xFrom;
        double right = xTo;
        if (left > right){
            double temp = left;
            left = right;
            right = temp;
        }

        this.xValues = new double[count];
        this.yValues = new double[count];

        // Шаг: (right - left) / (count - 1)
        double step = (right - left) / (count - 1);
        for (int i = 0; i < count; i++) {
            double xi = left + i * step;
            this.xValues[i] = xi;
            this.yValues[i] = source.apply(xi);
        }// В силу арифметики с плавающей точкой гарантируем,
        // что последний элемент == right
        this.xValues[count - 1] = right;
        setCount(count);
        logger.debug("Функция табулирована на диапазоне [{}; {}] с шагом {}", left, right, step);
    }

    @Override
    public double getX(int index) {
        if (index < 0 || index >= count)
            throw new IllegalArgumentException("Неверный индекс");
        checkIndex(index);
        logger.trace("Возвращаем x[{}] = {}", index, xValues[index]);
        return xValues[index];
    }

    @Override
    public double getY(int index) {
        if (index < 0 || index >= count)
            throw new IllegalArgumentException("Неверный индекс");
        checkIndex(index);
        logger.trace("Возвращаем y[{}] = {}", index, yValues[index]);
        return yValues[index];
    }

    @Override
    public void setY(int index, double value) {
        if (index < 0 || index >= count)
            throw new IllegalArgumentException("Неверный индекс для вставки");
        checkIndex(index);
        logger.debug("Обновляем y[{}]: {} -> {}", index, yValues[index], value);
        yValues[index] = value;
    }

    @Override
    public int indexOfX(double x) {
        // Используем бинарный поиск для точного совпадения
        int idx = Arrays.binarySearch(xValues, x);
        logger.trace("Точный поиск x={} вернул индекс {}", x, idx);
        return idx >= 0 ? idx : -1;
    }

    @Override
    public int indexOfY(double y) {
        // Линейный поиск по yValues (вдруг неупорядоченны)
        for (int i = 0; i < count; i++) {
            if (Double.compare(yValues[i], y) == 0) {
                logger.trace("Точный поиск y={} вернул индекс {}", y, i);
                return i;
            }
        }
        logger.trace("Точный поиск y={} не дал результатов", y);
        return -1;
    }

    @Override
    public double leftBound() {
        logger.trace("Запрошена левая граница: {}", xValues[0]);
        return xValues[0];
    }

    @Override
    public double rightBound() {
        logger.trace("Запрошена правая граница: {}", xValues[count - 1]);
        return xValues[count - 1];
    }

    /* Возвращает индекс i такого, что xValues[i] < x < xValues[i+1], т.е. "пол" для x.
     * Возвращаемые значения:
     * - если все xValues > x => 0
     * - если все xValues < x => count
     * - иначе вернуть i (0 <= i < count-1)
     */
    @Override
    protected int floorIndexOfX(double x) {
        int n = count;
        // Если x меньше или равно первому узлу (и не равен точно — equality проверяется отдельно)
        if (x <= xValues[0]) {
            throw new IllegalArgumentException("X меньше левой границы");
        }
        // Если x больше или равно последнему узлу
        if (x >= xValues[n - 1]) {
            return n;
        }
        // Бинарный поиск для нахождения первого индекса j, где xValues[j] > x
        int lo = 0, hi = n - 1; // Инвариант: xValues[lo] <= x ? но мы обрабатываем граничные случаи выше
        while (lo + 1 < hi) {
            int mid = (lo + hi) >>> 1;
            if (xValues[mid] > x) {
                hi = mid;
            } else {
                lo = mid;
            }
        }
        logger.trace("Индекс пола для x={} равен {}", x, lo);
        return lo;
    }

    // Интерполяция внутри интервала, заданного floorIndex
    @Override
    protected double interpolate(double x, int floorIndex) {
        int n = count;

        // Защита индекса
        if (floorIndex < 0 || floorIndex >= n - 1)
            throw new InterpolationException("Некорректный floorIndex: " + floorIndex);

        double leftX = xValues[floorIndex];
        double rightX = xValues[floorIndex + 1];
        double leftY = yValues[floorIndex];
        double rightY = yValues[floorIndex + 1];

        if (x < leftX || x > rightX) {
            throw new InterpolationException("x=" + x + " вне интервала [" + leftX + ", "
                    + rightX + "] для интерполяции");
        }

        logger.trace("Выполняем линейную интерполяцию для x={} между точками ({}, {}) и ({}, {})",
                x, leftX, leftY, rightX, rightY);
        return super.interpolate(x, leftX, rightX, leftY, rightY);
    }

    // Линейная экстраполяция слева: используем первые два узла (0 и 1)
    @Override
    protected double extrapolateLeft(double x) {
        int n = count;
        double result = super.interpolate(x, xValues[0], xValues[1], yValues[0], yValues[1]);
        logger.debug("Левая экстраполяция для x={} дала результат {}", x, result);
        return result;
    }

    // Линейная экстраполяция справа: используем последние два узла (n-2 и n-1)
    @Override
    protected double extrapolateRight(double x) {
        int n = count;
        double result = super.interpolate(x,
                xValues[n - 2], xValues[n - 1],
                yValues[n - 2], yValues[n - 1]);
        logger.debug("Правая экстраполяция для x={} дала результат {}", x, result);
        return result;
    }

    // Вспомогательный метод
    private void checkIndex(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index out of range: " + index);
        }
    }

    @Override
    public void insert(double x, double y) {
        int indexInArray = indexOfX(x);     //узнаём индекс
        if (indexInArray != -1) {       //если индекс существует, то устанавливаем y
            logger.info("Обновляем существующий узел при x={} новым значением y={}", x, y);
            yValues[indexInArray] = y;
            return;
        }

        int indexForInsert = 0;
        if(count == 0){
            logger.info("Вставляем первую точку ({}, {})", x, y);
            xValues = new double[] { x };
            yValues = new double[] { y };
            setCount(1);
            return;
        }else {
            if (x < xValues[0]) {       //вставка на самое первое место
                indexForInsert = 0;
            }
            else if (x > xValues[count - 1]) {      //вставка на последнее место
                indexForInsert = count;
            }
            else {
                for (int i = 0; i < count - 1; i++) {       //ищем индекс для вставки
                    if (x > xValues[i] && x < xValues[i + 1]) {
                        indexForInsert = i + 1;
                        break;
                    }
                }
            }
        }

        logger.debug("Вставляем точку ({}, {}) в позицию {}", x, y, indexForInsert);
        double[] newXValues = new double[count + 1];        //новые массивы для копирования
        double[] newYValues = new double[count + 1];
        //копируем элементы из старых массивов с начала, в новые с начала
        System.arraycopy(xValues, 0, newXValues, 0, indexForInsert);
        System.arraycopy(yValues, 0, newYValues, 0, indexForInsert);
        //добавляем значения
        newXValues[indexForInsert] = x;
        newYValues[indexForInsert] = y;
        //продолжаем копирование
        System.arraycopy(xValues, indexForInsert, newXValues, indexForInsert + 1, count - indexForInsert);
        System.arraycopy(yValues, indexForInsert, newYValues, indexForInsert + 1, count - indexForInsert);
        //обновляем ссылки на массивы
        xValues = newXValues;
        yValues = newYValues;
        setCount(count + 1);
        logger.debug("Размер таблицы после вставки: {}", getCount());
    }

    @Override
    public void remove(int index) {
        // Защита на некорректный индекс
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }

        int n = count;
        // Если всего один элемент — делаем таблицу пустой (логический размер = 0)
        if (n == 1) {
            logger.warn("Удаляем единственную точку с индексом {}; таблица станет пустой", index);
            setCount(0);
            return;
        }

        // Сдвигаем элементы после index влево на 1
        for (int i = index; i < n - 1; ++i) {
            xValues[i] = xValues[i + 1];
            yValues[i] = yValues[i + 1];
        }

        // Очистим последнюю позицию
        xValues[n - 1] = 0.0;
        yValues[n - 1] = 0.0;

        // уменьшаем логический размер
        setCount(n - 1);
        logger.info("Удалили точку с индексом {}; новый размер {}", index, getCount());
    }

    @Override
    public Iterator<Point> iterator() {
        return new Iterator<Point>() {
            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < count;
            }

            @Override
            public Point next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                logger.trace("Итератор возвращает точку с индексом {}", i);
                Point p = new Point(xValues[i], yValues[i]);
                i++;
                return p;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Итерация не возможна");
            }
        };
    }

}
