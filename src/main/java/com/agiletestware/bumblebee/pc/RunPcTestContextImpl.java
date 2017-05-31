package com.agiletestware.bumblebee.pc;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import com.agiletestware.bumblebee.BumblebeeGlobalConfig;
import com.agiletestware.bumblebee.client.pc.RunPcTestContext;
import com.agiletestware.bumblebee.client.utils.action.RetrySettings;

import hudson.FilePath;

/**
 * Context for running PC test.
 *
 * @author Sergey Oplavin
 *
 */
public class RunPcTestContextImpl implements RunPcTestContext, Serializable {

	/** . */
	private static final long serialVersionUID = 3758920973453470489L;
	private final StartRunParameters parameters;
	private final PcConnectionParameters connectionParameters;
	private final RetrySettings retrySettings;
	private final int pollingInterval;
	private final File outputDir;
	private final String bumblebeeUrl;
	private final int connectionTimeOut;

	public RunPcTestContextImpl(final RunPcTestBuildStep buildStep, final BumblebeeGlobalConfig globalConfig, final FilePath workspace) {
		this.parameters = createStartRunParameters(buildStep);
		this.connectionParameters = new PcConnectionParametersImpl(globalConfig, buildStep.getDomain(), buildStep.getProject());
		this.bumblebeeUrl = globalConfig.getBumblebeeUrl();
		this.connectionTimeOut = (int) TimeUnit.MINUTES.toSeconds(getTimeout(buildStep, globalConfig));
		this.retrySettings = new RetrySettings(buildStep.getRetryCount(), buildStep.getRetryInterval(), buildStep.getRetryIntervalMultiplier());
		this.pollingInterval = buildStep.getPollingInterval();
		this.outputDir = new File(workspace.getRemote(),
				buildStep.getOutputDir());
	}

	private StartRunParameters createStartRunParameters(final RunPcTestBuildStep buildStep) {
		final StartRunParameters parameters = new StartRunParameters();
		parameters.setPostRunAction(buildStep.getPostRunAction());
		parameters.setTestPath(buildStep.getTestPlanPath());
		parameters.setTestSetPath(buildStep.getTestLabPath());
		parameters.setTimeslotDuration(buildStep.getTimeslotDuration());
		parameters.setVudsMode(buildStep.isVudsMode());
		return parameters;
	}

	private int getTimeout(final RunPcTestBuildStep buildStep, final BumblebeeGlobalConfig globalConfig) {
		final int timeout = buildStep.getTimeout();
		return timeout > 0 ? timeout : globalConfig.getPcTimeOut();
	}

	@Override
	public String getBumblebeeUrl() {
		return bumblebeeUrl;
	}

	@Override
	public int getTimeout() {
		return connectionTimeOut;
	}

	@Override
	public PcConnectionParameters getConnectionParameters() {
		return connectionParameters;
	}

	@Override
	public StartRunParameters getStartRunParameters() {
		return parameters;
	}

	@Override
	public File getOutputDir() {
		return outputDir;
	}

	@Override
	public RetrySettings getRetrySettings() {
		return retrySettings;
	}

	@Override
	public int getPollingInterval() {
		return pollingInterval;
	}

	private static class PcConnectionParametersImpl implements PcConnectionParameters, Serializable {

		private final String project;
		private final String domain;
		private final String almUrl;
		private final String user;
		private final String pcUrl;
		private final String password;

		public PcConnectionParametersImpl(final BumblebeeGlobalConfig globalConfig, final String domain, final String project) {
			// have to copy all values into separate fields, as
			// BumblebeeGlobalConfig is not Serializable -> break build on
			// slave.
			this.project = project;
			this.domain = domain;
			this.almUrl = globalConfig.getQcUrl();
			this.user = globalConfig.getQcUserName();
			this.pcUrl = globalConfig.getPcUrl();
			this.password = globalConfig.getPassword();
		}

		/** . */
		private static final long serialVersionUID = 1L;

		@Override
		public String getUser() {
			return user;
		}

		@Override
		public String getProject() {
			return project;
		}

		@Override
		public String getPcUrl() {
			return pcUrl;
		}

		@Override
		public String getPassword() {
			return password;
		}

		@Override
		public String getDomain() {
			return domain;
		}

		@Override
		public String getAlmUrl() {
			return almUrl;
		}

	}

}
