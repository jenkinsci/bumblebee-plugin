package com.agiletestware.bumblebee.uft;

import org.apache.commons.lang.StringUtils;

import com.agiletestware.bumblebee.BumblebeeGlobalConfig;
import com.agiletestware.bumblebee.client.uftrunner.UftRunnerParameters;
import com.agiletestware.bumblebee.client.uftrunner.UftRunnerParametersImpl;
import com.agiletestware.bumblebee.tracking.ClientType;

import hudson.EnvVars;

/**
 * Factory to produce {@link UftRunnerParameters} instances.
 *
 * @author Sergey Oplavin
 *
 */
public enum UftRunnerParametersFactory {
	THE_INSTANCE;

	/**
	 * The name of environment variable to set path to UFT Batch Runner.
	 */
	public static final String UFT_RUNNER_ENV_VAR_NAME = "UFT_RUNNER";

	/**
	 * Create instance of {@link UftRunnerParameters}.
	 *
	 * @param step
	 *            step containing information about test path and output
	 *            directory.
	 * @param globalConfig
	 *            holds global configuration: Bumblebee URL, path to UFT runner.
	 * @param envVars
	 *            environment variables of agent on which job will be running.
	 * @return parameters, never <code>null</code>.
	 */
	public UftRunnerParameters create(final RunUftTestBuildStep step, final BumblebeeGlobalConfig globalConfig, final EnvVars envVars) {
		final UftRunnerParameters params = new UftRunnerParametersImpl();
		params.setBumbleBeeUrl(globalConfig.getBumblebeeUrl());
		params.setOutputDirName(step.getOutputDirPath());
		params.setTestPath(step.getTestPath());
		params.setTimeOut(globalConfig.getTimeOut());
		params.setClientType(ClientType.JENKINS);

		String uftRunnerPath = envVars.get(UFT_RUNNER_ENV_VAR_NAME);
		if (StringUtils.isEmpty(uftRunnerPath)) {
			uftRunnerPath = globalConfig.getUftRunnerPath();
		}
		params.setUftBatchRunnerExePath(uftRunnerPath);
		return new UftRunnerEnvSpecificParameters(params, envVars);
	}
}
