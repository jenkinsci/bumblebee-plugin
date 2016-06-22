package com.agiletestware.bumblebee;

import com.agiletestware.bumblebee.client.utils.BuildLogger;
import com.agiletestware.bumblebee.util.StringBuilderWrapper;

/**
 * Implementation of {@link BuildLogger}.
 *
 * @author Sergey Oplavin
 *
 */
public class JenkinsBuildLogger implements BuildLogger {

	private final StringBuilderWrapper logger;

	public JenkinsBuildLogger(final StringBuilderWrapper logger) {
		this.logger = logger;
	}

	@Override
	public void debug(final String message) {
		logger.println(message);
	}

	@Override
	public void info(final String message) {
		logger.println(message);
	}

	@Override
	public String toString() {
		return logger.toString();
	}
}
