package concurrent;

import functions.TabulatedFunction;

public class ReadTask implements Runnable {
    private final TabulatedFunction func;

    public ReadTask(TabulatedFunction func) {
        this.func = func;
    }

    @Override
    public void run() {
        int count = func.getCount();

        for (int i = 0; i < count; i++) {
            synchronized (func) {
                double x = func.getX(i);
                double y = func.getY(i);

                System.out.printf("After read: i = %d, x = %f, y = %f%n", i, x, y);
            }
        }
    }
}
