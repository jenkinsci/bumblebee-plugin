package com.agiletestware.bumblebee.validator;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.ProxyConfiguration;
import hudson.util.FormValidation;

/**
 * Validates that given URL is available (sends GET request).
 *
 * @author Sergey Oplavin
 *
 */
public class UrlAvailableValidator implements Validator<String, Integer> {
	static final Logger LOGGER = Logger.getLogger(UrlAvailableValidator.class.getName());
	private final StringNotEmptyValidator<Void> notEmptyValidator;
	private final String errorMessage;

	/**
	 * Constructor.
	 *
	 * @param emptyErrorMessage
	 *            error message in case if URL is null or empty string.
	 * @param errorMessage
	 *            error message in case if URL is unreachable.
	 */
	public UrlAvailableValidator(final String emptyErrorMessage, final String errorMessage) {
		this.notEmptyValidator = new StringNotEmptyValidator<>(emptyErrorMessage);
		this.errorMessage = errorMessage;
	}

	@Override
	public FormValidation validate(final String value, final Integer timeout) {
		final FormValidation validation = notEmptyValidator.validate(value, null);
		if (FormValidation.Kind.OK != validation.kind) {
			return validation;
		}
		return isUrlReachable(value, timeout) ? FormValidation.ok() : FormValidation.error(MessageFormat.format(errorMessage, value));
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
}