package com.agiletestware.bumblebee.testset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.agiletestware.bumblebee.BumblebeeGlobalConfig;
import com.agiletestware.bumblebee.client.api.AlmRunMode;
import com.agiletestware.bumblebee.client.testrunner.TestSetRunnerParameters;
import com.agiletestware.bumblebee.client.testrunner.TestSetRunnerParametersImpl;
import com.agiletestware.bumblebee.tracking.ClientType;
import com.agiletestware.bumblebee.util.BumblebeeUtils;

import hudson.AbortException;
import hudson.EnvVars;
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
 * Build step to trigger run of test sets in HP ALM.
 *
 * @author Sergey Oplavin
 *
 */
public class RunTestSetBuildStep extends Builder implements SimpleBuildStep {
	private static final String TEST_SET_DELIMETER = "\r\n";
	private final String domain;
	private final String project;
	private final AlmRunMode runMode;
	private final String host;
	private final String testSets;
	private final String outputDirPath;
	private final int timeOut;
	private final boolean failRunInAlmIfAnalysisFail;

	/**
	 * Constructor.
	 *
	 * @param domain
	 *            HP ALM domain.
	 * @param project
	 *            HP ALM project.
	 * @param runMode
	 *            run mode, see {@link AlmRunMode}.
	 * @param host
	 *            host on which test set shall be executed.
	 * @param testSets
	 *            string containing paths to test sets in HP ALM (e.g.
	 *            Root\test\testset). Paths are separated by new line.
	 * @param outputDirPath
	 *            output directory for test set execution results.
	 * @param timeOut
	 *            execution timeout in minutes.
	 * @param failRunInAlmIfAnalysisFail
	 *            the fail run in ALM if analysis fail.
	 */
	@DataBoundConstructor
	public RunTestSetBuildStep(final String domain, final String project, final String runMode, final String host, final String testSets,
			final String outputDirPath, final int timeOut, final boolean failRunInAlmIfAnalysisFail) {
		this.domain = domain;
		this.project = project;
		this.runMode = AlmRunMode.valueOf(runMode.toUpperCase());
		this.host = host;
		this.testSets = testSets;
		this.outputDirPath = outputDirPath;
		this.timeOut = timeOut;
		this.failRunInAlmIfAnalysisFail = failRunInAlmIfAnalysisFail;
	}

	@Override
	public void perform(final Run<?, ?> run, final FilePath workspace, final Launcher launcher, final TaskListener listener)
			throws InterruptedException, IOException {
		listener.getLogger().println("Bumblebee: running test sets");
		try {
			final RunTestSetTask task = new RunTestSetTask(listener, run.getExecutor().getOwner().getNode().getRootPath(), workspace,
					createParameters(run.getEnvironment(listener)));
			final int returnCode = launcher.getChannel().call(task);
			if (returnCode != 0) {
				throw new AbortException("Test set execution failed. See logs for details.");
			}
		} catch (final IOException e) {
			throw e;
		} catch (final Exception e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	private TestSetRunnerParameters createParameters(final EnvVars envVars) {
		final BumblebeeGlobalConfig globalConfig = GlobalConfiguration.all().get(BumblebeeGlobalConfig.class);
		final TestSetRunnerParameters params = new TestSetRunnerParametersImpl();
		globalConfig.populateBaseParameters(params);
		params.setDomain(getDomain());
		params.setProject(getProject());
		params.setRunMode(getRunMode());
		params.setHost(getHost());
		params.setOutputDirPath(getOutputDirPath());
		params.setTimeOut(getTimeOut());
		params.setClientType(ClientType.JENKINS);
		final String sets = getTestSets();
		final List<String> setsList = new ArrayList<>();
		final StringTokenizer tokenizer = new StringTokenizer(sets, TEST_SET_DELIMETER);
		while (tokenizer.hasMoreTokens()) {
			setsList.add(tokenizer.nextToken());
		}
		params.setTestSets(setsList);
		return new TestSetEnvSpecificParameters(params, envVars);
	}

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @return the project
	 */
	public String getProject() {
		return project;
	}

	/**
	 * @return the runMode
	 */
	public AlmRunMode getRunMode() {
		return runMode;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return the testSets
	 */
	public String getTestSets() {
		return testSets;
	}

	/**
	 * @return the outputDir
	 */
	public String getOutputDirPath() {
		return outputDirPath;
	}

	/**
	 * @return the timeOut
	 */
	public int getTimeOut() {
		return timeOut;
	}

	/**
	 * @return true, if is fail run in ALM if analysis fail.
	 */
	public boolean isFailRunInAlmIfAnalysisFail() {
		return failRunInAlmIfAnalysisFail;
	}

	/**
	 * Descriptor for {@link RunTestSetBuildStep}.
	 *
	 * @author Sergey Oplavin
	 *
	 */
	@Extension
	public static class Descriptor extends BuildStepDescriptor<Builder> {

		public Descriptor() {
			super(RunTestSetBuildStep.class);
			load();
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Bumblebee HP ALM Test Set Runner";
		}

		public AlmRunMode[] getRunModes() {
			return AlmRunMode.values();
		}

		public FormValidation doCheckDomain(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String domain) {
			project.checkPermission(Job.CONFIGURE);
			return BumblebeeUtils.validateRequiredField(domain);
		}

		public FormValidation doCheckProject(@AncestorInPath final AbstractProject<?, ?> abstrProject, @QueryParameter final String project) {
			abstrProject.checkPermission(Job.CONFIGURE);
			return BumblebeeUtils.validateRequiredField(project);
		}

		public FormValidation doCheckHost(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String host,
				@QueryParameter final AlmRunMode runMode)
						throws IOException, ServletException {
			project.checkPermission(Job.CONFIGURE);
			if (AlmRunMode.LOCAL == runMode || AlmRunMode.SCHEDULED == runMode || StringUtils.isNotEmpty(host)) {
				return FormValidation.ok();
			}
			return FormValidation.error("Host cannot be empty when ALM Run Mode is " + runMode);
		}

		public FormValidation doCheckTestSets(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String testSets) {
			project.checkPermission(Job.CONFIGURE);
			if (StringUtils.isEmpty(testSets)) {
				return FormValidation.error("Required");
			}
			final StringTokenizer tokenizer = new StringTokenizer(testSets, TEST_SET_DELIMETER);
			while (tokenizer.hasMoreTokens()) {
				final String testSet = tokenizer.nextToken();
				if (!testSet.startsWith("Root\\")) {
					return FormValidation.error("Test set must start with Root\\");
				}
			}
			return FormValidation.ok();
		}

		public FormValidation doCheckOutputDirPath(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String checkOutputDirPath) {
			project.checkPermission(Job.CONFIGURE);
			return BumblebeeUtils.validateRequiredField(checkOutputDirPath);
		}

	}
}
