package gui;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PersistWindowState {
    boolean value() default true;
}
