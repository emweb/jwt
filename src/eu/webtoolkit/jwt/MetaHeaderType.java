/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enumeration that indicates a meta header type.
 * <p>
 * 
 * @see WApplication#addMetaHeader(String name, CharSequence content, String
 *      lang)
 */
public enum MetaHeaderType {
	/**
	 * Of the form &lt;meta name=... content=... &gt;.
	 */
	MetaName,
	/**
	 * Of the form &lt;meta property=... content=... &gt;.
	 */
	MetaProperty,
	/**
	 * Of the form &lt;meta http-equiv=... content=... &gt;.
	 */
	MetaHttpHeader;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
