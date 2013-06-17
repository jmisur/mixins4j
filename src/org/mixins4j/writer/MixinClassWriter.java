package org.mixins4j.writer;

import static org.mixins4j.util.MethodUtils.EMPTY_FILTER;
import static org.mixins4j.util.MethodUtils.PROTECTED_PUBLIC_FILTER;
import static org.mixins4j.util.MethodUtils.constructMethodId;
import static org.mixins4j.util.MethodUtils.constructMethodInvocation;
import static org.mixins4j.util.MethodUtils.constructMethodSignature;
import static org.mixins4j.util.MethodUtils.constructMethodSignatureWithoutParamNames;
import static org.mixins4j.util.MethodUtils.isAbstract;
import static org.mixins4j.util.MethodUtils.isDefault;
import static org.mixins4j.util.MethodUtils.isPrivate;
import static org.mixins4j.util.MethodUtils.isProtected;
import static org.mixins4j.util.MethodUtils.isSamePackage;
import static org.mixins4j.util.NameUtils.getDelegateCanonicalName;
import static org.mixins4j.util.NameUtils.getPackageName;
import static org.mixins4j.util.NameUtils.getPackageNameWithDot;
import static org.mixins4j.util.NameUtils.getSimpleName;
import static org.mixins4j.util.TypeUtils.getSuperClass;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import org.mixins4j.extractor.DataExtractor;
import org.mixins4j.util.Aggregates;
import org.mixins4j.util.ClassData;
import org.mixins4j.util.DelegatesData;
import org.mixins4j.util.MethodUtils;
import org.mixins4j.util.MixinData;
import org.mixins4j.util.MixinMethods;
import org.mixins4j.util.NameUtils;
import org.mixins4j.util.PrintWriter;
import org.mixins4j.util.ProcessorMessages;

public class MixinClassWriter extends AbstractClassWriter {

	private final TypeElement superClass;
	private final List<TypeElement> delegates;
	private final boolean aggregate;
	private final boolean ignoreConflicting;
	private final ProcessorMessages messages;

	public MixinClassWriter(MixinClassWriterOptions options, ProcessorMessages messages) {
		super(options);
		this.messages = messages;
		this.superClass = options.getSuperClass();
		this.delegates = options.getDelegates();
		this.aggregate = options.isAggregate();
		this.ignoreConflicting = options.isIgnoreConflicting();
	}

	@Override
	protected void processConstructor(PrintWriter writer) {
		writer.println("public " + simpleName + "() {");
		writer.println("}");
	}

	@Override
	protected void processContents(PrintWriter writer) {
		DelegatesData delegatesData = new DelegatesData();
		MixinData mixinData = new MixinData();

		// processConstructor(writer);
		// writer.println();

		// TODO process superclass with event this
		if (delegates == null) {
			messages.addWarning("@Mixin annotation without delegates", classType);
		} else {
			processDelegates(delegatesData);
			processSuperclass(delegatesData);
			removeConstructors(delegatesData);
			processMixin(delegatesData, mixinData);

			if (messages.hasErrors()) {
				return;
			}

			writeDelegates(delegatesData);
			writeMixin(writer, mixinData);
		}
	}

	private void processSuperclass(DelegatesData delegatesData) {
		if (superClass != null) {
			ClassData superData = new DataExtractor(superClass).extractData();
			delegatesData.add(superData);
		}
	}

	private void removeConstructors(DelegatesData delegatesData) {
		for (ClassData classData : delegatesData) {
			for (Iterator<ExecutableElement> it = classData.getMethods().iterator(); it.hasNext();) {
				ExecutableElement method = it.next();
				if (MethodUtils.isConstructor(method)) {
					it.remove();
				}
			}
		}
	}

	private void writeMixin(PrintWriter writer, MixinData mixinData) {
		writeDelegatesVariables(writer, mixinData.getDelegates());
		writeMethods(writer, mixinData.getMethods());
	}

	private void writeDelegates(DelegatesData delegatesData) {
		for (ClassData delegate : delegatesData) {
			if (delegate.getClassType() != superClass)
				new DelegateClassWriter(createDelegateProcessorOptions(delegate)).writeClass();
		}
	}

