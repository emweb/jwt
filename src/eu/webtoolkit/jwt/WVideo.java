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
 * A video-playing widget.
 *
 * <p>This is a low-level widget, mapping directly onto a <code>&lt;video&gt;</code> element
 * available in HTML5 compliant browsers.
 *
 * <p>In almost every situation you should use the {@link WMediaPlayer} widget instead, which has
 * fallback and flexible user-interface options.
 *
 * <p>Usage of the video element consists of adding one or more video sources and setting some
 * options. Since not every browser supports HTML5 video, the class provides a mechanism to display
 * alternative content in browsers that cannot play the video.
 *
 * <p>There are two reasons why the a browser may use the alternative content: either because the
 * browser does not support the HTML5 video tag (alternative content is displayed even when
 * JavaScript is not available), or because none of the specified sources contain a video format
 * that is understood by the browser (requires JavaScript to display the alternative content).
 *
 * <p>The {@link WAbstractMedia#addSource(WLink link, String type, String media)
 * WAbstractMedia#addSource()} and {@link WAbstractMedia#setAlternativeContent(WWidget alternative)
 * WAbstractMedia#setAlternativeContent()} may not be called after the widget is rendered.
 *
 * <p>
 *
 * @see WMediaPlayer
 */
public class WVideo extends WAbstractMedia {
  private static Logger logger = LoggerFactory.getLogger(WVideo.class);

  /**
   * Creates a video widget.
   *
   * <p>The constructor sets the &apos;controls&apos; option, which causes the browser to display a
   * bar with play/pauze/volume/... controls.
   *
   * <p>A freshly constructed video widget has no poster image, no media sources, has preload mode
   * set to PreloadAuto, and only the Controls flag is set.
   */
  public WVideo(WContainerWidget parentContainer) {
    super();
    this.posterUrl_ = "";
    this.sizeChanged_ = false;
    this.posterChanged_ = false;
    this.setInline(false);
    this.setOptions(EnumSet.of(PlayerOption.Controls));
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a video widget.
   *
   * <p>Calls {@link #WVideo(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WVideo() {
    this((WContainerWidget) null);
  }
  /**
   * Set the poster image.
   *
   * <p>On browsers that support it, the poster image is displayed before the video is playing. Some
   * browsers display the first frame of the video stream once the video stream is loaded; it is
   * therefore a good idea to include the poster image as first frame in the video feed too.
   */
  public void setPoster(final String url) {
    this.posterUrl_ = url;
    this.posterChanged_ = true;
    this.repaint();
  }
  /**
   * Returns the JavaScript reference to the video object, or null.
   *
   * <p>It is possible, for compatibility reasons, that {@link WWidget#getJsRef()} is not the video
   * element. {@link WVideo#getJsVideoRef() getJsVideoRef()} is guaranteed to be an expression that
   * evaluates to the video object. This expression may yield null, if the video object is not
   * rendered at all (e.g. on older versions of Internet Explorer).
   */
  public String getJsVideoRef() {
    return this.getJsMediaRef();
  }

  public void resize(final WLength width, final WLength height) {
    this.sizeChanged_ = true;
    super.resize(width, height);
  }

  DomElement createMediaDomElement() {
    return DomElement.createNew(DomElementType.VIDEO);
  }

  DomElementType getDomElementType() {
    return DomElementType.VIDEO;
  }

  void updateMediaDom(final DomElement element, boolean all) {
    super.updateMediaDom(element, all);
    if (all || this.sizeChanged_) {
      if (!all || !this.getWidth().isAuto()) {
        element.setAttribute(
            "width",
            this.getWidth().isAuto() ? "" : String.valueOf((int) this.getWidth().toPixels()));
      }
      if (!all || !this.getHeight().isAuto()) {
        element.setAttribute(
            "height",
            this.getHeight().isAuto() ? "" : String.valueOf((int) this.getHeight().toPixels()));
      }
    }
    if (all || this.posterChanged_) {
      if (!all || !this.posterUrl_.equals("")) {
        element.setAttribute("poster", resolveRelativeUrl(this.posterUrl_));
      }
    }
    this.sizeChanged_ = this.posterChanged_ = false;
  }

  private String posterUrl_;
  private boolean sizeChanged_;
  private boolean posterChanged_;
}
