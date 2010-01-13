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
 * {@link WPopupMenuItem#setData(Object data) WPopupMenuItem#setData()}.</li>
 * <li>You can bind a separate method to each item&apos;s
 * {@link WPopupMenuItem#triggered() WPopupMenuItem#triggered()} signal.</li>
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
 * <h3>CSS</h3>
 * <p>
 * You may customize the look of a treeview using the <code>Wt-popupmenu</code>
 * CSS class.
 * <p>
 * <div class="fragment">
 * 
 * <pre class="fragment">
 * .Wt-popupmenu .Wt-item, .Wt-popupmenu .Wt-selected : item
 * .Wt-popupmenu .Wt-selected                         : selected item
 * .Wt-popupmenu .Wt-separator                        : separator
 * </pre>
 * 
 * </div>
 * <p>
 * 
 * @see WPopupMenuItem
 */
public class WPopupMenu extends WCompositeWidget {
	/**
	 * Creates a new popup menu.
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
		String TEMPLATE = "<span class=\"Wt-x1\"><span class=\"Wt-x1a\" /></span><span class=\"Wt-x2\"><span class=\"Wt-x2a\" /></span>${contents}";
		this
				.setImplementation(this.impl_ = new WTemplate(new WString(
						TEMPLATE)));
		this.setPositionScheme(PositionScheme.Absolute);
		this.setStyleClass("Wt-popupmenu Wt-outset");
		this.impl_.bindWidget("contents", new WContainerWidget());
		String CSS_RULES_NAME = "Wt::WPopupMenu";
		WApplication app = WApplication.getInstance();
		if (!app.getStyleSheet().isDefined(CSS_RULES_NAME)) {
			app.getStyleSheet().addRule(".Wt-notselected .Wt-popupmenu",
					"visibility: hidden;", CSS_RULES_NAME);
		}
		app.getDomRoot().addWidget(this);
		this.hide();
	}

	/**
	 * Adds an item with given text.
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
	 * Adds an item with given icon and text.
	 * <p>
	 * Adds an item to the menu with given text and icon, and returns the
	 * corresponding item object.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>The icon should have a width of 16 pixels.</i>
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
	 * Adds a submenu, with given text.
	 * <p>
	 * Adds an item with text <code>text</code>, that leads to a submenu
	 * <code>menu</code>.
	 * <p>
	 * 
	 * @see WPopupMenu#add(WPopupMenuItem item)
	 */
	public WPopupMenuItem addMenu(CharSequence text, WPopupMenu menu) {
		return this.addMenu("", text, menu);
	}

	/**
	 * Adds a submenu, with given icon and text.
	 * <p>
	 * Adds an item with given text and icon, that leads to a submenu
	 * <code>menu</code>.
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
	 * Adds a menu item.
	 * <p>
	 * Adds an item to the popup menu.
	 */
	public void add(WPopupMenuItem item) {
		this.getContents().addWidget(item);
	}

	/**
	 * Adds a separator to the menu.
	 * <p>
	 * Adds a separator the popup menu. The separator is an empty div with
	 * style-class &quot;separator&quot;.
	 */
	public void addSeparator() {
		this.add(new WPopupMenuItem(true));
	}

	/**
	 * Shows the the popup at a position.
	 * <p>
	 * Displays the popup at a point with document coordinates
	 * <code>point</code>. The positions intelligent, and will chose one of the
	 * four menu corners to correspond to this point so that the popup menu is
	 * completely visible within the window.
	 * <p>
	 * 
	 * @see WPopupMenu#exec(WPoint p)
	 */
	public void popup(WPoint p) {
		this.popupImpl();
		WApplication.getInstance().doJavaScript(
				"Wt3_1_0.positionXY('" + this.getId() + "',"
						+ String.valueOf(p.getX()) + ","
						+ String.valueOf(p.getY()) + ");");
	}

	/**
	 * Shows the the popup at the location of a mouse event.
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
	 * Shows the popup besides a widget.
	 * <p>
	 * 
	 * @see WWidget#positionAt(WWidget widget, Orientation orientation)
	 */
	public void popup(WWidget location, Orientation orientation) {
		this.popupImpl();
		this.positionAt(location, orientation);
	}

	/**
	 * Shows the popup besides a widget.
	 * <p>
	 * Calls {@link #popup(WWidget location, Orientation orientation)
	 * popup(location, Orientation.Vertical)}
	 */
	public final void popup(WWidget location) {
		popup(location, Orientation.Vertical);
	}

	/**
	 * Executes the the popup at a position.
	 * <p>
	 * Displays the popup at a point with document coordinates <code>p</code>,
	 * using {@link WPopupMenu#popup(WPoint p) popup()}, and the waits until a
	 * menu item is selected, or the menu is cancelled.
	 * <p>
	 * Returns the selected menu (or sub-menu) item, or <code>null</code> if the
	 * user cancelled the menu.
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
	 * Executes the the popup at the location of a mouse event.
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
	 * Executes the popup besides a widget.
	 * <p>
	 * 
	 * @see WWidget#positionAt(WWidget widget, Orientation orientation)
	 */
	public WPopupMenuItem exec(WWidget location, Orientation orientation) {
		if (this.recursiveEventLoop_) {
			throw new WtException(
					"WPopupMenu::exec(): already in recursive event loop.");
		}
		WebSession session = WApplication.getInstance().getSession();
		this.recursiveEventLoop_ = true;
		this.popup(location, orientation);
		do {
			session.doRecursiveEventLoop();
		} while (this.recursiveEventLoop_);
		return this.result_;
	}

	/**
	 * Executes the popup besides a widget.
	 * <p>
	 * Returns {@link #exec(WWidget location, Orientation orientation)
	 * exec(location, Orientation.Vertical)}
	 */
	public final WPopupMenuItem exec(WWidget location) {
		return exec(location, Orientation.Vertical);
	}

	/**
	 * Returns the last triggered menu item.
	 * <p>
	 * The result is <code>null</code> when the user cancelled the popup menu.
	 */
	public WPopupMenuItem getResult() {
		return this.result_;
	}

	public void setHidden(boolean hidden) {
		super.setHidden(hidden);
		WContainerWidget c = this.getContents();
		for (int i = 0; i < c.getCount(); ++i) {
			WPopupMenuItem item = ((c.getWidget(i)) instanceof WPopupMenuItem ? (WPopupMenuItem) (c
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

	private WTemplate impl_;
	WPopupMenuItem parentItem_;
	private WPopupMenuItem result_;
	private Signal aboutToHide_;
	private AbstractSignal.Connection globalClickConnection_;
	private AbstractSignal.Connection globalEscapeConnection_;
	private boolean recursiveEventLoop_;

	private WContainerWidget getContents() {
		return ((this.impl_.resolveWidget("contents")) instanceof WContainerWidget ? (WContainerWidget) (this.impl_
				.resolveWidget("contents"))
				: null);
	}

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

	private void popupImpl() {
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
	}

	void popupToo(WWidget location) {
		this.show();
		this.positionAt(location, Orientation.Horizontal);
	}

	private void prepareRender(WApplication app) {
		if (app.getEnvironment().agentIsIE()) {
			app.doJavaScript(this.getJsRef() + ".lastChild.style.width="
					+ this.getJsRef() + ".lastChild.offsetWidth+'px';");
		}
	}
}
