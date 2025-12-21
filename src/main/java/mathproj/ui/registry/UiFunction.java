package mathproj.ui.registry;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UiFunction {
    String name();
    int priority() default 100;
}

