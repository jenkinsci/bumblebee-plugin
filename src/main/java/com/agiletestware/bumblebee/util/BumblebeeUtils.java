/**
 *
 */
package com.agiletestware.bumblebee.util;

import hudson.FilePath;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.util.FormValidation;

import java.util.regex.Pattern;

/**
 * @author c_rsharv
 * @author Sergey Oplavin (refactored and cleaned up code)
 *
 */
public class BumblebeeUtils {

	public static FormValidation validatebumblebeeUrl(final String bumblebeeUrl) {
		final String t = Util.fixEmptyAndTrim(bumblebeeUrl);
		if ((t == null)
				|| (!t.matches("^(https?)://[0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])*(:\\d*[^/])?\\/bumblebee$"))) {
			return FormValidation
					.error("URL  should be  http(s)://<bumblebee_server>:<port>/bumblebee");
		}
		return FormValidation.ok();
	}

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
					.error("Custom properties  should  be  name=value and    seperated  by  commas ");
		} catch (final Exception anye) {
			return FormValidation
					.error("Custom properties  should  be  name=value and    seperated  by  commas "
							+ anye.getMessage());

		}
	}

	// FIXME: seems obsolete - clarify and remove.
	public static FormValidation validateTimeOut(String timeOut) {
		try {
			final String t = Util.fixEmptyAndTrim(timeOut);
			final Pattern p = Pattern.compile("\\d*");

			if (p.matcher(t).matches()) {
				return FormValidation.ok();
			}

			return FormValidation.error("TimeOut needs   to be   a  integer ");
		} catch (final Exception anye) {
			timeOut = "0";
			return FormValidation.error("TimeOut needs   to be   a  integer  "
					+ anye.getMessage());

		}
	}

	public static FormValidation validateqcUrl(final String qcUrl) {
		final String t = Util.fixEmptyAndTrim(qcUrl);

		if ((t == null)
				|| !t.matches("^(https?)\\:\\/\\/[0-9a-zA-Z]([-.\\w]*[0-9a-zA-Z])*(:\\d*[^\\/])?\\/qcbin$")) {
			return FormValidation
					.error("Url should  be  http(s)://<qcserver>:<qcport>/qcbin");
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
		final String preparedVal = Util.fixEmptyAndTrim(fieldValue);
		if (preparedVal == null) {
			return FormValidation.error("Required.");
		}
		return FormValidation.ok();
	}

	public static FormValidation validateTestPlan(final String testPlan) {
		final String t = Util.fixEmptyAndTrim(testPlan);
		if (t == null) {
			return FormValidation.error("Required.");
		}

		if (!t.startsWith("Subject\\") || t.contains("^") || t.contains("*")) {
			return FormValidation
					.error("Test Plan must begin with 'Subject\\' and  cannot  contain  '^' or  '*'");
		}
		return FormValidation.ok();
	}

	public static FormValidation validateTestLab(final String testLab) {
		final String t = Util.fixEmptyAndTrim(testLab);
		if (t == null) {
			return FormValidation.error("Required.");
		}
		if (!t.startsWith("Root\\") || t.contains("^") || t.contains("*")) {
			return FormValidation
					.error("Test Lab must begin with 'Root\\' and  cannot contain  '^' or '*'");
		}
		return FormValidation.ok();
	}

	public static FormValidation validateTestSet(final String testSet) {
		final String t = Util.fixEmptyAndTrim(testSet);
		if (t == null) {
			return FormValidation.error("Required.");
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
