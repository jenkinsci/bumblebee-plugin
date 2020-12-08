package com.agiletestware.bumblebee.encryption;

import hudson.util.Secret;

/**
 * Interface for Encypt and decrypt password.
 *
 * @author Ayman BEN AMOR
 */
public interface CustomSecret {

	/**
	 * Gets the encrypted value.
	 *
	 * @param plainTextPassword
	 *            the plain text password
	 * @return the encrypted value
	 */
	String getEncryptedValue(String plainTextPassword);

	/**
	 * Gets the plain text.
	 *
	 * @param encryptedPassword
	 *            the encrypted password
	 * @return the plain text
	 */
	String getPlainText(String encryptedPassword);

	/**
	 * Get plain text password from secret.
	 *
	 * @param secret
	 *            secret, might be <code>null</code>.
	 * @return plain text for the given secret, or <code>null</code> if secret
	 *         is null.
	 */
	String getPlainText(Secret secret);

	/**
	 * Get Secret from plain text password.
	 *
	 * @param plainTextPassword
	 *            plain text password, might be <code>null</code>.
	 * @return secret or <code>null</code> if plainTextPassword is null.
	 */
	Secret getSecret(String plainTextPassword);

}
