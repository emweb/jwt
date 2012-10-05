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
 * A WPanel provides a container with a title bar.
 * <p>
 * 
 * The panel provides a container with an optional title bar, and an optional
 * collapse icon.
 * <p>
 * <div align="center"> <img src="doc-files//WPanel-default-1.png"
 * alt="Two panels: one collapsed and one expanded (default theme)">
 * <p>
 * <strong>Two panels: one collapsed and one expanded (default theme)</strong>
 * </p>
 * </div> <div align="center"> <img src="doc-files//WPanel-polished-1.png"
 * alt="Two panels: one collapsed and one expanded (polished theme)">
 * <p>
 * <strong>Two panels: one collapsed and one expanded (polished theme)</strong>
 * </p>
 * </div>
 * <p>
 * <h3>CSS</h3>
 * <p>
 * A panel has the <code>Wt-panel</code> and <code>Wt-outset</code> style
 * classes. The look can be overridden using the following style class
 * selectors:
 * <p>
 * <div class="fragment">
 * 
 * <pre class="fragment">
 * .Wt-panel .titlebar : The title bar
 *  .Wt-panel .body     : The body (requires vertical padding 4px).
 * </pre>
 * 
 * </div>
 */
public class WPanel extends WCompositeWidget {
	private static Logger logger = LoggerFactory.getLogger(WPanel.class);

	/**
	 * Creates a panel.
	 */
	public WPanel(WContainerWidget parent) {
		super(parent);
		this.collapseIcon_ = null;
		this.title_ = null;
		this.centralWidget_ = null;
		this.animation_ = new WAnimation();
		this.collapsed_ = new Signal(this);
		this.expanded_ = new Signal(this);
		this.collapsedSS_ = new Signal1<Boolean>(this);
		this.expandedSS_ = new Signal1<Boolean>(this);
		String TEMPLATE = "${shadow-x1-x2}${titlebar}${contents}";
		this
				.setImplementation(this.impl_ = new WTemplate(new WString(
						TEMPLATE)));
		this.impl_.setStyleClass("Wt-panel Wt-outset");
		// this.implementStateless(WPanel.doExpand,WPanel.undoExpand);
		// this.implementStateless(WPanel.doCollapse,WPanel.undoCollapse);
		WContainerWidget centralArea = new WContainerWidget();
		centralArea.setStyleClass("body");
		this.impl_.bindString("shadow-x1-x2", WTemplate.DropShadow_x1_x2);
		this.impl_.bindWidget("titlebar", (WWidget) null);
		this.impl_.bindWidget("contents", centralArea);
		this
				.setJavaScriptMember(
						WT_RESIZE_JS,
						"function(self, w, h) {if (Wt3_2_2.boxSizing(self)) {h -= Wt3_2_2.px(self, 'borderTopWidth') + Wt3_2_2.px(self, 'borderBottomWidth');}var c = self.lastChild;var t = c.previousSibling;if (t.className == 'titlebar')h -= t.offsetHeight;h -= 8;if (h > 0) {c.style.height = h + 'px';$(c).children().each(function() { var self = $(this), padding = self.outerHeight() - self.height();self.height(h - padding);});}};");
		this.setJavaScriptMember(WT_GETPS_JS, StdWidgetItemImpl
				.getSecondGetPSJS());
	}

	/**
	 * Creates a panel.
	 * <p>
	 * Calls {@link #WPanel(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WPanel() {
		this((WContainerWidget) null);
	}

	/**
	 * Sets a title.
	 * <p>
	 * The panel title is set in the title bar. This method also makes the title
	 * bar visible by calling setTitleBar(true).
	 * <p>
	 * The default value is &quot;&quot; (no title).
	 * <p>
	 * 
	 * @see WPanel#getTitle()
	 * @see WPanel#setTitleBar(boolean enable)
	 */
	public void setTitle(CharSequence title) {
		this.setTitleBar(true);
		if (!(this.title_ != null)) {
			this.title_ = new WText();
			this.getTitleBarWidget().insertWidget(
					this.getTitleBarWidget().getCount() - 1, this.title_);
		}
		this.title_.setText(title);
	}

	/**
	 * Returns the title.
	 * <p>
	 * 
	 * @see WPanel#setTitle(CharSequence title)
	 */
	public WString getTitle() {
		if (this.title_ != null) {
			return this.title_.getText();
		} else {
			return new WString();
		}
	}

