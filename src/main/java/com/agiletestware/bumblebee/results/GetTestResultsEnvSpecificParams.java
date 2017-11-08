package com.agiletestware.bumblebee.results;

import com.agiletestware.bumblebee.BaseEnvSpecificParameters;

import hudson.EnvVars;

/**
 * Environment-specific implementation of {@link GetTestResultsParameters}.
 * Expands the variables in the testSetPath.
 *
 * @author Sergey Oplavin
 *
 */
public class GetTestResultsEnvSpecificParams extends BaseEnvSpecificParameters<GetTestResultsParameters> implements GetTestResultsParameters {

	/** . */
	private static final long serialVersionUID = 1L;

	public GetTestResultsEnvSpecificParams(final GetTestResultsParameters params, final EnvVars envVars) {
		super(params, envVars);
	}

	@Override
	public String getTestSetPath() {
		return expand(getParameters().getTestSetPath());
	}
}
