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
 * Enumeration for keyboard modifiers.
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
	 * Meta key pressed (&quot;Windows&quot; or &quot;Command&quot; (Mac) key).
	 */
	MetaModifier;

	public int getValue() {
		return ordinal();
	}
}
