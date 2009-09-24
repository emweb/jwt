/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.EnumSet;

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
 */
public class WPushButton extends WFormWidget {
	/**
	 * Create a push button with empty label.
	 */
	public WPushButton(WContainerWidget parent) {
		super(parent);
		this.text_ = new WString();
		this.textChanged_ = false;
	}

	/**
	 * Create a push button with empty label.
	 * <p>
	 * Calls {@link #WPushButton(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WPushButton() {
		this((WContainerWidget) null);
	}

	/**
	 * Create a push button with given label.
	 */
	public WPushButton(CharSequence text, WContainerWidget parent) {
		super(parent);
		this.text_ = WString.toWString(text);
		this.textChanged_ = false;
	}

	/**
	 * Create a push button with given label.
	 * <p>
	 * Calls {@link #WPushButton(CharSequence text, WContainerWidget parent)
	 * this(text, (WContainerWidget)null)}
	 */
	public WPushButton(CharSequence text) {
		this(text, (WContainerWidget) null);
	}

	/**
	 * Set the button text.
	 */
	public void setText(CharSequence text) {
		if (canOptimizeUpdates() && text.equals(this.text_)) {
			return;
		}
		this.text_ = WString.toWString(text);
		this.textChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Get the button text.
	 */
	public WString getText() {
		return this.text_;
	}

	public void refresh() {
		if (this.text_.refresh()) {
			this.textChanged_ = true;
			this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		}
		super.refresh();
	}

	private WString text_;
	private boolean textChanged_;

	void updateDom(DomElement element, boolean all) {
		if (all) {
			element.setAttribute("type", "button");
		}
		if (this.textChanged_ || all) {
			element.setProperty(Property.PropertyInnerHTML, this.text_
					.isLiteral() ? escapeText(this.text_, true).toString()
					: this.text_.toString());
			this.textChanged_ = false;
		}
		super.updateDom(element, all);
	}

	DomElementType getDomElementType() {
		return DomElementType.DomElement_BUTTON;
	}

	void propagateRenderOk(boolean deep) {
		this.textChanged_ = false;
		super.propagateRenderOk(deep);
	}
}
