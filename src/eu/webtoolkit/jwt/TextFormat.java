/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

/**
 * Enumeration that indicates the text format.
 * <p>
 * 
 * @see WText#setTextFormat(TextFormat textFormat)
 */
public enum TextFormat {
	/**
	 * Format text as XSS-safe XHTML markup&apos;ed text.
	 */
	XHTMLText,
	/**
	 * Format text as XHTML markup&apos;ed text.
	 */
	XHTMLUnsafeText,
	/**
	 * Format text as plain text.
	 */
	PlainText;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
