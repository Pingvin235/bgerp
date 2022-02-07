package org.bgerp.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation is used for marking elements, called from JSP templates or JEXL scripts.
 * Such ones should be handled especially careful.
 *
 * @author Shamil Vakhitov
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Dynamic {}
