package com.agiletestware.bumblebee.results;

import com.agiletestware.bumblebee.BumblebeeGlobalConfig;

/**
 * Parameters factory.
 *
 * @author Sergey Oplavin
 *
 */
public interface GetTestResultsParametersFactory {

	/**
	 * Creates parameters from global config and job config.
	 *
	 * @param globalConfig
	 *            global config
	 * @param jobConfig
	 *            job config
	 * @return parameters, implementations must not return <code>null</code>.
	 */
	GetTestResultsParameters create(BumblebeeGlobalConfig globalConfig, GetTestResultsConfiguration jobConfig);
}
