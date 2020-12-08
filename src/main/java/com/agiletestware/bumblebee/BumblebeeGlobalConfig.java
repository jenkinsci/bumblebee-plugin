package com.agiletestware.bumblebee;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.interceptor.RequirePOST;

import com.agiletestware.bumblebee.client.api.BaseParameters;
import com.agiletestware.bumblebee.client.api.BumblebeeApi;
import com.agiletestware.bumblebee.client.api.BumblebeeApiProvider;
import com.agiletestware.bumblebee.client.api.DefaultBumblebeeApiProvider;
import com.agiletestware.bumblebee.client.utils.Messages;
import com.agiletestware.bumblebee.client.utils.UrlAvailableValidator;
import com.agiletestware.bumblebee.encryption.CustomSecret;
import com.agiletestware.bumblebee.encryption.DefaultCustomSecret;
import com.agiletestware.bumblebee.validator.CustomUrlAvailableValidator;
import com.agiletestware.bumblebee.validator.HpUrls;
import com.agiletestware.bumblebee.validator.HpUserValidator;
import com.agiletestware.bumblebee.validator.RegExpMatchValidator;
import com.agiletestware.bumblebee.validator.UftRunnerPathValidator;
import com.agiletestware.bumblebee.validator.Validator;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.model.GlobalConfiguration;
import jenkins.model.Jenkins;

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
	private final transient BumblebeeApiProvider apiProvider;
	private final transient CustomSecret secretHelper;
	private final transient Validator<String, Integer> bumblebeeUrlValidator;
	private String bumblebeeUrl;
	private String qcUserName;
	private Secret password;
	private String qcUrl;
	private int timeOut;
	private String uftRunnerPath;
	private String pcUrl;
	private int pcTimeOut;
	private boolean skipConnectivityDiagnostic;
	private boolean trustSelfSignedCerts;

	/**
	 * Constructor.
	 */
	public BumblebeeGlobalConfig() {
		this(new DefaultBumblebeeApiProvider(), DefaultCustomSecret.THE_INSTANCE, BUMBLEBEE_URL_VALIDATOR);
	}

	BumblebeeGlobalConfig(final BumblebeeApiProvider apiProvider, final CustomSecret secretHelper, final Validator<String, Integer> bumblebeeUrlValidator) {
		this.apiProvider = apiProvider;
		this.secretHelper = secretHelper;
		this.bumblebeeUrlValidator = bumblebeeUrlValidator;
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
		params.setEncryptedPassword(this.getPasswordPlain());
		params.setAlmUserName(this.getQcUserName());
		params.setAlmUrl(this.getQcUrl());
	}

	// Used by global.jelly to authenticate User key
	@RequirePOST
	public FormValidation doSaveConnection(
			@QueryParameter("bumblebeeUrl") final String bumblebeeUrl,
			@QueryParameter("qcUrl") final String qcUrl,
			@QueryParameter("qcUserName") final String qcUserName,
			@QueryParameter("password") final Secret password,
			@QueryParameter("timeOut") final int timeOut,
			@QueryParameter("uftRunnerPath") final String uftRunnerPath,
			@QueryParameter("pcUrl") final String pcUrl,
			@QueryParameter("pcTimeOut") final int pcTimeOut,
			@QueryParameter("skipConnectivityDiagnostic") final boolean skipConnectivityDiagnostic,
			@QueryParameter("trustSelfSignedCerts") final boolean trustSelfSignedCerts) {
		Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
		final String bumblebeeUrlTrimmed = Util.fixEmptyAndTrim(bumblebeeUrl);
		final String qcUrlTrimmed = Util.fixEmptyAndTrim(qcUrl);
		final String userNameTrimmed = Util.fixEmptyAndTrim(qcUserName);
		final String uftRunnerPathTrimmed = Util.fixEmptyAndTrim(uftRunnerPath);
		final String pcUrlTrimmed = Util.fixEmptyAndTrim(pcUrl);

		try {
			final List<FormValidation> validators = new ArrayList<>();
			if (!skipConnectivityDiagnostic) {
				validators.add(bumblebeeUrlValidator.validate(bumblebeeUrlTrimmed, (int) TimeUnit.MINUTES.toSeconds(timeOut)));
			}
			validators.addAll(Arrays.asList(
					HpUserValidator.THE_INSTANCE.validate(userNameTrimmed, new HpUrls(qcUrl, pcUrl)),
					UftRunnerPathValidator.THE_INSTANCE.validate(uftRunnerPathTrimmed, null)));
			final FormValidation validation = FormValidation.aggregate(validators);
			if (FormValidation.Kind.ERROR == validation.kind) {
				return validation;
			}
			setPassword(password, bumblebeeUrlTrimmed, timeOut, skipConnectivityDiagnostic, trustSelfSignedCerts);
			this.bumblebeeUrl = bumblebeeUrlTrimmed;
			this.uftRunnerPath = uftRunnerPathTrimmed;
			this.qcUrl = qcUrlTrimmed;
			this.qcUserName = userNameTrimmed;
			this.timeOut = timeOut;
			this.pcUrl = pcUrlTrimmed;
			this.pcTimeOut = pcTimeOut;
			this.skipConnectivityDiagnostic = skipConnectivityDiagnostic;
			this.trustSelfSignedCerts = trustSelfSignedCerts;
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

	public Secret getPassword() {
		return password;
	}

	public String getPasswordPlain() {
		return secretHelper.getPlainText(password);
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

	public boolean isTrustSelfSignedCerts() {
		return trustSelfSignedCerts;
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

	private void setPassword(final Secret newPassword, final String bumblebeeUrl, final int timeOutMinutes, final boolean skipConnectivityDiag,
			final boolean trustSelfSignedCerts) throws Exception {
		if (ObjectUtils.equals(this.password, newPassword)) {
			return;
		}
		if (skipConnectivityDiag) {
			this.password = newPassword;
			return;
		}

		try (final BumblebeeApi bmapi = apiProvider.provide(bumblebeeUrl, (int) TimeUnit.MINUTES.toSeconds(timeOutMinutes), trustSelfSignedCerts)) {
			this.password = secretHelper.getSecret(bmapi.getEncryptedPassword(secretHelper.getPlainText(newPassword)));
		}
	}

}
