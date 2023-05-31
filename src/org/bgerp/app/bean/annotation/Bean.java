package org.bgerp.app.bean.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark created by name classes.
 *
 * @author Shamil Vakhitov
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Bean {
    /**
     * @return old bean full class names, which can be still used in configurations.
     */
    String[] oldClasses() default {};
}
