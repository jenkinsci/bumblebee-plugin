package com.agiletestware.bumblebee;

import com.agiletestware.bumblebee.client.api.BulkUpdateParameters;

import hudson.EnvVars;

/**
 * Decorator for {@link BulkUpdateParameters}. It resolves environment variables
 * in values of decorated parameters using given {@link EnvVars}.
 *
 * @author Sergey Oplavin
 *
 */
public class BulkUpdateEnvSpecificParameters extends BaseEnvSpecificParameters<BulkUpdateParameters>implements BulkUpdateParameters {

	/** . */
	private static final long serialVersionUID = -2876289816591828294L;

	/**
	 * Constructor.
	 *
	 * @param config
	 *            Instance of {@link BumblebeeConfiguration} to wrap.
	 * @param envVars
	 *            The {@link EnvVars} instance.
	 */
	public BulkUpdateEnvSpecificParameters(
			final BulkUpdateParameters params, final EnvVars envVars) {
		super(params, envVars);
	}

	@Override
	public String getTestPlanDirectory() {
		return expand(getParameters().getTestPlanDirectory());
	}

	@Override
	public void setTestPlanDirectory(final String testPlanDirectory) {
		getParameters().setTestPlanDirectory(testPlanDirectory);
	}

	@Override
	public String getTestLabDirectory() {
		return expand(getParameters().getTestLabDirectory());
	}

	@Override
	public void setTestLabDirectory(final String testLabDirectory) {
		getParameters().setTestLabDirectory(testLabDirectory);
	}

	@Override
	public String getFormat() {
		return expand(getParameters().getFormat());
	}

	@Override
	public void setFormat(final String format) {
		getParameters().setFormat(format);
	}

	@Override
	public String getTestSet() {
		return expand(getParameters().getTestSet());
	}

	@Override
	public void setTestSet(final String testSet) {
		getParameters().setTestSet(testSet);
	}

	@Override
	public String getResultPattern() {
		return expand(getParameters().getResultPattern());
	}

	@Override
	public void setResultPattern(final String resultPattern) {
		getParameters().setResultPattern(resultPattern);
	}

	@Override
	public void setMode(final String mode) {
		getParameters().setMode(mode);
	}

	@Override
	public String getMode() {
		return expand(getParameters().getMode());
	}

	@Override
	public void setTimeOut(final int timeOut) {
		getParameters().setTimeOut(timeOut);
	}

	@Override
	public int getTimeOut() {
		return getParameters().getTimeOut();
	}

	@Override
	public void setCustomProperties(final String customProperties) {
		getParameters().setCustomProperties(customProperties);
	}

	@Override
	public String getCustomProperties() {
		return expand(getParameters().getCustomProperties());
	}

	@Override
	public boolean isOffline() {
		return getParameters().isOffline();
	}

	@Override
	public void setOffline(final boolean offline) {
		getParameters().setOffline(offline);
	}

}
