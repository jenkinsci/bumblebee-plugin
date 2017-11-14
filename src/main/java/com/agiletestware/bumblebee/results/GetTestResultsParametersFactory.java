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
	 * @param commonJobConfig
	 *            common values for all configurations in the job.
	 * @param jobConfig
	 *            job config
	 * @return parameters, implementations must not return <code>null</code>.
	 */
	GetTestResultsParameters create(BumblebeeGlobalConfig globalConfig, GetTestResults commonJobConfig, GetTestResultsConfiguration jobConfig);
}
