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
 * Enumeration that indicates a pen style.
 */
public enum PenStyle {
	/**
	 * Do not stroke.
	 */
	NoPen,
	/**
	 * Stroke with a solid line.
	 */
	SolidLine,
	/**
	 * Stroked with a dashed line.
	 */
	DashLine,
	/**
	 * Stroke with a dotted line.
	 */
	DotLine,
	/**
	 * Stroke with a dash dot line.
	 */
	DashDotLine,
	/**
	 * Stroke with a dash dot dot line.
	 */
	DashDotDotLine;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
