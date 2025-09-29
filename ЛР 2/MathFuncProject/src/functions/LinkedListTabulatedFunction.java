package functions;

//класс хранения данных табличной функции на основе циклического двусвязного списка, расширяющий AbstractTabulatedFunction
//и реализующий Insertable, Removable
public class LinkedListTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable {
    Node head;      //голова списка

    private void addNode(double x, double y) {      //добавление узла
        Node newNode = new Node();      //создание нового узла
        //добавление значений точки
        newNode.x = x;
        newNode.y = y;

        if(head == null) {      //если нет элементов, то создаём голову списка, которая ссылается на саму себя
            head = newNode;
            //создание ссылок
            head.next = head;
            head.prev = head;
        }

        else {      //если список не пуст, то переопределяем связи между узлами
            Node last = head.prev;
            last.next = newNode;
            head.prev = newNode;
            newNode.next = head;
            newNode.prev = last;
        }

        setCount(getCount() + 1);     //расширяем список
    }

    //конструктор, если данные в массивах
    public LinkedListTabulatedFunction(double[] xValues, double[] yValues) {
        for(int i = 0; i < xValues.length; ++i) {
            addNode(xValues[i], yValues[i]);
        }
    }
    //конструктор при помощи другой функции
    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if(xFrom > xTo) {       //если xFrom > xTo, то свапаем их
            double temp = xFrom;
            xFrom = xTo;
            xTo = temp;
        }

