package com.agiletestware.bumblebee.encryption;

import hudson.util.Secret;

/**
 * Default implementation of {@link CustomSecret}.
 *
 * @author Ayman BEN AMOR
 *
 */
public enum DefaultCustomSecret implements CustomSecret {

	THE_INSTANCE;

	@Override
	public String getEncryptedValue(final String plainTextPassword) {
		return Secret.fromString(plainTextPassword).getEncryptedValue();
	}

	@Override
	public String getPlainText(final String encryptedPassword) {
		if (encryptedPassword == null) {
			return null;
		}
		final Secret secret = Secret.decrypt(encryptedPassword);
		if (secret == null) {
			throw new IllegalStateException("Error in decrypt password");
		}
		return secret.getPlainText();
	}

	@Override
	public String getPlainText(final Secret encryptedPassword) {
		return encryptedPassword != null ? encryptedPassword.getPlainText() : null;
	}

	@Override
	public Secret getSecret(final String plainTextPassword) {
		return plainTextPassword != null ? Secret.fromString(plainTextPassword) : null;
	}

}
