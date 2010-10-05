package org.mixins4j.extractor;

import static org.mixins4j.util.MethodUtils.constructMethodId;
import static org.mixins4j.util.NameUtils.getCanonicalName;

import java.util.Collection;
import java.util.HashSet;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;

import org.mixins4j.util.ClassData;

public class DataExtractor {

	private TypeElement classType;
	protected final Collection<String> written = new HashSet<String>();

	public DataExtractor(TypeElement classType) {
		this.classType = classType;
	}

	public ClassData extractData() {
		ClassData data = new ClassData(classType);
		processMethodsRecursively(data.getMethods(), classType);
		return data;
	}

	protected void processMethods(Collection<ExecutableElement> methods, TypeElement typeElement) {
		for (Element elem : typeElement.getEnclosedElements()) {
			if (elem instanceof ExecutableElement) {
				ExecutableElement method = (ExecutableElement) elem;

				// skip overridden methods
				String id = constructMethodId(method);
				if (written.contains(id))
					continue;

				methods.add(method);
				written.add(id);
			}
		}
	}

	protected void processMethodsRecursively(Collection<ExecutableElement> methods, TypeElement classType) {
		if (getCanonicalName(classType).equals(Object.class.getCanonicalName()))
			return;
		processMethods(methods, classType);
		if (classType.getSuperclass().getKind() != TypeKind.NONE)
			processMethodsRecursively(methods, (TypeElement) ((DeclaredType) classType.getSuperclass()).asElement());
	}
}
