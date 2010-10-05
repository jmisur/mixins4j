package org.mixins4j.util;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

public class NameUtils {
	public static String getSimpleName(TypeElement classType) {
		return classType.getSimpleName().toString();
	}

	public static String getSimpleNameWithSuffix(TypeElement element, String suffix) {
		return getSimpleName(element) + suffix;
	}

	public static String getCanonicalName(TypeElement clazz) {
		return clazz.getQualifiedName().toString();
	}

	public static String getCanonicalNameWithSuffix(TypeElement classType, String suffix) {
		return getCanonicalName(classType) + suffix;
	}

	public static String getSimpleName(String canonicalClassName) {
		int index = canonicalClassName.lastIndexOf('.');
		if (index > 0)
			return canonicalClassName.substring(index + 1);
		else
			return canonicalClassName;
	}

	public static String getPackageName(String canonicalClassName) {
		int index = canonicalClassName.lastIndexOf('.');
		if (index > 0)
			return canonicalClassName.substring(0, index);
		else
			return "";
	}

	public static boolean simpleNameEquals(DeclaredType type, String simpleName) {
		return ((TypeElement) (type).asElement()).getSimpleName().toString().equals(simpleName);
	}

	public static String getPackageName(TypeElement typeElement) {
		return getPackageName(getCanonicalName(typeElement));
	}

	public static String getDelegateCanonicalName(TypeElement classType, String delegateSuffix,
			String mixinCanonicalName) {
		return getCanonicalNameWithSuffix(classType, delegateSuffix) + "For_"
				+ getFlattenCanonicalName(mixinCanonicalName);
	}

	private static String getFlattenCanonicalName(String canonicalName) {
		return canonicalName.replaceAll("\\.", "_");
	}

	public static String getPackageNameWithDot(TypeElement classType) {
		String packageName = getPackageName(classType);
		if (packageName.isEmpty())
			return "";
		return packageName + ".";
	}
}
