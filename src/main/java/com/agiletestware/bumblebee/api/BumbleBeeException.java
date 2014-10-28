package com.agiletestware.bumblebee.api;

/**
 * BumbleBee specific exception.
 *
 * @author Sergey Oplavin (oplavin.sergei@gmail.com)
 *
 */
public class BumbleBeeException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public BumbleBeeException(final String message) {
		super(message);
	}

	public BumbleBeeException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
