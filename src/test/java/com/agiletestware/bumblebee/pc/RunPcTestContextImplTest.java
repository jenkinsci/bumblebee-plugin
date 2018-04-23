package com.agiletestware.bumblebee.pc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import com.agiletestware.bumblebee.BumblebeeGlobalConfig;
import com.agiletestware.bumblebee.client.api.BumblebeeApi;
import com.agiletestware.bumblebee.client.api.BumblebeeApiProvider;
import com.agiletestware.bumblebee.client.utils.action.RetrySettings;
import com.agiletestware.bumblebee.encryption.CustomSecret;

import hudson.FilePath;

/**
 * Tests for {@link RunPcTestContextImpl}.
 *
 * @author Sergey Oplavin
 *
 */
public class RunPcTestContextImplTest {

	private static final String BUMBLEBEE_URL = "bumblebeeUrl";
	private static final String ALM_URL = "alm";
	private static final String PC_URL = "pcUrl";
	private static final String ALM_USER = "user";
	private static final String ALM_PASSWORD = "pwd";
	private static final String DOMAIN = "domain";
	private static final String PROJECT = "project";
	private static final String OVERRIDNG_ALM_USER = "overridingUser";
	private static final String OVERRIDNG_ALM_PASSWORD = "overridingPwd";
	private static final String ENCRYPTED_OVERRIDNG_ALM_PASSWORD = "encryptedOverridingPwd";
	private static final double DEFAULT_RETRY_INTERVAL_MULTIPLY_FACTORY = 1.0;
	private final CustomSecret customSecret = mock(CustomSecret.class);
	private final BumblebeeApiProvider bumblebeeApiProvider = mock(BumblebeeApiProvider.class);

	@Before
	public void setUp() throws Exception {
		when(customSecret.getEncryptedValue(OVERRIDNG_ALM_PASSWORD)).thenReturn("secretPwd");
		when(customSecret.getPlainText("secretPwd")).thenReturn(OVERRIDNG_ALM_PASSWORD);
		final BumblebeeApi bumblebeeApi = mock(BumblebeeApi.class);
		when(bumblebeeApi.getEncryptedPassword(OVERRIDNG_ALM_PASSWORD)).thenReturn(ENCRYPTED_OVERRIDNG_ALM_PASSWORD);
		when(bumblebeeApiProvider.provide(anyString(), anyInt())).thenReturn(bumblebeeApi);
	}

	@Test
	public void testGetBumblebeeUrl() throws Exception {
		final RunPcTestBuildStep step = new RunPcTestBuildStep();
		step.setOutputDir("output");
		final RunPcTestContextImpl context = new RunPcTestContextImpl(step, createGlobalConfigMock(BUMBLEBEE_URL, null, null, null, null, 0),
				new FilePath(new File("")), customSecret, bumblebeeApiProvider);
		assertEquals(BUMBLEBEE_URL, context.getBumblebeeUrl());
	}

	@Test
	public void testGetPcConnectionParametersWithAlmUserAndAlmPasswordFromGlobalConfig() throws Exception {
		final RunPcTestBuildStep step = new RunPcTestBuildStep();
		step.setOutputDir("output");
		step.setDomain(DOMAIN);
		step.setProject(PROJECT);
		final RunPcTestContextImpl context = new RunPcTestContextImpl(step, createGlobalConfigMock(null, ALM_URL, PC_URL, ALM_USER, ALM_PASSWORD, 0),
				new FilePath(new File("")), customSecret, bumblebeeApiProvider);
		final PcConnectionParameters params = context.getConnectionParameters();
		assertPcConnectionParameters(params, ALM_USER, ALM_PASSWORD);
	}

	@Test
	public void testGetPcConnectionParametersWithAlmUserFromStep() throws Exception {
		final RunPcTestBuildStep step = new RunPcTestBuildStep();
		step.setCustomSecret(customSecret);
		step.setOutputDir("output");
		step.setDomain(DOMAIN);
		step.setProject(PROJECT);
		step.setAlmUser(OVERRIDNG_ALM_USER);
		final RunPcTestContextImpl context = new RunPcTestContextImpl(step, createGlobalConfigMock(null, ALM_URL, PC_URL, ALM_USER, ALM_PASSWORD, 0),
				new FilePath(new File("")), customSecret, bumblebeeApiProvider);
		final PcConnectionParameters params = context.getConnectionParameters();
		assertPcConnectionParameters(params, OVERRIDNG_ALM_USER, ALM_PASSWORD);
	}

