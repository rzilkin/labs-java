package concurrent;

import functions.TabulatedFunction;

public class WriteTask implements Runnable {
    private final TabulatedFunction func;
    private final double value;

    WriteTask(TabulatedFunction func, double value) {
        this.func = func;
        this.value = value;
    }

    @Override
    public void run() {
        int count = func.getCount();

        for(int i = 0; i < count; ++i) {
            synchronized (func) {
                double x = func.getX(i);
                func.setY(i, value);

                System.out.printf("Writing for index %d complete\n", i);
            }
        }
    }
}