	/**
	 * Shows or hides the title bar for the panel.
	 * <p>
	 * The title bar appears at the top of the panel.
	 * <p>
	 * The default value is <code>false:</code> the title bar is not shown
	 * unless a title is set or the panel is made collapsible.
	 * <p>
	 * 
	 * @see WPanel#setTitle(CharSequence title)
	 * @see WPanel#setCollapsible(boolean on)
	 */
	public void setTitleBar(boolean enable) {
		if (enable && !(this.getTitleBarWidget() != null)) {
			WContainerWidget titleBar = new WContainerWidget();
			this.impl_.bindWidget("titlebar", titleBar);
			titleBar.setStyleClass("titlebar");
			WBreak br;
			titleBar.addWidget(br = new WBreak());
			br.setClearSides(Side.Horizontals);
		} else {
			if (!enable && this.isTitleBar()) {
				this.impl_.bindWidget("titlebar", (WWidget) null);
				this.title_ = null;
				this.collapseIcon_ = null;
			}
		}
	}

	/**
	 * Returns if a title bar is set.
	 * <p>
	 * 
	 * @see WPanel#setTitleBar(boolean enable)
	 */
	public boolean isTitleBar() {
		return this.getTitleBarWidget() != null;
	}

	/**
	 * Returns the title bar widget.
	 * <p>
	 * The title bar widget contains the collapse/expand icon (if the panel
	 * {@link WPanel#isCollapsible() isCollapsible()}), and the title text (if a
	 * title was set using {@link WPanel#setTitle(CharSequence title)
	 * setTitle()}). You can access the title bar widget to customize the
	 * contents of the title.
	 * <p>
	 * The method returns <code>null</code> if {@link WPanel#isTitleBar()
	 * isTitleBar()} is <code>false</code>. You need to call
	 * {@link WPanel#setTitleBar(boolean enable) setTitleBar()} first.
	 * <p>
	 * 
	 * @see WPanel#setTitleBar(boolean enable)
	 */
	public WContainerWidget getTitleBarWidget() {
		return ((this.impl_.resolveWidget("titlebar")) instanceof WContainerWidget ? (WContainerWidget) (this.impl_
				.resolveWidget("titlebar"))
				: null);
	}

