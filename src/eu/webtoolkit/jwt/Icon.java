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
 * Enumeration that indiciates a standard icon.
 * <p>
 * <p>
 * <i><b>Note: </b>Not used yet. </i>
 * </p>
 */
public enum Icon {
	/**
	 * No icon.
	 */
	NoIcon(0),
	/**
	 * An information icon <i>(not implemented)</i>.
	 */
	Information(1),
	/**
	 * An warning icon <i>(not implemented)</i>.
	 */
	Warning(2),
	/**
	 * An critical icon <i>(not implemented)</i>.
	 */
	Critical(3),
	/**
	 * An question icon <i>(not implemented)</i>.
	 */
	Question(4);

	private int value;

	Icon(int value) {
		this.value = value;
	}

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return value;
	}
}
