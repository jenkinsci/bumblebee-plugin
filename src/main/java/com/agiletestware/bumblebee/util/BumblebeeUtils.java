/**
 *
 */
package com.agiletestware.bumblebee.util;

import java.util.regex.Pattern;

import hudson.FilePath;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.util.FormValidation;

/**
 * @author c_rsharv
 * @author Sergey Oplavin (refactored and cleaned up code)
 *
 */
public class BumblebeeUtils {

	private static final String REQUIRED = "Required";


	// FIXME: seems obsolete - clarify and remove.
	public static FormValidation validateCustomProperties(
			final String customProperties) {
		try {
			final String t = Util.fixEmptyAndTrim(customProperties);
			final Pattern p = Pattern
					.compile("(\\w+)=\"*((?<=\")[^\"]+(?=\")|([^\\s]+))\"*");

			if (p.matcher(t).matches()) {
				return FormValidation.ok();
			}

			return FormValidation
					.error("Custom properties should be name=value and seperated by commas");
		} catch (final Exception anye) {
			return FormValidation
					.error("Custom properties should be name=value and seperated by commas "
							+ anye.getMessage());

		}
	}

	public static FormValidation validateqcUrl(final String qcUrl) {
		final String t = Util.fixEmptyAndTrim(qcUrl);

		if ((t == null)
				|| !t.matches("^(https?)\\:\\/\\/[0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])*(:\\d*[^\\/])?\\/qcbin$")) {
			return FormValidation
					.error("Url should be http(s)://<qcserver>:<qcport>/qcbin");
		}

		return FormValidation.ok();

	}

	/**
	 * Performs validation of required field.
	 *
	 * @param fieldValue
	 *            the value of field
	 * @return {@link FormValidation#ok()} if field is not empty,
	 *         {@link FormValidation#error(String)} otherwise.
	 */
	public static FormValidation validateRequiredField(final String fieldValue) {
		// Util.fixEmptyAndTrim returns null for empty or blank string
		final String preparedVal = Util.fixEmptyAndTrim(fieldValue);
		if (preparedVal == null) {
			return FormValidation.error(REQUIRED);
		}
		return FormValidation.ok();
	}

	public static FormValidation validateTestPlan(final String testPlan) {
		final String t = Util.fixEmptyAndTrim(testPlan);
		if (t == null) {
			return FormValidation.error(REQUIRED);
		}

		if (!t.startsWith("Subject\\") || t.contains("^") || t.contains("*")) {
			return FormValidation
					.error("Test Plan must begin with 'Subject\\' and cannot contain '^' or '*'");
		}
		return FormValidation.ok();
	}

	public static FormValidation validateTestLab(final String testLab) {
		return validateTestLab(testLab, "Test Lab");
	}

	public static FormValidation validateTestLab(final String testLabPath, final String paramName) {
		final String t = Util.fixEmptyAndTrim(testLabPath);
		if (t == null) {
			return FormValidation.error(REQUIRED);
		}
		if (!t.startsWith("Root\\") || t.contains("^") || t.contains("*")) {
			return FormValidation
					.error(paramName + " must begin with 'Root\\' and cannot contain '^' or '*'");
		}
		return FormValidation.ok();
	}

	public static FormValidation validateTestSet(final String testSet) {
		final String t = Util.fixEmptyAndTrim(testSet);
		if (t == null) {
			return FormValidation.error(REQUIRED);
		}
		if (t.contains("^") || t.contains(",") || t.contains("\"")
				|| t.contains("*")) {
			return FormValidation
					.error("Test Set value cannot contain these characters ^ , \" *");
		}
		return FormValidation.ok();
	}

	public static String maskPasswordInString(final String strWithPassword) {
		return strWithPassword.replaceAll("password=[^&]*", "password=*******");
	}

	public static String maskPasswordInResponse(final String xmlResponse) {
		return maskPasswordInString(xmlResponse.replaceAll(
				"<encrypted_password>[^<]*", "<encrypted_password>*******"));
	}

	public static FilePath getWorkspace(final AbstractBuild build) {
		FilePath workspace = build.getWorkspace();
		if (workspace == null) {
			workspace = build.getProject().getSomeWorkspace();
		}
		return workspace;
	}

}
