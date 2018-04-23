package com.agiletestware.bumblebee.encryption;

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

}
