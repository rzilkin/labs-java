package concurrent;

import functions.ConstantFunction;
import functions.LinkedListTabulatedFunction;
import functions.TabulatedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadWriteTaskExecutor {
    private static final Logger log = LoggerFactory.getLogger(ReadWriteTaskExecutor.class);

    public static void main(String[] args) {
        log.info("Запуск ReadWriteTaskExecutor");

        ConstantFunction constFunc = new ConstantFunction(-1);
        log.debug("Создана константная функция со значением: -1");
        TabulatedFunction func = new LinkedListTabulatedFunction(constFunc, 1, 1000, 1000);
        log.debug("Создана табличная функция: {} точек на интервале [1, 1000]", func.getCount());

        Thread executionThreadRead = new Thread(new ReadTask(func));
        Thread executionThreadWrite = new Thread(new WriteTask(func, 0.5));
        log.info("Созданы потоки: ReadTask - {}, WriteTask - {}", executionThreadRead.getName(), executionThreadWrite.getName());

        log.info("Запуск потока чтения");
        executionThreadRead.start();
        log.info("Запуск потока записи");
        executionThreadWrite.start();

        log.info("Оба потока запущены, основная программа завершает выполнение");
    }
}
