package com.agiletestware.bumblebee.pc;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import com.agiletestware.bumblebee.BumblebeeGlobalConfig;
import com.agiletestware.bumblebee.BumblebeePublisher;
import com.agiletestware.bumblebee.JenkinsBuildLogger;
import com.agiletestware.bumblebee.client.api.DefaultBumblebeeApiProvider;
import com.agiletestware.bumblebee.client.pc.ParametersLogger;
import com.agiletestware.bumblebee.client.pc.RunPcTestContext;
import com.agiletestware.bumblebee.encryption.CustomSecret;
import com.agiletestware.bumblebee.encryption.DefaultCustomSecret;
import com.agiletestware.bumblebee.validator.StringNotEmptyValidator;
import com.agiletestware.bumblebee.validator.StringStartsWithValidator;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import jenkins.tasks.SimpleBuildStep;

/**
 * Build step which allows running test in Performance Center and fetch its
 * results.
 *
 * @author Sergey Oplavin
 *
 */
public class RunPcTestBuildStep extends Builder implements SimpleBuildStep {
	private static final Logger LOGGER = Logger.getLogger(BumblebeePublisher.class.getName());
	private CustomSecret customSecret = DefaultCustomSecret.THE_INSTANCE;
	private String project;
	private String domain;
	private String almUser;
	private String almPassword;
	private String outputDir;
	private String testPlanPath;
	private String testLabPath;
	private String postRunActionString;
	private int timeslotDuration;
	private boolean vudsMode;
	private int timeout;
	private int retryCount;
	private int retryInterval;
	private double retryIntervalMultiplier;
	private boolean retryCollateAndAnalysisFlag;
	private int retryCollateAndAnalysisAttempts;
	private int retryCollateAndAnalysisInterval;
	private int pollingInterval;
	private boolean failIfTaskFails;

	@DataBoundConstructor
	public RunPcTestBuildStep() {
	}

	public String getOutputDir() {
		return outputDir;
	}

	@DataBoundSetter
	public void setOutputDir(final String outputDir) {
		this.outputDir = outputDir;
	}

	public String getTestPlanPath() {
		return testPlanPath;
	}

	@DataBoundSetter
	public void setTestPlanPath(final String testPlanPath) {
		this.testPlanPath = testPlanPath;
	}

	public String getTestLabPath() {
		return testLabPath;
	}

	@DataBoundSetter
	public void setTestLabPath(final String testLabPath) {
		this.testLabPath = testLabPath;
	}

	public String getPostRunActionString() {
		return postRunActionString;
	}

	public PostRunAction getPostRunAction() {
		return StringUtils.isNotEmpty(postRunActionString) ? PostRunAction.fromLabel(postRunActionString) : null;
	}

	@DataBoundSetter
	public void setPostRunActionString(final String postRunActionString) {
		this.postRunActionString = postRunActionString;
	}

	public int getTimeslotDuration() {
		return timeslotDuration;
	}

	@DataBoundSetter
	public void setTimeslotDuration(final int timeslotDuration) {
		this.timeslotDuration = timeslotDuration;
	}

	public boolean isVudsMode() {
		return vudsMode;
	}

	@DataBoundSetter
	public void setVudsMode(final boolean vudsMode) {
		this.vudsMode = vudsMode;
	}

	public int getTimeout() {
		return timeout;
	}

	@DataBoundSetter
	public void setTimeout(final int timeout) {
		this.timeout = timeout;
	}

	public int getRetryCount() {
		return retryCount;
	}

	@DataBoundSetter
	public void setRetryCount(final int retryCount) {
		this.retryCount = retryCount;
	}

	public int getRetryInterval() {
		return retryInterval;
	}

	@DataBoundSetter
	public void setRetryInterval(final int retryInterval) {
		this.retryInterval = retryInterval;
	}

	public double getRetryIntervalMultiplier() {
		return retryIntervalMultiplier;
	}

	@DataBoundSetter
	public void setRetryIntervalMultiplier(final double retryIntervalMultiplier) {
		this.retryIntervalMultiplier = retryIntervalMultiplier;
	}

	public boolean isRetryCollateAndAnalysisFlag() {
		return retryCollateAndAnalysisFlag;
	}

	@DataBoundSetter
	public void setRetryCollateAndAnalysisFlag(final boolean retryCollateAndAnalysisFlag) {
		this.retryCollateAndAnalysisFlag = retryCollateAndAnalysisFlag;
	}

	public int getRetryCollateAndAnalysisInterval() {
		return retryCollateAndAnalysisInterval;
	}

	@DataBoundSetter
	public void setRetryCollateAndAnalysisInterval(final int retryCollateAndAnalysisInterval) {
		this.retryCollateAndAnalysisInterval = retryCollateAndAnalysisInterval;
	}

	public int getRetryCollateAndAnalysisAttempts() {
		return retryCollateAndAnalysisAttempts;
	}

