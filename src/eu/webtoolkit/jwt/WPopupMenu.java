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
 * It is a specialized {@link WMenu} from which it inherits most of the API.
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
 * As with {@link WDialog}, there are two ways of using the menu. The simplest
 * way is to use one of the synchronous {@link WPopupMenu#exec(WPoint p) exec()}
 * methods, which starts a reentrant event loop and waits until the user
 * cancelled the popup menu (by hitting Escape or clicking elsewhere), or
 * selected an item.
 * <p>
 * Alternatively, you can use one of the {@link WPopupMenu#popup(WPoint p)
 * popup()} methods to show the menu and listen to the
 * {@link WPopupMenu#triggered() triggered} signal where you read the
 * {@link WPopupMenu#getResult() getResult()}.
 * <p>
 * You have several options to react to the selection of an item:
 * <ul>
 * <li>Either you use the {@link WMenuItem} itself to identify the action,
 * perhaps by specialization or simply by binding custom data using
 * {@link WMenuItem#setData(Object data) WMenuItem#setData()}.</li>
 * <li>You can bind a separate method to each item&apos;s
 * {@link WMenuItem#triggered() WMenuItem#triggered()} signal.</li>
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
 * 	WMenuItem item = popup.exec(event);
 * 
 * 	if (item != null) {
 * 		// ... do associated action.
 * 	}
 * }
 * </pre>
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
 * @see WMenuItem
 */
public class WPopupMenu extends WMenu {
	private static Logger logger = LoggerFactory.getLogger(WPopupMenu.class);

	/**
	 * Creates a new popup menu.
	 * <p>
	 * The menu is hidden, by default, and must be shown using
	 * {@link WPopupMenu#popup(WPoint p) popup()} or
	 * {@link WPopupMenu#exec(WPoint p) exec()}.
	 */
	public WPopupMenu(WStackedWidget contentsStack) {
		super(contentsStack);
		this.topLevel_ = null;
		this.result_ = null;
		this.location_ = null;
		this.aboutToHide_ = new Signal(this);
		this.triggered_ = new Signal1<WMenuItem>(this);
		this.cancel_ = new JSignal(this, "cancel");
		this.recursiveEventLoop_ = false;
		this.autoHideDelay_ = -1;
		String CSS_RULES_NAME = "Wt::WPopupMenu";
		WApplication app = WApplication.getInstance();
		if (!app.getStyleSheet().isDefined(CSS_RULES_NAME)) {
			app.getStyleSheet().addRule(".Wt-notselected .Wt-popupmenu",
					"visibility: hidden;", CSS_RULES_NAME);
		}
		app.addGlobalWidget(this);
		this.hide();
	}

	/**
	 * Creates a new popup menu.
	 * <p>
	 * Calls {@link #WPopupMenu(WStackedWidget contentsStack)
	 * this((WStackedWidget)null)}
	 */
	public WPopupMenu() {
		this((WStackedWidget) null);
	}

	public void remove() {
		if (this.button_ != null) {
			WPushButton b = ((this.button_) instanceof WPushButton ? (WPushButton) (this.button_)
					: null);
			if (b != null) {
				b.setMenu((WPopupMenu) null);
			}
		}
		super.remove();
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
		this.doJavaScript("Wt3_3_0.positionXY('" + this.getId() + "',"
				+ String.valueOf(p.getX()) + "," + String.valueOf(p.getY())
				+ ");");
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

	public void setButton(WInteractWidget button) {
		this.button_ = button;
		if (this.button_ != null) {
			this.button_.clicked().addListener(this,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent e1) {
							WPopupMenu.this.popupAtButton();
						}
					});
		}
	}

	/**
	 * Shows the popup besides a widget.
	 * <p>
	 * 
	 * @see WWidget#positionAt(WWidget widget, Orientation orientation)
	 */
	public void popup(WWidget location, Orientation orientation) {
		this.location_ = location;
		this.popupImpl();
		this.doJavaScript("jQuery.data(" + this.getJsRef()
				+ ", 'obj').popupAt(" + location.getJsRef() + ");");
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
	public WMenuItem exec(WPoint p) {
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
	public WMenuItem exec(WMouseEvent e) {
		return this.exec(new WPoint(e.getDocument().x, e.getDocument().y));
	}

	/**
	 * Executes the popup besides a widget.
	 * <p>
	 * 
	 * @see WWidget#positionAt(WWidget widget, Orientation orientation)
	 */
	public WMenuItem exec(WWidget location, Orientation orientation) {
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
	public final WMenuItem exec(WWidget location) {
		return exec(location, Orientation.Vertical);
	}

	/**
	 * Returns the last triggered menu item.
	 * <p>
	 * The result is <code>null</code> when the user cancelled the popup menu.
	 */
	public WMenuItem getResult() {
		return this.result_;
	}

	public void setHidden(boolean hidden, WAnimation animation) {
		super.setHidden(hidden, animation);
		if (this.cancel_.isConnected()
				|| WApplication.getInstance().getSession().getRenderer()
						.isPreLearning()) {
			this.doJavaScript("jQuery.data(" + this.getJsRef()
					+ ", 'obj').setHidden(" + (hidden ? "1" : "0") + ");");
		}
	}

	public void setMaximumSize(WLength width, WLength height) {
		super.setMaximumSize(width, height);
		this.getUl().setMaximumSize(width, height);
	}

	public void setMinimumSize(WLength width, WLength height) {
		super.setMinimumSize(width, height);
		this.getUl().setMinimumSize(width, height);
	}

	/**
	 * Signal emitted when the popup is hidden.
	 * <p>
	 * Unlike the {@link WMenu#itemSelected() WMenu#itemSelected()} signal,
	 * {@link WPopupMenu#aboutToHide() aboutToHide()} is only emitted by the
	 * toplevel popup menu (and not by submenus), and is also emitted when no
	 * item was selected.
	 * <p>
	 * You can use {@link WPopupMenu#getResult() getResult()} to get the
	 * selected item, which may be <code>null</code>.
	 * <p>
	 * 
	 * @see WPopupMenu#triggered()
	 * @see WMenu#itemSelected()
	 */
	public Signal aboutToHide() {
		return this.aboutToHide_;
	}

	/**
	 * Signal emitted when an item is selected.
	 * <p>
	 * Unlike the {@link WMenu#itemSelected() WMenu#itemSelected()} signal,
	 * {@link WPopupMenu#triggered() triggered()} is only emitted by the
	 * toplevel popup menu (and not by submenus).
	 * <p>
	 * 
	 * @see WPopupMenu#aboutToHide()
	 * @see WMenu#itemSelected()
	 */
	public Signal1<WMenuItem> triggered() {
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

	protected void renderSelected(WMenuItem item, boolean selected) {
	}

	protected void setCurrent(int index) {
	}

	private WPopupMenu topLevel_;
	WMenuItem result_;
	private WWidget location_;
	private WInteractWidget button_;
	private Signal aboutToHide_;
	private Signal1<WMenuItem> triggered_;
	private JSignal cancel_;
	private boolean recursiveEventLoop_;
	private int autoHideDelay_;

	private void cancel() {
		if (!this.isHidden()) {
			this.done((WMenuItem) null);
		}
	}

	private void done(WMenuItem result) {
		if (this.isHidden()) {
			return;
		}
		if (this.location_ != null && this.location_ == this.button_) {
			this.button_.removeStyleClass("active", true);
		}
		this.location_ = null;
		this.result_ = result;
		this.hide();
		this.recursiveEventLoop_ = false;
		if (this.result_ != null) {
			this.triggered_.trigger(this.result_);
		}
		this.aboutToHide_.trigger();
	}

	private void popupImpl() {
		this.result_ = null;
		WApplication app = WApplication.getInstance();
		this.prepareRender(app);
		this.show();
	}

	private void prepareRender(WApplication app) {
		if (!this.cancel_.isConnected()) {
			app.loadJavaScript("js/WPopupMenu.js", wtjs1());
			StringBuilder s = new StringBuilder();
			s.append("new Wt3_3_0.WPopupMenu(")
					.append(app.getJavaScriptClass()).append(',').append(
							this.getJsRef()).append(',').append(
							this.autoHideDelay_).append(");");
			this.setJavaScriptMember(" WPopupMenu", s.toString());
			this.cancel_.addListener(this, new Signal.Listener() {
				public void trigger() {
					WPopupMenu.this.cancel();
				}
			});
			this.connectSignals(this);
		}
		this.adjustPadding();
	}

	private void adjustPadding() {
		boolean needPadding = false;
		for (int i = 0; i < this.getCount(); ++i) {
			WMenuItem item = this.itemAt(i);
			if (item.getIcon().length() != 0 || item.isCheckable()) {
				needPadding = true;
				break;
			}
		}
		for (int i = 0; i < this.getCount(); ++i) {
			WMenuItem item = this.itemAt(i);
			item.setItemPadding(needPadding);
			WPopupMenu subMenu = ((item.getMenu()) instanceof WPopupMenu ? (WPopupMenu) (item
					.getMenu())
					: null);
			if (subMenu != null) {
				subMenu.adjustPadding();
			}
		}
	}

	private void popupAtButton() {
		if (!(this.topLevel_ != null) || this.topLevel_ == this) {
			this.button_.addStyleClass("active", true);
			this.popup(this.button_);
		}
	}

	private void connectSignals(final WPopupMenu topLevel) {
		this.topLevel_ = topLevel;
		this.itemSelected().addListener(topLevel,
				new Signal1.Listener<WMenuItem>() {
					public void trigger(WMenuItem e1) {
						topLevel.done(e1);
					}
				});
		for (int i = 0; i < this.getCount(); ++i) {
			WMenuItem item = this.itemAt(i);
			WPopupMenu subMenu = ((item.getMenu()) instanceof WPopupMenu ? (WPopupMenu) (item
					.getMenu())
					: null);
			if (subMenu != null) {
				subMenu.connectSignals(topLevel);
			}
		}
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WPopupMenu",
				"function(p,c,q){function i(){p.emit(c.id,\"cancel\")}function r(a,b){$(a).toggleClass(\"active\",b)}function j(a){if(a.subMenu)return a.subMenu;else{var b=a.lastChild;if(b&&f.hasTag(b,\"UL\")){a.subMenu=b;b.parentItem=a;$(b).mousemove(s);k(b);return b}else return null}}function x(a){a.style.display=\"block\";if(a.parentNode==a.parentItem){a.parentNode.removeChild(a);c.parentNode.appendChild(a)}f.positionAtWidget(a.id,a.parentItem.id,f.Horizontal,-f.px(a, \"paddingTop\"));l(a,null)}function l(a,b){function t(h,d){if(h==d)return true;else if(d)return(d=d.parentNode.parentItem)?t(h,d):false;else return false}function m(h){var d,u;d=0;for(u=h.childNodes.length;d<u;++d){var e=h.childNodes[d];if(t(e,b)){if(e!==b)(e=j(e))&&m(e)}else{r(e,false);if(e=j(e)){e.style.display=\"none\";m(e)}}}}m(a)}function s(a){for(a=f.target(a);a&&!f.hasTag(a,\"LI\")&&!f.hasTag(a,\"UL\");)a=a.parentNode;if(f.hasTag(a,\"LI\"))if(a!==n){n=a;r(a,true);var b=j(a);b&&x(b);l(c,a)}}function y(){o= false;clearTimeout(g);if(q>=0)g=setTimeout(i,q)}function z(){o=true;clearTimeout(g)}function k(a){$(a).mouseleave(y).mouseenter(z)}function v(){i()}function w(a){a.keyCode==27&&i()}jQuery.data(c,\"obj\",this);var f=p.WT,g=null,o=false,n=null;this.setHidden=function(a){if(g){clearTimeout(g);g=null}o=false;n=null;if(a){c.style.position=\"\";c.style.display=\"\";c.style.left=\"\";c.style.top=\"\";$(document).unbind(\"click\",v);$(document).unbind(\"keydown\",w)}else{setTimeout(function(){$(document).bind(\"click\", v);$(document).bind(\"keydown\",w)},0);c.style.display=\"block\"}l(c,null)};this.popupAt=function(a){k(a)};setTimeout(function(){k(c)},0);$(c).mousemove(s)}");
	}
}
