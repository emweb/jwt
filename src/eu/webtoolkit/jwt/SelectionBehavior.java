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
 * Enumeration that indicates what is being selected.
 * 
 * @see WTreeView#setSelectionBehavior(SelectionBehavior behavior)
 */
public enum SelectionBehavior {
	/**
	 * Select single items.
	 */
	SelectItems(0),
	/**
	 * Select only rows.
	 */
	SelectRows(1);

	private int value;

	SelectionBehavior(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
