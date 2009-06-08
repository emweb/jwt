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

	public int getValue() {
		return ordinal();
	}
}
