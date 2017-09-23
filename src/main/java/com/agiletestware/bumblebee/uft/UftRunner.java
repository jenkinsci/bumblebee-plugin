package com.agiletestware.bumblebee.uft;

import static com.agiletestware.bumblebee.tracking.AttributeNames.DURATION;
import static com.agiletestware.bumblebee.tracking.AttributeNames.ERROR;
import static com.agiletestware.bumblebee.tracking.AttributeNames.RETURN_CODE;
import static com.agiletestware.bumblebee.tracking.AttributeNames.TESTS_RUN;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.file.Files;
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
import com.agiletestware.bumblebee.tracking.ClientType;
import com.agiletestware.bumblebee.tracking.EventModel;
import com.agiletestware.bumblebee.tracking.EventName;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.TaskListener;
import jersey.repackaged.com.google.common.collect.ImmutableMap;

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
		final long startTime = System.currentTimeMillis();
		final BumblebeeApi api = new BumblebeeApiImpl(parameters.getBumbleBeeUrl(), (int) TimeUnit.MINUTES.toSeconds(parameters.getTimeOut()));
		final File report = new File(context.getWorkingDir(), UUID.randomUUID().toString());
		try {
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
			sendTrackingEvent(api, parameters, System.currentTimeMillis() - startTime, report, code, buildLogger);
			return code;
		} catch (final Exception ex) {
			sendTrackingEvent(api, ex.getMessage(), parameters.getClientType(), buildLogger);
			throw ex;
		} finally {
			FileUtils.deleteQuietly(report);
		}
	}

	private void sendTrackingEvent(final BumblebeeApi api, final UftRunnerParameters params, final long duration, final File report, final int returnCode,
			final BuildLogger logger) {
		try {
			final String reportContent = new String(Files.readAllBytes(report.toPath()));
			final EventModel eventModel = new EventModel(EventName.UFT_TEST_RUN, params.getClientType(), ImmutableMap.<String, Object> builder()
					.put(TESTS_RUN, reportContent)
					.put(DURATION, duration)
					.put(RETURN_CODE, returnCode)
					.build());
			api.trackAction(eventModel);
		} catch (final Exception ex) {
			logger.error("Could not send tracking event: " + ex.getMessage(), ex);
		}
	}

	private void sendTrackingEvent(final BumblebeeApi api, final String errorMessage, final ClientType clientType, final BuildLogger logger) {
		try {

			final EventModel eventModel = new EventModel(EventName.UFT_TEST_RUN, clientType, ImmutableMap.<String, Object> builder()
					.put(ERROR, errorMessage)
					.build());
			api.trackAction(eventModel);
		} catch (final Exception ex) {
			logger.error("Could not send tracking event: " + ex.getMessage(), ex);
		}
	}

}
