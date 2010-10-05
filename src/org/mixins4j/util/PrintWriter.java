package org.mixins4j.util;

import java.io.Writer;

public class PrintWriter extends java.io.PrintWriter {

	private String indentElement = "	";
	private int indent;

	public PrintWriter(Writer out) {
		super(out);
	}

	public void indent() {
		indent++;
	}

	public void unindent() {
		indent--;
	}

	private void printIndent() {
		for (int i = 0; i < indent; i++)
			append(indentElement);
	}

	@Override
	public void println(boolean x) {
		printIndent();
		super.println(x);
	}

	@Override
	public void println(char x) {
		printIndent();
		super.println(x);
	}

	@Override
	public void println(char[] x) {
		printIndent();
		super.println(x);
	}

	@Override
	public void println(double x) {
		printIndent();
		super.println(x);
	}

	@Override
	public void println(float x) {
		printIndent();
		super.println(x);
	}

	@Override
	public void println(int x) {
		printIndent();
		super.println(x);
	}

	@Override
	public void println(long x) {
		printIndent();
		super.println(x);
	}

	@Override
	public void println(Object x) {
		printIndent();
		super.println(x);
	}

	@Override
	public void println(String x) {
		printIndent();
		super.println(x);
	}

}
