package org.mixins4j.processor;

import static org.mixins4j.util.TypeUtils.getSuperClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.tools.Diagnostic.Kind;

import org.mixins4j.Mixin;
import org.mixins4j.util.ProcessorMessages;
import org.mixins4j.writer.MixinClassWriter;
import org.mixins4j.writer.MixinClassWriterOptions;

// TODO support for final classes - don't make delegate
// TODO throws in method declarations
// TODO how constructors?
// TODO generics
// TODO final nad method/class a a ostatne
// TODO duplicate abstract method?
// TODO aggregate super methods + check conflicting
// TODO javadoc
// TODO other method/class annotations?
// TODO method.isVarArgs()
// TODO processingEvn.getElementUtils.getpackage, isdeprecated, getdoccomment
// TODO extract everything to utils (getqualified name etc)
// TODO tests
@SupportedAnnotationTypes({ "org.mixins4j.Mixin" })
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class MixinProcessor extends AbstractProcessor {
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Mixin.class);
		processElements(elements);
		return true;
	}

	private void processElements(Set<? extends Element> elements) {
		for (Element element : elements) {
			MixinClassWriterOptions options = processOptions();

			// check annotation applied to class only
			if (!checkKind(element))
				continue;

			// init parameters from annotation 
			processAnnotationParameters(element, options);
			//new ScalaProcessor().processAnnotationParameters(element, options, processingEnv);

			// check if superclass correctly specified
			TypeElement superclass = getSuperClass((TypeElement) element);
			if (superclass.getQualifiedName().toString().equals(Object.class.getCanonicalName())) {
				processingEnv.getMessager().printMessage(Kind.ERROR,
						"Mixin class must extends some superclass, e.g. " + element.getSimpleName() + "Mixin", element);
				continue;
			}

			options.setClassType((TypeElement) element);
			options.setProcessingEnv(processingEnv);

			ProcessorMessages messages = new ProcessorMessages();
			new MixinClassWriter(options, messages).writeClass();
			printMessages(messages);
		}
	}

	private void printMessages(ProcessorMessages messages) {
		messages.printErrors(processingEnv.getMessager());
		messages.printWarnings(processingEnv.getMessager());
	}

	private void processAnnotationParameters(Element element, MixinClassWriterOptions options) {
		for (AnnotationMirror annotation : element.getAnnotationMirrors()) {
			if (isMixin(annotation)) {

				// get all properties from this mixin annotation
				for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation
						.getElementValues().entrySet()) {

					ExecutableElement fieldName = entry.getKey();
					AnnotationValue fieldValue = entry.getValue();
					// TODO processingEnv.getMessager().printMessage(Kind.ERROR,
					// fieldName.getSimpleName(), element,
					// annotation, fieldValue);
					if (fieldName.getSimpleName().contentEquals("ignoreConflicting")) {
						options.setIgnoreConflicting((Boolean) fieldValue.getValue());
					} else if (fieldName.getSimpleName().contentEquals("aggregate")) {
						options.setAggregate((Boolean) fieldValue.getValue());
					} else if (fieldName.getSimpleName().contentEquals("superclass")) {
						DeclaredType clazz = (DeclaredType) fieldValue.getValue();
						TypeElement superClass = (TypeElement) clazz.asElement();
						options.setSuperClass(superClass);
					} else if (fieldName.getSimpleName().contentEquals("delegates")) {
						options.setDelegates(getDelegates(fieldValue));
					}
				}

			}
		}
	}

	private MixinClassWriterOptions processOptions() {
		MixinClassWriterOptions options = new MixinClassWriterOptions();
		options.setDelegateSuffix(getOption("delegateSuffix", "Delegate"));
		return options;
	}

	private String getOption(String optionName, String defaultValue) {
		return processingEnv.getOptions().get(optionName) != null ? processingEnv.getOptions().get(optionName)
				: defaultValue;
	}

	private boolean checkKind(Element element) {
		if (element.getKind() != ElementKind.CLASS) {
			processingEnv.getMessager().printMessage(Kind.ERROR, "Mixin annotation can be applied to class only",
					element);
			return false;
		}

		return true;
	}

	private boolean isMixin(AnnotationMirror m) {
		return ((TypeElement) m.getAnnotationType().asElement()).getQualifiedName().toString()
				.equals(Mixin.class.getCanonicalName());
	}

	private List<TypeElement> getDelegates(AnnotationValue fieldValue) {
		@SuppressWarnings("unchecked")
		Collection<? extends AnnotationValue> list = (Collection<? extends AnnotationValue>) fieldValue.getValue();

		List<TypeElement> types = new ArrayList<TypeElement>();

		for (AnnotationValue o : list) {
			DeclaredType type = (DeclaredType) o.getValue();
			TypeElement typeElement = (TypeElement) type.asElement();
			types.add(typeElement);
		}

		return types;
	}
}
