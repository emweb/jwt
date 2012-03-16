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
 * A widget that represents a push button.
 * <p>
 * 
 * To act on a button click, connect a slot to the
 * {@link WInteractWidget#clicked() WInteractWidget#clicked()} signal.
 * <p>
 * WPushButton is an {@link WWidget#setInline(boolean inlined) inline} widget.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * The widget corresponds to the HTML <code>&lt;button&gt;</code> tag and has
 * the <code>Wt-btn</code> style. It may be styled through the current theme, or
 * you can override the style using internal or external CSS as appropriate.
 */
public class WPushButton extends WFormWidget {
	private static Logger logger = LoggerFactory.getLogger(WPushButton.class);

	/**
	 * Creates a push button.
	 */
	public WPushButton(WContainerWidget parent) {
		super(parent);
		this.text_ = new WString();
		this.icon_ = new WLink();
		this.link_ = new WLink();
		this.linkTarget_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.redirectJS_ = null;
	}

	/**
	 * Creates a push button.
	 * <p>
	 * Calls {@link #WPushButton(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WPushButton() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates a push button with given label text.
	 */
	public WPushButton(CharSequence text, WContainerWidget parent) {
		super(parent);
		this.text_ = WString.toWString(text);
		this.icon_ = new WLink();
		this.link_ = new WLink();
		this.linkTarget_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.redirectJS_ = null;
	}

	/**
	 * Creates a push button with given label text.
	 * <p>
	 * Calls {@link #WPushButton(CharSequence text, WContainerWidget parent)
	 * this(text, (WContainerWidget)null)}
	 */
	public WPushButton(CharSequence text) {
		this(text, (WContainerWidget) null);
	}

	public void remove() {
		;
		super.remove();
	}

	/**
	 * Sets the button text.
	 */
	public void setText(CharSequence text) {
		if (canOptimizeUpdates() && text.equals(this.text_)) {
			return;
		}
		this.text_ = WString.toWString(text);
		this.flags_.set(BIT_TEXT_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Returns the button text.
	 * <p>
	 * 
	 * @see WPushButton#setText(CharSequence text)
	 */
	public WString getText() {
		return this.text_;
	}

	/**
	 * Sets an icon.
	 * <p>
	 * The icon is placed to the left of the text.
	 */
	public void setIcon(WLink link) {
		if (canOptimizeUpdates() && link.equals(this.icon_)) {
			return;
		}
		this.icon_ = link;
		this.flags_.set(BIT_ICON_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Returns the icon.
	 * <p>
	 * 
	 * @see WPushButton#setIcon(WLink link)
	 */
	public WLink getIcon() {
		return this.icon_;
	}

	/**
	 * Sets a destination link.
	 * <p>
	 * This method can be used to make the button behave like a {@link WAnchor}
	 * (or conversely, an anchor look like a button) and redirect to another URL
	 * when clicked.
	 * <p>
	 * The <code>link</code> may be to a URL, a resource, or an internal path.
	 * <p>
	 * By default, a button does not link to an URL and you should listen to the
	 * {@link WInteractWidget#clicked() WInteractWidget#clicked()} signal to
	 * react to a click event.
	 */
	public void setLink(WLink link) {
		if (link.equals(this.link_)) {
			return;
		}
		this.link_ = link;
		this.flags_.set(BIT_LINK_CHANGED);
		if (link.getType() == WLink.Type.Resource) {
			link.getResource().dataChanged().addListener(this,
					new Signal.Listener() {
						public void trigger() {
							WPushButton.this.resourceChanged();
						}
					});
		}
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyIEMobile));
	}

	/**
	 * Returns the destination link.
	 * <p>
	 * 
	 * @see WPushButton#setLink(WLink link)
	 */
	public WLink getLink() {
		return this.link_;
	}

	/**
	 * Sets a destination URL (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated Use {@link WPushButton#setLink(WLink link) setLink()}
	 *             insteadd.
	 */
	public void setRef(String url) {
		this.setLink(new WLink(url));
	}

	/**
	 * Returns the destination URL (<b>deprecated</b>).
	 * <p>
	 * When the button refers to a resource, the current resource URL is
	 * returned. Otherwise, the URL is returned that was set using
	 * {@link WPushButton#setRef(String url) setRef()}.
	 * <p>
	 * 
	 * @see WPushButton#setRef(String url)
	 * @see WResource#getUrl()
	 * @deprecated Use {@link WPushButton#getLink() getLink()} instead.
	 */
	public String getRef() {
		return this.link_.getUrl();
	}

	/**
	 * Sets a destination resource (<b>deprecated</b>).
	 * <p>
	 * This method can be used to make the button behave like a {@link WAnchor}
	 * (or conversely, an anchor look like a button) and redirect to another
	 * resource when clicked.
	 * <p>
	 * A resource specifies application-dependent content, which may be
	 * generated by your application on demand.
	 * <p>
	 * This sets the <code>resource</code> as the destination of the button, and
	 * is an alternative to {@link WPushButton#setRef(String url) setRef()}. The
	 * resource may be cleared by passing <code>resource</code> =
	 * <code>null</code>.
	 * <p>
	 * The button does not assume ownership of the resource.
	 * <p>
	 * 
	 * @see WPushButton#setRef(String url)
	 * @deprecated Use {@link WPushButton#setLink(WLink link) setLink()}
	 *             instead.
	 */
	public void setResource(WResource resource) {
		this.setLink(new WLink(resource));
	}

	/**
	 * Returns the destination resource (<b>deprecated</b>).
	 * <p>
	 * Returns <code>null</code> if no resource has been set.
	 * <p>
	 * 
	 * @see WPushButton#setResource(WResource resource)
	 * @deprecated Use {@link WPushButton#getLink() getLink()} instead.
	 */
	public WResource getResource() {
		return this.link_.getResource();
	}

	/**
	 * Returns the current value.
	 * <p>
	 * Returns an empty string, since a button has no value.
	 */
	public String getValueText() {
		return "";
	}

	/**
	 * Sets the current value.
	 * <p>
	 * Has no effect, since a button has not value.
	 */
	public void setValueText(String value) {
	}

	/**
	 * Sets the link target.
	 * <p>
	 * This sets the target where the linked contents should be displayed. The
	 * default target is TargetSelf.
	 */
	public void setLinkTarget(AnchorTarget target) {
		this.linkTarget_ = target;
	}

	/**
	 * Returns the location where the linked content should be displayed.
	 * <p>
	 * 
	 * @see WPushButton#setLinkTarget(AnchorTarget target)
	 */
	public AnchorTarget getLinkTarget() {
		return this.linkTarget_;
	}

	public void refresh() {
		if (this.text_.refresh()) {
			this.flags_.set(BIT_TEXT_CHANGED);
			this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		}
		super.refresh();
	}

	private static final int BIT_TEXT_CHANGED = 0;
	private static final int BIT_ICON_CHANGED = 1;
	private static final int BIT_ICON_RENDERED = 2;
	private static final int BIT_LINK_CHANGED = 3;
	private WString text_;
	private WLink icon_;
	private WLink link_;
	private AnchorTarget linkTarget_;
	BitSet flags_;
	private JSlot redirectJS_;

	void updateDom(DomElement element, boolean all) {
		if (all) {
			element.setAttribute("type", "button");
			element.setProperty(Property.PropertyClass, "Wt-btn");
		}
		if (this.flags_.get(BIT_ICON_CHANGED) || all && !this.icon_.isNull()) {
			DomElement image = DomElement
					.createNew(DomElementType.DomElement_IMG);
			image.setProperty(Property.PropertySrc, this.icon_.getUrl());
			image.setId("im" + this.getFormName());
			element.insertChildAt(image, 0);
			this.flags_.set(BIT_ICON_RENDERED);
		}
		if (this.flags_.get(BIT_TEXT_CHANGED) || all) {
			element.setProperty(Property.PropertyInnerHTML, this.text_
					.isLiteral() ? escapeText(this.text_, true).toString()
					: this.text_.toString());
			this.flags_.clear(BIT_TEXT_CHANGED);
		}
		if (this.flags_.get(BIT_LINK_CHANGED) || all && !this.link_.isNull()) {
			if (!this.link_.isNull()) {
				WApplication app = WApplication.getInstance();
				if (!(this.redirectJS_ != null)) {
					this.redirectJS_ = new JSlot();
					this.clicked().addListener(this.redirectJS_);
					if (!app.getEnvironment().hasAjax()) {
						this.clicked().addListener(this,
								new Signal1.Listener<WMouseEvent>() {
									public void trigger(WMouseEvent e1) {
										WPushButton.this.doRedirect();
									}
								});
					}
				}
				if (this.link_.getType() == WLink.Type.InternalPath) {
					this.redirectJS_
							.setJavaScript("function(){Wt3_2_1.history.navigate("
									+ jsStringLiteral(this.link_
											.getInternalPath()) + ",true);}");
				} else {
					if (this.linkTarget_ == AnchorTarget.TargetNewWindow) {
						this.redirectJS_
								.setJavaScript("function(){window.open("
										+ jsStringLiteral(this.link_.getUrl())
										+ ");}");
					} else {
						this.redirectJS_
								.setJavaScript("function(){window.location="
										+ jsStringLiteral(this.link_.getUrl())
										+ ";}");
					}
				}
				this.clicked().senderRepaint();
			} else {
				;
				this.redirectJS_ = null;
			}
		}
		super.updateDom(element, all);
	}

	DomElementType getDomElementType() {
		return DomElementType.DomElement_BUTTON;
	}

	void propagateRenderOk(boolean deep) {
		this.flags_.clear();
		super.propagateRenderOk(deep);
	}

	void getDomChanges(List<DomElement> result, WApplication app) {
		if (this.flags_.get(BIT_ICON_CHANGED)
				&& this.flags_.get(BIT_ICON_RENDERED)) {
			DomElement image = DomElement.getForUpdate("im"
					+ this.getFormName(), DomElementType.DomElement_IMG);
			if (this.icon_.isNull()) {
				image.removeFromParent();
				this.flags_.clear(BIT_ICON_RENDERED);
			} else {
				image.setProperty(Property.PropertySrc, this.icon_.getUrl());
			}
			result.add(image);
			this.flags_.clear(BIT_ICON_CHANGED);
		}
		super.getDomChanges(result, app);
	}

	private void doRedirect() {
		WApplication app = WApplication.getInstance();
		if (!app.getEnvironment().hasAjax()) {
			if (this.link_.getType() == WLink.Type.InternalPath) {
				app.setInternalPath(this.link_.getInternalPath(), true);
			} else {
				app.redirect(this.link_.getUrl());
			}
		}
	}

	private void resourceChanged() {
		this.flags_.set(BIT_LINK_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyIEMobile));
	}
}
