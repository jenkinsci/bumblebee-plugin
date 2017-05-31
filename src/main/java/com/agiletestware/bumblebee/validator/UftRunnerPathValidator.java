package com.agiletestware.bumblebee.validator;

import org.apache.commons.lang.StringUtils;

import hudson.util.FormValidation;

/**
 * Validates UFT Batch Runner path.
 *
 * @author Sergey Oplavin
 *
 */
public enum UftRunnerPathValidator implements Validator<String, Void> {

	THE_INSTANCE;
	private static final String UFT_BATCH_RUNNER_CMD = "UFTBatchRunnerCMD.exe";

	@Override
	public FormValidation validate(final String value, final Void param) {
		if (StringUtils.isEmpty(value)) {
			return FormValidation.ok();
		}
		return value.endsWith(UFT_BATCH_RUNNER_CMD) ? FormValidation.ok() : FormValidation.error("Must end with " + UFT_BATCH_RUNNER_CMD);
	}

}