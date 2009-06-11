package eu.webtoolkit.jwt;

import java.util.EnumSet;

/**
 * The abstract base class for a paint device.
 * 
 * 
 * A WPaintDevice is a device on which may be painted using a {@link WPainter}.
 * You should never paint directly on a paint device.
 * <p>
 * The device defines the size of the drawing area, using
 * {@link WPaintDevice#getWidth()} and {@link WPaintDevice#getHeight()}. These
 * dimensions must be defined in pixel units. In the future, additional
 * information will be included to convert these pixel units to lengths (using
 * DPI information).
 * <p>
 * You should reimplement this class if you wish to extend the Wt paint system
 * to paint on other devices than the ones provided by the library.
 * <p>
 * <i>Note: this interface is subject to changes to increase optimization
 * possibilities for the painting using different devices.</i>
 * <p>
 * 
 * @see WPainter
 */
public interface WPaintDevice {
	/**
	 * Enumeration to communicate painter state changes.
	 * 
	 * @see WPaintDevice#setChanged(EnumSet flags)
	 */
	public enum ChangeFlag {
		/**
		 * Properties of the pen have changed.
		 */
		Pen,
		/**
		 * Properties of the brush have changed.
		 */
		Brush,
		/**
		 * Properties of the font have changed.
		 */
		Font,
		/**
		 * Some render hints have changed.
		 */
		Hints,
		/**
		 * The transformation has changed.
		 */
		Transform,
		/**
		 * The clipping has changed.
		 */
		Clipping;

		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Returns the device width.
	 * 
	 * The device width, in pixels, establishes the width of the device
	 * coordinate system.
	 */
	public WLength getWidth();

	/**
	 * Returns the device height.
	 * 
	 * The device height, in pixels, establishes the height of the device
	 * coordinate system.
	 */
	public WLength getHeight();

	/**
	 * Indicate changes in painter state.
	 * 
	 * The <i>flags</i> argument is the logical OR of one or more change flags.
	 * <p>
	 * 
	 * @see WPaintDevice.ChangeFlag
	 */
	public void setChanged(EnumSet<WPaintDevice.ChangeFlag> flags);

	/**
	 * Draw an arc.
	 * 
	 * The arc is defined as in
	 * {@link WPainter#drawArc(WRectF rectangle, int startAngle, int spanAngle)}
	 * (const {@link WRectF}&amp;, startAngle, spanAngle), but the angle is
	 * expressed in degrees.
	 * <p>
	 * The arc must be stroked, filled, and transformed using the current
	 * painter settings.
	 */
	public void drawArc(WRectF rect, double startAngle, double spanAngle);

	/**
	 * Draw an image.
	 * 
	 * Draws <i>sourceRect</i> from the image with URL <i>imageUri</i> and
	 * original dimensions <i>imgWidth</i> and <i>imgHeight</i> to the location,
	 * into the rectangle defined by <i>rect</i>.
	 * <p>
	 * The image is transformed using the current painter settings.
	 */
	public void drawImage(WRectF rect, String imageUri, int imgWidth,
			int imgHeight, WRectF sourceRect);

	/**
	 * Draw a line.
	 * 
	 * The line must be stroked and transformed using the current painter
	 * settings.
	 */
	public void drawLine(double x1, double y1, double x2, double y2);

	/**
	 * Draw a path.
	 * 
	 * The path must be stroked, filled, and transformed using the current
	 * painter settings.
	 */
	public void drawPath(WPainterPath path);

	/**
	 * Draw text.
	 * 
	 * The text must be rendered, stroked and transformed using the current
	 * painter settings.
	 */
	public void drawText(WRectF rect, EnumSet<AlignmentFlag> flags,
			CharSequence text);

	/**
	 * Initialize the device for painting.
	 * 
	 * This method is called when a {@link WPainter} starts painting.
	 * <p>
	 * 
	 * @see WPainter#begin(WPaintDevice device)
	 * @see WPaintDevice#getPainter()
	 */
	public void init();

	/**
	 * Finishes painting on the device.
	 * 
	 * This method is called when a {@link WPainter} stopped painting.
	 * <p>
	 * 
	 * @see WPainter#isEnd()
	 */
	public void done();

	/**
	 * Returns whether painting is active.
	 * 
	 * @see WPaintDevice#init()
	 * @see WPaintDevice#getPainter()
	 */
	public boolean isPaintActive();

	/**
	 * Returns the paint flags.
	 */
	public EnumSet<PaintFlag> getPaintFlags();

	/**
	 * Returns the painter that is currently painting on the device.
	 * 
	 * @see WPaintDevice#init()
	 */
	WPainter getPainter();

	/**
	 * Sets the painter.
	 */
	void setPainter(WPainter painter);

	/**
	 * Set paint flags.
	 */
	public void setPaintFlags(EnumSet<PaintFlag> paintFlags);
}
