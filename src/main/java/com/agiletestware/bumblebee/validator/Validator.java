package com.agiletestware.bumblebee.validator;

import hudson.util.FormValidation;

/**
 * Simple validator.
 *
 * @author Sergey Oplavin
 *
 * @param <V>
 *            type of value
 * @param <P>
 *            type of parameter
 */
public interface Validator<V, P> {

	/**
	 * Validates the given value.
	 *
	 * @param value
	 *            value.
	 * @param param
	 *            additional parameter.
	 * @return validation result.
	 */
	FormValidation validate(V value, P param);
}