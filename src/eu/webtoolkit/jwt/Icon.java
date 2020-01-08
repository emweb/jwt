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
 * Enumeration that indiciates a standard icon.
 * <p>
 * 
 * @see WMessageBox
 */
public enum Icon {
	/**
	 * No icon.
	 */
	NoIcon(0),
	/**
	 * An information icon.
	 */
	Information(1),
	/**
	 * A warning icon.
	 */
	Warning(2),
	/**
	 * A critical icon.
	 */
	Critical(3),
	/**
	 * A question icon.
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
