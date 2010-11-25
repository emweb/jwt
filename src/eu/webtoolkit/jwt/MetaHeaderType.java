/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

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