	private void processDelegates(DelegatesData delegatesData) {
		for (TypeElement delegate : delegates) {
			ClassData delegateData = new DataExtractor(delegate).extractData();
			delegatesData.add(delegateData);
		}
	}

	private void writeDelegatesVariables(PrintWriter writer, Set<TypeElement> delegates) {
		for (TypeElement delegate : delegates) {
			if (delegate == superClass)
				continue;

			String clazz = getDelegateClassName(delegate);
			String name = getPropertyName(delegate);
			String constructor = clazz + "(this)";
			writer.println("private " + clazz + " " + name + " = new " + constructor + ";");
			writer.println();
		}

	}

	private void processMixin(DelegatesData delegatesData, MixinData mixinData) {
		performValidations(delegatesData, messages);
		Aggregates aggregates = createAggregates(delegatesData);
		processData(aggregates, messages, mixinData);
	}

	// TODO perform duplicates with return types validation
	// TODO javadoc recursively from overridden
	// TODO diamond inheritance is not duplicates

	private void performValidations(DelegatesData data, ProcessorMessages messages) {
		for (ClassData classData : data) {
			for (Iterator<ExecutableElement> it = classData.getMethods().iterator(); it.hasNext();) {
				ExecutableElement method = it.next();

				// private method - remove
				if (isPrivate(method)) {
					it.remove();
				}
				// default abstract in different package - error
				else if (!validateDefaultAbstractDifferentPackage(method, classData.getClassType(), messages)) {
					it.remove();
				}
				// default in different package - remove
				else if (!validateDefaultDifferentPackage(method, classData.getClassType())) {
					it.remove();
				}
			}
		}
	}

	private boolean validateDefaultDifferentPackage(ExecutableElement method, TypeElement methodClassType) {
		if (isDefault(method) && !isSamePackage(methodClassType, classType)) {
			return false;
		}
		return true;
	}

	private boolean validateDefaultAbstractDifferentPackage(ExecutableElement method, TypeElement methodClassType,
			ProcessorMessages messages) {
		if (isDefault(method) && isAbstract(method) && !isSamePackage(methodClassType, classType)) {
			messages.addError(
					"Method `" + constructMethodId(method) + "' from type " + methodClassType.getQualifiedName()
							+ " is not visible in this package (" + getPackageName(classType) + ")", classType);
			return false;
		}
		return true;
	}

	private Aggregates createAggregates(Set<ClassData> data) {
		Aggregates aggregates = new Aggregates();
		for (ClassData classData : data) {
			for (ExecutableElement method : classData.getMethods()) {
				aggregates.addMethod(classData, method, constructMethodId(method));
			}
		}
		return aggregates;
	}

	private void processData(Aggregates aggregates, ProcessorMessages messages, MixinData mixinData) {
		for (Map<ExecutableElement, ClassData> methods : aggregates.values()) {
			if (methods.size() > 1) {
				int conflicting = getConflictingCount(methods);

				if (conflicting > 1) {
					if (ignoreConflicting) {
						removeMethods(methods);
					} else {
						messages.addError(constructConflictingMethodsMessage(methods), classType);
					}
				} else {
					// TODO conflicting by throws
					// TODO wrapExceptions
					if (!returnsVoid(methods)) {
						if (ignoreConflicting) {
							removeMethods(methods);
						} else {
							messages.addError(constructConflictingMethodsMessage(methods), classType);
						}
					} else {
						if (aggregate) {
							addToMixinData(mixinData, methods);
						} else {
							removeMethods(methods);
						}
					}
				}
			} else {
				addToMixinData(mixinData, methods);
			}
		}
	}

	private boolean returnsVoid(Map<ExecutableElement, ClassData> methods) {
		assert methods.keySet().size() > 0;
		return methods.keySet().iterator().next().getReturnType().getKind() == TypeKind.VOID;
	}

	private int getConflictingCount(Map<ExecutableElement, ClassData> methods) {
		Set<String> conflicting = new HashSet<String>();
		for (ExecutableElement method : methods.keySet()) {
			String signature = constructMethodSignatureWithoutParamNames(method);
			conflicting.add(signature);
		}
		return conflicting.size();
	}

