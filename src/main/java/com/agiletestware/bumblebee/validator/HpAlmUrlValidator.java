package com.agiletestware.bumblebee.validator;

import org.apache.commons.lang.StringUtils;

import hudson.util.FormValidation;

/**
 * Validates HP ALM URL.
 *
 * @author Sergey Oplavin
 *
 */
public enum HpAlmUrlValidator implements Validator<String, Integer> {

	THE_INSTANCE;
	private final UrlAvailableValidator urlValidator = new UrlAvailableValidator("HP ALM is required", "FAILED: Could not connect to {0}");;

	@Override
	public FormValidation validate(final String value, final Integer timeout) {
		if (StringUtils.isEmpty(value)) {
			return FormValidation.ok();
		}
		return urlValidator.validate(value, timeout);
	}

}