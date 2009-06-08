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
 * Enumeration that indicates how items may be selected.
 * 
 * @see WTreeView#setSelectionMode(SelectionMode mode)
 */
public enum SelectionMode {
	/**
	 * No selections.
	 */
	NoSelection(0),
	/**
	 * Single selection only.
	 */
	SingleSelection(1),
	/**
	 * Multiple selection.
	 */
	ExtendedSelection(3);

	private int value;

	SelectionMode(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
