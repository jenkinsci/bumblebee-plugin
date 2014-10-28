package com.agiletestware.bumblebee.util;

import java.text.MessageFormat;

/**
 * Convenient implementation of thread local MessageFormat.
 *
 * @author Sergey Oplavin (oplavin.sergei@gmail.com)
 *
 */
public class ThreadLocalMessageFormat {

	/** format thread local instance. */
	private final ThreadLocal<MessageFormat> format;

	/**
	 * Constructor.
	 *
	 * @param pattern
	 *            format pattern see {@link MessageFormat} for details.
	 */
	public ThreadLocalMessageFormat(final String pattern) {
		this.format = new ThreadLocal<MessageFormat>() {
			@Override
			protected MessageFormat initialValue() {
				return new MessageFormat(pattern);
			}

		};
	}

	/**
	 * Formats an object to produce a string. See
	 * {@link MessageFormat#format(Object)} for details.
	 *
	 * @param obj
	 *            the object to format.
	 * @return formatted string.
	 */
	public String format(final Object... obj) {
		return this.format.get().format(obj);
	}
}