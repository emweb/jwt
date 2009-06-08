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
 * A interactive area in a widget, specified by a rectangle
 * 
 * 
 * The area may be added to a {@link WImage} or {@link WPaintedWidget} to
 * provide interactivity on a rectangular area of the image. The rectangle is
 * specified in pixel coordinates.
 * <p>
 * <p>
 * 
 * @see WImage#addArea(WAbstractArea area)
 * @see WPaintedWidget#addArea(WAbstractArea area)
 * @see WCircleArea
 * @see WPolygonArea
 */
public class WRectArea extends WAbstractArea {
	/**
	 * Default constructor.
	 * 
	 * The default constructor creates a rectangular area spans the whole
	 * widget.
	 */
	public WRectArea() {
		super();
		this.x_ = 0;
		this.y_ = 0;
		this.width_ = 0;
		this.height_ = 0;
	}

	/**
	 * Construct a rectangular area with given geometry.
	 * 
	 * The arguments are in pixel units.
	 */
	public WRectArea(int x, int y, int width, int height) {
		super();
		this.x_ = x;
		this.y_ = y;
		this.width_ = width;
		this.height_ = height;
	}

	/**
	 * Construct a rectangular area with given geometry.
	 * 
	 * The <i>rect</i> argument is in pixel units.
	 */
	public WRectArea(WRectF rect) {
		super();
		this.x_ = (int) rect.getX();
		this.y_ = (int) rect.getY();
		this.width_ = (int) rect.getWidth();
		this.height_ = (int) rect.getHeight();
	}

	/**
	 * Set the top-left X coordinate.
	 */
	public void setX(int x) {
		this.x_ = x;
		this.repaint();
	}

	/**
	 * Returns the top-left X coordinate.
	 */
	public int getX() {
		return this.x_;
	}

	/**
	 * Set the top-left Y coordinate.
	 */
	public void setY(int y) {
		this.y_ = y;
		this.repaint();
	}

	/**
	 * Returns the top-left Y coordinate.
	 */
	public int getY() {
		return this.y_;
	}

	/**
	 * Set the width.
	 */
	public void setWidth(int width) {
		this.width_ = width;
		this.repaint();
	}

	/**
	 * Returns the width.
	 */
	public int getWidth() {
		return this.width_;
	}

	/**
	 * Set the height.
	 */
	public void setHeight(int height) {
		this.height_ = height;
		this.repaint();
	}

	/**
	 * Returns the height.
	 */
	public int getHeight() {
		return this.height_;
	}

	private int x_;
	private int y_;
	private int width_;
	private int height_;

	protected void updateDom(DomElement element, boolean all) {
		element.setAttribute("shape", "rect");
		StringWriter coords = new StringWriter();
		if (this.x_ == 0 && this.y_ == 0 && this.width_ == 0
				&& this.height_ == 0) {
			coords.append("0%,0%,100%,100%");
		} else {
			coords.append(String.valueOf(this.x_)).append(',').append(
					String.valueOf(this.y_)).append(',').append(
					String.valueOf(this.x_ + this.width_)).append(',').append(
					String.valueOf(this.y_ + this.height_));
		}
		element.setAttribute("coords", coords.toString());
		super.updateDom(element, all);
	}
}
