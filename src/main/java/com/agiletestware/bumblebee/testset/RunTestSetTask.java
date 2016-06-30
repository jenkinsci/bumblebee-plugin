package com.agiletestware.bumblebee.testset;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang.mutable.MutableInt;
import org.jenkinsci.remoting.RoleChecker;

import com.agiletestware.bumblebee.JenkinsBuildLogger;
import com.agiletestware.bumblebee.client.testrunner.CommandLineBuilder;
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
		final MutableInt returnCode = new MutableInt();
		final TestSetRunner runner = new TestSetRunner(jenkinsDir) {

			@Override
			protected void runTestSets(final CommandLineBuilder cmdBuilder, final File projectXml, final File outputDirectory, final BuildLogger logger)
					throws Exception {
				final List<String> cmdList = cmdBuilder.getCommandLineArguments(true);
				final Launcher launcher = new hudson.Launcher.LocalLauncher(listener);
				launcher.launch().cmds(cmdList).pwd(projectXml.getParentFile());
				final Proc proc = launcher.launch().cmds(cmdList).pwd(getBumblebeeDir()).readStdout().start();
				final PrintStream stream = listener.getLogger();
				try (final BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getStdout()), 4096)) {
					String line;
					while ((line = reader.readLine()) != null) {
						stream.println(line);
					}
				}
				final int code = proc.join();
				stream.println("Return code: " + code);
				returnCode.setValue(code);
			}
		};
		final Path outputDir = Paths.get(workspace.getRemote(), parameters.getOutputDirPath());
		if (!Files.exists(outputDir)) {
			Files.createDirectories(outputDir);
		}
		runner.runTestSets(parameters, outputDir.toFile(), new JenkinsBuildLogger(listener));
		return returnCode.intValue();
	}

	@Override
	public void checkRoles(final RoleChecker arg0) throws SecurityException {
	}

}
