package org.mixins4j.writer;

import org.mixins4j.util.ClassData;

public class DelegateClassWriterOptions extends ClassWriterOptions {
	private String mixinCanonicalName;
	private ClassData classData;

	public String getMixinCanonicalName() {
		return mixinCanonicalName;
	}

	public void setMixinCanonicalName(String mixinCanonicalName) {
		this.mixinCanonicalName = mixinCanonicalName;
	}

	public ClassData getClassData() {
		return classData;
	}

	public void setClassData(ClassData classData) {
		this.classData = classData;
	}
}
