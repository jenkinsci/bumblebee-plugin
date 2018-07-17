package com.agiletestware.bumblebee.uft;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import com.agiletestware.bumblebee.ReportFolderProvider;
import com.agiletestware.bumblebee.client.api.BumblebeeApi;
import com.agiletestware.bumblebee.client.api.BumblebeeApiImpl;
import com.agiletestware.bumblebee.client.runner.ExecutionEnvironment;
import com.agiletestware.bumblebee.client.runner.Runner;
import com.agiletestware.bumblebee.client.runner.TestPathFileProvider;
import com.agiletestware.bumblebee.client.uftrunner.UftCommandLineBuilder;
import com.agiletestware.bumblebee.client.uftrunner.UftRunnerParameters;
import com.agiletestware.bumblebee.client.utils.BuildLogger;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.TaskListener;

/**
 * {@link Runner} implementation which runs UFT Batch Runner via cmd.
 *
 * @author Sergey Oplavin
 *
 */
public class UftRunner implements Runner<UftRunnerParameters, Integer>, Serializable {
	/** . */
	private static final long serialVersionUID = -4096227064300451050L;
	private final FilePath workspace;
	private final TaskListener listener;

	/**
	 * Constructor.
	 *
	 * @param workspace
	 *            workspace.
	 * @param listener
	 *            listener.
	 */
	public UftRunner(final FilePath workspace, final TaskListener listener) {
		this.workspace = workspace;
		this.listener = listener;
	}

	@Override
	public Integer run(final UftRunnerParameters parameters, final ExecutionEnvironment context, final BuildLogger buildLogger) throws Exception {
		final File report = new File(context.getWorkingDir(), UUID.randomUUID().toString());
		try (final BumblebeeApi api = new BumblebeeApiImpl(parameters.getBumbleBeeUrl(), (int) TimeUnit.MINUTES.toSeconds(parameters.getTimeOut()))) {
			parameters.setReportFileName(report.getAbsolutePath());
			final UftCommandLineBuilder uftBuilder = new UftCommandLineBuilder(parameters, new ReportFolderProvider(new File(workspace.getRemote())),
					new TestPathFileProvider(new File(workspace.getRemote())));
			final List<String> cmdList = uftBuilder.getCommandLineArguments(context, true);
			final Launcher launcher = new hudson.Launcher.LocalLauncher(listener);
			final Proc proc = launcher.launch().cmds(cmdList).pwd(context.getBumblebeeDir()).readStdout().start();
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
		} finally {
			FileUtils.deleteQuietly(report);
		}
	}

}
