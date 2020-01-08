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
 * Enumeration for keyboard modifiers.
 * <p>
 * 
 * @see WMouseEvent#getModifiers()
 * @see WKeyEvent#getModifiers()
 */
public enum KeyboardModifier {
	/**
	 * No modifiers.
	 */
	NoModifier,
	/**
	 * Shift key pressed.
	 */
	ShiftModifier,
	/**
	 * Control key pressed.
	 */
	ControlModifier,
	/**
	 * Alt key pressed.
	 */
	AltModifier,
	/**
	 * Meta key pressed (&quot;Windows&quot; or &quot;Command&quot; (Mac) key)
	 */
	MetaModifier;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
