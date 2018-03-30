package com.agiletestware.bumblebee.jasmine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.agiletestware.bumblebee.client.jasmine.JasmineResources;
import com.agiletestware.bumblebee.client.jasmine.Spec;
import com.agiletestware.bumblebee.client.jasmine.Suite;

import hudson.FilePath;

/**
 * Factory for create {@link JasmineResources} instances.
 *
 * @author Ayman BEN AMOR
 *
 */
public enum JasmineResourcesFactory {

	THE_INSTANCE;

	/**
	 * Creates the.
	 *
	 * @param suites
	 *            the suites
	 * @param workspace
	 *            the workspace
	 * @param screenshotPath
	 *            the screenshot path
	 * @return the jasmine resources
	 */
	public JasmineResources create(final List<Suite> suites, final FilePath workspace, final String screenshotPath) {
		final JasmineResources resource = new JasmineResources();
		final List<File> screenShots = new ArrayList<>();
		final File screenshotDir = new File(workspace.getRemote() + deleteFirstPointCharacterFromString(screenshotPath));
		final File[] fileList = screenshotDir.listFiles();
		if (!suites.isEmpty()) {
			for (final Suite suite : suites) {
				final List<Spec> specs = suite.getSpecs();
				if (!specs.isEmpty()) {
					for (final Spec spec : specs) {
						if (!spec.getScreenShots().isEmpty()) {
							screenShots.addAll(createFiles(spec.getScreenShots(), fileList));
						}
					}
					resource.setScreenShots(screenShots);
				}
			}
		}
		return resource;
	}

	/**
	 * Creates a new JasmineResources object.
	 *
	 * @param fileNames
	 *            the file names
	 * @param fileList
	 *            the file list
	 * @return the list< file>
	 */
	private List<File> createFiles(final List<String> fileNames, final File[] fileList) {
		final List<File> files = new ArrayList<>();
		for (final String fileName : fileNames) {
			for (final File file : fileList) {
				if (fileName.equalsIgnoreCase(file.getName())) {
					files.add(file);
				}
			}
		}
		return files;
	}

	/**
	 * Delete first point character from string.
	 *
	 * @param str
	 *            the str
	 * @return the string
	 */
	private String deleteFirstPointCharacterFromString(final String str) {
		return str.replaceFirst(".", "");
	}

}
