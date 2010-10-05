package org.mixins4j.util;

import static org.mixins4j.util.NameUtils.getPackageName;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;

public class MethodUtils {
	public static final Collection<Modifier> ALL_MODIFIERS_FILTER = new HashSet<Modifier>();
	public static final Collection<Modifier> PROTECTED_PUBLIC_ABSTRACT_FILTER = new HashSet<Modifier>();
	public static final Collection<Modifier> PROTECTED_PUBLIC_FILTER = new HashSet<Modifier>();
	public static final Collection<Modifier> EMPTY_FILTER = new HashSet<Modifier>();
	static {
		ALL_MODIFIERS_FILTER.addAll(Arrays.asList(Modifier.values()));
		PROTECTED_PUBLIC_ABSTRACT_FILTER.add(Modifier.PROTECTED);
		PROTECTED_PUBLIC_ABSTRACT_FILTER.add(Modifier.PUBLIC);
		PROTECTED_PUBLIC_ABSTRACT_FILTER.add(Modifier.ABSTRACT);
		PROTECTED_PUBLIC_FILTER.add(Modifier.PROTECTED);
		PROTECTED_PUBLIC_FILTER.add(Modifier.PUBLIC);
	}

	public static String constructMethodSignature(ExecutableElement method, Collection<Modifier> filter,
			boolean includeParameterNames) {
		StringBuilder signature = new StringBuilder();
		for (Modifier modifier : method.getModifiers()) {
			if (!filter.contains(modifier))
				signature.append(modifier.toString() + " ");
		}
		signature.append(method.getReturnType().toString() + " ");
		signature.append(method.getSimpleName().toString());
		constructMethodParameters(method, signature, includeParameterNames, true, true);
		return signature.toString();
	}

	private static void constructMethodParameters(ExecutableElement method, StringBuilder str,
			boolean includeParameterNames, boolean includeModifiers, boolean includeTypes) {
		str.append("(");
		List<? extends VariableElement> params = method.getParameters();
		for (Iterator<? extends VariableElement> it = params.iterator(); it.hasNext();) {
			VariableElement param = it.next();

			// modifiers
			if (includeModifiers) {
				for (Modifier modifier : param.getModifiers()) {
					str.append(modifier.toString() + " ");
				}
			}

			// types
			if (includeTypes) {
				String paramType = param.asType().toString();
				if (param.asType() instanceof ArrayType && !it.hasNext() && method.isVarArgs()) {
					paramType = paramType.replaceAll("\\[\\]", "...");
				}
				str.append(paramType);
			}

			// names
			if (includeParameterNames)
				str.append(" " + param.getSimpleName());
			str.append(", ");
		}

		// remove trailing ', '
		if (params.size() != 0) {
			str.deleteCharAt(str.length() - 1);
			str.deleteCharAt(str.length() - 1);
		}
		str.append(")");
	}

	public static String constructMethodInvocation(ExecutableElement method) {
		StringBuilder invocation = new StringBuilder();
		invocation.append(method.getSimpleName().toString());
		constructMethodParameters(method, invocation, true, false, false);
		return invocation.toString();
	}

	public static String constructMethodId(ExecutableElement method) {
		StringBuilder signature = new StringBuilder();
		signature.append(method.getSimpleName().toString());
		constructMethodParameters(method, signature, false, false, true);
		return signature.toString();
	}

	public static boolean isPrivate(ExecutableElement method) {
		return method.getModifiers().contains(Modifier.PRIVATE);
	}

	public static boolean isPublic(ExecutableElement method) {
		return method.getModifiers().contains(Modifier.PUBLIC);
	}

	public static boolean isProtected(ExecutableElement method) {
		return method.getModifiers().contains(Modifier.PROTECTED);
	}

	public static boolean isConstructor(ExecutableElement method) {
		return method.getKind() == ElementKind.CONSTRUCTOR;
	}

	public static boolean isAbstract(ExecutableElement method) {
		return method.getModifiers().contains(Modifier.ABSTRACT);
	}

	public static boolean isDefault(ExecutableElement method) {
		return (!method.getModifiers().contains(Modifier.PROTECTED) && !method.getModifiers().contains(Modifier.PUBLIC));
	}

	public static boolean isSamePackage(TypeElement first, TypeElement second) {
		return getPackageName(first).equals(getPackageName(second));
	}

	public static String constructMethodSignatureWithoutParamNames(ExecutableElement method) {
		return constructMethodSignature(method, EMPTY_FILTER, false);
	}
}
