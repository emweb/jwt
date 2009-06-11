package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

/**
 * An abstract class that provides support for localized strings
 * 
 * 
 * This abstract class provides the content to localized WStrings, by resolving
 * the key to a string using the current application locale.
 * <p>
 * 
 * @see WString#tr(String)
 * @see WApplication#setLocalizedStrings(WLocalizedStrings translator)
 */
public abstract class WLocalizedStrings {
	/**
	 * Destructor.
	 */
	public void destroy() {
	}

	/**
	 * Reread the message resources.
	 * 
	 * Purge any cached key/values, if applicable.
	 * <p>
	 * The default implementation does nothing.
	 */
	public void refresh() {
	}

	/**
	 * Purge memory resources, if possible.
	 * 
	 * This is called afer event handling, and is an opportunity to conserve
	 * memory inbetween events, by freeing memory used for cached key/value
	 * bindings, if applicable.
	 * <p>
	 * The default implementation does nothing.
	 */
	public void hibernate() {
	}

	public abstract String resolveKey(String key);
}
