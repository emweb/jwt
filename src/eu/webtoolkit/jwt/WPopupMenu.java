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
 * A menu presented in a popup window.
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
 * 
 * <pre>
 * {
 * 	&#064;code
 * 	// Create a menu with some items
 * 	WPopupMenu popup = new WPopupMenu();
 * 	popup.addItem(&quot;icons/item1.gif&quot;, &quot;Item 1&quot;);
 * 	popup.addItem(&quot;Item 2&quot;).setCheckable(true);
 * 	popup.addItem(&quot;Item 3&quot;);
 * 	popup.addSeparator();
 * 	popup.addItem(&quot;Item 4&quot;);
 * 	popup.addSeparator();
 * 	popup.addItem(&quot;Item 5&quot;);
 * 	popup.addItem(&quot;Item 6&quot;);
 * 	popup.addSeparator();
 * 
 * 	WPopupMenu subMenu = new WPopupMenu();
 * 	subMenu.addItem(&quot;Sub Item 1&quot;);
 * 	subMenu.addItem(&quot;Sub Item 2&quot;);
 * 	popup.addMenu(&quot;Item 7&quot;, subMenu);
 * 
 * 	WPopupMenuItem item = popup.exec(event);
 * 
 * 	if (item) {
 * 		// ... do associated action.
 * 	}
 * }
 * </pre>
 * <p>
 * <h3>CSS</h3>
 * <p>
 * A {@link WPopupMenu} has the <code>Wt-popupmenu</code> style class. The look
 * can be overridden using the following style class selectors:
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
 * A snapshot of the {@link WPopupMenu}: <div align="center"> <img
 * src="doc-files//WPopupMenu-default-1.png" alt="WPopupMenu example (default)">
 * <p>
 * <strong>WPopupMenu example (default)</strong>
 * </p>
 * </div> <div align="center"> <img src="doc-files//WPopupMenu-polished-1.png"
 * alt="WPopupMenu example (polished)">
 * <p>
 * <strong>WPopupMenu example (polished)</strong>
 * </p>
 * </div>
 * 
 * @see WPopupMenuItem
 */
public class WPopupMenu extends WCompositeWidget {
	private static Logger logger = LoggerFactory.getLogger(WPopupMenu.class);

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
		this.triggered_ = new Signal1<WPopupMenuItem>(this);
		this.cancel_ = new JSignal(this, "cancel");
		this.globalClickConnection_ = new AbstractSignal.Connection();
		this.globalEscapeConnection_ = new AbstractSignal.Connection();
		this.recursiveEventLoop_ = false;
		this.autoHideDelay_ = -1;
		String TEMPLATE = "${shadow-x1-x2}${contents}";
		this
				.setImplementation(this.impl_ = new WTemplate(new WString(
						TEMPLATE)));
		this.impl_.setLoadLaterWhenInvisible(false);
		this.setPositionScheme(PositionScheme.Absolute);
		this.setStyleClass("Wt-popupmenu Wt-outset");
		this.impl_.bindString("shadow-x1-x2", WTemplate.DropShadow_x1_x2);
		WContainerWidget content = new WContainerWidget();
		content.setStyleClass("content");
		this.impl_.bindWidget("contents", content);
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
		this.setOffsets(new WLength(42), EnumSet.of(Side.Left, Side.Top));
		this.setOffsets(new WLength(-10000), EnumSet.of(Side.Left, Side.Top));
		WApplication.getInstance().doJavaScript(
				"Wt3_2_1.positionXY('" + this.getId() + "',"
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
			throw new WException("WPopupMenu::exec(): already being executed.");
		}
		WApplication app = WApplication.getInstance();
		this.recursiveEventLoop_ = true;
		this.popup(p);
		if (app.getEnvironment().isTest()) {
			app.getEnvironment().popupExecuted().trigger(this);
			if (this.recursiveEventLoop_) {
				throw new WException("Test case must close popup menu.");
			}
		} else {
			do {
				app.getSession().doRecursiveEventLoop();
			} while (this.recursiveEventLoop_);
		}
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
			throw new WException("WPopupMenu::exec(): already being executed.");
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

	public void setHidden(boolean hidden, WAnimation animation) {
		super.setHidden(hidden, animation);
		if (hidden) {
			this.renderOutAll();
		}
	}

	public void setMaximumSize(WLength width, WLength height) {
		super.setMaximumSize(width, height);
		this.getContents().setMaximumSize(width, height);
	}

