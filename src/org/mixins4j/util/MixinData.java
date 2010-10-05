package org.mixins4j.util;

import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public class MixinData {

	private final MixinMethods methods = new MixinMethods();
	private final Set<TypeElement> delegates = new HashSet<TypeElement>();

	public MixinMethods getMethods() {
		return methods;
	}

	public void addMethod(ExecutableElement method, Set<TypeElement> classTypes) {
		methods.put(method, classTypes);
		delegates.addAll(classTypes);
	}

	public Set<TypeElement> getDelegates() {
		return delegates;
	}

}