	/**
	 * Makes the panel collapsible.
	 * <p>
	 * When <code>on</code> is <code>true</code>, a collapse/expand icon is
	 * added to the title bar. This also calls setTitleBar(true) to enable the
	 * title bar.
	 * <p>
	 * The default value is <code>false</code>.
	 * <p>
	 * 
	 * @see WPanel#setTitleBar(boolean enable)
	 * @see WPanel#setCollapsed(boolean on)
	 * @see WPanel#isCollapsed()
	 */
	public void setCollapsible(boolean on) {
		if (on && !(this.collapseIcon_ != null)) {
			String resources = WApplication.getResourcesUrl();
			this.setTitleBar(true);
			this.collapseIcon_ = new WIconPair(resources + "collapse.gif",
					resources + "expand.gif");
			this.collapseIcon_.setInline(false);
			this.collapseIcon_.setFloatSide(Side.Left);
			this.getTitleBarWidget().insertWidget(0, this.collapseIcon_);
			this.collapseIcon_.icon1Clicked().addListener(this,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent e1) {
							WPanel.this.doCollapse();
						}
					});
			this.collapseIcon_.icon1Clicked().addListener(this,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent e1) {
							WPanel.this.onCollapse();
						}
					});
			this.collapseIcon_.icon2Clicked().addListener(this,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent e1) {
							WPanel.this.doExpand();
						}
					});
			this.collapseIcon_.icon2Clicked().addListener(this,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent e1) {
							WPanel.this.onExpand();
						}
					});
			this.collapseIcon_.setState(0);
		} else {
			if (!on && this.collapseIcon_ != null) {
				if (this.collapseIcon_ != null)
					this.collapseIcon_.remove();
				this.collapseIcon_ = null;
			}
		}
	}

	/**
	 * Returns if the panel can be collapsed by the user.
	 * <p>
	 * 
	 * @see WPanel#setCollapsible(boolean on)
	 */
	public boolean isCollapsible() {
		return this.collapseIcon_ != null;
	}

	/**
	 * Sets the panel expanded or collapsed.
	 * <p>
	 * When <code>on</code> is <code>true</code>, equivalent to
	 * {@link WPanel#collapse() collapse()}, otherwise to
	 * {@link WPanel#expand() expand()}.
	 * <p>
	 * The default value is <code>false</code>.
	 * <p>
	 * 
	 * @see WPanel#setCollapsible(boolean on)
	 */
	public void setCollapsed(boolean on) {
		if (on) {
			this.collapse();
		} else {
			this.expand();
		}
	}

	/**
	 * Returns if the panel is collapsed.
	 * <p>
	 * 
	 * @see WPanel#setCollapsed(boolean on)
	 * @see WPanel#collapsed()
	 * @see WPanel#expanded()
	 */
	public boolean isCollapsed() {
		return this.isCollapsible() && this.collapseIcon_.getState() == 1;
	}

	/**
	 * Collapses the panel.
	 * <p>
	 * When {@link WPanel#isCollapsible() isCollapsible()} is true, the panel is
	 * collapsed to minimize screen real-estate.
	 * <p>
	 * 
	 * @see WPanel#setCollapsible(boolean on)
	 * @see WPanel#expand()
	 */
	public void collapse() {
		if (this.isCollapsible()) {
			this.collapseIcon_.showIcon2();
			this.doCollapse();
		}
	}

	/**
	 * Collapses the panel.
	 * <p>
	 * When {@link WPanel#isCollapsible() isCollapsible()} is true, the panel is
	 * expanded to its original state.
	 * <p>
	 * 
	 * @see WPanel#setCollapsible(boolean on)
	 * @see WPanel#expand()
	 */
	public void expand() {
		if (this.isCollapsible()) {
			this.collapseIcon_.showIcon1();
			this.doExpand();
		}
	}

	/**
	 * Sets an animation.
	 * <p>
	 * The animation is used when collapsing or expanding the panel.
	 */
	public void setAnimation(WAnimation transition) {
		this.animation_ = transition;
		if (!this.animation_.isEmpty()) {
			this.addStyleClass("Wt-animated");
		}
	}

	/**
	 * Sets the central widget.
	 * <p>
	 * Sets the widget that is the contents of the panel. When a widget was
	 * previously set, the old widget is deleted first.
	 * <p>
	 * The default value is <code>null</code> (no widget set).
	 */
	public void setCentralWidget(WWidget w) {
		if (this.centralWidget_ != null)
			this.centralWidget_.remove();
		this.centralWidget_ = w;
		if (w != null) {
			this.getCentralArea().addWidget(w);
			w.setInline(false);
		}
	}

	/**
	 * Returns the central widget.
	 * <p>
	 * 
	 * @see WPanel#setCentralWidget(WWidget w)
	 */
	public WWidget getCentralWidget() {
		return this.centralWidget_;
	}

	/**
	 * Signal emitted when the panel is collapsed.
	 * <p>
	 * Signal emitted when the panel is collapsed. The signal is only emitted
	 * when the panel is collapsed by the user using the collapse icon in the
	 * tible bar, not when calling {@link WPanel#setCollapsed(boolean on)
	 * setCollapsed()}.
	 * <p>
	 * 
	 * @see WPanel#expanded()
	 */
	public Signal collapsed() {
		return this.collapsed_;
	}

	/**
	 * Signal emitted when the panel is expanded.
	 * <p>
	 * Signal emitted when the panel is expanded. The signal is only emitted
	 * when the panel is expanded by the user using the expand icon in the title
	 * bar, not when calling {@link WPanel#setCollapsed(boolean on)
	 * setCollapsed()}.
	 * <p>
	 * 
	 * @see WPanel#collapsed()
	 */
	public Signal expanded() {
		return this.expanded_;
	}

	Signal1<Boolean> collapsedSS() {
		return this.collapsedSS_;
	}

	Signal1<Boolean> expandedSS() {
		return this.expandedSS_;
	}

	WIconPair getCollapseIcon() {
		return this.collapseIcon_;
	}

	private WIconPair collapseIcon_;
	private WText title_;
	private WTemplate impl_;
	private WWidget centralWidget_;
	private WAnimation animation_;
	private Signal collapsed_;
	private Signal expanded_;
	private Signal1<Boolean> collapsedSS_;
	private Signal1<Boolean> expandedSS_;
	private boolean wasCollapsed_;

	// private void setJsSize() ;
	private void doExpand() {
		this.wasCollapsed_ = this.isCollapsed();
		this.getCentralArea().animateShow(this.animation_);
		this.expandedSS_.trigger(true);
	}

	private void doCollapse() {
		this.wasCollapsed_ = this.isCollapsed();
		this.getCentralArea().animateHide(this.animation_);
		this.collapsedSS_.trigger(true);
	}

	private void undoExpand() {
		if (this.wasCollapsed_) {
			this.collapse();
		}
		this.expandedSS_.trigger(false);
	}

	private void undoCollapse() {
		if (!this.wasCollapsed_) {
			this.expand();
		}
		this.collapsedSS_.trigger(false);
	}

	private void onExpand() {
		this.expanded_.trigger();
	}

	private void onCollapse() {
		this.collapsed_.trigger();
	}

	private WContainerWidget getCentralArea() {
		return ((this.impl_.resolveWidget("contents")) instanceof WContainerWidget ? (WContainerWidget) (this.impl_
				.resolveWidget("contents"))
				: null);
	}
}
