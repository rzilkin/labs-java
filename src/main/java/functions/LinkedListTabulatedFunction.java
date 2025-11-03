package functions;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//класс хранения данных табличной функции на основе циклического двусвязного списка, расширяющий AbstractTabulatedFunction
//и реализующий Insertable, Removable
public class LinkedListTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable, Serializable {
    private static final Logger log = LoggerFactory.getLogger(LinkedListTabulatedFunction.class);

    private static final long serialVersionUID = -55920830421344972L;

    private static class Node implements Serializable {
        private static final long serialVersionUID = 7024348912566824596L;
        private Node next;
        private Node prev;
        private double x;
        private double y;
    }

    Node head;      //голова списка

    private void addNode(double x, double y) {      //добавление узла
        log.debug("Добавление узла с координатами ({}, {})", x, y);

        Node newNode = new Node();      //создание нового узла
        //добавление значений точки
        newNode.x = x;
        newNode.y = y;

        if(head == null) {      //если нет элементов, то создаём голову списка, которая ссылается на саму себя
            log.debug("Создание головы списка");
            head = newNode;
            //создание ссылок
            head.next = head;
            head.prev = head;
        }

        else {      //если список не пуст, то переопределяем связи между узлами
            log.debug("Добавление узла в конец списка");
            Node last = head.prev;
            last.next = newNode;
            head.prev = newNode;
            newNode.next = head;
            newNode.prev = last;
        }

        setCount(count + 1);     //расширяем список
        log.debug("Количество узлов увеличено до: {}", count);
    }

    //конструктор, если данные в массивах
    public LinkedListTabulatedFunction(double[] xValues, double[] yValues) {
        log.info("Создание табличной функции из массивов: x[{}], y[{}]", xValues.length, yValues.length);
        AbstractTabulatedFunction.checkLengthIsTheSame(xValues, yValues);

        if (xValues.length < 2 || yValues.length < 2) {
            log.error("Недостаточная длина массивов: x={}, y={}", xValues.length, yValues.length);
            throw new IllegalArgumentException("Длина меньше минимальной");
        }

        AbstractTabulatedFunction.checkSorted(xValues);

        for(int i = 0; i < xValues.length; ++i) {
            addNode(xValues[i], yValues[i]);
        }
        log.debug("Табличная функция создана с {} точками", count);
    }
    //конструктор при помощи другой функции
    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        log.info("Создание табличной функции из функции {} на интервале [{}, {}] с {} точками",
                source.getClass().getSimpleName(), xFrom, xTo, count);
        if(count < 2) {
            log.error("Недостаточная количество точек: {}", count);
            throw new IllegalArgumentException("Длина меньше минимальной");
        }

        if(xFrom > xTo) {       //если xFrom > xTo, то свапаем их
            log.debug("Обмен границ интервала: {} -> {}", xFrom, xTo);
            double temp = xFrom;
            xFrom = xTo;
            xTo = temp;
        }

