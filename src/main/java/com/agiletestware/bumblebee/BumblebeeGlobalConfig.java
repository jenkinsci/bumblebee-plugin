package com.agiletestware.bumblebee;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

import com.agiletestware.bumblebee.client.api.BaseParameters;
import com.agiletestware.bumblebee.client.api.BumblebeeApi;
import com.agiletestware.bumblebee.util.BumblebeeUtils;

import hudson.Extension;
import hudson.ProxyConfiguration;
import hudson.model.AbstractProject;
import hudson.util.FormValidation;
import jenkins.model.GlobalConfiguration;

/**
 * Global configuration for Bumblebee plugin.
 *
 * @author Sergey Oplavin
 *
 */
@Extension
public class BumblebeeGlobalConfig extends GlobalConfiguration {
	/** Logger. */
	private static final Logger LOGGER = Logger.getLogger(BumblebeeGlobalConfig.class.getName());
	private static final String PLUGIN_HELP_PAGE_URI = "/plugin/bumblebee/help/main.html";
	private static final String PLUGIN_DISPLAY_NAME = "Bumblebee  HP  ALM  Uploader";
	private String bumblebeeUrl;
	private String qcUserName;
	private String password;
	private String qcUrl;
	private int timeOut;

	/**
	 * Constructor.
	 */
	public BumblebeeGlobalConfig() {
		load();
	}

	@Override
	public String getDisplayName() {
		return PLUGIN_DISPLAY_NAME;
	}

	@Override
	public String getHelpFile() {
		return PLUGIN_HELP_PAGE_URI;
	}

	public void populateBaseParameters(final BaseParameters params) {
		params.setBumbleBeeUrl(this.getBumblebeeUrl());
		params.setEncryptedPassword(this.getPassword());
		params.setAlmUserName(this.getQcUserName());
		params.setAlmUrl(this.getQcUrl());
	}

	// Used by global.jelly to authenticate User key
	public FormValidation doSaveConnection(@QueryParameter("bumblebeeUrl") final String bumblebeeUrl, @QueryParameter("qcUrl") final String qcUrl,
			@QueryParameter("qcUserName") final String qcUserName, @QueryParameter("password") final String password,
			@QueryParameter("timeOut") final int timeOut) {
		try {
			final String qcUrlTrimmed = StringUtils.trim(qcUrl);
			if (!isUrlReachable(qcUrlTrimmed, timeOut)) {
				return FormValidation.error("FAILED: Could not connect to " + qcUrlTrimmed);
			}
			final String bumblebeeUrlTrimmed = StringUtils.trim(bumblebeeUrl);
			if (!isUrlReachable(bumblebeeUrlTrimmed, timeOut)) {
				return FormValidation.error("FAILED: Could not connect to " + bumblebeeUrl);
			}
			this.qcUserName = qcUserName;
			this.qcUrl = qcUrlTrimmed;
			this.bumblebeeUrl = bumblebeeUrl;
			this.timeOut = timeOut;
			final BumblebeeApi bmapi = new BumblebeeApi(this.bumblebeeUrl, this.timeOut);
			// Set password only if old value is null/empty/blank OR if new
			// value is not equal to old
			if (StringUtils.isBlank(this.password) || !this.password.equals(password)) {
				this.password = bmapi.getEncryptedPassword(StringUtils.trim(password));
			}
			save();
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, null, e);
			return FormValidation.error("FAILED: " + e.getMessage());
		}
		return FormValidation.ok("Configuration  Saved");
	}

	/**
	 * Is given URL can be reached with HTTP.
	 *
	 * @param url
	 *            URL
	 * @param timeout
	 *            connection timeout. zero means infinite timeout.
	 * @return
	 */
	private boolean isUrlReachable(final String url, final int timeout) {
		try {
			final HttpURLConnection connection = (HttpURLConnection) ProxyConfiguration.open(new URL(url));
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
			connection.setRequestMethod("GET");
			final int responseCode = connection.getResponseCode();
			LOGGER.log(Level.INFO, url + " --> HTTP " + responseCode);
			return true;
		} catch (final Exception ex) {
			LOGGER.log(Level.SEVERE, "Could not get response from URL: " + url, ex);
		}
		return false;
	}

	public String getBumblebeeUrl() {
		return this.bumblebeeUrl;
	}

	public String getQcUserName() {
		return this.qcUserName;
	}

	public String getQcUrl() {
		return this.qcUrl;
	}

	public String getPassword() {
		return this.password;
	}

	public int getTimeOut() {
		return this.timeOut;
	}

	public FormValidation doCheckbumblebeeUrl(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String bumblebeeUrl)
			throws IOException, ServletException {
		return BumblebeeUtils.validatebumblebeeUrl(bumblebeeUrl);
	}

	public FormValidation doCheckqcUrl(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String qcUrl)
			throws IOException, ServletException {
		return BumblebeeUtils.validateqcUrl(qcUrl);
	}

	public FormValidation doCheckqcUserName(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String qcUserName)
			throws IOException, ServletException {
		return BumblebeeUtils.validateRequiredField(qcUserName);
	}

}
