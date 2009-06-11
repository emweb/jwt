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
 * An interactive area in a widget, specified by a polygon
 * 
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
	 * Default constructor.
	 * 
	 * Defines an empty polygon.
	 */
	public WPolygonArea() {
		super();
		this.points_ = new ArrayList<WPoint>();
	}

	/**
	 * Construct a polygon area with given vertices.
	 * 
	 * The polygon is defined with vertices corresponding to <i>points</i>. The
	 * polygon is closed by connecting the last point with the first point.
	 */
	public WPolygonArea(List<WPoint> points) {
		super();
		this.points_ = points;
	}

	/**
	 * Add a point.
	 */
	public void addPoint(int x, int y) {
		this.points_.add(new WPoint(x, y));
	}

	/**
	 * Add a point.
	 */
	public void addPoint(WPoint point) {
		this.points_.add(point);
	}

	/**
	 * Set the polygon vertices.
	 * 
	 * The polygon is defined with vertices corresponding to <i>points</i>. The
	 * polygon is closed by connecting the last point with the first point.
	 */
	public void setPoints(List<WPoint> points) {
		this.points_ = points;
	}

	/**
	 * Returns the polygon vertices.
	 * 
	 * @see WPolygonArea#setPoints(List points)
	 */
	public List<WPoint> getPoints() {
		return this.points_;
	}

	private List<WPoint> points_;

	protected void updateDom(DomElement element, boolean all) {
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
