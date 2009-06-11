package eu.webtoolkit.jwt;

import java.util.EnumSet;
import java.util.List;

/**
 * A WPanel provides a container with a title bar
 * 
 * 
 * The panel provides a container with an optional title bar, and an optional
 * collapse icon.
 * <p>
 * <div align="center"> <img src="/WPanel-1.png"
 * alt="Two panels: the top panel is collapsed, and the bottom panel expanded">
 * <p>
 * <strong>Two panels: the top panel is collapsed, and the bottom panel
 * expanded</strong>
 * </p>
 * </div>
 */
public class WPanel extends WCompositeWidget {
	/**
	 * Construct a panel.
	 */
	public WPanel(WContainerWidget parent) {
		super(parent);
		this.titleBar_ = null;
		this.collapseIcon_ = null;
		this.title_ = null;
		this.centralWidget_ = null;
		this.collapsed_ = new Signal(this);
		this.expanded_ = new Signal(this);
		this.collapsedSS_ = new Signal1<Boolean>(this);
		this.expandedSS_ = new Signal1<Boolean>(this);
		this.setImplementation(this.impl_ = new WContainerWidget());
		// this.implementStateless(WPanel.doExpand,WPanel.undoExpand);
		// this.implementStateless(WPanel.doCollapse,WPanel.undoCollapse);
		this.impl_.setStyleClass("Wt-panel");
		WContainerWidget centralArea = new WContainerWidget();
		centralArea.setStyleClass("body");
		this.impl_.addWidget(centralArea);
		String CSS_RULES_NAME = "Wt::WPanel";
		WApplication app = WApplication.getInstance();
		if (!app.getStyleSheet().isDefined(CSS_RULES_NAME)) {
			app
					.getStyleSheet()
					.addRule(
							"div.Wt-panel",
							"border: 3px solid #888888;background: #EEEEEE none repeat scroll 0%;",
							CSS_RULES_NAME);
			app
					.getStyleSheet()
					.addRule("div.Wt-panel .titlebar",
							"background: #888888; color: #FFFFFF;padding: 0px 6px 3px;font-size: 10pt;");
			app.getStyleSheet().addRule("div.Wt-panel .body",
					"background: #FFFFFF;padding: 4px 6px 4px;");
		}
	}

	public WPanel() {
		this((WContainerWidget) null);
	}

	/**
	 * Set a title.
	 * 
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
			this.title_ = new WText(this.titleBar_);
		}
		this.title_.setText(title);
	}

	/**
	 * Get the title.
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
	 * Show or hide a title bar for the panel.
	 * 
	 * The title bar appears at the top of the panel.
	 * <p>
	 * The default value is false: the title bar is not shown unless a title is
	 * set or the panel is made collapsible.
	 * <p>
	 * 
	 * @see WPanel#setTitle(CharSequence title)
	 * @see WPanel#setCollapsible(boolean on)
	 */
	public void setTitleBar(boolean enable) {
		if (enable && !(this.titleBar_ != null)) {
			this.titleBar_ = new WContainerWidget();
			this.impl_.insertWidget(0, this.titleBar_);
			this.titleBar_.setStyleClass("titlebar");
		} else {
			if (!enable && this.titleBar_ != null) {
				if (this.titleBar_ != null)
					this.titleBar_.remove();
				this.titleBar_ = null;
				this.title_ = null;
				this.collapseIcon_ = null;
			}
		}
	}

	/**
	 * Returns if a title bar is set.
	 * 
	 * @see WPanel#setTitleBar(boolean enable)
	 */
	public boolean isTitleBar() {
		return this.titleBar_ != null;
	}

