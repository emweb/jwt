/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A value class that defines a hyperlink target.
 * <p>
 * 
 * This class abstracts a link target. Depending on the context, it may
 * reference a URL, a dynamic {@link WResource resource}, or (for certain
 * usages) an internal path.
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
	 * An enumeration for a link type.
	 * <p>
	 */
	public enum Type {
		/**
		 * A static URL.
		 */
		Url,
		/**
		 * A dynamic resource.
		 */
		Resource,
		/**
		 * An internal path.
		 */
		InternalPath;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Default constructor.
	 * <p>
	 * This constructs a null link.
	 */
	public WLink() {
		this.type_ = WLink.Type.Url;
		this.value_ = new Object();
		this.target_ = AnchorTarget.TargetSelf;
		this.setUrl("");
	}

	/**
	 * Creates a link to a (static) URL.
	 * <p>
	 */
	public WLink(String url) {
		this.value_ = new Object();
		this.target_ = AnchorTarget.TargetSelf;
		this.setUrl(url);
	}

	/**
	 * Creates a link to a (static) URL or an internal path.
	 * <p>
	 * Using this constructor, you can create a link to a static URL (
	 * <code>type</code> == {@link WLink.Type#Url}) or an internal path (
	 * <code>type</code> == {@link WLink.Type#InternalPath}). For an internal
	 * path, the <code>value</code> will be interpreted as a UTF8 encoded
	 * string.
	 * <p>
	 */
	public WLink(WLink.Type type, final String value) {
		this.value_ = new Object();
		this.target_ = AnchorTarget.TargetSelf;
		switch (type) {
		case Url:
			this.setUrl(value);
			break;
		case InternalPath:
			this.setInternalPath(new WString(value).toString());
			break;
		default:
			throw new WException(
					"WLink::WLink(type) cannot be used for a Resource");
		}
	}

	/**
	 * Creates a link to a resource.
	 * <p>
	 */
	public WLink(WResource resource) {
		this.value_ = new Object();
		this.target_ = AnchorTarget.TargetSelf;
		this.setResource(resource);
	}

	/**
	 * Returns the link type.
	 * <p>
	 * The type is implicitly set depending on the constructor or after calling
	 * {@link }, {@link } or {@link }.
	 * <p>
	 * The default type for a null link is {@link WLink.Type#Url}.
	 */
	public WLink.Type getType() {
		return this.type_;
	}

	/**
	 * Returns whether the link is unspecified.
	 * <p>
	 * A null link is a link created using the default constructor and points to
	 * nowhere.
	 * <p>
	 * 
	 * @see WLink#WLink()
	 */
	public boolean isNull() {
		return this.type_ == WLink.Type.Url && this.getUrl().length() == 0;
	}

	/**
	 * Sets the link URL.
	 * <p>
	 * This sets the type to {@link WLink.Type#Url}.
	 */
	public void setUrl(final String url) {
		this.type_ = WLink.Type.Url;
		this.value_ = url;
	}

	/**
	 * Returns the link URL.
	 * <p>
	 * The return value is the URL set by {@link WLink#setUrl(String url)
	 * setUrl()}, the resource URL of the resource set using {@link }, or the
	 * canonical URL of an internal path within the current application context.
	 */
	public String getUrl() {
		switch (this.type_) {
		case Url:
			return ((String) this.value_);
		case Resource:
			return this.getResource().getUrl();
		case InternalPath:
			return WApplication.getInstance().getBookmarkUrl(
					this.getInternalPath());
		}
		return "";
	}

	/**
	 * Sets the link resource.
	 * <p>
	 * This sets the type to {@link WLink.Type#Resource}.
	 */
	public void setResource(WResource resource) {
		this.type_ = WLink.Type.Resource;
		this.value_ = resource;
	}

	/**
	 * Returns the link resource.
	 * <p>
	 * This returns the resource previously set using
	 * {@link WLink#setResource(WResource resource) setResource()}, or
	 * <code>null</code>.
	 * <p>
	 * 
	 * @see WLink#setResource(WResource resource)
	 */
	public WResource getResource() {
		if (this.type_ == WLink.Type.Resource) {
			return ((WResource) this.value_);
		} else {
			return null;
		}
	}

	/**
	 * Sets the link internal path.
	 * <p>
	 * This points the link to the given internal path.
	 */
	public void setInternalPath(final String internalPath) {
		this.type_ = WLink.Type.InternalPath;
		String path = internalPath;
		if (path.startsWith("#/")) {
			path = path.substring(1);
		}
		this.value_ = path;
	}

	/**
	 * Returns the internal path.
	 * <p>
	 * This returns the internal path perviously set using
	 * {@link WLink#setInternalPath(String internalPath) setInternalPath()}, or
	 * an empty string otherwise.
	 * <p>
	 * 
	 * @see WLink#setInternalPath(String internalPath)
	 */
	public String getInternalPath() {
		if (this.type_ == WLink.Type.InternalPath) {
			return ((String) this.value_);
		} else {
			return "";
		}
	}

	/**
	 * Sets the location where the linked content should be displayed.
	 * <p>
	 * By default, the linked content is displayed in the application ({@link }).
	 * When the destination is an HTML document, the application is replaced
	 * with the new document. When the link is to a document that cannot be
	 * displayed in the browser, it is offered for download or opened using an
	 * external program, depending on browser settings.
	 * <p>
	 * By setting <code>target</code> to {@link }, the destination is displayed
	 * in a new browser window or tab.
	 * <p>
	 */
	public void setTarget(AnchorTarget target) {
		this.target_ = target;
	}

	/**
	 * Returns the location where the linked content should be displayed.
	 * <p>
	 * 
	 * @see WLink#setTarget(AnchorTarget target)
	 */
	public AnchorTarget getTarget() {
		return this.target_;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 */
	public boolean equals(final WLink other) {
		return this.type_ == other.type_ && this.value_.equals(other.value_);
	}

	String resolveUrl(WApplication app) {
		String relativeUrl = "";
		switch (this.type_) {
		case InternalPath: {
			if (app.getEnvironment().hasAjax()) {
				relativeUrl = app.getBookmarkUrl(this.getInternalPath());
			} else {
				if (app.getEnvironment().agentIsSpiderBot()) {
					relativeUrl = app.getBookmarkUrl(this.getInternalPath());
				} else {
					relativeUrl = app.getSession().getMostRelativeUrl(
							this.getInternalPath());
				}
			}
		}
			break;
		default:
			relativeUrl = this.getUrl();
		}
		return app.resolveRelativeUrl(relativeUrl);
	}

	private WLink.Type type_;
	private Object value_;
	private AnchorTarget target_;

	JSlot manageInternalPathChange(WApplication app, WInteractWidget widget,
			JSlot slot) {
		if (this.type_ == WLink.Type.InternalPath) {
			if (app.getEnvironment().hasAjax()) {
				if (!(slot != null)) {
					slot = new JSlot();
					widget.clicked().addListener(slot);
					widget.clicked().preventDefaultAction();
				}
				slot.setJavaScript("function(){" + app.getJavaScriptClass()
						+ "._p_.setHash("
						+ WWebWidget.jsStringLiteral(this.getInternalPath())
						+ ",true);}");
				widget.clicked().senderRepaint();
				return slot;
			}
		}
		;
		return null;
	}
}
