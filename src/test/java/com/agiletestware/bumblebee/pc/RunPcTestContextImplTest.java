package com.agiletestware.bumblebee.pc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Test;

import com.agiletestware.bumblebee.BumblebeeGlobalConfig;
import com.agiletestware.bumblebee.client.utils.action.RetrySettings;

import hudson.FilePath;

/**
 * Tests for {@link RunPcTestContextImpl}.
 *
 * @author Sergey Oplavin
 *
 */
public class RunPcTestContextImplTest {

	@Test
	public void testGetBumblebeeUrl() {
		final String expBumblebeeUrl = "exp bbe url";
		final RunPcTestBuildStep step = new RunPcTestBuildStep();
		step.setOutputDir("output");
		final RunPcTestContextImpl context = new RunPcTestContextImpl(step,
				createGlobalConfigMock(expBumblebeeUrl, null, null, null, null, 0),
				new FilePath(new File("")));
		assertEquals(expBumblebeeUrl, context.getBumblebeeUrl());
	}

	@Test
	public void testGetPcConnectionParameters() {
		final String almUrl = "alm";
		final String pcUrl = "pcUrl";
		final String user = "user";
		final String pwd = "pwd";
		final String domain = "domain";
		final String project = "project";
		final RunPcTestBuildStep step = new RunPcTestBuildStep();
		step.setOutputDir("output");
		step.setDomain(domain);
		step.setProject(project);
		final RunPcTestContextImpl context = new RunPcTestContextImpl(step,
				createGlobalConfigMock(null, almUrl, pcUrl, user, pwd, 0),
				new FilePath(new File("")));
		final PcConnectionParameters params = context.getConnectionParameters();
		assertEquals(almUrl, params.getAlmUrl());
		assertEquals(pcUrl, params.getPcUrl());
		assertEquals(user, params.getUser());
		assertEquals(pwd, params.getPassword());
		assertEquals(domain, params.getDomain());
		assertEquals(project, params.getProject());
	}

	@Test
	public void testGetOutputDir() {
		final RunPcTestBuildStep step = new RunPcTestBuildStep();
		final File file = new File("temp");
		step.setOutputDir("output");

		final RunPcTestContextImpl context = new RunPcTestContextImpl(step,
				mock(BumblebeeGlobalConfig.class),
				new FilePath(file));
		assertEquals(new File(file, "output"), context.getOutputDir());
	}

	@Test
	public void testGetStartRunParameters() {
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
		final RunPcTestContextImpl context = new RunPcTestContextImpl(step,
				mock(BumblebeeGlobalConfig.class),
				new FilePath(new File("")));
		final StartRunParameters params = context.getStartRunParameters();
		assertEquals(action, params.getPostRunAction());
		assertEquals(testPath, params.getTestPath());
		assertEquals(testSetPath, params.getTestSetPath());
		assertEquals(timeSlotDuration, params.getTimeslotDuration());
	}

	@Test
	public void testGetRetrySettings() {
		final int retryCount = 1;
		final int retryInterval = 2;
		final double increaseFactor = 2.0;
		final RunPcTestBuildStep step = new RunPcTestBuildStep();
		step.setOutputDir("output");
		step.setRetryCount(retryCount);
		step.setRetryInterval(retryInterval);
		step.setRetryIntervalMultiplier(increaseFactor);
		final RunPcTestContextImpl context = new RunPcTestContextImpl(step,
				mock(BumblebeeGlobalConfig.class),
				new FilePath(new File("")));
		final RetrySettings settings = context.getRetrySettings();
		assertEquals(retryCount, settings.getRetryAttempts());
		assertEquals(retryInterval, settings.getRetryIntervalSeconds());
		assertEquals(increaseFactor, settings.getRetryIntervalMultiplyFactor(), 0);
	}

	@Test
	public void testGetPollingInterval() {
		final int pollingInterval = 42;
		final RunPcTestBuildStep step = new RunPcTestBuildStep();
		step.setOutputDir("output");
		step.setPollingInterval(pollingInterval);
		final RunPcTestContextImpl context = new RunPcTestContextImpl(step,
				mock(BumblebeeGlobalConfig.class),
				new FilePath(new File("")));
		assertEquals(pollingInterval, context.getPollingInterval());
	}

	@Test
	public void testGetTimeOutFromTask() {
		final int timeout = 42;
		final RunPcTestBuildStep step = new RunPcTestBuildStep();
		step.setOutputDir("output");
		step.setTimeout(timeout);
		final BumblebeeGlobalConfig global = mock(BumblebeeGlobalConfig.class);
		when(global.getPcTimeOut()).thenReturn(24);
		final RunPcTestContextImpl context = new RunPcTestContextImpl(step,
				global,
				new FilePath(new File("")));
		assertEquals(timeout * 60, context.getTimeout());
	}

	@Test
	public void testGetTimeOutFromGlobal() {
		final int timeout = 42;
		final RunPcTestBuildStep step = new RunPcTestBuildStep();
		step.setOutputDir("output");
		final BumblebeeGlobalConfig global = mock(BumblebeeGlobalConfig.class);
		when(global.getPcTimeOut()).thenReturn(timeout);
		final RunPcTestContextImpl context = new RunPcTestContextImpl(step,
				global,
				new FilePath(new File("")));
		assertEquals(timeout * 60, context.getTimeout());
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
