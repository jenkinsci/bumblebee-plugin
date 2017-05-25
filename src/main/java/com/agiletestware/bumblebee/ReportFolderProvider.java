package com.agiletestware.bumblebee;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.agiletestware.bumblebee.client.runner.FileProvider;

/**
 * Provides location of report folder location. Location is relative to the
 * given root directory.
 *
 * @author Sergey Oplavin
 *
 */
public class ReportFolderProvider implements FileProvider {

	private final File rootDirectory;

	/**
	 * Constructor.
	 *
	 * @param rootDirectory
	 *            root directory.
	 */
	public ReportFolderProvider(final File rootDirectory) {
		if (rootDirectory == null) {
			throw new NullPointerException("Root directory is null");
		}
		this.rootDirectory = rootDirectory;
	}

	@Override
	public File getFile(final String fileName) throws IOException {
		final File outputDir = new File(rootDirectory, fileName);
		if (!outputDir.exists()) {
			FileUtils.forceMkdir(outputDir);
		}
		return outputDir;
	}

}
