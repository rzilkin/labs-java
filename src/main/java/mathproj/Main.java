package mathproj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Приложение запущено");
        Runtime runtime = Runtime.getRuntime();
        logger.debug("Доступных процессоров: {}", runtime.availableProcessors());
        logger.debug("Максимальный объём памяти: {} байт", runtime.maxMemory());
        logger.info("Приложение завершило инициализацию");
    }
}
