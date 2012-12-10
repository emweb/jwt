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
 * A widget that adds scrolling capabilities to its content.
 * <p>
 * 
 * Use a {@link WScrollArea} to add scrolling capabilities to another widget.
 * When the content is bigger than the {@link WScrollArea}, scrollbars are added
 * so that the user can still view the entire content.
 * <p>
 * Use {@link WScrollArea#setScrollBarPolicy(WScrollArea.ScrollBarPolicy policy)
 * setScrollBarPolicy()} to configure if and when the scrollbars may appear.
 * <p>
 * In many cases, it might be easier to use the CSS overflow property on a
 * {@link WContainerWidget} (see
 * {@link WContainerWidget#setOverflow(WContainerWidget.Overflow value, EnumSet orientation)
 * WContainerWidget#setOverflow()}). However, this class will behave better when
 * used inside a layout manager: in that case it will make sure horizontal
 * scrolling works properly, since otherwise the layout manager would overflow
 * rather than scrollbars appear.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * This widget is rendered using a <code>&lt;div&gt;</code> with a CSS overflow
 * attribute. When in a layout manager it is positioned absolutely. It can be
 * styled using inline or external CSS as appropriate.
 */
public class WScrollArea extends WWebWidget {
	private static Logger logger = LoggerFactory.getLogger(WScrollArea.class);

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
		 * Never show a scrollbar.
		 */
		ScrollBarAlwaysOff,
		/**
		 * Always show a scrollbar.
		 */
		ScrollBarAlwaysOn;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Creates a scroll area.
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
	 * Creates a scroll area.
	 * <p>
	 * Calls {@link #WScrollArea(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WScrollArea() {
		this((WContainerWidget) null);
	}

	public void remove() {
		;
		;
		super.remove();
	}

	/**
	 * Sets the widget that is the content of the scroll area.
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
			widget.setParentWidget(this);
			if (WApplication.getInstance().getEnvironment().agentIsIElt(9)) {
				this.setPositionScheme(PositionScheme.Relative);
				widget.setPositionScheme(PositionScheme.Relative);
			}
		}
	}

	/**
	 * Removes the widget content.
	 */
	public WWidget takeWidget() {
		WWidget result = this.widget_;
		this.widget_ = null;
		this.setWidget((WWidget) null);
		if (result != null) {
			result.setParentWidget((WWidget) null);
		}
		return result;
	}

	/**
	 * Returns the widget content.
	 */
	public WWidget getWidget() {
		return this.widget_;
	}

	/**
	 * Returns the horizontal scrollbar.
	 */
	public WScrollBar getHorizontalScrollBar() {
		return this.horizontalScrollBar_;
	}

	/**
	 * Returns the vertical scrollbar.
	 */
	public WScrollBar getVerticalScrollBar() {
		return this.verticalScrollBar_;
	}

	/**
	 * Sets the policy for both scrollbars.
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
	 * Sets the horizontal scroll bar policy.
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
	 * Sets the vertical scroll bar policy.
	 * <p>
	 * 
	 * @see WScrollArea#setScrollBarPolicy(WScrollArea.ScrollBarPolicy policy)
	 */
	public void setVerticalScrollBarPolicy(WScrollArea.ScrollBarPolicy policy) {
		this.verticalScrollBarPolicy_ = policy;
		this.scrollBarPolicyChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	/**
	 * Returns the horizontal scroll bar policy.
	 * <p>
	 * 
	 * @see WScrollArea#setHorizontalScrollBarPolicy(WScrollArea.ScrollBarPolicy
	 *      policy)
	 */
	public WScrollArea.ScrollBarPolicy getHorizontalScrollBarPolicy() {
		return this.horizontalScrollBarPolicy_;
	}

	/**
	 * Returns the vertical scroll bar policy.
	 * <p>
	 * 
	 * @see WScrollArea#setVerticalScrollBarPolicy(WScrollArea.ScrollBarPolicy
	 *      policy)
	 */
	public WScrollArea.ScrollBarPolicy getVerticalScrollBarPolicy() {
		return this.verticalScrollBarPolicy_;
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

	void updateDom(DomElement element, boolean all) {
		if (this.widgetChanged_ || all) {
			if (this.widget_ != null) {
				element.addChild(this.widget_.createSDomElement(WApplication
						.getInstance()));
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
