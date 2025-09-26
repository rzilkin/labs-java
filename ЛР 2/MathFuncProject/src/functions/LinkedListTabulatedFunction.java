package functions;

public class LinkedListTabulatedFunction extends AbstractTabulatedFunction implements Insertable, Removable {

    private class Node {
        public Node next;
        public Node prev;
        public double x;
        public double y;
    }

    protected int count;
    Node head;

    private void addNode(double x, double y) {
        Node newNode = new Node();

        newNode.x = x;
        newNode.y = y;

        if(head == null) {
            head = newNode;
            head.next = head;
            head.prev = head;
        }

        else {
            Node last = head.prev;
            last.next = newNode;
            head.prev = newNode;
            newNode.next = head;
            newNode.prev = last;
        }

        count += 1;
    }

    public LinkedListTabulatedFunction(double[] xValues, double[] yValues) {
        for(int i = 0; i < xValues.length; ++i) {
            addNode(xValues[i], yValues[i]);
        }
    }

    public LinkedListTabulatedFunction(MathFunction source, double xFrom, double xTo, int count) {
        if(xFrom > xTo) {
            double temp = xFrom;
            xFrom = xTo;
            xTo = temp;
        }

        if(xFrom == xTo) {
            for (int i = 0; i < count; ++i) {
                addNode(xFrom, source.apply(xFrom));
            }
        }
        else {
            double step = (xTo - xFrom) / (count - 1);
            for(int i = 0; i < count; ++i) {
                double x = xFrom + i * step;
                addNode(x, source.apply(x));
            }
        }
    }

    @Override
    public void insert(double x, double y) {
        if (head == null) {
            addNode(x, y);
            return;
        }

        Node cur = head;
        int iter = 0;

        do {
            if (cur.x == x) {
                cur.y = y;
                return;
            }

            if (x < cur.x) {
                insertBefore(cur, x, y);
                if (cur == head) {
                    head = head.prev;
                }
                return;
            }

            if (cur.next == head || x < cur.next.x) {
                insertAfter(cur, x, y);
                return;
            }

            cur = cur.next;
            iter++;
        } while (cur != head && iter < count);

        insertBefore(head, x, y);
    }

    private void insertAfter(Node node, double x, double y) {
        Node newNode = new Node();
        newNode.x = x;
        newNode.y = y;

        newNode.next = node.next;
        newNode.prev = node;
        node.next.prev = newNode;
        node.next = newNode;

        count++;
    }

    private void insertBefore(Node node, double x, double y) {
        Node newNode = new Node();
        newNode.x = x;
        newNode.y = y;

        newNode.prev = node.prev;
        newNode.next = node;
        node.prev.next = newNode;
        node.prev = newNode;

        count++;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public double leftBound() {
        return head.x;
    }

    @Override
    public double rightBound() {
        return head.prev.x;
    }

    private Node getNode(int index) {
        if (index < count / 2) {
            Node cur = head;
            for (int i = 0; i < index; i++) {
                cur = cur.next;
            }
            return cur;
        }
        else {
            Node cur = head.prev;
            for (int i = count - 1; i > index; ++i) {
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
    public int indexOfX(double x) {
        Node cur = head;
        for (int i = 0; i < count; i++) {
            if (cur.x == x) {
                return i;
            }
            cur = cur.next;
        }
        return -1;
    }

    @Override
    public int indexOfY(double y) {
        Node cur = head;
        for (int i = 0; i < count; i++) {
            if (cur.y == y) {
                return i;
            }
            cur = cur.next;
        }
        return -1;
    }

    @Override
    public int floorIndexOfX(double x) {
        if (x < head.x) return 0;

        Node cur = head;
        for (int i = 0; i < count; i++) {
            if (cur.x > x) {
                return i == 0 ? 0 : i - 1;
            }
            cur = cur.next;
        }
        return count;
    }

    @Override
    public double extrapolateLeft(double x) {
        if (count == 1) return head.y;
        Node first = head;
        Node second = head.next;
        return interpolate(x, first.x, second.x, first.y, second.y);
    }

    @Override
    public double extrapolateRight(double x) {
        if (count == 1) return head.y;
        Node last = head.prev;
        Node secondLast = last.prev;
        return interpolate(x, secondLast.x, last.x, secondLast.y, last.y);
    }

    @Override
    public double interpolate(double x, int floorIndex) {
        if (count == 1) return head.y;
        if (floorIndex == count) return head.prev.y;

        Node left = getNode(floorIndex);
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
            count = 0;
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
        count--;
        setCount(count);

        // Обнуляем ссылки удалённого узла
        cur.next = null;
        cur.prev = null;
    }
}