        if(xFrom == xTo) {      //если границы совпадают, то создаём каунтовое количество точек с функцией source
            for (int i = 0; i < count; ++i) {
                addNode(xFrom, source.apply(xFrom));
            }
        }
        else {                  //если не совпадают, то добавляем точки при помощи равномерной дискретизации
            double step = (xTo - xFrom) / (count - 1); //задаём шаг, который будет ровно делить отрезок
            for(int i = 0; i < count; ++i) {
                double x = xFrom + i * step;        //создаём каждый x подобным образом: x1 = a + b, x2 = a + 2b, и так далее
                addNode(x, source.apply(x));        //добавляем точку
            }
        }
    }

    @Override
    public void insert(double x, double y) {        //добавление точки
        if (head == null) {         //если список пуст, просто добавляем узел
            addNode(x, y);
            return;
        }

        Node cur = head;    //создаём текущий узел
        int iter = 0;

        do {
            if (cur.x == x) {       //если совпал x, то просто меняем y на новый
                cur.y = y;
                return;
            }

            if (x < cur.x) {        //если меньше некоторого x в списке, то вставляем слева от него
                insertBefore(cur, x, y);        //вызов вспомогательного метода
                if (cur == head) {
                    head = head.prev;       //обновляем голову, если вставляем перед ней
                }
                return;
            }

            if (cur.next == head || x < cur.next.x) {       //если меньше следующего, но больше текущего, то вставляем после
                insertAfter(cur, x, y);     //вызов доп. метода
                return;
            }

            cur = cur.next;     //двигаемя дальше по списку
            iter++;
        } while (cur != head && iter < getCount());  //идём пока текущий указатель не ссылается на голову и количество итераций не перевалит за число элементов в списке

        insertBefore(head, x, y);       //вставка в конец
    }

    private void insertAfter(Node node, double x, double y) {       //вставка после
        Node newNode = new Node();      //новый узел
        newNode.x = x;          //добавление значений
        newNode.y = y;

        newNode.next = node.next;       //обновление связей узлов
        newNode.prev = node;
        node.next.prev = newNode;
        node.next = newNode;

        setCount(getCount() + 1);        //увеличиваем длину списка
    }

    private void insertBefore(Node node, double x, double y) {      //вставка до
        Node newNode = new Node();      //новый узел
        newNode.x = x;          //добавление значений
        newNode.y = y;

        newNode.prev = node.prev;       //обновление связей узлов
        newNode.next = node;
        node.prev.next = newNode;
        node.prev = newNode;

        setCount(getCount() + 1);        //увеличиваем длину списка
    }

    @Override
    public double leftBound() {
        return head.x;
    }

    @Override
    public double rightBound() {
        return head.prev.x;
    }

    private Node getNode(int index) {       //вспомогательный метод получения узла по индексу
        if (index < getCount() / 2) {        //ускоряем работу получения узла, узнав в какой части списка узел
            Node cur = head;
            //по списку проходим до нашего индекса
            for (int i = 0; i < index; i++) {
                cur = cur.next;
            }
            return cur;
        }
        else {
            Node cur = head.prev;
            //по списку проходим до нашего индекса, но если во второй части списка
            for (int i = getCount() - 1; i > index; ++i) {
                cur = cur.prev;
            }
            return cur;
        }
    }

    @Override
    public double getX(int index) {
        return getNode(index).x;
    }

    @Override
    public double getY(int index) {
        return getNode(index).y;
    }

    @Override
    public void setY(int index, double value) {
        getNode(index).y = value;
    }

    @Override
    public int indexOfX(double x) {     //индекс по значению x
        Node cur = head;
        for (int i = 0; i < getCount(); i++) {
            if (cur.x == x) {
                return i;
            }
            cur = cur.next;
        }
        return -1;
    }

    @Override
    public int indexOfY(double y) {     //индекс по значению y
        Node cur = head;
        for (int i = 0; i < getCount(); i++) {
            if (cur.y == y) {
                return i;
            }
            cur = cur.next;
        }
        return -1;
    }

    @Override
    public int floorIndexOfX(double x) {        //находит x, удовлетворяющий: x[i] <= x < x[i+1]
        if (x < head.x) return 0;

        Node cur = head;
        for (int i = 0; i < getCount(); i++) {
            if (cur.x > x) {
                return i == 0 ? 0 : i - 1;
            }
            cur = cur.next;
        }
        return getCount();
    }

    @Override
    public double extrapolateLeft(double x) {       //левая экстраполяция
        if (getCount() == 1) return head.y;          //если один элемент в списке
        Node first = head;
        Node second = head.next;
        return interpolate(x, first.x, second.x, first.y, second.y);
    }

    @Override
    public double extrapolateRight(double x) {  //правая экстраполяция
        if (getCount() == 1) return head.y;          //если один элемент в списке
        Node last = head.prev;
        Node secondLast = last.prev;
        return interpolate(x, secondLast.x, last.x, secondLast.y, last.y);
    }

    @Override
    public double interpolate(double x, int floorIndex) {       //интерполяция
        if (getCount() == 1) return head.y;          //если один элемент в списке
        if (floorIndex == getCount()) return head.prev.y;        //случай правой экстраполяции, просто возвращаем послединй y

        Node left = getNode(floorIndex);        //если мужду двумя x
        Node right = left.next;
        return interpolate(x, left.x, right.x, left.y, right.y);
    }

    @Override
    public void remove(int index) {
        // Защита на некорректный индекс
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("Index: " + index);
        }

        // Если единственный элемент — очистить список
        if (getCount() == 1) {
            head = null;
            setCount(0);
            return;
        }

        // выбираем более короткий путь: от головы (вперёд) или от хвоста (назад)
        Node cur;
        int i;
        int n = getCount();

        if (index < n / 2) {
            // Идём от головы вперёд
            cur = head;
            i = 0;
            while (i < index) {
                cur = cur.next;
                i++;
            }
        } else {
            // Идём от хвоста назад
            cur = head.prev; // Последний элемент
            i = n - 1;
            while (i > index) {
                cur = cur.prev;
                i--;
            }
        }

        // cur — узел, который нужно удалить
        cur.prev.next = cur.next;
        cur.next.prev = cur.prev;

        // Если удаляем головной элемент — обновляем head
        if (cur == head) {
            head = cur.next;
        }

        // Уменьшаем счётчик и синхронизируем с базой
        setCount(getCount() - 1);

        // Обнуляем ссылки удалённого узла
        cur.next = null;
        cur.prev = null;
    }
}
