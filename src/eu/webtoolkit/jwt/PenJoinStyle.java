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
 * Enumeration that indicates how line joins are rendered.
 */
public enum PenJoinStyle {
	/**
	 * Pointy joins.
	 */
	MiterJoin,
	/**
	 * Squared-off joins.
	 */
	BevelJoin,
	/**
	 * Rounded joins.
	 */
	RoundJoin;

	public int getValue() {
		return ordinal();
	}
}
