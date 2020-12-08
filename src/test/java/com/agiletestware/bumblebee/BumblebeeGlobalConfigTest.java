package com.agiletestware.bumblebee;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.acegisecurity.Authentication;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.agiletestware.bumblebee.client.api.BumblebeeApi;
import com.agiletestware.bumblebee.client.api.BumblebeeApiProvider;
import com.agiletestware.bumblebee.encryption.CustomSecret;
import com.agiletestware.bumblebee.validator.Validator;

import hudson.security.ACL;
import hudson.security.Permission;
import hudson.util.FormValidation;
import hudson.util.FormValidation.Kind;
import hudson.util.Secret;
import jenkins.model.Jenkins;

/**
 * Tests for {@link BumblebeeGlobalConfig}.
 *
 * @author Sergey Oplavin
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Jenkins.class, Secret.class })
public class BumblebeeGlobalConfigTest {

	private static final int PC_TIMEOUT = 42;
	private static final String PC_URL = "pcUrl";
	private static final int TIMEOUT = 1;
	private static final String UFT_BATCH_RUNNER_CMD_EXE = "UFTBatchRunnerCMD.exe";
	private static final String QC_USER_NAME = "qcUser";
	private static final String BUMBLEBEE_URL = "bumblebee url";
	private static final String QC_URL = "http://aaa/qcbin";
	private static final boolean TRUST_CERTS = false;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	@Rule
	public ExpectedException expeted = ExpectedException.none();

	private final CustomSecret secret = mock(CustomSecret.class);
	private final BumblebeeApi api = mock(BumblebeeApi.class);
	private final BumblebeeApiProvider provider = (url, timeout, trust) -> api;
	private BumblebeeGlobalConfig config;
	private final Jenkins jenkins = mock(Jenkins.class);
	@SuppressWarnings("unchecked")
	private final Validator<String, Integer> bumblebeeUrlValidator = mock(Validator.class);

	@Before
	public void setUp() throws Exception {
		PowerMockito.mockStatic(Jenkins.class);
		PowerMockito.mockStatic(Secret.class);
		PowerMockito.when(Jenkins.getInstance()).thenReturn(jenkins);
		when(jenkins.getRootDir()).thenReturn(tempFolder.getRoot());
		config = new BumblebeeGlobalConfig(provider, secret, bumblebeeUrlValidator);
		when(jenkins.getACL()).thenReturn(new ACL() {
			@Override
			public boolean hasPermission(final Authentication a, final Permission permission) {
				return true;
			}
		});

	}

	@Test
	public void doSaveConnection_noExistingPassword() throws Exception {
		final String password = "qcPassword";
		final Secret secretFromForm = createSecretMock(password, "something");

		doSaveConnection(secretFromForm, false);
		verify(api).getEncryptedPassword(password);
	}

	@Test
	public void doSaveConnection_differentPasswordSet() throws Exception {
		final String password1 = "qcPassword";
		final String password2 = "other password";
		final Secret secret1 = createSecretMock(password1, "something");
		final Secret secret2 = createSecretMock(password2, "something different");

		doSaveConnection(secret1, false);
		doSaveConnection(secret2, false);
		assertConfig(BUMBLEBEE_URL, QC_URL, QC_USER_NAME, secret2, UFT_BATCH_RUNNER_CMD_EXE, PC_URL, TIMEOUT, PC_TIMEOUT);
		verify(api).getEncryptedPassword(password1);
		verify(api).getEncryptedPassword(password2);
	}

	@Test
	public void doSaveConnection_passwordsMatch() throws Exception {
		final String password1 = "qcPassword";
		final Secret secret1 = createSecretMock(password1, "something");

		doSaveConnection(secret1, false);
		doSaveConnection(secret1, false);
		assertConfig(BUMBLEBEE_URL, QC_URL, QC_USER_NAME, secret1, UFT_BATCH_RUNNER_CMD_EXE, PC_URL, TIMEOUT, PC_TIMEOUT);
		verify(api, times(1)).getEncryptedPassword(password1);
	}

	@Test
	public void doSaveConnection_skipConnectivityDiagnostic() throws Exception {
		final String password1 = "qcPassword";
		final Secret secret1 = createSecretMock(password1, "something");
		doSaveConnection(secret1, true);
		assertConfig(BUMBLEBEE_URL, QC_URL, QC_USER_NAME, secret1, UFT_BATCH_RUNNER_CMD_EXE, PC_URL, TIMEOUT, PC_TIMEOUT);
		verifyNoMoreInteractions(api);
	}

	@Test
	public void doSaveConnection_apiException() throws Exception {
		final Secret secret = createSecretMock("someting", "something");
		final RuntimeException exception = new RuntimeException("oops");
		when(api.getEncryptedPassword(any())).thenThrow(exception);
		final FormValidation validation = config.doSaveConnection(BUMBLEBEE_URL, QC_URL, QC_USER_NAME, secret, TIMEOUT, UFT_BATCH_RUNNER_CMD_EXE, PC_URL,
				PC_TIMEOUT,
				false,
				TRUST_CERTS);
		assertEquals(Kind.ERROR, validation.kind);
		assertEquals("FAILED: oops", validation.getMessage());
		assertConfig(null, null, null, null, null, null, 0, 0);
	}

	@Test
	public void doSaveConnection_nullValues() throws Exception {
		final Secret secret = createSecretMock("someting", "something");
		final FormValidation validation = new BumblebeeGlobalConfig().doSaveConnection(null, null, null, secret, 0, null, null, 0, false, false);
		assertEquals(Kind.ERROR, validation.kind);
		assertEquals(
				"ERROR: <ul style='list-style-type: none; padding-left: 0; margin: 0'><li>Bumblebee URL is required</li><li><div/></li><li><div/></li></ul>",
				validation.toString());
		assertConfig(null, null, null, null, null, null, 0, 0);
	}

	@Test
	public void doSaveConnection_almUrlSet_almUserMissing() throws Exception {
		final FormValidation validation = new BumblebeeGlobalConfig().doSaveConnection(BUMBLEBEE_URL, QC_URL, null, createSecretMock("aaa", "something"),
				0, null, null, 0, true, false);
		assertEquals(Kind.ERROR, validation.kind);
		assertEquals(
				"ERROR: <ul style='list-style-type: none; padding-left: 0; margin: 0'><li>Login is required</li><li><div/></li></ul>",
				validation.toString());
		assertConfig(null, null, null, null, null, null, 0, 0);
	}

	@Test
	public void doSaveConnection_incorrectUftPath() throws Exception {
		final FormValidation validation = new BumblebeeGlobalConfig().doSaveConnection(BUMBLEBEE_URL, null, null, createSecretMock("aaa", "something"), 0,
				"uft", null, 0, true, false);
		assertEquals(Kind.ERROR, validation.kind);
		assertEquals(
				"ERROR: <ul style='list-style-type: none; padding-left: 0; margin: 0'><li><div/></li><li>Must end with UFTBatchRunnerCMD.exe</li></ul>",
				validation.toString());
		assertConfig(null, null, null, null, null, null, 0, 0);
	}

	@Test
	public void doSaveConnection_nullBumblebeeUrl_skipDiagnosticCheck() throws Exception {
		final Secret secret = createSecretMock("someting", "something");
		final FormValidation validation = new BumblebeeGlobalConfig().doSaveConnection(null, null, null, secret, 0, null, null, 0, true, false);
		assertEquals(Kind.OK, validation.kind);
		assertEquals("Configuration Saved", validation.getMessage());
		assertConfig(null, null, null, null, null, null, 0, 0);
	}

	private void doSaveConnection(final Secret newPassword, final boolean skipConnectivityCheck) {
		final FormValidation validation = config.doSaveConnection(BUMBLEBEE_URL, QC_URL, QC_USER_NAME, newPassword, TIMEOUT, UFT_BATCH_RUNNER_CMD_EXE, PC_URL,
				PC_TIMEOUT,
				skipConnectivityCheck,
				TRUST_CERTS);
		assertEquals(Kind.OK, validation.kind);
		assertEquals("Configuration Saved", validation.getMessage());
	}

	private void assertConfig(final String bumblebeeUrl, final String qcUrl, final String user, final Secret password, final String uftRunnerPath,
			final String pcUrl, final int timeOut, final int pcTimeOut) {
		assertEquals(bumblebeeUrl, config.getBumblebeeUrl());
		assertEquals(qcUrl, config.getQcUrl());
		assertEquals(user, config.getQcUserName());
		assertEquals(password, config.getPassword());
		assertEquals(uftRunnerPath, config.getUftRunnerPath());
		assertEquals(pcUrl, config.getPcUrl());
		assertEquals(timeOut, config.getTimeOut());
		assertEquals(pcTimeOut, config.getPcTimeOut());
	}

	private Secret createSecretMock(final String plainPassword, final String bbeEncryptedPassword) throws Exception {
		final Secret s = mock(Secret.class);
		when(secret.getSecret(bbeEncryptedPassword)).thenReturn(s);
		when(secret.getPlainText(s)).thenReturn(plainPassword);
		when(bumblebeeUrlValidator.validate(BUMBLEBEE_URL, TIMEOUT * 60)).thenReturn(FormValidation.ok());
		when(api.getEncryptedPassword(plainPassword)).thenReturn(bbeEncryptedPassword);
		return s;
	}

}
