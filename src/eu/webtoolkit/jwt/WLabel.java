package eu.webtoolkit.jwt;

import java.util.EnumSet;
import java.util.List;

/**
 * A label for a form field
 * 
 * 
 * The label may contain an image and/or text. It acts like a proxy for giving
 * focus to a {@link WFormWidget}. When both an image and text are specified,
 * the image is put to the left of the text.
 * <p>
 * Usage example:
 * <p>
 * <code>
 WContainerWidget w = new WContainerWidget(); <br> 
 WLabel label = new WLabel(&quot;Favourite Actress: &quot;, w); <br> 
 WLineEdit edit = new WLineEdit(&quot;Ren&eacute;e Zellweger&quot;, w); <br> 
 label.setBuddy(edit);
</code>
 * <p>
 * The widget corresponds to the HTML <code>&lt;label&gt;</code> tag.
 * <p>
 * WLabel is an {@link WWidget#setInline(boolean inlined) inline} widget.
 */
public class WLabel extends WInteractWidget {
	/**
	 * Construct a {@link WLabel} with empty text and optional parent.
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

	public WLabel() {
		this((WContainerWidget) null);
	}

	/**
	 * Construct a {@link WLabel} with a given text.
	 */
	public WLabel(CharSequence text, WContainerWidget parent) {
		super(parent);
		this.buddy_ = null;
		this.image_ = null;
		this.buddyChanged_ = false;
		this.newImage_ = false;
		this.newText_ = false;
		this.text_ = new WText(text);
		this.text_.setWordWrap(false);
		this.text_.setParent(this);
	}

	public WLabel(CharSequence text) {
		this(text, (WContainerWidget) null);
	}

	/**
	 * Construct a {@link WLabel} with an image.
	 */
	public WLabel(WImage image, WContainerWidget parent) {
		super(parent);
		this.buddy_ = null;
		this.text_ = null;
		this.buddyChanged_ = false;
		this.newImage_ = false;
		this.newText_ = false;
		this.image_ = image;
		this.image_.setParent(this);
	}

	public WLabel(WImage image) {
		this(image, (WContainerWidget) null);
	}

	public void remove() {
		this.setBuddy((WFormWidget) null);
		super.remove();
	}

	/**
	 * Return the buddy of this label.
	 * 
	 * @see WLabel#setBuddy(WFormWidget buddy)
	 */
	public WFormWidget getBuddy() {
		return this.buddy_;
	}

	/**
	 * Set the buddy of this label.
	 * 
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
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	// public void setBuddy(FormField buddy) ;
	/**
	 * Set the label text.
	 */
	public void setText(CharSequence text) {
		if (this.getText().equals(text)) {
			return;
		}
		if (!(this.text_ != null)) {
			this.text_ = new WText();
			this.text_.setWordWrap(false);
			this.text_.setParent(this);
			this.newText_ = true;
			this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		}
		this.text_.setText(text);
	}

	/**
	 * Get the label text.
	 */
	public WString getText() {
		if (this.text_ != null) {
			return this.text_.getText();
		} else {
			return empty;
		}
	}

	/**
	 * Set the image.
	 */
	public void setImage(WImage image, Side side) {
		if (this.image_ != null) {
			if (this.image_ != null)
				this.image_.remove();
		}
		this.image_ = image;
		if (this.image_ != null) {
			this.image_.setParent(this);
			this.imageSide_ = side;
		}
		this.newImage_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	public final void setImage(WImage image) {
		setImage(image, Side.Left);
	}

	/**
	 * Get the image.
	 */
	public WImage getImage() {
		return this.image_;
	}

	/**
	 * Configure word wrapping.
	 * 
	 * When <i>on</i> is true, the widget may break lines, creating a multi-line
	 * text. When <i>on</i> is false, the text will displayed on a single line,
	 * unless the text contains end-of-lines (for {@link TextFormat#PlainText})
	 * or &lt;br /&gt; tags or other block-level tags (for
	 * {@link TextFormat#XHTMLText}).
	 * <p>
	 * The default value is false.
	 * <p>
	 * 
	 * @see WLabel#hasWordWrap()
	 */
	public void setWordWrap(boolean how) {
		if (!(this.text_ != null)) {
			this.text_ = new WText();
			this.text_.setParent(this);
			this.newText_ = true;
			this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		}
		this.text_.setWordWrap(how);
	}

	/**
	 * Returns whether word wrapping is on.
	 * 
	 * @see WLabel#setWordWrap(boolean how)
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

	protected void updateDom(DomElement element, boolean all) {
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

	protected DomElementType getDomElementType() {
		if (this.buddy_ != null) {
			return DomElementType.DomElement_LABEL;
		} else {
			return this.isInline() ? DomElementType.DomElement_SPAN
					: DomElementType.DomElement_DIV;
		}
	}

	protected void getDomChanges(List<DomElement> result, WApplication app) {
		super.getDomChanges(result, app);
		if (this.text_ != null) {
			((WWebWidget) this.text_).getDomChanges(result, app);
		}
		if (this.image_ != null) {
			((WWebWidget) this.image_).getDomChanges(result, app);
		}
	}

	protected void propagateRenderOk(boolean deep) {
		this.newImage_ = false;
		this.newText_ = false;
		this.buddyChanged_ = false;
		super.propagateRenderOk(deep);
	}

	protected void updateImage(DomElement element, boolean all,
			WApplication app, int pos) {
		if (this.newImage_ || all) {
			if (this.image_ != null) {
				element.insertChildAt(((WWebWidget) this.image_)
						.createDomElement(app), pos);
			}
			this.newImage_ = false;
		}
	}

	protected void updateText(DomElement element, boolean all,
			WApplication app, int pos) {
		if (this.newText_ || all) {
			if (this.text_ != null) {
				element.insertChildAt(((WWebWidget) this.text_)
						.createDomElement(app), pos);
			}
			this.newText_ = false;
		}
	}

	protected static WString empty = new WString("");
}
