package mathproj.functions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Функция, всегда возвращающая 1.
public class UnitFunction extends ConstantFunction{
    private static final Logger logger = LoggerFactory.getLogger(UnitFunction.class);

    public UnitFunction(){
        super(1.0); // Вызов конструктора родителя
        logger.debug("Создана функция, всегда возвращающая единицу");
    }
}
