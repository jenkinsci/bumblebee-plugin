/**
 *
 */
package com.agiletestware.bumblebee;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.agiletestware.bumblebee.BumblebeeRemoteExecutor.Parameters;
import com.agiletestware.bumblebee.api.BumbleBeeApi;
import com.agiletestware.bumblebee.util.BumblebeeUtils;
import com.agiletestware.bumblebee.util.ThreadLocalMessageFormat;

/**
 * Main class for the plugin.
 *
 * @author Sergey Oplavin (refactored)
 */
public class BumblebeePublisher extends Recorder {

	/** Descriptor instance. */
	@Extension
	public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

	/** Logger. */
	private static final Logger LOGGER = Logger
			.getLogger(BumblebeePublisher.class.getName());

	/** Message format for log messages. */
	private static final ThreadLocalMessageFormat LOG_FORMAT = new ThreadLocalMessageFormat(
			"\nQC  Configuration:\nBumblebeeUrl: {0}" + "\nPassword: *******"
					+ "\nQCURL : {1}" + "\nQC UserName: {2}" + "\n Domain: {3}"
					+ "\nResults Pattern: {4}" + "\n Format: {5}"
					+ "\nProjectName: {6}" + "\nTestSetName: {7}"
					+ "\nTestLab: {8}" + "\nTestPlanDirectory:  {9}"
					+ "\nTest Type: {10}" + "\nCustom Properties: {11}\n");

	/** Configurations. */
	private final BumblebeeConfiguration[] configs;

	/**
	 * Constructor.
	 *
	 * @param configs
	 *            Configurations which are set for current job.
	 */
	public BumblebeePublisher(final BumblebeeConfiguration... configs) {
		this.configs = configs;
	}

	/**
	 * Constructor.
	 *
	 * @param configs
	 *            List of configurations.
	 */
	public BumblebeePublisher(final Collection<BumblebeeConfiguration> configs) {
		this(configs.toArray(new BumblebeeConfiguration[configs.size()]));
	}

