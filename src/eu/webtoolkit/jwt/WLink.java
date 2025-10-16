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
 * A value class that defines a hyperlink target.
 *
 * <p>This class abstracts a link target. Depending on the context, it may reference a URL, a
 * dynamic {@link WResource resource}, or (for certain usages) an internal path.
 *
 * <p>
 *
 * @see WAnchor
 * @see WImage
 * @see WMediaPlayer
 * @see WPushButton
 */
public class WLink {
  private static Logger logger = LoggerFactory.getLogger(WLink.class);

  /**
   * Default constructor.
   *
   * <p>This constructs a null link.
   */
  public WLink() {
    this.type_ = LinkType.Url;
    this.stringValue_ = "";
    this.resource_ = null;
    this.target_ = LinkTarget.Self;
    this.setUrl("");
  }
  /**
   * Creates a link to a (static) URL.
   *
   * <p>
   *
   * @see WLink#setUrl(String url)
   */
  public WLink(String url) {
    this.stringValue_ = "";
    this.resource_ = null;
    this.target_ = LinkTarget.Self;
    this.setUrl(url);
  }
  /**
   * Creates a link to a (static) URL or an internal path.
   *
   * <p>Using this constructor, you can create a link to a static URL (<code>type</code> == {@link
   * LinkType#Url}) or an internal path (<code>type</code> == {@link LinkType#InternalPath}). For an
   * internal path, the <code>value</code> will be interpreted as a CharEncoding::UTF8 encoded
   * string.
   *
   * <p>
   *
   * @see WLink#setUrl(String url)
   * @see WLink#setInternalPath(String internalPath)
   */
  public WLink(LinkType type, final String value) {
    this.stringValue_ = "";
    this.resource_ = null;
    this.target_ = LinkTarget.Self;
    switch (type) {
      case Url:
        this.setUrl(value);
        break;
      case InternalPath:
        this.setInternalPath(new WString(value).toString());
        break;
      default:
        throw new WException("WLink::WLink(type) cannot be used for a Resource");
    }
  }
  /**
   * Creates a link to a resource.
   *
   * <p>
   *
   * @see WLink#setResource(WResource resource)
   */
  public WLink(final WResource resource) {
    this.stringValue_ = "";
    this.resource_ = null;
    this.target_ = LinkTarget.Self;
    this.setResource(resource);
  }
  /**
   * Returns the link type.
   *
   * <p>The type is implicitly set depending on the constructor or after calling {@link
   * WLink#setUrl(String url) setUrl()}, {@link WLink#setResource(WResource resource) setResource()}
   * or {@link WLink#setInternalPath(String internalPath) setInternalPath()}.
   *
   * <p>The default type for a null link is {@link LinkType#Url}.
   */
  public LinkType getType() {
    return this.type_;
  }
  /**
   * Returns whether the link is unspecified.
   *
   * <p>A null link is a link created using the default constructor and points to nowhere.
   *
   * <p>
   *
   * @see WLink#WLink()
   */
  public boolean isNull() {
    return this.type_ == LinkType.Url && this.getUrl().length() == 0;
  }
  /**
   * Sets the link URL.
   *
   * <p>This sets the type to {@link LinkType#Url}.
   */
  public void setUrl(final String url) {
    this.type_ = LinkType.Url;
    this.stringValue_ = url;
    this.resource_ = null;
  }
  /**
   * Returns the link URL.
   *
   * <p>The return value is the URL set by {@link WLink#setUrl(String url) setUrl()}, the resource
   * URL of the resource set using {@link WLink#setResource(WResource resource) setResource()}, or
   * the canonical URL of an internal path within the current application context.
   */
  public String getUrl() {
    switch (this.type_) {
      case Url:
        return this.stringValue_;
      case Resource:
        return this.getResource().getUrl();
      case InternalPath:
        return WApplication.getInstance().getBookmarkUrl(this.getInternalPath());
    }
    return "";
  }
  /**
   * Sets the link resource.
   *
   * <p>This sets the type to {@link LinkType#Resource}.
   */
  public void setResource(final WResource resource) {
    this.type_ = LinkType.Resource;
    this.resource_ = resource;
    this.stringValue_ = "";
  }
  /**
   * Returns the link resource.
   *
   * <p>This returns the resource previously set using {@link WLink#setResource(WResource resource)
   * setResource()}, or <code>null</code>.
   *
   * <p>
   *
   * @see WLink#setResource(WResource resource)
   */
  public WResource getResource() {
    return this.resource_;
  }
  /**
   * Sets the link internal path.
   *
   * <p>This points the link to the given internal path.
   */
  public void setInternalPath(final String internalPath) {
    this.type_ = LinkType.InternalPath;
    String path = internalPath;
    if (path.startsWith("#/")) {
      path = path.substring(1);
    }
    this.stringValue_ = path;
    this.resource_ = null;
  }
  /**
   * Returns the internal path.
   *
   * <p>This returns the internal path perviously set using {@link WLink#setInternalPath(String
   * internalPath) setInternalPath()}, or an empty string otherwise.
   *
   * <p>
   *
   * @see WLink#setInternalPath(String internalPath)
   */
  public String getInternalPath() {
    if (this.type_ == LinkType.InternalPath) {
      return this.stringValue_;
    } else {
      return "";
    }
  }
  /**
   * Sets the location where the linked content should be displayed.
   *
   * <p>By default, the linked content is displayed in the application ({@link LinkTarget#Self}).
   * When the destination is an HTML document, the application is replaced with the new document.
   * When the link is to a document that cannot be displayed in the browser, it is offered for
   * download or opened using an external program, depending on browser settings.
   *
   * <p>By setting <code>target</code> to {@link LinkTarget#NewWindow}, the destination is displayed
   * in a new browser window or tab.
   *
   * <p>
   *
   * @see WLink#getTarget()
   */
  public void setTarget(LinkTarget target) {
    this.target_ = target;
  }
  /**
   * Returns the location where the linked content should be displayed.
   *
   * <p>
   *
   * @see WLink#setTarget(LinkTarget target)
   */
  public LinkTarget getTarget() {
    return this.target_;
  }
  /** Indicates whether some other object is "equal to" this one. */
  public boolean equals(final WLink other) {
    return this.type_ == other.type_
        && this.stringValue_.equals(other.stringValue_)
        && this.resource_ == other.resource_;
  }