	/**
	 * Make the panel collapsible.
	 * 
	 * When <i>on</i> is true, a collapse/expand icon is added to the title bar.
	 * This also calls setTitleBar(true) to enable the title bar.
	 * <p>
	 * The default value is false.
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
			this.collapseIcon_.setMargin(new WLength(2), EnumSet.of(Side.Top));
			this.titleBar_.insertWidget(0, this.collapseIcon_);
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
	 * 
	 * @see WPanel#setCollapsible(boolean on)
	 */
	public boolean isCollapsible() {
		return this.collapseIcon_ != null;
	}

	/**
	 * Set the panel expanded or collapsed.
	 * 
	 * When <i>on</i> is true, equivalent to {@link WPanel#collapse()},
	 * otherwise to {@link WPanel#expand()}.
	 * <p>
	 * The default value is false.
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
	 * 
	 * @see WPanel#setCollapsed(boolean on)
	 * @see WPanel#collapsed()
	 * @see WPanel#expanded()
	 */
	public boolean isCollapsed() {
		return this.isCollapsible() && this.collapseIcon_.getState() == 1;
	}

	/**
	 * Collapse the panel.
	 * 
	 * When {@link WPanel#isCollapsible()} is true, the panel is collapsed to
	 * minimize screen real-estate.
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
	 * Collapse the panel.
	 * 
	 * When {@link WPanel#isCollapsible()} is true, the panel is expanded to its
	 * original state.
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
	 * Set the central widget.
	 * 
	 * Sets the widget that is the contents of the panel. When a widget was
	 * previously set, the old widget is deleted first.
	 * <p>
	 * The default value is 0 (no widget set).
	 */
	public void setCentralWidget(WWidget w) {
		if (this.centralWidget_ != null)
			this.centralWidget_.remove();
		this.centralWidget_ = w;
		if (w != null) {
			this.getCentralArea().addWidget(w);
		}
	}

	/**
	 * Return the central widget.
	 * 
	 * @see WPanel#setCentralWidget(WWidget w)
	 */
	public WWidget getCentralWidget() {
		return this.centralWidget_;
	}

	/**
	 * Signal emitted when the panel is collapsed.
	 * 
	 * Signal emitted when the panel is collapsed. The signal is only emitted
	 * when the panel is collapsed by the user using the collapse icon in the
	 * tible bar, not when calling {@link WPanel#setCollapsed(boolean on)}.
	 * <p>
	 * 
	 * @see WPanel#expanded()
	 */
	public Signal collapsed() {
		return this.collapsed_;
	}

	/**
	 * Signal emitted when the panel is expanded.
	 * 
	 * Signal emitted when the panel is expanded. The signal is only emitted
	 * when the panel is expanded by the user using the expand icon in the title
	 * bar, not when calling {@link WPanel#setCollapsed(boolean on)}.
	 * <p>
	 * 
	 * @see WPanel#collapsed()
	 */
	public Signal expanded() {
		return this.expanded_;
	}

	public Signal1<Boolean> collapsedSS() {
		return this.collapsedSS_;
	}

	public Signal1<Boolean> expandedSS() {
		return this.expandedSS_;
	}

	public WIconPair getCollapseIcon() {
		return this.collapseIcon_;
	}

	private WContainerWidget titleBar_;
	private WIconPair collapseIcon_;
	private WText title_;
	private WContainerWidget impl_;
	private WWidget centralWidget_;
	private Signal collapsed_;
	private Signal expanded_;
	private Signal1<Boolean> collapsedSS_;
	private Signal1<Boolean> expandedSS_;
	private boolean wasCollapsed_;

	private void doExpand() {
		this.wasCollapsed_ = this.isCollapsed();
		this.getCentralArea().show();
		this.expandedSS_.trigger(true);
	}

	private void doCollapse() {
		this.wasCollapsed_ = this.isCollapsed();
		this.getCentralArea().hide();
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
		return ((this.impl_.getChildren()
				.get(this.impl_.getChildren().size() - 1)) instanceof WContainerWidget ? (WContainerWidget) (this.impl_
				.getChildren().get(this.impl_.getChildren().size() - 1))
				: null);
	}
}