	private void addToMixinData(MixinData dataForMixin, Map<ExecutableElement, ClassData> methods) {
		ExecutableElement method = methods.keySet().iterator().next();
		Set<TypeElement> classTypes = new HashSet<TypeElement>();
		for (ClassData data : methods.values()) {
			classTypes.add(data.getClassType());
		}
		dataForMixin.addMethod(method, classTypes);
	}

	private void removeMethods(Map<ExecutableElement, ClassData> methods) {
		for (Map.Entry<ExecutableElement, ClassData> entry : methods.entrySet()) {
			entry.getValue().getMethods().remove(entry.getKey());
		}
	}

	private String constructConflictingMethodsMessage(Map<ExecutableElement, ClassData> methods) {
		StringBuilder str = new StringBuilder();
		str.append("Conflicting method signatures:\n");
		for (ExecutableElement method : methods.keySet()) {
			str.append("`" + constructMethodSignatureWithoutParamNames(method) + "' in class "
					+ ((TypeElement) method.getEnclosingElement()).getQualifiedName() + "\n");
		}
		return str.toString();
	}

	private DelegateClassWriterOptions createDelegateProcessorOptions(ClassData delegate) {
		DelegateClassWriterOptions options = new DelegateClassWriterOptions();
		options.setClassType(delegate.getClassType());
		options.setDelegateSuffix(delegateSuffix);
		options.setMixinCanonicalName(canonicalName);
		options.setProcessingEnv(processingEnv);
		options.setClassData(delegate);
		return options;
	}

	@Override
	protected String getThisClassCanonicalName(ClassWriterOptions options) {
		return getPackageNameWithDot(classType) + getSimpleName(getSuperClass(classType));
	}

	@Override
	protected void processClassHeader(PrintWriter writer) {
		String extendsClass = superClass != null ? " extends " + NameUtils.getCanonicalName(superClass) : "";
		writer.println("public abstract class " + simpleName + extendsClass + " {");
	}

	public void writeMethods(PrintWriter writer, MixinMethods methods) {
		for (Map.Entry<ExecutableElement, Set<TypeElement>> entry : methods.entrySet()) {
			ExecutableElement method = entry.getKey();
			Set<TypeElement> delegates = entry.getValue();

			writeJavaDoc(writer, method);

			String signature = getSignature(method);

			// if abstract write only signature
			if (isAbstract(method)) {
				writer.println(signature + ";");
			} else {
				writer.println(signature + " {");
				writer.indent();

				String returnStatement = getReturnStatement(method);
				if (!returnStatement.isEmpty()) {
					// should fail on conflict check before
					assert delegates.size() == 1;
				}

				for (TypeElement delegate : delegates) {
					String propertyName = getPropertyName(delegate);
					if (delegate == superClass)
						propertyName = "super";
					writer.println(returnStatement + propertyName + "." + constructMethodInvocation(method) + ";");
				}

				writer.unindent();
				writer.println("}");
			}

			writer.println();
		}
	}

	private void writeJavaDoc(PrintWriter writer, ExecutableElement method) {
		String javadoc = processingEnv.getElementUtils().getDocComment(method);
		if (javadoc == null)
			return;

		writer.println("/**");
		for (String line : javadoc.split("\n")) {
			writer.println(" * " + line);
		}
		writer.println(" */");
	}

	private String getPropertyName(TypeElement typeElement) {
		return getDelegateClassName(typeElement).replaceAll("\\.", "_");
	}

	private String getDelegateClassName(TypeElement delegate) {
		return getDelegateCanonicalName(delegate, delegateSuffix, canonicalName);
	}

	private String getSignature(ExecutableElement method) {
		if (isAbstract(method)
				&& (!isSamePackage((TypeElement) method.getEnclosingElement(), classType) || isProtected(method))) {
			// if abstract from different package or protected abstract from
			// this package
			// must change to public so delegate can see it
			return "public " + constructMethodSignature(method, PROTECTED_PUBLIC_FILTER, true);
		} else {
			return constructMethodSignature(method, EMPTY_FILTER, true);
		}
	}

}
