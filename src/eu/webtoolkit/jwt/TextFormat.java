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
 * Enumeration that indicates the text format.
 * 
 * @see WText#setTextFormat(TextFormat textFormat)
 */
public enum TextFormat {
	/**
	 * Format text as XSS-safe XHTML markup&apos;ed text.
	 */
	XHTMLText,
	/**
	 * Format text as XHTML markup&apos;ed text.
	 */
	XHTMLUnsafeText,
	/**
	 * Format text as plain text.
	 */
	PlainText;

	public int getValue() {
		return ordinal();
	}
}
