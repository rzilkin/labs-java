package concurrent;

import functions.TabulatedFunction;

public class MultiplyingTask implements Runnable {
    private final TabulatedFunction function;

    public MultiplyingTask(TabulatedFunction function) {
        this.function = function;
    }

    @Override
    public void run() {
        int n = function.getCount();
        for(int i = 0; i < n; i++) {
            synchronized (function) {
                double y = function.getY(i);
                function.setY(i, y * 2.0);
            }
        }

        System.out.println("Поток " + Thread.currentThread().getName() + " закончил выполнение задачи");
    }
}
