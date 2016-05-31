package com.agiletestware.bumblebee;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jenkinsci.remoting.RoleChecker;

import com.agiletestware.bumblebee.client.api.BumblebeeApi;
import com.agiletestware.bumblebee.client.api.BulkUpdateParameters;
import com.agiletestware.bumblebee.util.StringBuilderWrapper;

import hudson.FilePath;
import hudson.remoting.Callable;

public class BumblebeeRemoteExecutor implements
		Callable<String, RemoteExecutorException>, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3670838509646174454L;

	private final JenkinsBuildLogger log = new JenkinsBuildLogger(new StringBuilderWrapper());
	private final FilePath workspace;
	private final BulkUpdateParameters parameters;

	public BumblebeeRemoteExecutor(final FilePath workspace,
			final BulkUpdateParameters parameters) {
		this.workspace = workspace;
		this.parameters = parameters;
	}

	@Override
	public String call() throws RemoteExecutorException {
		try {
			return execute();
		} catch (final Throwable t) {
			// return execution log to caller
			throw new RemoteExecutorException(t, log.toString());
		}
	}

	public String execute() throws Exception {
		final BumblebeeApi api = new BumblebeeApi(parameters.getBumbleBeeUrl(),
				parameters.getTimeOut() * 60);

		boolean errorSeen = false;

		final List<FilePath> filesToBeUploaded = locateBumbleBeeReports(
				workspace, parameters.getResultPattern());
		if (filesToBeUploaded.size() == 0) {
			throw new Exception("Did not find any file matching the pattern "
					+ parameters.getResultPattern() + ". Please check pattern");
		}

		for (final FilePath filePath : filesToBeUploaded) {
			final boolean fileUploaded = api.sendSingleTestReport(parameters, new File(filePath.getRemote()),
					log);
			if (!fileUploaded && !errorSeen) {
				errorSeen = true;
			}
			log.info("--------------------------");
		}

		if (errorSeen) {
			throw new Exception(
					"[Bumblebee] Could not upload results to HP ALM using the following arameters: " + parameters
							+ " , HP URL " + parameters.getAlmUrl()
							+ ". Please check settings.");
		}

		log.info("Upload done");
		return log.toString();
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

	public static class Parameters implements Serializable {

		/**
		 *
		 */
		private static final long serialVersionUID = 7098445803055973230L;
		private String bumbleBeeUrl;
		private String domain;
		private String project;
		private String testplandirectory;
		private String testlabdirectory;
		private String format;
		private String qcUserName;
		private String testSet;
		private String resultPattern;
		private String qcUrl;
		private String mode;
		private String encryptedPassword;
		private String customProperties;
		private int timeOut;

		public String getBumbleBeeUrl() {
			return bumbleBeeUrl;
		}

		public void setBumbleBeeUrl(final String bumbleBeeUrl) {
			this.bumbleBeeUrl = bumbleBeeUrl;
		}

		public String getDomain() {
			return domain;
		}

		public void setDomain(final String domain) {
			this.domain = domain;
		}

		public String getProject() {
			return project;
		}

		public void setProject(final String project) {
			this.project = project;
		}

		public String getTestplandirectory() {
			return testplandirectory;
		}

		public void setTestplandirectory(final String testplandirectory) {
			this.testplandirectory = testplandirectory;
		}

		public String getTestlabdirectory() {
			return testlabdirectory;
		}

		public void setTestlabdirectory(final String testlabdirectory) {
			this.testlabdirectory = testlabdirectory;
		}

		public String getFormat() {
			return format;
		}

		public void setFormat(final String format) {
			this.format = format;
		}

		public String getQcUserName() {
			return qcUserName;
		}

		public void setQcUserName(final String qcUserName) {
			this.qcUserName = qcUserName;
		}

		public String getTestSet() {
			return testSet;
		}

		public void setTestSet(final String testSet) {
			this.testSet = testSet;
		}

		public String getResultPattern() {
			return resultPattern;
		}

		public void setResultPattern(final String resultPattern) {
			this.resultPattern = resultPattern;
		}

		public String getQcUrl() {
			return qcUrl;
		}

		public void setQcUrl(final String qcUrl) {
			this.qcUrl = qcUrl;
		}

		public String getEncryptedPassword() {
			return encryptedPassword;
		}

		public void setEncryptedPassword(final String encryptedPassword) {
			this.encryptedPassword = encryptedPassword;
		}

		public void setMode(final String mode) {
			this.mode = mode;
		}

		public String getMode() {
			return mode;
		}

		public void setTimeOut(final int timeOut) {
			this.timeOut = timeOut;
		}

		public int getTimeOut() {
			return timeOut;
		}

		public void setCustomProperties(final String customProperties) {
			this.customProperties = customProperties;
		}

		public String getCustomProperties() {
			return this.customProperties;
		}

	}

	@Override
	public void checkRoles(final RoleChecker arg0) throws SecurityException {
	}

}