	public void setMinimumSize(WLength width, WLength height) {
		super.setMinimumSize(width, height);
		this.getContents().setMinimumSize(width, height);
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

	/**
	 * Signal emitted when an item is activated.
	 * <p>
	 * Passes the activated item as argument. This signal is only emitted for
	 * the toplevel menu.
	 * <p>
	 * 
	 * @see WPopupMenuItem#triggered()
	 */
	public Signal1<WPopupMenuItem> triggered() {
		return this.triggered_;
	}

	/**
	 * Configure auto-hide when the mouse leaves the menu.
	 * <p>
	 * If <code>enabled</code>, The popup menu will be hidden when the mouse
	 * leaves the menu for longer than <code>autoHideDelay</code>
	 * (milliseconds). The popup menu result will be 0, as if the user
	 * cancelled.
	 * <p>
	 * By default, this option is disabled.
	 */
	public void setAutoHide(boolean enabled, int autoHideDelay) {
		if (enabled) {
			this.autoHideDelay_ = autoHideDelay;
		} else {
			this.autoHideDelay_ = -1;
		}
	}

	/**
	 * Configure auto-hide when the mouse leaves the menu.
	 * <p>
	 * Calls {@link #setAutoHide(boolean enabled, int autoHideDelay)
	 * setAutoHide(enabled, 0)}
	 */
	public final void setAutoHide(boolean enabled) {
		setAutoHide(enabled, 0);
	}

	protected boolean isExposed(WWidget w) {
		if (super.isExposed(w)) {
			return true;
		}
		if (w == WApplication.getInstance().getRoot()) {
			return true;
		}
		WContainerWidget c = this.getContents();
		for (int i = 0; i < c.getCount(); ++i) {
			WPopupMenuItem item = ((c.getWidget(i)) instanceof WPopupMenuItem ? (WPopupMenuItem) (c
					.getWidget(i))
					: null);
			if (item.getPopupMenu() != null) {
				if (item.getPopupMenu().isExposed(w)) {
					return true;
				}
			}
		}
		if (!(this.parentItem_ != null)) {
			this.done();
			return true;
		} else {
			return false;
		}
	}

	private WTemplate impl_;
	WPopupMenuItem parentItem_;
	WPopupMenuItem result_;
	private Signal aboutToHide_;
	private Signal1<WPopupMenuItem> triggered_;
	private JSignal cancel_;
	private AbstractSignal.Connection globalClickConnection_;
	private AbstractSignal.Connection globalEscapeConnection_;
	private boolean recursiveEventLoop_;
	private int autoHideDelay_;

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
		WApplication app = WApplication.getInstance();
		app.getRoot().clicked().disconnect(this.globalClickConnection_);
		app.globalEscapePressed().disconnect(this.globalEscapeConnection_);
		app.popExposedConstraint(this);
		this.recursiveEventLoop_ = false;
		this.triggered_.trigger(this.result_);
		this.aboutToHide_.trigger();
	}

	private void popupImpl() {
		this.renderOutAll();
		this.result_ = null;
		WApplication app = WApplication.getInstance();
		if (app.globalEscapePressed().isConnected()) {
			app.globalEscapePressed().trigger();
		}
		this.globalClickConnection_ = app.getRoot().clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WPopupMenu.this.done();
					}
				});
		this.globalEscapeConnection_ = app.globalEscapePressed().addListener(
				this, new Signal.Listener() {
					public void trigger() {
						WPopupMenu.this.done();
					}
				});
		app.pushExposedConstraint(this);
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
		if (this.autoHideDelay_ >= 0) {
			if (!this.cancel_.isConnected()) {
				app.loadJavaScript("js/WPopupMenu.js", wtjs1());
				this.doJavaScript("new Wt3_2_1.WPopupMenu("
						+ app.getJavaScriptClass() + "," + this.getJsRef()
						+ "," + String.valueOf(this.autoHideDelay_) + ");");
				this.cancel_.addListener(this, new Signal.Listener() {
					public void trigger() {
						WPopupMenu.this.done();
					}
				});
			}
		}
	}

	void renderOutAll() {
		WContainerWidget c = this.getContents();
		for (int i = 0; i < c.getCount(); ++i) {
			WPopupMenuItem item = ((c.getWidget(i)) instanceof WPopupMenuItem ? (WPopupMenuItem) (c
					.getWidget(i))
					: null);
			item.renderOut();
		}
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WPopupMenu",
				"function(e,b,c){function f(){e.emit(b,\"cancel\")}jQuery.data(b,\"obj\",this);var a=null,d=false;c>=0&&$(document).find(\".Wt-popupmenu\").mouseleave(function(){if(d){clearTimeout(a);a=setTimeout(f,c)}}).mouseenter(function(){d=true;clearTimeout(a)})}");
	}
}
