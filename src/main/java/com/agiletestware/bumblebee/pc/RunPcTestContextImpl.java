package com.agiletestware.bumblebee.pc;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import com.agiletestware.bumblebee.BumblebeeGlobalConfig;
import com.agiletestware.bumblebee.client.api.BumblebeeApi;
import com.agiletestware.bumblebee.client.api.BumblebeeApiProvider;
import com.agiletestware.bumblebee.client.pc.RunPcTestContext;
import com.agiletestware.bumblebee.client.utils.action.RetrySettings;
import com.agiletestware.bumblebee.encryption.CustomSecret;

import hudson.FilePath;
import hudson.Util;

/**
 * Context for running PC test.
 *
 * @author Sergey Oplavin
 *
 */
public class RunPcTestContextImpl implements RunPcTestContext, Serializable {

	private static final double DEFAULT_RETRY_INTERVAL_MULTIPLY_FACTORY = 1.0;
	private static final long serialVersionUID = 3758920973453470489L;
	private final StartRunParameters parameters;
	private final PcConnectionParameters connectionParameters;
	private final RetrySettings genericRetrySettings;
	private final RetrySettings collateAnalyzeRetrySettings;
	private final int pollingInterval;
	private final File outputDir;
	private final String bumblebeeUrl;
	private final int connectionTimeOut;
	private final boolean trustSelfSignedCertificate;

	public RunPcTestContextImpl(final RunPcTestBuildStep buildStep, final BumblebeeGlobalConfig globalConfig, final FilePath workspace,
			final CustomSecret customSecret, final BumblebeeApiProvider bumblebeeApiProvider) throws Exception {
		this.parameters = createStartRunParameters(buildStep);
		this.connectionParameters = new PcConnectionParametersImpl(globalConfig, buildStep.getDomain(), buildStep.getProject(), buildStep.getAlmUser(),
				buildStep.getAlmPassword(), customSecret, bumblebeeApiProvider);
		this.bumblebeeUrl = globalConfig.getBumblebeeUrl();
		this.connectionTimeOut = (int) TimeUnit.MINUTES.toSeconds(getTimeout(buildStep, globalConfig));
		this.genericRetrySettings = new RetrySettings(buildStep.getRetryCount(), buildStep.getRetryInterval(), buildStep.getRetryIntervalMultiplier());
		this.collateAnalyzeRetrySettings = createCollateAnalyzeRetrySettings(buildStep);
		this.pollingInterval = buildStep.getPollingInterval();
		this.outputDir = new File(workspace.getRemote(), buildStep.getOutputDir());
		this.trustSelfSignedCertificate = globalConfig.isTrustSelfSignedCerts();
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
	public RetrySettings getGenericRetrySettings() {
		return genericRetrySettings;
	}

	@Override
	public RetrySettings getCollateAnalyzeRetrySettings() {
		return collateAnalyzeRetrySettings;
	}

	@Override
	public int getPollingInterval() {
		return pollingInterval;
	}

	@Override
	public boolean isTrustSelfSignedCerts() {
		return trustSelfSignedCertificate;
	}

	private RetrySettings createCollateAnalyzeRetrySettings(final RunPcTestBuildStep buildStep) {
		final boolean enableRetry = buildStep.isRetryCollateAndAnalysisFlag();
		return enableRetry ? new RetrySettings(buildStep.getRetryCollateAndAnalysisAttempts(), buildStep.getRetryCollateAndAnalysisInterval(),
				DEFAULT_RETRY_INTERVAL_MULTIPLY_FACTORY)
				: new RetrySettings(0, 0, DEFAULT_RETRY_INTERVAL_MULTIPLY_FACTORY);
	}

	private static class PcConnectionParametersImpl implements PcConnectionParameters, Serializable {

		private static final long serialVersionUID = 1L;
		private final CustomSecret customSecret;
		private final BumblebeeApiProvider bumblebeeApiProvider;
		private final String project;
		private final String domain;
		private final String almUrl;
		private final String user;
		private final String pcUrl;
		private final String password;
		private final boolean trustSelfSignedCertificate;

		public PcConnectionParametersImpl(final BumblebeeGlobalConfig globalConfig, final String domain, final String project, final String almUser,
				final String almPassword, final CustomSecret customSecret, final BumblebeeApiProvider bumblebeeApiProvider) throws Exception {
			// have to copy all values into separate fields, as
			// BumblebeeGlobalConfig is not Serializable -> break build on
			// slave.
			this.project = project;
			this.domain = domain;
			this.almUrl = globalConfig.getQcUrl();
			final String almUserFromStep = Util.fixEmptyAndTrim(almUser);
			this.customSecret = customSecret;
			this.bumblebeeApiProvider = bumblebeeApiProvider;
			this.user = StringUtils.isNotEmpty(almUserFromStep) ? almUserFromStep : globalConfig.getQcUserName();
			this.pcUrl = globalConfig.getPcUrl();
			this.password = getEncryptedAlmPassword(globalConfig, almPassword);
			this.trustSelfSignedCertificate = globalConfig.isTrustSelfSignedCerts();
		}

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

		private String getEncryptedAlmPassword(final BumblebeeGlobalConfig globalConfig, final String almPasswordFromStep) throws Exception {
			if (StringUtils.isEmpty(almPasswordFromStep)) {
				return globalConfig.getPassword();
			}
			final String bumblebeeUrl = globalConfig.getBumblebeeUrl();
			if (StringUtils.isEmpty(bumblebeeUrl)) {
				throw new IllegalStateException("Bumblebee URL is not defined in Bumblebee Global Configuration");
			}
			try (final BumblebeeApi bumblebeeApi = bumblebeeApiProvider.provide(bumblebeeUrl, (int) TimeUnit.MINUTES.toSeconds(globalConfig.getTimeOut()),
					trustSelfSignedCertificate)) {
				return bumblebeeApi.getEncryptedPassword(customSecret.getPlainText(almPasswordFromStep));
			}
		}

	}

}
