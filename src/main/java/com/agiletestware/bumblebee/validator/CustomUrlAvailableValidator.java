package com.agiletestware.bumblebee.validator;

import java.text.MessageFormat;

import com.agiletestware.bumblebee.client.utils.UrlAvailableValidator;

import hudson.util.FormValidation;

/**
 * Validates that given URL is available (sends GET request).
 *
 * @author Sergey Oplavin
 * @author Ayman BEN AMOR
 *
 */
public class CustomUrlAvailableValidator implements Validator<String, Integer> {

	private final StringNotEmptyValidator<Void> notEmptyValidator;
	private final String errorMessage;
	private final UrlAvailableValidator urlAvailableValidator;

	/**
	 * Instantiates a new custom url available validator.
	 *
	 * @param emptyErrorMessage
	 *            error message in case if URL is null or empty string.
	 * @param errorMessage
	 *            error message in case if URL is unreachable.
	 * @param urlAvailableValidator
	 *            the url available validator.
	 */
	public CustomUrlAvailableValidator(final String emptyErrorMessage, final String errorMessage, final UrlAvailableValidator urlAvailableValidator) {
		this.notEmptyValidator = new StringNotEmptyValidator<>(emptyErrorMessage);
		this.errorMessage = errorMessage;
		this.urlAvailableValidator = urlAvailableValidator;
	}

	@Override
	public FormValidation validate(final String value, final Integer timeout) {
		final FormValidation validation = notEmptyValidator.validate(value, null);
		if (FormValidation.Kind.OK != validation.kind) {
			return validation;
		}
		return urlAvailableValidator.isUrlReachable(value, timeout) ? FormValidation.ok() : FormValidation.error(MessageFormat.format(errorMessage, value));
	}
}
