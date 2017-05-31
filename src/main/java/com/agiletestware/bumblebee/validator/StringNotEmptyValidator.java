package com.agiletestware.bumblebee.validator;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;

import hudson.util.FormValidation;

/**
 * Validates that the given string is not empty.
 *
 * @author Sergey Oplavin
 *
 * @param <P>
 *            parameter type.
 */
public class StringNotEmptyValidator<P> implements Validator<String, P> {

	private final String errorMessageFormat;

	/**
	 * Constructor.
	 *
	 * @param errorMessageFormat
	 *            error message format. When error message is created, it uses
	 *            {@link MessageFormat#format(String, Object...)} method to
	 *            generate actual message. Value is passed as a parameter.
	 */
	public StringNotEmptyValidator(final String errorMessageFormat) {
		this.errorMessageFormat = errorMessageFormat;
	}

	@Override
	public FormValidation validate(final String value, final P param) {
		return StringUtils.isEmpty(value) ? FormValidation.error(getErrorMessage(value)) : FormValidation.ok();
	}

	protected String getErrorMessage(final String value) {
		return MessageFormat.format(errorMessageFormat, value);
	}

}