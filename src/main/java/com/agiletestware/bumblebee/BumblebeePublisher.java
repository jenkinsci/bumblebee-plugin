/**
 *
 */
package com.agiletestware.bumblebee;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.agiletestware.bumblebee.client.api.BulkUpdateParameters;
import com.agiletestware.bumblebee.client.api.BulkUpdateParametersImpl;
import com.agiletestware.bumblebee.util.BumblebeeUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

/**
 * Main class for the plugin.
 *
 * @author Sergey Oplavin (refactored)
 */
public class BumblebeePublisher extends Recorder implements SimpleBuildStep {

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
	@DataBoundConstructor
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
			return Collections.emptyList();
		} else {
			return Arrays.asList(configs);
		}
	}

	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	@Override
	public void perform(final Run<?, ?> run, final FilePath workspace, final Launcher launcher, final TaskListener listener)
			throws InterruptedException, IOException {
		boolean success = true;
		for (final BumblebeeConfiguration config : getConfigs()) {
			try {
				doBulkUpdate(config, run, workspace, launcher, listener);
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
		if (!success) {
			throw new AbortException("Bumblebee: Fail if upload flag is set to true -> mark build as failed");
		}
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
	public void doBulkUpdate(final BumblebeeConfiguration config, final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
			final TaskListener listener)
					throws Exception {
		@SuppressFBWarnings(justification = "Objects.requreNonNull does null check but FindBugs still consider this as potential NPE", value = "NP")
		final BumblebeeGlobalConfig globalConfig = Objects.requireNonNull(GlobalConfiguration.all().get(BumblebeeGlobalConfig.class),
				"Bumblebee Global Configuration is null!");
		final BulkUpdateParameters params = new BulkUpdateParametersImpl();
		globalConfig.populateBaseParameters(params);
		params.setDomain(config.getDomain());
		params.setProject(config.getProjectName());
		params.setFormat(config.getFormat());
		params.setTestPlanDirectory(config.getTestPlan());
		params.setTestLabDirectory(config.getTestLab());
		params.setTestSet(config.getTestSet());
		params.setResultPattern(config.getResultPattern());
		params.setMode(config.getMode());
		params.setTimeOut(globalConfig.getTimeOut());
		params.setCustomProperties(config.getCustomProperties());
		params.setOffline(config.isOffline());
		params.setTrustSelfSignedCerts(globalConfig.isTrustSelfSignedCerts());
		params.setDefectCreatePolicy(config.getDefectCreatePolicy());
		params.setDefectCreateStatus(config.getDefectCreateStatus());
		params.setDefectSeverity(config.getDefectSeverity());
		params.setDefectReopenStatus(config.getDefectReopenStatus());
		params.setDefectResolvePolicy(config.getDefectResolvePolicy());
		params.setDefectResolveStatus(config.getDefectResolveStatus());
		params.setDefectCreateProperties(config.getDefectCreateProperties());
		params.setDefectReopenProperties(config.getDefectReopenProperties());
		params.setDefectResolveProperties(config.getDefectResolveProperties());

		final PrintStream logger = listener.getLogger();
		final BumblebeeRemoteExecutor remoteExecutor = new BumblebeeRemoteExecutor(workspace,
				new BulkUpdateEnvSpecificParameters(params, run.getEnvironment(listener)), listener);
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
		private static final List<String> DEFECT_CREATE_POLICIES = Collections.unmodifiableList(Arrays.asList("Create", "Reopen"));
		private static final List<String> DEFECT_RESOLVE_POLICIES = Collections.unmodifiableList(Arrays.asList("Close"));

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

		@SuppressWarnings("deprecation")
		@Override
		public BumblebeePublisher newInstance(@CheckForNull final StaplerRequest req, @Nonnull final JSONObject formData) throws FormException {
			return new BumblebeePublisher(
					Objects.requireNonNull(req, "Req is null").bindParametersToList(BumblebeeConfiguration.class, "Bumblebee.bumblebeeConfiguration."));
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

		public List<String> getDefectCreatePolicies() {
			return DEFECT_CREATE_POLICIES;
		}

		public List<String> getDefectResolvePolicies() {
			return DEFECT_RESOLVE_POLICIES;
		}

	}
}
