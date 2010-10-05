package org.mixins4j.writer;

import static org.mixins4j.util.MethodUtils.PROTECTED_PUBLIC_ABSTRACT_FILTER;
import static org.mixins4j.util.MethodUtils.constructMethodInvocation;
import static org.mixins4j.util.MethodUtils.constructMethodSignature;
import static org.mixins4j.util.MethodUtils.isAbstract;
import static org.mixins4j.util.MethodUtils.isDefault;
import static org.mixins4j.util.MethodUtils.isProtected;
import static org.mixins4j.util.NameUtils.getSimpleName;

import javax.lang.model.element.ExecutableElement;

import org.mixins4j.util.DelegateMethods;
import org.mixins4j.util.MethodUtils;
import org.mixins4j.util.NameUtils;
import org.mixins4j.util.PrintWriter;

public class DelegateClassWriter extends AbstractClassWriter {
	private final String mixinCanonicalName;
	private final DelegateMethods methods;

	public DelegateClassWriter(DelegateClassWriterOptions options) {
		super(options);
		this.mixinCanonicalName = options.getMixinCanonicalName();
		this.methods = options.getClassData().getMethods();
	}

	@Override
	protected String getThisClassCanonicalName(ClassWriterOptions options) {
		return NameUtils.getDelegateCanonicalName(options.getClassType(), options.getDelegateSuffix(),
				((DelegateClassWriterOptions) options).getMixinCanonicalName());
	}

	@Override
	protected void processConstructor(PrintWriter writer) {
		writer.println("private final " + mixinCanonicalName + " mixin;");
		writer.println();
		writer.println("public " + simpleName + "(" + mixinCanonicalName + " mixin) {");
		writer.indent();
		writer.println("this.mixin = mixin;");
		writer.unindent();
		writer.println("}");
		writer.println();
	}

	@Override
	protected void processClassHeader(PrintWriter writer) {
		writer.println("public class " + simpleName + " extends " + getSimpleName(classType) + " {");
	}

	public void writeMethods(PrintWriter writer) {
		for (ExecutableElement method : methods) {
			if (mustWrite(method)) {
				String returnStatement = getReturnStatement(method);
				String signature = getSignature(method);
				String propertyName = getPropertyName(method);
				String invocation = getInvocation(method);

				writer.println("@Override");
				writer.println(signature + " {");
				writer.indent();
				writer.println(returnStatement + propertyName + "." + invocation + ";");
				writer.unindent();
				writer.println("}");
				writer.println();
			}
		}
	}

	private boolean mustWrite(ExecutableElement method) {
		return isProtected(method) || isDefault(method) || isAbstract(method);
	}

	private String getSignature(ExecutableElement method) {
		return "public " + constructMethodSignature(method, PROTECTED_PUBLIC_ABSTRACT_FILTER, true);
	}

	private String getPropertyName(ExecutableElement method) {
		if (MethodUtils.isAbstract(method)) {
			return "mixin";
		} else {
			return "super";
		}
	}

	private String getInvocation(ExecutableElement method) {
		return constructMethodInvocation(method);
	}

	@Override
	protected void processContents(PrintWriter writer) {
		processConstructor(writer);
		writeMethods(writer);
	}

}
