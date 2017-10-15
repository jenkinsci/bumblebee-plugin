package com.agiletestware.bumblebee.testset;

import org.jenkinsci.remoting.RoleChecker;

import com.agiletestware.bumblebee.client.api.AddTestToSetParameters;
import com.agiletestware.bumblebee.client.api.BumblebeeApi;
import com.agiletestware.bumblebee.client.api.BumblebeeApiImpl;

import hudson.remoting.Callable;

/**
 * Callable for {@link AddTestToSetStep} step.
 *
 * @author Sergey Oplavin
 *
 */
public class AddTestToSetCallable implements Callable<Void, Exception> {

	/** . */
	private static final long serialVersionUID = 7530514014384384415L;

	private final AddTestToSetParameters parameters;
	private final int timeout;

	/**
	 * Constructor.
	 *
	 * @param parameters
	 *            parameters.
	 * @param timeout
	 *            timeout.
	 */
	public AddTestToSetCallable(final AddTestToSetParameters parameters, final int timeout) {
		this.parameters = parameters;
		this.timeout = timeout;
	}

	@Override
	public Void call() throws Exception {
		try (final BumblebeeApi bumblebeeApi = new BumblebeeApiImpl(parameters.getBumbleBeeUrl(), timeout)) {
			bumblebeeApi.addTestToSet(parameters);
			return null;
		}
	}

	@Override
	public void checkRoles(final RoleChecker checker) throws SecurityException {

	}

}
