package com.agiletestware.bumblebee;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;

import com.agiletestware.bumblebee.client.api.BaseParameters;
import com.agiletestware.bumblebee.client.api.BumblebeeApi;
import com.agiletestware.bumblebee.client.api.BumblebeeApiImpl;
import com.agiletestware.bumblebee.client.utils.Messages;
import com.agiletestware.bumblebee.client.utils.UrlAvailableValidator;
import com.agiletestware.bumblebee.validator.CustomUrlAvailableValidator;
import com.agiletestware.bumblebee.validator.HpUrls;
import com.agiletestware.bumblebee.validator.HpUserValidator;
import com.agiletestware.bumblebee.validator.RegExpMatchValidator;
import com.agiletestware.bumblebee.validator.UftRunnerPathValidator;

import hudson.Extension;
import hudson.Util;
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
	static final Logger LOGGER = Logger.getLogger(BumblebeeGlobalConfig.class.getName());
	private static final String PLUGIN_HELP_PAGE_URI = "/plugin/bumblebee/help/main.html";
	private static final String PLUGIN_DISPLAY_NAME = "Bumblebee  HP  ALM  Uploader";
	private static final CustomUrlAvailableValidator BUMBLEBEE_URL_VALIDATOR = new CustomUrlAvailableValidator("Bumblebee URL is required",
			"FAILED: Could not connect to {0}", new UrlAvailableValidator(new JenkinsLogger(BumblebeeGlobalConfig.class)));
	private static final RegExpMatchValidator BUMBLEBEE_URL_REGEXP_VALIDATOR = new RegExpMatchValidator(
			"Bumblebee URL should be http(s)://<bumblebee_server>:<port>/bumblebee", "^(https?)://[0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])*(:\\d*[^/])?\\/bumblebee$");
	private static final RegExpMatchValidator ALM_URL_REGEXP_VALIDATOR = new RegExpMatchValidator("HP ALM URL should be http(s)://<qcserver>:<qcport>/qcbin",
			"^(https?)\\:\\/\\/[0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])*(:\\d*[^\\/])?\\/qcbin$");
	private String bumblebeeUrl;
	private String qcUserName;
	private String password;
	private String qcUrl;
	private int timeOut;
	private String uftRunnerPath;
	private String pcUrl;
	private int pcTimeOut;
	private boolean skipConnectivityDiagnostic;

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
	public FormValidation doSaveConnection(
			@QueryParameter("bumblebeeUrl") final String bumblebeeUrl,
			@QueryParameter("qcUrl") final String qcUrl,
			@QueryParameter("qcUserName") final String qcUserName,
			@QueryParameter("password") final String password,
			@QueryParameter("timeOut") final int timeOut,
			@QueryParameter("uftRunnerPath") final String uftRunnerPath,
			@QueryParameter("pcUrl") final String pcUrl,
			@QueryParameter("pcTimeOut") final int pcTimeOut,
			@QueryParameter("skipConnectivityDiagnostic") final boolean skipConnectivityDiagnostic) {
		final String bumblebeeUrlTrimmed = Util.fixEmptyAndTrim(bumblebeeUrl);
		final String qcUrlTrimmed = Util.fixEmptyAndTrim(qcUrl);
		final String userNameTrimmed = Util.fixEmptyAndTrim(qcUserName);
		final String uftRunnerPathTrimmed = Util.fixEmptyAndTrim(uftRunnerPath);
		final String pcUrlTrimmed = Util.fixEmptyAndTrim(pcUrl);

		try {
			final List<FormValidation> validators = new ArrayList<>();
			if (!skipConnectivityDiagnostic) {
				validators.add(BUMBLEBEE_URL_VALIDATOR.validate(bumblebeeUrlTrimmed, timeOut));
			}
			validators.addAll(Arrays.asList(
					HpUserValidator.THE_INSTANCE.validate(userNameTrimmed, new HpUrls(qcUrl, pcUrl)),
					UftRunnerPathValidator.THE_INSTANCE.validate(uftRunnerPathTrimmed, null)));
			final FormValidation validation = FormValidation.aggregate(validators);
			if (FormValidation.Kind.ERROR == validation.kind) {
				return validation;
			}

			this.bumblebeeUrl = bumblebeeUrlTrimmed;
			this.uftRunnerPath = uftRunnerPathTrimmed;
			this.qcUrl = qcUrlTrimmed;
			this.qcUserName = userNameTrimmed;
			this.timeOut = timeOut;
			this.pcUrl = pcUrlTrimmed;
			this.pcTimeOut = pcTimeOut;
			this.skipConnectivityDiagnostic = skipConnectivityDiagnostic;

			try (final BumblebeeApi bmapi = new BumblebeeApiImpl(this.bumblebeeUrl, this.timeOut)) {
				// Set password only if old value is null/empty/blank OR if new
				// value is not equal to old
				if (StringUtils.isBlank(this.password) || !this.password.equals(password)) {
					this.password = skipConnectivityDiagnostic ? StringUtils.trim(password) : bmapi.getEncryptedPassword(StringUtils.trim(password));
				}
			}
			save();
		} catch (final Exception e) {
			LOGGER.log(Level.SEVERE, null, e);
			return FormValidation.error("FAILED: " + e.getMessage());
		}
		return FormValidation.ok("Configuration Saved");
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

	public String getUftRunnerPath() {
		return uftRunnerPath;
	}

	public String getPcUrl() {
		return pcUrl;
	}

	public int getPcTimeOut() {
		return pcTimeOut;
	}

	public boolean isSkipConnectivityDiagnostic() {
		return skipConnectivityDiagnostic;
	}

	public FormValidation doCheckBumblebeeUrl(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String bumblebeeUrl)
			throws IOException, ServletException {
		return BUMBLEBEE_URL_REGEXP_VALIDATOR.validate(bumblebeeUrl, null);
	}

	public FormValidation doCheckQcUrl(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String qcUrl)
			throws IOException, ServletException {
		if (StringUtils.isEmpty(qcUrl)) {
			return FormValidation.ok();
		}
		return ALM_URL_REGEXP_VALIDATOR.validate(qcUrl, null);
	}

	public FormValidation doCheckQcUserName(@AncestorInPath final AbstractProject<?, ?> project, @QueryParameter final String qcUserName,
			@QueryParameter final String qcUrl, @QueryParameter final String pcUrl)
					throws IOException, ServletException {
		return HpUserValidator.THE_INSTANCE.validate(qcUserName, new HpUrls(qcUrl, pcUrl));
	}

	public FormValidation doCheckUftRunnerPath(@QueryParameter final String uftRunnerPath) {
		return UftRunnerPathValidator.THE_INSTANCE.validate(uftRunnerPath, null);
	}

	public FormValidation doCheckSkipConnectivityDiagnostic(@QueryParameter final boolean skipConnectivityDiagnostic) {
		return skipConnectivityDiagnostic ? FormValidation.warning(Messages.THE_INSTANCE.getWarningMessage(bumblebeeUrl)) : FormValidation.ok();
	}

}
