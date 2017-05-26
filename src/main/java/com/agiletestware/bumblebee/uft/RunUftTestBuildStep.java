package com.agiletestware.bumblebee.uft;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;

import com.agiletestware.bumblebee.BumblebeeGlobalConfig;
import com.agiletestware.bumblebee.testset.RunTestSetBuildStep;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import jenkins.model.GlobalConfiguration;
import jenkins.tasks.SimpleBuildStep;

/**
 * Build step for running local UFT tests.
 *
 * @author Sergey Oplavin
 *
 */
public class RunUftTestBuildStep extends Builder implements SimpleBuildStep {
	private final String testPath;
	private final String outputDirPath;

	@DataBoundConstructor
	public RunUftTestBuildStep(final String testPath, final String outputDirPath) {
		this.testPath = testPath;
		this.outputDirPath = outputDirPath;
	}

	@Override
	public void perform(final Run<?, ?> run, final FilePath workspace, final Launcher launcher, final TaskListener listener)
			throws InterruptedException, IOException {
		listener.getLogger().println("Bumblebee: Run local UFT test");
		try {
			final RunUftTestTask task = new RunUftTestTask(UftRunnerParametersFactory.THE_INSTANCE.create(this,
					GlobalConfiguration.all().get(BumblebeeGlobalConfig.class), run.getEnvironment(listener)), listener,
					run.getExecutor().getOwner().getNode().getRootPath(), workspace);
			final int returnCode = launcher.getChannel().call(task);
			if (returnCode != 0) {
				throw new AbortException("UFT test execution failed. See logs for details.");
			}
		} catch (final IOException e) {
			throw e;
		} catch (final Exception e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	public String getTestPath() {
		return testPath;
	}

	public String getOutputDirPath() {
		return outputDirPath;
	}

	/**
	 * Descriptor for {@link RunTestSetBuildStep}.
	 *
	 * @author Sergey Oplavin
	 *
	 */
	@Extension
	public static class Descriptor extends BuildStepDescriptor<Builder> {
		public Descriptor() {
			super(RunUftTestBuildStep.class);
			load();
		}

		@SuppressWarnings("rawtypes")
		@Override
		public boolean isApplicable(final Class<? extends AbstractProject> arg0) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Bumblebee Local UFT Test Runner";
		}

	}

}
