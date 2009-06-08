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
 * Enumeration that indicates a sort order.
 */
public enum SortOrder {
	/**
	 * Ascending sort order.
	 */
	AscendingOrder,
	/**
	 * Descending sort order.
	 */
	DescendingOrder;

	public int getValue() {
		return ordinal();
	}
}