	@DataBoundSetter
	public void setRetryCollateAndAnalysisAttempts(final int retryCollateAndAnalysisAttempts) {
		this.retryCollateAndAnalysisAttempts = retryCollateAndAnalysisAttempts;
	}

	public int getPollingInterval() {
		return pollingInterval;
	}

	@DataBoundSetter
	public void setPollingInterval(final int pollingInterval) {
		this.pollingInterval = pollingInterval;
	}

	public boolean isFailIfTaskFails() {
		return failIfTaskFails;
	}

	@DataBoundSetter
	public void setFailIfTaskFails(final boolean failIfTaskFails) {
		this.failIfTaskFails = failIfTaskFails;
	}

	public String getProject() {
		return project;
	}

	@DataBoundSetter
	public void setProject(final String project) {
		this.project = project;
	}

	public String getDomain() {
		return domain;
	}

	@DataBoundSetter
	public void setDomain(final String domain) {
		this.domain = domain;
	}

	public String getAlmUser() {
		return almUser;
	}

	@DataBoundSetter
	public void setAlmUser(final String almUser) {
		this.almUser = almUser;
	}

	public String getAlmPassword() {
		return almPassword;
	}

	@DataBoundSetter
	public void setAlmPassword(final String almPassword) {
		final String plainTextPassword = Util.fixEmpty(almPassword);
		this.almPassword = plainTextPassword != null ? customSecret.getEncryptedValue(plainTextPassword) : null;
	}

	// Used only on tests.
	void setCustomSecret(final CustomSecret customSecret) {
		this.customSecret = customSecret;
	}

	@Override
	public void perform(final Run<?, ?> run, final FilePath workspace, final Launcher launcher, final TaskListener listener)
			throws InterruptedException, IOException {
		final PrintStream logger = listener.getLogger();
		logger.println("Start Performance Center test");
		try {
			final RunPcTestContext context = new RunPcTestContextImpl(this, GlobalConfiguration.all().get(BumblebeeGlobalConfig.class),
					workspace, DefaultCustomSecret.THE_INSTANCE, new DefaultBumblebeeApiProvider());
			ParametersLogger.THE_INSTANCE.logParameters(context, new JenkinsBuildLogger(listener), isFailIfTaskFails());
			final RunPerformanceTestCallable callable = new RunPerformanceTestCallable(context, listener, run.getStartTimeInMillis());
			launcher.getChannel().call(callable);
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
			if (isFailIfTaskFails()) {
				e.printStackTrace(logger);
				throw new AbortException(e.getMessage());
			} else {
				logger.println(e.getMessage());
				e.printStackTrace(logger);
				logger.println("Fail If Task Fail flag is set to false -> continue build regardless of the error");
			}
		}

	}

	/**
	 * Descriptor.
	 *
	 * @author Sergey Oplavin
	 *
	 */
	@Extension
	public static class Descriptor extends BuildStepDescriptor<Builder> {
		private static final StringNotEmptyValidator<Void> NOT_EMPTY_VALIDATOR = new StringNotEmptyValidator<>("Required");
		private static StringStartsWithValidator TEST_PATH_VALIDATOR = new StringStartsWithValidator("Subject\\", "Test Path is required",
				"Test Path must start with Subject\\");
		private static final StringStartsWithValidator TEST_SET_VALIDATOR = new StringStartsWithValidator("Root\\", "Test Set is required",
				"Test Set must start with Root\\");

		public Descriptor() {
			super(RunPcTestBuildStep.class);
			load();
		}

		@Override
		public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Bumblebee HP PC Test Runner";
		}

		public List<String> getAllRunActions() {
			return PostRunAction.getLabels();
		}

		public FormValidation doCheckDomain(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String domain) {
			project.checkPermission(Job.CONFIGURE);
			return NOT_EMPTY_VALIDATOR.validate(domain, null);
		}

		public FormValidation doCheckProject(@AncestorInPath final AbstractProject<?, ?> abstrProj, @QueryParameter final String project) {
			abstrProj.checkPermission(Job.CONFIGURE);
			return NOT_EMPTY_VALIDATOR.validate(project, null);
		}

		public FormValidation doCheckOutputDir(@AncestorInPath final AbstractProject<?, ?> abstrProj, @QueryParameter final String outputDir) {
			abstrProj.checkPermission(Job.CONFIGURE);
			return NOT_EMPTY_VALIDATOR.validate(outputDir, null);
		}

		public FormValidation doCheckTestPlanPath(@AncestorInPath final AbstractProject<?, ?> abstrProj, @QueryParameter final String testPlanPath) {
			abstrProj.checkPermission(Job.CONFIGURE);
			return TEST_PATH_VALIDATOR.validate(testPlanPath, null);
		}

		public FormValidation doCheckTestLabPath(@AncestorInPath final AbstractProject<?, ?> abstrProj, @QueryParameter final String testLabPath) {
			abstrProj.checkPermission(Job.CONFIGURE);
			return TEST_SET_VALIDATOR.validate(testLabPath, null);
		}
	}

}
