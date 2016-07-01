package com.agiletestware.bumblebee;

import com.agiletestware.bumblebee.client.api.BaseParameters;

import hudson.EnvVars;

/**
 * Decorator for {@link BaseParameters} which resolves parameter values
 * containing environment variables using {@link EnvVars}.
 *
 * @author Sergey Oplavin
 *
 * @param <T>
 *            concrete type of decorated parameters.
 */
public class BaseEnvSpecificParameters<T extends BaseParameters> implements BaseParameters {

	/** . */
	private static final long serialVersionUID = 6609958109617860227L;
	private final T params;
	private final EnvVars envVars;

	public BaseEnvSpecificParameters(final T params, final EnvVars envVars) {
		this.params = params;
		this.envVars = envVars;
	}

	@Override
	public String getBumbleBeeUrl() {
		return expand(params.getBumbleBeeUrl());
	}

	@Override
	public void setBumbleBeeUrl(final String bumbleBeeUrl) {
		params.setAlmUrl(bumbleBeeUrl);
	}

	@Override
	public String getAlmUrl() {
		return expand(params.getAlmUrl());
	}

	@Override
	public void setAlmUrl(final String almUrl) {
		params.setAlmUrl(almUrl);
	}

	@Override
	public String getDomain() {
		return expand(params.getDomain());
	}

	@Override
	public void setDomain(final String domain) {
		params.setDomain(domain);
	}

	@Override
	public String getProject() {
		return expand(params.getProject());
	}

	@Override
	public void setProject(final String project) {
		params.setProject(project);
	}

	@Override
	public String getAlmUserName() {
		return expand(params.getAlmUserName());
	}

	@Override
	public void setAlmUserName(final String almUserName) {
		params.setAlmUserName(almUserName);
	}

	@Override
	public String getEncryptedPassword() {
		// no env variables allowed in password.
		return params.getEncryptedPassword();
	}

	@Override
	public void setEncryptedPassword(final String encryptedPassword) {
		params.setEncryptedPassword(encryptedPassword);
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
