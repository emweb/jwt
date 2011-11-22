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
 * Enumeration that specifies the way text should be printed.
 * <p>
 * 
 * @see WPainter#drawText(WRectF rectangle, EnumSet alignmentFlags, TextFlag
 *      textFlag, CharSequence text)
 */
public enum TextFlag {
	/**
	 * Text will be printed on just one line.
	 */
	TextSingleLine,
	/**
	 * Lines will break at word boundaries.
	 */
	TextWordWrap;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
