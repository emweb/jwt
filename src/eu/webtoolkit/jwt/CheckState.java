/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
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

/**
 * Enumeration for the check state of a check box.
 * <p>
 * 
 * @see WCheckBox
 */
public enum CheckState {
	/**
	 * Unchecked.
	 */
	Unchecked,
	/**
	 * Partially checked (for a tri-state checkbox).
	 */
	PartiallyChecked,
	/**
	 * Checked.
	 */
	Checked;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
