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
 * An XHTML to PDF renderer.
 *
 * <p>This class implements an XHTML to PDF renderer. The rendering engine supports only a subset of
 * XHTML. See the documentation of {@link WTextRenderer} for more information.
 *
 * <p>The renderer renders to a PDFJet PDF document (using {@link WPdfImage}).
 *
 * <p>By default it uses a pixel resolution of 72 DPI, which is the default for a {@link WPdfImage},
 * but differs from the default used by most browsers (which is 96 DPI and has nothing to do with
 * the actual screen resolution). The pixel resolution can be configured using {@link
 * WPdfRenderer#setDpi(int dpi) setDpi()}. Increasing the resolution has the effect of scaling down
 * the rendering. This can be used in conjunction with {@link WTextRenderer#setFontScale(double
 * factor) WTextRenderer#setFontScale()} to scale the font size differently than other content.
 *
 * <p>Font information is embedded in the PDF. Fonts supported are native PostScript fonts (Base-14)
 * (only ASCII-7), or true type fonts (Unicode). See {@link WPdfRenderer#addFontCollection(String
 * directory, boolean recursive) addFontCollection()} for more information on how fonts are located.
 */
public class WPdfRenderer extends WTextRenderer {
  private static Logger logger = LoggerFactory.getLogger(WPdfRenderer.class);

  /**
   * Creates a new PDF renderer.
   *
   * <p>The PDF renderer will render on the given <code>pdf</code> (starting). If the <code>page
   * </code> is not <code>null</code>, then rendering will happen on this first page (and its page
   * sizes will be taken into account).
   *
   * <p>Default margins are 0, and the default DPI is 72.
   */
  public WPdfRenderer(com.pdfjet.PDF pdf, com.pdfjet.Page page) {
    super();
    this.fontCollections_ = new ArrayList<WPdfRenderer.FontCollection>();
    this.pdf_ = pdf;
    this.dpi_ = 72;
    this.painter_ = null;
    for (int i = 0; i < 4; ++i) {
      this.margin_[i] = 0;
    }
    this.setCurrentPage(page);
  }
  /**
   * Creates a new PDF renderer.
   *
   * <p>Calls {@link #WPdfRenderer(com.pdfjet.PDF pdf, com.pdfjet.Page page) this(pdf,
   * (com.pdfjet.Page)null)}
   */
  public WPdfRenderer(com.pdfjet.PDF pdf) {
    this(pdf, (com.pdfjet.Page) null);
  }
  /**
   * Sets the page margins.
   *
   * <p>This sets page margins, in <code>cm</code>, for one or more <code>sides</code>.
   */
  public void setMargin(double margin, EnumSet<Side> sides) {
    if (sides.contains(Side.Top)) {
      this.margin_[0] = margin;
    }
    if (sides.contains(Side.Right)) {
      this.margin_[1] = margin;
    }
    if (sides.contains(Side.Bottom)) {
      this.margin_[2] = margin;
    }
    if (sides.contains(Side.Left)) {
      this.margin_[3] = margin;
    }
  }
  /**
   * Sets the page margins.
   *
   * <p>Calls {@link #setMargin(double margin, EnumSet sides) setMargin(margin, EnumSet.of(side,
   * sides))}
   */
  public final void setMargin(double margin, Side side, Side... sides) {
    setMargin(margin, EnumSet.of(side, sides));
  }
  /**
   * Sets the page margins.
   *
   * <p>Calls {@link #setMargin(double margin, EnumSet sides) setMargin(margin, Side.AllSides)}
   */
  public final void setMargin(double margin) {
    setMargin(margin, Side.AllSides);
  }
  /**
   * Sets the resolution.
   *
   * <p>The resolution used between CSS pixels and actual page dimensions. Note that his does not
   * have an effect on the <i>de facto</i> standard CSS resolution of 96 DPI that is used to convert
   * between physical {@link WLength} units (like <i>cm</i>, <i>inch</i> and <i>point</i>) and
   * pixels. Instead it has the effect of scaling down or up the rendered XHTML on the page.
   *
   * <p>The dpi setting also affects the {@link WPdfRenderer#pageWidth(int page) pageWidth()},
   * {@link WPdfRenderer#pageHeight(int page) pageHeight()}, and {@link WPdfRenderer#getMargin(Side
   * side) getMargin()} pixel calculations.
   *
   * <p>The default resolution is 72 DPI.
   */
  public void setDpi(int dpi) {
    this.dpi_ = dpi;
  }
  /**
   * Adds a font collection.
   *
   * <p>If JWt has been configured to use <code>libpango</code>, then font matching and character
   * selection is done by libpango, and calls to this method are ignored. See {@link
   * WPdfImage#addFontCollection(String directory, boolean recursive) WPdfImage#addFontCollection()}
   * for more details.
   *
   * <p>If JWt was not configured to use <code>libpango</code>, you will have to add the directories
   * where JWt should look for fonts. You will also have to specify the required font in the HTML
   * source, e.g.:
   *
   * <p>
   *
   * <pre>{@code
   * WPdfRenderer renderer = new WPdfRenderer(pdf, page);
   * // ...
   * renderer.render("<p style=\"font-family: 'DejaVuSans', Arial\">\u00E9l\u00E8ve, fen\u00EAtre, \u00E2me</p>");
   *
   * }</pre>
   *
   * <p>
   *
   * @see WPdfImage#addFontCollection(String directory, boolean recursive)
   */
  public void addFontCollection(final String directory, boolean recursive) {
    WPdfRenderer.FontCollection c = new WPdfRenderer.FontCollection();
    c.directory = directory;
    c.recursive = recursive;
    this.fontCollections_.add(c);
  }
  /**
   * Adds a font collection.
   *
   * <p>Calls {@link #addFontCollection(String directory, boolean recursive)
   * addFontCollection(directory, true)}
   */
  public final void addFontCollection(final String directory) {
    addFontCollection(directory, true);
  }
  /** Sets the current page. */
  public void setCurrentPage(com.pdfjet.Page page) {
    this.page_ = page;
  }
  /**
   * Returns the current page.
   *
   * <p>This returns the page last created using {@link WPdfRenderer#createPage(int page)
   * createPage()}, or the page set with {@link WPdfRenderer#setCurrentPage(com.pdfjet.Page page)
   * setCurrentPage()} page.
   */
  public com.pdfjet.Page getCurrentPage() {
    return this.page_;
  }

