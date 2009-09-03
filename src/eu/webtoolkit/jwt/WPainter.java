/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Helper class for painting on a {@link WPaintDevice}.
 * <p>
 * 
 * The painter class provides a rich interface for painting on a
 * {@link WPaintDevice}. To start painting on a device, either pass the device
 * through the constructor {@link WPainter#WPainter(WPaintDevice device)
 * WPainter() }, or use {@link WPainter#begin(WPaintDevice device) begin() }.
 * Typically, you will instantiate a WPainter from within the
 * {@link WPaintedWidget#paintEvent(WPaintDevice paintDevice) paintEvent() }
 * method, but you can also use a painter to paint directly to a particular
 * paint device, for example to create an SVG image.
 * <p>
 * The painter maintains state such as the current
 * {@link WPainter#setPen(WPen p) pen}, {@link WPainter#setBrush(WBrush b)
 * brush}, {@link WPainter#setFont(WFont f) font},
 * {@link WPainter#getWorldTransform() transformation} and clipping settings
 * (see {@link WPainter#setClipping(boolean enable) setClipping() } and
 * {@link WPainter#setClipPath(WPainterPath clipPath) setClipPath() }). A
 * particular state can be saved using {@link WPainter#save() save() } and later
 * restored using {@link WPainter#restore() restore() }.
 * <p>
 * The painting system distinguishes between device coordinates, logical
 * coordinates, and local coordinates. The device coordinate system ranges from
 * (0, 0) in the top left corner of the device, to (
 * {@link WPaintDevice#getWidth() getWidth() }, {@link WPaintDevice#getHeight()
 * getHeight() }) for the bottom right corner. The logical coordinate system
 * defines a coordinate system that may be chosen independent of the geometry of
 * the device, which is convenient to make abstraction of the actual device
 * size. Finally, the current local coordinate system may be different from the
 * logical coordinate system because of a world transformation. Initially, the
 * local coordinate system coincides with the logical coordinate system, which
 * coincides with the device coordinate system.
 * <p>
 * By setting a {@link WPainter#getViewPort() getViewPort() } and a
 * {@link WPainter#getWindow() getWindow() }, a viewPort transformation is
 * defined which maps logical coordinates onto device coordinates. By changing
 * the world transformation (using
 * {@link WPainter#setWorldTransform(WTransform matrix, boolean combine)
 * setWorldTransform() }, or {@link WPainter#translate(WPointF p) translate() },
 * {@link WPainter#rotate(double angle) rotate() },
 * {@link WPainter#scale(double sx, double sy) scale() } operations), it is
 * defined how current local coordinates map onto logical coordinates.
 * <p>
 * Although the painter has support for clipping using an arbitrary
 * {@link WPainterPath path}, not all devices support clipping.
 * <p>
 * 
 * @see WPaintedWidget#paintEvent(WPaintDevice paintDevice)
 */
public class WPainter {
	/**
	 * Enumeration for render hints.
	 */
	public enum RenderHint {
		/**
		 * Antialiasing.
		 */
		Antialiasing(0X1);

		private int value;

		RenderHint(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	/**
	 * Default constructor.
	 * <p>
	 * Before painting, you must invoke
	 * {@link WPainter#begin(WPaintDevice device) begin() } on a paint device.
	 * <p>
	 * 
	 * @see WPainter#WPainter(WPaintDevice device)
	 */
	public WPainter() {
		this.device_ = null;
		this.viewPort_ = new WRectF();
		this.window_ = new WRectF();
		this.viewTransform_ = new WTransform();
		this.stateStack_ = new ArrayList<WPainter.State>();
	}

	/**
	 * Create a paint and initialize it for a paint device.
	 */
	public WPainter(WPaintDevice device) {
		this.device_ = null;
		this.viewPort_ = new WRectF();
		this.window_ = new WRectF();
		this.viewTransform_ = new WTransform();
		this.stateStack_ = new ArrayList<WPainter.State>();
		this.begin(device);
	}

	/**
	 * Destructor.
	 */
	public void destroy() {
		this.isEnd();
	}

	/**
	 * Begin painting on a paint device.
	 * <p>
	 * Starts painting on a paint device. The paint device is automatically
	 * cleared to become entirely transparent.
	 * <p>
	 * 
	 * @see WPainter#isEnd()
	 * @see WPainter#isActive()
	 */
	public boolean begin(WPaintDevice device) {
		if (this.device_ != null) {
			return false;
		}
		if (device.isPaintActive()) {
			return false;
		}
		this.stateStack_.add(new WPainter.State());
		this.device_ = device;
		this.device_.setPainter(this);
		this.device_.init();
		this.viewPort_.setX(0);
		this.viewPort_.setY(0);
		this.viewPort_.setWidth(this.device_.getWidth().getValue());
		this.viewPort_.setHeight(this.device_.getHeight().getValue());
		this.window_.assign(this.viewPort_);
		this.recalculateViewTransform();
		return true;
	}

	/**
	 * Return whether this painter is active on a paint device.
	 * <p>
	 * 
	 * @see WPainter#begin(WPaintDevice device)
	 * @see WPainter#isEnd()
	 */
	public boolean isActive() {
		return this.device_ != null;
	}

	/**
	 * End painting.
	 * <p>
	 * This method is called automatically from the destructor.
	 */
	public boolean isEnd() {
		if (!(this.device_ != null)) {
			return false;
		}
		this.device_.done();
		this.device_.setPainter((WPainter) null);
		this.device_ = null;
		this.stateStack_.clear();
		return true;
	}

	/**
	 * The device on which this painter is active (or 0 if not active).
	 * <p>
	 * 
	 * @see WPainter#begin(WPaintDevice device)
	 * @see WPainter#WPainter(WPaintDevice device)
	 * @see WPainter#isActive()
	 */
	public WPaintDevice getDevice() {
		return this.device_;
	}

	/**
	 * Set a render hint.
	 * <p>
	 * Renderers may ignore particular hints for which they have no support.
	 */
	public void setRenderHint(WPainter.RenderHint hint, boolean on) {
		int old = this.getS().renderHints_;
		if (on) {
			this.getS().renderHints_ |= hint.getValue();
		} else {
			this.getS().renderHints_ &= ~hint.getValue();
		}
		if (this.device_ != null && old != this.getS().renderHints_) {
			this.device_.setChanged(EnumSet.of(WPaintDevice.ChangeFlag.Hints));
		}
	}

	/**
	 * Set a render hint.
	 * <p>
	 * Calls {@link #setRenderHint(WPainter.RenderHint hint, boolean on)
	 * setRenderHint(hint, true)}
	 */
	public final void setRenderHint(WPainter.RenderHint hint) {
		setRenderHint(hint, true);
	}

	/**
	 * Get the current render hints.
	 * <p>
	 * Returns the logical OR of render hints currently set.
	 * <p>
	 * 
	 * @see WPainter#setRenderHint(WPainter.RenderHint hint, boolean on)
	 */
	public int getRenderHints() {
		return this.getS().renderHints_;
	}

	/**
	 * Draw an arc.
	 * <p>
	 * Draws an arc using the current pen, and fills using the current brush.
	 * <p>
	 * The arc is defined as a segment from an ellipse, which fits in the
	 * <i>rectangle</i>. The segment starts at <i>startAngle</i>, and spans an
	 * angle given by <i>spanAngle</i>. These angles have as unit 1/16th of a
	 * degree, and are measured counter-clockwise starting from the 3
	 * o&apos;clock position.
	 * <p>
	 * 
	 * @see WPainter#drawEllipse(WRectF rectangle)
	 * @see WPainter#drawChord(WRectF rectangle, int startAngle, int spanAngle)
	 * @see WPainter#drawArc(double x, double y, double width, double height,
	 *      int startAngle, int spanAngle)
	 */
	public void drawArc(WRectF rectangle, int startAngle, int spanAngle) {
		WBrush oldBrush = this.getBrush().clone();
		this.setBrush(new WBrush(WBrushStyle.NoBrush));
		this.device_.drawArc(rectangle.getNormalized(), startAngle / 16.,
				spanAngle / 16.);
		this.setBrush(oldBrush);
	}

	/**
	 * Draw an arc.
	 * <p>
	 * This is an overloaded method for convenience.
	 * <p>
	 * 
	 * @see WPainter#drawArc(WRectF rectangle, int startAngle, int spanAngle)
	 */
	public void drawArc(double x, double y, double width, double height,
			int startAngle, int spanAngle) {
		this.drawArc(new WRectF(x, y, width, height), startAngle, spanAngle);
	}

	/**
	 * Draw a chord.
	 * <p>
	 * Draws an arc using the current pen, and connects start and end point with
	 * a line. The area is filled using the current brush.
	 * <p>
	 * The arc is defined as a segment from an ellipse, which fits in the
	 * <i>rectangle</i>. The segment starts at <i>startAngle</i>, and spans an
	 * angle given by <i>spanAngle</i>. These angles have as unit 1/16th of a
	 * degree, and are measured counter-clockwise starting at 3 o&apos;clock.
	 * <p>
	 * 
	 * @see WPainter#drawEllipse(WRectF rectangle)
	 * @see WPainter#drawArc(WRectF rectangle, int startAngle, int spanAngle)
	 * @see WPainter#drawChord(double x, double y, double width, double height,
	 *      int startAngle, int spanAngle)
	 */
	public void drawChord(WRectF rectangle, int startAngle, int spanAngle) {
		WTransform oldTransform = this.getWorldTransform().clone();
		this.translate(rectangle.getCenter().getX(), rectangle.getCenter()
				.getY());
		this.scale(1., rectangle.getHeight() / rectangle.getWidth());
		double start = startAngle / 16.;
		double span = spanAngle / 16.;
		WPainterPath path = new WPainterPath();
		path.arcMoveTo(0, 0, rectangle.getWidth() / 2., start);
		path.arcTo(0, 0, rectangle.getWidth() / 2., start, span);
		path.closeSubPath();
		this.drawPath(path);
		this.setWorldTransform(oldTransform);
	}

	/**
	 * Draw a chord.
	 * <p>
	 * This is an overloaded method for convenience.
	 * <p>
	 * 
	 * @see WPainter#drawChord(WRectF rectangle, int startAngle, int spanAngle)
	 */
	public void drawChord(double x, double y, double width, double height,
			int startAngle, int spanAngle) {
		this.drawChord(new WRectF(x, y, width, height), startAngle, spanAngle);
	}

	/**
	 * Draw an ellipse.
	 * <p>
	 * Draws an ellipse using the current pen and fills it using the current
	 * brush.
	 * <p>
	 * The ellipse is defined as being bounded by the <i>rectangle</i>.
	 * <p>
	 * 
	 * @see WPainter#drawArc(WRectF rectangle, int startAngle, int spanAngle)
	 * @see WPainter#drawEllipse(double x, double y, double width, double
	 *      height)
	 */
	public void drawEllipse(WRectF rectangle) {
		this.device_.drawArc(rectangle.getNormalized(), 0, 360);
	}

	/**
	 * Draw an ellipse.
	 * <p>
	 * This is an overloaded method for convenience.
	 * <p>
	 * 
	 * @see WPainter#drawEllipse(WRectF rectangle)
	 */
	public void drawEllipse(double x, double y, double width, double height) {
		this.drawEllipse(new WRectF(x, y, width, height));
	}

	/**
	 * An image that can be rendered on a {@link WPainter}
	 * <p>
	 * 
	 * The image is specified in terms of a URL, and the width and height.
	 * <p>
	 * 
	 * @see WPainter#drawImage(WPointF point, WPainter.Image image)
	 */
	public static class Image {
		/**
		 * Create an image.
		 * <p>
		 * Create an image which is located at the <i>uri</i>, and which has
		 * dimensions <i>width</i> x <i>height</i>.
		 */
		public Image(String uri, int width, int height) {
			this.uri_ = uri;
			this.width_ = width;
			this.height_ = height;
		}

		// public Image(String uri, String file) ;
		/**
		 * Returns the uri.
		 */
		public String getUri() {
			return this.uri_;
		}

		/**
		 * Returns the image width.
		 */
		public int getWidth() {
			return this.width_;
		}

		/**
		 * Returns the image height.
		 */
		public int getHeight() {
			return this.height_;
		}

		private String uri_;
		private int width_;
		private int height_;
	}

	/**
	 * Draw an image.
	 * <p>
	 * Draws the <i>image</i> so that the top left corner corresponds to
	 * <i>point</i>.
	 * <p>
	 * This is an overloaded method provided for convenience.
	 */
	public void drawImage(WPointF point, WPainter.Image image) {
		this.drawImage(new WRectF(point.getX(), point.getY(), image.getWidth(),
				image.getHeight()), image, new WRectF(0, 0, image.getWidth(),
				image.getHeight()));
	}

	/**
	 * Draw part of an image.
	 * <p>
	 * Draws the <i>sourceRect</i> rectangle from an image to the location
	 * <i>point</i>.
	 * <p>
	 * This is an overloaded method provided for convenience.
	 */
	public void drawImage(WPointF point, WPainter.Image image, WRectF sourceRect) {
		this.drawImage(new WRectF(point.getX(), point.getY(), sourceRect
				.getWidth(), sourceRect.getHeight()), image, sourceRect);
	}

	/**
	 * Draw an image inside a rectangle.
	 * <p>
	 * Draw the <i>image</i> inside <i>rect</i> (If necessary, the image is
	 * scaled to fit into the rectangle).
	 * <p>
	 * This is an overloaded method provided for convenience.
	 */
	public void drawImage(WRectF rect, WPainter.Image image) {
		this.drawImage(rect, image, new WRectF(0, 0, image.getWidth(), image
				.getHeight()));
	}

	/**
	 * Draw part of an image inside a rectangle.
	 * <p>
	 * Draws the <i>sourceRect</i> rectangle from an image inside <i>rect</i>
	 * (If necessary, the image is scaled to fit into the rectangle).
	 */
	public void drawImage(WRectF rect, WPainter.Image image, WRectF sourceRect) {
		this.device_.drawImage(rect.getNormalized(), image.getUri(), image
				.getWidth(), image.getHeight(), sourceRect.getNormalized());
	}

	/**
	 * Draw part of an image.
	 * <p>
	 * Draws the <i>sourceRect</i> rectangle with top left corner (<i>sx</i>,
	 * <i>sy</i>) and size <i>sw</i> x <i>sh</i> from an image to the location
	 * (<i>x</i>, <i>y</i>).
	 */
	public void drawImage(double x, double y, WPainter.Image image, double sx,
			double sy, double sw, double sh) {
		if (sw <= 0) {
			sw = image.getWidth() - sx;
		}
		if (sh <= 0) {
			sh = image.getHeight() - sy;
		}
		this.device_.drawImage(new WRectF(x, y, sw, sh), image.getUri(), image
				.getWidth(), image.getHeight(), new WRectF(sx, sy, sw, sh));
	}

	/**
	 * Draw part of an image.
	 * <p>
	 * Calls
	 * {@link #drawImage(double x, double y, WPainter.Image image, double sx, double sy, double sw, double sh)
	 * drawImage(x, y, image, 0, 0, - 1, - 1)}
	 */
	public final void drawImage(double x, double y, WPainter.Image image) {
		drawImage(x, y, image, 0, 0, -1, -1);
	}

	/**
	 * Draw part of an image.
	 * <p>
	 * Calls
	 * {@link #drawImage(double x, double y, WPainter.Image image, double sx, double sy, double sw, double sh)
	 * drawImage(x, y, image, sx, 0, - 1, - 1)}
	 */
	public final void drawImage(double x, double y, WPainter.Image image,
			double sx) {
		drawImage(x, y, image, sx, 0, -1, -1);
	}

	/**
	 * Draw part of an image.
	 * <p>
	 * Calls
	 * {@link #drawImage(double x, double y, WPainter.Image image, double sx, double sy, double sw, double sh)
	 * drawImage(x, y, image, sx, sy, - 1, - 1)}
	 */
	public final void drawImage(double x, double y, WPainter.Image image,
			double sx, double sy) {
		drawImage(x, y, image, sx, sy, -1, -1);
	}

	/**
	 * Draw part of an image.
	 * <p>
	 * Calls
	 * {@link #drawImage(double x, double y, WPainter.Image image, double sx, double sy, double sw, double sh)
	 * drawImage(x, y, image, sx, sy, sw, - 1)}
	 */
	public final void drawImage(double x, double y, WPainter.Image image,
			double sx, double sy, double sw) {
		drawImage(x, y, image, sx, sy, sw, -1);
	}

	/**
	 * Draw a line.
	 * <p>
	 * Draws a line using the current pen.
	 * <p>
	 * 
	 * @see WPainter#drawLine(WPointF p1, WPointF p2)
	 * @see WPainter#drawLine(double x1, double y1, double x2, double y2)
	 */
	public void drawLine(WLineF line) {
		this.drawLine(line.getX1(), line.getY1(), line.getX2(), line.getY2());
	}

	/**
	 * Draw a line.
	 * <p>
	 * Draws a line defined by two points.
	 * <p>
	 * 
	 * @see WPainter#drawLine(WLineF line)
	 * @see WPainter#drawLine(double x1, double y1, double x2, double y2)
	 */
	public void drawLine(WPointF p1, WPointF p2) {
		this.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
	}

	/**
	 * Draw a line.
	 * <p>
	 * Draws a line defined by two points.
	 * <p>
	 * 
	 * @see WPainter#drawLine(WLineF line)
	 * @see WPainter#drawLine(WPointF p1, WPointF p2)
	 */
	public void drawLine(double x1, double y1, double x2, double y2) {
		this.device_.drawLine(x1, y1, x2, y2);
	}

	/**
	 * Draws an array of lines.
	 * <p>
	 * Draws the <i>lineCount</i> first lines from the given array of lines.
	 */
	public void drawLines(WLineF[] lines, int lineCount) {
		for (int i = 0; i < lineCount; ++i) {
			this.drawLine(lines[i]);
		}
	}

	/**
	 * Draws an array of lines.
	 * <p>
	 * Draws <i>lineCount</i> lines, where each line is specified using a begin
	 * and end point that are read from an array. Thus, the <i>pointPairs</i>
	 * array must have at least 2*<i>lineCount</i> points.
	 */
	public void drawLines(WPointF[] pointPairs, int lineCount) {
		for (int i = 0; i < lineCount; ++i) {
			this.drawLine(pointPairs[i * 2], pointPairs[i * 2 + 1]);
		}
	}

	/**
	 * Draws an array of lines.
	 * <p>
	 * Draws the lines given in the vector.
	 */
	public void drawLinesLine(List<WLineF> lines) {
		for (int i = 0; i < lines.size(); ++i) {
			this.drawLine(lines.get(i));
		}
	}

	/**
	 * Draws an array of lines.
	 * <p>
	 * Draws a number of lines that are specified by pairs of begin- and
	 * endpoints. The vector should hold a number of points that is a multiple
	 * of two.
	 */
	public void drawLinesPoint(List<WPointF> pointPairs) {
		for (int i = 0; i < pointPairs.size() / 2; ++i) {
			this.drawLine(pointPairs.get(i * 2), pointPairs.get(i * 2 + 1));
		}
	}

	/**
	 * Draw a (complex) path.
	 * <p>
	 * Draws and fills the given path using the current pen and brush.
	 * <p>
	 * 
	 * @see WPainter#strokePath(WPainterPath path, WPen p)
	 * @see WPainter#fillPath(WPainterPath path, WBrush b)
	 */
	public void drawPath(WPainterPath path) {
		this.device_.drawPath(path);
	}

	/**
	 * Draw a pie.
	 * <p>
	 * Draws an arc using the current pen, and connects start and end point with
	 * the center of the corresponding ellipse. The area is filled using the
	 * current brush.
	 * <p>
	 * The arc is defined as a segment from an ellipse, which fits in the
	 * <i>rectangle</i>. The segment starts at <i>startAngle</i>, and spans an
	 * angle given by <i>spanAngle</i>. These angles have as unit 1/16th of a
	 * degree, and are measured counter-clockwise starting at 3 o&apos;clock.
	 * <p>
	 * 
	 * @see WPainter#drawEllipse(WRectF rectangle)
	 * @see WPainter#drawArc(WRectF rectangle, int startAngle, int spanAngle)
	 * @see WPainter#drawPie(double x, double y, double width, double height,
	 *      int startAngle, int spanAngle)
	 */
	public void drawPie(WRectF rectangle, int startAngle, int spanAngle) {
		WTransform oldTransform = this.getWorldTransform().clone();
		this.translate(rectangle.getCenter().getX(), rectangle.getCenter()
				.getY());
		this.scale(1., rectangle.getHeight() / rectangle.getWidth());
		WPainterPath path = new WPainterPath(new WPointF(0.0, 0.0));
		path.arcTo(0.0, 0.0, rectangle.getWidth() / 2.0, startAngle / 16.,
				spanAngle / 16.);
		path.closeSubPath();
		this.drawPath(path);
		this.setWorldTransform(oldTransform);
	}

	/**
	 * Draw a pie.
	 * <p>
	 * This is an overloaded method for convenience.
	 * <p>
	 * 
	 * @see WPainter#drawPie(WRectF rectangle, int startAngle, int spanAngle)
	 */
	public void drawPie(double x, double y, double width, double height,
			int startAngle, int spanAngle) {
		this.drawPie(new WRectF(x, y, width, height), startAngle, spanAngle);
	}

	/**
	 * Draw a point.
	 * <p>
	 * Draw a single point using the current pen.
	 * <p>
	 * 
	 * @see WPainter#drawPoint(double x, double y)
	 */
	public void drawPoint(WPointF point) {
		this.drawPoint(point.getX(), point.getY());
	}

	/**
	 * Draw a point.
	 * <p>
	 * This is an overloaded method for convenience.
	 * <p>
	 * 
	 * @see WPainter#drawPoint(WPointF point)
	 */
	public void drawPoint(double x, double y) {
		this.drawLine(x - 0.1, y - 0.1, x + 0.1, y + 0.1);
	}

	/**
	 * Draw a number of points.
	 * <p>
	 * Draws the <i>pointCount</i> first points from the given array of points.
	 * <p>
	 * 
	 * @see WPainter#drawPoint(WPointF point)
	 */
	public void drawPoints(WPointF[] points, int pointCount) {
		for (int i = 0; i < pointCount; ++i) {
			this.drawPoint(points[i]);
		}
	}

	/**
	 * Draw a polygon.
	 * <p>
	 * Draws a polygon that is specified by a list of points, using the current
	 * pen. The polygon is closed by connecting the last point with the first
	 * point, and filled using the current brush.
	 * <p>
	 * 
	 * @see WPainter#drawPath(WPainterPath path)
	 * @see WPainter#drawPolyline(WPointF[] points, int pointCount)
	 */
	public void drawPolygon(WPointF[] points, int pointCount) {
		if (pointCount < 2) {
			return;
		}
		WPainterPath path = new WPainterPath();
		path.moveTo(points[0]);
		for (int i = 1; i < pointCount; ++i) {
			path.lineTo(points[i]);
		}
		path.closeSubPath();
		this.drawPath(path);
	}

	/**
	 * Draw a polyline.
	 * <p>
	 * Draws a polyline that is specified by a list of points, using the current
	 * pen.
	 * <p>
	 * 
	 * @see WPainter#drawPath(WPainterPath path)
	 * @see WPainter#drawPolygon(WPointF[] points, int pointCount)
	 */
	public void drawPolyline(WPointF[] points, int pointCount) {
		if (pointCount < 2) {
			return;
		}
		WPainterPath path = new WPainterPath();
		path.moveTo(points[0]);
		for (int i = 1; i < pointCount; ++i) {
			path.lineTo(points[i]);
		}
		WBrush oldBrush = this.getBrush().clone();
		this.setBrush(new WBrush());
		this.drawPath(path);
		this.setBrush(oldBrush);
	}

	/**
	 * Draw a rectangle.
	 * <p>
	 * Draws and fills a rectangle using the current pen and brush.
	 * <p>
	 * 
	 * @see WPainter#drawRect(double x, double y, double width, double height)
	 */
	public void drawRect(WRectF rectangle) {
		this.drawRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(),
				rectangle.getHeight());
	}

	/**
	 * Draw a rectangle.
	 * <p>
	 * This is an overloaded method for convenience.
	 * <p>
	 * 
	 * @see WPainter#drawRect(WRectF rectangle)
	 */
	public void drawRect(double x, double y, double width, double height) {
		WPainterPath path = new WPainterPath(new WPointF(x, y));
		path.lineTo(x + width, y);
		path.lineTo(x + width, y + height);
		path.lineTo(x, y + height);
		path.closeSubPath();
		this.drawPath(path);
	}

	/**
	 * Draw a number of rectangles.
	 * <p>
	 * Draws and fills the <i>rectCount</i> first rectangles from the given
	 * array, using the current pen and brush.
	 * <p>
	 * 
	 * @see WPainter#drawRect(WRectF rectangle)
	 */
	public void drawRects(WRectF[] rectangles, int rectCount) {
		for (int i = 0; i < rectCount; ++i) {
			this.drawRect(rectangles[i]);
		}
	}

	/**
	 * Draw a number of rectangles.
	 * <p>
	 * Draws and fills a list of rectangles using the current pen and brush.
	 * <p>
	 * 
	 * @see WPainter#drawRect(WRectF rectangle)
	 */
	public void drawRects(List<WRectF> rectangles) {
		for (int i = 0; i < rectangles.size(); ++i) {
			this.drawRect(rectangles.get(i));
		}
	}

	/**
	 * Draw text.
	 * <p>
	 * Draw text using inside the rectangle, using the current font. The text is
	 * aligned inside the rectangle following alignment indications given in
	 * <i>flags</i>. The text is drawn using the current transformation, pen
	 * color ({@link WPainter#getPen() getPen() }) and font settings (
	 * {@link WPainter#getFont() getFont() }).
	 * <p>
	 * Flags is the logical OR of a horizontal and vertical alignment.
	 * Horizontal alignment may be one of AlignLeft, AlignCenter, or AlignRight.
	 * Vertical alignment is one of AlignTop, AlignMiddle or AlignBottom.
	 * <p>
	 * <i>Note: HtmlCanvas will always render the text horizontally (unaffected
	 * by rotation), and unaffected by the scaling component of the
	 * transformation matrix. Also, text is overlayed on top of painted shapes,
	 * and is not covered by shapes that are painted after the text. Use the SVG
	 * and VML renderers (WPaintedWidget::inlineSvgVml) for the most accurate
	 * font rendering.</i>
	 */
	public void drawText(WRectF rectangle, EnumSet<AlignmentFlag> flags,
			CharSequence text) {
		this.device_.drawText(rectangle.getNormalized(), flags, text);
	}

	/**
	 * Draw text.
	 * <p>
	 * This is an overloaded method for convenience.
	 * <p>
	 * 
	 * @see WPainter#drawText(WRectF rectangle, EnumSet flags, CharSequence
	 *      text)
	 */
	public void drawText(double x, double y, double width, double height,
			EnumSet<AlignmentFlag> flags, CharSequence text) {
		this.device_.drawText(new WRectF(x, y, width, height), flags, text);
	}

	/**
	 * Fill a (complex) path.
	 * <p>
	 * Like {@link WPainter#drawPath(WPainterPath path) drawPath() }, but does
	 * not stroke the path, and fills the path with the given <i>brush</i>.
	 * <p>
	 * 
	 * @see WPainter#drawPath(WPainterPath path)
	 * @see WPainter#strokePath(WPainterPath path, WPen p)
	 */
	public void fillPath(WPainterPath path, WBrush b) {
		WBrush oldBrush = this.getBrush().clone();
		WPen oldPen = this.getPen().clone();
		this.setBrush(b);
		this.setPen(new WPen(PenStyle.NoPen));
		this.drawPath(path);
		this.setBrush(oldBrush);
		this.setPen(oldPen);
	}

	/**
	 * Fill a rectangle.
	 * <p>
	 * Like {@link WPainter#drawRect(WRectF rectangle) drawRect() }, but does
	 * not stroke the rect, and fills the rect with the given <i>brush</i>.
	 * <p>
	 * 
	 * @see WPainter#drawRect(WRectF rectangle)
	 */
	public void fillRect(WRectF rectangle, WBrush b) {
		WBrush oldBrush = this.getBrush().clone();
		WPen oldPen = this.getPen().clone();
		this.setBrush(b);
		this.setPen(new WPen(PenStyle.NoPen));
		this.drawRect(rectangle);
		this.setBrush(oldBrush);
		this.setPen(oldPen);
	}

	/**
	 * Fill a rectangle.
	 * <p>
	 * This is an overloaded method for convenience.
	 * <p>
	 * 
	 * @see WPainter#fillRect(WRectF rectangle, WBrush b)
	 */
	public void fillRect(double x, double y, double width, double height,
			WBrush brush) {
		this.fillRect(new WRectF(x, y, width, height), brush);
	}

	/**
	 * Stroke a path.
	 * <p>
	 * Like {@link WPainter#drawPath(WPainterPath path) drawPath() }, but does
	 * not fill the path, and strokes the path with the given <i>pen</i>.
	 * <p>
	 * 
	 * @see WPainter#drawPath(WPainterPath path)
	 * @see WPainter#fillPath(WPainterPath path, WBrush b)
	 */
	public void strokePath(WPainterPath path, WPen p) {
		WBrush oldBrush = this.getBrush().clone();
		WPen oldPen = this.getPen().clone();
		this.setBrush(new WBrush());
		this.setPen(p);
		this.drawPath(path);
		this.setBrush(oldBrush);
		this.setPen(oldPen);
	}

	/**
	 * Change the fill style.
	 * <p>
	 * Changes the fills style for subsequent draw operations.
	 * <p>
	 * 
	 * @see WPainter#getBrush()
	 * @see WPainter#setPen(WPen p)
	 */
	public void setBrush(WBrush b) {
		if (!this.getBrush().equals(b)) {
			this.getS().currentBrush_ = b;
			this.device_.setChanged(EnumSet.of(WPaintDevice.ChangeFlag.Brush));
		}
	}

	/**
	 * Change the font.
	 * <p>
	 * Changes the font for subsequent text rendering. Note that only font sizes
	 * that are defined as an explicit size (see {@link WFont.Size#FixedSize})
	 * will render correctly in all devices (SVG, VML, and HtmlCanvas).
	 * <p>
	 * 
	 * @see WPainter#getFont()
	 * @see WPainter#drawText(WRectF rectangle, EnumSet flags, CharSequence
	 *      text)
	 */
	public void setFont(WFont f) {
		if (!this.getFont().equals(f)) {
			this.getS().currentFont_ = f;
			this.device_.setChanged(EnumSet.of(WPaintDevice.ChangeFlag.Font));
		}
	}

	/**
	 * Change the pen.
	 * <p>
	 * Changes the pen used for stroking subsequent draw operations.
	 * <p>
	 * 
	 * @see WPainter#getPen()
	 * @see WPainter#setBrush(WBrush b)
	 */
	public void setPen(WPen p) {
		if (!this.getPen().equals(p)) {
			this.getS().currentPen_ = p;
			this.device_.setChanged(EnumSet.of(WPaintDevice.ChangeFlag.Pen));
		}
	}

	/**
	 * Returns the current brush.
	 * <p>
	 * Returns the brush style that is currently used for filling.
	 * <p>
	 * 
	 * @see WPainter#setBrush(WBrush b)
	 */
	public WBrush getBrush() {
		return this.getS().currentBrush_;
	}

	/**
	 * Returns the current font.
	 * <p>
	 * Returns the font that is currently used for rendering text. The default
	 * font is a 10pt sans serif font.
	 * <p>
	 * 
	 * @see WPainter#setFont(WFont f)
	 */
	public WFont getFont() {
		return this.getS().currentFont_;
	}

	/**
	 * Returns the current pen.
	 * <p>
	 * Returns the pen that is currently used for stroking.
	 * <p>
	 * 
	 * @see WPainter#setPen(WPen p)
	 */
	public WPen getPen() {
		return this.getS().currentPen_;
	}

	/**
	 * Enable or disable clipping.
	 * <p>
	 * Enables are disables clipping for subsequent operations using the current
	 * clip path set using {@link WPainter#setClipPath(WPainterPath clipPath)
	 * setClipPath() }.
	 * <p>
	 * <i>Note: Clipping is not supported for the VML renderer.</i>
	 * <p>
	 * 
	 * @see WPainter#hasClipping()
	 * @see WPainter#setClipPath(WPainterPath clipPath)
	 */
	public void setClipping(boolean enable) {
		if (this.getS().clipping_ != enable) {
			this.getS().clipping_ = enable;
			this.device_.setChanged(EnumSet
					.of(WPaintDevice.ChangeFlag.Clipping));
		}
	}

	/**
	 * Return whether clipping is enabled.
	 * <p>
	 * <i>Note: Clipping is not supported for the VML renderer.</i>
	 * <p>
	 * 
	 * @see WPainter#setClipping(boolean enable)
	 * @see WPainter#setClipPath(WPainterPath clipPath)
	 */
	public boolean hasClipping() {
		return this.getS().clipping_;
	}

	/**
	 * Set the clip path.
	 * <p>
	 * Sets the path that is used for clipping subsequent drawing operations.
	 * The clip path is only used when clipping is enabled using
	 * {@link WPainter#setClipping(boolean enable) setClipping() }. The path is
	 * specified in local coordinates.
	 * <p>
	 * <i>Note: Only clipping with a rectangle is supported for the VML renderer
	 * (see {@link WPainterPath#addRect(WRectF rectangle) WPainterPath#addRect()
	 * }). The rectangle must, after applying the combined transformation
	 * system, be aligned with the window.</i>
	 * <p>
	 * 
	 * @see WPainter#getClipPath()
	 * @see WPainter#setClipping(boolean enable)
	 */
	public void setClipPath(WPainterPath clipPath) {
		this.getS().clipPath_.assign(clipPath);
		this.getS().clipPathTransform_.assign(this.getCombinedTransform());
		if (this.getS().clipping_) {
			this.device_.setChanged(EnumSet
					.of(WPaintDevice.ChangeFlag.Clipping));
		}
	}

	/**
	 * Get the clip path.
	 * <p>
	 * The clip path is returned as it was defined: in the local coordinates at
	 * time of definition.
	 * <p>
	 * 
	 * @see WPainter#setClipPath(WPainterPath clipPath)
	 */
	public WPainterPath getClipPath() {
		return this.getS().clipPath_;
	}

	/**
	 * Reset the current transformation.
	 * <p>
	 * Resets the current transformation to the identity transformation matrix,
	 * so that the logical coordinate system coincides with the device
	 * coordinate system.
	 */
	public void resetTransform() {
		this.getS().worldTransform_.reset();
		this.device_.setChanged(EnumSet.of(WPaintDevice.ChangeFlag.Transform));
	}

	/**
	 * Rotate the logical coordinate system.
	 * <p>
	 * Rotates the logical coordinate system around its origin. The <i>angle</i>
	 * is specified in degrees, and positive values are clock-wise.
	 * <p>
	 * 
	 * @see WPainter#scale(double sx, double sy)
	 * @see WPainter#translate(double dx, double dy)
	 * @see WPainter#resetTransform()
	 */
	public void rotate(double angle) {
		this.getS().worldTransform_.rotate(angle);
		this.device_.setChanged(EnumSet.of(WPaintDevice.ChangeFlag.Transform));
	}

	/**
	 * Scale the logical coordinate system.
	 * <p>
	 * Scales the logical coordinate system around its origin, by a factor in
	 * the X and Y directions.
	 * <p>
	 * 
	 * @see WPainter#rotate(double angle)
	 * @see WPainter#translate(double dx, double dy)
	 * @see WPainter#resetTransform()
	 */
	public void scale(double sx, double sy) {
		this.getS().worldTransform_.scale(sx, sy);
		this.device_.setChanged(EnumSet.of(WPaintDevice.ChangeFlag.Transform));
	}

	/**
	 * Translate the origin of the logical coordinate system.
	 * <p>
	 * Translates the origin of the logical coordinate system to a new location
	 * relative to the current logical coordinate system.
	 * <p>
	 * 
	 * @see WPainter#translate(double dx, double dy)
	 * @see WPainter#rotate(double angle)
	 * @see WPainter#scale(double sx, double sy)
	 * @see WPainter#resetTransform()
	 */
	public void translate(WPointF p) {
		this.translate(p.getX(), p.getY());
	}

	/**
	 * Translate the origin of the logical coordinate system.
	 * <p>
	 * Translates the origin of the logical coordinate system to a new location
	 * relative to the logical coordinate system.
	 * <p>
	 * 
	 * @see WPainter#translate(WPointF p)
	 * @see WPainter#rotate(double angle)
	 * @see WPainter#scale(double sx, double sy)
	 * @see WPainter#resetTransform()
	 */
	public void translate(double dx, double dy) {
		this.getS().worldTransform_.translate(dx, dy);
		this.device_.setChanged(EnumSet.of(WPaintDevice.ChangeFlag.Transform));
	}

	/**
	 * Set a transformation for the logical coordinate system.
	 * <p>
	 * Sets a new transformation which transforms logical coordinates to device
	 * coordinates. When <i>combine</i> is true, the transformation is combined
	 * with the current world transformation matrix.
	 * <p>
	 * 
	 * @see WPainter#getWorldTransform()
	 * @see WPainter#rotate(double angle)
	 * @see WPainter#scale(double sx, double sy)
	 * @see WPainter#translate(double dx, double dy)
	 * @see WPainter#resetTransform()
	 */
	public void setWorldTransform(WTransform matrix, boolean combine) {
		if (combine) {
			this.getS().worldTransform_.multiply(matrix);
		} else {
			this.getS().worldTransform_.assign(matrix);
		}
		this.device_.setChanged(EnumSet.of(WPaintDevice.ChangeFlag.Transform));
	}

	/**
	 * Set a transformation for the logical coordinate system.
	 * <p>
	 * Calls {@link #setWorldTransform(WTransform matrix, boolean combine)
	 * setWorldTransform(matrix, false)}
	 */
	public final void setWorldTransform(WTransform matrix) {
		setWorldTransform(matrix, false);
	}

	/**
	 * Get the current world transformation matrix.
	 * <p>
	 * 
	 * @see WPainter#setWorldTransform(WTransform matrix, boolean combine)
	 */
	public WTransform getWorldTransform() {
		return this.getS().worldTransform_;
	}

	/**
	 * Save the current state.
	 * <p>
	 * A copy of the current state is saved on a stack. This state will may
	 * later be restored by popping this state from the stack using
	 * {@link WPainter#restore() restore() }.
	 * <p>
	 * The state that is saved is the current {@link WPainter#setPen(WPen p)
	 * pen}, {@link WPainter#setBrush(WBrush b) brush},
	 * {@link WPainter#setFont(WFont f) font},
	 * {@link WPainter#getWorldTransform() transformation} and clipping settings
	 * (see {@link WPainter#setClipping(boolean enable) setClipping() } and
	 * {@link WPainter#setClipPath(WPainterPath clipPath) setClipPath() }).
	 * <p>
	 * 
	 * @see WPainter#restore()
	 */
	public void save() {
		this.stateStack_.add(this.stateStack_.get(this.stateStack_.size() - 1)
				.clone());
	}

	/**
	 * Get the last save state.
	 * <p>
	 * Pops the last saved state from the state stack.
	 * <p>
	 * 
	 * @see WPainter#save()
	 */
	public void restore() {
		if (this.stateStack_.size() > 1) {
			EnumSet<WPaintDevice.ChangeFlag> flags = EnumSet
					.noneOf(WPaintDevice.ChangeFlag.class);
			WPainter.State last = this.stateStack_
					.get(this.stateStack_.size() - 1);
			WPainter.State next = this.stateStack_
					.get(this.stateStack_.size() - 2);
			if (!last.worldTransform_.equals(next.worldTransform_)) {
				flags.add(WPaintDevice.ChangeFlag.Transform);
			}
			if (!last.currentBrush_.equals(next.currentBrush_)) {
				flags.add(WPaintDevice.ChangeFlag.Brush);
			}
			if (!last.currentFont_.equals(next.currentFont_)) {
				flags.add(WPaintDevice.ChangeFlag.Font);
			}
			if (!last.currentPen_.equals(next.currentPen_)) {
				flags.add(WPaintDevice.ChangeFlag.Pen);
			}
			if (last.renderHints_ != next.renderHints_) {
				flags.add(WPaintDevice.ChangeFlag.Hints);
			}
			if (!last.clipPath_.equals(next.clipPath_)) {
				flags.add(WPaintDevice.ChangeFlag.Clipping);
			}
			if (last.clipping_ != next.clipping_) {
				flags.add(WPaintDevice.ChangeFlag.Clipping);
			}
			this.stateStack_.remove(0 + this.stateStack_.size() - 1);
			if (!flags.isEmpty() && this.device_ != null) {
				this.device_.setChanged(flags);
			}
		}
	}

	/**
	 * Set the viewport.
	 * <p>
	 * Selects the part of the device that will correspond to the logical
	 * coordinate system.
	 * <p>
	 * By default, the viewport spans the entire device: it is the rectangle (0,
	 * 0) to (device-&gt;width(), device-&gt;height()). The window defines how
	 * the viewport is mapped to logical coordinates.
	 * <p>
	 * 
	 * @see WPainter#getViewPort()
	 * @see WPainter#setWindow(WRectF window)
	 */
	public void setViewPort(WRectF viewPort) {
		this.viewPort_.assign(viewPort);
		this.recalculateViewTransform();
	}

	/**
	 * Set the viewport.
	 * <p>
	 * This is an overloaded method for convenience.
	 * <p>
	 * 
	 * @see WPainter#setViewPort(WRectF viewPort)
	 */
	public void setViewPort(double x, double y, double width, double height) {
		this.setViewPort(new WRectF(x, y, width, height));
	}

	/**
	 * Returns the viewport.
	 * <p>
	 * 
	 * @see WPainter#setViewPort(WRectF viewPort)
	 */
	public WRectF getViewPort() {
		return this.viewPort_;
	}

	/**
	 * Set the window.
	 * <p>
	 * Defines the viewport rectangle in logical coordinates, and thus how
	 * logical coordinates map onto the viewPort.
	 * <p>
	 * By default, is (0, 0) to (device-&gt;width(), device-&gt;height()). Thus,
	 * the default window and viewport leave logical coordinates identical to
	 * device coordinates.
	 * <p>
	 * 
	 * @see WPainter#getWindow()
	 * @see WPainter#setViewPort(WRectF viewPort)
	 */
	public void setWindow(WRectF window) {
		this.window_.assign(window);
		this.recalculateViewTransform();
	}

	/**
	 * Set the window.
	 * <p>
	 * This is an overloaded method for convenience.
	 * <p>
	 * 
	 * @see WPainter#setWindow(WRectF window)
	 */
	public void setWindow(double x, double y, double width, double height) {
		this.setWindow(new WRectF(x, y, width, height));
	}

	/**
	 * Returns the current window.
	 * <p>
	 * 
	 * @see WPainter#setViewPort(WRectF viewPort)
	 */
	public WRectF getWindow() {
		return this.window_;
	}

	/**
	 * Returns the combined transformation matrix.
	 * <p>
	 * Returns the transformation matrix that maps coordinates to device
	 * coordinates. It is the combination of the current world transformation
	 * (which defines the transformation within the logical coordinate system)
	 * and the window/viewport transformation (which transforms logical
	 * coordinates to device coordinates).
	 * <p>
	 * 
	 * @see WPainter#setWorldTransform(WTransform matrix, boolean combine)
	 * @see WPainter#setViewPort(WRectF viewPort)
	 * @see WPainter#setWindow(WRectF window)
	 */
	public WTransform getCombinedTransform() {
		return WTransform.multiply(this.viewTransform_,
				this.getS().worldTransform_);
	}

	public WTransform getClipPathTransform() {
		return this.getS().clipPathTransform_;
	}

	public WLength normalizedPenWidth(WLength penWidth, boolean correctCosmetic) {
		double w = penWidth.getValue();
		if (w == 0 && correctCosmetic) {
			WTransform t = this.getCombinedTransform();
			if (!t.isIdentity()) {
				WTransform.TRSRDecomposition d = new WTransform.TRSRDecomposition();
				t.decomposeTranslateRotateScaleRotate(d);
				w = 2.0 / (Math.abs(d.sx) + Math.abs(d.sy));
			} else {
				w = 1.0;
			}
			return new WLength(w, WLength.Unit.Pixel);
		} else {
			if (w != 0 && !correctCosmetic) {
				WTransform t = this.getCombinedTransform();
				if (!t.isIdentity()) {
					WTransform.TRSRDecomposition d = new WTransform.TRSRDecomposition();
					t.decomposeTranslateRotateScaleRotate(d);
					w *= (Math.abs(d.sx) + Math.abs(d.sy)) / 2.0;
				}
				return new WLength(w, WLength.Unit.Pixel);
			} else {
				return penWidth;
			}
		}
	}

	private WPaintDevice device_;
	private WRectF viewPort_;
	private WRectF window_;
	private WTransform viewTransform_;

	private static class State {
		public WTransform worldTransform_;
		public WBrush currentBrush_;
		public WFont currentFont_;
		public WPen currentPen_;
		public int renderHints_;
		public WPainterPath clipPath_;
		public WTransform clipPathTransform_;
		public boolean clipping_;

		public State() {
			this.worldTransform_ = new WTransform();
			this.currentBrush_ = new WBrush();
			this.currentFont_ = new WFont();
			this.currentPen_ = new WPen();
			this.renderHints_ = 0;
			this.clipPath_ = new WPainterPath();
			this.clipPathTransform_ = new WTransform();
			this.clipping_ = false;
			this.currentFont_.setFamily(WFont.GenericFamily.SansSerif);
			this.currentFont_.setSize(WFont.Size.FixedSize, new WLength(10,
					WLength.Unit.Point));
		}

		public WPainter.State clone() {
			WPainter.State result = new WPainter.State();
			result.worldTransform_.assign(this.worldTransform_);
			result.currentBrush_ = this.currentBrush_;
			result.currentFont_ = this.currentFont_;
			result.currentPen_ = this.currentPen_;
			result.renderHints_ = this.renderHints_;
			result.clipPath_.assign(this.clipPath_);
			result.clipPathTransform_.assign(this.clipPathTransform_);
			result.clipping_ = this.clipping_;
			return result;
		}
	}

	private List<WPainter.State> stateStack_;

	private WPainter.State getS() {
		return this.stateStack_.get(this.stateStack_.size() - 1);
	}

	private void recalculateViewTransform() {
		this.viewTransform_.assign(new WTransform());
		double scaleX = this.viewPort_.getWidth() / this.window_.getWidth();
		double scaleY = this.viewPort_.getHeight() / this.window_.getHeight();
		this.viewTransform_.translate(this.viewPort_.getX()
				- this.window_.getX() * scaleX, this.viewPort_.getY()
				- this.window_.getY() * scaleY);
		this.viewTransform_.scale(scaleX, scaleY);
		this.device_.setChanged(EnumSet.of(WPaintDevice.ChangeFlag.Transform));
	}
}
