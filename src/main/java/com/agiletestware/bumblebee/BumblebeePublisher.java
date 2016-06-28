/**
 *
 */
package com.agiletestware.bumblebee;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.agiletestware.bumblebee.client.api.BulkUpdateParameters;
import com.agiletestware.bumblebee.util.BumblebeeUtils;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Job;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;

/**
 * Main class for the plugin.
 *
 * @author Sergey Oplavin (refactored)
 */
public class BumblebeePublisher extends Recorder {

	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(BumblebeePublisher.class.getName());

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

	@SuppressWarnings("rawtypes")
	@Override
	public boolean perform(final AbstractBuild build, final Launcher launcher, final BuildListener listener) throws InterruptedException, IOException {
		final EnvVars envVars = build.getEnvironment(listener);
		final List<EnvDependentConfigurationWrapper> configWrappers = getConfigWrappers(envVars);

		boolean success = true;
		for (final EnvDependentConfigurationWrapper config : configWrappers) {
			try {
				doBulkUpdate(config, build, launcher, listener);

			} catch (final Throwable ex) {
				listener.getLogger().println(ex.getMessage());
				LOGGER.log(Level.SEVERE, null, ex);
				if (config.getFailIfUploadFailed()) {
					listener.getLogger().println("Bumblebee: Fail if upload flag is set to true -> mark build as failed");
					success = false;
				} else {
					listener.getLogger().println("Bumblebee: Fail if upload flag is set to false -> ignore errors in the build step");
				}

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
	private List<EnvDependentConfigurationWrapper> getConfigWrappers(final EnvVars envVars) {
		final List<BumblebeeConfiguration> configList = getConfigs();
		final List<EnvDependentConfigurationWrapper> configWrappers = new ArrayList<EnvDependentConfigurationWrapper>();
		for (final BumblebeeConfiguration config : configList) {
			configWrappers.add(new EnvDependentConfigurationWrapper(config, envVars));
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
	@SuppressWarnings("rawtypes")
	public void doBulkUpdate(final EnvDependentConfigurationWrapper config, final AbstractBuild build, final Launcher launcher, final BuildListener listener)
			throws Exception {
		final BumblebeeGlobalConfig globalConfig = GlobalConfiguration.all().get(BumblebeeGlobalConfig.class);
		final BulkUpdateParameters params = new BulkUpdateParameters();
		params.setBumbleBeeUrl(globalConfig.getBumblebeeUrl());
		params.setDomain(config.getDomain());
		params.setProject(config.getProjectName());
		params.setEncryptedPassword(globalConfig.getPassword());
		params.setFormat(config.getFormat());
		params.setAlmUserName(globalConfig.getQcUserName());
		params.setAlmUrl(globalConfig.getQcUrl());
		params.setTestPlanDirectory(config.getTestPlan());
		params.setTestLabDirectory(config.getTestLab());
		params.setTestSet(config.getTestSet());
		params.setResultPattern(config.getResultPattern());
		params.setMode(config.getMode());
		params.setTimeOut(globalConfig.getTimeOut());
		params.setCustomProperties(config.getCustomProperties());
		params.setOffline(config.getOffline());

		final PrintStream logger = listener.getLogger();
		final BumblebeeRemoteExecutor remoteExecutor = new BumblebeeRemoteExecutor(BumblebeeUtils.getWorkspace(build), params, listener);
		try {
			launcher.getChannel().call(remoteExecutor);
		} catch (final Throwable e) {
			logger.println(e);
			e.printStackTrace(logger);
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
	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
		private static final String PLUGIN_HELP_PAGE_URI = "/plugin/bumblebee/help/main.html";
		private static final String PLUGIN_DISPLAY_NAME = "Bumblebee  HP  ALM  Uploader";

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

		@SuppressWarnings("rawtypes")
		@Override
		public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public BumblebeePublisher newInstance(final StaplerRequest req, final JSONObject formData) throws FormException {
			final List<BumblebeeConfiguration> configs = req.bindParametersToList(BumblebeeConfiguration.class, "Bumblebee.bumblebeeConfiguration.");

			return new BumblebeePublisher(configs);
		}


		public FormValidation doCheckDomain(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String domain)
				throws IOException, ServletException {
			project.checkPermission(Job.CONFIGURE);
			return BumblebeeUtils.validateRequiredField(domain);
		}

		public FormValidation doCheckProjectName(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String projectName)
				throws IOException, ServletException {
			project.checkPermission(Job.CONFIGURE);
			return BumblebeeUtils.validateRequiredField(projectName);
		}

		public FormValidation doCheckTestPlan(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String testPlan)
				throws IOException, ServletException {
			project.checkPermission(Job.CONFIGURE);
			return BumblebeeUtils.validateTestPlan(testPlan);
		}

		public FormValidation doCheckTestLab(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String testLab)
				throws IOException, ServletException {
			project.checkPermission(Job.CONFIGURE);
			return BumblebeeUtils.validateTestLab(testLab);
		}

		public FormValidation doCheckTestSet(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String testSet)
				throws IOException, ServletException {
			project.checkPermission(Job.CONFIGURE);
			return BumblebeeUtils.validateTestSet(testSet);
		}

		public FormValidation doCheckFormat(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String format)
				throws IOException, ServletException {
			project.checkPermission(Job.CONFIGURE);
			return BumblebeeUtils.validateRequiredField(format);
		}

	}

}
