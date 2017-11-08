package com.agiletestware.bumblebee.results;

import com.agiletestware.bumblebee.BumblebeeGlobalConfig;

public interface GetTestResultsParametersFactory {

	GetTestResultsParameters create(BumblebeeGlobalConfig globalConfig, GetTestResults jobConfig);
}
