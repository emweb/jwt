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
 * Enumeration for the check state of a check box.
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

	public int getValue() {
		return ordinal();
	}
}
