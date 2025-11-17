package mathproj.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Функция, всегда возвращающая 0.
public class ZeroFunction extends ConstantFunction{
    private static final Logger logger = LoggerFactory.getLogger(ZeroFunction.class);

    public ZeroFunction(){
        super(0.0); // Вызов конструктора родителя
        logger.debug("Создана функция, всегда возвращающая ноль");
    }
}
