package mathproj.concurrent;

import mathproj.functions.TabulatedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ReadTask.class);

    private final TabulatedFunction func;

    public ReadTask(TabulatedFunction func) {
        log.debug("Создание ReadTask с функцией: {}", func.getClass().getSimpleName());
        this.func = func;
    }

    @Override
    public void run() {
        log.info("Поток {} начал выполнение ReadTask", Thread.currentThread().getName());

        int count = func.getCount();
        log.debug("Чтение функции с {} точками", count);
        for (int i = 0; i < count; i++) {
            synchronized (func) {
                double x = func.getX(i);
                double y = func.getY(i);
                log.trace("Поток {}: точка [{}] x = {}, y = {}", Thread.currentThread().getName(), i, x, y);

                System.out.printf("After read: i = %d, x = %f, y = %f%n", i, x, y);
            }
        }
        log.info("Поток {} закончил выполнение ReadTask", Thread.currentThread().getName());
    }
}
