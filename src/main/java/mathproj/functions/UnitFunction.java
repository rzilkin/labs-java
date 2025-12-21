package mathproj.functions;

import mathproj.ui.registry.UiFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Функция, всегда возвращающая 1.
@UiFunction(name = "Единичная функция", priority = 10)
public class UnitFunction extends ConstantFunction{
    private static final Logger logger = LoggerFactory.getLogger(UnitFunction.class);

    public UnitFunction(){
        super(1.0); // Вызов конструктора родителя
        logger.debug("Создана функция, всегда возвращающая единицу");
    }
}
