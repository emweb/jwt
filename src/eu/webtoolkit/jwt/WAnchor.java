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
 * A widget that represents an HTML anchor (to link to other documents).
 * <p>
 * 
 * Use an anchor to link to another web page, document, internal application
 * path or a resource (which specifies application-dependent content that may be
 * generated by your application on demand). The anchor may contain a label
 * text, an image, or any other widget (as it inherits from
 * {@link WContainerWidget}). If you link to a document or external url, and do
 * not want the application to terminate when the user follows the anchor, you
 * must use {@link WAnchor#setTarget(AnchorTarget target)
 * setTarget(TargetNewWindow)}. Even for non-HTML documents, this may be
 * important since pending Ajax requests are cancelled if documents are not
 * served within the browser window in certain browsers.
 * <p>
 * WAnchor is an {@link WWidget#setInline(boolean inlined) inline} widget.
 * <p>
 * <p>
 * <i><b>Note: </b>If you set a text or image using one of the API methods like
 * {@link WAnchor#setText(CharSequence text) setText()} or
 * {@link WAnchor#setImage(WImage image) setImage()} or a constructor, you
 * should not attempt to remove all contents (using
 * {@link WContainerWidget#clear() WContainerWidget#clear()}, or provide a
 * layout (using {@link WContainerWidget#setLayout(WLayout layout)
 * WContainerWidget#setLayout()}), as this will result in undefined behaviour:
 * the text or image are simply inserted as widgets into the container.</i>
 * </p>
 * <h3>CSS</h3>
 * <p>
 * The widget corresponds to the HTML <code>&lt;a&gt;</code> tag and does not
 * provide styling. It can be styled using inline or external CSS as
 * appropriate.
 */
public class WAnchor extends WContainerWidget {
	private static Logger logger = LoggerFactory.getLogger(WAnchor.class);

	/**
	 * Creates an anchor.
	 */
	public WAnchor(WContainerWidget parent) {
		super(parent);
		this.link_ = new WLink();
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
	}

	/**
	 * Creates an anchor.
	 * <p>
	 * Calls {@link #WAnchor(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WAnchor() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates an anchor for the given link.
	 * <p>
	 * The <code>link</code> may point to a URL, a dynamic resource, or an
	 * internal path.
	 * <p>
	 * 
	 * @see WAnchor#setLink(WLink link)
	 */
	public WAnchor(WLink link, WContainerWidget parent) {
		super(parent);
		this.link_ = new WLink();
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
		this.setLink(link);
	}

	/**
	 * Creates an anchor for the given link.
	 * <p>
	 * Calls {@link #WAnchor(WLink link, WContainerWidget parent) this(link,
	 * (WContainerWidget)null)}
	 */
	public WAnchor(WLink link) {
		this(link, (WContainerWidget) null);
	}

	/**
	 * Creates an anchor linking to a URL (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated Use
	 *             {@link WAnchor#WAnchor(WLink link, WContainerWidget parent)
	 *             WAnchor()} instead.
	 */
	public WAnchor(String ref, WContainerWidget parent) {
		super(parent);
		this.link_ = new WLink(WLink.Type.Url, ref);
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
	}

	/**
	 * Creates an anchor linking to a URL (<b>deprecated</b>).
	 * <p>
	 * Calls {@link #WAnchor(String ref, WContainerWidget parent) this(ref,
	 * (WContainerWidget)null)}
	 */
	public WAnchor(String ref) {
		this(ref, (WContainerWidget) null);
	}

	/**
	 * Creates an anchor linking to a resource (<b>deprecated</b>).
	 * <p>
	 * The <code>resource</code> specifies application-dependent content that
	 * may be generated by your application on demand.
	 * <p>
	 * The anchor does not assume ownership of <code>resource</code>, so that
	 * you may share the same resources for several anchors.
	 * <p>
	 * 
	 * @deprecated Use
	 *             {@link WAnchor#WAnchor(WLink link, WContainerWidget parent)
	 *             WAnchor()} instead.
	 */
	public WAnchor(WResource resource, WContainerWidget parent) {
		super(parent);
		this.link_ = new WLink();
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
		this.setResource(resource);
	}

	/**
	 * Creates an anchor linking to a resource (<b>deprecated</b>).
	 * <p>
	 * Calls {@link #WAnchor(WResource resource, WContainerWidget parent)
	 * this(resource, (WContainerWidget)null)}
	 */
	public WAnchor(WResource resource) {
		this(resource, (WContainerWidget) null);
	}

	/**
	 * Creates an anchor for the given link with a text.
	 * <p>
	 * The <code>link</code> may point to a URL, a dynamic resource, or an
	 * internal path.
	 * <p>
	 * 
	 * @see WAnchor#setLink(WLink link)
	 * @see WAnchor#setText(CharSequence text)
	 */
	public WAnchor(WLink link, CharSequence text, WContainerWidget parent) {
		super(parent);
		this.link_ = new WLink();
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
		this.setLink(link);
		this.text_ = new WText(text, this);
	}

	/**
	 * Creates an anchor for the given link with a text.
	 * <p>
	 * Calls
	 * {@link #WAnchor(WLink link, CharSequence text, WContainerWidget parent)
	 * this(link, text, (WContainerWidget)null)}
	 */
	public WAnchor(WLink link, CharSequence text) {
		this(link, text, (WContainerWidget) null);
	}

	/**
	 * Creates an anchor linking to a URL with a text (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated Use
	 *             {@link WAnchor#WAnchor(WLink link, CharSequence text, WContainerWidget parent)
	 *             WAnchor()} instead.
	 */
	public WAnchor(String ref, CharSequence text, WContainerWidget parent) {
		super(parent);
		this.link_ = new WLink(WLink.Type.Url, ref);
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
		this.text_ = new WText(text, this);
	}

	/**
	 * Creates an anchor linking to a URL with a text (<b>deprecated</b>).
	 * <p>
	 * Calls
	 * {@link #WAnchor(String ref, CharSequence text, WContainerWidget parent)
	 * this(ref, text, (WContainerWidget)null)}
	 */
	public WAnchor(String ref, CharSequence text) {
		this(ref, text, (WContainerWidget) null);
	}

	/**
	 * Creates an anchor linking to a resource with a text (<b>deprecated</b>).
	 * <p>
	 * The <code>resource</code> specifies application-dependent content that
	 * may be generated by your application on demand.
	 * <p>
	 * The anchor does not assume ownership of <code>resource</code>, so that
	 * you may share the same resources for several anchors.
	 * <p>
	 * 
	 * @deprecated Use
	 *             {@link WAnchor#WAnchor(WLink link, CharSequence text, WContainerWidget parent)
	 *             WAnchor()} instead.
	 */
	public WAnchor(WResource resource, CharSequence text,
			WContainerWidget parent) {
		super(parent);
		this.link_ = new WLink();
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
		this.setResource(resource);
		this.text_ = new WText(text, this);
	}

	/**
	 * Creates an anchor linking to a resource with a text (<b>deprecated</b>).
	 * <p>
	 * Calls
	 * {@link #WAnchor(WResource resource, CharSequence text, WContainerWidget parent)
	 * this(resource, text, (WContainerWidget)null)}
	 */
	public WAnchor(WResource resource, CharSequence text) {
		this(resource, text, (WContainerWidget) null);
	}

	/**
	 * Creates an anchor for the given link with an image.
	 * <p>
	 * 
	 * @see WAnchor#setLink(WLink link)
	 * @see WAnchor#setImage(WImage image)
	 */
	public WAnchor(WLink link, WImage image, WContainerWidget parent) {
		super(parent);
		this.link_ = new WLink();
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
		this.setLink(link);
		this.image_ = image;
		if (this.image_ != null) {
			this.addWidget(this.image_);
		}
	}

	/**
	 * Creates an anchor for the given link with an image.
	 * <p>
	 * Calls {@link #WAnchor(WLink link, WImage image, WContainerWidget parent)
	 * this(link, image, (WContainerWidget)null)}
	 */
	public WAnchor(WLink link, WImage image) {
		this(link, image, (WContainerWidget) null);
	}

	/**
	 * Creates an anchor linking to a URL with an image (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated Use
	 *             {@link WAnchor#WAnchor(WLink link, WImage image, WContainerWidget parent)
	 *             WAnchor()} instead.
	 */
	public WAnchor(String ref, WImage image, WContainerWidget parent) {
		super(parent);
		this.link_ = new WLink(WLink.Type.Url, ref);
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
		this.image_ = image;
		if (this.image_ != null) {
			this.addWidget(this.image_);
		}
	}

	/**
	 * Creates an anchor linking to a URL with an image (<b>deprecated</b>).
	 * <p>
	 * Calls {@link #WAnchor(String ref, WImage image, WContainerWidget parent)
	 * this(ref, image, (WContainerWidget)null)}
	 */
	public WAnchor(String ref, WImage image) {
		this(ref, image, (WContainerWidget) null);
	}

	/**
	 * Creates an anchor linking to a resource with an image
	 * (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated Use
	 *             {@link WAnchor#WAnchor(WLink link, WImage image, WContainerWidget parent)
	 *             WAnchor()} instead.
	 */
	public WAnchor(WResource resource, WImage image, WContainerWidget parent) {
		super(parent);
		this.link_ = new WLink();
		this.text_ = null;
		this.image_ = null;
		this.target_ = AnchorTarget.TargetSelf;
		this.flags_ = new BitSet();
		this.changeInternalPathJS_ = null;
		this.setInline(true);
		this.setResource(resource);
		this.image_ = image;
		if (this.image_ != null) {
			this.addWidget(this.image_);
		}
	}

	/**
	 * Creates an anchor linking to a resource with an image
	 * (<b>deprecated</b>).
	 * <p>
	 * Calls
	 * {@link #WAnchor(WResource resource, WImage image, WContainerWidget parent)
	 * this(resource, image, (WContainerWidget)null)}
	 */
	public WAnchor(WResource resource, WImage image) {
		this(resource, image, (WContainerWidget) null);
	}

	public void remove() {
		;
		super.remove();
	}

	/**
	 * Sets the link.
	 * <p>
	 * The link may hold a URL, a resource, or an internal path.
	 * <p>
	 * When the link points to a {@link WLink.Type#Resource resource}, the
	 * contents of the link may be generated by your application on demand.
	 * <p>
	 * When the link points to an {@link WLink.Type#InternalPath internal path},
	 * activating the anchor will change the
	 * {@link WApplication#getBookmarkUrl() application&apos;s internal path} or
	 * open a new session with the given path as
	 * {@link WEnvironment#getInternalPath() initial path}). This is the easiest
	 * way to let the application participate in browser history, and generate
	 * URLs that are bookmarkable and search engine friendly.
	 */
	public void setLink(WLink link) {
		if (this.link_.getType() != WLink.Type.Resource
				&& this.link_.equals(link)) {
			return;
		}
		if (this.link_.isNull()) {
			this.flags_.set(BIT_CHANGE_TAG);
		}
		this.link_ = link;
		this.flags_.set(BIT_LINK_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyIEMobile));
		switch (this.link_.getType()) {
		case Resource:
			this.link_.getResource().dataChanged().addListener(this,
					new Signal.Listener() {
						public void trigger() {
							WAnchor.this.resourceChanged();
						}
					});
			break;
		case InternalPath:
			WApplication.getInstance().enableInternalPaths();
			break;
		default:
			break;
		}
	}

	/**
	 * Returns the link.
	 * <p>
	 * 
	 * @see WAnchor#setLink(WLink link)
	 */
	public WLink getLink() {
		return this.link_;
	}

	/**
	 * Sets the linked URL (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated Use {@link WAnchor#setLink(WLink link) setLink()} instead.
	 */
	public void setRef(String url) {
		this.setLink(new WLink(WLink.Type.Url, url));
	}

	/**
	 * Sets a link to an internal path (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated Use {@link WAnchor#setLink(WLink link) setLink()} instead.
	 */
	public void setRefInternalPath(String path) {
		this.setLink(new WLink(WLink.Type.InternalPath, path));
	}

	// public String getRef() ;
	/**
	 * Sets a destination resource (<b>deprecated</b>).
	 * <p>
	 * The anchor does not assume ownership of the resource.
	 * <p>
	 * 
	 * @deprecated Use {@link WAnchor#setLink(WLink link) setLink()} instead.
	 */
	public void setResource(WResource resource) {
		this.setLink(new WLink(resource));
	}

	// public WResource getResource() ;
	/**
	 * Sets the label text.
	 * <p>
	 * If no text was previously set, a new {@link WText} widget is added using
	 * {@link WContainerWidget#addWidget(WWidget widget)
	 * WContainerWidget#addWidget()}.
	 */
	public void setText(CharSequence text) {
		if (!(this.text_ != null)) {
			this.text_ = new WText(text, this);
		} else {
			if (!(text.length() == 0)) {
				this.text_.setText(text);
			} else {
				if (this.text_ != null)
					this.text_.remove();
				this.text_ = null;
			}
		}
	}

	/**
	 * Returns the label text.
	 * <p>
	 * Returns an empty string if no label was set.
	 * <p>
	 * 
	 * @see WAnchor#setText(CharSequence text)
	 */
	public WString getText() {
		if (this.text_ != null) {
			return this.text_.getText();
		} else {
			return empty;
		}
	}

	/**
	 * Configures text word wrapping.
	 * <p>
	 * When <code>wordWrap</code> is <code>true</code>, the text set with
	 * {@link WAnchor#setText(CharSequence text) setText()} may be broken up
	 * over multiple lines. When <code>wordWrap</code> is <code>false</code>,
	 * the text will displayed on a single line, unless the text contains
	 * <code>&lt;br /&gt;</code> tags or other block-level tags.
	 * <p>
	 * The default value is <code>true</code>.
	 * <p>
	 * 
	 * @see WAnchor#hasWordWrap()
	 */
	public void setWordWrap(boolean wordWrap) {
		if (!(this.text_ != null)) {
			this.text_ = new WText(this);
		}
		this.text_.setWordWrap(wordWrap);
	}

	/**
	 * Configures the text format.
	 * <p>
	 * The default text format is XHTMLText.
	 * <p>
	 * 
	 * @see WText#setTextFormat(TextFormat textFormat)
	 */
	public void setTextFormat(TextFormat textFormat) {
		if (!(this.text_ != null)) {
			this.text_ = new WText(this);
		}
		this.text_.setTextFormat(textFormat);
	}

	/**
	 * Returns the text format.
	 * <p>
	 * 
	 * @see WAnchor#setTextFormat(TextFormat textFormat)
	 */
	public TextFormat getTextFormat() {
		return this.text_ != null ? this.text_.getTextFormat()
				: TextFormat.XHTMLText;
	}

	/**
	 * Returns whether the widget may break lines.
	 * <p>
	 * 
	 * @see WAnchor#setWordWrap(boolean wordWrap)
	 */
	public boolean hasWordWrap() {
		return this.text_ != null ? this.text_.isWordWrap() : true;
	}

	/**
	 * Sets an image.
	 * <p>
	 * If an image was previously set, it is deleted. The <code>image</code> is
	 * added using {@link WContainerWidget#addWidget(WWidget widget)
	 * WContainerWidget#addWidget()}.
	 * <p>
	 * Ownership of the image is transferred to the anchor.
	 */
	public void setImage(WImage image) {
		if (this.image_ != null)
			this.image_.remove();
		this.image_ = image;
		if (this.image_ != null) {
			this.addWidget(this.image_);
		}
	}

	/**
	 * Returns the image.
	 * <p>
	 * Returns <code>null</code> if no image is set.
	 * <p>
	 * 
	 * @see WAnchor#setImage(WImage image)
	 */
	public WImage getImage() {
		return this.image_;
	}

	/**
	 * Sets the location where the linked content should be displayed.
	 * <p>
	 * By default, the linked content is displayed in the application (
	 * {@link AnchorTarget#TargetSelf}). When the destination is an HTML
	 * document, the application is replaced with the new document. When the
	 * link is to a document that cannot be displayed in the browser, it is
	 * offered for download or opened using an external program, depending on
	 * browser settings.
	 * <p>
	 * By setting <code>target</code> to {@link AnchorTarget#TargetNewWindow},
	 * the destination is displayed in a new browser window or tab.
	 * <p>
	 * 
	 * @see WAnchor#getTarget()
	 */
	public void setTarget(AnchorTarget target) {
		if (this.target_ != target) {
			this.target_ = target;
			this.flags_.set(BIT_TARGET_CHANGED);
		}
	}

	/**
	 * Returns the location where the linked content should be displayed.
	 * <p>
	 * 
	 * @see WAnchor#setTarget(AnchorTarget target)
	 */
	public AnchorTarget getTarget() {
		return this.target_;
	}

	private static final int BIT_LINK_CHANGED = 0;
	private static final int BIT_TARGET_CHANGED = 1;
	private static final int BIT_CHANGE_TAG = 2;
	private WLink link_;
	private WText text_;
	private WImage image_;
	private AnchorTarget target_;
	BitSet flags_;
	private JSlot changeInternalPathJS_;

	private void resourceChanged() {
		this.flags_.set(BIT_LINK_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyIEMobile));
	}

	void updateDom(DomElement element, boolean all) {
		if (element.getType() != DomElementType.DomElement_A) {
			super.updateDom(element, all);
			return;
		}
		if (this.flags_.get(BIT_CHANGE_TAG)) {
			if (!all) {
				element.callJavaScript("Wt3_2_0.changeTag(" + this.getJsRef()
						+ ",'a');");
			}
			this.flags_.clear(BIT_CHANGE_TAG);
		}
		boolean needsUrlResolution = false;
		if (this.flags_.get(BIT_LINK_CHANGED) || all) {
			WApplication app = WApplication.getInstance();
			String url = this.link_.resolveUrl(app);
			if (this.target_ == AnchorTarget.TargetSelf) {
				this.changeInternalPathJS_ = this.link_
						.manageInternalPathChange(app, this,
								this.changeInternalPathJS_);
			} else {
				;
				this.changeInternalPathJS_ = null;
			}
			url = app.encodeUntrustedUrl(url);
			String href = resolveRelativeUrl(url);
			element.setAttribute("href", href);
			needsUrlResolution = !app.getEnvironment().hashInternalPaths()
					&& href.indexOf("://") == -1 && href.charAt(0) != '/';
			this.flags_.clear(BIT_LINK_CHANGED);
		}
		if (this.flags_.get(BIT_TARGET_CHANGED) || all) {
			switch (this.target_) {
			case TargetSelf:
				if (!all) {
					element.setProperty(Property.PropertyTarget, "_self");
				}
				break;
			case TargetThisWindow:
				element.setProperty(Property.PropertyTarget, "_top");
				break;
			case TargetNewWindow:
				element.setProperty(Property.PropertyTarget, "_blank");
			}
			this.flags_.clear(BIT_TARGET_CHANGED);
		}
		super.updateDom(element, all);
		if (needsUrlResolution) {
			if (all) {
				element.setProperty(Property.PropertyClass, StringUtils
						.addWord(this.getStyleClass(), "Wt-rr"));
			} else {
				element.callJavaScript("$('#" + this.getId()
						+ "').addClass('Wt-rr');");
			}
		}
	}

	DomElementType getDomElementType() {
		return this.link_.isNull() ? super.getDomElementType()
				: DomElementType.DomElement_A;
	}

	void propagateRenderOk(boolean deep) {
		this.flags_.clear(BIT_LINK_CHANGED);
		this.flags_.clear(BIT_TARGET_CHANGED);
		super.propagateRenderOk(deep);
	}

	protected void enableAjax() {
		if (this.link_.getType() == WLink.Type.InternalPath) {
			this.flags_.set(BIT_LINK_CHANGED);
			this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyIEMobile));
		}
		super.enableAjax();
	}

	static WString empty = new WString("");
}
