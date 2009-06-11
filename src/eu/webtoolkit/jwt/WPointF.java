package eu.webtoolkit.jwt;


/**
 * Utility class that defines a 2D point.
 */
public class WPointF {
	/**
	 * Default constructor.
	 * 
	 * Constructs a point (<i>x=0</i>, <i>y=0</i>).
	 */
	public WPointF() {
		this.x_ = 0;
		this.y_ = 0;
	}

	/**
	 * Construct a point.
	 * 
	 * Constructs a point (<i>x</i>, <i>y</i>).
	 */
	public WPointF(double x, double y) {
		this.x_ = x;
		this.y_ = y;
	}

	/**
	 * Copy constructor.
	 */
	public WPointF(WPointF other) {
		this.x_ = other.getX();
		this.y_ = other.getY();
	}

	/**
	 * Changes the X coordinate.
	 */
	public void setX(double x) {
		this.x_ = x;
	}

	/**
	 * Changes the Y coordinate.
	 */
	public void setY(double y) {
		this.y_ = y;
	}

	/**
	 * Returns the X coordinate.
	 */
	public double getX() {
		return this.x_;
	}

	/**
	 * Returns the Y coordinate.
	 */
	public double getY() {
		return this.y_;
	}

	/**
	 * Comparison operator.
	 */
	public boolean equals(WPointF other) {
		return this.x_ == other.x_ && this.y_ == other.y_;
	}

	public WPointF add(WPointF other) {
		this.x_ += other.x_;
		this.y_ += other.y_;
		return this;
	}

	private double x_;
	private double y_;
}