	@Test
	public void testGetPcConnectionParametersWithAlmPasswordFromStep() throws Exception {
		final RunPcTestBuildStep step = new RunPcTestBuildStep();
		step.setOutputDir("output");
		step.setDomain(DOMAIN);
		step.setProject(PROJECT);
		step.setCustomSecret(customSecret);
		step.setAlmPassword(OVERRIDNG_ALM_PASSWORD);
		final RunPcTestContextImpl context = new RunPcTestContextImpl(step, createGlobalConfigMock(BUMBLEBEE_URL, ALM_URL, PC_URL, ALM_USER, ALM_PASSWORD, 0),
				new FilePath(new File("")), customSecret, bumblebeeApiProvider);
		final PcConnectionParameters params = context.getConnectionParameters();
		assertPcConnectionParameters(params, ALM_USER, ENCRYPTED_OVERRIDNG_ALM_PASSWORD);
	}

	@Test
	public void testGetOutputDir() throws Exception {
		final RunPcTestBuildStep step = new RunPcTestBuildStep();
		final File file = new File("temp");
		step.setOutputDir("output");

		final RunPcTestContextImpl context = new RunPcTestContextImpl(step, mock(BumblebeeGlobalConfig.class), new FilePath(file), customSecret,
				bumblebeeApiProvider);
		assertEquals(new File(file, "output"), context.getOutputDir());
	}

	@Test
	public void testGetStartRunParameters() throws Exception {
		final PostRunAction action = PostRunAction.COLLATE_AND_ANALYZE;
		final String testPath = "Subject\\aaa";
		final String testSetPath = "Root\\aaa";
		final int timeSlotDuration = 42;
		final RunPcTestBuildStep step = new RunPcTestBuildStep();
		step.setOutputDir("output");
		step.setPostRunActionString(action.getLabel());
		step.setTestPlanPath(testPath);
		step.setTestLabPath(testSetPath);
		step.setTimeslotDuration(timeSlotDuration);
		final RunPcTestContextImpl context = new RunPcTestContextImpl(step, mock(BumblebeeGlobalConfig.class), new FilePath(new File("")), customSecret,
				bumblebeeApiProvider);
		final StartRunParameters params = context.getStartRunParameters();
		assertEquals(action, params.getPostRunAction());
		assertEquals(testPath, params.getTestPath());
		assertEquals(testSetPath, params.getTestSetPath());
		assertEquals(timeSlotDuration, params.getTimeslotDuration());
	}

	@Test
	public void testGetGenericRetrySettings() throws Exception {
		final int retryCount = 1;
		final int retryInterval = 2;
		final double increaseFactor = 2.0;
		final RunPcTestBuildStep step = new RunPcTestBuildStep();
		step.setOutputDir("output");
		step.setRetryCount(retryCount);
		step.setRetryInterval(retryInterval);
		step.setRetryIntervalMultiplier(increaseFactor);
		final RunPcTestContextImpl context = new RunPcTestContextImpl(step, mock(BumblebeeGlobalConfig.class), new FilePath(new File("")), customSecret,
				bumblebeeApiProvider);
		final RetrySettings settings = context.getGenericRetrySettings();
		assertEquals(retryCount, settings.getRetryAttempts());
		assertEquals(retryInterval, settings.getRetryIntervalSeconds());
		assertEquals(increaseFactor, settings.getRetryIntervalMultiplyFactor(), 0);
	}

	@Test
	public void testGetCollateAnalyzeRetrySettingsWithEnableRetryEqualTrue() throws Exception {
		final boolean retryCollateAndAnalysisFlag = true;
		final int retryCollateAndAnalysisAttempts = 2;
		final int retryCollateAndAnalysisInterval = 12;
		final RunPcTestBuildStep step = new RunPcTestBuildStep();
		step.setOutputDir("output");
		step.setRetryCollateAndAnalysisFlag(retryCollateAndAnalysisFlag);
		step.setRetryCollateAndAnalysisAttempts(retryCollateAndAnalysisAttempts);
		step.setRetryCollateAndAnalysisInterval(retryCollateAndAnalysisInterval);
		final RunPcTestContextImpl context = new RunPcTestContextImpl(step, mock(BumblebeeGlobalConfig.class), new FilePath(new File("")), customSecret,
				bumblebeeApiProvider);
		final RetrySettings settings = context.getCollateAnalyzeRetrySettings();
		assertEquals(retryCollateAndAnalysisAttempts, settings.getRetryAttempts());
		assertEquals(retryCollateAndAnalysisInterval, settings.getRetryIntervalSeconds());
		assertEquals(0, DEFAULT_RETRY_INTERVAL_MULTIPLY_FACTORY, settings.getRetryIntervalMultiplyFactor());
	}

