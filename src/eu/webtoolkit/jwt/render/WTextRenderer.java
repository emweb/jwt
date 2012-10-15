/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.render;

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
 * An XHTML renderering engine.
 * <p>
 * 
 * This class implements a rendering engine for a (subset of) XHTML. Its
 * intended use is to be able to accurately render the output of the
 * {@link WTextEdit} widget (although it handles a more general subset of XHTML
 * than is required to do just that). Its focus is on high-quality rendering of
 * text-like contents.
 * <p>
 * The following are the main features:
 * <ul>
 * <li>compliant rendering of inline (text) contents, floats, tables, images,
 * ordered lists, unordered lists, in any arbitrary combination</li>
 * <li>support for fonts, font styles, font sizes and text decorations</li>
 * <li>support for text alignment options, padding, margins, borders, background
 * color (only on block elements) and text colors</li>
 * <li>supports automatic page breaks</li>
 * <li>supports font scaling (rendering text at another DPI than the rest)</li>
 * <li>can be used in conjunction with other drawing instructions to the same
 * paint device.</li>
 * </ul>
 * <p>
 * Some of the main limitations are:
 * <ul>
 * <li>there is no CSS style resolution other than inline style definitions: the
 * &quot;class&quot; attribute is ignored entirely.</li>
 * <li>only &quot;display: inline&quot; or &quot;display: block&quot; elements
 * are supported. &quot;display: none&quot; and &quot;display:
 * inline-block&quot; are not (yet) recognized.</li>
 * <li>only normal positioning is supported (absolute positioning and relative
 * positioning is not supported <i>yet</i>).</li>
 * <li>only colors defined in terms of RGB values are supported: CSS named
 * colors (e.g. &apos;blue&apos;) are not allowed.</li>
 * </ul>
 * <p>
 * The basics are solid though and the hardest part has been implemented
 * (notably handling floats and (nested) tables). Anything else could be easily
 * improved, let us known in the bug tracker.
 * <p>
 * This class is an abstract class. A concrete class implements the pure virtual
 * methods to create an appropriate {@link WPaintDevice} for each page and to
 * provide page dimension information. The paint device needs to support font
 * metrics, which currently is only implemented by {@link WPdfImage} or
 * {@link WRasterImage}.
 * <p>
 * All coordinates and dimensions used by this class are pixel coordinates.
 */
public abstract class WTextRenderer {
	private static Logger logger = LoggerFactory.getLogger(WTextRenderer.class);

	/**
	 * Renders an XHTML fragment.
	 * <p>
	 * The text is rendered, starting at position <code>y</code>, and flowing
	 * down the page. New pages are created using
	 * <code>{@link WTextRenderer#startPage(int page) startPage()}</code> to
	 * render more contents on a next page. The return value is the position at
	 * which rendering stopped on the last page on which was rendered.
	 * <p>
	 * This <code>y</code> position and returned position are <i>text
	 * coordinates</i>, which differ from page coordinates in that they exclude
	 * margins.
	 * <p>
	 * The function returns the end position. You may call this function
	 * multiple times.
	 * <p>
	 * Each invocation to
	 * {@link WTextRenderer#render(CharSequence text, double y) render()} has
	 * the effect of resetting the logical page numbering used by
	 * {@link WTextRenderer#pageWidth(int page) pageWidth()},
	 * {@link WTextRenderer#pageHeight(int page) pageHeight()} and
	 * {@link WTextRenderer#startPage(int page) startPage()} so that the current
	 * page is page 0.
	 */
	public double render(CharSequence text, double y) {
		String xhtml = text.toString();
		try {
			net.n3.nanoxml.XMLElement doc = RenderUtils.parseXHTML(xhtml);
			Block docBlock = new Block(doc, (Block) null);
			docBlock.determineDisplay();
			docBlock.normalizeWhitespace(false, doc);
			PageState currentPs = new PageState();
			currentPs.y = y;
			currentPs.page = 0;
			currentPs.minX = 0;
			currentPs.maxX = this.textWidth(currentPs.page);
			this.device_ = this.startPage(currentPs.page);
			this.painter_ = this.getPainter(this.device_);
			WFont defaultFont = new WFont();
			defaultFont.setFamily(WFont.GenericFamily.SansSerif);
			this.painter_.setFont(defaultFont);
			double collapseMarginBottom = 0;
			double minX = 0;
			double maxX = this.textWidth(currentPs.page);
			boolean tooWide = false;
			for (;;) {
				currentPs.minX = minX;
				currentPs.maxX = maxX;
				collapseMarginBottom = docBlock.layoutBlock(currentPs, false,
						this, Double.MAX_VALUE, collapseMarginBottom);
				if (currentPs.maxX > maxX) {
					if (!tooWide) {
						logger.warn(new StringWriter().append(
								"contents too wide for page.").toString());
						tooWide = true;
					}
					maxX = currentPs.maxX;
				} else {
					Block.clearFloats(currentPs, maxX - minX);
					break;
				}
			}
			for (int page = 0; page <= currentPs.page; ++page) {
				if (page != 0) {
					this.device_ = this.startPage(page);
					this.painter_ = this.getPainter(this.device_);
					this.painter_.setFont(defaultFont);
				}
				docBlock.render(this, page);
				this.endPage(this.device_);
			}
			return currentPs.y;
		} catch (RuntimeException e) {
			throw e;
		}
	}

