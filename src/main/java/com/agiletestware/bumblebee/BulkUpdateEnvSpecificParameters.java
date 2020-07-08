package com.agiletestware.bumblebee;

import com.agiletestware.bumblebee.client.api.BulkUpdateParameters;

import hudson.EnvVars;

/**
 * Decorator for {@link BulkUpdateParameters}. It resolves environment variables
 * in values of decorated parameters using given {@link EnvVars}.
 *
 * @author Sergey Oplavin
 *
 */
public class BulkUpdateEnvSpecificParameters extends BaseEnvSpecificParameters<BulkUpdateParameters> implements BulkUpdateParameters {

	/** . */
	private static final long serialVersionUID = -2876289816591828294L;

	/**
	 * Constructor.
	 *
	 * @param config
	 *            Instance of {@link BumblebeeConfiguration} to wrap.
	 * @param envVars
	 *            The {@link EnvVars} instance.
	 */
	public BulkUpdateEnvSpecificParameters(
			final BulkUpdateParameters params, final EnvVars envVars) {
		super(params, envVars);
	}

	@Override
	public String getTestPlanDirectory() {
		return expand(getParameters().getTestPlanDirectory());
	}

	@Override
	public void setTestPlanDirectory(final String testPlanDirectory) {
		getParameters().setTestPlanDirectory(testPlanDirectory);
	}

	@Override
	public String getTestLabDirectory() {
		return expand(getParameters().getTestLabDirectory());
	}

	@Override
	public void setTestLabDirectory(final String testLabDirectory) {
		getParameters().setTestLabDirectory(testLabDirectory);
	}

	@Override
	public String getFormat() {
		return expand(getParameters().getFormat());
	}

	@Override
	public void setFormat(final String format) {
		getParameters().setFormat(format);
	}

	@Override
	public String getTestSet() {
		return expand(getParameters().getTestSet());
	}

	@Override
	public void setTestSet(final String testSet) {
		getParameters().setTestSet(testSet);
	}

	@Override
	public String getResultPattern() {
		return expand(getParameters().getResultPattern());
	}

	@Override
	public void setResultPattern(final String resultPattern) {
		getParameters().setResultPattern(resultPattern);
	}

	@Override
	public void setMode(final String mode) {
		getParameters().setMode(mode);
	}

	@Override
	public String getMode() {
		return expand(getParameters().getMode());
	}

	@Override
	public void setTimeOut(final int timeOut) {
		getParameters().setTimeOut(timeOut);
	}

	@Override
	public int getTimeOut() {
		return getParameters().getTimeOut();
	}

	@Override
	public void setCustomProperties(final String customProperties) {
		getParameters().setCustomProperties(customProperties);
	}

	@Override
	public String getCustomProperties() {
		return expand(getParameters().getCustomProperties());
	}

	@Override
	public boolean isOffline() {
		return getParameters().isOffline();
	}

	@Override
	public void setOffline(final boolean offline) {
		getParameters().setOffline(offline);
	}

	@Override
	public String getDefectCreatePolicy() {
		return getParameters().getDefectCreatePolicy();
	}

	@Override
	public void setDefectCreatePolicy(final String defectCreatePolicy) {
		getParameters().setDefectCreatePolicy(defectCreatePolicy);
	}

	@Override
	public String getDefectCreateStatus() {
		return expand(getParameters().getDefectCreateStatus());
	}

	@Override
	public void setDefectCreateStatus(final String defectCreateStatus) {
		getParameters().setDefectCreateStatus(defectCreateStatus);
	}

	@Override
	public String getDefectSeverity() {
		return expand(getParameters().getDefectSeverity());
	}

	@Override
	public void setDefectSeverity(final String defectSeverity) {
		getParameters().setDefectSeverity(defectSeverity);
	}

	@Override
	public String getDefectReopenStatus() {
		return expand(getParameters().getDefectReopenStatus());
	}

	@Override
	public void setDefectReopenStatus(final String defectReopenStatus) {
		getParameters().setDefectReopenStatus(defectReopenStatus);
	}

	@Override
	public String getDefectResolvePolicy() {
		return getParameters().getDefectResolvePolicy();
	}

	@Override
	public void setDefectResolvePolicy(final String defectResolvePolicy) {
		getParameters().setDefectResolvePolicy(defectResolvePolicy);
	}

	@Override
	public String getDefectResolveStatus() {
		return expand(getParameters().getDefectResolveStatus());
	}

	@Override
	public void setDefectResolveStatus(final String defectResolveStatus) {
		getParameters().setDefectResolveStatus(defectResolveStatus);
	}

	@Override
	public String toString() {
		return "BulkUpdateEnvSpecificParameters [getTestPlanDirectory()=" + getTestPlanDirectory() + ", getTestLabDirectory()=" + getTestLabDirectory()
		+ ", getFormat()=" + getFormat() + ", getTestSet()=" + getTestSet() + ", getResultPattern()=" + getResultPattern() + ", getMode()=" + getMode()
		+ ", getTimeOut()=" + getTimeOut() + ", getCustomProperties()=" + getCustomProperties() + ", isOffline()=" + isOffline()
		+ ", getDefectCreatePolicy()=" + getDefectCreatePolicy() + ", getDefectCreateStatus()=" + getDefectCreateStatus() + ", getDefectSeverity()="
		+ getDefectSeverity() + ", getDefectReopenStatus()=" + getDefectReopenStatus() + ", getDefectResolvePolicy()=" + getDefectResolvePolicy()
		+ ", getDefectResolveStatus()=" + getDefectResolveStatus() + ", getAlmUrl()=" + getAlmUrl() + ", getDomain()=" + getDomain() + ", getProject()="
		+ getProject() + ", getAlmUserName()=" + getAlmUserName() + ", getEncryptedPassword()=*******" + ", toString()="
		+ super.toString() + ", getBumbleBeeUrl()=" + getBumbleBeeUrl() + ", isTrustSelfSignedCerts()=" + isTrustSelfSignedCerts() + "]";
	}

}
