package org.mixins4j.util;

import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

public class ProcessorMessage {

	private final Kind kind;
	private final String message;
	private final TypeElement typeElement;

	public ProcessorMessage(Kind kind, String message, TypeElement typeElement) {
		super();
		this.kind = kind;
		this.message = message;
		this.typeElement = typeElement;
	}

	public Kind getKind() {
		return kind;
	}

	public String getMessage() {
		return message;
	}

	public TypeElement getTypeElement() {
		return typeElement;
	}
}
