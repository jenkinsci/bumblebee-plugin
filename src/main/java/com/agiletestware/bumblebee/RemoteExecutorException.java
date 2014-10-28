package com.agiletestware.bumblebee;

/**
 * Exception which is thrown from remote slave and contains execution log.
 *
 * @author Sergey Oplavin (oplavin.sergei@gmail.com)
 *
 */
public class RemoteExecutorException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = -4793880791595022973L;
	private final String executionLog;

	/**
	 * Constructor.
	 *
	 * @param cause
	 *            cause throwable.
	 * @param executionLog
	 *            exception log.
	 */
	public RemoteExecutorException(final Throwable cause,
			final String executionLog) {
		super(cause);
		this.executionLog = executionLog;
	}

	/**
	 *
	 * @return the execution log before the exception occured.
	 */
	public String getRemoteExecutionLog() {
		return executionLog;
	}

}
