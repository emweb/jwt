/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A menu presented in a popup window.
 *
 * <p>The menu implements a typical context menu, with support for submenu&apos;s. It is a
 * specialized {@link WMenu} from which it inherits most of the API.
 *
 * <p>When initially created, the menu is invisible, until {@link WPopupMenu#popup(WPoint p)
 * popup()} or exec() is called. Then, the menu will remain visible until an item is selected, or
 * the user cancels the menu (by hitting Escape or clicking elsewhere).
 *
 * <p>The implementation assumes availability of JavaScript to position the menu at the current
 * mouse position and provide feed-back of the currently selected item.
 *
 * <p>As with {@link WDialog}, there are two ways of using the menu. The simplest way is to use one
 * of the synchronous exec() methods, which starts a reentrant event loop and waits until the user
 * cancelled the popup menu (by hitting Escape or clicking elsewhere), or selected an item.
 *
 * <p>Alternatively, you can use one of the {@link WPopupMenu#popup(WPoint p) popup()} methods to
 * show the menu and listen to the {@link WPopupMenu#triggered() triggered()} signal where you read
 * the {@link WPopupMenu#getResult() getResult()}, or associate the menu with a button using {@link
 * WPushButton#setMenu(WPopupMenu popupMenu) WPushButton#setMenu()}.
 *
 * <p>You have several options to react to the selection of an item:
 *
 * <ul>
 *   <li>Either you use the {@link WMenuItem} itself to identify the action, perhaps by
 *       specialization or simply by binding custom data using {@link WMenuItem#setData(Object data)
 *       WMenuItem#setData()}.
 *   <li>You can bind a separate method to each item&apos;s {@link WMenuItem#triggered()} signal.
 * </ul>
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * // Create a menu with some items
 * WPopupMenu popup = new WPopupMenu();
 * popup.addItem("icons/item1.gif", "Item 1");
 * popup.addItem("Item 2").setCheckable(true);
 * popup.addItem("Item 3");
 * popup.addSeparator();
 * popup.addItem("Item 4");
 * popup.addSeparator();
 * popup.addItem("Item 5");
 * popup.addItem("Item 6");
 * popup.addSeparator();
 *
 * WPopupMenu subMenu = new WPopupMenu();
 * subMenu.addItem("Sub Item 1");
 * subMenu.addItem("Sub Item 2");
 * popup.addMenu("Item 7", subMenu);
 *
 * WMenuItem item = popup.exec(event);
 *
 * if (item != null) {
 * // ... do associated action.
 * }
 *
 * }</pre>
 *
 * <p>A snapshot of the {@link WPopupMenu}: <div align="center"> <img
 * src="doc-files/WPopupMenu-default-1.png">
 *
 * <p><strong>WPopupMenu example (default)</strong> </div>
 *
 * <p><div align="center"> <img src="doc-files/WPopupMenu-polished-1.png">
 *
 * <p><strong>WPopupMenu example (polished)</strong> </div>
 *
 * <p>
 *
 * @see WMenuItem
 */
public class WPopupMenu extends WMenu {
  private static Logger logger = LoggerFactory.getLogger(WPopupMenu.class);

  /**
   * Enumeration of auto-hide options.
   *
   * <p>This enumeration is used to configure how auto-hide affects other submenus of the same menu.
   *
   * <p>
   *
   * @see WPopupMenu#setAutoHideBehaviour(WPopupMenu.AutoHideBehaviour behaviour)
   * @see WPopupMenu#setAutoHide(boolean enabled, int autoHideDelay)
   */
  public enum AutoHideBehaviour {
    /** When the mouse leaves the menu, hides the top-most submenu with auto-hide enabled. */
    HideAllEnabled(0),
    /**
     * As long as a submenu with auto-hide disabled is visible, keeps all of its parent menus
     * visible as well.
     */
    HideAfterLastDisabled(1),
    /**
     * Prevents hiding (due to auto-hide) to propagate to the parent menu. This means that only the
     * submenu left by the mouse (and its submenus) could be hidden.
     */
    KeepParents(2);

    private int value;

    AutoHideBehaviour(int value) {
      this.value = value;
    }

    /** Returns the numerical representation of this enum. */
    public int getValue() {
      return value;
    }
  }
  /**
   * Creates a new popup menu.
   *
   * <p>The menu is hidden, by default, and must be shown using {@link WPopupMenu#popup(WPoint p)
   * popup()} or exec().
   */
  public WPopupMenu(WStackedWidget contentsStack, WContainerWidget parentContainer) {
    super(contentsStack, (WContainerWidget) null);
    this.flags_ = new BitSet();
    this.topLevel_ = null;
    this.result_ = null;
    this.location_ = null;
    this.button_ = null;
    this.aboutToHide_ = new Signal();
    this.triggered_ = new Signal1<WMenuItem>();
    this.cancel_ = new JSignal(this, "cancel");
    this.recursiveEventLoop_ = false;
    this.hideOnSelect_ = true;
    this.open_ = false;
    this.adjustFlags_ = Orientation.AllOrientations;
    this.autoHideDelay_ = -1;
    this.renderedAutoHideDelay_ = -1;
    this.autoHideBehaviour_ = WPopupMenu.AutoHideBehaviour.HideAllEnabled;
    String CSS_RULES_NAME = "Wt::WPopupMenu";
    WApplication app = WApplication.getInstance();
    if (!app.getStyleSheet().isDefined(CSS_RULES_NAME)) {
      app.getStyleSheet()
          .addRule(".Wt-notselected .Wt-popupmenu", "visibility: hidden;", CSS_RULES_NAME);
    }
    app.addGlobalWidget(this);
    this.getWebWidget().setBaseZIndex(110000);
    this.setPopup(true);
    this.hide();
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a new popup menu.
   *
   * <p>Calls {@link #WPopupMenu(WStackedWidget contentsStack, WContainerWidget parentContainer)
   * this((WStackedWidget)null, (WContainerWidget)null)}
   */
  public WPopupMenu() {
    this((WStackedWidget) null, (WContainerWidget) null);
  }
  /**
   * Creates a new popup menu.
   *
   * <p>Calls {@link #WPopupMenu(WStackedWidget contentsStack, WContainerWidget parentContainer)
   * this(contentsStack, (WContainerWidget)null)}
   */
  public WPopupMenu(WStackedWidget contentsStack) {
    this(contentsStack, (WContainerWidget) null);
  }

  public void remove() {
    if (this.button_ != null) {
      WPushButton b = ObjectUtils.cast(this.button_, WPushButton.class);
      if (b != null && b.getMenu() == this) {
        b.setMenu((WPopupMenu) null);
      }
    }
    if (this.isGlobalWidget()) {
      WApplication.getInstance().removeGlobalWidget(this);
    }
    super.remove();
  }
  /**
   * Shows the the popup at a position.
   *
   * <p>Displays the popup at a point with document coordinates <code>point</code>. The positions
   * intelligent, and will chose one of the four menu corners to correspond to this point so that
   * the popup menu is completely visible within the window.
   *
   * <p>
   */
  public void popup(final WPoint p) {
    this.popupImpl();
    this.setOffsets(new WLength(42), EnumSet.of(Side.Left, Side.Top));
    this.setOffsets(new WLength(-10000), EnumSet.of(Side.Left, Side.Top));
    String canAdjustX = this.adjustFlags_.contains(Orientation.Horizontal) ? "true" : "false";
    String canAdjustY = this.adjustFlags_.contains(Orientation.Vertical) ? "true" : "false";
    this.doJavaScript(
        "Wt4_12_2.positionXY('"
            + this.getId()
            + "',"
            + String.valueOf(p.getX())
            + ","
            + String.valueOf(p.getY())
            + ","
            + canAdjustX
            + ","
            + canAdjustY
            + ");");
  }
  /**
   * Shows the the popup at the location of a mouse event.
   *
   * <p>This is a convenience method for {@link WPopupMenu#popup(WPoint p) popup()} that uses the
   * event&apos;s document coordinates.
   *
   * <p>
   *
   * @see WPopupMenu#popup(WPoint p)
   * @see WMouseEvent#getDocument()
   */
  public void popup(final WMouseEvent e) {
    this.popup(new WPoint(e.getDocument().x, e.getDocument().y));
  }

  public void setButton(WInteractWidget button) {
    this.button_ = button;
    if (this.button_ != null) {
      this.button_
          .clicked()
          .addListener(
              this,
              (WMouseEvent e1) -> {
                WPopupMenu.this.popupAtButton();
              });
      this.button_.addThemeStyle("dropdown-toggle");
    }
  }
  /**
   * Shows the popup besides a widget.
   *
   * <p>
   *
   * @see WWidget#positionAt(WWidget widget, Orientation orientation, EnumSet adjustOrientations)
   */
  public void popup(WWidget location, Orientation orientation) {
    this.location_ = location;
    this.popupImpl();
    this.doJavaScript(this.getJsRef() + ".wtObj.popupAt(" + location.getJsRef() + ");");
    this.positionAt(location, orientation, this.adjustFlags_);
  }
  /**
   * Shows the popup besides a widget.
   *
   * <p>Calls {@link #popup(WWidget location, Orientation orientation) popup(location,
   * Orientation.Vertical)}
   */
  public final void popup(WWidget location) {
    popup(location, Orientation.Vertical);
  }
  /**
   * Executes the the popup at a position.
   *
   * <p>Displays the popup at a point with document coordinates <code>p</code>, using {@link
   * WPopupMenu#popup(WPoint p) popup()}, and the waits until a menu item is selected, or the menu
   * is cancelled.
   *
   * <p>Returns the selected menu (or sub-menu) item, or <code>null</code> if the user cancelled the
   * menu.
   *
   * <p>
   *
   * @see WPopupMenu#popup(WPoint p)
   */
  public WMenuItem exec(final WPoint p) {
    if (this.recursiveEventLoop_) {
      throw new WException("WPopupMenu::exec(): already being executed.");
    }
    this.popup(p);
    this.exec();
    return this.result_;
  }
  /**
   * Executes the the popup at the location of a mouse event.
   *
   * <p>This is a convenience method for {@link WPopupMenu#exec(WPoint p) exec()} that uses the
   * event&apos;s document coordinates.
   *
   * <p>
   *
   * @see WPopupMenu#exec(WPoint p)
   */
  public WMenuItem exec(final WMouseEvent e) {
    return this.exec(new WPoint(e.getDocument().x, e.getDocument().y));
  }
  /**
   * Executes the popup besides a widget.
   *
   * <p>
   *
   * @see WWidget#positionAt(WWidget widget, Orientation orientation, EnumSet adjustOrientations)
   */
  public WMenuItem exec(WWidget location, Orientation orientation) {
    if (this.recursiveEventLoop_) {
      throw new WException("WPopupMenu::exec(): already being executed.");
    }
    this.popup(location, orientation);
    this.exec();
    return this.result_;
  }
  /**
   * Executes the popup besides a widget.
   *
   * <p>Returns {@link #exec(WWidget location, Orientation orientation) exec(location,
   * Orientation.Vertical)}
   */
  public final WMenuItem exec(WWidget location) {
    return exec(location, Orientation.Vertical);
  }
  /**
   * Returns the last triggered menu item.
   *
   * <p>The result is <code>null</code> when the user cancelled the popup menu.
   */
  public WMenuItem getResult() {
    return this.result_;
  }

  public void setHidden(boolean hidden, final WAnimation animation) {
    WApplication app = WApplication.getInstance();
    if (hidden != this.isHidden()) {
      this.handleFocusOnHide(hidden);
    }
    super.setHidden(hidden, animation);
    if (this.cancel_.isConnected()
        || WApplication.getInstance().getSession().getRenderer().isPreLearning()) {
      this.doJavaScript(this.getJsRef() + ".wtObj.setHidden(" + (hidden ? "1" : "0") + ");");
    }
  }

  public void setMaximumSize(final WLength width, final WLength height) {
    super.setMaximumSize(width, height);
    this.getUl().setMaximumSize(width, height);
  }

  public void setMinimumSize(final WLength width, final WLength height) {
    super.setMinimumSize(width, height);
    this.getUl().setMinimumSize(width, height);
  }
  /**
   * Signal emitted when the popup is hidden.
   *
   * <p>Unlike the {@link WMenu#itemSelected()} signal, {@link WPopupMenu#aboutToHide()
   * aboutToHide()} is only emitted by the toplevel popup menu (and not by submenus), and is also
   * emitted when no item was selected.
   *
   * <p>You can use {@link WPopupMenu#getResult() getResult()} to get the selected item, which may
   * be <code>null</code>.
   *
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
   *
   * <p>Unlike the {@link WMenu#itemSelected()} signal, {@link WPopupMenu#triggered() triggered()}
   * is only emitted by the toplevel popup menu (and not by submenus).
   *
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
   *
   * <p>If <code>enabled</code>, The popup menu will be hidden when the mouse leaves the menu, its
   * submenus or its parent menu, for longer than <code>autoHideDelay</code> (milliseconds).
   *
   * <p>It is possible to configure how this affects its submenus and parents menus using {@link
   * WPopupMenu#setAutoHideBehaviour(WPopupMenu.AutoHideBehaviour behaviour)
   * setAutoHideBehaviour()}.
   *
   * <p>If the top-level menu is automatically hidden, its {@link WPopupMenu#getResult()
   * getResult()} will be a <code>null</code>, as if the user cancelled.
   *
   * <p>By default, this option is disabled.
   */
  public void setAutoHide(boolean enabled, int autoHideDelay) {
    if (enabled) {
      this.autoHideDelay_ = autoHideDelay;
    } else {
      this.autoHideDelay_ = -1;
    }
    this.flags_.set(BIT_AUTO_HIDE_CHANGED);
    this.scheduleRender();
  }
  /**
   * Configure auto-hide when the mouse leaves the menu.
   *
   * <p>Calls {@link #setAutoHide(boolean enabled, int autoHideDelay) setAutoHide(enabled, 0)}
   */
  public final void setAutoHide(boolean enabled) {
    setAutoHide(enabled, 0);
  }
  /**
   * Configures auto-hide behaviour.
   *
   * <p>Defines how auto-hide behaves in this popup menu.
   *
   * <p>This setting can only be set on the top-level popup menu. Setting it on a submenu has no
   * effect.
   *
   * <p>By default, the behaviour is {@link WPopupMenu.AutoHideBehaviour#HideAllEnabled
   * HideAllEnabled}.
   *
   * <p>
   *
   * @see WPopupMenu#setAutoHide(boolean enabled, int autoHideDelay)
   */
  public void setAutoHideBehaviour(WPopupMenu.AutoHideBehaviour behaviour) {
    this.autoHideBehaviour_ = behaviour;
    this.flags_.set(BIT_AUTO_HIDE_BEHAVIOR_CHANGED);
    this.scheduleRender();
  }
  /**
   * Returns the auto-hide behaviour.
   *
   * <p>
   *
   * @see WPopupMenu#setAutoHideBehaviour(WPopupMenu.AutoHideBehaviour behaviour)
   */
  public WPopupMenu.AutoHideBehaviour getAutoHideBehaviour() {
    return this.autoHideBehaviour_;
  }
  /**
   * Set whether this popup menu should hide when an item is selected.
   *
   * <p>Defaults to true.
   *
   * <p>
   *
   * @see WPopupMenu#isHideOnSelect()
   */
  public void setHideOnSelect(boolean enabled) {
    this.hideOnSelect_ = enabled;
  }
  /**
   * Set whether this popup menu should hide when an item is selected.
   *
   * <p>Calls {@link #setHideOnSelect(boolean enabled) setHideOnSelect(true)}
   */
  public final void setHideOnSelect() {
    setHideOnSelect(true);
  }
  /**
   * Returns whether this popup menu should hide when an item is selected.
   *
   * <p>
   *
   * @see WPopupMenu#setHideOnSelect(boolean enabled)
   */
  public boolean isHideOnSelect() {
    return this.hideOnSelect_;
  }
  /**
   * Sets in which direction this popup menu can adjust its coordinates on popup.
   *
   * <p>This sets in which orientations the popup menu can adjust its position in order to be fully
   * visible in the window, potentially hiding the widget (or point) from which it popped up. @see
   * WWidget#positionAt(WWidget widget, Orientation orientation, EnumSet adjustOrientations)
   *
   * <p>By default, it can adjust in both orientations.
   */
  public void setAdjust(EnumSet<Orientation> adjustOrientations) {
    this.adjustFlags_ = EnumSet.copyOf(adjustOrientations);
    this.refresh();
    this.scheduleRerender(true);
  }
  /**
   * Sets in which direction this popup menu can adjust its coordinates on popup.
   *
   * <p>Calls {@link #setAdjust(EnumSet adjustOrientations) setAdjust(EnumSet.of(adjustOrientation,
   * adjustOrientations))}
   */
  public final void setAdjust(Orientation adjustOrientation, Orientation... adjustOrientations) {
    setAdjust(EnumSet.of(adjustOrientation, adjustOrientations));
  }
  /**
   * Returns in which orientations this popup widget can adjust its coordinates on popup.
   *
   * <p>
   *
   * @see WPopupMenu#setAdjust(EnumSet adjustOrientations)
   */
  public EnumSet<Orientation> getAdjust() {
    return this.adjustFlags_;
  }

  protected void renderSelected(WMenuItem item, boolean selected) {}

  protected void setCurrent(int index) {
    if (this.getContentsStack() != null) {
      super.setCurrent(index);
    }
  }

  void getSDomChanges(final List<DomElement> result, WApplication app) {
    super.getSDomChanges(result, app);
    this.flags_.clear(BIT_WILL_POPUP);
  }

  protected void render(EnumSet<RenderFlag> flags) {
    if (this.flags_.get(BIT_OPEN_CHANGED)) {
      if (this.button_ != null && this.button_.isThemeStyleEnabled()) {
        this.button_.toggleStyleClass("active", this.open_, true);
      }
      if (this.getParentItem() != null && this.isThemeStyleEnabled()) {
        this.getParentItem().toggleStyleClass("open", this.open_);
      }
      this.flags_.clear(BIT_OPEN_CHANGED);
    }
    if (this.cancel_.isConnected() && this.flags_.get(BIT_AUTO_HIDE_BEHAVIOR_CHANGED)) {
      StringBuilder s = new StringBuilder();
      s.append(this.getJsRef())
          .append(".wtObj.setAutoHideBehaviour(")
          .append(this.autoHideBehaviour_.getValue())
          .append(");");
      this.doJavaScript(s.toString());
      this.flags_.clear(BIT_AUTO_HIDE_BEHAVIOR_CHANGED);
    }
    if (this.flags_.get(BIT_AUTO_HIDE_CHANGED)) {
      final String styleClassBase = "Wt-AutoHideDelay-";
      if (this.renderedAutoHideDelay_ != -1) {
        this.removeStyleClass(styleClassBase + String.valueOf(this.renderedAutoHideDelay_));
      }
      if (this.autoHideDelay_ != -1) {
        this.addStyleClass(styleClassBase + String.valueOf(this.autoHideDelay_));
      }
      this.renderedAutoHideDelay_ = this.autoHideDelay_;
      this.flags_.clear(BIT_AUTO_HIDE_CHANGED);
    }
    if (this.adjustFlags_.contains(Orientation.Horizontal)) {
      this.addStyleClass("Wt-AdjustX");
    } else {
      this.removeStyleClass("Wt-AdjustX");
    }
    if (this.adjustFlags_.contains(Orientation.Vertical)) {
      this.addStyleClass("Wt-AdjustY");
    } else {
      this.removeStyleClass("Wt-AdjustY");
    }
    super.render(flags);
    this.flags_.clear(BIT_WILL_POPUP);
  }

  String renderRemoveJs(boolean recursive) {
    String result = super.renderRemoveJs(true);
    result += "Wt4_12_2.remove('" + this.getId() + "');";
    return result;
  }

  private static final int BIT_WILL_POPUP = 0;
  private static final int BIT_OPEN_CHANGED = 1;
  private static final int BIT_AUTO_HIDE_CHANGED = 2;
  private static final int BIT_AUTO_HIDE_BEHAVIOR_CHANGED = 3;
  private BitSet flags_;
  private WPopupMenu topLevel_;
  WMenuItem result_;
  private WWidget location_;
  private WInteractWidget button_;
  private Signal aboutToHide_;
  private Signal1<WMenuItem> triggered_;
  private JSignal cancel_;
  private boolean recursiveEventLoop_;
  private boolean hideOnSelect_;
  private boolean open_;
  private EnumSet<Orientation> adjustFlags_;
  private int autoHideDelay_;
  private int renderedAutoHideDelay_;
  private WPopupMenu.AutoHideBehaviour autoHideBehaviour_;

  private void exec() {
    WApplication app = WApplication.getInstance();
    this.recursiveEventLoop_ = true;
    if (app.getEnvironment().isTest()) {
      app.getEnvironment().popupExecuted().trigger(this);
      if (this.recursiveEventLoop_) {
        throw new WException("Test case must close popup menu.");
      }
    } else {
      do {
        app.waitForEvent();
      } while (this.recursiveEventLoop_);
    }
  }

  private void cancel() {
    if (this.flags_.get(BIT_WILL_POPUP)) {
      return;
    }
    if (!this.isHidden()) {
      this.done((WMenuItem) null);
    }
  }

  private void done(WMenuItem result) {
    if (this.isHidden()) {
      return;
    }
    if (this.location_ != null && this.location_ == this.button_) {
      this.open_ = false;
      this.flags_.set(BIT_OPEN_CHANGED);
      this.scheduleRender();
    }
    this.location_ = null;
    this.result_ = result;
    boolean shouldHide =
        !(result != null) || ((WPopupMenu) result.getParentMenu()).isHideOnSelect();
    if (shouldHide) {
      this.hide();
    }
    this.recursiveEventLoop_ = false;
    if (this.result_ != null) {
      this.triggered_.trigger(this.result_);
    }
    if (shouldHide) {
      this.aboutToHide_.trigger();
    }
  }

  private void popupImpl() {
    this.result_ = null;
    WApplication app = WApplication.getInstance();
    this.prepareRender(app);
    this.show();
    this.flags_.set(BIT_WILL_POPUP);
    this.scheduleRender();
  }

  private void prepareRender(WApplication app) {
    if (!this.cancel_.isConnected()) {
      app.loadJavaScript("js/WPopupMenu.js", wtjs1());
      StringBuilder s = new StringBuilder();
      s.append("new Wt4_12_2.WPopupMenu(")
          .append(app.getJavaScriptClass())
          .append(',')
          .append(this.getJsRef())
          .append(',')
          .append(this.autoHideDelay_)
          .append(',')
          .append(this.autoHideBehaviour_.getValue())
          .append(");");
      this.setJavaScriptMember(" WPopupMenu", s.toString());
      this.cancel_.addListener(
          this,
          () -> {
            WPopupMenu.this.cancel();
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
      WPopupMenu subMenu = ObjectUtils.cast(item.getMenu(), WPopupMenu.class);
      if (subMenu != null) {
        subMenu.adjustPadding();
      }
    }
  }

  private void popupAtButton() {
    if (!this.isHidden()) {
      return;
    }
    if (!(this.topLevel_ != null) || this.topLevel_ == this) {
      this.open_ = true;
      this.flags_.set(BIT_OPEN_CHANGED);
      this.popup(this.button_);
      this.scheduleRender();
    }
  }

  private void connectSignals(final WPopupMenu topLevel) {
    this.topLevel_ = topLevel;
    this.itemSelected()
        .addListener(
            topLevel,
            (WMenuItem e1) -> {
              topLevel.done(e1);
            });
    for (int i = 0; i < this.getCount(); ++i) {
      WMenuItem item = this.itemAt(i);
      WPopupMenu subMenu = ObjectUtils.cast(item.getMenu(), WPopupMenu.class);
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
        "(function(e,t,n,o){t.wtObj=this;const i=e.WT,s=\"Wt-AutoHideDelay-\",u=0,r=1,c=2;let a=o,d=null,l=null,f=null,m=null;if(i.isIOS){t.addEventListener(\"touchstart\",W);t.addEventListener(\"touchend\",k)}function p(){E(t,null);t.style.display=\"none\";setTimeout((function(){e.emit(t.id,\"cancel\")}),0)}function v(e,t){e.classList.toggle(\"active\",t)}function L(e){if(e.subMenu)return e.subMenu;{const t=e.lastChild;if(t&&i.hasTag(t,\"UL\")){e.subMenu=t;t.parentItem=e;t.addEventListener(\"mousemove\",h);g(t,e);return t}return null}}function E(e,t){function n(e,t){if(e===t)return!0;if(t){const o=t.parentNode.parentItem;return!!o&&n(e,o)}return!1}!function e(o){for(const i of o.childNodes)if(n(i,t)){if(i!==t){const t=L(i);t&&e(t)}}else{v(i,!1);const t=L(i);if(t){t.style.display=\"none\";e(t)}}}(e)}function h(e){let n=i.target(e);for(;n&&!i.hasTag(n,\"LI\")&&!i.hasTag(n,\"UL\");)n=n.parentNode;if(i.hasTag(n,\"LI\")){if(n===l)return;l=n;v(n,!0);const e=L(n);e&&function(e){e.style.display=\"block\";if(e.parentNode===e.parentItem){e.parentNode.removeChild(e);t.parentNode.appendChild(e)}const n=i.px(e,\"paddingTop\")+i.pxComputedStyle(e,\"borderTopWidth\");i.positionAtWidget(e.id,e.parentItem.id,i.Horizontal,-n,e.classList.contains(\"Wt-AdjustX\"),e.classList.contains(\"Wt-AdjustY\"));E(e,null);f=e;if(i.isIOS){e.removeEventListener(\"touchstart\",W);e.addEventListener(\"touchstart\",W);e.removeEventListener(\"touchend\",k);e.addEventListener(\"touchend\",k)}}(e);E(t,n)}}function y(){for(;f&&\"none\"===f.style.display;)f=f.parentItem?f.parentItem.parentNode:null;return f}function b(){clearTimeout(d)}function T(e){let t=-1;for(const n of e.classList)if(n.startsWith(s)){const e=n.substring(s.length),o=parseInt(e);isNaN(o)||(t=o)}return t}function g(e,t=null){let o;e.wtObj||(e.wtObj={});e.wtObj.mouseEnter=function(){clearTimeout(o);t&&t.parentNode.wtObj.mouseEnter()};e.wtObj.mouseLeave=function(){const n=T(e);if(t){if(n>=0){clearTimeout(o);o=setTimeout((function(){const n=y();if(\"none\"!==e.style.display&&(a!==r||!n||T(n)>=0)){l=null;E(t.parentNode,null)}}),n)}(a===u||a===r&&n>=0)&&t.parentNode.wtObj.mouseLeave()}};if(t){t.parentNode.addEventListener(\"mouseleave\",e.wtObj.mouseLeave);t.parentNode.addEventListener(\"mouseenter\",e.wtObj.mouseEnter);e.addEventListener(\"mouseleave\",e.wtObj.mouseLeave);e.addEventListener(\"mouseenter\",e.wtObj.mouseEnter)}e.addEventListener(\"mouseleave\",(function(){a===c&&t||function(){clearTimeout(d);n>=0&&(d=setTimeout((function(){const e=y();(a!==r||!e||T(e)>=0)&&p()}),n))}()}));e.addEventListener(\"mouseenter\",b)}function w(){return null!==document.getElementById(t.id)}function O(e){w()&&1!==i.button(e)&&p()}function I(){w()&&p()}function N(e){w()&&27===e.keyCode&&p()}this.setAutoHideBehaviour=function(e){a=e};this.setHidden=function(e){if(d){clearTimeout(d);d=null}l=null;if(e){t.style.position=\"\";t.style.display=\"none\";t.style.left=\"\";t.style.top=\"\";document.removeEventListener(\"mousedown\",O);!function(){if(i.isIOS){document.removeEventListener(\"touchstart\",j);document.removeEventListener(\"touchend\",M)}else document.removeEventListener(\"click\",I)}();document.removeEventListener(\"keydown\",N)}else{setTimeout((function(){document.addEventListener(\"mousedown\",O);!function(){if(i.isIOS){document.addEventListener(\"touchstart\",j);document.addEventListener(\"touchend\",M)}else document.addEventListener(\"click\",I)}();document.addEventListener(\"keydown\",N)}),0);t.style.display=\"block\"}E(t,null)};this.popupAt=function(e){g(e)};function j(e){const t=e.originalEvent.touches;m=t.length>1?null:{x:t[0].screenX,y:t[0].screenY}}function W(e){e.stopPropagation()}function M(e){if(m){const t=e.originalEvent.changedTouches[0];Math.abs(m.x-t.screenX)<20&&Math.abs(m.y-t.screenY)<20&&I();m=null}}function k(e){e.stopPropagation()}setTimeout((function(){g(t)}),0);t.addEventListener(\"mousemove\",h)})");
  }
}