  public double pageWidth(int page) {
    return this.page_.getWidth() * this.dpi_ / 72.0;
  }

  public double pageHeight(int page) {
    return this.page_.getHeight() * this.dpi_ / 72.0;
  }

  public double getMargin(Side side) {
    final double CmPerInch = 2.54;
    switch (side) {
      case Top:
        return this.margin_[0] / CmPerInch * this.dpi_;
      case Right:
        return this.margin_[1] / CmPerInch * this.dpi_;
      case Bottom:
        return this.margin_[2] / CmPerInch * this.dpi_;
      case Left:
        return this.margin_[3] / CmPerInch * this.dpi_;
      default:
        logger.error(
            new StringWriter()
                .append("margin(Side) with invalid side")
                .append(String.valueOf((int) side.getValue()))
                .toString());
        return 0;
    }
  }

  public WPaintDevice startPage(int page) {
    if (page > 0) {
      this.setCurrentPage(this.createPage(page));
    }
    WPdfImage device =
        new WPdfImage(this.pdf_, this.page_, 0, 0, this.pageWidth(page), this.pageHeight(page));
    WTransform deviceTransform = new WTransform();
    deviceTransform.scale(72.0f / this.dpi_, 72.0f / this.dpi_);
    device.setDeviceTransform(deviceTransform);
    for (int i = 0; i < this.fontCollections_.size(); ++i) {
      device.addFontCollection(
          this.fontCollections_.get(i).directory, this.fontCollections_.get(i).recursive);
    }
    return device;
  }

  public void endPage(WPaintDevice device) {

    this.painter_ = null;
  }

  public WPainter getPainter(WPaintDevice device) {
    if (!(this.painter_ != null)) {
      this.painter_ = new WPainter(device);
    }
    return this.painter_;
  }
  /**
   * Creates a new page.
   *
   * <p>The default implementation creates a new page with the same dimensions as the previous page.
   *
   * <p>You may want to specialize this method to add e.g.~headers and footers.
   */
  public com.pdfjet.Page createPage(int page) {
    return PdfRenderUtils.createPage(this.pdf_, this.page_.getWidth(), this.page_.getHeight());
  }

  static class FontCollection {
    private static Logger logger = LoggerFactory.getLogger(FontCollection.class);

    public String directory;
    public boolean recursive;
  }

  private List<WPdfRenderer.FontCollection> fontCollections_;
  private com.pdfjet.PDF pdf_;
  private com.pdfjet.Page page_;
  private double[] margin_ = new double[4];
  private int dpi_;
  private WPainter painter_;
}
