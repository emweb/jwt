/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A layout manager which arranges widgets vertically.
 * <p>
 * 
 * This convenience class creates a vertical box layout, laying contained
 * widgets out from top to bottom.
 * <p>
 * See {@link WBoxLayout} for available member methods and more information.
 * <p>
 * 
 * <p>
 * <i><b>Note: </b>First consider if you can achieve your layout using CSS !</i>
 * </p>
 * 
 * @see WHBoxLayout
 */
public class WVBoxLayout extends WBoxLayout {
	private static Logger logger = LoggerFactory.getLogger(WVBoxLayout.class);

	/**
	 * Create a new vertical box layout.
	 * <p>
	 * 
	 * Use <code>parent=0</code> to created a layout manager that can be nested
	 * inside other layout managers.
	 */
	public WVBoxLayout(WWidget parent) {
		super(WBoxLayout.Direction.TopToBottom, parent);
	}

	/**
	 * Create a new vertical box layout.
	 * <p>
	 * Calls {@link #WVBoxLayout(WWidget parent) this((WWidget)null)}
	 */
	public WVBoxLayout() {
		this((WWidget) null);
	}
}
