/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * A menu presented in a popup window
 * <p>
 * 
 * The menu implements a typical context menu, with support for submenu&apos;s.
 * It is not to be confused with {@link WMenu} which implements an
 * always-visible navigation menu for a web application.
 * <p>
 * When initially created, the menu is invisible, until
 * {@link WPopupMenu#popup(WPoint p) popup()} or
 * {@link WPopupMenu#exec(WPoint p) exec()} is called. Then, the menu will
 * remain visible until an item is selected, or the user cancels the menu (by
 * hitting Escape or clicking elsewhere).
 * <p>
 * The implementation assumes availability of JavaScript to position the menu at
 * the current mouse position and provide feed-back of the currently selected
 * item.
 * <p>
 * Similar in use as {@link WDialog}, there are two ways of using the menu. The
 * simplest way is to use one of the {@link WPopupMenu#exec(WPoint p) exec()}
 * methods, to use a reentrant event loop and wait until the user cancelled the
 * popup menu (by hitting Escape or clicking elsewhere), or selected an item.
 * <p>
 * Alternatively, you can use one of the {@link WPopupMenu#popup(WPoint p)
 * popup()} methods to show the menu and listen to the
 * {@link WPopupMenu#aboutToHide() aboutToHide} signal where you read the
 * {@link WPopupMenu#getResult() getResult()}.
 * <p>
 * You have several options to react to the selection of an item:
 * <ul>
 * <li>Either you use the {@link WPopupMenuItem} itself to identify the action,
 * perhaps by specialization or simply by binding custom data using
 * {@link WPopupMenuItem#setData(Object data) setData()}.</li>
 * <li>You can bind a separate method to each item&apos;s
 * {@link WPopupMenuItem#triggered() triggered()} signal.</li>
 * </ul>
 * <p>
 * Usage example:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * // Create a menu with some items
 * WPopupMenu popup = new WPopupMenu();
 * popup.addItem(&quot;icons/item1.gif&quot;, &quot;Item 1&quot;);
 * popup.addItem(&quot;Item 2&quot;).setCheckable(true);
 * popup.addItem(&quot;Item 3&quot;);
 * popup.addSeparator();
 * popup.addItem(&quot;Item 4&quot;);
 * popup.addSeparator();
 * popup.addItem(&quot;Item 5&quot;);
 * popup.addItem(&quot;Item 6&quot;);
 * popup.addSeparator();
 * 
 * WPopupMenu subMenu = new WPopupMenu();
 * subMenu.addItem(&quot;Sub Item 1&quot;);
 * subMenu.addItem(&quot;Sub Item 2&quot;);
 * popup.addMenu(&quot;Item 7&quot;, subMenu);
 * 
 * WPopupMenuItem item = popup.exec(event);
 * 
 * if (item) {
 * 	// ... do associated action.
 * }
 * </pre>
 * 
 * </blockquote>
 * <p>
 * The menu implementation does not provide any style. You can style the menu
 * using CSS.
 * <p>
 * For example: <blockquote>
 * 
 * <pre>
 * div.Wt-popupmenu {
 *      background: white;
 *      color: black;
 *     border: 1px solid #666666;
 *     z-index: 200;
 *     cursor: default;
 *  }
 * 
 *  div.Wt-popupmenu .notselected, div.Wt-popupmenu .selected {
 *      padding: 2px 0px;
 *  }
 * 
 *  div.Wt-popupmenu .selected {
 *      background: blue;
 *      color: white;
 *  }
 * 
 *  div.Wt-popupmenu .separator {
 *      border-top: 1px solid #CCCCCC;
 *     border-bottom: 1px solid #DDDDDD;
 *     margin: 0px 3px;
 *  }
 * </pre>
 * 
 * </blockquote>
 * <p>
 * 
 * @see WPopupMenuItem
 */
public class WPopupMenu extends WCompositeWidget {
	/**
	 * Create a new popup menu.
	 * <p>
	 * The menu is hidden, by default, and must be shown using
	 * {@link WPopupMenu#popup(WPoint p) popup()} or
	 * {@link WPopupMenu#exec(WPoint p) exec()}.
	 */
	public WPopupMenu() {
		super();
		this.parentItem_ = null;
		this.result_ = null;
		this.aboutToHide_ = new Signal(this);
		this.globalClickConnection_ = new AbstractSignal.Connection();
		this.globalEscapeConnection_ = new AbstractSignal.Connection();
		this.recursiveEventLoop_ = false;
		this.setImplementation(this.impl_ = new WContainerWidget());
		this.setPositionScheme(PositionScheme.Absolute);
		this.setStyleClass("Wt-popupmenu");
		String CSS_RULES_NAME = "Wt::WPopupMenu";
		WApplication app = WApplication.getInstance();
		if (!app.getStyleSheet().isDefined(CSS_RULES_NAME)) {
			app.getStyleSheet().addRule(".notselected .Wt-popupmenu",
					"visibility: hidden;", CSS_RULES_NAME);
		}
		app.getDomRoot().addWidget(this);
		this.hide();
	}

	/**
	 * Add an item with given text.
	 * <p>
	 * Adds an item to the menu with given text, and returns the corresponding
	 * item object.
	 * <p>
	 * 
	 * @see WPopupMenu#add(WPopupMenuItem item)
	 */
	public WPopupMenuItem addItem(CharSequence text) {
		return this.addItem("", text);
	}

	/**
	 * Add an item with given icon and text.
	 * <p>
	 * Adds an item to the menu with given text and icon, and returns the
	 * corresponding item object.
	 * <p>
	 * <p>
	 * <i><b>Note:</b>The icon should have a width of 16 pixels.</i>
	 * </p>
	 * 
	 * @see WPopupMenu#add(WPopupMenuItem item)
	 */
	public WPopupMenuItem addItem(String iconPath, CharSequence text) {
		WPopupMenuItem item = new WPopupMenuItem(iconPath, text);
		this.add(item);
		return item;
	}

	// public WPopupMenuItem addItem(CharSequence text, T target,
	// <pointertomember or dependentsizedarray> methodpointertomember or
	// dependentsizedarray>) ;
	// public WPopupMenuItem addItem(String iconPath, CharSequence text, T
	// target, <pointertomember or dependentsizedarray> methodpointertomember or
	// dependentsizedarray>) ;
	/**
	 * Add a submenu, with given text.
	 * <p>
	 * Adds an item with text <i>text</i>, that leads to a submenu <i>menu</i>.
	 * <p>
	 * 
	 * @see WPopupMenu#add(WPopupMenuItem item)
	 */
	public WPopupMenuItem addMenu(CharSequence text, WPopupMenu menu) {
		return this.addMenu("", text, menu);
	}

	/**
	 * Add a submenu, with given icon and text.
	 * <p>
	 * Adds an item with given text and icon, that leads to a submenu
	 * <i>menu</i>.
	 * <p>
	 * 
	 * @see WPopupMenu#add(WPopupMenuItem item)
	 */
	public WPopupMenuItem addMenu(String iconPath, CharSequence text,
			WPopupMenu menu) {
		WPopupMenuItem item = this.addItem(iconPath, text);
		item.setPopupMenu(menu);
		return item;
	}

	/**
	 * Add a menu item.
	 * <p>
	 * Adds an item to the popup menu.
	 */
	public void add(WPopupMenuItem item) {
		this.impl_.addWidget(item);
	}

	/**
	 * Add a separator to the menu.
	 * <p>
	 * Adds a separator the popup menu. The separator is an empty div with
	 * style-class &quot;separator&quot;.
	 */
	public void addSeparator() {
		this.add(new WPopupMenuItem(true));
	}

	/**
	 * Show the the popup at a position.
	 * <p>
	 * Displays the popup at a point with document coordinates <i>point</i>. The
	 * positions intelligent, and will chose one of the four menu corners to
	 * correspond to this point so that the popup menu is completely visible
	 * within the window.
	 * <p>
	 * 
	 * @see WPopupMenu#exec(WPoint p)
	 */
	public void popup(WPoint p) {
		this.result_ = null;
		WApplication app = WApplication.getInstance();
		if (app.getRoot().escapePressed().isConnected()) {
			app.getRoot().escapePressed().trigger();
		}
		this.globalClickConnection_ = app.getRoot().clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WPopupMenu.this.done();
					}
				});
		this.globalEscapeConnection_ = app.getRoot().escapePressed()
				.addListener(this, new Signal.Listener() {
					public void trigger() {
						WPopupMenu.this.done();
					}
				});
		this.prepareRender(app);
		this.show();
		WApplication.getInstance().doJavaScript(
				"Wt2_99_5.positionXY('" + this.getId() + "',"
						+ String.valueOf(p.getX()) + ","
						+ String.valueOf(p.getY()) + ");");
	}

	/**
	 * Show the the popup at the location of a mouse event.
	 * <p>
	 * This is a convenience method for {@link WPopupMenu#popup(WPoint p)
	 * popup()} that uses the event&apos;s document coordinates.
	 * <p>
	 * 
	 * @see WPopupMenu#popup(WPoint p)
	 * @see WMouseEvent#getDocument()
	 */
	public void popup(WMouseEvent e) {
		this.popup(new WPoint(e.getDocument().x, e.getDocument().y));
	}

	/**
	 * Execute the the popup at a position.
	 * <p>
	 * Displays the popup at a point with document coordinates <i>p</i>, using
	 * {@link WPopupMenu#popup(WPoint p) popup()}, and the waits until a menu
	 * item is selected, or the menu is cancelled.
	 * <p>
	 * Returns the selected menu (or sub-menu) item, or 0 if the user cancelled
	 * the menu.
	 * <p>
	 * 
	 * @see WPopupMenu#popup(WPoint p)
	 */
	public WPopupMenuItem exec(WPoint p) {
		if (this.recursiveEventLoop_) {
			throw new WtException(
					"WPopupMenu::exec(): already in recursive event loop.");
		}
		WebSession session = WApplication.getInstance().getSession();
		this.recursiveEventLoop_ = true;
		this.popup(p);
		do {
			session.doRecursiveEventLoop();
		} while (this.recursiveEventLoop_);
		return this.result_;
	}

	/**
	 * Execute the the popup at the location of a mouse event.
	 * <p>
	 * This is a convenience method for {@link WPopupMenu#exec(WPoint p) exec()}
	 * that uses the event&apos;s document coordinates.
	 * <p>
	 * 
	 * @see WPopupMenu#exec(WPoint p)
	 */
	public WPopupMenuItem exec(WMouseEvent e) {
		return this.exec(new WPoint(e.getDocument().x, e.getDocument().y));
	}

	/**
	 * Returns the last triggered menu item.
	 * <p>
	 * The result is 0 when the user cancelled the popup menu.
	 */
	public WPopupMenuItem getResult() {
		return this.result_;
	}

	public void setHidden(boolean hidden) {
		super.setHidden(hidden);
		for (int i = 0; i < this.impl_.getCount(); ++i) {
			WPopupMenuItem item = ((this.impl_.getWidget(i)) instanceof WPopupMenuItem ? (WPopupMenuItem) (this.impl_
					.getWidget(i))
					: null);
			item.renderOut();
		}
	}

	/**
	 * Signal emitted when the popup is hidden.
	 * <p>
	 * This signal is emitted when the popup is hidden, either because an item
	 * was selected, or when the menu was cancelled.
	 * <p>
	 * You can use {@link WPopupMenu#getResult() getResult()} to get the
	 * selected item.
	 */
	public Signal aboutToHide() {
		return this.aboutToHide_;
	}

	private WContainerWidget impl_;
	WPopupMenuItem parentItem_;
	private WPopupMenuItem result_;
	private Signal aboutToHide_;
	private AbstractSignal.Connection globalClickConnection_;
	private AbstractSignal.Connection globalEscapeConnection_;
	private boolean recursiveEventLoop_;

	WPopupMenu getTopLevelMenu() {
		return this.parentItem_ != null ? this.parentItem_.getTopLevelMenu()
				: this;
	}

	private void done() {
		this.done((WPopupMenuItem) null);
	}

	void done(WPopupMenuItem result) {
		this.result_ = result;
		this.hide();
		this.globalClickConnection_.disconnect();
		this.globalEscapeConnection_.disconnect();
		WApplication.getInstance().getRoot().clicked().senderRepaint();
		WApplication.getInstance().getRoot().escapePressed().senderRepaint();
		this.recursiveEventLoop_ = false;
		this.aboutToHide_.trigger();
	}

	void popup(WWidget location) {
		this.show();
		WApplication.getInstance().doJavaScript(
				"Wt2_99_5.positionAtWidget('" + this.getId() + "','"
						+ location.getId() + "');");
	}

	private void prepareRender(WApplication app) {
		if (app.getEnvironment().agentIsIE()) {
			app.doJavaScript(this.getJsRef() + ".firstChild.style.width="
					+ this.getJsRef() + ".firstChild.offsetWidth+'px';");
		}
	}
}
