package org.mixins4j.util;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;

public class Aggregates extends HashMap<String, Map<ExecutableElement, ClassData>> {

	private static final long serialVersionUID = 1L;

	public void addMethod(ClassData classData, ExecutableElement method, String id) {
		Map<ExecutableElement, ClassData> types = this.get(id);
		if (types == null) {
			types = new HashMap<ExecutableElement, ClassData>();
			this.put(id, types);
		}
		types.put(method, classData);
	}

}
