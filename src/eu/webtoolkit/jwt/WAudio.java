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
 * A widget that plays audio.
 *
 * <p>This is a low-level widget, mapping directly onto a <code>&lt;audio&gt;</code> element
 * available in HTML5 compliant browsers.
 *
 * <p>In almost every situation you should use the {@link WMediaPlayer} widget if you want the user
 * to be able to interact with the audio, or {@link WSound} for simple sound feed-back.
 *
 * <p>Usage of the audio element consists of adding one or more audio sources and setting some
 * options. Since not every browser supports HTML5 audio, the class provides a mechanism to display
 * alternative content in browsers that cannot play the video.
 *
 * <p>There are two reasons why the a browser may use the alternative content: either because the
 * browser does not support the HTML5 audio tag (alternative content is displayed even when
 * JavaScript is not available), or because none of the specified sources contain an audio format
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
public class WAudio extends WAbstractMedia {
  private static Logger logger = LoggerFactory.getLogger(WAudio.class);

  /**
   * Creates a audio widget.
   *
   * <p>A freshly constructed Audio widget has no media sources, no options, and has preload mode
   * set to PreloadAuto.
   */
  public WAudio(WContainerWidget parentContainer) {
    super();
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a audio widget.
   *
   * <p>Calls {@link #WAudio(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WAudio() {
    this((WContainerWidget) null);
  }
  /**
   * Returns the JavaScript reference to the audio object, or null.
   *
   * <p>It is possible, for browser compatibility reasons, that {@link WWidget#getJsRef()} is not
   * the HTML5 audio element. {@link WAudio#getJsAudioRef() getJsAudioRef()} is guaranteed to be an
   * expression that evaluates to the media object. This expression may yield null, if the video
   * object is not rendered at all (e.g. on older versions of Internet Explorer).
   */
  public String getJsAudioRef() {
    return this.getJsMediaRef();
  }

  DomElement createMediaDomElement() {
    return DomElement.createNew(DomElementType.AUDIO);
  }

  DomElementType getDomElementType() {
    return DomElementType.AUDIO;
  }
}
