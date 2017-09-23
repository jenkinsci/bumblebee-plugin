package com.agiletestware.bumblebee.uft;

import com.agiletestware.bumblebee.VeryBaseEnvSpecificParameters;
import com.agiletestware.bumblebee.client.uftrunner.UftRunnerParameters;
import com.agiletestware.bumblebee.tracking.ClientType;

import hudson.EnvVars;

/**
 * Decorator for {@link UftRunnerParameters}. It resolves environment variables
 * in values of decorated parameters using given {@link EnvVars}.
 *
 * @author Sergey Oplavin
 *
 */
public class UftRunnerEnvSpecificParameters extends VeryBaseEnvSpecificParameters<UftRunnerParameters> implements UftRunnerParameters {

	/** . */
	private static final long serialVersionUID = -6113850224387682258L;

	public UftRunnerEnvSpecificParameters(final UftRunnerParameters params, final EnvVars envVars) {
		super(params, envVars);
	}

	@Override
	public String getTestPath() {
		return expand(getParameters().getTestPath());
	}

	@Override
	public void setTestPath(final String testPath) {
		getParameters().setTestPath(testPath);
	}

	@Override
	public String getOutputDirName() {
		return expand(getParameters().getOutputDirName());
	}

	@Override
	public void setOutputDirName(final String outputDirName) {
		getParameters().setOutputDirName(outputDirName);
	}

	@Override
	public String getUftBatchRunnerExePath() {
		return expand(getParameters().getUftBatchRunnerExePath());
	}

	@Override
	public void setUftBatchRunnerExePath(final String uftBatchRunnerExePath) {
		getParameters().setUftBatchRunnerExePath(uftBatchRunnerExePath);
	}

	@Override
	public int getTimeOut() {
		return getParameters().getTimeOut();
	}

	@Override
	public void setTimeOut(final int timeout) {
		getParameters().setTimeOut(timeout);
	}

	@Override
	public String getReportFileName() {
		return getParameters().getReportFileName();
	}

	@Override
	public void setReportFileName(final String fileName) {
		getParameters().setReportFileName(fileName);
	}

	@Override
	public ClientType getClientType() {
		return getParameters().getClientType();
	}

	@Override
	public void setClientType(final ClientType clientType) {
		getParameters().setClientType(clientType);
	}

}
