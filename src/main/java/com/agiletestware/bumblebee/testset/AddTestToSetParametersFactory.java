package com.agiletestware.bumblebee.testset;

import com.agiletestware.bumblebee.BumblebeeGlobalConfig;
import com.agiletestware.bumblebee.client.api.AddTestToSetParameters;
import com.agiletestware.bumblebee.client.api.AddTestToSetParametersImpl;

/**
 * Factory for producing {@link AddTestToSetParameters} instance.
 *
 * @author Sergey Oplavin
 *
 */
public enum AddTestToSetParametersFactory {

	THE_INSTANCE;

	/**
	 * Creates a new {@link AddTestToSetParameters} parameters.
	 *
	 * @param step
	 *            step.
	 * @param globalConfig
	 *            global configuration.
	 * @return parameter object.
	 */
	public AddTestToSetParameters create(final AddTestToSetStep step, final BumblebeeGlobalConfig globalConfig) {
		final AddTestToSetParameters params = new AddTestToSetParametersImpl();
		params.setAlmUrl(globalConfig.getQcUrl());
		params.setBumbleBeeUrl(globalConfig.getBumblebeeUrl());
		params.setAlmUserName(globalConfig.getQcUserName());
		params.setEncryptedPassword(globalConfig.getPassword());
		params.setDomain(step.getDomain());
		params.setProject(step.getProject());
		params.setTestPlanPath(step.getTestPlanPath());
		params.setTestSetPath(step.getTestSetPath());
		params.setTrustSelfSignedCerts(globalConfig.isTrustSelfSignedCerts());
		return params;
	}
}
