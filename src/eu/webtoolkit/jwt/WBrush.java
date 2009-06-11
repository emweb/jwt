package eu.webtoolkit.jwt;


/**
 * Class that defines the style for filling areas.
 * 
 * 
 * A brush defines the properties of how areas (the interior of shapes) are
 * filled. A brush is defined using a color and a fill type (currently only
 * solid fills are supported).
 * <p>
 * 
 * @see WPainter#setBrush(WBrush b)
 * @see WPen
 */
public class WBrush {
	/**
	 * Default constructor.
	 * 
	 * Constructs a brush with a {@link WBrushStyle#NoBrush NoBrush} fill style.
	 */
	public WBrush() {
		this.style_ = WBrushStyle.NoBrush;
		this.color_ = WColor.black;
	}

	/**
	 * Construct a brush with a particular style.
	 * 
	 * Constructs a black brush with the indicated <i>style</i>.
	 */
	public WBrush(WBrushStyle style) {
		this.style_ = style;
		this.color_ = WColor.black;
	}

	/**
	 * Construct a solid brush of a particular color.
	 * 
	 * Constructs a solid brush with the indicated <i>color</i>.
	 */
	public WBrush(WColor color) {
		this.style_ = WBrushStyle.SolidPattern;
		this.color_ = color;
	}

	public WBrush clone() {
		WBrush result = new WBrush();
		result.color_ = this.color_;
		result.style_ = this.style_;
		return result;
	}

	/**
	 * Comparison operator.
	 * 
	 * Returns true if the brushes are exactly the same.
	 */
	public boolean equals(WBrush other) {
		return this.color_.equals(other.color_) && this.style_ == other.style_;
	}

	/**
	 * Change the brush style.
	 * 
	 * @see WBrush#getStyle()
	 */
	public void setStyle(WBrushStyle style) {
		this.style_ = style;
	}

	/**
	 * Returns the fill style.
	 * 
	 * @see WBrush#setStyle(WBrushStyle style)
	 */
	public WBrushStyle getStyle() {
		return this.style_;
	}

	/**
	 * Change the brush color.
	 * 
	 * @see WBrush#getColor()
	 */
	public void setColor(WColor color) {
		this.color_ = color;
	}

	/**
	 * Returns the brush color.
	 * 
	 * @see WBrush#getColor()
	 */
	public WColor getColor() {
		return this.color_;
	}

	private WBrushStyle style_;
	private WColor color_;
}
