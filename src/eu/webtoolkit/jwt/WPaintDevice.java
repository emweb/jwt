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
 * The abstract base class for a paint device.
 * <p>
 * 
 * A WPaintDevice is a device on which may be painted using a {@link WPainter}.
 * You should never paint directly on a paint device.
 * <p>
 * The device defines the size of the drawing area, using
 * {@link WPaintDevice#getWidth() getWidth()} and
 * {@link WPaintDevice#getHeight() getHeight()}. These dimensions must be
 * defined in pixel units. In the future, additional information will be
 * included to convert these pixel units to lengths (using DPI information).
 * <p>
 * You should reimplement this class if you wish to extend the JWt paint system
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
	 * <p>
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
		Clipping,
		/**
		 * Properties of the shadow have changed.
		 */
		Shadow;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Enumeration to indicate paint device features.
	 * <p>
	 * 
	 * @see WPaintDevice#getFeatures()
	 */
	public enum FeatureFlag {
		/**
		 * Implements
		 * {@link WPaintDevice#drawText(WRectF rect, EnumSet alignmentFlags, TextFlag textFlag, CharSequence text)
		 * drawText()} with {@link }.
		 */
		CanWordWrap,
		/**
		 * Implements {@link WPaintDevice#getFontMetrics() getFontMetrics()} and
		 * {@link WPaintDevice#measureText(CharSequence text, double maxWidth, boolean wordWrap)
		 * measureText()}.
		 */
		HasFontMetrics;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Returns device features.
	 */
	public EnumSet<WPaintDevice.FeatureFlag> getFeatures();

	/**
	 * Returns the device width.
	 * <p>
	 * The device width, in pixels, establishes the width of the device
	 * coordinate system.
	 */
	public WLength getWidth();

	/**
	 * Returns the device height.
	 * <p>
	 * The device height, in pixels, establishes the height of the device
	 * coordinate system.
	 */
	public WLength getHeight();

	/**
	 * Indicates changes in painter state.
	 * <p>
	 * The <code>flags</code> argument is the logical OR of one or more change
	 * flags.
	 * <p>
	 * 
	 * @see WPaintDevice.ChangeFlag
	 */
	public void setChanged(EnumSet<WPaintDevice.ChangeFlag> flags);

	/**
	 * Indicates changes in painter state.
	 * <p>
	 * Calls {@link #setChanged(EnumSet flags) setChanged(EnumSet.of(flag,
	 * flags))}
	 */
	public void setChanged(WPaintDevice.ChangeFlag flag,
			WPaintDevice.ChangeFlag... flags);

	/**
	 * Draws an arc.
	 * <p>
	 * The arc is defined as in
	 * {@link WPainter#drawArc(WRectF rectangle, int startAngle, int spanAngle)
	 * WPainter#drawArc()}
	 * <p>
	 * The arc must be stroked, filled, and transformed using the current
	 * painter settings.
	 */
	public void drawArc(WRectF rect, double startAngle, double spanAngle);

	/**
	 * Draws an image.
	 * <p>
	 * Draws <i>sourceRect</i> from the image with URL <code>imageUri</code> and
	 * original dimensions <i>imgWidth</i> and <code>imgHeight</code> to the
	 * location, into the rectangle defined by <code>rect</code>.
	 * <p>
	 * The image is transformed using the current painter settings.
	 */
	public void drawImage(WRectF rect, String imageUri, int imgWidth,
			int imgHeight, WRectF sourceRect);

	/**
	 * Draws a line.
	 * <p>
	 * The line must be stroked and transformed using the current painter
	 * settings.
	 */
	public void drawLine(double x1, double y1, double x2, double y2);

	/**
	 * Draws a path.
	 * <p>
	 * The path must be stroked, filled, and transformed using the current
	 * painter settings.
	 */
	public void drawPath(WPainterPath path);

	/**
	 * Draws text.
	 * <p>
	 * The text must be rendered, stroked and transformed using the current
	 * painter settings.
	 */
	public void drawText(WRectF rect, EnumSet<AlignmentFlag> alignmentFlags,
			TextFlag textFlag, CharSequence text);

	/**
	 * Measures rendered text size.
	 * <p>
	 * Returns the bounding rect of the given text when rendered using the
	 * current font.
	 * <p>
	 * If <code>maxWidth</code> != -1, then the text is truncated to fit in the
	 * width.
	 * <p>
	 * If <code>wordWrap</code> = <code>true</code> then text is truncated only
	 * at word boundaries. Note that in this case the whitespace at the
	 * truncated position is included in the text but not accounted for by the
	 * returned width (since usually you will not render the whitespace at the
	 * end of a line).
	 * <p>
	 * Throws a std::logic_error if the underlying device does not provide font
	 * metrics.
	 */
	public WTextItem measureText(CharSequence text, double maxWidth,
			boolean wordWrap);

	/**
	 * Measures rendered text size.
	 * <p>
	 * Returns
	 * {@link #measureText(CharSequence text, double maxWidth, boolean wordWrap)
	 * measureText(text, - 1, false)}
	 */
	public WTextItem measureText(CharSequence text);

	/**
	 * Measures rendered text size.
	 * <p>
	 * Returns
	 * {@link #measureText(CharSequence text, double maxWidth, boolean wordWrap)
	 * measureText(text, maxWidth, false)}
	 */
	public WTextItem measureText(CharSequence text, double maxWidth);

	/**
	 * Returns font metrics.
	 * <p>
	 * This returns font metrics for the current font.
	 * <p>
	 * Throws a std::logic_error if the underlying device does not provide font
	 * metrics.
	 */
	public WFontMetrics getFontMetrics();

	/**
	 * Initializes the device for painting.
	 * <p>
	 * This method is called when a {@link WPainter} starts painting.
	 * <p>
	 * 
	 * @see WPainter#begin(WPaintDevice device)
	 * @see WPaintDevice#getPainter()
	 */
	public void init();

	/**
	 * Finishes painting on the device.
	 * <p>
	 * This method is called when a {@link WPainter} stopped painting.
	 * <p>
	 * 
	 * @see WPainter#end()
	 */
	public void done();

	/**
	 * Returns whether painting is active.
	 * <p>
	 * 
	 * @see WPaintDevice#init()
	 * @see WPaintDevice#getPainter()
	 */
	public boolean isPaintActive();

	/**
	 * Returns the painter that is currently painting on the device.
	 * <p>
	 * 
	 * @see WPaintDevice#init()
	 */
	WPainter getPainter();

	/**
	 * Sets the painter.
	 */
	void setPainter(WPainter painter);
}
