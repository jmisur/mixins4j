package org.mixins4j.writer;

import static org.mixins4j.util.NameUtils.getPackageName;
import static org.mixins4j.util.NameUtils.getSimpleName;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;

import org.mixins4j.util.PrintWriter;

public abstract class AbstractClassWriter implements ClassWriter {
	protected final TypeElement classType;
	protected final ProcessingEnvironment processingEnv;
	protected final String canonicalName;
	protected final String packageName;
	protected final String simpleName;
	protected final String delegateSuffix;

	public AbstractClassWriter(ClassWriterOptions options) {
		this.classType = options.getClassType();
		this.processingEnv = options.getProcessingEnv();
		this.delegateSuffix = options.getDelegateSuffix();

		canonicalName = getThisClassCanonicalName(options);
		packageName = getPackageName(canonicalName);
		simpleName = getSimpleName(canonicalName);
	}

	protected abstract String getThisClassCanonicalName(ClassWriterOptions options);

	public void writeClass() {
		PrintWriter writer = null;
		try {
			JavaFileObject delegateFile = processingEnv.getFiler().createSourceFile(canonicalName);
			writer = new PrintWriter(delegateFile.openWriter());

			writeInternal(writer);
		} catch (FileNotFoundException e) {
			// TODO ERROR
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	protected void writeInternal(PrintWriter writer) {
		processPackageHeader(writer);
		processClassHeader(writer);
		writer.indent();
		processContents(writer);
		writer.unindent();
		processFooter(writer);
	}

	protected abstract void processClassHeader(PrintWriter writer);

	protected abstract void processConstructor(PrintWriter writer);

	protected abstract void processContents(PrintWriter writer);

	private void processFooter(PrintWriter writer) {
		writer.println("}");
	}

	private void processPackageHeader(PrintWriter writer) {
		if (!packageName.isEmpty())
			writer.println("package " + packageName + ";");
		writer.println();
	}

	protected String getReturnStatement(ExecutableElement method) {
		if (method.getReturnType().getKind() != TypeKind.VOID) {
			return "return ";
		}
		return "";
	}

}
