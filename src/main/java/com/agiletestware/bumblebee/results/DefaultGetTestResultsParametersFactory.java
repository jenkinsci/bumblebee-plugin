package com.agiletestware.bumblebee.results;

import com.agiletestware.bumblebee.BumblebeeGlobalConfig;
import com.agiletestware.bumblebee.tracking.ClientType;

/**
 * Factory for producing {@link GetTestResultsParameters} objects.
 *
 * @author Sergey Oplavin
 *
 */
public enum DefaultGetTestResultsParametersFactory implements GetTestResultsParametersFactory {

	THE_INSTANCE;
	@Override
	public GetTestResultsParameters create(final BumblebeeGlobalConfig globalConfig, final GetTestResults jobConfig) {
		return GetTestResultsParametersImpl.builder(globalConfig.getBumblebeeUrl())
				.almUrl(globalConfig.getQcUrl())
				.almUser(globalConfig.getQcUserName())
				.almEncryptedPassword(globalConfig.getPassword())
				.almDomain(jobConfig.getDomain())
				.almProject(jobConfig.getProject())
				.testSetPath(jobConfig.getTestSetPath())
				.clientType(ClientType.JENKINS)
				.build();
	}

}
