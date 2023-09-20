/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

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
 * A paint device for rendering using Scalable Vector Graphics (SVG).
 *
 * <p>The WSvgImage is primarily used by {@link WPaintedWidget} to render to the browser in Support
 * Vector Graphics (SVG) format.
 */
public class WSvgImage extends WResource implements WVectorImage {
  private static Logger logger = LoggerFactory.getLogger(WSvgImage.class);

  /**
   * Create an SVG paint device.
   *
   * <p>If <code>paintUpdate</code> is <code>true</code>, then only an SVG fragment will be rendered
   * that can be used to update the DOM of an existing SVG image, instead of a full SVG image.
   */
  public WSvgImage(final WLength width, final WLength height, boolean paintUpdate) {
    super();
    this.width_ = width;
    this.height_ = height;
    this.painter_ = null;
    this.paintUpdate_ = paintUpdate;
    this.changeFlags_ = EnumSet.noneOf(PainterChangeFlag.class);
    this.newGroup_ = true;
    this.newClipPath_ = false;
    this.busyWithPath_ = false;
    this.currentClipId_ = -1;
    this.currentFillGradientId_ = -1;
    this.currentStrokeGradientId_ = -1;
    this.currentTransform_ = new WTransform();
    this.currentBrush_ = new WBrush();
    this.currentFont_ = new WFont();
    this.currentPen_ = new WPen();
    this.currentShadow_ = new WShadow();
    this.currentShadowId_ = -1;
    this.nextShadowId_ = 0;
    this.pathTranslation_ = new WPointF();
    this.pathBoundingBox_ = null;
    this.shapes_ = new StringBuilder();
    this.fontMetrics_ = null;
    this.fillStyle_ = "";
    this.strokeStyle_ = "";
    this.fontStyle_ = "";
  }
  /**
   * Create an SVG paint device.
   *
   * <p>Calls {@link #WSvgImage(WLength width, WLength height, boolean paintUpdate) this(width,
   * height, false)}
   */
  public WSvgImage(final WLength width, final WLength height) {
    this(width, height, false);
  }

  public EnumSet<PaintDeviceFeatureFlag> getFeatures() {
    if (ServerSideFontMetrics.isAvailable()) {
      return EnumSet.of(PaintDeviceFeatureFlag.FontMetrics, PaintDeviceFeatureFlag.WordWrap);
    } else {
      return EnumSet.of(PaintDeviceFeatureFlag.WordWrap);
    }
  }

  public void setChanged(EnumSet<PainterChangeFlag> flags) {
    if (!flags.isEmpty()) {
      this.newGroup_ = true;
    }
    if (flags.contains(PainterChangeFlag.Clipping)) {
      this.newClipPath_ = true;
    }
    this.changeFlags_.addAll(flags);
  }

