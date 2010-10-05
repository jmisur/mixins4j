package org.mixins4j.util;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

public class TypeUtils {
	public static TypeElement getSuperClass(TypeElement element) {
		return (TypeElement) ((DeclaredType) element.getSuperclass()).asElement();
	}

}
