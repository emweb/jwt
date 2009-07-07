/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.composer;

public class Attachment
{
	/**
	 * The file name.
	 */
	public String fileName;

	/**
	 * The content description.
	 */
	public String contentDescription;

	/**
	 * the spooled file name.
	 */
	public String spoolFileName;

	/**
	 * Create an attachment.
	 */
	public Attachment(final String aFileName, final String aContentDescription, final String aSpoolFileName)
	{
		fileName = aFileName;
		contentDescription = aContentDescription;
		spoolFileName = aSpoolFileName;
	}
}
