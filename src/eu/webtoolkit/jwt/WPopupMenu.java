package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

/**
 * A menu presented in a popup window
 * 
 * 
 * The menu implements a typical context menu, with support for submenu&apos;s.
 * It is not to be confused with {@link WMenu} which implements an
 * always-visible navigation menu for a web application.
 * <p>
 * When initially created, the menu is invisible, until
 * {@link WPopupMenu#popup(WPoint p)} or {@link WPopupMenu#exec(WPoint p)} is
 * called. Then, the menu will remain visible until an item is selected, or the
 * user cancels the menu (by hitting Escape or clicking elsewhere).
 * <p>
 * The implementation assumes availability of JavaScript to position the menu at
 * the current mouse position and provide feed-back of the currently selected
 * item.
 * <p>
 * Similar in use as {@link WDialog}, there are two ways of using the menu. The
 * simplest way is to use one of the {@link WPopupMenu#exec(WPoint p)} methods,
 * to use a reentrant event loop and wait until the user cancelled the popup
 * menu (by hitting Escape or clicking elsewhere), or selected an item.
 * <p>
 * Alternatively, you can use one of the {@link WPopupMenu#popup(WPoint p)}
 * methods to show the menu and listen to the {@link WPopupMenu#aboutToHide()
 * aboutToHide} signal where you read the {@link WPopupMenu#getResult()}.
 * <p>
 * You have several options to react to the selection of an item:
 * <ul>
 * <li>Either you use the {@link WPopupMenuItem} itself to identify the action,
 * perhaps by specialization or simply by binding custom data using
 * {@link WPopupMenuItem#setData(Object data)}.</li>
 * <li>You can bind a separate method to each item&apos;s
 * {@link WPopupMenuItem#triggered()} signal.</li>
 * </ul>
 * <p>
 * Usage example:
 * <p>
 * <code>
 // Create a menu with some items <br> 
 WPopupMenu popup = new WPopupMenu(); <br> 
 popup.addItem(&quot;icons/item1.gif&quot;, &quot;Item 1&quot;); <br> 
 popup.addItem(&quot;Item 2&quot;).setCheckable(true); <br> 
 popup.addItem(&quot;Item 3&quot;); <br> 
 popup.addSeparator(); <br> 
 popup.addItem(&quot;Item 4&quot;); <br> 
 popup.addSeparator(); <br> 
 popup.addItem(&quot;Item 5&quot;); <br> 
 popup.addItem(&quot;Item 6&quot;); <br> 
 popup.addSeparator(); <br> 
		  <br> 
 WPopupMenu subMenu = new WPopupMenu(); <br> 
 subMenu.addItem(&quot;Sub Item 1&quot;); <br> 
 subMenu.addItem(&quot;Sub Item 2&quot;); <br> 
 popup.addMenu(&quot;Item 7&quot;, subMenu); <br> 
		  <br> 
 WPopupMenuItem item = popup.exec(event); <br> 
		  <br> 
 if (item) { <br> 
  // ... do associated action. <br> 
 } <br> 
 /endcode <br> 
 /endif <br> 
 <br> 
 The menu implementation does not provide any style. You can style the <br> 
 menu using CSS. <br> 
 <br> 
 For example: <br> 
 \code <br> 
 div.Wt-popupmenu { <br> 
     background: white; <br> 
     color: black; <br> 
    border: 1px solid #666666; <br> 
    z-index: 200; <br> 
    cursor: default; <br> 
 } <br> 
 <br> 
 div.Wt-popupmenu .notselected, div.Wt-popupmenu .selected { <br> 
     padding: 2px 0px; <br> 
 } <br> 
 <br> 
 div.Wt-popupmenu .selected { <br> 
     background: blue; <br> 
     color: white; <br> 
 } <br> 
 <br> 
 div.Wt-popupmenu .separator { <br> 
     border-top: 1px solid #CCCCCC; <br> 
    border-bottom: 1px solid #DDDDDD; <br> 
    margin: 0px 3px; <br> 
 }
</code>
 * <p>
 * 
 * @see WPopupMenuItem
 */
public class WPopupMenu extends WCompositeWidget {
	/**
	 * Create a new popup menu.
	 * 
	 * The menu is hidden, by default, and must be shown using
	 * {@link WPopupMenu#popup(WPoint p)} or {@link WPopupMenu#exec(WPoint p)}.
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
	 * 
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
	 * 
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
	 * 
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
	 * 
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
	 * 
	 * Adds an item to the popup menu.
	 */
	public void add(WPopupMenuItem item) {
		this.impl_.addWidget(item);
	}

	/**
	 * Add a separator to the menu.
	 * 
	 * Adds a separator the popup menu. The separator is an empty div with
	 * style-class &quot;separator&quot;.
	 */
	public void addSeparator() {
		this.add(new WPopupMenuItem(true));
	}

	/**
	 * Show the the popup at a position.
	 * 
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
				"Wt2_99_2.positionXY('" + this.getFormName() + "',"
						+ String.valueOf(p.getX()) + ","
						+ String.valueOf(p.getY()) + ");");
	}

	/**
	 * Show the the popup at the location of a mouse event.
	 * 
	 * This is a convenience method for {@link WPopupMenu#popup(WPoint p)} that
	 * uses the event&apos;s document coordinates.
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
	 * 
	 * Displays the popup at a point with document coordinates <i>p</i>, using
	 * {@link WPopupMenu#popup(WPoint p)}, and the waits until a menu item is
	 * selected, or the menu is cancelled.
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
		session.doRecursiveEventLoop("");
		return this.result_;
	}

	/**
	 * Execute the the popup at the location of a mouse event.
	 * 
	 * This is a convenience method for {@link WPopupMenu#exec(WPoint p)} that
	 * uses the event&apos;s document coordinates.
	 * <p>
	 * 
	 * @see WPopupMenu#exec(WPoint p)
	 */
	public WPopupMenuItem exec(WMouseEvent e) {
		return this.exec(new WPoint(e.getDocument().x, e.getDocument().y));
	}

	/**
	 * Returns the last triggered menu item.
	 * 
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
	 * 
	 * This signal is emitted when the popup is hidden, either because an item
	 * was selected, or when the menu was cancelled.
	 * <p>
	 * You can use {@link WPopupMenu#getResult()} to get the selected item.
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
		boolean recursive = this.recursiveEventLoop_;
		this.recursiveEventLoop_ = false;
		this.aboutToHide_.trigger();
		if (recursive) {
			WebSession session = WApplication.getInstance().getSession();
			session.unlockRecursiveEventLoop();
		}
	}

	void popup(WWidget location) {
		this.show();
		WApplication.getInstance().doJavaScript(
				"Wt2_99_2.positionAtWidget('" + this.getFormName() + "','"
						+ location.getFormName() + "');");
	}

	private void prepareRender(WApplication app) {
		if (app.getEnvironment().agentIsIE()) {
			app.doJavaScript(this.getJsRef() + ".firstChild.style.width="
					+ this.getJsRef() + ".firstChild.offsetWidth+'px';");
		}
	}
}
