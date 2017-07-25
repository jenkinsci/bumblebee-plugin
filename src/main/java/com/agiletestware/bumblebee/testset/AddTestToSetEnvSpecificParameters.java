package com.agiletestware.bumblebee.testset;

import com.agiletestware.bumblebee.BaseEnvSpecificParameters;
import com.agiletestware.bumblebee.client.api.AddTestToSetParameters;

import hudson.EnvVars;

/**
 * Decorator for {@link AddTestToSetParameters} which replaces env vars with
 * values.
 *
 * @author Sergey Oplavin
 *
 */
public class AddTestToSetEnvSpecificParameters extends BaseEnvSpecificParameters<AddTestToSetParameters>
implements AddTestToSetParameters {

	/** . */
	private static final long serialVersionUID = -8560779914812156808L;

	/**
	 * Constructor.
	 *
	 * @param params
	 *            params.
	 * @param envVars
	 *            environment vars.
	 */
	public AddTestToSetEnvSpecificParameters(final AddTestToSetParameters params, final EnvVars envVars) {
		super(params, envVars);
	}

	@Override
	public String getTestPlanPath() {
		return expand(getParameters().getTestPlanPath());
	}

	@Override
	public void setTestPlanPath(final String testPlanPath) {
		getParameters().setTestPlanPath(testPlanPath);
	}

	@Override
	public String getTestSetPath() {
		return expand(getParameters().getTestSetPath());
	}

	@Override
	public void setTestSetPath(final String testSetPath) {
		getParameters().setTestSetPath(testSetPath);
	}

}
