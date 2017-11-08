package com.agiletestware.bumblebee.results;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import com.agiletestware.bumblebee.BumblebeeGlobalConfig;
import com.agiletestware.bumblebee.client.api.BumblebeeApi;
import com.agiletestware.bumblebee.client.api.BumblebeeApiProvider;
import com.agiletestware.bumblebee.client.api.DefaultBumblebeeApiProvider;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.model.GlobalConfiguration;

/**
 * Post build action for fetching test results from HP ALM TestSet. It fetches
 * results from HP ALM as a JUnit-like report and stores it into some results
 * folder where it can be picked up by JUnit results publisher.
 *
 * @author Sergey Oplavin
 *
 */
public class GetTestResults extends Recorder {
	private static final String PLUGIN_DISPLAY_NAME = "Bumblebee: Fetch Results from HP ALM";

	private BumblebeeApiProvider bumblebeeApiProvider = new DefaultBumblebeeApiProvider();
	private final GetTestResultsParametersFactory parametersFactory = DefaultGetTestResultsParametersFactory.THE_INSTANCE;

	private final String domain;
	private final String project;
	private final String testSetPath;
	private final String resultsDir;

	@DataBoundConstructor
	public GetTestResults(final String domain, final String project, final String testSetPath, final String resultsDir) {
		this.domain = domain;
		this.project = project;
		this.testSetPath = testSetPath;
		this.resultsDir = resultsDir;
	}

	@Override
	public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
		final PrintStream logger = listener.getLogger();
		logger.println("Fetching test results from HP ALM");
		final BumblebeeGlobalConfig globalConfig = GlobalConfiguration.all().get(BumblebeeGlobalConfig.class);
		final GetTestResultsParameters params = new GetTestResultsEnvSpecificParams(parametersFactory.create(globalConfig, this),
				build.getEnvironment(listener));
		logParameters(globalConfig, params, logger);
		final BumblebeeApi api = bumblebeeApiProvider.provide(globalConfig.getBumblebeeUrl(), (int) TimeUnit.MINUTES.toSeconds(globalConfig.getTimeOut()));
		final FilePath workspace = build.getWorkspace();
		final FilePath reportFile = workspace.child(getResultsDir()).child(createFileName(params.getTestSetPath()));
		try (InputStream stream = api.getTestResults(params)) {
			logger.println("Writing results into: " + reportFile.getRemote());
			try (OutputStream out = reportFile.write()) {
				IOUtils.copy(stream, out);
				out.flush();
			}
		}
		return true;
	}

	private String createFileName(final String testSetPath) {
		return testSetPath.replaceAll("\\\\", "_") + "_" + System.currentTimeMillis() + ".xml";
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	public String getDomain() {
		return domain;
	}

	public String getProject() {
		return project;
	}

	public String getTestSetPath() {
		return testSetPath;
	}

	public String getResultsDir() {
		return resultsDir;
	}

	void setBumblebeeApiProvider(final BumblebeeApiProvider bumblebeeApiProvider) {
		this.bumblebeeApiProvider = bumblebeeApiProvider;
	}

	private void logParameters(final BumblebeeGlobalConfig config, final GetTestResultsParameters params, final PrintStream logger) {
		logger.println("Parameters:");
		logger.println("Bumblebee URL: " + config.getBumblebeeUrl());
		logger.println("HP ALM URL: " + config.getQcUrl());
		logger.println("HP ALM User: " + config.getQcUserName());
		logger.println("HP ALM Domain: " + params.getDomain());
		logger.println("HP ALM Project: " + params.getProject());
		logger.println("Test Lab Path: " + params.getTestSetPath());
		logger.println("Results directory: " + getResultsDir());
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		@Override
		public String getDisplayName() {
			return PLUGIN_DISPLAY_NAME;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
			return true;
		}

	}

}
