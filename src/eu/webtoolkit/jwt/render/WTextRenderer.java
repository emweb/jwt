/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.render;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An XHTML renderering engine.
 *
 * <p>This class implements a rendering engine for a (subset of) XHTML/CSS. Its intended use is to
 * be able to accurately render the output of the {@link WTextEdit} widget (although it handles a
 * more general subset of XHTML/CSS than is required to do just that). Its focus is on high-quality
 * rendering of text-like contents.
 *
 * <p>The following are the main features:
 *
 * <ul>
 *   <li>decent rendering of inline (text) contents, floats, tables, images, ordered lists,
 *       unordered lists, in any arbitrary combination, with mixtures of font sizes, etc...
 *   <li>CSS stylesheet support using in-document &lt;style&gt;, inline style attributes or using
 *       {@link WTextRenderer#setStyleSheetText(CharSequence styleSheetContents)
 *       setStyleSheetText()}.
 *   <li>support for relative and absolute positioned layout contexts
 *   <li>support for (true type) fonts, font styles, font sizes and text decorations
 *   <li>support for text alignment options, padding, margins, borders, background color (only on
 *       block elements) and text colors
 *   <li>supports automatic and CSS page breaks (page-break-after or page-break-before)
 *   <li>supports font scaling (rendering text at another DPI than the rest)
 *   <li>can be used in conjunction with other drawing instructions to the same paint device.
 * </ul>
 *
 * <p>Some of the main limitations are:
 *
 * <ul>
 *   <li>only &quot;display: inline&quot; or &quot;display: block&quot; elements are supported.
 *       &quot;display: none&quot; and &quot;display: inline-block&quot; are not (yet) recognized.
 *   <li>only colors defined in terms of RGB values are supported: CSS named colors (e.g.
 *       &apos;blue&apos;) are not allowed.
 *   <li>Bidi (Right-to-Left) text rendering is not supported
 * </ul>
 *
 * <p>The renderer is not CSS compliant (simply because it is still lacking alot of features), but
 * the subset of CSS that is supported is a pragmatic choice. If things are lacking, let us known in
 * the bug tracker.
 *
 * <p>This class is an abstract class. A concrete class implements the pure virtual methods to
 * create an appropriate {@link WPaintDevice} for each page and to provide page dimension
 * information.
 *
 * <p>All coordinates and dimensions in the API below are pixel coordinates.
 */
public abstract class WTextRenderer {
  private static Logger logger = LoggerFactory.getLogger(WTextRenderer.class);

  /**
   * A rendering box of a layed out DOM node.
   *
   * <p>
   *
   * @see WTextRenderer#paintNode(WPainter painter, WTextRenderer.Node node)
   */
  public static class Node {
    private static Logger logger = LoggerFactory.getLogger(Node.class);

    /** Returns the element type. */
    public DomElementType getType() {
      return this.block_.getType();
    }
    /**
     * Returns an attribute value.
     *
     * <p>This returns an empty string for an undefined attribute.
     */
    public String attributeValue(final String attribute) {
      String a = attribute;
      String ans = this.block_.attributeValue(a);
      return ans;
    }
    /** Returns the page. */
    public int getPage() {
      return this.lb_.page;
    }
    /** Returns the x position. */
    public double getX() {
      return this.lb_.x + this.renderer_.getMargin(Side.Left);
    }
    /** Returns the y position. */
    public double getY() {
      return this.lb_.y + this.renderer_.getMargin(Side.Top);
    }
    /** Returns the width. */
    public double getWidth() {
      return this.lb_.width;
    }
    /** Returns the height. */
    public double getHeight() {
      return this.lb_.height;
    }
    /**
     * Returns the fragment number.
     *
     * <p>A single DOM node can result in multiple layout boxes: e.g. inline contents can be split
     * over multiple lines, resulting in a layout box for each line, and block layout contents can
     * be split over multiple pages, resulting in a layout box per page.
     */
    public int getFragment() {
      if (!this.block_.blockLayout.isEmpty()) {
        for (int i = 0; i < this.block_.blockLayout.size(); ++i) {
          if (this.lb_ == this.block_.blockLayout.get(i)) {
            return i;
          }
        }
        return -1;
      } else {
        for (int i = 0; i < this.block_.inlineLayout.size(); ++i) {
          if (this.lb_ == this.block_.inlineLayout.get(i)) {
            return i;
          }
        }
        return -1;
      }
    }
    /**
     * Returns the fragment count.
     *
     * <p>
     *
     * @see WTextRenderer.Node#getFragment()
     */
    public int getFragmentCount() {
      return this.block_.blockLayout.size() + this.block_.inlineLayout.size();
    }

    private final WTextRenderer renderer_;
    final Block block_;
    private final LayoutBox lb_;

    Node(final Block block, final LayoutBox lb, final WTextRenderer renderer) {
      this.renderer_ = renderer;
      this.block_ = block;
      this.lb_ = lb;
    }

    Block getBlock() {
      return this.block_;
    }

