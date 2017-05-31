package com.agiletestware.bumblebee.validator;

import hudson.util.FormValidation;

/**
 * Validates that the string value starts with a particular substring.
 *
 * @author Sergey Oplavin
 *
 */
public class StringStartsWithValidator implements Validator<String, Void> {

	private final StringNotEmptyValidator<Void> notEmptyValidator;
	private final String startsWith;
	private final String errorMessage;

	/**
	 * Constructor.
	 *
	 * @param startsWith
	 *            prefix from which value must start.
	 * @param errorMessageOnEmpty
	 *            error message in case if value is null or empty.
	 * @param errorMessage
	 *            error message in case if value does not start with given
	 *            prefix.
	 */
	public StringStartsWithValidator(final String startsWith, final String errorMessageOnEmpty, final String errorMessage) {
		if (startsWith == null) {
			throw new IllegalArgumentException("startWith cannot be null");
		}
		if (errorMessage == null) {
			throw new IllegalArgumentException("errorMessage cannot be null");
		}
		this.notEmptyValidator = new StringNotEmptyValidator<>(errorMessageOnEmpty);
		this.startsWith = startsWith;
		this.errorMessage = errorMessage;
	}

	@Override
	public FormValidation validate(final String value, final Void param) {
		final FormValidation validation = notEmptyValidator.validate(value, null);
		if (FormValidation.Kind.ERROR == validation.kind) {
			return validation;
		}
		return value.startsWith(startsWith) ? FormValidation.ok() : FormValidation.error(errorMessage);
	}

}
