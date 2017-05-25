package com.agiletestware.bumblebee.uft;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.remoting.RoleChecker;

import com.agiletestware.bumblebee.JenkinsBuildLogger;
import com.agiletestware.bumblebee.client.runner.DefaultExecutionEnvironmentProvider;
import com.agiletestware.bumblebee.client.runner.ExecutionEnvironmentProvider;
import com.agiletestware.bumblebee.client.runner.ExternalProcessRunner;
import com.agiletestware.bumblebee.client.runner.Runner;
import com.agiletestware.bumblebee.client.uftrunner.UftRunnerParameters;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.Callable;

/**
 * Callable which can be run on remote agent and launch UFT Batch Runner.
 *
 * @author Sergey Oplavin
 *
 */
public class RunUftTestTask implements Callable<Integer, Exception> {

	private static final long serialVersionUID = -2882280267417631378L;
	private final UftRunnerParameters parameters;
	private final TaskListener listener;
	private final FilePath jenkinsDirPath;
	private ExecutionEnvironmentProvider executionEnvironmentProvider;
	private Runner<UftRunnerParameters, Integer> uftRunner;

	/**
	 * Constructor.
	 *
	 * @param parameters
	 *            parameters.
	 * @param listener
	 *            listener.
	 * @param jenkinsDirPath
	 *            path to the Jenkins root directory.
	 * @param workspace
	 *            build workspace.
	 */
	public RunUftTestTask(final UftRunnerParameters parameters, final TaskListener listener, final FilePath jenkinsDirPath, final FilePath workspace) {
		super();
		this.parameters = parameters;
		this.listener = listener;
		this.jenkinsDirPath = jenkinsDirPath;
		this.uftRunner = new UftRunner(workspace, listener);
		this.executionEnvironmentProvider = new DefaultExecutionEnvironmentProvider();

	}

	@Override
	public Integer call() throws Exception {
		validateParameters(parameters);
		final File jenkinsDir = new File(jenkinsDirPath.getRemote());
		return new ExternalProcessRunner<UftRunnerParameters>(jenkinsDir, executionEnvironmentProvider).run(uftRunner, parameters,
				new JenkinsBuildLogger(listener));
	}

	/**
	 * For testing purposes. Set runner.
	 *
	 * @param uftRunner
	 *            runner.
	 */
	void setUftRunner(final Runner<UftRunnerParameters, Integer> uftRunner) {
		this.uftRunner = uftRunner;
	}

	/**
	 * For testing purposes. Set environment provider.
	 *
	 * @param executionEnvironmentProvider
	 *            provider.
	 */
	void setExecutionEnvironmentProvider(final ExecutionEnvironmentProvider executionEnvironmentProvider) {
		this.executionEnvironmentProvider = executionEnvironmentProvider;
	}

	@Override
	public void checkRoles(final RoleChecker arg0) throws SecurityException {
	}

	private void validateParameters(final UftRunnerParameters parameters) {
		// TODO: move parameter labels and messages to properties file.
		assertNotEmpty(parameters.getBumbleBeeUrl(), "Bumblebee URL");
		final String uftPath = parameters.getUftBatchRunnerExePath();
		assertNotEmpty(uftPath, "UFT Batch Runner");
		final File uftRunner = new File(uftPath);
		if (!uftRunner.exists() || !uftRunner.isFile()) {
			throw new RuntimeException("UFT Batch Runner: " + uftRunner.getAbsolutePath() + " does not exist or not a file.");
		}
		assertNotEmpty(parameters.getTestPath(), "Test Path");
		assertNotEmpty(parameters.getOutputDirName(), "JUnit Results Directory");
	}

	private void assertNotEmpty(final String value, final String label) {
		if (StringUtils.isEmpty(value)) {
			throw new RuntimeException(label + " is not defined. Please check your configuration");
		}
	}

}