    LayoutBox getLb() {
      return this.lb_;
    }
  }
  /**
   * Renders an XHTML fragment.
   *
   * <p>The text is rendered, starting at position <code>y</code>, and flowing down the page. New
   * pages are created using <code>{@link WTextRenderer#startPage(int page) startPage()}</code> to
   * render more contents on a next page. The return value is the position at which rendering
   * stopped on the last page on which was rendered.
   *
   * <p>This <code>y</code> position and returned position are <i>text coordinates</i>, which differ
   * from page coordinates in that they exclude margins.
   *
   * <p>The function returns the end position. You may call this function multiple times.
   *
   * <p>Each invocation to {@link WTextRenderer#render(CharSequence text, double y) render()} has
   * the effect of resetting the logical page numbering used by {@link WTextRenderer#pageWidth(int
   * page) pageWidth()}, {@link WTextRenderer#pageHeight(int page) pageHeight()} and {@link
   * WTextRenderer#startPage(int page) startPage()} so that the current page is page 0.
   */
  public double render(final CharSequence text, double y) {
    String xhtml = WString.toWString(text).toXhtml();
    try {
      net.n3.nanoxml.XMLElement doc = RenderUtils.parseXHTML(xhtml);
      Block docBlock = new Block(doc, (Block) null);
      CombinedStyleSheet styles = new CombinedStyleSheet();
      if (this.styleSheet_ != null) {
        styles.use(this.styleSheet_);
      }
      StringBuilder ss = new StringBuilder();
      docBlock.collectStyles(ss);
      if (!(ss.length() == 0)) {
        CssParser parser = new CssParser();
        StyleSheet docStyles = parser.parse(ss.toString());
        if (docStyles != null) {
          styles.use(docStyles);
        } else {
          logger.error(
              new StringWriter()
                  .append("Error parsing style sheet: ")
                  .append(parser.getLastError())
                  .toString());
        }
      }
      docBlock.setStyleSheet(styles);
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
      defaultFont.setFamily(FontFamily.SansSerif);
      this.painter_.setFont(defaultFont);
      double collapseMarginBottom = 0;
      double minX = 0;
      double maxX = this.textWidth(currentPs.page);
      boolean tooWide = false;
      for (int i = 0; i < 2; ++i) {
        currentPs.y = y;
        currentPs.page = 0;
        currentPs.minX = minX;
        currentPs.maxX = maxX;
        collapseMarginBottom =
            docBlock.layoutBlock(currentPs, false, this, Double.MAX_VALUE, collapseMarginBottom);
        if (isEpsilonMore(currentPs.maxX, maxX)) {
          if (!tooWide) {
            logger.warn(
                new StringWriter()
                    .append("contents too wide for page. (")
                    .append(String.valueOf(currentPs.maxX))
                    .append(" > ")
                    .append(String.valueOf(maxX))
                    .append(")")
                    .toString());
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
        docBlock.render(this, this.painter_, page);
        this.endPage(this.device_);
      }
      return currentPs.y;
    } catch (final RuntimeException e) {
      throw e;
    }
  }
  /**
   * Renders an XHTML fragment.
   *
   * <p>Returns {@link #render(CharSequence text, double y) render(text, 0)}
   */
  public final double render(final CharSequence text) {
    return render(text, 0);
  }
  /**
   * Sets the contents of a cascading style sheet (CSS).
   *
   * <p>This sets the text <code>contents</code> to be used as CSS. Any previous CSS declarations
   * are discarded. Returns true if parsing was successful, false if otherwise. If parsing failed,
   * the stylesheet text that was already in use will not have been changed. Use
   * getStyleSheetParseErrors to access parse error information.
   *
   * <p>
   *
   * <p><i><b>Warning: </b>Only the following CSS selector features are supported:
   *
   * <ul>
   *   <li>tag selectors: e.g. span or *
   *   <li>class name selectors: .class
   *   <li>id selectors: #id
   *   <li>descendant selectors: h1 h2 h3 {}
   *   <li>multiples: h1, h2, h3 {}
   *       <pre>{@code
   * h1.a1#one.a2 h3#two.c {}
   *
   * }</pre>
   * </ul>
   *
   * </i>
   *
   * @see WTextRenderer#getStyleSheetParseErrors()
   */
  public boolean setStyleSheetText(final CharSequence styleSheetContents) {
    if ((styleSheetContents.length() == 0)) {
      this.styleSheetText_ = new WString();
      this.styleSheet_ = null;
      this.error_ = "";
      return true;
    } else {
      CssParser parser = new CssParser();
      StyleSheet styleSheet = parser.parse(styleSheetContents);
      if (!(styleSheet != null)) {
        this.error_ = parser.getLastError();
        return false;
      }
      this.error_ = "";
      this.styleSheetText_ = WString.toWString(styleSheetContents);
      this.styleSheet_ = styleSheet;
      return true;
    }
  }
  /**
   * Appends an external cascading style sheet (CSS).
   *
   * <p>This is an overloaded member, provided for convenience. Equivalent to:
   *
   * <pre>{@code
   * setStyleSheetText(styleSheetText() + <filename_contents>)
   *
   * }</pre>
   *
   * <p>
   *
   * @see WTextRenderer#setStyleSheetText(CharSequence styleSheetContents)
   */
  public boolean useStyleSheet(final CharSequence filename) {
    String contents = FileUtils.fileToString(filename.toString());
    if (!(contents != null)) {
      return false;
    }
    boolean b = this.setStyleSheetText(this.getStyleSheetText().append("\n").append(contents));

    return b;
  }
  /**
   * Clears the used stylesheet.
   *
   * <p>This is an overloaded member, provided for convenience. Equivalent to:
   *
   * <pre>{@code
   * setStyleSheetText("")
   *
   * }</pre>
   *
   * <p>
   *
   * @see WTextRenderer#setStyleSheetText(CharSequence styleSheetContents)
   */
  public void clearStyleSheet() {
    this.setStyleSheetText("");
  }
  /**
   * Returns the CSS in use.
   *
   * <p>This returns all the CSS declarations in use.
   *
   * <p>
   *
   * @see WTextRenderer#setStyleSheetText(CharSequence styleSheetContents)
   */
  public WString getStyleSheetText() {
    return this.styleSheetText_;
  }
  /**
   * Returns all parse error information of the last call to setStyleSheetText.
   *
   * <p>setStyleSheetText stores all parse errors inside. Use getStyleSheetParseErrors to access
   * information about them. Information is newline(\ n) separated.
   *
   * <p>
   *
   * @see WTextRenderer#setStyleSheetText(CharSequence styleSheetContents)
   */
  public String getStyleSheetParseErrors() {
    return this.error_;
  }
  /**
   * Returns the page text width.
   *
   * <p>This returns the width of the page in which text needs to be rendered, excluding horizontal
   * margins, in pixels.
   *
   * <p>
   *
   * @see WTextRenderer#textHeight(int page)
   */
  public double textWidth(int page) {
    return this.pageWidth(page) - this.getMargin(Side.Left) - this.getMargin(Side.Right);
  }
  /**
   * Returns the page text height.
   *
   * <p>This returns the height of the page in which text needs to be rendered, excluding vertical
   * margins, in pixels.
   *
   * <p>
   *
   * @see WTextRenderer#textWidth(int page)
   */
  public double textHeight(int page) {
    return this.pageHeight(page) - this.getMargin(Side.Top) - this.getMargin(Side.Bottom);
  }
  /**
   * Sets the scaling factor used for font rendering.
   *
   * <p>A scaling can be set for text. The scaling factor has as effect that text font sizes are
   * modified by the scale. Also CSS length units that are defined in terms of font units
   * (&quot;em&quot; and &quot;ex&quot;) are scaled accordingly.
   *
   * <p>The default value is 1.
   */
  public void setFontScale(double factor) {
    this.fontScale_ = factor;
  }
  /**
   * Returns the font scaling factor.
   *
   * <p>
   *
   * @see WTextRenderer#setFontScale(double factor)
   */
  public double getFontScale() {
    return this.fontScale_;
  }
  /**
   * Returns the page width.
   *
   * <p>Returns the total page width (in pixel units), including horizontal margins.
   */
  public abstract double pageWidth(int page);
  /**
   * Returns the page height.
   *
   * <p>Returns the total page height (in pixel units), including vertical margins.
   */
  public abstract double pageHeight(int page);
  /**
   * Returns the margin.
   *
   * <p>Returns the margin at given side (in pixel units).
   */
  public abstract double getMargin(Side side);
  /**
   * Returns a paint device to render a given page.
   *
   * <p>The {@link WTextRenderer#render(CharSequence text, double y) render()} method calls this
   * function once for each page it wants to render.
   */
  public abstract WPaintDevice startPage(int page);
  /** Stops painting on the given page. */
  public abstract void endPage(WPaintDevice device);
  /** Returns a painter for the current page. */
  public abstract WPainter getPainter(WPaintDevice device);
  /**
   * Paints an XHTML node.
   *
   * <p>The default implementation paints the node conforming to the XHTML specification.
   *
   * <p>You may want to specialize this method if you wish to customize (or ignore) the rendering
   * for certain nodes or node types, or if you want to capture the actual layout positions for
   * other processing.
   *
   * <p>The node information contains the layout position at which the node is being painted.
   */
  public void paintNode(final WPainter painter, final WTextRenderer.Node node) {
    node.getBlock().actualRender(this, painter, node.getLb());
  }
  /** Constructor. */
  protected WTextRenderer() {
    this.device_ = null;
    this.fontScale_ = 1;
    this.styleSheetText_ = new WString();
    this.styleSheet_ = (StyleSheet) null;
    this.error_ = "";
  }

  private WPainter painter_;
  private WPaintDevice device_;
  private double fontScale_;
  private WString styleSheetText_;
  private StyleSheet styleSheet_;
  private String error_;

  WPainter getPainter() {
    return this.painter_;
  }

  private static final double EPSILON = 1e-4;

  static boolean isEpsilonMore(double x, double limit) {
    return x - EPSILON > limit;
  }
}
