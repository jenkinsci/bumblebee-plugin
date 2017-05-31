package com.agiletestware.bumblebee.validator;

/**
 * Helper class to pass HP ALM and PC URLs as a parameter to validator.
 *
 * @author Sergey Oplavin
 *
 */
public class HpUrls {
	private final String almUrl;
	private final String pcUrl;

	public HpUrls(final String almUrl, final String pcUrl) {
		super();
		this.almUrl = almUrl;
		this.pcUrl = pcUrl;
	}

	public String getAlmUrl() {
		return almUrl;
	}

	public String getPcUrl() {
		return pcUrl;
	}
}