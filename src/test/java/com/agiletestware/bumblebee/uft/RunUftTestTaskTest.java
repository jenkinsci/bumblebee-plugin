package com.agiletestware.bumblebee.uft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.agiletestware.bumblebee.client.runner.ExecutionEnvironment;
import com.agiletestware.bumblebee.client.runner.ExecutionEnvironmentProvider;
import com.agiletestware.bumblebee.client.runner.Runner;
import com.agiletestware.bumblebee.client.runner.RunnerContext;
import com.agiletestware.bumblebee.client.uftrunner.UftRunnerParameters;
import com.agiletestware.bumblebee.client.uftrunner.UftRunnerParametersImpl;
import com.agiletestware.bumblebee.client.utils.BuildLogger;
import com.agiletestware.bumblebee.client.utils.Mutable;

import hudson.FilePath;
import hudson.model.StreamBuildListener;

/**
 * Tests for {@link RunUftTestTask}.
 *
 * @author Sergey Oplavin
 *
 */
public class RunUftTestTaskTest {

	private static File tempFolder;
	private static File jenkinsDir;
	private static File workspace;

	@BeforeClass
	public static void beforeClass() throws IOException {
		tempFolder = Files.createTempDirectory(null).toFile();
		jenkinsDir = new File(tempFolder, "jenkins");
		workspace = new File(tempFolder, "workspace");
	}

	@AfterClass
	public static void afterClass() throws IOException {
		FileUtils.deleteDirectory(tempFolder);
	}

	@Before
	public void beforeTest() throws IOException {
		FileUtils.cleanDirectory(tempFolder);
		jenkinsDir.mkdir();
		workspace.mkdir();
	}

	@Test
	public void testWithCorrectParams() throws Exception {
		final int expectedCode = 42;
		final UftRunnerParameters uftParams = createValidParams();
		final RunUftTestTask task = createUftRunner(uftParams);
		final Mutable<Boolean> invoked = new Mutable<Boolean>(false);
		task.setUftRunner(new Runner<UftRunnerParameters, Integer>() {

			@Override
			public Integer run(final UftRunnerParameters parameters, final RunnerContext context, final BuildLogger buildLogger) throws Exception {
				invoked.set(true);
				assertEquals(uftParams, parameters);
				final File bumblebeeDir = new File(jenkinsDir, "bumblebee");
				assertEquals(bumblebeeDir, context.getBumblebeeDir());
				assertEquals(new File(bumblebeeDir, "work"), context.getWorkingDir());
				assertEquals(new File(bumblebeeDir, "something"), context.getRunnerExecutable());
				return expectedCode;
			}
		});
		final int actual = task.call();
		assertTrue("Uft runner should have been invoked, but it was not", invoked.get());
		assertEquals(expectedCode, actual);
	}

	@Test
	public void testWithMissingBumblebeeUrl() throws Exception {
		final UftRunnerParameters uftParams = createValidParams();
		uftParams.setBumbleBeeUrl(null);
		assertException(uftParams, "Bumblebee URL is not defined. Please check your configuration");
		uftParams.setBumbleBeeUrl("");
		assertException(uftParams, "Bumblebee URL is not defined. Please check your configuration");
	}

	@Test
	public void testWithMissingTestPath() throws Exception {
		final UftRunnerParameters uftParams = createValidParams();
		uftParams.setTestPath(null);
		assertException(uftParams, "Test Path is not defined. Please check your configuration");
		uftParams.setTestPath("");
		assertException(uftParams, "Test Path is not defined. Please check your configuration");
	}

	@Test
	public void testWithMissingOutputDir() throws Exception {
		final UftRunnerParameters uftParams = createValidParams();
		uftParams.setOutputDirName(null);
		assertException(uftParams, "JUnit Results Directory is not defined. Please check your configuration");
		uftParams.setOutputDirName("");
		assertException(uftParams, "JUnit Results Directory is not defined. Please check your configuration");
	}

	@Test
	public void testWithMissingUftRunner() throws Exception {
		final UftRunnerParameters uftParams = createValidParams();
		uftParams.setUftBatchRunnerExePath(null);
		assertException(uftParams, "UFT Batch Runner is not defined. Please check your configuration");
		uftParams.setUftBatchRunnerExePath("");
		assertException(uftParams, "UFT Batch Runner is not defined. Please check your configuration");
	}

	@Test
	public void testWithInvalidUftRunner() throws Exception {
		final UftRunnerParameters uftParams = createValidParams();
		final File nonExisting = new File("aaa");
		uftParams.setUftBatchRunnerExePath(nonExisting.getAbsolutePath());
		assertException(uftParams, "UFT Batch Runner: " + nonExisting.getAbsolutePath() + " does not exist or not a file.");
		uftParams.setUftBatchRunnerExePath(jenkinsDir.getAbsolutePath());
		assertException(uftParams, "UFT Batch Runner: " + jenkinsDir.getAbsolutePath() + " does not exist or not a file.");
	}

	private void assertException(final UftRunnerParameters params, final String errorMessage) {
		try {
			final RunUftTestTask task = createUftRunner(params);
			task.call();
			fail("Should have failed with error: " + errorMessage);
		} catch (final Exception ex) {
			assertEquals(errorMessage, ex.getMessage());
		}
	}

	private RunUftTestTask createUftRunner(final UftRunnerParameters uftParams) {
		final RunUftTestTask task = new RunUftTestTask(uftParams, new StreamBuildListener(System.out, StandardCharsets.UTF_8), new FilePath(jenkinsDir),
				new FilePath(workspace));
		task.setUftRunner(new Runner<UftRunnerParameters, Integer>() {

			@Override
			public Integer run(final UftRunnerParameters parameters, final RunnerContext context, final BuildLogger buildLogger) throws Exception {
				return 0;
			}
		});
		task.setExecutionEnvironmentProvider(new ExecutionEnvironmentProviderImpl());
		return task;
	}

	private UftRunnerParameters createValidParams() throws IOException {
		final UftRunnerParameters uftParams = new UftRunnerParametersImpl();
		uftParams.setBumbleBeeUrl("bbeUrl");
		uftParams.setOutputDirName("out");
		uftParams.setTestPath("testPath");
		final File batchFile = new File(jenkinsDir, "uft.exe");
		if (!batchFile.exists()) {
			batchFile.createNewFile();
		}
		uftParams.setUftBatchRunnerExePath(batchFile.getAbsolutePath());
		return uftParams;
	}

	private static class ExecutionEnvironmentProviderImpl implements ExecutionEnvironmentProvider {

		@Override
		public ExecutionEnvironment getOrCreateEnvironment(final String bumblebeeUrl, final File tempDir, final BuildLogger buildLogger)
				throws IOException {
			final File bumblebeeDir = createDir(tempDir, "bumblebee");
			final File dummyFile = new File(bumblebeeDir, "something");
			if (!dummyFile.exists()) {
				dummyFile.createNewFile();
			}
			return new ExecutionEnvironment(bumblebeeDir, createDir(bumblebeeDir, "work"), dummyFile, dummyFile, dummyFile);
		}

		private File createDir(final File parent, final String name) {
			final File dir = new File(parent, name);
			if (!dir.exists()) {
				dir.mkdir();
			}
			return dir;
		}

	}
}
