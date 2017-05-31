package com.agiletestware.bumblebee.validator;

import hudson.util.FormValidation;

/**
 * Validates that value matches given regular expression.
 *
 * @author Sergey Oplavin
 *
 */
public class RegExpMatchValidator extends StringNotEmptyValidator<Void> {

	private final String regExp;

	/**
	 * Constructor.
	 *
	 * @param errorMessage
	 *            error message.
	 * @param regExp
	 *            regular expression.
	 */
	public RegExpMatchValidator(final String errorMessage, final String regExp) {
		super(errorMessage);
		this.regExp = regExp;
	}

	@Override
	public FormValidation validate(final String value, final Void param) {
		final FormValidation validation = super.validate(value, param);
		if (FormValidation.Kind.OK != validation.kind) {
			return validation;
		}
		return value.matches(regExp) ? FormValidation.ok() : FormValidation.error(getErrorMessage(value));
	}

}