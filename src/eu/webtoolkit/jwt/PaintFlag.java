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
 * Enumeration that indicates how to change a selection.
 * 
 * @see WPaintedWidget#update(EnumSet flags)
 * @see WPaintDevice#getPaintFlags()
 */
public enum PaintFlag {
	/**
	 * The canvas is not cleared, but further painted on.
	 */
	PaintUpdate;

	public int getValue() {
		return ordinal();
	}
}
