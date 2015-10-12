/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.chart;

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
 * Enumeration of mouse wheel actions for interactive charts.
 * <p>
 * 
 * @see WCartesianChart#setWheelActions(Map wheelActions)
 */
public enum InteractiveAction {
	/**
	 * Zoom x-axis.
	 */
	ZoomX,
	/**
	 * Zoom y-axis.
	 */
	ZoomY,
	/**
	 * Zoom along both x and y-axes.
	 */
	ZoomXY,
	/**
	 * Zoom y-axis on vertical scroll, x-axis on horizontal scroll.
	 */
	ZoomMatching,
	/**
	 * Pan x-axis.
	 */
	PanX,
	/**
	 * Pan y-axis.
	 */
	PanY,
	/**
	 * Pan y-axis on vertical scroll, x-axis on horizontal scroll.
	 */
	PanMatching;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
