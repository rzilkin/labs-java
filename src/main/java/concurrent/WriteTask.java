package concurrent;

import functions.TabulatedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriteTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(WriteTask.class);

    private final TabulatedFunction func;
    private final double value;

    WriteTask(TabulatedFunction func, double value) {
        log.debug("Создание WriteTask с функцией: {} и значением: {}", func.getClass().getSimpleName(), value);
        this.func = func;
        this.value = value;
    }

    @Override
    public void run() {
        log.info("Поток {} начал выполнение WriteTask со значением: {}", Thread.currentThread().getName(), value);
        int count = func.getCount();
        log.debug("Запись в функцию с {} точками", count);
        for(int i = 0; i < count; ++i) {
            synchronized (func) {
                double x = func.getX(i);
                double oldY = func.getY(i);
                func.setY(i, value);
                log.trace("Поток {}: точка [{}] x = {}, y изменено с {} на {}", Thread.currentThread().getName(), i, x, oldY, value);

                System.out.printf("Writing for index %d complete\n", i);
            }
        }
        log.info("Поток {} закончил выполнение WriteTask", Thread.currentThread().getName());
    }
}
