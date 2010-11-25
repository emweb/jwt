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
 * Enumeration that indicates a relative location.
 * <p>
 * Values of CenterX, CenterY, and CenterXY are only valid for
 * {@link WCssDecorationStyle#setBackgroundImage(String image, WCssDecorationStyle.Repeat repeat, EnumSet sides)
 * WCssDecorationStyle#setBackgroundImage()}
 * <p>
 * 
 * @see WWidget#setMargin(WLength margin, EnumSet sides)
 * @see WWidget#setOffsets(WLength offset, EnumSet sides)
 * @see WWidget#setFloatSide(Side s)
 * @see WWidget#setClearSides(EnumSet sides)
 * @see WContainerWidget#setPadding(WLength length, EnumSet sides)
 * @see WCssDecorationStyle#setBackgroundImage(String image,
 *      WCssDecorationStyle.Repeat repeat, EnumSet sides)
 */
public enum Side {
	/**
	 * Top side.
	 */
	Top,
	/**
	 * Bottom side.
	 */
	Bottom,
	/**
	 * Left side.
	 */
	Left,
	/**
	 * Right side.
	 */
	Right,
	/**
	 * Center horiziontally.
	 */
	CenterX,
	/**
	 * Center vertically.
	 */
	CenterY;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}

	/**
	 * No side.
	 */
	public static final EnumSet<Side> None = EnumSet.noneOf(Side.class);
	/**
	 * (CenterX | CenterY)
	 */
	public static final EnumSet<Side> CenterXY = EnumSet.of(Side.CenterX,
			Side.CenterY);
	/**
	 * (Left | Right)
	 */
	public static final EnumSet<Side> Horizontals = EnumSet.of(Side.Left,
			Side.Right);
	/**
	 * (Top | Bottom)
	 */
	public static final EnumSet<Side> Verticals = EnumSet.of(Side.Top,
			Side.Bottom);
	/**
	 * All sides.
	 */
	public static final EnumSet<Side> All = EnumSet.of(Side.Top, Side.Bottom,
			Side.Left, Side.Right);
}
