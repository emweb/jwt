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
 * A label for a form field.
 * <p>
 * 
 * The label may contain an image and/or text. It acts like a proxy for giving
 * focus to a {@link WFormWidget}. When both an image and text are specified,
 * the image is put to the left of the text.
 * <p>
 * Usage example:
 * <p>
 * 
 * <pre>
 * {
 * 	&#064;code
 * 	WContainerWidget w = new WContainerWidget();
 * 	WLabel label = new WLabel(&quot;Favourite Actress: &quot;, w);
 * 	WLineEdit edit = new WLineEdit(&quot;Renee Zellweger&quot;, w);
 * 	label.setBuddy(edit);
 * }
 * </pre>
 * <p>
 * The widget corresponds to the HTML <code>&lt;label&gt;</code> tag. When no
 * buddy is set, it is rendered using an HTML <code>&lt;span&gt;</code> or
 * <code>&lt;div&gt;</code> to avoid click event handling misbehavior on
 * Microsoft Internet Explorer.
 * <p>
 * WLabel is an {@link WWidget#setInline(boolean inlined) inline} widget.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * This widget does not provide styling, and can be styled using inline or
 * external CSS as appropriate. A label&apos;s text may be styled via a nested
 * <code>&lt;span&gt;</code> element, and it&apos;s image may be styled via a
 * nested <code>&lt;img&gt;</code> element.
 */
public class WLabel extends WInteractWidget {
	private static Logger logger = LoggerFactory.getLogger(WLabel.class);

	/**
	 * Creates a label with empty text and optional parent.
	 */
	public WLabel(WContainerWidget parent) {
		super(parent);
		this.buddy_ = null;
		this.text_ = null;
		this.image_ = null;
		this.buddyChanged_ = false;
		this.newImage_ = false;
		this.newText_ = false;
	}

	/**
	 * Creates a label with empty text and optional parent.
	 * <p>
	 * Calls {@link #WLabel(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WLabel() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates a label with a given text.
	 */
	public WLabel(final CharSequence text, WContainerWidget parent) {
		super(parent);
		this.buddy_ = null;
		this.image_ = null;
		this.buddyChanged_ = false;
		this.newImage_ = false;
		this.newText_ = false;
		this.text_ = new WText(text);
		this.text_.setWordWrap(false);
		this.text_.setParentWidget(this);
	}

	/**
	 * Creates a label with a given text.
	 * <p>
	 * Calls {@link #WLabel(CharSequence text, WContainerWidget parent)
	 * this(text, (WContainerWidget)null)}
	 */
	public WLabel(final CharSequence text) {
		this(text, (WContainerWidget) null);
	}

	/**
	 * Creates a label with an image.
	 */
	public WLabel(WImage image, WContainerWidget parent) {
		super(parent);
		this.buddy_ = null;
		this.text_ = null;
		this.buddyChanged_ = false;
		this.newImage_ = false;
		this.newText_ = false;
		this.image_ = image;
		this.image_.setParentWidget(this);
	}

	/**
	 * Creates a label with an image.
	 * <p>
	 * Calls {@link #WLabel(WImage image, WContainerWidget parent) this(image,
	 * (WContainerWidget)null)}
	 */
	public WLabel(WImage image) {
		this(image, (WContainerWidget) null);
	}

	public void remove() {
		this.setBuddy((WFormWidget) null);
		super.remove();
	}

	/**
	 * Returns the buddy of this label.
	 * <p>
	 * 
	 * @see WLabel#setBuddy(WFormWidget buddy)
	 */
	public WFormWidget getBuddy() {
		return this.buddy_;
	}

	/**
	 * Sets the buddy of this label.
	 * <p>
	 * Sets the buddy FormWidget for which this label acts as a proxy.
	 * <p>
	 * 
	 * @see WFormWidget#getLabel()
	 * @see WLabel#getBuddy()
	 */
	public void setBuddy(WFormWidget buddy) {
		if (this.buddy_ != null) {
			this.buddy_.setLabel((WLabel) null);
		}
		this.buddy_ = buddy;
		if (this.buddy_ != null) {
			this.buddy_.setLabel(this);
		}
		this.buddyChanged_ = true;
		this.repaint();
	}

	/**
	 * Sets the label text.
	 */
	public void setText(final CharSequence text) {
		if ((this.getText().toString().equals(text.toString()))) {
			return;
		}
		if (!(this.text_ != null)) {
			this.text_ = new WText();
			this.text_.setWordWrap(false);
			this.text_.setParentWidget(this);
			this.newText_ = true;
			this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
		}
		this.text_.setText(text);
	}

	/**
	 * Returns the label text.
	 */
	public WString getText() {
		if (this.text_ != null) {
			return this.text_.getText();
		} else {
			return empty;
		}
	}

	/**
	 * Sets the text format.
	 * <p>
	 * The textFormat controls how the string should be interpreted: either as
	 * plain text, which is displayed literally, or as XHTML-markup.
	 * <p>
	 * When changing the textFormat to {@link TextFormat#XHTMLText}, and the
	 * current text is literal (not created using {@link WString#tr(String key)
	 * WString#tr()}), the current text is parsed using an XML parser which
	 * discards malicious tags and attributes silently. When the parser
	 * encounters an XML parse error, the textFormat is left unchanged, and this
	 * method returns false.
	 * <p>
	 * Returns whether the textFormat could be set for the current text.
	 * <p>
	 * The default format is {@link TextFormat#XHTMLText}.
	 */
	public boolean setTextFormat(TextFormat format) {
		if (!(this.text_ != null)) {
			this.setText("A");
			this.setText("");
		}
		return this.text_.setTextFormat(format);
	}

	/**
	 * Returns the text format.
	 * <p>
	 * 
	 * @see WLabel#setTextFormat(TextFormat format)
	 */
	public TextFormat getTextFormat() {
		if (!(this.text_ != null)) {
			return TextFormat.XHTMLText;
		} else {
			return this.text_.getTextFormat();
		}
	}

	/**
	 * Sets the image.
	 */
	public void setImage(WImage image, Side side) {
		if (this.image_ != null)
			this.image_.remove();
		this.image_ = image;
		if (this.image_ != null) {
			this.image_.setParentWidget(this);
			this.imageSide_ = side;
		}
		this.newImage_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
	}

	/**
	 * Sets the image.
	 * <p>
	 * Calls {@link #setImage(WImage image, Side side) setImage(image,
	 * Side.Left)}
	 */
	public final void setImage(WImage image) {
		setImage(image, Side.Left);
	}

	/**
	 * Returns the image.
	 */
	public WImage getImage() {
		return this.image_;
	}

	/**
	 * Configures word wrapping.
	 * <p>
	 * When <code>wordWrap</code> is <code>true</code>, the widget may break
	 * lines, creating a multi-line text. When <code>wordWrap</code> is
	 * <code>false</code>, the text will displayed on a single line, unless the
	 * text contains end-of-lines (for {@link TextFormat#PlainText}) or &lt;br
	 * /&gt; tags or other block-level tags (for {@link TextFormat#XHTMLText}).
	 * <p>
	 * The default value is <code>false</code>.
	 * <p>
	 * 
	 * @see WLabel#hasWordWrap()
	 */
	public void setWordWrap(boolean wordWrap) {
		if (!(this.text_ != null)) {
			this.text_ = new WText();
			this.text_.setParentWidget(this);
			this.newText_ = true;
			this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
		}
		this.text_.setWordWrap(wordWrap);
	}

	/**
	 * Returns whether word wrapping is on.
	 * <p>
	 * 
	 * @see WLabel#setWordWrap(boolean wordWrap)
	 */
	public boolean hasWordWrap() {
		return this.text_ != null ? this.text_.isWordWrap() : false;
	}

	private WFormWidget buddy_;
	private WText text_;
	private WImage image_;
	private Side imageSide_;
	private boolean buddyChanged_;
	private boolean newImage_;
	private boolean newText_;

	void updateDom(final DomElement element, boolean all) {
		WApplication app = WApplication.getInstance();
		if (this.image_ != null && this.text_ != null) {
			if (this.imageSide_ == Side.Left) {
				this.updateImage(element, all, app, 0);
				this.updateText(element, all, app, 1);
			} else {
				this.updateText(element, all, app, 0);
				this.updateImage(element, all, app, 1);
			}
		} else {
			this.updateText(element, all, app, 0);
			this.updateImage(element, all, app, 0);
		}
		if (this.buddyChanged_ || all) {
			if (this.buddy_ != null) {
				element.setAttribute("for", this.buddy_.getFormName());
			}
			this.buddyChanged_ = false;
		}
		super.updateDom(element, all);
	}

	DomElementType getDomElementType() {
		if (this.buddy_ != null) {
			return DomElementType.DomElement_LABEL;
		} else {
			return this.isInline() ? DomElementType.DomElement_SPAN
					: DomElementType.DomElement_DIV;
		}
	}

	protected void getDomChanges(final List<DomElement> result, WApplication app) {
		super.getDomChanges(result, app);
		if (this.text_ != null) {
			((WWebWidget) this.text_).getDomChanges(result, app);
		}
		if (this.image_ != null) {
			((WWebWidget) this.image_).getDomChanges(result, app);
		}
	}

	void propagateRenderOk(boolean deep) {
		this.newImage_ = false;
		this.newText_ = false;
		this.buddyChanged_ = false;
		super.propagateRenderOk(deep);
	}

	protected void propagateSetEnabled(boolean enabled) {
		if (this.text_ != null) {
			this.text_.propagateSetEnabled(enabled);
		}
		super.propagateSetEnabled(enabled);
	}

	protected void updateImage(final DomElement element, boolean all,
			WApplication app, int pos) {
		if (this.newImage_ || all) {
			if (this.image_ != null) {
				element.insertChildAt(this.image_.createSDomElement(app), pos);
			}
			this.newImage_ = false;
		}
	}

	protected void updateText(final DomElement element, boolean all,
			WApplication app, int pos) {
		if (this.newText_ || all) {
			if (this.text_ != null) {
				element.insertChildAt(this.text_.createSDomElement(app), pos);
			}
			this.newText_ = false;
		}
	}

	static WString empty = new WString("");
}
