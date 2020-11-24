package com.agiletestware.bumblebee;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jenkinsci.remoting.RoleChecker;

import com.agiletestware.bumblebee.client.api.BulkUpdateParameters;
import com.agiletestware.bumblebee.client.api.DefaultBumblebeeApiProvider;
import com.agiletestware.bumblebee.client.uploader.ReportFilesUploaderFactory;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.Callable;

public class BumblebeeRemoteExecutor implements Callable<Void, Exception>, Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 3670838509646174454L;
	private final JenkinsBuildLogger log;
	private final FilePath workspace;
	private final BulkUpdateParameters parameters;

	public BumblebeeRemoteExecutor(final FilePath workspace,
			final BulkUpdateParameters parameters, final TaskListener listener) {
		this.workspace = workspace;
		this.parameters = parameters;
		log = new JenkinsBuildLogger(listener);
	}

	@Override
	public Void call() throws Exception {
		try {
			execute();
		} catch (final Exception ex) {
			log.error("Exception ocurred during report upload: " + ex.getMessage(), ex);
			throw ex;
		}
		return null;
	}

	public void execute() throws Exception {
		final List<File> filesToBeUploaded = locateBumbleBeeReports(workspace, parameters.getResultPattern());
		ReportFilesUploaderFactory.getReportFilesUploader(parameters.getFormat()).sendReportFiles(new DefaultBumblebeeApiProvider(), parameters, log,
				filesToBeUploaded, workspace.getRemote());
	}

	private List<File> locateBumbleBeeReports(final FilePath workspace, final String pattern) throws IOException, InterruptedException {
		final List<File> files = new ArrayList<>();
		try {
			final FilePath[] filePaths = workspace.list(pattern);
			if (filePaths.length > 0) {
				for (final FilePath filePath : filePaths) {
					files.add(new File(filePath.getRemote()));
				}
			}
		} catch (final Exception ex) {
			log.error("Error occurred during the search with pattern: " + pattern + ", error: " + ex.getMessage(), ex);
		}
		return files;
	}

	@Override
	public void checkRoles(final RoleChecker arg0) throws SecurityException {
	}
}
