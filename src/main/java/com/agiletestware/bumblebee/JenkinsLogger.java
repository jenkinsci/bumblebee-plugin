package com.agiletestware.bumblebee;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.agiletestware.bumblebee.client.utils.BuildLogger;

/**
 * Implementation of {@link BuildLogger} for java.util.logging.
 *
 * @author Ayman BEN AMOR
 *
 */
public class JenkinsLogger implements BuildLogger {

	private final Logger logger;

	public JenkinsLogger(final Class<?> clazz) {
		logger = Logger.getLogger(clazz.getName());
	}

	@Override
	public void info(final String message) {
		logger.log(Level.INFO, message);
	}

	@Override
	public void debug(final String message) {
		logger.log(Level.WARNING, message);
	}

	@Override
	public void error(final String message) {
		logger.log(Level.SEVERE, message);
	}

	@Override
	public void error(final String message, final Throwable cause) {
		logger.log(Level.SEVERE, message, cause);
	}
}
