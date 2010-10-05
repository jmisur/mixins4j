package org.mixins4j.util;

import javax.lang.model.element.TypeElement;

public class ClassData {
	private final TypeElement classType;
	private DelegateMethods methods = new DelegateMethods();

	public ClassData(TypeElement classType) {
		super();
		this.classType = classType;
	}

	public DelegateMethods getMethods() {
		return methods;
	}

	public void setMethods(DelegateMethods methods) {
		this.methods = methods;
	}

	public TypeElement getClassType() {
		return classType;
	}

}
