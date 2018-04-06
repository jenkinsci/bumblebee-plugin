package com.agiletestware.bumblebee.results;

import static hudson.Util.fixEmptyAndTrim;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.agiletestware.bumblebee.BumblebeeGlobalConfig;
import com.agiletestware.bumblebee.client.api.BumblebeeApi;
import com.agiletestware.bumblebee.client.api.BumblebeeApiProvider;
import com.agiletestware.bumblebee.client.api.DefaultBumblebeeApiProvider;
import com.agiletestware.bumblebee.util.BumblebeeUtils;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.GlobalConfiguration;
import jenkins.tasks.SimpleBuildStep;

/**
 * Post build action for fetching test results from HP ALM TestSet. It fetches
 * results from HP ALM as a JUnit-like report and stores it into some results
 * folder where it can be picked up by JUnit results publisher.
 *
 * @author Sergey Oplavin
 *
 */
public class GetTestResults extends Recorder implements SimpleBuildStep {
	private static final Logger LOGGER = Logger.getLogger(GetTestResults.class.getName());
	private static final String PLUGIN_DISPLAY_NAME = "Bumblebee: Import HP ALM Test Results";
	private BumblebeeApiProvider bumblebeeApiProvider = new DefaultBumblebeeApiProvider();
	private final GetTestResultsParametersFactory parametersFactory = DefaultGetTestResultsParametersFactory.THE_INSTANCE;
	private final String domain;
	private final String project;
	private String user;
	private String password;
	private final String resultsDir;
	private final List<GetTestResultsConfiguration> configurations;

	/**
	 * Constructor.
	 *
	 * @param domain
	 *            domain
	 * @param project
	 *            project
	 * @param user
	 *            user
	 * @param password
	 *            password
	 * @param resultsDir
	 *            results directory
	 * @param configuration
	 *            list of configurations.
	 */
	@DataBoundConstructor
	public GetTestResults(final String domain, final String project, final String user, final String password, final String resultsDir,
			final List<GetTestResultsConfiguration> configurations) {
		this.domain = fixEmptyAndTrim(domain);
		this.project = fixEmptyAndTrim(project);
		this.user = fixEmptyAndTrim(user);
		final String plainPassword = fixEmptyAndTrim(password);
		this.password = plainPassword != null ? Secret.fromString(plainPassword).getEncryptedValue() : null;
		this.resultsDir = fixEmptyAndTrim(resultsDir);
		this.configurations = configurations;
	}

	@Override
	public void perform(final Run<?, ?> run, final FilePath workspace, final Launcher launcher, final TaskListener listener)
			throws InterruptedException, IOException {
		boolean success = true;
		final PrintStream logger = listener.getLogger();

		final BumblebeeGlobalConfig globalConfig = GlobalConfiguration.all().get(BumblebeeGlobalConfig.class);
		assertGlobalConfig(globalConfig);
		final String bumblebeeUrl = globalConfig.getBumblebeeUrl();
		try (final BumblebeeApi api = bumblebeeApiProvider.provide(bumblebeeUrl,
				(int) TimeUnit.MINUTES.toSeconds(globalConfig.getTimeOut()))) {
			final String encryptedPassword = StringUtils.isNotEmpty(password) ? api.getEncryptedPassword(Secret.decrypt(password).getPlainText()) : null;
			for (final GetTestResultsConfiguration configuration : configurations) {
				try {
					logger.println("Fetching test results from HP ALM");
					final GetTestResultsParameters params = new GetTestResultsEnvSpecificParams(parametersFactory.create(globalConfig, this, configuration),
							run.getEnvironment(listener));
					if (encryptedPassword != null) {
						params.setEncryptedPassword(encryptedPassword);
					}
					logParameters(bumblebeeUrl, params, resultsDir, logger);
					final FilePath reportFile = workspace.child(resultsDir).child(createFileName(params.getTestSetPath()));
					try (InputStream stream = api.getJunitTestResults(params)) {
						logger.println("Writing results into: " + reportFile.getRemote());
						try (OutputStream out = reportFile.write()) {
							IOUtils.copy(stream, out);
							out.flush();
						}
					}
				} catch (final Exception ex) {
					logger.println(ex.getMessage());
					LOGGER.log(Level.SEVERE, null, ex);
					success = false;
				}
			}
		} catch (final Exception ex) {
			logger.println(ex.getMessage());
			LOGGER.log(Level.SEVERE, null, ex);
		}
		if (!success) {
			logger.println("One or more configurations have fail --> mark build as failure. Check the log for details");
		}

	}

	private void assertGlobalConfig(final BumblebeeGlobalConfig globalConfig) throws AbortException {
		if (globalConfig == null) {
			throw new AbortException("Bumblebee Global configuration is not set propertly. Please configure Bumblebee Global settings");
		}
		final String bumblebeeUrl = globalConfig.getBumblebeeUrl();
		if (StringUtils.isEmpty(bumblebeeUrl)) {
			throw new AbortException("Bumblebee URL is not set. Please set a valid URL on Bumblebee Global configuration page");
		}
	}

	public List<GetTestResultsConfiguration> getConfigurations() {
		return configurations;
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

	public String getUser() {
		return user;
	}

	@DataBoundSetter
	public void setUser(final String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	@DataBoundSetter
	public void setPassword(final String password) {
		this.password = password;
	}

	void setBumblebeeApiProvider(final BumblebeeApiProvider bumblebeeApiProvider) {
		this.bumblebeeApiProvider = bumblebeeApiProvider;
	}

	private String createFileName(final String testSetPath) {
		return testSetPath.replaceAll("[\\\\/:*?\"<>|]", "_") + "_" + System.currentTimeMillis() + ".xml";
	}

	private void logParameters(final String bumblebeeUrl, final GetTestResultsParameters params, final String resultsDir, final PrintStream logger) {
		logger.println("Parameters:");
		logger.println("Bumblebee URL: " + bumblebeeUrl);
		logger.println("HP ALM URL: " + params.getAlmUrl());
		logger.println("HP ALM User: " + params.getAlmUserName());
		logger.println("HP ALM Domain: " + params.getDomain());
		logger.println("HP ALM Project: " + params.getProject());
		logger.println("Test Lab Path: " + params.getTestSetPath());
		logger.println("Results directory: " + resultsDir);
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

		public FormValidation doCheckDomain(@QueryParameter final String domain)
				throws IOException, ServletException {
			return BumblebeeUtils.validateRequiredField(domain);
		}

		public FormValidation doCheckProject(@QueryParameter final String project)
				throws IOException, ServletException {
			return BumblebeeUtils.validateRequiredField(project);
		}

		public FormValidation doCheckTestSetPath(@QueryParameter final String testSetPath)
				throws IOException, ServletException {
			return BumblebeeUtils.validateTestLab(testSetPath, "Test Set Path");
		}

		public FormValidation doCheckResultsDir(@QueryParameter final String resultsDir)
				throws IOException, ServletException {
			return BumblebeeUtils.validateRequiredField(resultsDir);
		}

	}

}
