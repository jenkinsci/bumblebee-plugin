package com.agiletestware.bumblebee.testset;

import java.util.ArrayList;
import java.util.List;

import com.agiletestware.bumblebee.client.api.AlmRunMode;
import com.agiletestware.bumblebee.client.testrunner.TestSetRunnerParameters;

import hudson.EnvVars;

/**
 * Decorator for {@link TestSetRunnerParameters} which expands its values using
 * passed {@link EnvVars} object.
 *
 * @author Sergey Oplavin
 *
 */
public class TestSetEnvSpecificParameters implements TestSetRunnerParameters {

	/** . */
	private static final long serialVersionUID = 5224372037113848684L;
	private final TestSetRunnerParameters parameters;
	private final EnvVars envVars;

	/**
	 * Constructor.
	 *
	 * @param parameters
	 *            original parameters.
	 * @param envVars
	 *            environment variables which will be used to expand values of
	 *            original parameters.
	 */
	public TestSetEnvSpecificParameters(final TestSetRunnerParameters parameters, final EnvVars envVars) {
		this.parameters = parameters;
		this.envVars = envVars;
	}

	@Override
	public String getBumbleBeeUrl() {
		return expand(parameters.getBumbleBeeUrl());
	}

	@Override
	public void setBumbleBeeUrl(final String bumbleBeeUrl) {
		parameters.setAlmUrl(bumbleBeeUrl);
	}

	@Override
	public String getAlmUrl() {
		return expand(parameters.getAlmUrl());
	}

	@Override
	public void setAlmUrl(final String almUrl) {
		parameters.setAlmUrl(almUrl);
	}

	@Override
	public String getDomain() {
		return expand(parameters.getDomain());
	}

	@Override
	public void setDomain(final String domain) {
		parameters.setDomain(domain);
	}

	@Override
	public String getProject() {
		return expand(parameters.getProject());
	}

	@Override
	public void setProject(final String project) {
		parameters.setProject(project);
	}

	@Override
	public String getAlmUserName() {
		return expand(parameters.getAlmUserName());
	}

	@Override
	public void setAlmUserName(final String almUserName) {
		parameters.setAlmUserName(almUserName);
	}

	@Override
	public String getEncryptedPassword() {
		// no env variables allowed in password.
		return parameters.getEncryptedPassword();
	}

	@Override
	public void setEncryptedPassword(final String encryptedPassword) {
		parameters.setEncryptedPassword(encryptedPassword);
	}

	@Override
	public AlmRunMode getRunMode() {
		// no env variables, as this is a closed list.
		return parameters.getRunMode();
	}

	@Override
	public String getHost() {
		return expand(parameters.getHost());
	}

	@Override
	public List<String> getTestSets() {
		final List<String> testSets = parameters.getTestSets();
		// just in case
		if (testSets == null) {
			return null;
		}
		final List<String> adjustedValues = new ArrayList<>(testSets.size());
		for (final String testSet : testSets) {
			adjustedValues.add(expand(testSet));
		}
		return adjustedValues;
	}

	@Override
	public void setRunMode(final AlmRunMode runMode) {
		parameters.setRunMode(runMode);
	}

	@Override
	public void setHost(final String host) {
		parameters.setHost(host);
	}

	@Override
	public void setTestSets(final List<String> testSets) {
		parameters.setTestSets(testSets);
	}

	@Override
	public int getTimeOut() {
		return parameters.getTimeOut();
	}

	@Override
	public void setTimeOut(final int timeOut) {
		parameters.setTimeOut(timeOut);
	}

	@Override
	public String getOutputDirPath() {
		return expand(parameters.getOutputDirPath());
	}

	@Override
	public void setOutputDirPath(final String outputDirPath) {
		parameters.setOutputDirPath(outputDirPath);
	}

	private String expand(final String value) {
		return envVars.expand(value);
	}

}
