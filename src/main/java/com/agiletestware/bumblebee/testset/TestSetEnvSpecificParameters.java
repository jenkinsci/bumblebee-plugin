package com.agiletestware.bumblebee.testset;

import java.util.ArrayList;
import java.util.List;

import com.agiletestware.bumblebee.BaseEnvSpecificParameters;
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
public class TestSetEnvSpecificParameters extends BaseEnvSpecificParameters<TestSetRunnerParameters>implements TestSetRunnerParameters {

	/** . */
	private static final long serialVersionUID = 5224372037113848684L;

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
		super(parameters, envVars);
	}

	@Override
	public AlmRunMode getRunMode() {
		// no env variables, as this is a closed list.
		return getParameters().getRunMode();
	}

	@Override
	public String getHost() {
		return expand(getParameters().getHost());
	}

	@Override
	public List<String> getTestSets() {
		final List<String> testSets = getParameters().getTestSets();
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
		getParameters().setRunMode(runMode);
	}

	@Override
	public void setHost(final String host) {
		getParameters().setHost(host);
	}

	@Override
	public void setTestSets(final List<String> testSets) {
		getParameters().setTestSets(testSets);
	}

	@Override
	public int getTimeOut() {
		return getParameters().getTimeOut();
	}

	@Override
	public void setTimeOut(final int timeOut) {
		getParameters().setTimeOut(timeOut);
	}

	@Override
	public String getOutputDirPath() {
		return expand(getParameters().getOutputDirPath());
	}

	@Override
	public void setOutputDirPath(final String outputDirPath) {
		getParameters().setOutputDirPath(outputDirPath);
	}
}
