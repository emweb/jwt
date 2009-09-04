/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.EnumSet;

/**
 * A widget that adds scrolling capabilities to its content
 * <p>
 * 
 * Use a {@link WScrollArea} to add scrolling capabilities to another widget.
 * When the content is bigger than the {@link WScrollArea}, scrollbars are added
 * so that the user can still view the entire content.
 * <p>
 * Use {@link WScrollArea#setScrollBarPolicy(WScrollArea.ScrollBarPolicy policy)
 * setScrollBarPolicy()} to configure if and when the scrollbars may appear.
 * <p>
 * In many cases, it might be easier to use the CSS overflow property
 */
public class WScrollArea extends WWebWidget {
	/**
	 * <p>
	 * brief Policy for showing a scrollbar.
	 */
	public enum ScrollBarPolicy {
		/**
		 * Automatic.
		 */
		ScrollBarAsNeeded,
		/**
		 * Always show a scrollbar.
		 */
		ScrollBarAlwaysOff,
		/**
		 * Never show a scrollbar.
		 */
		ScrollBarAlwaysOn;

		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Create a scroll area.
	 */
	public WScrollArea(WContainerWidget parent) {
		super(parent);
		this.widget_ = null;
		this.widgetChanged_ = false;
		this.scrollBarChanged_ = false;
		this.horizontalScrollBarPolicy_ = WScrollArea.ScrollBarPolicy.ScrollBarAsNeeded;
		this.verticalScrollBarPolicy_ = WScrollArea.ScrollBarPolicy.ScrollBarAsNeeded;
		this.scrollBarPolicyChanged_ = false;
		this.setInline(false);
		this.horizontalScrollBar_ = new WScrollBar(this, Orientation.Horizontal);
		this.verticalScrollBar_ = new WScrollBar(this, Orientation.Vertical);
	}

	/**
	 * Create a scroll area.
	 * <p>
	 * Calls {@link #WScrollArea(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WScrollArea() {
		this((WContainerWidget) null);
	}

	public void remove() {
		/* delete this.horizontalScrollBar_ */;
		/* delete this.verticalScrollBar_ */;
		super.remove();
	}

	/**
	 * Set the widget that is the contents of the scroll area.
	 * <p>
	 * Setting a new widget will delete the previously set widget.
	 */
	public void setWidget(WWidget widget) {
		if (this.widget_ != null)
			this.widget_.remove();
		this.widget_ = widget;
		this.widgetChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		if (widget != null) {
			widget.setParent(this);
		}
	}

	/**
	 * Remove the widget content.
	 */
	public WWidget takeWidget() {
		WWidget result = this.widget_;
		this.widget_ = null;
		this.setWidget((WWidget) null);
		if (result != null) {
			result.setParent((WWidget) null);
		}
		return result;
	}

	/**
	 * Get the widget content.
	 */
	public WWidget getWidget() {
		return this.widget_;
	}

	/**
	 * Get the horizontal scrollbar.
	 */
	public WScrollBar getHorizontalScrollBar() {
		return this.horizontalScrollBar_;
	}

	/**
	 * Get the vertical scrollbar.
	 */
	public WScrollBar getVerticalScrollBar() {
		return this.verticalScrollBar_;
	}

	/**
	 * Set the policy for both scrollbars.
	 * <p>
	 * 
	 * @see WScrollArea#setHorizontalScrollBarPolicy(WScrollArea.ScrollBarPolicy
	 *      policy)
	 * @see WScrollArea#setVerticalScrollBarPolicy(WScrollArea.ScrollBarPolicy
	 *      policy)
	 */
	public void setScrollBarPolicy(WScrollArea.ScrollBarPolicy policy) {
		this.horizontalScrollBarPolicy_ = this.verticalScrollBarPolicy_ = policy;
		this.scrollBarPolicyChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	/**
	 * Set the horizontal scroll bar policy.
	 * <p>
	 * 
	 * @see WScrollArea#setScrollBarPolicy(WScrollArea.ScrollBarPolicy policy)
	 */
	public void setHorizontalScrollBarPolicy(WScrollArea.ScrollBarPolicy policy) {
		this.horizontalScrollBarPolicy_ = policy;
		this.scrollBarPolicyChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	/**
	 * Set the vertical scroll bar policy.
	 * <p>
	 * 
	 * @see WScrollArea#setScrollBarPolicy(WScrollArea.ScrollBarPolicy policy)
	 */
	public void setVerticalScrollBarPolicy(WScrollArea.ScrollBarPolicy policy) {
		this.verticalScrollBarPolicy_ = policy;
		this.scrollBarPolicyChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	private WWidget widget_;
	private boolean widgetChanged_;
	private WScrollBar horizontalScrollBar_;
	private WScrollBar verticalScrollBar_;
	private boolean scrollBarChanged_;
	private WScrollArea.ScrollBarPolicy horizontalScrollBarPolicy_;
	private WScrollArea.ScrollBarPolicy verticalScrollBarPolicy_;
	private boolean scrollBarPolicyChanged_;

	void scrollBarChanged() {
		this.scrollBarChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	protected void updateDom(DomElement element, boolean all) {
		if (this.widgetChanged_ || all) {
			if (this.widget_ != null) {
				element.addChild(this.widget_.getWebWidget().createDomElement(
						WApplication.getInstance()));
			}
			this.widgetChanged_ = false;
		}
		if (this.scrollBarChanged_ || all) {
			if (this.horizontalScrollBar_.tiesChanged_
					|| this.verticalScrollBar_.tiesChanged_) {
				this.horizontalScrollBar_.tiesChanged_ = true;
				this.verticalScrollBar_.tiesChanged_ = true;
			}
			this.horizontalScrollBar_.updateDom(element, all);
			this.verticalScrollBar_.updateDom(element, all);
			this.scrollBarChanged_ = false;
		}
		if (this.scrollBarPolicyChanged_ || all) {
			switch (this.horizontalScrollBarPolicy_) {
			case ScrollBarAsNeeded:
				element.setProperty(Property.PropertyStyleOverflowX, "auto");
				break;
			case ScrollBarAlwaysOff:
				element.setProperty(Property.PropertyStyleOverflowX, "hidden");
				break;
			case ScrollBarAlwaysOn:
				element.setProperty(Property.PropertyStyleOverflowX, "scroll");
				break;
			}
			switch (this.verticalScrollBarPolicy_) {
			case ScrollBarAsNeeded:
				element.setProperty(Property.PropertyStyleOverflowY, "auto");
				break;
			case ScrollBarAlwaysOff:
				element.setProperty(Property.PropertyStyleOverflowY, "hidden");
				break;
			case ScrollBarAlwaysOn:
				element.setProperty(Property.PropertyStyleOverflowY, "scroll");
				break;
			}
			this.scrollBarPolicyChanged_ = false;
		}
		super.updateDom(element, all);
	}

	DomElementType getDomElementType() {
		return DomElementType.DomElement_DIV;
	}
}