        if(xFrom == xTo) {      //если границы совпадают, то создаём каунтовое количество точек с функцией source
            log.debug("Границы интервала совпадают, создание {} точек с x = {}", count, xFrom);
            for (int i = 0; i < count; ++i) {
                addNode(xFrom, source.apply(xFrom));
            }
        }
        else {                  //если не совпадают, то добавляем точки при помощи равномерной дискретизации
            double step = (xTo - xFrom) / (count - 1); //задаём шаг, который будет ровно делить отрезок
            log.debug("Равномерная дискретизация с шагом: {}", step);
            for(int i = 0; i < count; ++i) {
                double x = xFrom + i * step;        //создаём каждый x подобным образом: x1 = a + b, x2 = a + 2b, и так далее
                addNode(x, source.apply(x));        //добавляем точку
            }
        }
        log.debug("Табличная функция создана с {} точками", this.count);
    }

    @Override
    public void insert(double x, double y) {        //добавление точки
        log.debug("Вставка точки ({}, {}) в табличную функцию", x, y);

        if (head == null) {         //если список пуст, просто добавляем узел
            log.debug("Список пуст, создание головы");
            addNode(x, y);
            return;
        }

        Node cur = head;    //создаём текущий узел
        int iter = 0;

        do {
            if (cur.x == x) {       //если совпал x, то просто меняем y на новый
                log.debug("Точка с x={} уже существует, обновление y с {} на {}", x, cur.y, y);
                cur.y = y;
                return;
            }

            if (x < cur.x) {        //если меньше некоторого x в списке, то вставляем слева от него
                log.debug("Вставка перед узлом с x={}", cur.x);
                insertBefore(cur, x, y);        //вызов вспомогательного метода
                if (cur == head) {
                    log.debug("Обновление головы списка");
                    head = head.prev;       //обновляем голову, если вставляем перед ней
                }
                return;
            }

            if (cur.next == head || x < cur.next.x) {       //если меньше следующего, но больше текущего, то вставляем после
                log.debug("Вставка после узла с x={}", cur.x);
                insertAfter(cur, x, y);     //вызов доп. метода
                return;
            }

            cur = cur.next;     //двигаемя дальше по списку
            iter++;
            log.trace("Переход к следующему узлу, итерация: {}", iter);
        } while (cur != head && iter < count);  //идём пока текущий указатель не ссылается на голову и количество итераций не перевалит за число элементов в списке

        log.debug("Вставка в конец списка");
        insertBefore(head, x, y);       //вставка в конец
    }

    private void insertAfter(Node node, double x, double y) {       //вставка после
        log.debug("Вставка узла ({}, {}) после узла с x={}", x, y, node.x);
        Node newNode = new Node();      //новый узел
        newNode.x = x;          //добавление значений
        newNode.y = y;

        newNode.next = node.next;       //обновление связей узлов
        newNode.prev = node;
        node.next.prev = newNode;
        node.next = newNode;

        setCount(count + 1);        //увеличиваем длину списка
        log.debug("Количество узлов увеличено до: {}", count);
    }

    private void insertBefore(Node node, double x, double y) {      //вставка до
        log.debug("Вставка узла ({}, {}) перед узлом с x={}", x, y, node.x);
        Node newNode = new Node();      //новый узел
        newNode.x = x;          //добавление значений
        newNode.y = y;

        newNode.prev = node.prev;       //обновление связей узлов
        newNode.next = node;
        node.prev.next = newNode;
        node.prev = newNode;

        setCount(count + 1);        //увеличиваем длину списка
        log.debug("Количество узлов увеличено до: {}", count);
    }

    @Override
    public double leftBound() {
        double leftBound = head.x;
        log.debug("Левая граница функции: {}", leftBound);
        return leftBound;
    }

    @Override
    public double rightBound() {
        double rightBound = head.prev.x;
        log.debug("Правая граница функции: {}", rightBound);
        return rightBound;
    }

    private Node getNode(int index) {       //вспомогательный метод получения узла по индексу
        log.debug("Получение узла по индексу: {}", index);
        if (index < 0 || index >= count) {
            log.error("Индекс находится за границами списка: {}", index);
            throw new IllegalArgumentException("Индекс неверный");
        }
        if (index < count / 2) {        //ускоряем работу получения узла, узнав в какой части списка узел
            log.debug("Индекс находится ближе к голове");
            Node cur = head;
            //по списку проходим до нашего индекса
            for (int i = 0; i < index; i++) {
                cur = cur.next;
            }
            return cur;
        }
        else {
            log.debug("Индекс находится ближе к хвосту");
            Node cur = head.prev;
            //по списку проходим до нашего индекса, но если во второй части списка
            for (int i = count - 1; i > index; --i) {
                cur = cur.prev;
            }
            return cur;
        }
    }

    @Override
    public double getX(int index)
    {
        log.debug("Получение x по индексу: {}", index);
        if (index < 0 || index >= count) {
            log.error("Индекс находится за границами списка: {}", index);
            throw new IllegalArgumentException("Индекс неверный");
        }
        double x = getNode(index).x;
        log.debug("x[{}] = {}", index, x);
        return x;
    }

    @Override
    public double getY(int index) {
        log.debug("Получение y по индексу: {}", index);
        if (index < 0 || index >= count) {
            log.error("Индекс находится за границами списка: {}", index);
            throw new IllegalArgumentException("Индекс неверный");
        }
        double y = getNode(index).y;
        log.debug("y[{}] = {}", index, y);
        return y;
    }

    @Override
    public void setY(int index, double value) {
        log.debug("Установка y[{}] = {}", index, value);
        if (index < 0 || index >= count) {
            log.error("Индекс находится за границами списка: {}", index);
            throw new IllegalArgumentException("Индекс неверный");
        }
        getNode(index).y = value;
        log.debug("y[{}] успешно установлено в {}", index, value);
    }

    @Override
    public int indexOfX(double x) {     //индекс по значению x
        log.debug("Вычисление индекс по x = {}", x);
        Node cur = head;
        for (int i = 0; i < count; i++) {
            if (cur.x == x) {
                log.debug("Найден индекс: {} для x = {}", i, x);
                return i;
            }
            cur = cur.next;
        }
        log.debug("x = {} не найден", x);
        return -1;
    }

    @Override
    public int indexOfY(double y) {     //индекс по значению y
        log.debug("Вычисление индекса по y = {}", y);
        Node cur = head;
        for (int i = 0; i < count; i++) {
            if (cur.y == y) {
                log.debug("Найден индекс: {} для y = {}", i, y);
                return i;
            }
            cur = cur.next;
        }
        log.debug("y = {} не найден", y);
        return -1;
    }

    @Override
    public int floorIndexOfX(double x) {        //находит x, удовлетворяющий: x[i] <= x < x[i+1]
        log.debug("Поиск floorIndex для x = {}", x);
        if (x < head.x) {
            log.error("x = {} меньше левой границы {}", x, head.x);
            throw new IllegalArgumentException("X меньше левой границы");
        }

        Node cur = head;
        for (int i = 0; i < count; i++) {
            if (cur.x > x) {
                int res = i == 0 ? 0 : i - 1;
                log.debug("Найден floorIndex: {} для x = {}", res, x);
                return res;
            }
            cur = cur.next;
        }
        log.debug("x = {} превышает правую границу, возвращение count = {}", x, count);
        return count;
    }

    @Override
    public double extrapolateLeft(double x) {       //левая экстраполяция
        log.debug("Левая экстраполяция для x = {}", x);
        Node first = head;
        Node second = head.next;
        double res = interpolate(x, first.x, second.x, first.y, second.y);
        log.debug("Итог левой экстраполяции: {}", res);
        return res;
    }

    @Override
    public double extrapolateRight(double x) {  //правая экстраполяция
        log.debug("Правая экстраполяция для x = {}", x);
        Node last = head.prev;
        Node secondLast = last.prev;
        double res = interpolate(x, secondLast.x, last.x, secondLast.y, last.y);
        log.debug("Итог правой экстраполяции: {}", res);
        return res;
    }

    @Override
    public double interpolate(double x, int floorIndex) {       //интерполяция
        log.debug("Интерполяция для x = {} с floorIndex = {}", x, floorIndex);
        if (floorIndex < 0 || floorIndex >= count - 1) {
            log.error("Некорректный индекс floorIndex: {}", floorIndex);
            throw new exceptions.InterpolationException("Некорректный floorIndex: " + floorIndex);
        }

        Node left = getNode(floorIndex);        //если между двумя x
        Node right = left.next;

        if (x < left.x || x > right.x) {
            log.error("x = {} вне интервала [{}, {}]", x, left.x, right.x);
            throw new exceptions.InterpolationException("x=" + x + " вне интервала [" + left.x
                    + ", " + right.x + "] для интерполяции");
        }

        double res = interpolate(x, left.x, right.x, left.y, right.y);
        log.debug("Итог интерполяции: {}", res);
        return res;
    }

    @Override
    public void remove(int index) {
        log.debug("Удаление узла с индексом: {}", index);
        // Защита на некорректный индекс
        if (index < 0 || index >= count) {
            log.error("Индекс для удаления находится за границами списка: {}", index);
            throw new IndexOutOfBoundsException("Index: " + index);
        }

        // Если единственный элемент — очистить список
        if (count == 1) {
            log.debug("Удаление единственного узла в списке");
            head = null;
            setCount(0);
            return;
        }

        // выбираем более короткий путь: от головы (вперёд) или от хвоста (назад)
        Node cur;
        int i;
        int n = count;

        if (index < n / 2) {
            log.debug("Узел для удаления ближе к голове");
            // Идём от головы вперёд
            cur = head;
            i = 0;
            while (i < index) {
                cur = cur.next;
                i++;
            }
        } else {
            log.debug("Узел для удаления ближе к хвосту");
            // Идём от хвоста назад
            cur = head.prev; // Последний элемент
            i = n - 1;
            while (i > index) {
                cur = cur.prev;
                i--;
            }
        }

        log.debug("Удаление узла с x = {}", cur.x);
        // cur — узел, который нужно удалить
        cur.prev.next = cur.next;
        cur.next.prev = cur.prev;

        // Если удаляем головной элемент — обновляем head
        if (cur == head) {
            log.debug("Удаляемый узел является головой, обновление головы");
            head = cur.next;
        }

        // Уменьшаем счётчик и синхронизируем с базой
        setCount(count - 1);
        log.debug("Количество узлов уменьшено до: {}", count);

        // Обнуляем ссылки удалённого узла
        cur.next = null;
        cur.prev = null;
    }

    @Override
    public Iterator<Point> iterator() {
        log.debug("Создание итератора для табличной функции");
        return new Iterator<Point>() {
            private Node cur = head;
            private int returned = 0;

            @Override
            public boolean hasNext() {
                boolean hasNext = returned < count;
                log.trace("Проверка hasNext: {}", hasNext);
                return hasNext;
            }

            @Override
            public Point next() {
                if (!hasNext()) {
                    log.error("Попытка вызова next() при отсутствии элементов");
                    throw new NoSuchElementException();
                }

                Point point = new Point(cur.x, cur.y);
                log.trace("Итератор вернул точку: {}", point);
                cur = cur.next;
                returned++;
                return point;
            }

            @Override
            public void remove() {
                log.warn("Попытка вызова remove() у итератора");
                throw new UnsupportedOperationException();
            }
        };
    }
}

