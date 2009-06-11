package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.List;

/**
 * A path defining a shape.
 * 
 * 
 * A painter path represents a (complex) path that may be composed of lines,
 * arcs and bezier curve segments, and painted onto a paint device using
 * {@link WPainter#drawPath(WPainterPath path)}.
 * <p>
 * The path that is composed in a painter path may consist of multiple closed
 * sub-paths. Only the last sub-path can be left open.
 * <p>
 * To compose a path, this class maintains a current position, which is the
 * starting point for the next drawing operation. An operation may draw a line
 * (see {@link WPainterPath#lineTo(WPointF point)}), arc (see
 * {@link WPainterPath#arcTo(double cx, double cy, double radius, double startAngle, double sweepLength)}
 * ), or bezier curve (see
 * {@link WPainterPath#quadTo(WPointF c, WPointF endPoint)} and
 * {@link WPainterPath#cubicTo(WPointF c1, WPointF c2, WPointF endPoint)}) from
 * the current position to a new position. A new sub path may be started by
 * moving the current position to a new location (see
 * {@link WPainterPath#moveTo(WPointF point)}), which automatically closes the
 * previous sub path.
 * <p>
 * When sub paths overlap, the result is undefined (it is dependent on the
 * underlying painting device).
 * <p>
 * Usage example:
 * <p>
 * <code>
 WPainter painter = new WPainter(); <br> 
	  <br> 
 WPainterPath path = new WPainterPath(new WPointF(10, 10)); <br> 
 path.lineTo(10, 20); <br> 
 path.lineTo(30, 20); <br> 
 path.closeSubPath(); <br> 
		  <br> 
 painter.setPen(new WPen(WColor.red)); <br> 
 painter.setBrush(new WBrush(WColor.blue)); <br> 
 painter.drawPath(path);
</code>
 * <p>
 * 
 * @see WPainter#drawPath(WPainterPath path)
 */
public class WPainterPath {
	/**
	 * Default constructor.
	 * 
	 * Creates an empty path, and sets the current position to (0, 0).
	 */
	public WPainterPath() {
		this.isRect_ = false;
		this.segments_ = new ArrayList<WPainterPath.Segment>();
	}

	/**
	 * Construct a new path, and set the initial position.
	 * 
	 * Creates an empty path, and sets the current position to
	 * <i>startPoint</i>.
	 */
	public WPainterPath(WPointF startPoint) {
		this.isRect_ = false;
		this.segments_ = new ArrayList<WPainterPath.Segment>();
		this.moveTo(startPoint);
	}

	/**
	 * Copy constructor.
	 */
	public WPainterPath(WPainterPath path) {
		this.isRect_ = path.isRect_;
		this.segments_ = path.segments_;
	}

	/**
	 * Assignment operator.
	 */
	public WPainterPath assign(WPainterPath path) {
		this.segments_ = path.segments_;
		this.isRect_ = path.isRect_;
		return this;
	}

	/**
	 * Return the current position.
	 * 
	 * Returns the current position, which is the end point of the last move or
	 * draw operation, and which well be the start point of the next draw
	 * operation.
	 */
	public WPointF getCurrentPosition() {
		return this.getPositionAtSegment(this.segments_.size());
	}

	/**
	 * Returns whether the path is empty.
	 * 
	 * Returns true if the path contains no drawing operations.
	 */
	public boolean isEmpty() {
		return this.segments_.isEmpty();
	}

