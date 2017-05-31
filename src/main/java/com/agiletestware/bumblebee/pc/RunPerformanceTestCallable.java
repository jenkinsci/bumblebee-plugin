package com.agiletestware.bumblebee.pc;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jenkinsci.remoting.RoleChecker;

import com.agiletestware.bumblebee.JenkinsBuildLogger;
import com.agiletestware.bumblebee.client.pc.RunPcTestCallable;
import com.agiletestware.bumblebee.client.pc.RunPcTestContext;
import com.agiletestware.bumblebee.client.utils.BuildLogger;
import com.agiletestware.bumblebee.client.utils.ObsoleteFileRemover;

import hudson.AbortException;
import hudson.model.TaskListener;
import hudson.remoting.Callable;

/**
 * Implementation of {@link Callable} interface which executes running PC test
 * on a remote agent. Please note that all parameters passed into constructor
 * must implement {@link Serializable} interface.
 *
 * @author Sergey Oplavin
 *
 */
class RunPerformanceTestCallable implements Callable<Void, Exception> {

	/** . */
	private static final long serialVersionUID = -8912232168754236306L;

	private final RunPcTestContext context;

	private final TaskListener listener;

	private final long jenkinsBuildStartTime;

	/**
	 * Constructor.
	 *
	 * @param context
	 *            context.
	 * @param listener
	 *            listener.
	 * @param jenkinsBuildStartTime
	 *            start time of Jenkins build, in milliseconds. It is needed to
	 *            decide which files in resulting directory should be deleted.
	 */
	public RunPerformanceTestCallable(final RunPcTestContext context, final TaskListener listener, final long jenkinsBuildStartTime) {
		this.context = context;
		this.listener = listener;
		this.jenkinsBuildStartTime = jenkinsBuildStartTime;
	}

	@Override
	public Void call() throws Exception {
		final File outputDir = context.getOutputDir();
		final int executionTimeout = context.getTimeout();
		final BuildLogger logger = new JenkinsBuildLogger(listener);
		if (outputDir.exists()) {
			ObsoleteFileRemover.THE_INSTANCE.deleteObsoleteFiles(outputDir, jenkinsBuildStartTime, logger);
		} else {
			outputDir.mkdir();
		}
		final RunPcTestCallable callable = new RunPcTestCallable(
				context, logger);
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			final Future<Void> task = executor.submit(callable);
			try {
				if (executionTimeout > 0) {
					task.get(executionTimeout, TimeUnit.SECONDS);
				} else {
					task.get();
				}
			} catch (final TimeoutException ex) {
				task.cancel(true);
				throw new AbortException("Timeout has occurred, but Performance Center task is still running. Terminating the task");
			} catch (InterruptedException | ExecutionException e) {
				logger.error("Error has occurred: " + e.getMessage());
				throw new RuntimeException("Exception during Performance Center task execution: " + e.getMessage(), e);
			}
		} finally {
			executor.shutdownNow();
		}
		return null;
	}

	@Override
	public void checkRoles(final RoleChecker checker) throws SecurityException {
	}

}