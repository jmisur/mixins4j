package org.mixins4j.util;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

public class ProcessorMessages {
	private List<ProcessorMessage> errors = new ArrayList<ProcessorMessage>();
	private List<ProcessorMessage> warnings = new ArrayList<ProcessorMessage>();
	private static final long serialVersionUID = 1L;

	public void addError(String message, TypeElement classType) {
		errors.add(new ProcessorMessage(Kind.ERROR, message, classType));
	}

	public void addWarning(String message, TypeElement classType) {
		warnings.add(new ProcessorMessage(Kind.WARNING, message, classType));
	}

	public void printErrors(Messager messager) {
		for (ProcessorMessage message : errors) {
			messager.printMessage(message.getKind(), message.getMessage(), message.getTypeElement());
		}
	}

	public void printWarnings(Messager messager) {
		for (ProcessorMessage message : warnings) {
			messager.printMessage(message.getKind(), message.getMessage(), message.getTypeElement());
		}
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}
}
