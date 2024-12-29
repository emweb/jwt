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
 * A more obvious loading indicator that grays the window.
 *
 * <p>This loading indicator uses a gray semi-transparent overlay to darken the window contents, and
 * centers a loading icon (with some text).
 *
 * <p>Usage example:
 *
 * <p><div align="center"> <img src="doc-files/WOverlayLoadingIndicator.png">
 *
 * <p><strong>The overlay loading indicator</strong> </div>
 *
 * <p>
 *
 * <p><i><b>Note: </b>For this loading indicator to render properly in IE, you need to reset the
 * &quot;body&quot; margin to 0. Using the inline stylesheet, this could be done using: </i>
 *
 * <h3>CSS</h3>
 *
 * <p>This widget does not provide styling, and can be styled using inline or external CSS as
 * appropriate.
 *
 * <p>
 *
 * <h3>i18n</h3>
 *
 * <p>The strings used in this class can be translated by overriding the default values for the
 * following localization keys:
 *
 * <ul>
 *   <li>Wt.WOverlayLoadingIndicator.Loading: Loading...
 * </ul>
 *
 * <p>
 *
 * @see WApplication#setLoadingIndicator(WLoadingIndicator indicator)
 */
public class WOverlayLoadingIndicator extends WLoadingIndicator {
  private static Logger logger = LoggerFactory.getLogger(WOverlayLoadingIndicator.class);

  /**
   * Construct the loading indicator.
   *
   * <p>
   *
   * @param styleClass the style class for the central box
   * @param backgroundStyleClass the style class for the &quot;background&quot; part of the
   *     indicator
   * @param textStyleClass the style class for the text that is displayed
   *     <p><i><b>Note: </b>if styleClass is not set, the central box gets the CSS style elements
   *     <pre>{@code
   * background: white;
   * border: 3px solid #333333;
   * z-index: 10001; visibility: visible;
   * position: absolute; left: 50%; top: 50%;
   * margin-left: -50px; margin-top: -40px;
   * width: 100px; height: 80px;
   * font-family: arial,sans-serif;
   * text-align: center
   *
   * }</pre>
   *     </i>
   *     <p><i><b>Note: </b>if backgroundStyleClass is not set, the background gets the CSS style
   *     elements
   *     <pre>{@code
   * background: #DDDDDD;
   * height: 100%; width: 100%;
   * top: 0px; left: 0px;
   * z-index: 10000;
   * -moz-background-clip: -moz-initial;
   * -moz-background-origin: -moz-initial;
   * -moz-background-inline-policy: -moz-initial;
   * opacity: 0.5; filter: alpha(opacity=50); -moz-opacity:0.5;
   * position: absolute;
   *
   * }</pre>
   *     </i>
   */
  public WOverlayLoadingIndicator(
      final String styleClass, final String backgroundStyleClass, final String textStyleClass) {
    super();
    WContainerWidget impl;
    this.setImplementation(impl = new WContainerWidget());
    this.setInline(false);
    WApplication app = WApplication.getInstance();
    impl.addWidget(this.cover_ = new WContainerWidget());
    impl.addWidget(this.center_ = new WContainerWidget());
    WImage img;
    this.center_.addWidget(
        img =
            new WImage(
                new WLink(WApplication.getRelativeResourcesUrl() + "ajax-loading.gif"),
                (WContainerWidget) null));
    img.setMargin(new WLength(7), EnumSet.of(Side.Top, Side.Bottom));
    this.center_.addWidget(
        this.text_ = new WText(tr("Wt.WOverlayLoadingIndicator.Loading"), (WContainerWidget) null));
    this.text_.setInline(false);
    this.text_.setMargin(WLength.Auto, EnumSet.of(Side.Left, Side.Right));
    if (styleClass.length() != 0) {
      this.center_.setStyleClass(styleClass);
    }
    if (textStyleClass.length() != 0) {
      this.text_.setStyleClass(textStyleClass);
    }
    if (backgroundStyleClass.length() != 0) {
      this.cover_.setStyleClass(backgroundStyleClass);
    }
    if (app.getEnvironment().getAgent() == UserAgent.IE6) {
      app.getStyleSheet().addRule("body", "height: 100%; margin: 0;");
    }
    String position = app.getEnvironment().getAgent() == UserAgent.IE6 ? "absolute" : "fixed";
    if (backgroundStyleClass.length() == 0) {
      app.getStyleSheet()
          .addRule(
              "div#" + this.cover_.getId(),
              ""
                  + "background: #DDDDDD;height: 100%; width: 100%;top: 0px; left: 0px;position: "
                  + position
                  + ";z-index: 10000;"
                  + (app.getEnvironment().agentIsIE()
                      ? "filter: alpha(opacity=50);"
                      : "opacity: 0.5;"));
    }
    if (styleClass.length() == 0) {
      app.getStyleSheet()
          .addRule(
              "div#" + this.center_.getId(),
              "background: white;border: 3px solid #333333;z-index: 10001; visibility: visible;position: "
                  + position
                  + "; left: 50%; top: 50%;margin-left: -50px; margin-top: -40px;width: 100px; height: 80px;font-family: arial,sans-serif;text-align: center");
    }
  }
  /**
   * Construct the loading indicator.
   *
   * <p>Calls {@link #WOverlayLoadingIndicator(String styleClass, String backgroundStyleClass,
   * String textStyleClass) this("", "", "")}
   */
  public WOverlayLoadingIndicator() {
    this("", "", "");
  }
  /**
   * Construct the loading indicator.
   *
   * <p>Calls {@link #WOverlayLoadingIndicator(String styleClass, String backgroundStyleClass,
   * String textStyleClass) this(styleClass, "", "")}
   */
  public WOverlayLoadingIndicator(final String styleClass) {
    this(styleClass, "", "");
  }
  /**
   * Construct the loading indicator.
   *
   * <p>Calls {@link #WOverlayLoadingIndicator(String styleClass, String backgroundStyleClass,
   * String textStyleClass) this(styleClass, backgroundStyleClass, "")}
   */
  public WOverlayLoadingIndicator(final String styleClass, final String backgroundStyleClass) {
    this(styleClass, backgroundStyleClass, "");
  }

  public void setMessage(final CharSequence text) {
    this.text_.setText(text);
  }

  private WContainerWidget cover_;
  private WContainerWidget center_;
  private WText text_;
}
