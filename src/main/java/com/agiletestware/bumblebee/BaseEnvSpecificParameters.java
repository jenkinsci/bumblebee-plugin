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
public class BaseEnvSpecificParameters<T extends BaseParameters> extends VeryBaseEnvSpecificParameters<T> implements BaseParameters {

	/** . */
	private static final long serialVersionUID = 6609958109617860227L;

	public BaseEnvSpecificParameters(final T params, final EnvVars envVars) {
		super(params, envVars);
	}

	@Override
	public String getAlmUrl() {
		return expand(getParameters().getAlmUrl());
	}

	@Override
	public void setAlmUrl(final String almUrl) {
		getParameters().setAlmUrl(almUrl);
	}

	@Override
	public String getDomain() {
		return expand(getParameters().getDomain());
	}

	@Override
	public void setDomain(final String domain) {
		getParameters().setDomain(domain);
	}

	@Override
	public String getProject() {
		return expand(getParameters().getProject());
	}

	@Override
	public void setProject(final String project) {
		getParameters().setProject(project);
	}

	@Override
	public String getAlmUserName() {
		return expand(getParameters().getAlmUserName());
	}

	@Override
	public void setAlmUserName(final String almUserName) {
		getParameters().setAlmUserName(almUserName);
	}

	@Override
	public String getEncryptedPassword() {
		// no env variables allowed in password.
		return getParameters().getEncryptedPassword();
	}

	@Override
	public void setEncryptedPassword(final String encryptedPassword) {
		getParameters().setEncryptedPassword(encryptedPassword);
	}

}
