package concurrent;

import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import functions.UnitFunction;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiplyingTaskExecutor {
    private static final Logger log = LoggerFactory.getLogger(MultiplyingTaskExecutor.class);

    public static void main(String[] args) throws InterruptedException {
        log.info("MultiplyingTaskExecutor запущен");

        TabulatedFunction func = new LinkedListTabulatedFunction(new UnitFunction(), 1.0, 1000.0, 1000);
        log.debug("Создана табличная функция: {} точек на интервале [1.0, 1000.0]", func.getCount());
        List<Thread> threads = new ArrayList<>();
        log.info("Создание 10 потоков для выполнения MultiplyingTask");

        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(new MultiplyingTask(func));
            threads.add(t);
            log.debug("Создан поток #{}: {}", i, t.getName());
        }

        log.info("Запуск всех потоков");
        for (Thread t : threads) {
            t.start();
            log.debug("Запущен поток: {}", t.getName());
        }

        log.info("Ожидание завершения всех потоков");
        for (Thread t : threads) {
            t.join();
            log.debug("Поток {} завершил выполнение", t.getName());
        }

        log.info("Все потоки завершены. Результат функции:");
        log.debug("Финальное состояние функции: {}", func.toString());
        System.out.println(func.toString());
        log.info("MultiplyingTaskExecutor завершён");
    }
}
