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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An interactive area in a widget, specified by a polygon.
 * <p>
 * 
 * The area may be added to a {@link WImage} or {@link WPaintedWidget} to
 * provide interactivity on a polygon area of the image. The polygon is
 * specified in pixel coordinates, and uses an even-odd winding rule (overlaps
 * create holes).
 * <p>
 * The polygon area corresponds to the HTML
 * <code>&lt;area shape=&quot;poly&quot;&gt;</code> tag.
 * <p>
 * 
 * @see WImage#addArea(WAbstractArea area)
 * @see WPaintedWidget#addArea(WAbstractArea area)
 * @see WCircleArea
 * @see WRectArea
 */
public class WPolygonArea extends WAbstractArea {
	private static Logger logger = LoggerFactory.getLogger(WPolygonArea.class);

	/**
	 * Creates an empty polygon.
	 * <p>
	 * Defines an empty polygon.
	 */
	public WPolygonArea() {
		super();
		this.points_ = new ArrayList<WPoint>();
	}

	/**
	 * Creates a polygon area with given vertices.
	 * <p>
	 * The polygon is defined with vertices corresponding to <code>points</code>
	 * . The polygon is closed by connecting the last point with the first
	 * point.
	 */
	public WPolygonArea(final List<WPoint> points) {
		super();
		this.points_ = points;
	}

	/**
	 * Adds a point.
	 */
	public void addPoint(int x, int y) {
		this.points_.add(new WPoint(x, y));
		this.repaint();
	}

	/**
	 * Adds a point.
	 */
	public void addPoint(double x, double y) {
		this.points_.add(new WPoint((int) x, (int) y));
		this.repaint();
	}

	/**
	 * Adds a point.
	 */
	public void addPoint(final WPoint point) {
		this.points_.add(point);
		this.repaint();
	}

	/**
	 * Adds a point.
	 */
	public void addPoint(final WPointF point) {
		this.points_.add(new WPoint((int) point.getX(), (int) point.getY()));
		this.repaint();
	}

	/**
	 * Sets the polygon vertices.
	 * <p>
	 * The polygon is defined with vertices corresponding to <code>points</code>
	 * . The polygon is closed by connecting the last point with the first
	 * point.
	 */
	public void setPoints(final List<WPoint> points) {
		Utils.copyList(points, this.points_);
		this.repaint();
	}

	/**
	 * Returns the polygon vertices.
	 * <p>
	 * 
	 * @see WPolygonArea#setPoints(List points)
	 */
	public List<WPoint> getPoints() {
		return this.points_;
	}

	private List<WPoint> points_;

	protected boolean updateDom(final DomElement element, boolean all) {
		element.setAttribute("shape", "poly");
		StringWriter coords = new StringWriter();
		for (int i = 0; i < this.points_.size(); ++i) {
			if (i != 0) {
				coords.append(',');
			}
			coords.append(String.valueOf(this.points_.get(i).getX())).append(
					',').append(String.valueOf(this.points_.get(i).getY()));
		}
		element.setAttribute("coords", coords.toString());
		return super.updateDom(element, all);
	}
}
