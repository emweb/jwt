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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract class that provides support for localized strings.
 * <p>
 * 
 * This abstract class provides the content to localized WStrings, by resolving
 * the key to a string using the current application locale.
 * <p>
 * 
 * @see WString#tr(String key)
 * @see WApplication#setLocalizedStrings(WLocalizedStrings translator)
 */
public abstract class WLocalizedStrings {
	private static Logger logger = LoggerFactory
			.getLogger(WLocalizedStrings.class);

	/**
	 * Rereads the message resources.
	 * <p>
	 * Purge any cached key/values, if applicable.
	 * <p>
	 * The default implementation does nothing.
	 */
	public void refresh() {
	}

	/**
	 * Purges memory resources, if possible.
	 * <p>
	 * This is called afer event handling, and is an opportunity to conserve
	 * memory inbetween events, by freeing memory used for cached key/value
	 * bindings, if applicable.
	 * <p>
	 * The default implementation does nothing.
	 */
	public void hibernate() {
	}

	/**
	 * Resolves a key in the current locale.
	 * <p>
	 * This method is used by {@link WString} to obtain the UTF8 value
	 * corresponding to a key in the current locale.
	 * <p>
	 * Returns the value if the key could be resolved. Returns <code>null</code>
	 * otherwise.
	 * <p>
	 * 
	 * @see WApplication#getLocale()
	 * @see WString#tr(String key)
	 */
	public abstract String resolveKey(final String key);
}
