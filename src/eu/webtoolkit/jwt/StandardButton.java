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
 * Enumeration that indicates a standard button.
 * <p>
 * Multiple buttons may be specified by logically or&apos;ing these values
 * together, e.g. <blockquote>
 * 
 * <pre>
 * Ok | Cancel
 * </pre>
 * 
 * </blockquote>
 * <p>
 * 
 * @see WMessageBox
 */
public enum StandardButton {
	/**
	 * No button.
	 */
	NoButton,
	/**
	 * An OK button.
	 */
	Ok,
	/**
	 * A Cancel button.
	 */
	Cancel,
	/**
	 * A Yes button.
	 */
	Yes,
	/**
	 * A No button.
	 */
	No,
	/**
	 * An Abort button.
	 */
	Abort,
	/**
	 * A Retry button.
	 */
	Retry,
	/**
	 * An Ignore button.
	 */
	Ignore,
	/**
	 * A Yes-to-All button.
	 */
	YesAll,
	/**
	 * A No-to-All button.
	 */
	NoAll;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
