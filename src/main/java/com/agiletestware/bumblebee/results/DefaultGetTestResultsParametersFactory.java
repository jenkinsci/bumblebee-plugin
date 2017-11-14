package com.agiletestware.bumblebee.results;

import org.apache.commons.lang.StringUtils;

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
	public GetTestResultsParameters create(final BumblebeeGlobalConfig globalConfig, final GetTestResults commonJobConfig,
			final GetTestResultsConfiguration jobConfig) {
		return GetTestResultsParametersImpl.builder(globalConfig.getBumblebeeUrl())
				.almUrl(globalConfig.getQcUrl())
				.almUser(getValueFromConfig(globalConfig.getQcUserName(), commonJobConfig.getUser()))
				.almEncryptedPassword(getValueFromConfig(globalConfig.getPassword(), commonJobConfig.getPassword()))
				.almDomain(commonJobConfig.getDomain())
				.almProject(commonJobConfig.getProject())
				.testSetPath(jobConfig.getTestSetPath())
				.clientType(ClientType.JENKINS)
				.build();
	}

	private String getValueFromConfig(final String globalValue, final String localValue) {
		return StringUtils.isNotEmpty(localValue) ? localValue : globalValue;
	}
}
