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
 * Enumeration that indicates how line end points are rendered.
 */
public enum PenCapStyle {
	/**
	 * Flat ends.
	 */
	FlatCap,
	/**
	 * Square ends (prolongs line with half width).
	 */
	SquareCap,
	/**
	 * Round ends (terminates with a half circle).
	 */
	RoundCap;

	public int getValue() {
		return ordinal();
	}
}
