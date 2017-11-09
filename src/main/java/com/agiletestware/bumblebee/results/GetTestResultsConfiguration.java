package com.agiletestware.bumblebee.results;

import java.io.Serializable;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Configuration for getting test results.
 *
 * @author Sergey Oplavin
 *
 */
public class GetTestResultsConfiguration implements Serializable {

	/** . */
	private static final long serialVersionUID = -4600406379228450005L;
	private final String domain;
	private final String project;
	private final String testSetPath;
	private final String resultsDir;

	/**
	 * Data bound constructor. Gets the values from form and saves to fields.
	 *
	 * @param domain
	 *            domain
	 * @param project
	 *            project
	 * @param testSetPath
	 *            path to a test set
	 * @param resultsDir
	 *            results directory
	 */
	@DataBoundConstructor
	public GetTestResultsConfiguration(final String domain, final String project, final String testSetPath, final String resultsDir) {
		this.domain = domain;
		this.project = project;
		this.testSetPath = testSetPath;
		this.resultsDir = resultsDir;
	}

	public String getDomain() {
		return domain;
	}

	public String getProject() {
		return project;
	}

	public String getTestSetPath() {
		return testSetPath;
	}

	public String getResultsDir() {
		return resultsDir;
	}

}