	@Test
	public void testGetCollateAnalyzeRetrySettingsWithEnableRetryEqualFalse() throws Exception {
		final boolean retryCollateAndAnalysisFlag = false;
		final int retryCollateAndAnalysisAttempts = 0;
		final int retryCollateAndAnalysisInterval = 0;
		final RunPcTestBuildStep step = new RunPcTestBuildStep();
		step.setOutputDir("output");
		step.setRetryCollateAndAnalysisFlag(retryCollateAndAnalysisFlag);
		step.setRetryCollateAndAnalysisAttempts(retryCollateAndAnalysisAttempts);
		step.setRetryCollateAndAnalysisInterval(retryCollateAndAnalysisInterval);
		final RunPcTestContextImpl context = new RunPcTestContextImpl(step, mock(BumblebeeGlobalConfig.class), new FilePath(new File("")), customSecret,
				bumblebeeApiProvider);
		final RetrySettings settings = context.getCollateAnalyzeRetrySettings();
		assertEquals(retryCollateAndAnalysisAttempts, settings.getRetryAttempts());
		assertEquals(retryCollateAndAnalysisInterval, settings.getRetryIntervalSeconds());
		assertEquals(0, DEFAULT_RETRY_INTERVAL_MULTIPLY_FACTORY, settings.getRetryIntervalMultiplyFactor());
	}

	@Test
	public void testGetPollingInterval() throws Exception {
		final int pollingInterval = 42;
		final RunPcTestBuildStep step = new RunPcTestBuildStep();
		step.setOutputDir("output");
		step.setPollingInterval(pollingInterval);
		final RunPcTestContextImpl context = new RunPcTestContextImpl(step, mock(BumblebeeGlobalConfig.class), new FilePath(new File("")), customSecret,
				bumblebeeApiProvider);
		assertEquals(pollingInterval, context.getPollingInterval());
	}

	@Test
	public void testGetTimeOutFromTask() throws Exception {
		final int timeout = 42;
		final RunPcTestBuildStep step = new RunPcTestBuildStep();
		step.setOutputDir("output");
		step.setTimeout(timeout);
		final BumblebeeGlobalConfig global = mock(BumblebeeGlobalConfig.class);
		when(global.getPcTimeOut()).thenReturn(24);
		final RunPcTestContextImpl context = new RunPcTestContextImpl(step, global, new FilePath(new File("")), customSecret, bumblebeeApiProvider);
		assertEquals(timeout * 60, context.getTimeout());
	}

	@Test
	public void testGetTimeOutFromGlobal() throws Exception {
		final int timeout = 42;
		final RunPcTestBuildStep step = new RunPcTestBuildStep();
		step.setOutputDir("output");
		final BumblebeeGlobalConfig global = mock(BumblebeeGlobalConfig.class);
		when(global.getPcTimeOut()).thenReturn(timeout);
		final RunPcTestContextImpl context = new RunPcTestContextImpl(step, global, new FilePath(new File("")), customSecret, bumblebeeApiProvider);
		assertEquals(timeout * 60, context.getTimeout());
	}

	private void assertPcConnectionParameters(final PcConnectionParameters params, final String expectedAlmUser, final String expectedAlmPassword) {
		assertEquals(ALM_URL, params.getAlmUrl());
		assertEquals(PC_URL, params.getPcUrl());
		assertEquals(expectedAlmUser, params.getUser());
		assertEquals(expectedAlmPassword, params.getPassword());
		assertEquals(DOMAIN, params.getDomain());
		assertEquals(PROJECT, params.getProject());
	}

	private BumblebeeGlobalConfig createGlobalConfigMock(final String bumblbeeUrl, final String almUrl, final String pcUrl, final String userName,
			final String pwd, final int pcTimeout) {
		final BumblebeeGlobalConfig conf = mock(BumblebeeGlobalConfig.class);
		when(conf.getBumblebeeUrl()).thenReturn(bumblbeeUrl);
		when(conf.getQcUrl()).thenReturn(almUrl);
		when(conf.getPcUrl()).thenReturn(pcUrl);
		when(conf.getQcUserName()).thenReturn(userName);
		when(conf.getPassword()).thenReturn(pwd);
		when(conf.getPcTimeOut()).thenReturn(pcTimeout);
		return conf;
	}
}
