package com.agiletestware.bumblebee;

import com.agiletestware.bumblebee.client.api.BumblebeeParameters;

import hudson.EnvVars;

/**
 * Decorator for {@link BumblebeeParameters}. It resolves environment variables
 * in values of decorated parameters using given {@link EnvVars}.
 *
 * @author Sergey Oplavin
 *
 */
// FIXME: a stupid name introduced because there was already
// BaseEnvSpecificParameters
public class VeryBaseEnvSpecificParameters<T extends BumblebeeParameters> implements BumblebeeParameters {

	private static final long serialVersionUID = 2336029834088473649L;
	private final T params;
	private final EnvVars envVars;

	public VeryBaseEnvSpecificParameters(final T params, final EnvVars envVars) {
		this.params = params;
		this.envVars = envVars;
	}

	@Override
	public String getBumbleBeeUrl() {
		return expand(params.getBumbleBeeUrl());
	}

	@Override
	public void setBumbleBeeUrl(final String bumbleBeeUrl) {
		params.setBumbleBeeUrl(bumbleBeeUrl);
	}

	protected String expand(final String value) {
		return envVars.expand(value);
	}

	protected T getParameters() {
		return params;
	}

	protected EnvVars getEnvVars() {
		return envVars;
	}
}
