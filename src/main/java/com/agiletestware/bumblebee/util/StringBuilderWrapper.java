package com.agiletestware.bumblebee.util;

import java.io.Serializable;

/**
 * Wrapper for {@link StringBuilder}. Provides convenient methods to add lines
 * of text.
 *
 * @author Sergey Oplavin (oplavin.sergei@gmail.com)
 *
 */
public class StringBuilderWrapper implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -4663937071045469032L;
	private final StringBuilder wrappedBuilder;
	private final String lineSeparator = System.lineSeparator();

	public StringBuilderWrapper() {
		this.wrappedBuilder = new StringBuilder();
	}

	/**
	 * Prints a line to underlying builder.
	 *
	 * @param str
	 *            The value to print.
	 */
	public void println(final String str) {
		wrappedBuilder.append(str);
		wrappedBuilder.append(lineSeparator);
	}

	/**
	 * Prints given string to underlying builder.
	 *
	 * @param str
	 */
	public void print(final String str) {
		wrappedBuilder.append(str);
	}

	@Override
	public String toString() {
		return wrappedBuilder.toString();
	}

}
