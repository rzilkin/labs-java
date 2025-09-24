package functions;

public class LinkedListTabulatedFunction extends AbstractTabulatedFunction {

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

}
