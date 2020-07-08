/**
 *
 */
package com.agiletestware.bumblebee;

import java.io.Serializable;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Util;

/**
 * Class which holds configuration for each bumblebee step.
 *
 * @author c_rsharv
 * @author Sergey Oplavin (refactored).
 *
 */
public final class BumblebeeConfiguration implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String MODE = "FULL";
	private final String projectName;
	private final String testPlan;
	private final String testLab;
	private final String testSet;
	private final String format;
	private final String domain;
	private final String resultPattern;
	private final String customProperties;
	private final boolean failIfUploadFailed;
	private final boolean offline;
	private final String defectCreatePolicy;
	private final String defectCreateStatus;
	private final String defectSeverity;
	private final String defectReopenStatus;
	private final String defectResolvePolicy;
	private final String defectResolveStatus;

	/**
	 * Creates new instance.
	 *
	 * @param domain
	 *            Domain
	 * @param projectName
	 *            Project name
	 * @param testPlan
	 *            Test plan
	 * @param testLab
	 *            Test Lab
	 * @param testSet
	 *            Test set
	 * @param format
	 *            Format
	 * @param resultPattern
	 *            Pattern for searching report files
	 * @param customProperties
	 *            Custom properties
	 */
	@DataBoundConstructor
	public BumblebeeConfiguration(final String domain,
			final String projectName, final String testPlan,
			final String testLab, final String testSet, final String format,
			final String resultPattern, final String customProperties,
			final boolean failIfUploadFailed,
			final boolean offline,
			final String defectCreatePolicy,
			final String defectCreateStatus,
			final String defectSeverity,
			final String defectReopenStatus,
			final String defectResolvePolicy,
			final String defectResolveStatus) {
		super();
		this.domain = Util.fixEmptyAndTrim(domain);
		this.projectName = Util.fixEmptyAndTrim(projectName);
		this.testPlan = Util.fixEmptyAndTrim(testPlan);
		this.testLab = Util.fixEmptyAndTrim(testLab);
		this.testSet = Util.fixEmptyAndTrim(testSet);
		this.format = Util.fixEmptyAndTrim(format);
		this.resultPattern = Util.fixEmptyAndTrim(resultPattern);
		this.customProperties = Util.fixEmptyAndTrim(customProperties);
		this.failIfUploadFailed = failIfUploadFailed;
		this.offline = offline;
		this.defectCreatePolicy = Util.fixEmptyAndTrim(defectCreatePolicy);
		this.defectCreateStatus = Util.fixEmptyAndTrim(defectCreateStatus);
		this.defectSeverity = Util.fixEmptyAndTrim(defectSeverity);
		this.defectReopenStatus = Util.fixEmptyAndTrim(defectReopenStatus);
		this.defectResolvePolicy = Util.fixEmptyAndTrim(defectResolvePolicy);
		this.defectResolveStatus = Util.fixEmptyAndTrim(defectResolveStatus);
	}

	/**
	 *
	 * @return Test Plan.
	 */
	public String getTestPlan() {
		return this.testPlan;
	}

	/**
	 *
	 * @return Pattern for seaching report files.
	 */
	public String getResultPattern() {
		return this.resultPattern;
	}

	/**
	 *
	 * @return Custom properties.
	 */
	public String getCustomProperties() {
		return this.customProperties;
	}

	/**
	 *
	 * @return Test lab.
	 */
	public String getTestLab() {
		return this.testLab;
	}

	/**
	 *
	 * @return Test set.
	 */
	public String getTestSet() {
		return this.testSet;
	}

	/**
	 *
	 * @return Format.
	 */
	public String getFormat() {
		return this.format;
	}

	/**
	 *
	 * @return Project name.
	 */
	public String getProjectName() {
		return this.projectName;
	}

	/**
	 *
	 * @return Domain.
	 */
	public String getDomain() {
		return this.domain;
	}

	/**
	 *
	 * @return Mode.
	 */
	public String getMode() {
		return MODE;
	}

	/**
	 *
	 * @return failIfUploadFailed.
	 */
	public boolean getFailIfUploadFailed() {
		return failIfUploadFailed;
	}

	/**
	 * @return the offline
	 */
	public boolean isOffline() {
		return offline;
	}

	public String getDefectCreatePolicy() {
		return defectCreatePolicy;
	}

	public String getDefectCreateStatus() {
		return defectCreateStatus;
	}

	public String getDefectSeverity() {
		return defectSeverity;
	}

	public String getDefectReopenStatus() {
		return defectReopenStatus;
	}

	public String getDefectResolvePolicy() {
		return defectResolvePolicy;
	}

	public String getDefectResolveStatus() {
		return defectResolveStatus;
	}
}
