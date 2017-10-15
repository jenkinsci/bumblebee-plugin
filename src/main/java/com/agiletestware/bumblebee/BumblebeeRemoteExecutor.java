package com.agiletestware.bumblebee;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jenkinsci.remoting.RoleChecker;

import com.agiletestware.bumblebee.client.api.BulkUpdateParameters;
import com.agiletestware.bumblebee.client.api.BumblebeeApi;
import com.agiletestware.bumblebee.client.api.BumblebeeApiImpl;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.Callable;

public class BumblebeeRemoteExecutor implements
Callable<Void, Exception>, Serializable {

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
		execute();
		return null;
	}

	public void execute() throws Exception {

		boolean errorSeen = false;

		final List<FilePath> filesToBeUploaded = locateBumbleBeeReports(
				workspace, parameters.getResultPattern());
		if (filesToBeUploaded.size() == 0) {
			throw new Exception("Did not find any file matching the pattern "
					+ parameters.getResultPattern() + ". Please check pattern");
		}

		try (final BumblebeeApi api = new BumblebeeApiImpl(parameters.getBumbleBeeUrl(),
				parameters.getTimeOut() * 60)) {
			for (final FilePath filePath : filesToBeUploaded) {
				final boolean fileUploaded = api.sendSingleTestReport(parameters, new File(filePath.getRemote()),
						log);
				if (!fileUploaded && !errorSeen) {
					errorSeen = true;
				}
				log.info("--------------------------");
			}
		}

		if (errorSeen) {
			throw new Exception(
					"[Bumblebee] Could not upload results to HP ALM using the following parameters: " + parameters
					+ " , HP URL " + parameters.getAlmUrl()
					+ ". Please check settings.");
		}

		log.info("Upload done");
	}

	// XXX legacy code, but working
	private List<FilePath> locateBumbleBeeReports(final FilePath workspace,
			final String includes) throws IOException, InterruptedException {

		// First use ant-style pattern
		try {
			final FilePath[] ret = workspace.list(includes);
			if (ret.length > 0) {
				return Arrays.asList(ret);
			}
		} catch (final IOException e) {
			// Do nothing.
		}

		// If it fails, do a legacy search
		final ArrayList<FilePath> files = new ArrayList<FilePath>();
		final String parts[] = includes.split("\\s*[;:,]+\\s*");
		for (final String path : parts) {
			final FilePath src = workspace.child(path);
			if (src.exists()) {
				if (src.isDirectory()) {
					files.addAll(Arrays.asList(src.list("**/*")));
				} else {
					files.add(src);
				}
			}
		}
		return files;
	}

	@Override
	public void checkRoles(final RoleChecker arg0) throws SecurityException {
	}

}
