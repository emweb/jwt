/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.List;

/**
 * A widget that represents a push button
 * <p>
 * 
 * To act on a button click, connect a slot to the
 * {@link WInteractWidget#clicked() WInteractWidget#clicked()} signal.
 * <p>
 * The widget corresponds to the HTML <code>&lt;button&gt;</code> tag.
 * <p>
 * WPushButton is an {@link WWidget#setInline(boolean inlined) inline} widget.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * You may customize the look of a push button using the <code>Wt-btn</code> CSS
 * class.
 */
public class WPushButton extends WFormWidget {
	/**
	 * Creates a push button.
	 */
	public WPushButton(WContainerWidget parent) {
		super(parent);
		this.text_ = new WString();
		this.icon_ = "";
		this.flags_ = new BitSet();
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
		this.icon_ = "";
		this.flags_ = new BitSet();
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
	public void setIcon(String url) {
		if (canOptimizeUpdates() && url.equals(this.icon_)) {
			return;
		}
		this.icon_ = url;
		this.flags_.set(BIT_ICON_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Returns the icon.
	 * <p>
	 * 
	 * @see WPushButton#setIcon(String url)
	 */
	public String getIcon() {
		return this.icon_;
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
	private WString text_;
	private String icon_;
	BitSet flags_;

	void updateDom(DomElement element, boolean all) {
		if (all) {
			element.setAttribute("type", "button");
			element.setProperty(Property.PropertyClass, "Wt-btn");
		}
		if (this.flags_.get(BIT_ICON_CHANGED) || all
				&& this.icon_.length() != 0) {
			DomElement image = DomElement
					.createNew(DomElementType.DomElement_IMG);
			image.setProperty(Property.PropertySrc, this.icon_);
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
			if (this.icon_.length() == 0) {
				image.removeFromParent();
				this.flags_.clear(BIT_ICON_RENDERED);
			} else {
				image.setProperty(Property.PropertySrc, this.icon_);
			}
			result.add(image);
			this.flags_.clear(BIT_ICON_CHANGED);
		}
		super.getDomChanges(result, app);
	}
}
