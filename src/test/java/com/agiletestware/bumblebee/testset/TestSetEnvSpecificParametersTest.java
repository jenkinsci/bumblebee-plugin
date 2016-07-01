package com.agiletestware.bumblebee.testset;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.agiletestware.bumblebee.client.api.AlmRunMode;
import com.agiletestware.bumblebee.client.testrunner.TestSetRunnerParameters;
import com.agiletestware.bumblebee.client.testrunner.TestSetRunnerParametersImpl;

import hudson.EnvVars;

/**
 * Tests for {@link TestSetEnvSpecificParameters}.
 *
 * @author Sergey Oplavin
 *
 */
public class TestSetEnvSpecificParametersTest {

	@Test
	public void testGetters() {
		final String key = "BUILD_ID";
		final String envVar = "${" + key + "}";
		final String envVarValue = "build";
		final String almUrl = "url";
		final String bumblebeeUrl = "bbe url";
		final String domain = "domain";
		final String user = "user";
		final String password = "pwd";
		final String project = "project";
		final String host = "host";
		final String outputDir = "outputDir";
		final int timeout = 5;
		final AlmRunMode runMode = AlmRunMode.SCHEDULED;
		final List<String> testSets = Arrays.asList("suite1", "suite2");
		final Map<String, String> envMap = new HashMap<>();
		envMap.put(key, envVarValue);
		final TestSetRunnerParameters params = new TestSetRunnerParametersImpl();
		params.setAlmUrl(almUrl + envVar);
		params.setBumbleBeeUrl(envVar + bumblebeeUrl);
		params.setDomain(domain + envVar);
		params.setProject(project + envVar);
		params.setAlmUserName(user + envVar);
		params.setEncryptedPassword(password + envVar);
		params.setHost(host + envVar);
		params.setOutputDirPath(outputDir + envVar);
		params.setRunMode(runMode);
		params.setTimeOut(timeout);
		params.setTestSets(addStringToAll(testSets, envVarValue));

		final TestSetEnvSpecificParameters decoratedParams = new TestSetEnvSpecificParameters(params, new EnvVars(envMap));
		assertEquals(almUrl + envVarValue, decoratedParams.getAlmUrl());
		assertEquals(envVarValue + bumblebeeUrl, decoratedParams.getBumbleBeeUrl());
		assertEquals(domain + envVarValue, decoratedParams.getDomain());
		assertEquals(project + envVarValue, decoratedParams.getProject());
		assertEquals(user + envVarValue, decoratedParams.getAlmUserName());
		// password shall not be processed anyhow
		assertEquals(password + envVar, decoratedParams.getEncryptedPassword());
		assertEquals(host + envVarValue, decoratedParams.getHost());
		assertEquals(outputDir + envVarValue, decoratedParams.getOutputDirPath());
		assertEquals(runMode, decoratedParams.getRunMode());
		assertEquals(timeout, decoratedParams.getTimeOut());
		assertEquals(addStringToAll(testSets, envVarValue), decoratedParams.getTestSets());
	}

	private List<String> addStringToAll(final List<String> originalSets, final String envVar) {
		final List<String> setsWithEnvVars = new ArrayList<>(originalSets.size());
		for (final String set : originalSets) {
			setsWithEnvVars.add(set + envVar);
		}
		return setsWithEnvVars;
	}
}
