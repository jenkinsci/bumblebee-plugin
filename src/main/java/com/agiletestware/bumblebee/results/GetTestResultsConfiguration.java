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
	private final String testSetPath;

	/**
	 * Data bound constructor. Gets the values from form and saves to fields.
	 *
	 * @param testSetPath
	 *            path to a test set
	 */
	@DataBoundConstructor
	public GetTestResultsConfiguration(final String testSetPath) {
		this.testSetPath = testSetPath;
	}

	public String getTestSetPath() {
		return testSetPath;
	}

}
