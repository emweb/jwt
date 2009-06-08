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
 * Enumeration for a cursor style.
 * 
 * @see WCssDecorationStyle#setCursor(Cursor c)
 * @see WAbstractArea#setCursor(Cursor cursor)
 */
public enum Cursor {
	/**
	 * Arrow, CSS &apos;default&apos; cursor.
	 */
	ArrowCursor,
	/**
	 * Cursor chosen by the browser, CSS &apos;auto&apos; cursor.
	 */
	AutoCursor,
	/**
	 * Crosshair, CSS &apos;cross&apos; cursor.
	 */
	CrossCursor,
	/**
	 * Pointing hand, CSS &apos;pointer&apos; cursor.
	 */
	PointingHandCursor,
	/**
	 * Open hand, CSS &apos;move&apos; cursor.
	 */
	OpenHandCursor,
	/**
	 * Wait, CSS &apos;wait&apos; cursor.
	 */
	WaitCursor,
	/**
	 * Text edit, CSS &apos;text&apos; cursor.
	 */
	IBeamCursor,
	/**
	 * Help, CSS &apos;help&apos; cursor.
	 */
	WhatsThisCursor;

	public int getValue() {
		return ordinal();
	}
}
