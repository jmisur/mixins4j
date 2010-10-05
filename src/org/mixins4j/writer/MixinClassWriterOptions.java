package org.mixins4j.writer;

import java.util.List;

import javax.lang.model.element.TypeElement;

public class MixinClassWriterOptions extends ClassWriterOptions {
	private TypeElement superClass;
	private List<TypeElement> delegates;
	private boolean aggregate;
	private boolean ignoreConflicting;

	public boolean isIgnoreConflicting() {
		return ignoreConflicting;
	}

	public void setIgnoreConflicting(boolean ignoreConflicting) {
		this.ignoreConflicting = ignoreConflicting;
	}

	public TypeElement getSuperClass() {
		return superClass;
	}

	public void setSuperClass(TypeElement superClass) {
		this.superClass = superClass;
	}

	public List<TypeElement> getDelegates() {
		return delegates;
	}

	public void setDelegates(List<TypeElement> delegates) {
		this.delegates = delegates;
	}

	public boolean isAggregate() {
		return aggregate;
	}

	public void setAggregate(boolean aggregate) {
		this.aggregate = aggregate;
	}

}
