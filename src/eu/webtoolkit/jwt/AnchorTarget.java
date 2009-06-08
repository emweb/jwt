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
 * Enumeration that specifies where the target of an anchor should be displayed.
 * 
 * @see WAnchor#setTarget(AnchorTarget target)
 */
public enum AnchorTarget {
	/**
	 * Show Instead of the application.
	 */
	TargetSelf,
	/**
	 * Show in the top level frame of the application window.
	 */
	TargetThisWindow,
	/**
	 * Show in a separate new tab or window.
	 */
	TargetNewWindow;

	public int getValue() {
		return ordinal();
	}
}
