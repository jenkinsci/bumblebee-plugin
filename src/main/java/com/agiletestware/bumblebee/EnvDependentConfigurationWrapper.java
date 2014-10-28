package com.agiletestware.bumblebee;

import hudson.EnvVars;

/**
 * Wrapper/decorator for {@link BumblebeeConfiguration}. It resolves environment
 * variables in values of wrapped configuration using given {@link EnvVars}.
 *
 * @author Sergey Oplavin (oplavin.sergei@gmail.com)
 *
 */
public class EnvDependentConfigurationWrapper {

	/** Wrapped {@link BumblebeeConfiguration}. */
	private final BumblebeeConfiguration wrappedConfig;
	/** {@link EnvVars} to resolve values. */
	private final EnvVars envVars;

	/**
	 * Constructor.
	 *
	 * @param config
	 *            Instance of {@link BumblebeeConfiguration} to wrap.
	 * @param envVars
	 *            The {@link EnvVars} instance.
	 */
	public EnvDependentConfigurationWrapper(
			final BumblebeeConfiguration config, final EnvVars envVars) {
		this.wrappedConfig = config;
		this.envVars = envVars;
	}

	/**
	 *
	 * @return Value of {@link BumblebeeConfiguration#getProjectName()} where
	 *         all environment variables resolved.
	 */
	public String getProjectName() {
		return envVars.expand(wrappedConfig.getProjectName());
	}

	/**
	 *
	 * @return Value of {@link BumblebeeConfiguration#getDomain()} where all
	 *         environment variables resolved.
	 */
	public String getDomain() {
		return envVars.expand(wrappedConfig.getDomain());
	}

	/**
	 *
	 * @return Value of {@link BumblebeeConfiguration#getTestPlan()} where all
	 *         environment variables resolved.
	 */
	public String getTestPlan() {
		return envVars.expand(wrappedConfig.getTestPlan());
	}

	/**
	 *
	 * @return Value of {@link BumblebeeConfiguration#getResultPattern()} where
	 *         all environment variables resolved.
	 */
	public String getResultPattern() {
		return envVars.expand(wrappedConfig.getResultPattern());
	}

	/**
	 *
	 * @return Value of {@link BumblebeeConfiguration#getCustomProperties()}
	 *         where all environment variables resolved.
	 */
	public String getCustomProperties() {
		return envVars.expand(wrappedConfig.getCustomProperties());
	}

	/**
	 *
	 * @return Value of {@link BumblebeeConfiguration#getTestLab()} where all
	 *         environment variables resolved.
	 */
	public String getTestLab() {
		return envVars.expand(wrappedConfig.getTestLab());
	}

	/**
	 *
	 * @return Value of {@link BumblebeeConfiguration#getTestSet()} where all
	 *         environment variables resolved.
	 */
	public String getTestSet() {
		return envVars.expand(wrappedConfig.getTestSet());
	}

	/**
	 *
	 * @return Value of {@link BumblebeeConfiguration#getFormat()} where all
	 *         environment variables resolved.
	 */
	public String getFormat() {
		return envVars.expand(wrappedConfig.getFormat());
	}

	/**
	 *
	 * @return mode.
	 */
	public String getMode() {
		return wrappedConfig.getMode();
	}

}
