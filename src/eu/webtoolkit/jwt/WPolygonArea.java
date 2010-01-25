/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * An interactive area in a widget, specified by a polygon
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
	public WPolygonArea(List<WPoint> points) {
		super();
		this.points_ = points;
	}

	/**
	 * Adds a point.
	 */
	public void addPoint(int x, int y) {
		this.points_.add(new WPoint(x, y));
	}

	/**
	 * Adds a point.
	 */
	public void addPoint(WPoint point) {
		this.points_.add(point);
	}

	/**
	 * Sets the polygon vertices.
	 * <p>
	 * The polygon is defined with vertices corresponding to <code>points</code>
	 * . The polygon is closed by connecting the last point with the first
	 * point.
	 */
	public void setPoints(List<WPoint> points) {
		this.points_ = points;
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

	void updateDom(DomElement element, boolean all) {
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
		super.updateDom(element, all);
	}
}
