/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

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
 * Vector graphics painting class.
 *
 * <p>The painter class provides a vector graphics interface for painting. It needs to be used in
 * conjunction with a {@link WPaintDevice}, onto which it paints. To start painting on a device,
 * either pass the device through the constructor, or use {@link WPainter#begin(WPaintDevice device)
 * begin()}.
 *
 * <p>A typical use is to instantiate a WPainter from within a specialized {@link
 * WPaintedWidget#paintEvent(WPaintDevice paintDevice) WPaintedWidget#paintEvent()} implementation,
 * to paint on the given paint device, but you can also use a painter to paint directly to a
 * particular paint device of choice, for example to create SVG, PDF or PNG images (as resources).
 *
 * <p>The painter maintains state such as the current {@link WPainter#setPen(WPen p) pen}, {@link
 * WPainter#setBrush(WBrush b) brush}, {@link WPainter#setFont(WFont f) font}, {@link
 * WPainter#getShadow() getShadow()}, {@link WPainter#getWorldTransform() transformation} and
 * clipping settings (see {@link WPainter#setClipping(boolean enable) setClipping()} and {@link
 * WPainter#setClipPath(WPainterPath clipPath) setClipPath()}). A particular state can be saved
 * using {@link WPainter#save() save()} and later restored using {@link WPainter#restore()
 * restore()}.
 *
 * <p>The painting system distinguishes between device coordinates, logical coordinates, and local
 * coordinates. The device coordinate system ranges from (0, 0) in the top left corner of the
 * device, to (device.width().toPixels(), device.height().toPixels()) for the bottom right corner.
 * The logical coordinate system defines a coordinate system that may be chosen independent of the
 * geometry of the device, which is convenient to make abstraction of the actual device size.
 * Finally, the current local coordinate system may be different from the logical coordinate system
 * because of a transformation set (using {@link WPainter#translate(WPointF p) translate()}, {@link
 * WPainter#rotate(double angle) rotate()}, and {@link WPainter#scale(double sx, double sy)
 * scale()}). Initially, the local coordinate system coincides with the logical coordinate system,
 * which coincides with the device coordinate system.
 *
 * <p>The device coordinates are defined in terms of pixels. Even though most underlying devices are
 * actual vector graphics formats, when used in conjunction with a {@link WPaintedWidget}, these
 * vector graphics are rendered by the browser onto a pixel-based canvas (like the rest of the
 * user-interface). The coordinates are defined such that integer values correspond to an imaginary
 * raster which separates the individual pixels, as in the figure below.
 *
 * <p><div align="center"> <img src="doc-files/WPainter.png">
 *
 * <p><strong>The device coordinate system for a 6x5 pixel device</strong> </div>
 *
 * <p>As a consequence, to avoid anti-aliasing effects when drawing straight lines of width one
 * pixel, you will need to use vertices that indicate the middle of a pixel to get a crisp one-pixel
 * wide line, as in the example figure.
 *
 * <p>By setting a {@link WPainter#getViewPort() getViewPort()} and a {@link WPainter#getWindow()
 * getWindow()}, a viewPort transformation is defined which maps logical coordinates onto device
 * coordinates. By changing the world transformation (using {@link
 * WPainter#setWorldTransform(WTransform matrix, boolean combine) setWorldTransform()}, or {@link
 * WPainter#translate(WPointF p) translate()}, {@link WPainter#rotate(double angle) rotate()},
 * {@link WPainter#scale(double sx, double sy) scale()} operations), it is defined how current local
 * coordinates map onto logical coordinates.
 *
 * <p>The painter provides support for clipping using an arbitrary {@link WPainterPath path}, but
 * not that the WVmlImage paint device only has limited support for clipping.
 *
 * <p>
 *
 * @see WPaintedWidget#paintEvent(WPaintDevice paintDevice)
 */
public class WPainter {
  private static Logger logger = LoggerFactory.getLogger(WPainter.class);

  /**
   * Default constructor.
   *
   * <p>Before painting, you must invoke {@link WPainter#begin(WPaintDevice device) begin()} on a
   * paint device.
   *
   * <p>
   *
   * @see WPainter#WPainter(WPaintDevice device)
   */
  public WPainter() {
    this.device_ = null;
    this.viewPort_ = null;
    this.window_ = null;
    this.viewTransform_ = new WTransform();
    this.stateStack_ = new ArrayList<WPainter.State>();
    this.stateStack_.add(new WPainter.State());
  }
  /** Creates a painter on a given paint device. */
  public WPainter(WPaintDevice device) {
    this.device_ = null;
    this.viewPort_ = null;
    this.window_ = null;
    this.viewTransform_ = new WTransform();
    this.stateStack_ = new ArrayList<WPainter.State>();
    this.begin(device);
  }
  /**
   * Begins painting on a paint device.
   *
   * <p>
   *
   * @see WPainter#end()
   * @see WPainter#isActive()
   */
  public boolean begin(WPaintDevice device) {
    if (this.device_ != null) {
      return false;
    }
    if (device.isPaintActive()) {
      return false;
    }
    this.stateStack_.clear();
    this.stateStack_.add(new WPainter.State());
    this.device_ = device;
    this.device_.setPainter(this);
    this.device_.init();
    this.viewPort_ =
        new WRectF(0, 0, this.device_.getWidth().getValue(), this.device_.getHeight().getValue());
    this.window_ = this.viewPort_;
    this.recalculateViewTransform();
    return true;
  }
  /**
   * Returns whether this painter is active on a paint device.
   *
   * <p>
   *
   * @see WPainter#begin(WPaintDevice device)
   * @see WPainter#end()
   */
  public boolean isActive() {
    return this.device_ != null;
  }
  /** Ends painting. */
  public boolean end() {
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
   * Returns the device on which this painter is active (or 0 if not active).
   *
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
   * Sets a render hint.
   *
   * <p>Renderers may ignore particular hints for which they have no support.
   */
  public void setRenderHint(RenderHint hint, boolean on) {
    int old = EnumUtils.valueOf(this.getS().renderHints_);
    if (on) {
      this.getS().renderHints_.add(hint);
    } else {
      this.getS().renderHints_.remove(hint);
    }
    if (this.device_ != null && old != EnumUtils.valueOf(this.getS().renderHints_)) {
      this.device_.setChanged(EnumSet.of(PainterChangeFlag.Hints));
    }
  }
  /**
   * Sets a render hint.
   *
   * <p>Calls {@link #setRenderHint(RenderHint hint, boolean on) setRenderHint(hint, true)}
   */
  public final void setRenderHint(RenderHint hint) {
    setRenderHint(hint, true);
  }
  /**
   * Returns the current render hints.
   *
   * <p>Returns the logical OR of render hints currently set.
   *
   * <p>
   *
   * @see WPainter#setRenderHint(RenderHint hint, boolean on)
   */
  public EnumSet<RenderHint> getRenderHints() {
    return this.getS().renderHints_;
  }
  /**
   * Draws an arc.
   *
   * <p>Draws an arc using the current pen, and fills using the current brush.
   *
   * <p>The arc is defined as a segment from an ellipse, which fits in the <i>rectangle</i>. The
   * segment starts at <code>startAngle</code>, and spans an angle given by <code>spanAngle</code>.
   * These angles have as unit 1/16th of a degree, and are measured counter-clockwise starting from
   * the 3 o&apos;clock position.
   *
   * <p>
   *
   * @see WPainter#drawEllipse(WRectF rectangle)
   * @see WPainter#drawChord(WRectF rectangle, int startAngle, int spanAngle)
   * @see WPainter#drawArc(double x, double y, double width, double height, int startAngle, int
   *     spanAngle)
   */
  public void drawArc(final WRectF rectangle, int startAngle, int spanAngle) {
    this.device_.drawArc(rectangle.getNormalized(), startAngle / 16., spanAngle / 16.);
  }
  /**
   * Draws an arc.
   *
   * <p>This is an overloaded method for convenience.
   *
   * <p>
   *
   * @see WPainter#drawArc(WRectF rectangle, int startAngle, int spanAngle)
   */
  public void drawArc(
      double x, double y, double width, double height, int startAngle, int spanAngle) {
    this.drawArc(new WRectF(x, y, width, height), startAngle, spanAngle);
  }
  /**
   * Draws a chord.
   *
   * <p>Draws an arc using the current pen, and connects start and end point with a line. The area
   * is filled using the current brush.
   *
   * <p>The arc is defined as a segment from an ellipse, which fits in the <i>rectangle</i>. The
   * segment starts at <code>startAngle</code>, and spans an angle given by <code>spanAngle</code>.
   * These angles have as unit 1/16th of a degree, and are measured counter-clockwise starting at 3
   * o&apos;clock.
   *
   * <p>
   *
   * @see WPainter#drawEllipse(WRectF rectangle)
   * @see WPainter#drawArc(WRectF rectangle, int startAngle, int spanAngle)
   * @see WPainter#drawChord(double x, double y, double width, double height, int startAngle, int
   *     spanAngle)
   */
  public void drawChord(final WRectF rectangle, int startAngle, int spanAngle) {
    WTransform oldTransform = this.getWorldTransform().clone();
    this.translate(rectangle.getCenter().getX(), rectangle.getCenter().getY());
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
   * Draws a chord.
   *
   * <p>This is an overloaded method for convenience.
   *
   * <p>
   *
   * @see WPainter#drawChord(WRectF rectangle, int startAngle, int spanAngle)
   */
  public void drawChord(
      double x, double y, double width, double height, int startAngle, int spanAngle) {
    this.drawChord(new WRectF(x, y, width, height), startAngle, spanAngle);
  }
  /**
   * Draws an ellipse.
   *
   * <p>Draws an ellipse using the current pen and fills it using the current brush.
   *
   * <p>The ellipse is defined as being bounded by the <code>rectangle</code>.
   *
   * <p>
   *
   * @see WPainter#drawArc(WRectF rectangle, int startAngle, int spanAngle)
   * @see WPainter#drawEllipse(double x, double y, double width, double height)
   */
  public void drawEllipse(final WRectF rectangle) {
    this.device_.drawArc(rectangle.getNormalized(), 0, 360);
  }
  /**
   * Draws an ellipse.
   *
   * <p>This is an overloaded method for convenience.
   *
   * <p>
   *
   * @see WPainter#drawEllipse(WRectF rectangle)
   */
  public void drawEllipse(double x, double y, double width, double height) {
    this.drawEllipse(new WRectF(x, y, width, height));
  }
  /**
   * An image that can be rendered on a {@link WPainter}.
   *
   * <p>The image is specified in terms of a URL, and the width and height.
   *
   * <p>
   *
   * @see WPainter#drawImage(WPointF point, WPainter.Image image)
   */
  public static class Image {
    private static Logger logger = LoggerFactory.getLogger(Image.class);

    /**
     * Creates an image.
     *
     * <p>Create an image which is located at the <i>uri</i>, and which has dimensions <i>width</i>
     * x <i>height</i>.
     */
    public Image(final String url, int width, int height) {
      this.width_ = width;
      this.height_ = height;
      this.useOld_ = true;
      this.info_ = (WAbstractDataInfo) null;
      WDataInfo info = new WDataInfo();
      if (DataUri.isDataUri(url)) {
        info.setDataUri(url);
      } else {
        info.setUrl(url);
      }
      this.info_ = info;
    }
    /**
     * Creates an image.
     *
     * <p>Create an image which URI and/or file path is given by <code>info</code>, and which has
     * dimensions <code>width</code> x <code>height</code>.
     *
     * <p>
     *
     * <p><i><b>Note: </b>The information required depends on the {@link WPaintDevice} used. </i>
     */
    public Image(WAbstractDataInfo info, int width, int height) {
      this.width_ = width;
      this.height_ = height;
      this.useOld_ = false;
      this.info_ = info;
    }
    /**
     * Creates an image.
     *
     * <p>Create an image which is located at <i>uri</i> which is available on the local filesystem
     * as <i>file</i>. The image dimensions are retrieved from the file.
     */
    public Image(final String url, final String fileName) {
      this.useOld_ = true;
      this.info_ = (WAbstractDataInfo) null;
      WDataInfo info = new WDataInfo();
      info.setFilePath(fileName);
      if (DataUri.isDataUri(url)) {
        info.setDataUri(url);
      } else {
        info.setUrl(url);
      }
      this.info_ = info;
      this.evaluateSize();
    }
    /**
     * Creates an image.
     *
     * <p>Create an image which URI and/or file path is given by <code>info</code>. The image
     * dimensions are retrieved from the file (or the URI if it is a data URI).
     *
     * <p>
     *
     * <p><i><b>Note: </b>The information required depends on the {@link WPaintDevice} used. </i>
     */
    public Image(WAbstractDataInfo info) {
      this.useOld_ = false;
      this.info_ = info;
      this.evaluateSize();
    }
    /** Returns the url. */
    public String getUri() {
      String uri = "";
      if (this.info_.hasUrl()) {
        uri = this.info_.getUrl();
      } else {
        if (this.info_.hasDataUri()) {
          uri = this.info_.getDataUri();
        }
      }
      return uri;
    }
    /** Returns the data info of the image. */
    public WAbstractDataInfo getInfo() {
      return this.info_;
    }
    /** Returns the image width. */
    public int getWidth() {
      return this.width_;
    }
    /** Returns the image height. */
    public int getHeight() {
      return this.height_;
    }

    private int width_;
    private int height_;
    private boolean useOld_;
    private WAbstractDataInfo info_;

    private void evaluateSize() {
      if (this.info_.hasDataUri()) {
        DataUri uri = new DataUri(this.info_.getDataUri());
        WPoint size = eu.webtoolkit.jwt.utils.ImageUtils.getSize(uri.data);
        if (size.getX() == 0 || size.getY() == 0) {
          throw new WException("data uri: (" + uri.mimeType + "): could not determine image size");
        }
        this.width_ = size.getX();
        this.height_ = size.getY();
      } else {
        if (this.info_.hasFilePath()) {
          String fileName = this.info_.getFilePath();
          WPoint size = eu.webtoolkit.jwt.utils.ImageUtils.getSize(fileName);
          if (size.getX() == 0 || size.getY() == 0) {
            throw new WException("'" + fileName + "': could not determine image size");
          }
          this.width_ = size.getX();
          this.height_ = size.getY();
        } else {
          throw new WException(
              "'"
                  + this.info_.getName()
                  + "': could not determine image size. Add a filePath or a data uri.");
        }
      }
    }
  }
  /**
   * Draws an image.
   *
   * <p>Draws the <code>image</code> so that the top left corner corresponds to <code>point</code>.
   *
   * <p>This is an overloaded method provided for convenience.
   */
  public void drawImage(final WPointF point, final WPainter.Image image) {
    this.drawImage(
        new WRectF(point.getX(), point.getY(), image.getWidth(), image.getHeight()),
        image,
        new WRectF(0, 0, image.getWidth(), image.getHeight()));
  }
  /**
   * Draws part of an image.
   *
   * <p>Draws the <code>sourceRect</code> rectangle from an image to the location <code>point</code>
   * .
   *
   * <p>This is an overloaded method provided for convenience.
   */
  public void drawImage(final WPointF point, final WPainter.Image image, final WRectF sourceRect) {
    this.drawImage(
        new WRectF(point.getX(), point.getY(), sourceRect.getWidth(), sourceRect.getHeight()),
        image,
        sourceRect);
  }
  /**
   * Draws an image inside a rectangle.
   *
   * <p>Draws the <i>image</i> inside <code>rect</code> (If necessary, the image is scaled to fit
   * into the rectangle).
   *
   * <p>This is an overloaded method provided for convenience.
   */
  public void drawImage(final WRectF rect, final WPainter.Image image) {
    this.drawImage(rect, image, new WRectF(0, 0, image.getWidth(), image.getHeight()));
  }
  /**
   * Draws part of an image inside a rectangle.
   *
   * <p>Draws the <code>sourceRect</code> rectangle from an image inside <code>rect</code> (If
   * necessary, the image is scaled to fit into the rectangle).
   */
  public void drawImage(final WRectF rect, final WPainter.Image image, final WRectF sourceRect) {
    if (image.useOld_) {
      this.device_.drawImage(
          rect.getNormalized(),
          image.getInfo().getUrl(),
          image.getWidth(),
          image.getHeight(),
          sourceRect.getNormalized());
      return;
    }
    this.device_.drawImage(
        rect.getNormalized(),
        image.getInfo(),
        image.getWidth(),
        image.getHeight(),
        sourceRect.getNormalized());
  }
  /**
   * Draws part of an image.
   *
   * <p>Draws the <code>sourceRect</code> rectangle with top left corner (<i>sx</i>, <i>sy</i>) and
   * size <i>sw</i> x <code>sh</code> from an image to the location (<i>x</i>, <code>y</code>).
   */
  public void drawImage(
      double x, double y, final WPainter.Image image, double sx, double sy, double sw, double sh) {
    if (sw <= 0) {
      sw = image.getWidth() - sx;
    }
    if (sh <= 0) {
      sh = image.getHeight() - sy;
    }
    if (image.useOld_) {
      this.device_.drawImage(
          new WRectF(x, y, sw, sh),
          image.getInfo().getUrl(),
          image.getWidth(),
          image.getHeight(),
          new WRectF(sx, sy, sw, sh));
      return;
    }
    this.device_.drawImage(
        new WRectF(x, y, sw, sh),
        image.getInfo(),
        image.getWidth(),
        image.getHeight(),
        new WRectF(sx, sy, sw, sh));
  }
  /**
   * Draws part of an image.
   *
   * <p>Calls {@link #drawImage(double x, double y, WPainter.Image image, double sx, double sy,
   * double sw, double sh) drawImage(x, y, image, 0, 0, - 1, - 1)}
   */
  public final void drawImage(double x, double y, final WPainter.Image image) {
    drawImage(x, y, image, 0, 0, -1, -1);
  }
  /**
   * Draws part of an image.
   *
   * <p>Calls {@link #drawImage(double x, double y, WPainter.Image image, double sx, double sy,
   * double sw, double sh) drawImage(x, y, image, sx, 0, - 1, - 1)}
   */
  public final void drawImage(double x, double y, final WPainter.Image image, double sx) {
    drawImage(x, y, image, sx, 0, -1, -1);
  }
  /**
   * Draws part of an image.
   *
   * <p>Calls {@link #drawImage(double x, double y, WPainter.Image image, double sx, double sy,
   * double sw, double sh) drawImage(x, y, image, sx, sy, - 1, - 1)}
   */
  public final void drawImage(
      double x, double y, final WPainter.Image image, double sx, double sy) {
    drawImage(x, y, image, sx, sy, -1, -1);
  }
  /**
   * Draws part of an image.
   *
   * <p>Calls {@link #drawImage(double x, double y, WPainter.Image image, double sx, double sy,
   * double sw, double sh) drawImage(x, y, image, sx, sy, sw, - 1)}
   */
  public final void drawImage(
      double x, double y, final WPainter.Image image, double sx, double sy, double sw) {
    drawImage(x, y, image, sx, sy, sw, -1);
  }
  /**
   * Draws a line.
   *
   * <p>Draws a line using the current pen.
   *
   * <p>
   *
   * @see WPainter#drawLine(WPointF p1, WPointF p2)
   * @see WPainter#drawLine(double x1, double y1, double x2, double y2)
   */
  public void drawLine(final WLineF line) {
    this.drawLine(line.getX1(), line.getY1(), line.getX2(), line.getY2());
  }
  /**
   * Draws a line.
   *
   * <p>Draws a line defined by two points.
   *
   * <p>
   *
   * @see WPainter#drawLine(WLineF line)
   * @see WPainter#drawLine(double x1, double y1, double x2, double y2)
   */
  public void drawLine(final WPointF p1, final WPointF p2) {
    this.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
  }
  /**
   * Draws a line.
   *
   * <p>Draws a line defined by two points.
   *
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
   *
   * <p>Draws the <code>lineCount</code> first lines from the given array of lines.
   */
  public void drawLines(WLineF[] lines, int lineCount) {
    for (int i = 0; i < lineCount; ++i) {
      this.drawLine(lines[i]);
    }
  }
  /**
   * Draws an array of lines.
   *
   * <p>Draws <code>lineCount</code> lines, where each line is specified using a begin and end point
   * that are read from an array. Thus, the <i>pointPairs</i> array must have at least 2*<code>
   * lineCount</code> points.
   */
  public void drawLines(WPointF[] pointPairs, int lineCount) {
    for (int i = 0; i < lineCount; ++i) {
      this.drawLine(pointPairs[i * 2], pointPairs[i * 2 + 1]);
    }
  }
  /**
   * Draws an array of lines.
   *
   * <p>Draws the lines given in the vector.
   */
  public void drawLinesLine(final List<WLineF> lines) {
    for (int i = 0; i < lines.size(); ++i) {
      this.drawLine(lines.get(i));
    }
  }
  /**
   * Draws an array of lines.
   *
   * <p>Draws a number of lines that are specified by pairs of begin- and endpoints. The vector
   * should hold a number of points that is a multiple of two.
   */
  public void drawLinesPoint(final List<WPointF> pointPairs) {
    for (int i = 0; i < pointPairs.size() / 2; ++i) {
      this.drawLine(pointPairs.get(i * 2), pointPairs.get(i * 2 + 1));
    }
  }
  /**
   * Draws a (complex) path.
   *
   * <p>Draws and fills the given path using the current pen and brush.
   *
   * <p>
   *
   * @see WPainter#strokePath(WPainterPath path, WPen p)
   * @see WPainter#fillPath(WPainterPath path, WBrush b)
   */
  public void drawPath(final WPainterPath path) {
    this.device_.drawPath(path);
  }
  /**
   * Draws a {@link WPainterPath} on every anchor point of a path.
   *
   * <p>Draws the first {@link WPainterPath} on every anchor point of the second path. When
   * rendering to an HTML canvas, this will cause far less JavaScript to be generated than separate
   * calls to drawPath. Also, it&apos;s possible for either path to be {@link
   * WJavaScriptExposableObject#isJavaScriptBound() JavaScript bound} through, e.g. applying a
   * JavaScript bound {@link WTransform} without deforming the other path. This is used by
   * WCartesianChart to draw data series markers that don&apos;t change size when zooming in.
   *
   * <p>If one of the anchor points of the path is outside of the current clipping area, the stencil
   * will be drawn if softClipping is disabled, and it will not be drawn when softClipping is
   * enabled.
   */
  public void drawStencilAlongPath(
      final WPainterPath stencil, final WPainterPath path, boolean softClipping) {
    WCanvasPaintDevice cDevice = ObjectUtils.cast(this.device_, WCanvasPaintDevice.class);
    if (cDevice != null) {
      cDevice.drawStencilAlongPath(stencil, path, softClipping);
    } else {
      for (int i = 0; i < path.getSegments().size(); ++i) {
        final WPainterPath.Segment seg = path.getSegments().get(i);
        if (softClipping
            && !this.getClipPath().isEmpty()
            && !this.getClipPathTransform()
                .map(this.getClipPath())
                .isPointInPath(this.getWorldTransform().map(new WPointF(seg.getX(), seg.getY())))) {
          continue;
        }
        if (seg.getType() == SegmentType.LineTo
            || seg.getType() == SegmentType.MoveTo
            || seg.getType() == SegmentType.CubicEnd
            || seg.getType() == SegmentType.QuadEnd) {
          WPointF p = new WPointF(seg.getX(), seg.getY());
          this.drawPath(new WTransform().translate(p).map(stencil));
        }
      }
    }
  }
  /**
   * Draws a {@link WPainterPath} on every anchor point of a path.
   *
   * <p>Calls {@link #drawStencilAlongPath(WPainterPath stencil, WPainterPath path, boolean
   * softClipping) drawStencilAlongPath(stencil, path, false)}
   */
  public final void drawStencilAlongPath(final WPainterPath stencil, final WPainterPath path) {
    drawStencilAlongPath(stencil, path, false);
  }
  /**
   * Draws a pie.
   *
   * <p>Draws an arc using the current pen, and connects start and end point with the center of the
   * corresponding ellipse. The area is filled using the current brush.
   *
   * <p>The arc is defined as a segment from an ellipse, which fits in the <i>rectangle</i>. The
   * segment starts at <code>startAngle</code>, and spans an angle given by <code>spanAngle</code>.
   * These angles have as unit 1/16th of a degree, and are measured counter-clockwise starting at 3
   * o&apos;clock.
   *
   * <p>
   *
   * @see WPainter#drawEllipse(WRectF rectangle)
   * @see WPainter#drawArc(WRectF rectangle, int startAngle, int spanAngle)
   * @see WPainter#drawPie(double x, double y, double width, double height, int startAngle, int
   *     spanAngle)
   */
  public void drawPie(final WRectF rectangle, int startAngle, int spanAngle) {
    WTransform oldTransform = this.getWorldTransform().clone();
    this.translate(rectangle.getCenter().getX(), rectangle.getCenter().getY());
    this.scale(1., rectangle.getHeight() / rectangle.getWidth());
    WPainterPath path = new WPainterPath(new WPointF(0.0, 0.0));
    path.arcTo(0.0, 0.0, rectangle.getWidth() / 2.0, startAngle / 16., spanAngle / 16.);
    path.closeSubPath();
    this.drawPath(path);
    this.setWorldTransform(oldTransform);
  }
  /**
   * Draws a pie.
   *
   * <p>This is an overloaded method for convenience.
   *
   * <p>
   *
   * @see WPainter#drawPie(WRectF rectangle, int startAngle, int spanAngle)
   */
  public void drawPie(
      double x, double y, double width, double height, int startAngle, int spanAngle) {
    this.drawPie(new WRectF(x, y, width, height), startAngle, spanAngle);
  }
  /**
   * Draws a point.
   *
   * <p>Draws a single point using the current pen. This is implemented by drawing a very short
   * line, centered around the given <code>position</code>. To get the result of a single point, you
   * should use a pen with a Wt::PenCapStyle::Square or Wt::PenCapStyle::Round pen cap style.
   *
   * <p>
   *
   * @see WPainter#drawPoint(double x, double y)
   */
  public void drawPoint(final WPointF point) {
    this.drawPoint(point.getX(), point.getY());
  }
  /**
   * Draws a point.
   *
   * <p>This is an overloaded method for convenience.
   *
   * <p>
   *
   * @see WPainter#drawPoint(WPointF point)
   */
  public void drawPoint(double x, double y) {
    this.drawLine(x - 0.05, y - 0.05, x + 0.05, y + 0.05);
  }
  /**
   * Draws a number of points.
   *
   * <p>Draws the <code>pointCount</code> first points from the given array of points.
   *
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
   * Draws a polygon.
   *
   * <p>Draws a polygon that is specified by a list of points, using the current pen. The polygon is
   * closed by connecting the last point with the first point, and filled using the current brush.
   *
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
   * Draws a polyline.
   *
   * <p>Draws a polyline that is specified by a list of points, using the current pen.
   *
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
   * Draws a rectangle.
   *
   * <p>Draws and fills a rectangle using the current pen and brush.
   *
   * <p>
   *
   * @see WPainter#drawRect(double x, double y, double width, double height)
   */
  public void drawRect(final WRectF rectangle) {
    this.device_.drawRect(rectangle);
  }
  /**
   * Draws a rectangle.
   *
   * <p>This is an overloaded method for convenience.
   *
   * <p>
   *
   * @see WPainter#drawRect(WRectF rectangle)
   */
  public void drawRect(double x, double y, double width, double height) {
    this.drawRect(new WRectF(x, y, width, height));
  }
  /**
   * Draws a number of rectangles.
   *
   * <p>Draws and fills the <code>rectCount</code> first rectangles from the given array, using the
   * current pen and brush.
   *
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
   * Draws a number of rectangles.
   *
   * <p>Draws and fills a list of rectangles using the current pen and brush.
   *
   * <p>
   *
   * @see WPainter#drawRect(WRectF rectangle)
   */
  public void drawRects(final List<WRectF> rectangles) {
    for (int i = 0; i < rectangles.size(); ++i) {
      this.drawRect(rectangles.get(i));
    }
  }
  /**
   * Draws text.
   *
   * <p>Draws text using inside the rectangle, using the current font. The text is aligned inside
   * the rectangle following alignment indications given in <code>flags</code>. The text is drawn
   * using the current transformation, pen color ({@link WPainter#getPen() getPen()}) and font
   * settings ({@link WPainter#getFont() getFont()}).
   *
   * <p>AlignmentFlags is the logical OR of a horizontal and vertical alignment. {@link
   * Orientation#Horizontal} alignment may be one of {@link AlignmentFlag#Left}, {@link
   * AlignmentFlag#Center}, or {@link AlignmentFlag#Right}. {@link Orientation#Vertical} alignment
   * is one of {@link AlignmentFlag#Top}, {@link AlignmentFlag#Middle} or {@link
   * AlignmentFlag#Bottom}.
   *
   * <p>TextFlag determines how the text is rendered in the rectangle. Text can be rendered on one
   * line or by wrapping the words within the rectangle.
   *
   * <p>If a clipPoint is provided, the text will not be drawn if the point is outside of the {@link
   * WPainter#getClipPath() getClipPath()}.
   *
   * <p>
   *
   * <p><i><b>Note: </b>If the clip path is not a polygon, i.e. it has rounded segments in it, an
   * approximation will be used instead by having the polygon pass through the control points, and
   * the begin and end point of arcs. </i>
   *
   * <p><i><b>Note: </b>HtmlCanvas: on older browsers implementing Html5 canvas, text will be
   * rendered horizontally (unaffected by rotation and unaffected by the scaling component of the
   * transformation matrix). In that case, text is overlayed on top of painted shapes (in DOM
   * div&apos;s), and is not covered by shapes that are painted after the text. Use the SVG and VML
   * renderers (WPaintedWidget::inlineSvgVml) for the most accurate font rendering. Native HTML5
   * text rendering is supported on Firefox3+, Chrome2+ and Safari4+. </i>
   *
   * <p><i><b>Note: </b>{@link TextFlag#WordWrap}: using the {@link TextFlag#WordWrap} TextFlag is
   * currently only supported by the SVG backend. The code generated by the SVG backend uses
   * features currently only supported by Inkscape. Inkscape currently supports only {@link
   * Side#Top} vertical alignments. </i>
   */
  public void drawText(
      final WRectF rectangle,
      EnumSet<AlignmentFlag> alignmentFlags,
      TextFlag textFlag,
      final CharSequence text,
      WPointF clipPoint) {
    if (textFlag == TextFlag.SingleLine) {
      if (!!EnumUtils.mask(alignmentFlags, AlignmentFlag.AlignVerticalMask).isEmpty()) {
        alignmentFlags.add(AlignmentFlag.Top);
      }
      if (!!EnumUtils.mask(alignmentFlags, AlignmentFlag.AlignHorizontalMask).isEmpty()) {
        alignmentFlags.add(AlignmentFlag.Left);
      }
      this.device_.drawText(
          rectangle.getNormalized(), alignmentFlags, TextFlag.SingleLine, text, clipPoint);
    } else {
      if (!!EnumUtils.mask(alignmentFlags, AlignmentFlag.AlignVerticalMask).isEmpty()) {
        alignmentFlags.add(AlignmentFlag.Top);
      }
      if (!!EnumUtils.mask(alignmentFlags, AlignmentFlag.AlignHorizontalMask).isEmpty()) {
        alignmentFlags.add(AlignmentFlag.Left);
      }
      if (this.device_.getFeatures().contains(PaintDeviceFeatureFlag.WordWrap)) {
        this.device_.drawText(rectangle.getNormalized(), alignmentFlags, textFlag, text, clipPoint);
      } else {
        if (this.device_.getFeatures().contains(PaintDeviceFeatureFlag.FontMetrics)) {
        } else {
          throw new WException(
              "WPainter::drawText(): device does not support WordWrap or FontMetrics");
        }
      }
    }
  }
  /**
   * Draws text.
   *
   * <p>Calls {@link #drawText(WRectF rectangle, EnumSet alignmentFlags, TextFlag textFlag,
   * CharSequence text, WPointF clipPoint) drawText(rectangle, alignmentFlags, textFlag, text,
   * (WPointF)null)}
   */
  public final void drawText(
      final WRectF rectangle,
      EnumSet<AlignmentFlag> alignmentFlags,
      TextFlag textFlag,
      final CharSequence text) {
    drawText(rectangle, alignmentFlags, textFlag, text, (WPointF) null);
  }

  public void drawTextOnPath(
      final WRectF rect,
      EnumSet<AlignmentFlag> alignmentFlags,
      final List<WString> text,
      final WTransform transform,
      final WPainterPath path,
      double angle,
      double lineHeight,
      boolean softClipping) {
    if (!!EnumUtils.mask(alignmentFlags, AlignmentFlag.AlignVerticalMask).isEmpty()) {
      alignmentFlags.add(AlignmentFlag.Top);
    }
    if (!!EnumUtils.mask(alignmentFlags, AlignmentFlag.AlignHorizontalMask).isEmpty()) {
      alignmentFlags.add(AlignmentFlag.Left);
    }
    WCanvasPaintDevice cDevice = ObjectUtils.cast(this.device_, WCanvasPaintDevice.class);
    if (cDevice != null) {
      cDevice.drawTextOnPath(
          rect, alignmentFlags, text, transform, path, angle, lineHeight, softClipping);
    } else {
      WPainterPath tpath = transform.map(path);
      for (int i = 0; i < path.getSegments().size(); ++i) {
        if (i >= text.size()) {
          break;
        }
        final WPainterPath.Segment seg = path.getSegments().get(i);
        final WPainterPath.Segment tseg = tpath.getSegments().get(i);
        List<WString> splitText = splitLabel(text.get(i));
        if (seg.getType() == SegmentType.MoveTo
            || seg.getType() == SegmentType.LineTo
            || seg.getType() == SegmentType.QuadEnd
            || seg.getType() == SegmentType.CubicEnd) {
          this.save();
          this.setClipping(false);
          this.translate(tseg.getX(), tseg.getY());
          this.rotate(-angle);
          for (int j = 0; j < splitText.size(); ++j) {
            double yOffset =
                calcYOffset(
                    j,
                    splitText.size(),
                    lineHeight,
                    EnumUtils.mask(alignmentFlags, AlignmentFlag.AlignVerticalMask));
            WPointF p = new WPointF(tseg.getX(), tseg.getY());
            this.drawText(
                new WRectF(
                    rect.getLeft(), rect.getTop() + yOffset, rect.getWidth(), rect.getHeight()),
                alignmentFlags,
                TextFlag.SingleLine,
                splitText.get(j),
                softClipping ? p : null);
          }
          this.restore();
        }
      }
    }
  }
  /**
   * Draws text.
   *
   * <p>This is an overloaded method for convenience, it will render text on a single line.
   *
   * <p>
   */
  public void drawText(
      final WRectF rectangle, EnumSet<AlignmentFlag> flags, final CharSequence text) {
    if (!!EnumUtils.mask(flags, AlignmentFlag.AlignVerticalMask).isEmpty()) {
      flags.add(AlignmentFlag.Top);
    }
    if (!!EnumUtils.mask(flags, AlignmentFlag.AlignHorizontalMask).isEmpty()) {
      flags.add(AlignmentFlag.Left);
    }
    this.device_.drawText(
        rectangle.getNormalized(), flags, TextFlag.SingleLine, text, (WPointF) null);
  }
  /**
   * Draws text.
   *
   * <p>This is an overloaded method for convenience.
   *
   * <p>
   *
   * @see WPainter#drawText(WRectF rectangle, EnumSet flags, CharSequence text)
   */
  public void drawText(
      double x,
      double y,
      double width,
      double height,
      EnumSet<AlignmentFlag> flags,
      final CharSequence text) {
    this.drawText(new WRectF(x, y, width, height), flags, text);
  }
  /**
   * Draws text.
   *
   * <p>This is an overloaded method for convenience.
   *
   * <p>
   *
   * @see WPainter#drawText(WRectF rectangle, EnumSet alignmentFlags, TextFlag textFlag,
   *     CharSequence text, WPointF clipPoint)
   * @see WRectF
   * @see TextFlag
   * @see WString
   */
  public void drawText(
      double x,
      double y,
      double width,
      double height,
      EnumSet<AlignmentFlag> alignmentFlags,
      TextFlag textFlag,
      final CharSequence text) {
    this.drawText(new WRectF(x, y, width, height), alignmentFlags, textFlag, text);
  }
  /**
   * Fills a (complex) path.
   *
   * <p>Like {@link WPainter#drawPath(WPainterPath path) drawPath()}, but does not stroke the path,
   * and fills the path with the given <code>brush</code>.
   *
   * <p>
   *
   * @see WPainter#drawPath(WPainterPath path)
   * @see WPainter#strokePath(WPainterPath path, WPen p)
   */
  public void fillPath(final WPainterPath path, final WBrush b) {
    WBrush oldBrush = this.getBrush().clone();
    WPen oldPen = this.getPen().clone();
    this.setBrush(b);
    this.setPen(new WPen(PenStyle.None));
    this.drawPath(path);
    this.setBrush(oldBrush);
    this.setPen(oldPen);
  }
  /**
   * Fills a rectangle.
   *
   * <p>Like {@link WPainter#drawRect(WRectF rectangle) drawRect()}, but does not stroke the rect,
   * and fills the rect with the given <code>brush</code>.
   *
   * <p>
   *
   * @see WPainter#drawRect(WRectF rectangle)
   */
  public void fillRect(final WRectF rectangle, final WBrush b) {
    WBrush oldBrush = this.getBrush().clone();
    WPen oldPen = this.getPen().clone();
    this.setBrush(b);
    this.setPen(new WPen(PenStyle.None));
    this.drawRect(rectangle);
    this.setBrush(oldBrush);
    this.setPen(oldPen);
  }
  /**
   * Fills a rectangle.
   *
   * <p>This is an overloaded method for convenience.
   *
   * <p>
   *
   * @see WPainter#fillRect(WRectF rectangle, WBrush b)
   */
  public void fillRect(double x, double y, double width, double height, final WBrush brush) {
    this.fillRect(new WRectF(x, y, width, height), brush);
  }
  /**
   * Strokes a path.
   *
   * <p>Like {@link WPainter#drawPath(WPainterPath path) drawPath()}, but does not fill the path,
   * and strokes the path with the given <code>pen</code>.
   *
   * <p>
   *
   * @see WPainter#drawPath(WPainterPath path)
   * @see WPainter#fillPath(WPainterPath path, WBrush b)
   */
  public void strokePath(final WPainterPath path, final WPen p) {
    WBrush oldBrush = this.getBrush().clone();
    WPen oldPen = this.getPen().clone();
    this.setBrush(new WBrush());
    this.setPen(p);
    this.drawPath(path);
    this.setBrush(oldBrush);
    this.setPen(oldPen);
  }
  /**
   * Sets a shadow effect.
   *
   * <p>The shadow effect is applied to all things drawn (paths, text and images).
   *
   * <p>
   *
   * <p><i><b>Note: </b>With the VML backend (IE), the shadow is not applied to images, and the
   * shadow color is always black; only the opacity (alpha) channel is taken into account. </i>
   */
  public void setShadow(final WShadow shadow) {
    if (!this.getShadow().equals(shadow)) {
      this.getS().currentShadow_ = shadow;
      this.device_.setChanged(EnumSet.of(PainterChangeFlag.Shadow));
    }
  }
  /**
   * Returns the current shadow effect.
   *
   * <p>
   *
   * @see WPainter#setShadow(WShadow shadow)
   */
  public WShadow getShadow() {
    return this.getS().currentShadow_;
  }
  /**
   * Sets the fill style.
   *
   * <p>Changes the fills style for subsequent draw operations.
   *
   * <p>
   *
   * @see WPainter#getBrush()
   * @see WPainter#setPen(WPen p)
   */
  public void setBrush(final WBrush b) {
    if (!this.getBrush().equals(b)) {
      this.getS().currentBrush_ = b;
      this.device_.setChanged(EnumSet.of(PainterChangeFlag.Brush));
    }
  }
  /**
   * Sets the font.
   *
   * <p>Changes the font for subsequent text rendering. Note that only font sizes that are defined
   * as an explicit size (see {@link FontSize#FixedSize}) will render correctly in all devices (SVG,
   * VML, and HtmlCanvas).
   *
   * <p>The list of fonts that will render correctly with VML (on IE&lt;9) are limited to the
   * following: <a
   * href="http://www.ampsoft.net/webdesign-l/WindowsMacFonts.html">http://www.ampsoft.net/webdesign-l/WindowsMacFonts.html</a>
   *
   * <p>Careful, for a font family that contains a space, you need to add quotes, to {@link
   * WFont#setFamily(FontFamily genericFamily, CharSequence specificFamilies) WFont#setFamily()}
   * e.g.
   *
   * <p>
   *
   * <pre>{@code
   * WFont mono;
   * mono.setFamily(FontFamily::Monospace, "'Courier New'");
   * mono.setSize(18);
   *
   * }</pre>
   *
   * <p>
   *
   * @see WPainter#getFont()
   * @see WPainter#drawText(WRectF rectangle, EnumSet alignmentFlags, TextFlag textFlag,
   *     CharSequence text, WPointF clipPoint)
   */
  public void setFont(final WFont f) {
    if (!this.getFont().equals(f)) {
      this.getS().currentFont_ = f;
      this.device_.setChanged(EnumSet.of(PainterChangeFlag.Font));
    }
  }
  /**
   * Sets the pen.
   *
   * <p>Changes the pen used for stroking subsequent draw operations.
   *
   * <p>
   *
   * @see WPainter#getPen()
   * @see WPainter#setBrush(WBrush b)
   */
  public void setPen(final WPen p) {
    if (!this.getPen().equals(p)) {
      this.getS().currentPen_ = p;
      this.device_.setChanged(EnumSet.of(PainterChangeFlag.Pen));
    }
  }
  /**
   * Returns the current brush.
   *
   * <p>Returns the brush style that is currently used for filling.
   *
   * <p>
   *
   * @see WPainter#setBrush(WBrush b)
   */
  public WBrush getBrush() {
    return this.getS().currentBrush_;
  }
  /**
   * Returns the current font.
   *
   * <p>Returns the font that is currently used for rendering text. The default font is a 10pt sans
   * serif font.
   *
   * <p>
   *
   * @see WPainter#setFont(WFont f)
   */
  public WFont getFont() {
    return this.getS().currentFont_;
  }
  /**
   * Returns the current pen.
   *
   * <p>Returns the pen that is currently used for stroking.
   *
   * <p>
   *
   * @see WPainter#setPen(WPen p)
   */
  public WPen getPen() {
    return this.getS().currentPen_;
  }
  /**
   * Enables or disables clipping.
   *
   * <p>Enables are disables clipping for subsequent operations using the current clip path set
   * using {@link WPainter#setClipPath(WPainterPath clipPath) setClipPath()}.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Clipping support is limited for the VML renderer. Only clipping with a
   * rectangle is supported for the VML renderer (see {@link WPainterPath#addRect(WRectF rectangle)
   * addRect()}). The rectangle must, after applying the combined transformation system, be aligned
   * with the window. </i>
   *
   * @see WPainter#hasClipping()
   * @see WPainter#setClipPath(WPainterPath clipPath)
   */
  public void setClipping(boolean enable) {
    if (this.getS().clipping_ != enable) {
      this.getS().clipping_ = enable;
      if (this.device_ != null) {
        this.device_.setChanged(EnumSet.of(PainterChangeFlag.Clipping));
      }
    }
  }
  /**
   * Returns whether clipping is enabled.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Clipping support is limited for the VML renderer. </i>
   *
   * @see WPainter#setClipping(boolean enable)
   * @see WPainter#setClipPath(WPainterPath clipPath)
   */
  public boolean hasClipping() {
    return this.getS().clipping_;
  }
  /**
   * Sets the clip path.
   *
   * <p>Sets the path that is used for clipping subsequent drawing operations. The clip path is only
   * used when clipping is enabled using {@link WPainter#setClipping(boolean enable) setClipping()}.
   * The path is specified in local coordinates.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Clipping support is limited for the VML renderer. </i>
   *
   * @see WPainter#getClipPath()
   * @see WPainter#setClipping(boolean enable)
   */
  public void setClipPath(final WPainterPath clipPath) {
    this.getS().clipPath_.assign(clipPath);
    this.getS().clipPathTransform_.assign(this.getCombinedTransform());
    if (this.getS().clipping_ && this.device_ != null) {
      this.device_.setChanged(EnumSet.of(PainterChangeFlag.Clipping));
    }
  }
  /**
   * Returns the clip path.
   *
   * <p>The clip path is returned as it was defined: in the local coordinates at time of definition.
   *
   * <p>
   *
   * @see WPainter#setClipPath(WPainterPath clipPath)
   */
  public WPainterPath getClipPath() {
    return this.getS().clipPath_;
  }
  /**
   * Resets the current transformation.
   *
   * <p>Resets the current transformation to the identity transformation matrix, so that the logical
   * coordinate system coincides with the device coordinate system.
   */
  public void resetTransform() {
    this.getS().worldTransform_.reset();
    if (this.device_ != null) {
      this.device_.setChanged(EnumSet.of(PainterChangeFlag.Transform));
    }
  }
  /**
   * Rotates the logical coordinate system.
   *
   * <p>Rotates the logical coordinate system around its origin. The <code>angle</code> is specified
   * in degrees, and positive values are clock-wise.
   *
   * <p>
   *
   * @see WPainter#scale(double sx, double sy)
   * @see WPainter#translate(double dx, double dy)
   * @see WPainter#resetTransform()
   */
  public void rotate(double angle) {
    this.getS().worldTransform_.rotate(angle);
    if (this.device_ != null) {
      this.device_.setChanged(EnumSet.of(PainterChangeFlag.Transform));
    }
  }
  /**
   * Scales the logical coordinate system.
   *
   * <p>Scales the logical coordinate system around its origin, by a factor in the X and Y
   * directions.
   *
   * <p>
   *
   * @see WPainter#rotate(double angle)
   * @see WPainter#translate(double dx, double dy)
   * @see WPainter#resetTransform()
   */
  public void scale(double sx, double sy) {
    this.getS().worldTransform_.scale(sx, sy);
    if (this.device_ != null) {
      this.device_.setChanged(EnumSet.of(PainterChangeFlag.Transform));
    }
  }
  /**
   * Translates the origin of the logical coordinate system.
   *
   * <p>Translates the origin of the logical coordinate system to a new location relative to the
   * current logical coordinate system.
   *
   * <p>
   *
   * @see WPainter#translate(double dx, double dy)
   * @see WPainter#rotate(double angle)
   * @see WPainter#scale(double sx, double sy)
   * @see WPainter#resetTransform()
   */
  public void translate(final WPointF p) {
    this.getS().worldTransform_.translate(p);
    if (this.device_ != null) {
      this.device_.setChanged(EnumSet.of(PainterChangeFlag.Transform));
    }
  }
  /**
   * Translates the origin of the logical coordinate system.
   *
   * <p>Translates the origin of the logical coordinate system to a new location relative to the
   * logical coordinate system.
   *
   * <p>
   *
   * @see WPainter#translate(WPointF p)
   * @see WPainter#rotate(double angle)
   * @see WPainter#scale(double sx, double sy)
   * @see WPainter#resetTransform()
   */
  public void translate(double dx, double dy) {
    this.translate(new WPointF(dx, dy));
  }
  /**
   * Sets a transformation for the logical coordinate system.
   *
   * <p>Sets a new transformation which transforms logical coordinates to device coordinates. When
   * <code>combine</code> is <code>true</code>, the transformation is combined with the current
   * world transformation matrix.
   *
   * <p>
   *
   * @see WPainter#getWorldTransform()
   * @see WPainter#rotate(double angle)
   * @see WPainter#scale(double sx, double sy)
   * @see WPainter#translate(double dx, double dy)
   * @see WPainter#resetTransform()
   */
  public void setWorldTransform(final WTransform matrix, boolean combine) {
    if (combine) {
      this.getS().worldTransform_.multiplyAndAssign(matrix);
    } else {
      this.getS().worldTransform_.assign(matrix);
    }
    if (this.device_ != null) {
      this.device_.setChanged(EnumSet.of(PainterChangeFlag.Transform));
    }
  }
  /**
   * Sets a transformation for the logical coordinate system.
   *
   * <p>Calls {@link #setWorldTransform(WTransform matrix, boolean combine)
   * setWorldTransform(matrix, false)}
   */
  public final void setWorldTransform(final WTransform matrix) {
    setWorldTransform(matrix, false);
  }
  /**
   * Returns the current world transformation matrix.
   *
   * <p>
   *
   * @see WPainter#setWorldTransform(WTransform matrix, boolean combine)
   */
  public WTransform getWorldTransform() {
    return this.getS().worldTransform_;
  }
  /**
   * Saves the current state.
   *
   * <p>A copy of the current state is saved on a stack. This state will may later be restored by
   * popping this state from the stack using {@link WPainter#restore() restore()}.
   *
   * <p>The state that is saved is the current {@link WPainter#setPen(WPen p) pen}, {@link
   * WPainter#setBrush(WBrush b) brush}, {@link WPainter#setFont(WFont f) font}, {@link
   * WPainter#getShadow() getShadow()}, {@link WPainter#getWorldTransform() transformation} and
   * clipping settings (see {@link WPainter#setClipping(boolean enable) setClipping()} and {@link
   * WPainter#setClipPath(WPainterPath clipPath) setClipPath()}).
   *
   * <p>
   *
   * @see WPainter#restore()
   */
  public void save() {
    this.stateStack_.add(this.stateStack_.get(this.stateStack_.size() - 1).clone());
  }
  /**
   * Returns the last save state.
   *
   * <p>Pops the last saved state from the state stack.
   *
   * <p>
   *
   * @see WPainter#save()
   */
  public void restore() {
    if (this.stateStack_.size() > 1) {
      EnumSet<PainterChangeFlag> flags = EnumSet.noneOf(PainterChangeFlag.class);
      final WPainter.State last = this.stateStack_.get(this.stateStack_.size() - 1);
      final WPainter.State next = this.stateStack_.get(this.stateStack_.size() - 2);
      if (!last.worldTransform_.equals(next.worldTransform_)) {
        flags.add(PainterChangeFlag.Transform);
      }
      if (!last.currentBrush_.equals(next.currentBrush_)) {
        flags.add(PainterChangeFlag.Brush);
      }
      if (!last.currentFont_.equals(next.currentFont_)) {
        flags.add(PainterChangeFlag.Font);
      }
      if (!last.currentPen_.equals(next.currentPen_)) {
        flags.add(PainterChangeFlag.Pen);
      }
      if (!last.currentShadow_.equals(next.currentShadow_)) {
        flags.add(PainterChangeFlag.Shadow);
      }
      if (!last.renderHints_.equals(next.renderHints_)) {
        flags.add(PainterChangeFlag.Hints);
      }
      if (!last.clipPath_.equals(next.clipPath_)) {
        flags.add(PainterChangeFlag.Clipping);
      }
      if (last.clipping_ != next.clipping_) {
        flags.add(PainterChangeFlag.Clipping);
      }
      this.stateStack_.remove(0 + this.stateStack_.size() - 1);
      if (!flags.isEmpty() && this.device_ != null) {
        this.device_.setChanged(flags);
      }
    }
  }
  /**
   * Sets the viewport.
   *
   * <p>Selects the part of the device that will correspond to the logical coordinate system.
   *
   * <p>By default, the viewport spans the entire device: it is the rectangle (0, 0) to
   * (device.width(), device.height()). The window defines how the viewport is mapped to logical
   * coordinates.
   *
   * <p>
   *
   * @see WPainter#getViewPort()
   * @see WPainter#setWindow(WRectF window)
   */
  public void setViewPort(final WRectF viewPort) {
    this.viewPort_ = viewPort;
    this.recalculateViewTransform();
  }
  /**
   * Sets the viewport.
   *
   * <p>This is an overloaded method for convenience.
   *
   * <p>
   *
   * @see WPainter#setViewPort(WRectF viewPort)
   */
  public void setViewPort(double x, double y, double width, double height) {
    this.setViewPort(new WRectF(x, y, width, height));
  }
  /**
   * Returns the viewport.
   *
   * <p>
   *
   * @see WPainter#setViewPort(WRectF viewPort)
   */
  public WRectF getViewPort() {
    return this.viewPort_;
  }
  /**
   * Sets the window.
   *
   * <p>Defines the viewport rectangle in logical coordinates, and thus how logical coordinates map
   * onto the viewPort.
   *
   * <p>By default, is (0, 0) to (device.width(), device.height()). Thus, the default window and
   * viewport leave logical coordinates identical to device coordinates.
   *
   * <p>
   *
   * @see WPainter#getWindow()
   * @see WPainter#setViewPort(WRectF viewPort)
   */
  public void setWindow(final WRectF window) {
    this.window_ = window;
    this.recalculateViewTransform();
  }
  /**
   * Sets the window.
   *
   * <p>This is an overloaded method for convenience.
   *
   * <p>
   *
   * @see WPainter#setWindow(WRectF window)
   */
  public void setWindow(double x, double y, double width, double height) {
    this.setWindow(new WRectF(x, y, width, height));
  }
  /**
   * Returns the current window.
   *
   * <p>
   *
   * @see WPainter#setViewPort(WRectF viewPort)
   */
  public WRectF getWindow() {
    return this.window_;
  }
  /**
   * Returns the combined transformation matrix.
   *
   * <p>Returns the transformation matrix that maps coordinates to device coordinates. It is the
   * combination of the current world transformation (which defines the transformation within the
   * logical coordinate system) and the window/viewport transformation (which transforms logical
   * coordinates to device coordinates).
   *
   * <p>
   *
   * @see WPainter#setWorldTransform(WTransform matrix, boolean combine)
   * @see WPainter#setViewPort(WRectF viewPort)
   * @see WPainter#setWindow(WRectF window)
   */
  public WTransform getCombinedTransform() {
    return this.viewTransform_.multiply(this.getS().worldTransform_);
  }

  WTransform getClipPathTransform() {
    return this.getS().clipPathTransform_;
  }

  WLength normalizedPenWidth(final WLength penWidth, boolean correctCosmetic) {
    double w = penWidth.getValue();
    if (w == 0 && correctCosmetic) {
      final WTransform t = this.getCombinedTransform();
      if (!t.isIdentity()) {
        WTransform.TRSRDecomposition d = new WTransform.TRSRDecomposition();
        t.decomposeTranslateRotateScaleRotate(d);
        w = 2.0 / (Math.abs(d.sx) + Math.abs(d.sy));
      } else {
        w = 1.0;
      }
      return new WLength(w, LengthUnit.Pixel);
    } else {
      if (w != 0 && !correctCosmetic) {
        final WTransform t = this.getCombinedTransform();
        if (!t.isIdentity()) {
          WTransform.TRSRDecomposition d = new WTransform.TRSRDecomposition();
          t.decomposeTranslateRotateScaleRotate(d);
          w *= (Math.abs(d.sx) + Math.abs(d.sy)) / 2.0;
        }
        return new WLength(w, LengthUnit.Pixel);
      } else {
        return penWidth;
      }
    }
  }
  // private  WPainter(final WPainter anon1) ;
  private WPaintDevice device_;
  private WRectF viewPort_;
  private WRectF window_;
  private WTransform viewTransform_;

  static class State {
    private static Logger logger = LoggerFactory.getLogger(State.class);

    public WTransform worldTransform_;
    public WBrush currentBrush_;
    public WFont currentFont_;
    public WPen currentPen_;
    public WShadow currentShadow_;
    public EnumSet<RenderHint> renderHints_;
    public WPainterPath clipPath_;
    public WTransform clipPathTransform_;
    public boolean clipping_;

    public State() {
      this.worldTransform_ = new WTransform();
      this.currentBrush_ = new WBrush();
      this.currentFont_ = new WFont();
      this.currentPen_ = new WPen();
      this.currentShadow_ = new WShadow();
      this.renderHints_ = EnumSet.noneOf(RenderHint.class);
      this.clipPath_ = new WPainterPath();
      this.clipPathTransform_ = new WTransform();
      this.clipping_ = false;
      this.currentFont_.setFamily(FontFamily.SansSerif);
      this.currentFont_.setSize(new WLength(10, LengthUnit.Point));
    }

    public WPainter.State clone() {
      WPainter.State result = new WPainter.State();
      result.worldTransform_.assign(this.worldTransform_);
      result.currentBrush_ = this.currentBrush_;
      result.currentFont_ = this.currentFont_;
      result.currentPen_ = this.currentPen_;
      result.currentShadow_ = this.currentShadow_;
      result.renderHints_.clear();
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
    this.viewTransform_.translate(
        this.viewPort_.getX() - this.window_.getX() * scaleX,
        this.viewPort_.getY() - this.window_.getY() * scaleY);
    this.viewTransform_.scale(scaleX, scaleY);
    if (this.device_ != null) {
      this.device_.setChanged(EnumSet.of(PainterChangeFlag.Transform));
    }
  }
  // private void drawMultilineText(final WRectF rect, EnumSet<AlignmentFlag> alignmentFlags, final
  // CharSequence text) ;
  static List<WString> splitLabel(CharSequence text) {
    String s = text.toString();
    List<String> splitText = new ArrayList<String>();
    StringUtils.split(splitText, s, "\n", false);
    List<WString> result = new ArrayList<WString>();
    for (int i = 0; i < splitText.size(); ++i) {
      result.add(new WString(splitText.get(i)));
    }
    return result;
  }

  static double calcYOffset(
      int lineNb, int nbLines, double lineHeight, EnumSet<AlignmentFlag> verticalAlign) {
    if (verticalAlign.equals(AlignmentFlag.Middle)) {
      return -((nbLines - 1) * lineHeight / 2.0) + lineNb * lineHeight;
    } else {
      if (verticalAlign.equals(AlignmentFlag.Top)) {
        return lineNb * lineHeight;
      } else {
        if (verticalAlign.equals(AlignmentFlag.Bottom)) {
          return -(nbLines - 1 - lineNb) * lineHeight;
        } else {
          return 0;
        }
      }
    }
  }

  private static final double calcYOffset(
      int lineNb,
      int nbLines,
      double lineHeight,
      AlignmentFlag verticalAlig,
      AlignmentFlag... verticalAlign) {
    return calcYOffset(lineNb, nbLines, lineHeight, EnumSet.of(verticalAlig, verticalAlign));
  }
}
