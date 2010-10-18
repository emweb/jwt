/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * Enumeration that indicates a meta header type.
 * <p>
 * 
 * @see WApplication#addMetaHeader(String name, CharSequence content, String
 *      lang)
 */
public enum MetaHeaderType {
	/**
	 * A normal meta header defining a document property.
	 */
	MetaName,
	/**
	 * A http-equiv meta header defining a HTTP header.
	 */
	MetaHttpHeader;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