  public void drawArc(final WRectF rect, double startAngle, double spanAngle) {
    char[] buf = new char[30];
    if (Math.abs(spanAngle - 360.0) < 0.01 || spanAngle > 360.0) {
      this.finishPath();
      this.makeNewGroup();
      this.shapes_
          .append("<ellipse ")
          .append(" cx=\"")
          .append(MathUtils.roundJs(rect.getCenter().getX(), 3));
      this.shapes_.append("\" cy=\"").append(MathUtils.roundJs(rect.getCenter().getY(), 3));
      this.shapes_.append("\" rx=\"").append(MathUtils.roundJs(rect.getWidth() / 2, 3));
      this.shapes_
          .append("\" ry=\"")
          .append(MathUtils.roundJs(rect.getHeight() / 2, 3))
          .append("\" />");
    } else {
      WPainterPath path = new WPainterPath();
      path.arcMoveTo(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), startAngle);
      path.arcTo(
          rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), startAngle, spanAngle);
      this.drawPath(path);
    }
  }

  public void drawImage(
      final WRectF rect, final String imgUri, int imgWidth, int imgHeight, final WRectF srect) {
    this.finishPath();
    this.makeNewGroup();
    WApplication app = WApplication.getInstance();
    String imageUri = imgUri;
    if (app != null) {
      imageUri = app.resolveRelativeUrl(imgUri);
    }
    WRectF drect = rect;
    char[] buf = new char[30];
    boolean transformed = false;
    if (drect.getWidth() != srect.getWidth() || drect.getHeight() != srect.getHeight()) {
      this.shapes_
          .append("<g transform=\"matrix(")
          .append(MathUtils.roundJs(drect.getWidth() / srect.getWidth(), 3));
      this.shapes_
          .append(" 0 0 ")
          .append(MathUtils.roundJs(drect.getHeight() / srect.getHeight(), 3));
      this.shapes_.append(' ').append(MathUtils.roundJs(drect.getX(), 3));
      this.shapes_.append(' ').append(MathUtils.roundJs(drect.getY(), 3)).append(")\">");
      drect = new WRectF(0, 0, srect.getWidth(), srect.getHeight());
      transformed = true;
    }
    double scaleX = drect.getWidth() / srect.getWidth();
    double scaleY = drect.getHeight() / srect.getHeight();
    double x = drect.getX() - srect.getX() * scaleX;
    double y = drect.getY() - srect.getY() * scaleY;
    double width = imgWidth;
    double height = imgHeight;
    boolean useClipPath = false;
    int imgClipId = nextClipId_++;
    if (!new WRectF(x, y, width, height).equals(drect)) {
      this.shapes_.append("<clipPath id=\"imgClip").append(imgClipId).append("\">");
      this.shapes_.append("<rect x=\"").append(MathUtils.roundJs(drect.getX(), 3)).append('"');
      this.shapes_.append(" y=\"").append(MathUtils.roundJs(drect.getY(), 3)).append('"');
      this.shapes_.append(" width=\"").append(MathUtils.roundJs(drect.getWidth(), 3)).append('"');
      this.shapes_.append(" height=\"").append(MathUtils.roundJs(drect.getHeight(), 3)).append('"');
      this.shapes_.append(" /></clipPath>");
      useClipPath = true;
    }
    this.shapes_.append("<image xlink:href=\"").append(imageUri).append("\"");
    this.shapes_.append(" x=\"").append(MathUtils.roundJs(x, 3)).append('"');
    this.shapes_.append(" y=\"").append(MathUtils.roundJs(y, 3)).append('"');
    this.shapes_.append(" width=\"").append(MathUtils.roundJs(width, 3)).append('"');
    this.shapes_.append(" height=\"").append(MathUtils.roundJs(height, 3)).append('"');
    if (useClipPath) {
      this.shapes_.append(" clip-path=\"url(#imgClip").append(imgClipId).append(")\"");
    }
    this.shapes_.append("/>");
    if (transformed) {
      this.shapes_.append("</g>");
    }
  }

  public void drawLine(double x1, double y1, double x2, double y2) {
    WPainterPath path = new WPainterPath();
    path.moveTo(x1, y1);
    path.lineTo(x2, y2);
    this.drawPath(path);
  }

  public void drawRect(final WRectF rectangle) {
    this.drawPath(rectangle.toPath());
  }

  public void drawPath(final WPainterPath path) {
    if (path.isEmpty()) {
      return;
    }
    WRectF bbox = this.getPainter().getWorldTransform().map(path.getControlPointRect());
    if (this.busyWithPath_) {
      if (this.pathBoundingBox_.intersects(bbox)) {
        this.finishPath();
      } else {
        this.pathBoundingBox_ = this.pathBoundingBox_.united(bbox);
      }
    } else {
      this.pathBoundingBox_ = bbox;
    }
    this.makeNewGroup();
    this.drawPlainPath(this.shapes_, path);
  }

  public void drawText(
      final WRectF rect,
      EnumSet<AlignmentFlag> flags,
      TextFlag textFlag,
      final CharSequence text,
      WPointF clipPoint) {
    if (clipPoint != null
        && this.getPainter() != null
        && !this.getPainter().getClipPath().isEmpty()) {
      if (!this.getPainter()
          .getClipPathTransform()
          .map(this.getPainter().getClipPath())
          .isPointInPath(this.getPainter().getWorldTransform().map(clipPoint))) {
        return;
      }
    }
    this.finishPath();
    this.makeNewGroup();
    char[] buf = new char[30];
    StringBuilder style = new StringBuilder();
    style.append("style=\"stroke:none;");
    if (!this.getPainter().getPen().getColor().equals(this.getPainter().getBrush().getColor())
        || this.getPainter().getBrush().getStyle() == BrushStyle.None) {
      final WColor color = this.getPainter().getPen().getColor();
      style
          .append("fill:" + color.getCssText(false))
          .append(';')
          .append("fill-opacity:")
          .append(MathUtils.roundCss(color.getAlpha() / 255., 3))
          .append(';');
    }
    style.append('"');
    AlignmentFlag horizontalAlign =
        EnumUtils.enumFromSet(EnumUtils.mask(flags, AlignmentFlag.AlignHorizontalMask));
    AlignmentFlag verticalAlign =
        EnumUtils.enumFromSet(EnumUtils.mask(flags, AlignmentFlag.AlignVerticalMask));
    if (textFlag == TextFlag.WordWrap) {
      String hAlign = "";
      switch (horizontalAlign) {
        case Left:
          hAlign = "start";
          break;
        case Right:
          hAlign = "end";
          break;
        case Center:
          hAlign = "center";
          break;
        case Justify:
          hAlign = "justify";
        default:
          break;
      }
      this.shapes_
          .append("<flowRoot ")
          .append(style.toString())
          .append(">\n")
          .append("  <flowRegion>\n")
          .append("    <rect")
          .append(" width=\"")
          .append(rect.getWidth())
          .append("\"")
          .append(" height=\"")
          .append(rect.getHeight())
          .append("\"")
          .append(" x=\"")
          .append(rect.getX())
          .append("\"")
          .append(" y=\"")
          .append(rect.getY())
          .append("\"")
          .append("    />\n")
          .append("  </flowRegion>\n")
          .append("  <flowPara")
          .append(" text-align=\"")
          .append(hAlign)
          .append("\">\n")
          .append(" ")
          .append(WWebWidget.escapeText(text, false).toString())
          .append("\n")
          .append("  </flowPara>\n")
          .append("</flowRoot>\n");
    } else {
      this.shapes_.append("<text ").append(style.toString());
      switch (horizontalAlign) {
        case Left:
          this.shapes_.append(" x=").append(quote(rect.getLeft()));
          break;
        case Right:
          this.shapes_.append(" x=").append(quote(rect.getRight())).append(" text-anchor=\"end\"");
          break;
        case Center:
          this.shapes_
              .append(" x=")
              .append(quote(rect.getCenter().getX()))
              .append(" text-anchor=\"middle\"");
          break;
        default:
          break;
      }
      double fontSize = this.getPainter().getFont().getSizeLength(16).toPixels();
      double y = rect.getCenter().getY();
      switch (verticalAlign) {
        case Top:
          y = rect.getTop() + fontSize * 0.75;
          break;
        case Middle:
          y = rect.getCenter().getY() + fontSize * 0.25;
          break;
        case Bottom:
          y = rect.getBottom() - fontSize * 0.25;
          break;
        default:
          break;
      }
      this.shapes_.append(" y=").append(quote(y));
      this.shapes_
          .append(">")
          .append(WWebWidget.escapeText(text, false).toString())
          .append("</text>");
    }
  }

  public WTextItem measureText(final CharSequence text, double maxWidth, boolean wordWrap) {
    if (!(this.fontMetrics_ != null)) {
      this.fontMetrics_ = new ServerSideFontMetrics();
    }
    return this.fontMetrics_.measureText(this.getPainter().getFont(), text, maxWidth, wordWrap);
  }

  public WFontMetrics getFontMetrics() {
    if (!(this.fontMetrics_ != null)) {
      this.fontMetrics_ = new ServerSideFontMetrics();
    }
    return this.fontMetrics_.fontMetrics(this.getPainter().getFont());
  }

  public void init() {
    this.currentBrush_ = this.getPainter().getBrush();
    this.currentPen_ = this.getPainter().getPen();
    this.currentFont_ = this.getPainter().getFont();
    this.strokeStyle_ = this.getStrokeStyle();
    this.fillStyle_ = this.getFillStyle();
    this.fontStyle_ = this.getFontStyle();
    this.newClipPath_ = true;
  }

  public void done() {
    this.finishPath();
  }

  public boolean isPaintActive() {
    return this.painter_ != null;
  }

  public String getRendered() {
    try {
      StringWriter s = new StringWriter();
      this.streamResourceData(s);
      return s.toString();
    } catch (IOException ioe) {
      logger.info("Ignoring exception {}", ioe.getMessage(), ioe);
      return null;
    }
  }

  public WLength getWidth() {
    return this.width_;
  }

  public WLength getHeight() {
    return this.height_;
  }

  public void handleRequest(final WebRequest request, final WebResponse response)
      throws IOException {
    response.setContentType("image/svg+xml");
    Writer o = response.out();
    this.streamResourceData(o);
  }

  public WPainter getPainter() {
    return this.painter_;
  }

  public void setPainter(WPainter painter) {
    this.painter_ = painter;
  }

  private WLength width_;
  private WLength height_;
  private WPainter painter_;
  private boolean paintUpdate_;
  private EnumSet<PainterChangeFlag> changeFlags_;
  private boolean newGroup_;
  private boolean newClipPath_;
  private boolean busyWithPath_;
  private int currentClipId_;
  private static int nextClipId_ = 0;
  private int currentFillGradientId_;
  private int currentStrokeGradientId_;
  private static int nextGradientId_ = 0;
  private WTransform currentTransform_;
  private WBrush currentBrush_;
  private WFont currentFont_;
  private WPen currentPen_;
  private WShadow currentShadow_;
  private int currentShadowId_;
  private int nextShadowId_;
  private WPointF pathTranslation_;
  private WRectF pathBoundingBox_;
  private StringBuilder shapes_;
  private ServerSideFontMetrics fontMetrics_;

  private void finishPath() {
    if (this.busyWithPath_) {
      this.busyWithPath_ = false;
      this.shapes_.append("\" />");
    }
  }

  private void makeNewGroup() {
    if (!this.newGroup_) {
      return;
    }
    boolean brushChanged =
        this.changeFlags_.contains(PainterChangeFlag.Brush)
            && !this.currentBrush_.equals(this.getPainter().getBrush());
    boolean penChanged =
        this.changeFlags_.contains(PainterChangeFlag.Hints)
            || this.changeFlags_.contains(PainterChangeFlag.Pen)
                && !this.currentPen_.equals(this.getPainter().getPen());
    boolean fontChanged =
        this.changeFlags_.contains(PainterChangeFlag.Font)
            && !this.currentFont_.equals(this.getPainter().getFont());
    boolean shadowChanged = false;
    if (this.changeFlags_.contains(PainterChangeFlag.Shadow)) {
      if (this.currentShadowId_ == -1) {
        shadowChanged = !this.getPainter().getShadow().isNone();
      } else {
        shadowChanged = !this.currentShadow_.equals(this.getPainter().getShadow());
      }
    }
    if (shadowChanged) {
      this.newClipPath_ = true;
    }
    if (!this.newClipPath_) {
      if (!brushChanged && !penChanged) {
        WTransform f = this.getPainter().getCombinedTransform();
        if (this.busyWithPath_) {
          if (fequal(f.getM11(), this.currentTransform_.getM11())
              && fequal(f.getM12(), this.currentTransform_.getM12())
              && fequal(f.getM21(), this.currentTransform_.getM21())
              && fequal(f.getM22(), this.currentTransform_.getM22())) {
            double det = f.getM11() * f.getM22() - f.getM12() * f.getM21();
            double a11 = f.getM22() / det;
            double a12 = -f.getM12() / det;
            double a21 = -f.getM21() / det;
            double a22 = f.getM11() / det;
            double fdx = f.getDx() * a11 + f.getDy() * a21;
            double fdy = f.getDx() * a12 + f.getDy() * a22;
            final WTransform g = this.currentTransform_;
            double gdx = g.getDx() * a11 + g.getDy() * a21;
            double gdy = g.getDx() * a12 + g.getDy() * a22;
            double dx = fdx - gdx;
            double dy = fdy - gdy;
            this.pathTranslation_.setX(dx);
            this.pathTranslation_.setY(dy);
            this.changeFlags_ = EnumSet.noneOf(PainterChangeFlag.class);
            return;
          }
        } else {
          if (!fontChanged && this.currentTransform_.equals(f)) {
            this.newGroup_ = false;
            this.changeFlags_ = EnumSet.noneOf(PainterChangeFlag.class);
            return;
          }
        }
      }
    }
    this.newGroup_ = false;
    this.finishPath();
    char[] buf = new char[30];
    this.shapes_.append("</g>");
    this.currentTransform_.assign(this.getPainter().getCombinedTransform());
    if (this.newClipPath_) {
      this.shapes_.append("</g>");
      if (this.getPainter().hasClipping()) {
        this.currentClipId_ = nextClipId_++;
        this.shapes_.append("<defs><clipPath id=\"clip").append(this.currentClipId_).append("\">");
        this.drawPlainPath(this.shapes_, this.getPainter().getClipPath());
        this.shapes_.append('"');
        this.busyWithPath_ = false;
        final WTransform t = this.getPainter().getClipPathTransform();
        if (!t.isIdentity()) {
          this.shapes_.append(" transform=\"matrix(").append(MathUtils.roundJs(t.getM11(), 3));
          this.shapes_.append(' ').append(MathUtils.roundJs(t.getM12(), 3));
          this.shapes_.append(' ').append(MathUtils.roundJs(t.getM21(), 3));
          this.shapes_.append(' ').append(MathUtils.roundJs(t.getM22(), 3));
          this.shapes_.append(' ').append(MathUtils.roundJs(t.getM31(), 3));
          this.shapes_.append(' ').append(MathUtils.roundJs(t.getM32(), 3)).append(")\"");
        }
        this.shapes_.append("/></clipPath></defs>");
      }
      this.newClipPath_ = false;
      if (shadowChanged) {
        if (!this.getPainter().getShadow().isNone()) {
          if (!this.getPainter().getShadow().equals(this.currentShadow_)) {
            this.currentShadow_ = this.getPainter().getShadow();
            this.currentShadowId_ = this.createShadowFilter(this.shapes_);
          } else {
            this.currentShadowId_ = this.nextShadowId_;
          }
        } else {
          this.currentShadowId_ = -1;
        }
      }
      this.shapes_.append("<g");
      if (this.getPainter().hasClipping()) {
        this.shapes_.append(this.getClipPath());
      }
      if (this.currentShadowId_ != -1) {
        this.shapes_.append(" filter=\"url(#f").append(this.currentShadowId_).append(")\"");
      }
      this.shapes_.append('>');
    }
    if (penChanged) {
      this.currentPen_ = this.getPainter().getPen();
      if (!this.currentPen_.getGradient().isEmpty()) {
        this.currentStrokeGradientId_ = nextGradientId_++;
        this.defineGradient(this.currentPen_.getGradient(), this.currentStrokeGradientId_);
      }
      this.strokeStyle_ = this.getStrokeStyle();
    }
    if (brushChanged) {
      this.currentBrush_ = this.getPainter().getBrush();
      if (!this.currentBrush_.getGradient().isEmpty()) {
        this.currentFillGradientId_ = nextGradientId_++;
        this.defineGradient(this.currentBrush_.getGradient(), this.currentFillGradientId_);
      }
      this.fillStyle_ = this.getFillStyle();
    }
    if (fontChanged) {
      this.currentFont_ = this.getPainter().getFont();
      this.fontStyle_ = this.getFontStyle();
    }
    this.shapes_
        .append("<g style=\"")
        .append(this.fillStyle_)
        .append(this.strokeStyle_)
        .append(this.fontStyle_)
        .append('"');
    if (!this.currentTransform_.isIdentity()) {
      this.shapes_
          .append(" transform=\"matrix(")
          .append(MathUtils.roundJs(this.currentTransform_.getM11(), 3));
      this.shapes_.append(' ').append(MathUtils.roundJs(this.currentTransform_.getM12(), 3));
      this.shapes_.append(' ').append(MathUtils.roundJs(this.currentTransform_.getM21(), 3));
      this.shapes_.append(' ').append(MathUtils.roundJs(this.currentTransform_.getM22(), 3));
      this.shapes_.append(' ').append(MathUtils.roundJs(this.currentTransform_.getM31(), 3));
      this.shapes_
          .append(' ')
          .append(MathUtils.roundJs(this.currentTransform_.getM32(), 3))
          .append(")\"");
    }
    this.shapes_.append('>');
    this.changeFlags_ = EnumSet.noneOf(PainterChangeFlag.class);
  }

  private String getFillStyle() {
    char[] buf = new char[30];
    String result = "";
    switch (this.getPainter().getBrush().getStyle()) {
      case None:
        result += "fill:none;";
        break;
      case Solid:
        {
          final WColor color = this.getPainter().getBrush().getColor();
          result += "fill:" + color.getCssText(false) + ";";
          if (color.getAlpha() != 255) {
            result += "fill-opacity:";
            result += MathUtils.roundCss(color.getAlpha() / 255., 3);
            result += ';';
          }
          break;
        }
      case Gradient:
        if (!this.currentBrush_.getGradient().isEmpty()) {
          result += "fill:";
          result += "url(#gradient";
          result += String.valueOf(this.currentFillGradientId_);
          result += ");";
        }
    }
    return result;
  }

  private String getStrokeStyle() {
    StringBuilder result = new StringBuilder();
    String buf;
    final WPen pen = this.getPainter().getPen();
    if (!!EnumUtils.mask(this.getPainter().getRenderHints(), RenderHint.Antialiasing).isEmpty()) {
      result.append("shape-rendering:optimizeSpeed;");
    }
    if (pen.getStyle() != PenStyle.None) {
      final WColor color = pen.getColor();
      if (!pen.getGradient().isEmpty()) {
        result
            .append("stroke:url(#gradient")
            .append(String.valueOf(this.currentStrokeGradientId_))
            .append(");");
      } else {
        result.append("stroke:").append(color.getCssText(false)).append(';');
        if (color.getAlpha() != 255) {
          result
              .append("stroke-opacity:")
              .append(MathUtils.roundCss(color.getAlpha() / 255., 2))
              .append(';');
        }
      }
      WLength w = this.getPainter().normalizedPenWidth(pen.getWidth(), true);
      if (!w.equals(new WLength(1))) {
        result.append("stroke-width:").append(w.getCssText()).append(";");
      }
      switch (pen.getCapStyle()) {
        case Flat:
          break;
        case Square:
          result.append("stroke-linecap:square;");
          break;
        case Round:
          result.append("stroke-linecap:round;");
      }
      switch (pen.getJoinStyle()) {
        case Miter:
          break;
        case Bevel:
          result.append("stroke-linejoin:bevel;");
          break;
        case Round:
          result.append("stroke-linejoin:round;");
      }
      switch (pen.getStyle()) {
        case None:
          break;
        case SolidLine:
          break;
        case DashLine:
          result.append("stroke-dasharray:4,2;");
          break;
        case DotLine:
          result.append("stroke-dasharray:1,2;");
          break;
        case DashDotLine:
          result.append("stroke-dasharray:4,2,1,2;");
          break;
        case DashDotDotLine:
          result.append("stroke-dasharray:4,2,1,2,1,2;");
          break;
      }
    }
    return result.toString();
  }

  private String getFontStyle() {
    return this.getPainter().getFont().getCssText(false);
  }

  private String getClipPath() {
    if (this.getPainter().hasClipping()) {
      return " clip-path=\"url(#clip" + String.valueOf(this.currentClipId_) + ")\"";
    } else {
      return "";
    }
  }

  private int createShadowFilter(final StringBuilder out) {
    char[] buf = new char[30];
    int result = ++this.nextShadowId_;
    out.append("<filter id=\"f")
        .append(result)
        .append("\" width=\"150%\" height=\"150%\">")
        .append("<feOffset result=\"offOut\" in=\"SourceAlpha\" dx=\"")
        .append(MathUtils.roundJs(this.currentShadow_.getOffsetX(), 3))
        .append("\" dy=\"");
    out.append(MathUtils.roundJs(this.currentShadow_.getOffsetY(), 3)).append("\" />");
    out.append("<feColorMatrix result=\"colorOut\" in=\"offOut\" ")
        .append("type=\"matrix\" values=\"");
    double r = this.currentShadow_.getColor().getRed() / 255.;
    double g = this.currentShadow_.getColor().getGreen() / 255.;
    double b = this.currentShadow_.getColor().getBlue() / 255.;
    double a = this.currentShadow_.getColor().getAlpha() / 255.;
    out.append("0 0 0 ").append(MathUtils.roundJs(r, 3)).append(" 0 ");
    out.append("0 0 0 ").append(MathUtils.roundJs(g, 3)).append(" 0 ");
    out.append("0 0 0 ").append(MathUtils.roundJs(b, 3)).append(" 0 ");
    out.append("0 0 0 ").append(MathUtils.roundJs(a, 3)).append(" 0\"/>");
    out.append("<feGaussianBlur result=\"blurOut\" in=\"colorOut\" stdDeviation=\"")
        .append(MathUtils.roundJs(Math.sqrt(this.currentShadow_.getBlur()), 3))
        .append("\" /><feBlend in=\"SourceGraphic\" in2=\"blurOut\" mode=\"normal\" /></filter>");
    return result;
  }

  private void defineGradient(final WGradient gradient, int id) {
    char[] buf = new char[30];
    this.shapes_.append("<defs>");
    boolean linear = gradient.getStyle() == GradientStyle.Linear;
    if (linear) {
      this.shapes_.append("<linearGradient gradientUnits=\"userSpaceOnUse\" ");
      this.shapes_
          .append("x1=\"")
          .append(gradient.getLinearGradientVector().getX1())
          .append("\" ")
          .append("y1=\"")
          .append(gradient.getLinearGradientVector().getY1())
          .append("\" ")
          .append("x2=\"")
          .append(gradient.getLinearGradientVector().getX2())
          .append("\" ")
          .append("y2=\"")
          .append(gradient.getLinearGradientVector().getY2())
          .append("\" ");
    } else {
      this.shapes_.append("<radialGradient gradientUnits=\"userSpaceOnUse\" ");
      this.shapes_
          .append("cx=\"")
          .append(gradient.getRadialCenterPoint().getX())
          .append("\" ")
          .append("cy=\"")
          .append(gradient.getRadialCenterPoint().getY())
          .append("\" ")
          .append("r=\"")
          .append(gradient.getRadialRadius())
          .append("\" ")
          .append("fx=\"")
          .append(gradient.getRadialFocalPoint().getX())
          .append("\" ")
          .append("fy=\"")
          .append(gradient.getRadialFocalPoint().getY())
          .append("\" ");
    }
    this.shapes_.append("id=\"gradient").append(id).append("\">");
    for (int i = 0; i < gradient.getColorstops().size(); i++) {
      this.shapes_.append("<stop ");
      String offset = String.valueOf((int) (gradient.getColorstops().get(i).getPosition() * 100));
      offset += '%';
      this.shapes_.append("offset=\"").append(offset).append("\" ");
      this.shapes_
          .append("stop-color=\"")
          .append(gradient.getColorstops().get(i).getColor().getCssText(false))
          .append("\" ");
      this.shapes_
          .append("stop-opacity=\"")
          .append(
              MathUtils.roundCss(gradient.getColorstops().get(i).getColor().getAlpha() / 255., 3))
          .append("\" ");
      this.shapes_.append("/>");
    }
    if (linear) {
      this.shapes_.append("</linearGradient>");
    } else {
      this.shapes_.append("</radialGradient>");
    }
    this.shapes_.append("</defs>");
  }

  private String fillStyle_;
  private String strokeStyle_;
  private String fontStyle_;

  private static String quote(double d) {
    char[] buf = new char[30];
    return quote(MathUtils.roundJs(d, 3));
  }

  private static String quote(final String s) {
    return '"' + s + '"';
  }

  private void drawPlainPath(final StringBuilder out, final WPainterPath path) {
    char[] buf = new char[30];
    if (!this.busyWithPath_) {
      out.append("<path d=\"");
      this.busyWithPath_ = true;
      this.pathTranslation_.setX(0);
      this.pathTranslation_.setY(0);
    }
    final List<WPainterPath.Segment> segments = path.getSegments();
    if (!segments.isEmpty() && segments.get(0).getType() != SegmentType.MoveTo) {
      out.append("M0,0");
    }
    for (int i = 0; i < segments.size(); ++i) {
      final WPainterPath.Segment s = segments.get(i);
      if (s.getType() == SegmentType.ArcC) {
        WPointF current = path.getPositionAtSegment(i);
        final double cx = segments.get(i).getX();
        final double cy = segments.get(i).getY();
        final double rx = segments.get(i + 1).getX();
        final double ry = segments.get(i + 1).getY();
        final double theta1 = -WTransform.degreesToRadians(segments.get(i + 2).getX());
        final double deltaTheta =
            -WTransform.degreesToRadians(adjust360(segments.get(i + 2).getY()));
        i += 2;
        final double x1 = rx * Math.cos(theta1) + cx;
        final double y1 = ry * Math.sin(theta1) + cy;
        final double x2 = rx * Math.cos(theta1 + deltaTheta / 2.0) + cx;
        final double y2 = ry * Math.sin(theta1 + deltaTheta / 2.0) + cy;
        final double x3 = rx * Math.cos(theta1 + deltaTheta) + cx;
        final double y3 = ry * Math.sin(theta1 + deltaTheta) + cy;
        final int fa = 0;
        final int fs = deltaTheta > 0 ? 1 : 0;
        if (!fequal(current.getX(), x1) || !fequal(current.getY(), y1)) {
          out.append('L').append(MathUtils.roundJs(x1 + this.pathTranslation_.getX(), 3));
          out.append(',').append(MathUtils.roundJs(y1 + this.pathTranslation_.getY(), 3));
        }
        out.append('A').append(MathUtils.roundJs(rx, 3));
        out.append(',').append(MathUtils.roundJs(ry, 3));
        out.append(" 0 ").append(fa).append(",").append(fs);
        out.append(' ').append(MathUtils.roundJs(x2 + this.pathTranslation_.getX(), 3));
        out.append(',').append(MathUtils.roundJs(y2 + this.pathTranslation_.getY(), 3));
        out.append('A').append(MathUtils.roundJs(rx, 3));
        out.append(',').append(MathUtils.roundJs(ry, 3));
        out.append(" 0 ").append(fa).append(",").append(fs);
        out.append(' ').append(MathUtils.roundJs(x3 + this.pathTranslation_.getX(), 3));
        out.append(',').append(MathUtils.roundJs(y3 + this.pathTranslation_.getY(), 3));
      } else {
        switch (s.getType()) {
          case MoveTo:
            out.append('M');
            break;
          case LineTo:
            out.append('L');
            break;
          case CubicC1:
            out.append('C');
            break;
          case CubicC2:
          case CubicEnd:
            out.append(' ');
            break;
          case QuadC:
            out.append('Q');
            break;
          case QuadEnd:
            out.append(' ');
            break;
          default:
            assert false;
        }
        out.append(MathUtils.roundJs(s.getX() + this.pathTranslation_.getX(), 3));
        out.append(',').append(MathUtils.roundJs(s.getY() + this.pathTranslation_.getY(), 3));
      }
    }
  }

  private void streamResourceData(final Writer stream) throws IOException {
    this.finishPath();
    if (this.paintUpdate_) {
      stream
          .append(
              "<g xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"><g><g>")
          .append(this.shapes_.toString())
          .append("</g></g></g>");
    } else {
      stream
          .append(
              "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"1.1\" baseProfile=\"full\" width=\"")
          .append(this.getWidth().getCssText())
          .append("\" height=\"")
          .append(this.getHeight().getCssText())
          .append("\">")
          .append("<g><g>")
          .append(this.shapes_.toString())
          .append("</g></g></svg>");
    }
  }

  static double adjust360(double d) {
    if (d > 360.0) {
      return 360.0;
    } else {
      if (d < -360.0) {
        return -360.0;
      } else {
        return d;
      }
    }
  }

  static boolean fequal(double d1, double d2) {
    return Math.abs(d1 - d2) < 1E-5;
  }
}
