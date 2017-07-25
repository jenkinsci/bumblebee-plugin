package com.agiletestware.bumblebee.testset;

import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.agiletestware.bumblebee.BumblebeeGlobalConfig;
import com.agiletestware.bumblebee.client.api.AddTestToSetParameters;
import com.agiletestware.bumblebee.util.BumblebeeUtils;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
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
 * Step for adding test to test set.
 *
 * @author Sergey Oplavin
 *
 */
public class AddTestToSetStep extends Builder implements SimpleBuildStep {

	private final String domain;
	private final String project;
	private final String testPlanPath;
	private final String testSetPath;

	/**
	 * Constructor.
	 *
	 * @param domain
	 *            domain
	 * @param project
	 *            project
	 * @param testPlanPath
	 *            path to a test in test plan
	 * @param testSetPath
	 *            path to a test set
	 */
	@DataBoundConstructor
	public AddTestToSetStep(final String domain, final String project, final String testPlanPath, final String testSetPath) {
		this.domain = domain;
		this.project = project;
		this.testPlanPath = testPlanPath;
		this.testSetPath = testSetPath;
	}

	@Override
	public void perform(final Run<?, ?> run, final FilePath workspace, final Launcher launcher, final TaskListener listener)
			throws InterruptedException, IOException {
		final PrintStream logger = listener.getLogger();
		logger.println("Start adding tests from " + testPlanPath + " to " + testSetPath);
		final BumblebeeGlobalConfig globalConfig = GlobalConfiguration.all().get(BumblebeeGlobalConfig.class);
		final AddTestToSetParameters params = new AddTestToSetEnvSpecificParameters(AddTestToSetParametersFactory.THE_INSTANCE.create(this, globalConfig),
				run.getEnvironment(listener));
		logParameters(params, logger);
		try {
			launcher.getChannel().call(new AddTestToSetCallable(params, globalConfig.getTimeOut()));
			logger.println("Tests were added");
		} catch (final Exception e) {
			e.printStackTrace(logger);
			throw new AbortException("Could not add tests " + testPlanPath + " to " + testSetPath + ". Reason: " + e.getMessage());
		}
	}

	private void logParameters(final AddTestToSetParameters parameters, final PrintStream logger) {
		logger.println("Parameters:");
		logger.println("Bumblebee URL: " + parameters.getBumbleBeeUrl());
		logger.println("HP ALM URL: " + parameters.getAlmUrl());
		logger.println("HP ALM User: " + parameters.getAlmUserName());
		logger.println("HP ALM Domain: " + parameters.getDomain());
		logger.println("HP ALM Project: " + parameters.getProject());
		logger.println("Test Plan Path: " + parameters.getTestPlanPath());
		logger.println("Test Set Path: " + parameters.getTestSetPath());
	}

	/**
	 * @return domain.
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @return project.
	 */
	public String getProject() {
		return project;
	}

	/**
	 * @return test plan path.
	 */
	public String getTestPlanPath() {
		return testPlanPath;
	}

	/**
	 * @return test set path.
	 */
	public String getTestSetPath() {
		return testSetPath;
	}

	@Extension
	public static class Descriptor extends BuildStepDescriptor<Builder> {

		public Descriptor() {
			super(AddTestToSetStep.class);
			load();
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Bumblebee: Add Test to Test Set";
		}

		public FormValidation doCheckDomain(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String domain) {
			project.checkPermission(Job.CONFIGURE);
			return BumblebeeUtils.validateRequiredField(domain);
		}

		public FormValidation doCheckProject(@AncestorInPath final AbstractProject<?, ?> abstrProject, @QueryParameter final String project) {
			abstrProject.checkPermission(Job.CONFIGURE);
			return BumblebeeUtils.validateRequiredField(project);
		}

		public FormValidation doCheckTestPlanPath(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String testPlanPath) {
			project.checkPermission(Job.CONFIGURE);
			if (StringUtils.isEmpty(testPlanPath)) {
				return FormValidation.error("Required");
			}
			if (!testPlanPath.startsWith("Subject\\")) {
				return FormValidation.error("Test set must start with Subject\\");
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckTestSetPath(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String testSetPath) {
			project.checkPermission(Job.CONFIGURE);
			if (StringUtils.isEmpty(testSetPath)) {
				return FormValidation.error("Required");
			}
			if (!testSetPath.startsWith("Root\\")) {
				return FormValidation.error("Test set must start with Root\\");
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckOutputDirPath(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String checkOutputDirPath) {
			project.checkPermission(Job.CONFIGURE);
			return BumblebeeUtils.validateRequiredField(checkOutputDirPath);
		}

	}

}
