package org.mixins4j;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Mixin {
	Class<?>[] delegates() default {};

	Class<?> superclass() default Void.class;

	boolean aggregate() default false;

	boolean ignoreConflicting() default false;
}