	/**
	 * This method will return all the tasks
	 *
	 * @return List<TaskProperties>
	 */
	public List<BumblebeeConfiguration> getConfigs() {
		if (configs == null) {
			return new ArrayList<BumblebeeConfiguration>();
		} else {
			return Arrays.asList(configs);
		}
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public boolean perform(final AbstractBuild build, final Launcher launcher,
			final BuildListener listener) throws InterruptedException,
			IOException {
		final EnvVars envVars = build.getEnvironment(listener);
		final List<EnvDependentConfigurationWrapper> configWrappers = getConfigWrappers(envVars);

		boolean success = true;
		for (final EnvDependentConfigurationWrapper config : configWrappers) {
			listener.getLogger().println(
					LOG_FORMAT.format(DESCRIPTOR.bumblebeeUrl,
							DESCRIPTOR.qcUrl, DESCRIPTOR.qcUserName,
							config.getDomain(), config.getResultPattern(),
							config.getFormat(), config.getProjectName(),
							config.getProjectName(), config.getTestLab(),
							config.getTestPlan(), config.getFormat(),
							config.getCustomProperties()));
			try {
				doBulkUpdate(config, build, launcher, listener);

			} catch (final Exception ex) {
				listener.getLogger().println("\n" + ex.getMessage());
				LOGGER.log(Level.SEVERE, null, ex);
				success = false;

			}
		}
		return success;
	}

	/**
	 *
	 * @param envVars
	 *            Env variables map.
	 * @return A list of configuration wrappers which allow consumer to resolve
	 *         env variables in configuration values.
	 */
	private List<EnvDependentConfigurationWrapper> getConfigWrappers(
			final EnvVars envVars) {
		final List<BumblebeeConfiguration> configList = getConfigs();
		final List<EnvDependentConfigurationWrapper> configWrappers = new ArrayList<>();
		for (final BumblebeeConfiguration config : configList) {
			configWrappers.add(new EnvDependentConfigurationWrapper(config,
					envVars));
		}
		return configWrappers;
	}

	/**
	 * Send data to bumblebee server.
	 *
	 * @param config
	 *            Config wrapper
	 * @param bulkURL
	 *            The URL to use
	 * @param build
	 *            what it says
	 * @param launcher
	 *            what it says
	 * @param listener
	 *            what it says
	 * @throws Exception
	 */
	public void doBulkUpdate(final EnvDependentConfigurationWrapper config,
			final AbstractBuild build, final Launcher launcher,
			final BuildListener listener) throws Exception {
		listener.getLogger().println("Invoking the remote executor");
		final Parameters params = new Parameters();
		params.setBumbleBeeUrl(DESCRIPTOR.bumblebeeUrl);
		params.setDomain(config.getDomain());
		params.setProject(config.getProjectName());
		params.setEncryptedPassword(DESCRIPTOR.password);
		params.setFormat(config.getFormat());
		params.setQcUserName(DESCRIPTOR.qcUserName);
		params.setQcUrl(DESCRIPTOR.qcUrl);
		params.setTestplandirectory(config.getTestPlan());
		params.setTestlabdirectory(config.getTestLab());
		params.setTestSet(config.getTestSet());
		params.setResultPattern(config.getResultPattern());
		params.setMode(config.getMode());
		params.setTimeOut(DESCRIPTOR.timeOut);
		params.setCustomProperties(config.getCustomProperties());
		final BumblebeeRemoteExecutor remoteExecutor = new BumblebeeRemoteExecutor(
				BumblebeeUtils.getWorkspace(build), params);
		try {
			listener.getLogger().println(
					launcher.getChannel().call(remoteExecutor));
		} catch (final RemoteExecutorException e) {
			listener.getLogger().println(e.getRemoteExecutionLog());
			throw e;
		}
	}

	/**
	 * Descriptor for bumblebee plugin. It is needed to store global
	 * configuration.
	 *
	 * @author Sergey Oplavin (oplavin.sergei@gmail.com) (refactored)
	 *
	 */
	public static final class DescriptorImpl extends
			BuildStepDescriptor<Publisher> {
		private static final String PLUGIN_HELP_PAGE_URI = "/plugin/bumblebee/help/main.html";
		private static final String PLUGIN_DISPLAY_NAME = "Bumblebee  HP  ALM  Uploader";
		private String bumblebeeUrl;
		private String qcUserName;
		private String password;
		private String qcUrl;
		private int timeOut;
		private BumbleBeeApi bmapi;

		/**
		 * Constructor.
		 */
		public DescriptorImpl() {
			super(BumblebeePublisher.class);
			load();
		}

		@Override
		public String getDisplayName() {
			return PLUGIN_DISPLAY_NAME;
		}

		@Override
		public String getHelpFile() {
			return PLUGIN_HELP_PAGE_URI;
		}

		@Override
		public boolean isApplicable(
				final Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public BumblebeePublisher newInstance(final StaplerRequest req,
				final JSONObject formData) throws FormException {
			final List<BumblebeeConfiguration> configs = req
					.bindParametersToList(BumblebeeConfiguration.class,
							"Bumblebee.bumblebeeConfiguration.");

			return new BumblebeePublisher(configs);
		}

		@Override
		public boolean configure(final StaplerRequest req,
				final JSONObject formData) throws FormException {
			return super.configure(req, formData);
		}

		// Used by global.jelly to authenticate User key
		public FormValidation doSaveConnection(
				@QueryParameter("bumblebeeUrl") final String bumblebeeUrl,
				@QueryParameter("qcUrl") final String qcUrl,
				@QueryParameter("qcUserName") final String qcUserName,
				@QueryParameter("password") final String password,
				@QueryParameter("timeOut") final int timeOut) {
			try {
				this.qcUserName = StringUtils.trim(qcUserName);
				this.qcUrl = StringUtils.trim(qcUrl);
				this.bumblebeeUrl = StringUtils.trim(bumblebeeUrl);
				this.timeOut = timeOut;
				bmapi = new BumbleBeeApi(this.bumblebeeUrl, this.timeOut);
				// Set password only if old value is null/empty/blank OR if new
				// value is not equal to old
				if (StringUtils.isBlank(this.password)
						|| !this.password.equals(password)) {
					this.password = bmapi.getEncryptedPassword(StringUtils
							.trim(password));
				}
				bmapi.validateLicense();
				save();
			} catch (final Exception e) {
				LOGGER.log(Level.SEVERE, null, e);
				return FormValidation.error("FAILED: " + e.getMessage());
			}
			return FormValidation.ok("Configuration  Saved");
		}

		public String getBumblebeeUrl() {
			return this.bumblebeeUrl;
		}

		public String getQcUserName() {
			return this.qcUserName;
		}

		public String getQcUrl() {
			return this.qcUrl;
		}

		public String getPassword() {
			return this.password;
		}

		public int getTimeOut() {
			return this.timeOut;
		}

		public FormValidation doCheckDomain(
				@AncestorInPath final AbstractProject<?, ?> project,
				@QueryParameter final String domain) throws IOException,
				ServletException {
			project.checkPermission(Job.CONFIGURE);
			return BumblebeeUtils.validateRequiredField(domain);
		}

		public FormValidation doCheckProjectName(
				@AncestorInPath final AbstractProject<?, ?> project,
				@QueryParameter final String projectName) throws IOException,
				ServletException {
			project.checkPermission(Job.CONFIGURE);
			return BumblebeeUtils.validateRequiredField(projectName);
		}

		public FormValidation doCheckTestPlan(
				@AncestorInPath final AbstractProject<?, ?> project,
				@QueryParameter final String testPlan) throws IOException,
				ServletException {
			project.checkPermission(Job.CONFIGURE);
			return BumblebeeUtils.validateTestPlan(testPlan);
		}

		public FormValidation doCheckTestLab(
				@AncestorInPath final AbstractProject<?, ?> project,
				@QueryParameter final String testLab) throws IOException,
				ServletException {
			project.checkPermission(Job.CONFIGURE);
			return BumblebeeUtils.validateTestLab(testLab);
		}

		public FormValidation doCheckTestSet(
				@AncestorInPath final AbstractProject<?, ?> project,
				@QueryParameter final String testSet) throws IOException,
				ServletException {
			project.checkPermission(Job.CONFIGURE);
			return BumblebeeUtils.validateTestSet(testSet);
		}

		public FormValidation doCheckFormat(
				@AncestorInPath final AbstractProject<?, ?> project,
				@QueryParameter final String format) throws IOException,
				ServletException {
			project.checkPermission(Job.CONFIGURE);
			return BumblebeeUtils.validateRequiredField(format);
		}

		public FormValidation doCheckbumblebeeUrl(
				@AncestorInPath final AbstractProject<?, ?> project,
				@QueryParameter final String bumblebeeUrl) throws IOException,
				ServletException {
			return BumblebeeUtils.validatebumblebeeUrl(bumblebeeUrl);
		}

		public FormValidation doCheckqcUrl(
				@AncestorInPath final AbstractProject<?, ?> project,
				@QueryParameter final String qcUrl) throws IOException,
				ServletException {
			return BumblebeeUtils.validateqcUrl(qcUrl);
		}

		public FormValidation doCheckqcUserName(
				@AncestorInPath final AbstractProject<?, ?> project,
				@QueryParameter final String qcUserName) throws IOException,
				ServletException {
			return BumblebeeUtils.validateRequiredField(qcUserName);
		}

	}

}
