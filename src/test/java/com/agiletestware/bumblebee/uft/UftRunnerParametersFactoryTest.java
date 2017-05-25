package com.agiletestware.bumblebee.uft;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.agiletestware.bumblebee.BumblebeeGlobalConfig;
import com.agiletestware.bumblebee.client.uftrunner.UftRunnerParameters;

import hudson.EnvVars;

/**
 * Tests for {@link UftRunnerParametersFactory}.
 *
 * @author Sergey Oplavin
 *
 */
public class UftRunnerParametersFactoryTest {

	@Test
	public void testValueFromGlobal() {
		final String testPath = "testPath";
		final String outputDirPath = "outputDir";
		final String bumblebeeUrl = "bbeUrl";
		final String uftRunner = "uftRunner";
		final int timeout = 42;
		final BumblebeeGlobalConfig conf = mock(BumblebeeGlobalConfig.class);
		when(conf.getBumblebeeUrl()).thenReturn(bumblebeeUrl);
		when(conf.getUftRunnerPath()).thenReturn(uftRunner);
		when(conf.getTimeOut()).thenReturn(timeout);

		final UftRunnerParameters params = UftRunnerParametersFactory.THE_INSTANCE.create(new RunUftTestBuildStep(testPath, outputDirPath), conf,
				new EnvVars());

		assertEquals(testPath, params.getTestPath());
		assertEquals(outputDirPath, params.getOutputDirName());
		assertEquals(bumblebeeUrl, params.getBumbleBeeUrl());
		assertEquals(uftRunner, params.getUftBatchRunnerExePath());
		assertEquals(conf.getTimeOut(), params.getTimeOut());
	}

	@Test
	public void testGetUftPathFromEnvVars() {
		final String overriddenValue = "uftRunner on agent";
		final BumblebeeGlobalConfig conf = mock(BumblebeeGlobalConfig.class);
		when(conf.getUftRunnerPath()).thenReturn("uftRunner");
		final EnvVars envVars = new EnvVars();
		envVars.put("UFT_RUNNER", overriddenValue);
		final UftRunnerParameters params = UftRunnerParametersFactory.THE_INSTANCE.create(new RunUftTestBuildStep("", ""), conf,
				envVars);
		assertEquals(overriddenValue, params.getUftBatchRunnerExePath());
	}
}