	/**
	 * Renders an XHTML fragment.
	 * <p>
	 * Returns {@link #render(CharSequence text, double y) render(text, 0)}
	 */
	public final double render(CharSequence text) {
		return render(text, 0);
	}

	/**
	 * Returns the page text width.
	 * <p>
	 * This returns the width of the page in which text needs to be rendered,
	 * excluding horizontal margins, in pixels.
	 * <p>
	 * 
	 * @see WTextRenderer#textHeight(int page)
	 */
	public double textWidth(int page) {
		return this.pageWidth(page) - this.getMargin(Side.Left)
				- this.getMargin(Side.Right);
	}

	/**
	 * Returns the page text height.
	 * <p>
	 * This returns the height of the page in which text needs to be rendered,
	 * excluding vertical margins, in pixels.
	 * <p>
	 * 
	 * @see WTextRenderer#textWidth(int page)
	 */
	public double textHeight(int page) {
		return this.pageHeight(page) - this.getMargin(Side.Top)
				- this.getMargin(Side.Bottom);
	}

	/**
	 * Sets the scaling factor used for font rendering.
	 * <p>
	 * A scaling can be set for text. The scaling factor has as effect that text
	 * font sizes are modified by the scale. Also CSS length units that are
	 * defined in terms of font units (&quot;em&quot; and &quot;ex&quot;) are
	 * scaled accordingly.
	 * <p>
	 * The default value is 1.
	 */
	public void setFontScale(double factor) {
		this.fontScale_ = factor;
	}

	/**
	 * Returns the font scaling factor.
	 * <p>
	 * 
	 * @see WTextRenderer#setFontScale(double factor)
	 */
	public double getFontScale() {
		return this.fontScale_;
	}

	/**
	 * Returns the page width.
	 * <p>
	 * Returns the total page width (in pixel units), including horizontal
	 * margins.
	 */
	public abstract double pageWidth(int page);

	/**
	 * Returns the page height.
	 * <p>
	 * Returns the total page height (in pixel units), including vertical
	 * margins.
	 */
	public abstract double pageHeight(int page);

	/**
	 * Returns the margin.
	 * <p>
	 * Returns the margin at given side (in pixel units).
	 */
	public abstract double getMargin(Side side);

	/**
	 * Returns a paint device to render a given page.
	 * <p>
	 * The {@link WTextRenderer#render(CharSequence text, double y) render()}
	 * method calls this function once for each page it wants to render.
	 */
	public abstract WPaintDevice startPage(int page);

	/**
	 * Stops painting on the given page.
	 */
	public abstract void endPage(WPaintDevice device);

	/**
	 * Returns a painter for the current page.
	 */
	public abstract WPainter getPainter(WPaintDevice device);

	/**
	 * Constructor.
	 */
	protected WTextRenderer() {
		this.device_ = null;
		this.fontScale_ = 1;
	}

	private WPainter painter_;
	private WPaintDevice device_;
	private double fontScale_;

	WPainter getPainter() {
		return this.painter_;
	}
}
