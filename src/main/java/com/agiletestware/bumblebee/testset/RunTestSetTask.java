package com.agiletestware.bumblebee.testset;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.List;

import org.jenkinsci.remoting.RoleChecker;

import com.agiletestware.bumblebee.JenkinsBuildLogger;
import com.agiletestware.bumblebee.ReportFolderProvider;
import com.agiletestware.bumblebee.client.runner.ExecutionEnvironment;
import com.agiletestware.bumblebee.client.runner.ExternalProcessRunner;
import com.agiletestware.bumblebee.client.testrunner.TestSetCommandLineBuilder;
import com.agiletestware.bumblebee.client.testrunner.TestSetRunner;
import com.agiletestware.bumblebee.client.testrunner.TestSetRunnerParameters;
import com.agiletestware.bumblebee.client.utils.BuildLogger;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.TaskListener;
import hudson.remoting.Callable;

/**
 * {@link Callable} task which runs test set on remote agent.
 *
 * @author Sergey Oplavin
 *
 */
public class RunTestSetTask implements Callable<Integer, Exception> {

	/** . */
	private static final long serialVersionUID = 6667786334878876264L;
	private final TaskListener listener;
	private final FilePath jenkinsDirPath;
	private final FilePath workspace;
	private final TestSetRunnerParameters parameters;

	/**
	 * Creates a new task
	 *
	 * @param listener
	 *            listener
	 * @param jenkinsDirPath
	 *            base directory for Jenkins agent or master.
	 * @param workspace
	 *            workspace of a project.
	 * @param parameters
	 *            parameters.
	 */
	public RunTestSetTask(final TaskListener listener, final FilePath jenkinsDirPath, final FilePath workspace, final TestSetRunnerParameters parameters) {
		this.listener = listener;
		this.jenkinsDirPath = jenkinsDirPath;
		this.workspace = workspace;
		this.parameters = parameters;
	}

	@Override
	public Integer call() throws Exception {
		final File jenkinsDir = new File(jenkinsDirPath.getRemote());
		final TestSetRunner runner = new TestSetRunner(new ReportFolderProvider(new File(workspace.getRemote()))) {

			@Override
			protected Integer runTestSets(final TestSetCommandLineBuilder cmdBuilder, final File projectXml, final File outputDirectory,
					final ExecutionEnvironment environment, final BuildLogger logger)
							throws Exception {
				final List<String> cmdList = cmdBuilder.getCommandLineArguments(parameters, true);
				final Launcher launcher = new hudson.Launcher.LocalLauncher(listener);
				final Proc proc = launcher.launch().cmds(cmdList).pwd(environment.getBumblebeeDir()).readStdout().start();
				final PrintStream stream = listener.getLogger();
				try (final BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getStdout()), 4096)) {
					String line;
					while ((line = reader.readLine()) != null) {
						stream.println(line);
					}
				}
				final int code = proc.join();
				stream.println("Return code: " + code);
				return code;
			}

		};
		return new ExternalProcessRunner<TestSetRunnerParameters>(jenkinsDir).run(runner, parameters, new JenkinsBuildLogger(listener));
	}

	@Override
	public void checkRoles(final RoleChecker arg0) throws SecurityException {
	}

}