	/**
	 * Comparison operator.
	 * 
	 * Returns true if the paths are exactly the same.
	 */
	public boolean equals(WPainterPath path) {
		if (this.segments_.size() != path.segments_.size()) {
			return false;
		}
		for (int i = 0; i < this.segments_.size(); ++i) {
			if (!this.segments_.get(i).equals(path.segments_.get(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Close the last sub path.
	 * 
	 * Draws a line from the current position to the start position of the last
	 * sub path (which is the end point of the last move operation), and sets
	 * the current position to (0, 0).
	 */
	public void closeSubPath() {
		this.moveTo(0, 0);
	}

	/**
	 * Moves the current position to a new location.
	 * 
	 * Moves the current position to a new point, implicitly closing the last
	 * sub path.
	 * <p>
	 * 
	 * @see WPainterPath#closeSubPath()
	 * @see WPainterPath#moveTo(double x, double y)
	 */
	public void moveTo(WPointF point) {
		this.moveTo(point.getX(), point.getY());
	}

	/**
	 * Moves the current position to a new location.
	 * 
	 * Moves the current position to a new point, implicitly closing the last
	 * sub path.
	 * <p>
	 * 
	 * @see WPainterPath#closeSubPath()
	 * @see WPainterPath#moveTo(double x, double y)
	 */
	public void moveTo(double x, double y) {
		if (!this.segments_.isEmpty()
				&& this.segments_.get(this.segments_.size() - 1).getType() != WPainterPath.Segment.Type.MoveTo) {
			WPointF startP = this.getSubPathStart();
			WPointF currentP = this.getCurrentPosition();
			if (!startP.equals(currentP)) {
				this.lineTo(startP.getX(), startP.getY());
			}
		}
		this.segments_.add(new WPainterPath.Segment(x, y,
				WPainterPath.Segment.Type.MoveTo));
	}

	/**
	 * Draws a straight line.
	 * 
	 * Draws a straight line from the current position to <i>point</i>, which
	 * becomes the new current position.
	 * <p>
	 * 
	 * @see WPainterPath#lineTo(double x, double y)
	 */
	public void lineTo(WPointF point) {
		this.lineTo(point.getX(), point.getY());
	}

	/**
	 * Draws a straight line.
	 * 
	 * Draws a straight line from the current position to (<i>x</i>, <i>y</i>),
	 * which becomes the new current position.
	 * <p>
	 * 
	 * @see WPainterPath#lineTo(WPointF point)
	 */
	public void lineTo(double x, double y) {
		this.segments_.add(new WPainterPath.Segment(x, y,
				WPainterPath.Segment.Type.LineTo));
	}

	/**
	 * Draws a cubic bezier curve.
	 * 
	 * Draws a cubic bezier curve from the current position to <i>endPoint</i>,
	 * which becomes the new current position. The bezier curve uses the two
	 * control points <i>c1</i> and <i>c2</i>.
	 * <p>
	 * 
	 * @see WPainterPath#cubicTo(double c1x, double c1y, double c2x, double c2y,
	 *      double endPointx, double endPointy)
	 */
	public void cubicTo(WPointF c1, WPointF c2, WPointF endPoint) {
		this.cubicTo(c1.getX(), c1.getY(), c2.getX(), c2.getY(), endPoint
				.getX(), endPoint.getY());
	}

	/**
	 * Draws a cubic bezier curve.
	 * 
	 * This is an overloaded method provided for convenience.
	 * <p>
	 * 
	 * @see WPainterPath#cubicTo(WPointF c1, WPointF c2, WPointF endPoint)
	 */
	public void cubicTo(double c1x, double c1y, double c2x, double c2y,
			double endPointx, double endPointy) {
		this.segments_.add(new WPainterPath.Segment(c1x, c1y,
				WPainterPath.Segment.Type.CubicC1));
		this.segments_.add(new WPainterPath.Segment(c2x, c2y,
				WPainterPath.Segment.Type.CubicC2));
		this.segments_.add(new WPainterPath.Segment(endPointx, endPointy,
				WPainterPath.Segment.Type.CubicEnd));
	}

	/**
	 * Draws an arc.
	 * 
	 * Draws an arc which is a segment of a circle. The circle is defined with
	 * center (<i>cx</i>, <i>cy</i>) and <i>radius</i>. The segment starts at
	 * <i>startAngle</i>, and spans an angle given by <i>spanAngle</i>. These
	 * angles are expressed in degrees, and are measured counter-clockwise
	 * starting from the 3 o&apos;clock position.
	 * <p>
	 * Implicitly draws a line from the current position to the start of the
	 * arc, if the current position is different from the start.
	 * <p>
	 * 
	 * @see WPainterPath#arcMoveTo(double cx, double cy, double radius, double
	 *      angle)
	 */
	public void arcTo(double cx, double cy, double radius, double startAngle,
			double sweepLength) {
		this.arcTo(cx - radius, cy - radius, radius * 2, radius * 2,
				startAngle, sweepLength);
	}

	/**
	 * Move to a point on an arc.
	 * 
	 * Moves to a point on a circle. The circle is defined with center
	 * (<i>cx</i>, <i>cy</i>) and <i>radius</i>, and the point is at
	 * <i>angle</i> degrees measured counter-clockwise starting from the 3
	 * o&apos;clock position.
	 * <p>
	 * 
	 * @see WPainterPath#arcTo(double cx, double cy, double radius, double
	 *      startAngle, double sweepLength)
	 */
	public void arcMoveTo(double cx, double cy, double radius, double angle) {
		this.moveTo(getArcPosition(cx, cy, radius, radius, angle));
	}

	/**
	 * Move to a point on an arc.
	 * 
	 * Moves to a point on an ellipse. The ellipse fits in the rectangle defined
	 * by top left position (<i>x</i>, <i>y</i>), and size <i>width</i> x
	 * <i>height</i>, and the point is at <i>angle</i> degrees measured
	 * counter-clockwise starting from the 3 o&apos;clock position.
	 * <p>
	 * 
	 * @see WPainterPath#arcTo(double cx, double cy, double radius, double
	 *      startAngle, double sweepLength)
	 */
	public void arcMoveTo(double x, double y, double width, double height,
			double angle) {
		this.moveTo(getArcPosition(x + width / 2, y + height / 2, width / 2,
				height / 2, angle));
	}

	/**
	 * Draw a quadratic bezier curve.
	 * 
	 * Draws a quadratic bezier curve from the current position to
	 * <i>endPoint</i>, which becomes the new current position. The bezier curve
	 * uses the single control point <i>c</i>.
	 * <p>
	 * 
	 * @see WPainterPath#quadTo(double cx, double cy, double endPointX, double
	 *      endPointY)
	 */
	public void quadTo(WPointF c, WPointF endPoint) {
		this.quadTo(c.getX(), c.getY(), endPoint.getX(), endPoint.getY());
	}

	/**
	 * Draw a quadratic bezier curve.
	 * 
	 * This is an overloaded method provided for convenience.
	 * <p>
	 * 
	 * @see WPainterPath#quadTo(WPointF c, WPointF endPoint)
	 */
	public void quadTo(double cx, double cy, double endPointX, double endPointY) {
		this.segments_.add(new WPainterPath.Segment(cx, cy,
				WPainterPath.Segment.Type.QuadC));
		this.segments_.add(new WPainterPath.Segment(endPointX, endPointY,
				WPainterPath.Segment.Type.QuadEnd));
	}

	/**
	 * Draw an ellipse.
	 * 
	 * This method closes the current sub path, and adds an ellipse that is
	 * bounded by the rectangle <i>boundingRectangle</i>.
	 * <p>
	 * <i>Note: some renderers only support circles (width == height)</i>
	 * <p>
	 * 
	 * @see WPainterPath#addEllipse(double x, double y, double width, double
	 *      height)
	 * @see WPainterPath#arcTo(double cx, double cy, double radius, double
	 *      startAngle, double sweepLength)
	 */
	public void addEllipse(WRectF rect) {
		this.addEllipse(rect.getX(), rect.getY(), rect.getWidth(), rect
				.getHeight());
	}

	/**
	 * Draw an ellipse.
	 * 
	 * This method closes the current sub path, and adds an ellipse that is
	 * bounded by the rectangle defined by top left position (<i>x</i>,
	 * <i>y</i>), and size <i>width</i> x <i>height</i>.
	 * <p>
	 * <i>Note: some renderers only support circles (width == height)</i>
	 * <p>
	 * 
	 * @see WPainterPath#addEllipse(WRectF rect)
	 * @see WPainterPath#arcTo(double cx, double cy, double radius, double
	 *      startAngle, double sweepLength)
	 */
	public void addEllipse(double x, double y, double width, double height) {
		this.moveTo(x + width, y + height / 2);
		this.arcTo(x, y, width, height, 0, 360);
	}

	/**
	 * Draw a rectangle.
	 * 
	 * This method closes the current sub path, and adds a rectangle that is
	 * defined by <i>rectangle</i>.
	 * <p>
	 * 
	 * @see WPainterPath#addRect(double x, double y, double width, double
	 *      height)
	 */
	public void addRect(WRectF rectangle) {
		this.addRect(rectangle.getX(), rectangle.getY(), rectangle.getWidth(),
				rectangle.getHeight());
	}

	/**
	 * Draw a rectangle.
	 * 
	 * This method closes the current sub path, and adds a rectangle that is
	 * defined by top left position (<i>x</i>, <i>y</i>), and size <i>width</i>
	 * x <i>height</i>.
	 * <p>
	 * 
	 * @see WPainterPath#addRect(WRectF rectangle)
	 */
	public void addRect(double x, double y, double width, double height) {
		if (this.isEmpty()) {
			this.isRect_ = true;
		}
		this.moveTo(x, y);
		this.lineTo(x + width, y);
		this.lineTo(x + width, y + height);
		this.lineTo(x, y + height);
		this.lineTo(x, y);
	}

	/**
	 * Add a path.
	 * 
	 * Adds an entire <i>path</i> to the current path. If the path&apos;s begin
	 * position is different from the current position, the last sub path is
	 * first closed, otherwise the last sub path is extended with the
	 * path&apos;s first sub path.
	 * <p>
	 * 
	 * @see WPainterPath#connectPath(WPainterPath path)
	 */
	public void addPath(WPainterPath path) {
		if (!this.getCurrentPosition().equals(path.getBeginPosition())) {
			this.moveTo(path.getBeginPosition());
		}
		this.segments_.addAll(path.segments_);
	}

	/**
	 * Add a path, connecting.
	 * 
	 * Adds an entire <i>path</i> to the current path. If the path&apos;s begin
	 * position is different from the current position, the last sub path is
	 * first closed, otherwise the last sub path is extended with the
	 * path&apos;s first sub path.
	 * <p>
	 * 
	 * @see WPainterPath#connectPath(WPainterPath path)
	 */
	public void connectPath(WPainterPath path) {
		if (!this.getCurrentPosition().equals(path.getBeginPosition())) {
			this.lineTo(path.getBeginPosition());
		}
		this.addPath(path);
	}

	public static class Segment {
		public enum Type {
			MoveTo, LineTo, CubicC1, CubicC2, CubicEnd, QuadC, QuadEnd, ArcC, ArcR, ArcAngleSweep;

			public int getValue() {
				return ordinal();
			}
		}

		public double getX() {
			return this.x_;
		}

		public double getY() {
			return this.y_;
		}

		public WPainterPath.Segment.Type getType() {
			return this.type_;
		}

		public boolean equals(WPainterPath.Segment other) {
			return this.type_ == other.type_ && this.x_ == other.x_
					&& this.y_ == other.y_;
		}

		private Segment(double x, double y, WPainterPath.Segment.Type type) {
			this.x_ = x;
			this.y_ = y;
			this.type_ = type;
		}

		private double x_;
		private double y_;
		private WPainterPath.Segment.Type type_;
	}

	public List<WPainterPath.Segment> getSegments() {
		return this.segments_;
	}

	public WPointF getPositionAtSegment(int index) {
		if (index > 0) {
			WPainterPath.Segment s = this.segments_.get(index - 1);
			switch (s.getType()) {
			case MoveTo:
			case LineTo:
			case CubicEnd:
			case QuadEnd:
				return new WPointF(s.getX(), s.getY());
			case ArcAngleSweep: {
				int i = this.segments_.size() - 3;
				double cx = this.segments_.get(i).getX();
				double cy = this.segments_.get(i).getY();
				double rx = this.segments_.get(i + 1).getX();
				double ry = this.segments_.get(i + 1).getY();
				double theta1 = this.segments_.get(i + 2).getX();
				double deltaTheta = this.segments_.get(i + 2).getY();
				return getArcPosition(cx, cy, rx, ry, degreesToRadians(theta1
						+ deltaTheta));
			}
			default:
				assert false;
			}
		}
		return new WPointF(0, 0);
	}

	public boolean asRect(WRectF result) {
		if (this.isRect_) {
			if (this.segments_.size() == 4) {
				result = new WRectF(0, 0, this.segments_.get(0).getX(),
						this.segments_.get(1).getY());
				return true;
			} else {
				if (this.segments_.size() == 5
						&& this.segments_.get(0).getType() == WPainterPath.Segment.Type.MoveTo) {
					result = new WRectF(this.segments_.get(0).getX(),
							this.segments_.get(0).getY(), this.segments_.get(1)
									.getX()
									- this.segments_.get(0).getX(),
							this.segments_.get(2).getY()
									- this.segments_.get(0).getY());
					return true;
				} else {
					return false;
				}
			}
		} else {
			return false;
		}
	}

	/**
	 * Returns the bounding box of the control points.
	 * 
	 * Returns the bounding box of all control points. This is guaranteed to be
	 * a superset of the actual bounding box.
	 */
	public WRectF getControlPointRect() {
		if (this.isEmpty()) {
			return new WRectF();
		} else {
			double minX;
			double minY;
			double maxX;
			double maxY;
			minX = minY = Double.MAX_VALUE;
			maxX = maxY = Double.MIN_VALUE;
			for (int i = 0; i < this.segments_.size(); ++i) {
				WPainterPath.Segment s = this.segments_.get(i);
				switch (s.getType()) {
				case MoveTo:
				case LineTo:
				case CubicC1:
				case CubicC2:
				case CubicEnd:
				case QuadC:
				case QuadEnd: {
					minX = Math.min(s.getX(), minX);
					minY = Math.min(s.getY(), minY);
					maxX = Math.max(s.getX(), maxX);
					maxY = Math.max(s.getY(), maxY);
					break;
				}
				case ArcC: {
					WPainterPath.Segment s2 = this.segments_.get(i + 1);
					WPointF tl = new WPointF(s.getX() - s2.getX(), s.getY()
							- s2.getY());
					minX = Math.min(tl.getX(), minX);
					minY = Math.min(tl.getY(), minY);
					WPointF br = new WPointF(s.getX() + s2.getX(), s.getY()
							+ s2.getY());
					maxX = Math.max(br.getX(), maxX);
					maxY = Math.max(br.getY(), maxY);
					i += 2;
					break;
				}
				default:
					assert false;
				}
			}
			return new WRectF(minX, minY, maxX - minX, maxY - minY);
		}
	}

	private boolean isRect_;
	private List<WPainterPath.Segment> segments_;

	private WPointF getSubPathStart() {
		for (int i = this.segments_.size() - 1; i >= 0; --i) {
			if (this.segments_.get(i).getType() == WPainterPath.Segment.Type.MoveTo) {
				return new WPointF(this.segments_.get(i).getX(), this.segments_
						.get(i).getY());
			}
		}
		return new WPointF(0, 0);
	}

	private WPointF getBeginPosition() {
		WPointF result = new WPointF(0, 0);
		for (int i = 0; i < this.segments_.size()
				&& this.segments_.get(i).getType() == WPainterPath.Segment.Type.MoveTo; ++i) {
			result = new WPointF(this.segments_.get(i).getX(), this.segments_
					.get(i).getY());
		}
		return result;
	}

	private static WPointF getArcPosition(double cx, double cy, double rx,
			double ry, double angle) {
		double a = -degreesToRadians(angle);
		return new WPointF(cx + rx * Math.cos(a), cy + ry * Math.sin(a));
	}

	void arcTo(double x, double y, double width, double height,
			double startAngle, double sweepLength) {
		this.segments_.add(new WPainterPath.Segment(x + width / 2, y + height
				/ 2, WPainterPath.Segment.Type.ArcC));
		this.segments_.add(new WPainterPath.Segment(width / 2, height / 2,
				WPainterPath.Segment.Type.ArcR));
		this.segments_.add(new WPainterPath.Segment(startAngle, sweepLength,
				WPainterPath.Segment.Type.ArcAngleSweep));
	}

	static double degreesToRadians(double r) {
		return r / 180. * 3.14159265358979323846;
	}
}
