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
}
