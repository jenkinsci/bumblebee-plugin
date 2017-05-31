package com.agiletestware.bumblebee.validator;

import org.apache.commons.lang.StringUtils;

import hudson.util.FormValidation;

/**
 * Validator for HP ALM User field. It checks ALM and PC URLs and if one of then
 * is defined, then user name must not be empty.
 *
 * @author Sergey Oplavin
 *
 */
public enum HpUserValidator implements Validator<String, HpUrls> {

	THE_INSTANCE;
	private final StringNotEmptyValidator<Void> notEmptyValidator = new StringNotEmptyValidator<>("Login is required");

	@Override
	public FormValidation validate(final String value, final HpUrls urls) {
		if (StringUtils.isEmpty(urls.getAlmUrl()) && StringUtils.isEmpty(urls.getPcUrl())) {
			return FormValidation.ok();
		}
		return notEmptyValidator.validate(value, null);
	}

}