  String resolveUrl(WApplication app) {
    String relativeUrl = "";
    switch (this.type_) {
      case InternalPath:
        {
          if (app.getEnvironment().hasAjax()) {
            relativeUrl = app.getBookmarkUrl(this.getInternalPath());
          } else {
            if (app.getEnvironment().isTreatLikeBot()) {
              relativeUrl = app.getBookmarkUrl(this.getInternalPath());
            } else {
              relativeUrl = app.getSession().getMostRelativeUrl(this.getInternalPath());
            }
          }
        }
        break;
      default:
        relativeUrl = this.getUrl();
    }
    return app.resolveRelativeUrl(relativeUrl);
  }

  private LinkType type_;
  private String stringValue_;
  private WResource resource_;
  private LinkTarget target_;

  JSlot manageInternalPathChange(WApplication app, WInteractWidget widget, JSlot slot) {
    if (this.type_ == LinkType.InternalPath) {
      if (app.getEnvironment().hasAjax()) {
        if (!(slot != null)) {
          slot = new JSlot();
          widget.clicked().addListener(slot);
          widget.clicked().preventDefaultAction();
        }
        slot.setJavaScript(
            "function(){"
                + app.getJavaScriptClass()
                + "._p_.setHash("
                + WWebWidget.jsStringLiteral(this.getInternalPath())
                + ",true);}");
        widget.clicked().senderRepaint();
        return slot;
      }
    }

    return null;
  }
}
