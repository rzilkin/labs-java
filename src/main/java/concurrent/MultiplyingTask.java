package concurrent;

import functions.TabulatedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiplyingTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(MultiplyingTask.class);

    private final TabulatedFunction function;

    public MultiplyingTask(TabulatedFunction function) {
        log.debug("Создание MultiplyingTask с функцией {}", function.getClass().getSimpleName());
        this.function = function;
    }

    @Override
    public void run() {
        log.info("Поток {} начал выполнение MultiplyingTask", Thread.currentThread().getName());

        int n = function.getCount();
        log.debug("Обработка функции с {} точками", n);
        for(int i = 0; i < n; i++) {
            synchronized (function) {
                double y = function.getY(i);
                function.setY(i, y * 2.0);
                log.trace("Поток {}: точка [{}] y изменено с {} на {}", Thread.currentThread().getName(), i, y, y*2.0);
            }
        }

        log.info("Поток {} закончил выполнение MultiplyingTask", Thread.currentThread().getName());
    }
}
