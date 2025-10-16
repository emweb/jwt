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

class WWidgetRasterPainter extends WWidgetPainter {
  private static Logger logger = LoggerFactory.getLogger(WWidgetRasterPainter.class);

  public WWidgetRasterPainter(WPaintedWidget widget) {
    super(widget);
    this.device_ = null;
  }

  public WPaintDevice createPaintDevice(boolean paintUpdate) {
    return new WRasterPaintDevice(
        "png", new WLength(this.widget_.renderWidth_), new WLength(this.widget_.renderHeight_));
  }

  public WPaintDevice getPaintDevice(boolean paintUpdate) {
    if (!(this.device_ != null) || this.widget_.sizeChanged_) {
      this.device_ = this.createPaintDevice(paintUpdate);
    }
    if (!paintUpdate) {
      (ObjectUtils.cast(this.device_, WRasterPaintDevice.class)).clear();
    }
    return this.device_;
  }

  public void createContents(DomElement result, WPaintDevice device) {
    String wstr = String.valueOf(this.widget_.renderWidth_);
    String hstr = String.valueOf(this.widget_.renderHeight_);
    DomElement img = DomElement.createNew(DomElementType.IMG);
    img.setId('i' + this.widget_.getId());
    img.setAttribute("width", wstr);
    img.setAttribute("height", hstr);
    img.setAttribute("class", "unselectable");
    img.setAttribute("unselectable", "on");
    WResource resource = ObjectUtils.cast(device, WResource.class);
    img.setAttribute("src", resource.generateUrl());
    result.addChild(img);
    this.device_ = device;
    StringBuilder selectJS = new StringBuilder();
    selectJS
        .append("Wt4_12_1")
        .append(".$('")
        .append("i")
        .append(this.widget_.getId())
        .append("').onselectstart = ")
        .append("function() { return false; };");
    WApplication.getInstance().doJavaScript(selectJS.toString());
    StringBuilder mouseJS = new StringBuilder();
    mouseJS
        .append("Wt4_12_1")
        .append(".$('")
        .append("i")
        .append(this.widget_.getId())
        .append("').onmousedown = ")
        .append("function() { return false; };");
    WApplication.getInstance().doJavaScript(mouseJS.toString());
  }

  public void updateContents(final List<DomElement> result, WPaintDevice device) {
    WResource resource = ObjectUtils.cast(device, WResource.class);
    DomElement img = DomElement.getForUpdate('i' + this.widget_.getId(), DomElementType.IMG);
    if (this.widget_.sizeChanged_) {
      img.setAttribute("width", String.valueOf(this.widget_.renderWidth_));
      img.setAttribute("height", String.valueOf(this.widget_.renderHeight_));
      this.widget_.sizeChanged_ = false;
    }
    img.setAttribute("src", resource.generateUrl());
    result.add(img);
    this.device_ = device;
  }

  public WWidgetPainter.RenderType getRenderType() {
    return WWidgetPainter.RenderType.PngImage;
  }

  private WPaintDevice device_;
}
