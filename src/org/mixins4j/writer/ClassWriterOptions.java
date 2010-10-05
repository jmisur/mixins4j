package org.mixins4j.writer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

public class ClassWriterOptions {
	private String delegateSuffix;
	private TypeElement classType;
	private ProcessingEnvironment processingEnv;

	public String getDelegateSuffix() {
		return delegateSuffix;
	}

	public void setDelegateSuffix(String delegateSuffix) {
		this.delegateSuffix = delegateSuffix;
	}

	public TypeElement getClassType() {
		return classType;
	}

	public void setClassType(TypeElement classType) {
		this.classType = classType;
	}

	public ProcessingEnvironment getProcessingEnv() {
		return processingEnv;
	}

	public void setProcessingEnv(ProcessingEnvironment processingEnv) {
		this.processingEnv = processingEnv;
	}

}
