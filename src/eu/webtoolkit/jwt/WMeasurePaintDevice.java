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
 * {@link WMeasurePaintDevice} Wt/WMeasurePaintDevice Wt/WMeasurePaintDevice.
 *
 * <p>This implements a (pseudo)-paintdevice which measures the bounding rect of whatever is being
 * painted on it, using fontmetrics from the underlying device.
 *
 * <p>The only output of the device is the computation of a bounding rect which is returned by
 * {@link WMeasurePaintDevice#getBoundingRect() getBoundingRect()}.
 */
public class WMeasurePaintDevice implements WPaintDevice {
  private static Logger logger = LoggerFactory.getLogger(WMeasurePaintDevice.class);

  /** Creates a paint device to measure for the underlying device. */
  public WMeasurePaintDevice(WPaintDevice paintDevice) {
    super();
    this.painter_ = null;
    this.device_ = paintDevice;
    this.bounds_ = null;
  }
  /**
   * Returns the bounding rectangle of everything painted so far.
   *
   * <p>The bounding rect is returned in device coordinates (i.e. after all transformations
   * applied).
   */
  public WRectF getBoundingRect() {
    return this.bounds_;
  }

  public EnumSet<PaintDeviceFeatureFlag> getFeatures() {
    return this.device_.getFeatures();
  }

  public void setChanged(EnumSet<PainterChangeFlag> flags) {
    if (this.device_.getPainter() != this.painter_ && flags.contains(PainterChangeFlag.Font)) {
      this.device_.getPainter().setFont(this.painter_.getFont());
    }
    this.device_.setChanged(flags);
  }

  public void drawArc(final WRectF rect, double startAngle, double spanAngle) {
    WPainterPath p = new WPainterPath();
    double r = Math.max(rect.getWidth(), rect.getHeight()) / 2;
    double cx = rect.getCenter().getX();
    double cy = rect.getCenter().getY();
    p.arcMoveTo(cx, cy, r, startAngle);
    p.arcTo(cx, cy, r, startAngle, spanAngle);
    this.expandBounds(p.getControlPointRect());
  }

  public void drawImage(
      final WRectF rect,
      final String imageUri,
      int imgWidth,
      int imgHeight,
      final WRectF sourceRect) {
    this.expandBounds(rect);
  }

  public void drawLine(double x1, double y1, double x2, double y2) {
    this.expandBounds(new WRectF(x1, y1, x2 - x1, y2 - y1));
  }

  public void drawRect(final WRectF rect) {
    this.drawPath(rect.toPath());
  }

  public void drawPath(final WPainterPath path) {
    if (path.isEmpty()) {
      return;
    }
    this.expandBounds(path.getControlPointRect());
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
    double w = 0;
    double h = 0;
    WString line = WString.toWString(text);
    WFontMetrics fm = this.getFontMetrics();
    for (; ; ) {
      WTextItem t =
          this.measureText(line, rect.getWidth(), textFlag == TextFlag.WordWrap ? true : false);
      h += fm.getHeight();
      w = Math.max(w, t.getWidth());
      if ((t.getText().toString().equals(line.toString()))) {
        break;
      } else {
        line = new WString(line.toString().substring(t.getText().toString().length()));
      }
    }
    AlignmentFlag horizontalAlign =
        EnumUtils.enumFromSet(EnumUtils.mask(flags, AlignmentFlag.AlignHorizontalMask));
    AlignmentFlag verticalAlign =
        EnumUtils.enumFromSet(EnumUtils.mask(flags, AlignmentFlag.AlignVerticalMask));
    double x;
    double y;
    switch (horizontalAlign) {
      case Left:
        x = rect.getLeft();
        break;
      case Center:
        x = rect.getLeft() + (rect.getWidth() - w) / 2;
        break;
      case Right:
      default:
        x = rect.getRight() - w;
        break;
    }
    switch (verticalAlign) {
      case Top:
        y = rect.getTop();
        break;
      case Middle:
        y = rect.getTop() + (rect.getHeight() - h) / 2;
        break;
      case Bottom:
      default:
        y = rect.getBottom() - h;
        break;
    }
    this.expandBounds(new WRectF(x, y, w, h));
  }

  public WTextItem measureText(final CharSequence text, double maxWidth, boolean wordWrap) {
    return this.device_.measureText(text, maxWidth, wordWrap);
  }

  public WFontMetrics getFontMetrics() {
    return this.device_.getFontMetrics();
  }

  public void init() {
    if (!(this.device_.getPainter() != null)) {
      this.device_.setPainter(this.painter_);
      this.device_.init();
    } else {
      this.device_.getPainter().save();
    }
  }

  public void done() {
    if (this.painter_ == this.device_.getPainter()) {
      this.device_.done();
      this.device_.setPainter((WPainter) null);
    } else {
      this.device_.getPainter().restore();
    }
  }

  public boolean isPaintActive() {
    return this.painter_ != null;
  }

  public WLength getWidth() {
    return this.device_.getWidth();
  }

  public WLength getHeight() {
    return this.device_.getHeight();
  }

  public WPainter getPainter() {
    return this.painter_;
  }

  public void setPainter(WPainter painter) {
    this.painter_ = painter;
  }

  private WPainter painter_;
  private WPaintDevice device_;
  private WRectF bounds_;

  private void expandBounds(final WRectF bounds) {
    WTransform transform = this.getPainter().getCombinedTransform();
    WRectF bbox = transform.map(bounds);
    if (!(this.bounds_ == null)) {
      this.bounds_ = this.bounds_.united(bbox);
    } else {
      this.bounds_ = bbox;
    }
  }
}
