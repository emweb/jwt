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
 * Enumeration that indicates a fill style.
 */
public enum WBrushStyle {
	/**
	 * Do not fill.
	 */
	NoBrush(0),
	/**
	 * Fill with a solid color.
	 */
	SolidPattern(1);

	private int value;

	WBrushStyle(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
