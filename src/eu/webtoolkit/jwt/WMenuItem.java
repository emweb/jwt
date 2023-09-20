/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A single item in a menu.
 *
 * <p>Since JWt 3.3.0, this item is now a proper widget, which renders a single item in a menu.
 *
 * <p>An optional contents item can be associated with a menu item, which is inserted and shown in
 * the widget stack of the menu to which this menu item belongs.
 *
 * <p>
 *
 * <h3>CSS</h3>
 *
 * <p>A menu item renders as a &gt;li&amp;;lt with additional markup/style classes provided by the
 * theme. Unless you use the bootstrap theme, you will need to provide appropriate CSS.
 */
public class WMenuItem extends WContainerWidget {
  private static Logger logger = LoggerFactory.getLogger(WMenuItem.class);

  /**
   * Creates a new item with given label.
   *
   * <p>The optional contents is a widget that will be shown in the {@link WMenu} contents stack
   * when the item is selected. For this widget, a load <code>policy</code> specifies whether the
   * contents widgets is transmitted only when it the item is activated for the first time
   * (LazyLoading) or transmitted prior to first rendering.
   *
   * <p>If the menu supports internal path navigation, then a default {@link
   * WMenuItem#getPathComponent() getPathComponent()} will be derived from the <code>label</code>,
   * and can be customized using {@link WMenuItem#setPathComponent(String path) setPathComponent()}.
   */
  public WMenuItem(final CharSequence text, WWidget contents, ContentLoading policy) {
    super();
    this.uContents_ = null;
    this.oContents_ = null;
    this.uContentsContainer_ = null;
    this.oContentsContainer_ = null;
    this.separator_ = false;
    this.triggered_ = new Signal1<WMenuItem>();
    this.pathComponent_ = "";
    this.create("", text, contents, policy);
  }
  /**
   * Creates a new item with given label.
   *
   * <p>Calls {@link #WMenuItem(CharSequence text, WWidget contents, ContentLoading policy)
   * this(text, null, ContentLoading.Lazy)}
   */
  public WMenuItem(final CharSequence text) {
    this(text, null, ContentLoading.Lazy);
  }
  /**
   * Creates a new item with given label.
   *
   * <p>Calls {@link #WMenuItem(CharSequence text, WWidget contents, ContentLoading policy)
   * this(text, contents, ContentLoading.Lazy)}
   */
  public WMenuItem(final CharSequence text, WWidget contents) {
    this(text, contents, ContentLoading.Lazy);
  }

  public WMenuItem(
      final String iconPath, final CharSequence text, WWidget contents, ContentLoading policy) {
    super();
    this.uContents_ = null;
    this.oContents_ = null;
    this.uContentsContainer_ = null;
    this.oContentsContainer_ = null;
    this.separator_ = false;
    this.triggered_ = new Signal1<WMenuItem>();
    this.pathComponent_ = "";
    this.create(iconPath, text, contents, policy);
  }

  public WMenuItem(final String iconPath, final CharSequence text) {
    this(iconPath, text, null, ContentLoading.Lazy);
  }

  public WMenuItem(final String iconPath, final CharSequence text, WWidget contents) {
    this(iconPath, text, contents, ContentLoading.Lazy);
  }

  public void remove() {
    super.remove();
  }
  /**
   * Sets the text for this item.
   *
   * <p>Unless a custom path component was defined, the {@link WMenuItem#getPathComponent()
   * getPathComponent()} is also updated based on the new text.
   *
   * <p>The item widget is updated using updateItemWidget().
   *
   * <p>
   *
   * @see WMenuItem#setPathComponent(String path)
   */
  public void setText(final CharSequence text) {
    if (!(this.text_ != null)) {
      this.text_ = new WLabel();
      this.getAnchor().addWidget(this.text_);
      this.text_.setTextFormat(TextFormat.Plain);
    }
    this.text_.setText(text);
    if (!this.customPathComponent_) {
      String result = "";
      WString t = WString.toWString(text);
      if (t.isLiteral()) {
        result = t.toString();
      } else {
        result = t.getKey();
      }
      for (int i = 0; i < result.length(); ++i) {
        if (Character.isWhitespace((char) result.charAt(i))) {
          result = StringUtils.put(result, i, '-');
        } else {
          if (Character.isLetterOrDigit((char) result.charAt(i))) {
            result = StringUtils.put(result, i, Character.toLowerCase((char) result.charAt(i)));
          } else {
            result = StringUtils.put(result, i, '_');
          }
        }
      }
      this.setPathComponent(result);
      this.customPathComponent_ = false;
    }
  }
  /**
   * Returns the text for this item.
   *
   * <p>
   *
   * @see WMenuItem#setText(CharSequence text)
   */
  public WString getText() {
    if (this.text_ != null) {
      return this.text_.getText();
    } else {
      return WString.Empty;
    }
  }
  /**
   * Sets the item icon path.
   *
   * <p>The icon should have a width of 16 pixels.
   *
   * <p>
   *
   * @see WMenuItem#setText(CharSequence text)
   */
  public void setIcon(final String path) {
    if (!(this.icon_ != null)) {
      WAnchor a = this.getAnchor();
      if (!(a != null)) {
        return;
      }
      this.icon_ = new WText(" ");
      a.insertWidget(0, this.icon_);
      WApplication app = WApplication.getInstance();
      app.getTheme().apply(this, this.icon_, WidgetThemeRole.MenuItemIcon);
    }
    this.icon_.getDecorationStyle().setBackgroundImage(new WLink(path));
  }
  /**
   * Returns the item icon path.
   *
   * <p>
   *
   * @see WMenuItem#setIcon(String path)
   */
  public String getIcon() {
    if (this.icon_ != null) {
      return this.icon_.getDecorationStyle().getBackgroundImage();
    } else {
      return "";
    }
  }
  /**
   * Sets if the item is checkable.
   *
   * <p>When an item is checkable, a checkbox is displayed to the left of the item text (instead of
   * an icon).
   *
   * <p>
   *
   * @see WMenuItem#setChecked(boolean checked)
   * @see WMenuItem#isChecked()
   */
  public void setCheckable(boolean checkable) {
    if (this.isCheckable() != checkable) {
      if (checkable) {
        this.checkBox_ = new WCheckBox();
        this.getAnchor().insertWidget(0, this.checkBox_);
        this.setText(this.getText());
        this.text_.setBuddy(this.checkBox_);
        WApplication app = WApplication.getInstance();
        app.getTheme().apply(this, this.checkBox_, WidgetThemeRole.MenuItemCheckBox);
      } else {
        {
          WWidget toRemove = WidgetUtils.remove(this.getAnchor(), this.checkBox_);
          if (toRemove != null) toRemove.remove();
        }

        this.checkBox_ = null;
      }
    }
  }
  /**
   * Returns whether the item is checkable.
   *
   * <p>
   *
   * @see WMenuItem#setCheckable(boolean checkable)
   */
  public boolean isCheckable() {
    return this.checkBox_ != null;
  }
  /**
   * Sets the path component for this item.
   *
   * <p>The path component is used by the menu item in the application internal path (see {@link
   * WApplication#setInternalPath(String path, boolean emitChange) WApplication#setInternalPath()}),
   * when internal paths are enabled (see {@link WMenu#setInternalPathEnabled(String basePath)
   * WMenu#setInternalPathEnabled()}) for the menu.
   *
   * <p>You may specify an empty <code>path</code> to let a menu item be the &quot;default&quot;
   * menu option.
   *
   * <p>For example, if {@link WMenu#getInternalBasePath()} is <code>&quot;/examples/&quot;</code>
   * and {@link WMenuItem#getPathComponent() getPathComponent()} for is <code>&quot;charts/&quot;
   * </code>, then the internal path for the item will be <code>&quot;/examples/charts/&quot;</code>
   * .
   *
   * <p>By default, the path is automatically derived from {@link WMenuItem#getText() getText()}. If
   * a {@link WString#isLiteral()} is used, the path is based on the text itself, otherwise on the
   * {@link WString#getKey()}. It is converted to lower case, and replacing whitespace and special
   * characters with &apos;_&apos;.
   *
   * <p>
   *
   * @see WMenuItem#setText(CharSequence text)
   * @see WMenu#setInternalPathEnabled(String basePath)
   */
  public void setPathComponent(final String path) {
    this.customPathComponent_ = true;
    this.pathComponent_ = path;
    this.updateInternalPath();
    if (this.menu_ != null) {
      this.menu_.itemPathChanged(this);
    }
  }
  /**
   * Returns the path component for this item.
   *
   * <p>You may want to reimplement this to customize the path component set by the item in the
   * application internal path.
   *
   * <p>
   *
   * @see WMenuItem#setPathComponent(String path)
   */
  public String getPathComponent() {
    return this.pathComponent_;
  }
  /**
   * Configures internal path support for the item.
   *
   * <p>This configures whether the item supports internal paths (in a menu which supports internal
   * paths).
   *
   * <p>The default value is <code>true</code> for all items but section headers and separators.
   *
   * <p>
   *
   * @see WMenu#setInternalPathEnabled(String basePath)
   */
  public void setInternalPathEnabled(boolean enabled) {
    this.internalPathEnabled_ = enabled;
    this.updateInternalPath();
  }
  /**
   * Returns whether an item participates in internal paths.
   *
   * <p>
   *
   * @see WMenuItem#setInternalPathEnabled(boolean enabled)
   */
  public boolean isInternalPathEnabled() {
    return this.internalPathEnabled_;
  }
  /** Sets the associated link. */
  public void setLink(final WLink link) {
    WAnchor a = this.getAnchor();
    if (a != null) {
      a.setLink(link);
    }
    this.customLink_ = true;
  }
  /**
   * Returns the associated link.
   *
   * <p>
   *
   * @see WMenuItem#setLink(WLink link)
   */
  public WLink getLink() {
    WAnchor a = this.getAnchor();
    if (a != null) {
      return a.getLink();
    } else {
      return new WLink();
    }
  }
  /**
   * Sets a sub menu.
   *
   * <p>In most cases, the sub menu would use the same contents stack as the parent menu.
   *
   * <p>Note that adding a submenu makes this item not {@link WMenuItem#isSelectable() selectable}
   * by default.
   *
   * <p>
   *
   * <p><i><b>Note: </b>If the {@link WMenuItem#getParentMenu() parent menu} is a {@link
   * WPopupMenu}, the submenu should also be a {@link WPopupMenu}. </i>
   *
   * @see WMenuItem#setSelectable(boolean selectable)
   */
  public void setMenu(WMenu menu) {
    this.subMenu_ = menu;
    this.subMenu_.parentItem_ = this;
    WPopupMenu popup = ObjectUtils.cast(this.subMenu_, WPopupMenu.class);
    if (popup != null) {
      WApplication.getInstance().removeGlobalWidget(menu);
    }
    this.addWidget(menu);
    if (this.subMenu_.isPopup() && this.getParentMenu() != null && this.getParentMenu().isPopup()) {
      this.subMenu_
          .getWebWidget()
          .setZIndex(Math.max(this.getParentMenu().getZIndex() + 1000, this.subMenu_.getZIndex()));
    }
    if (popup != null) {
      this.setSelectable(false);
      popup.setButton(this.getAnchor());
      this.updateInternalPath();
      if (ObjectUtils.cast(this.menu_, WPopupMenu.class) != null) {
        popup.show();
      }
    }
  }
  /**
   * Returns the submenu.
   *
   * <p>
   *
   * @see WMenuItem#setMenu(WMenu menu)
   */
  public WMenu getMenu() {
    return this.subMenu_;
  }
  /**
   * Sets the checked state.
   *
   * <p>This is only used when {@link WMenuItem#isCheckable() isCheckable()} == <code>true</code>.
   *
   * <p>
   *
   * @see WMenuItem#setCheckable(boolean checkable)
   * @see WMenuItem#isCheckable()
   */
  public void setChecked(boolean checked) {
    if (this.isCheckable()) {
      WCheckBox cb = ObjectUtils.cast(this.getAnchor().getWidget(0), WCheckBox.class);
      cb.setChecked(checked);
    }
  }
  /**
   * Returns the checked state.
   *
   * <p>This is only used when {@link WMenuItem#isCheckable() isCheckable()} == <code>true</code>.
   *
   * <p>
   *
   * @see WMenuItem#setChecked(boolean checked)
   * @see WMenuItem#isCheckable()
   */
  public boolean isChecked() {
    if (this.isCheckable()) {
      WCheckBox cb = ObjectUtils.cast(this.getAnchor().getWidget(0), WCheckBox.class);
      return cb.isChecked();
    } else {
      return false;
    }
  }
  /**
   * Sets whether the menu item can be selected.
   *
   * <p>Only a menu item that can be selected can be the result of a popup menu selection.
   *
   * <p>The default value is <code>true</code> for a normal menu item, and <code>false</code> for a
   * menu item that has a popup submenu.
   *
   * <p>An item that is selectable but is disabled can still not be selected.
   */
  public void setSelectable(boolean selectable) {
    this.selectable_ = selectable;
  }
  /**
   * Returns whether the menu item can be selected.
   *
   * <p>
   *
   * @see WMenuItem#setSelectable(boolean selectable)
   */
  public boolean isSelectable() {
    return this.selectable_;
  }
  /**
   * Sets associated additional data with the item.
   *
   * <p>You can use this to associate model information with a menu item.
   */
  public void setData(Object data) {
    this.data_ = data;
  }
  /**
   * Returns additional data of the item.
   *
   * <p>
   *
   * @see WMenuItem#setData(Object data)
   */
  public Object getData() {
    return this.data_;
  }
  /**
   * Returns the checkbox for a checkable item.
   *
   * <p>
   *
   * @see WMenuItem#setCheckable(boolean checkable)
   */
  public WCheckBox getCheckBox() {
    return this.checkBox_;
  }
  /**
   * Make it possible to close this item interactively or by {@link WMenuItem#close() close()}.
   *
   * <p>
   *
   * @see WMenuItem#close()
   * @see WMenuItem#isCloseable()
   */
  public void setCloseable(boolean closeable) {
    if (this.closeable_ != closeable) {
      this.closeable_ = closeable;
      if (this.closeable_) {
        WText closeIcon = new WText("");
        this.insertWidget(0, closeIcon);
        WApplication app = WApplication.getInstance();
        app.getTheme().apply(this, closeIcon, WidgetThemeRole.MenuItemClose);
        closeIcon
            .clicked()
            .addListener(
                this,
                (WMouseEvent e1) -> {
                  WMenuItem.this.close();
                });
      } else {
        {
          WWidget toRemove = this.removeWidget(this.getWidget(0));
          if (toRemove != null) toRemove.remove();
        }
      }
    }
  }
  /**
   * Returns whether the item is closeable.
   *
   * <p>
   *
   * @see WMenuItem#setCloseable(boolean closeable)
   */
  public boolean isCloseable() {
    return this.closeable_;
  }
  /**
   * Closes this item.
   *
   * <p>Hides the item widget and emits {@link WMenu#itemClosed()} signal. Only closeable items can
   * be closed.
   *
   * <p>
   *
   * @see WMenuItem#setCloseable(boolean closeable)
   * @see WWidget#hide()
   */
  public void close() {
    if (this.menu_ != null) {
      this.menu_.close(this);
    }
  }
  /** Returns the menu that contains this item. */
  public WMenu getParentMenu() {
    return this.menu_;
  }
  /**
   * Sets the contents widget for this item.
   *
   * <p>The contents is a widget that will be shown in the {@link WMenu} contents stack when the
   * item is selected. For this widget, the load <code>policy</code> specifies whether the contents
   * widgets is transmitted only when it the item is activated for the first time (LazyLoading) or
   * transmitted prior to first rendering.
   */
  public void setContents(WWidget contents, ContentLoading policy) {
    int menuIdx = -1;
    WMenu menu = this.menu_;
    WMenuItem self = null;
    if (menu != null) {
      menuIdx = menu.indexOf(this);
      self = menu.removeItem(this);
    }
    this.uContents_ = contents;
    this.oContents_ = this.uContents_;
    this.loadPolicy_ = policy;
    if (this.uContents_ != null && this.loadPolicy_ == ContentLoading.Lazy) {
      if (!(this.oContentsContainer_ != null)) {
        this.uContentsContainer_ = new WContainerWidget();
        this.oContentsContainer_ = this.uContentsContainer_;
        this.oContentsContainer_.setJavaScriptMember(
            "wtResize", StdWidgetItemImpl.getChildrenResizeJS());
        this.oContentsContainer_.resize(WLength.Auto, new WLength(100, LengthUnit.Percentage));
      }
    }
    if (menu != null) {
      menu.insertItem(menuIdx, self);
    }
  }
  /**
   * Sets the contents widget for this item.
   *
   * <p>Calls {@link #setContents(WWidget contents, ContentLoading policy) setContents(contents,
   * ContentLoading.Lazy)}
   */
  public final void setContents(WWidget contents) {
    setContents(contents, ContentLoading.Lazy);
  }
  /**
   * Returns the contents widget for this item.
   *
   * <p>
   *
   * @see WMenuItem#setContents(WWidget contents, ContentLoading policy)
   */
  public WWidget getContents() {
    return this.oContents_;
  }
  /** Removes the contents widget from this item. */
  public WWidget getRemoveContents() {
    WWidget contents = this.oContents_;
    this.oContents_ = null;
    WWidget c = this.getContentsInStack();
    if (c != null) {
      WWidget w = c.getParent().removeWidget(c);
      if (this.oContentsContainer_ != null) {
        return this.oContentsContainer_.removeWidget(contents);
      } else {
        return w;
      }
    } else {
      final WWidget result = this.uContents_;
      this.uContents_ = null;
      return result;
    }
  }
  /**
   * Selects this item.
   *
   * <p>If the item was previously closed it will be shown.
   *
   * <p>
   *
   * @see WMenuItem#close()
   */
  public void select() {
    if (this.menu_ != null && this.selectable_ && !this.isDisabled()) {
      this.menu_.select(this);
    }
  }
  /**
   * Signal emitted when an item is activated.
   *
   * <p>Returns this item as argument.
   *
   * <p>
   */
  public Signal1<WMenuItem> triggered() {
    return this.triggered_;
  }
  /**
   * Returns whether this item is a separator.
   *
   * <p>
   *
   * @see WMenu#addSeparator()
   */
  public boolean isSeparator() {
    return this.separator_;
  }
  /**
   * Returns whether this item is a section header.
   *
   * <p>
   *
   * @see WMenu#addSectionHeader(CharSequence text)
   */
  public boolean isSectionHeader() {
    WAnchor a = this.getAnchor();
    return !this.separator_ && !(a != null) && !(this.subMenu_ != null) && this.text_ != null;
  }
  /**
   * Returns the anchor of this menu item.
   *
   * <p>Can be used to add widgets to the menu.
   */
  public WAnchor getAnchor() {
    for (int i = 0; i < this.getCount(); ++i) {
      WAnchor result = ObjectUtils.cast(this.getWidget(i), WAnchor.class);
      if (result != null) {
        return result;
      }
    }
    return null;
  }
  /**
   * Renders the item as selected or unselected.
   *
   * <p>The default implementation sets the styleclass for itemWidget() to &apos;item&apos; for an
   * unselected not closeable, &apos;itemselected&apos; for selected not closeable,
   * &apos;citem&apos; for an unselected closeable and &apos;citemselected&apos; for selected
   * closeable item.
   *
   * <p>Note that this method is called from within a stateless slot implementation, and thus should
   * be stateless as well.
   */
  public void renderSelected(boolean selected) {
    WApplication app = WApplication.getInstance();
    String active = app.getTheme().getActiveClass();
    WBootstrap5Theme bs5Theme = ObjectUtils.cast(app.getTheme(), WBootstrap5Theme.class);
    if (active.equals("Wt-selected")) {
      this.removeStyleClass(!selected ? "itemselected" : "item", true);
      this.addStyleClass(selected ? "itemselected" : "item", true);
    } else {
      if (bs5Theme != null) {
        WAnchor a = this.getAnchor();
        a.toggleStyleClass(active, selected, true);
      }
      this.toggleStyleClass(active, selected, true);
    }
  }

  void setFromInternalPath(final String path) {
    if (this.isInternalPathEnabled()
        && this.menu_.contentsStack_ != null
        && this.menu_.contentsStack_.getCurrentWidget() != this.getContents()) {
      this.menu_.select(this.menu_.indexOf(this), false);
    }
    if (this.subMenu_ != null && this.subMenu_.isInternalPathEnabled()) {
      this.subMenu_.internalPathChanged(path);
    }
  }

  public void enableAjax() {
    if (this.menu_.isInternalPathEnabled()) {
      this.resetLearnedSlots();
    }
    if (this.uContents_ != null) {
      this.uContents_.enableAjax();
    }
    super.enableAjax();
  }

  public void setHidden(boolean hidden, final WAnimation animation) {
    super.setHidden(hidden, animation);
    if (hidden) {
      if (this.menu_ != null && !this.menu_.isHidden()) {
        this.menu_.onItemHidden(this.menu_.indexOf(this), true);
      }
    }
  }

  public void setDisabled(boolean disabled) {
    super.setDisabled(disabled);
    if (disabled) {
      if (this.menu_ != null && !this.menu_.isDisabled()) {
        this.menu_.onItemHidden(this.menu_.indexOf(this), true);
      }
    }
  }

  protected void render(EnumSet<RenderFlag> flags) {
    this.connectSignals();
    super.render(flags);
  }

  WMenuItem(boolean separator, final CharSequence text) {
    super();
    this.uContents_ = null;
    this.oContents_ = null;
    this.uContentsContainer_ = null;
    this.oContentsContainer_ = null;
    this.separator_ = true;
    this.triggered_ = new Signal1<WMenuItem>();
    this.pathComponent_ = "";
    this.create("", WString.Empty, (WWidget) null, ContentLoading.Lazy);
    this.separator_ = separator;
    this.selectable_ = false;
    this.internalPathEnabled_ = false;
    if (!(text.length() == 0)) {
      this.text_ = new WLabel();
      this.addWidget(this.text_);
      this.text_.setTextFormat(TextFormat.Plain);
      this.text_.setText(text);
    }
  }

  private ContentLoading loadPolicy_;
  private WWidget uContents_;
  private WWidget oContents_;
  private WContainerWidget uContentsContainer_;
  private WContainerWidget oContentsContainer_;
  private WMenu menu_;
  private WMenu subMenu_;
  private WText icon_;
  private WLabel text_;
  private WCheckBox checkBox_;
  private Object data_;
  private boolean separator_;
  private boolean selectable_;
  private boolean signalsConnected_;
  private boolean customLink_;
  private Signal1<WMenuItem> triggered_;
  private String pathComponent_;
  private boolean customPathComponent_;
  private boolean internalPathEnabled_;
  private boolean closeable_;

  private void create(
      final String iconPath, final CharSequence text, WWidget contents, ContentLoading policy) {
    this.customLink_ = false;
    this.menu_ = null;
    this.customPathComponent_ = false;
    this.internalPathEnabled_ = true;
    this.closeable_ = false;
    this.selectable_ = true;
    this.text_ = null;
    this.icon_ = null;
    this.checkBox_ = null;
    this.subMenu_ = null;
    this.data_ = null;
    this.setContents(contents, policy);
    if (!this.separator_) {
      this.addWidget(new WAnchor());
      this.updateInternalPath();
    }
    this.signalsConnected_ = false;
    if (iconPath.length() != 0) {
      this.setIcon(iconPath);
    }
    if (!this.separator_) {
      this.setText(text);
    }
  }
  //  void purgeContents() ;
  void updateInternalPath() {
    if (this.menu_ != null && this.menu_.isInternalPathEnabled() && this.isInternalPathEnabled()) {
      String internalPath = this.menu_.getInternalBasePath() + this.getPathComponent();
      WLink link = new WLink(LinkType.InternalPath, internalPath);
      WAnchor a = this.getAnchor();
      if (a != null) {
        a.setLink(link);
      }
    } else {
      WAnchor a = this.getAnchor();
      if (a != null && !this.customLink_) {
        if (WApplication.getInstance().getEnvironment().getAgent() == UserAgent.IE6) {
          a.setLink(new WLink("#"));
        } else {
          a.setLink(new WLink());
        }
      }
    }
  }

  private boolean isContentsLoaded() {
    return this.oContents_ != null && !(this.uContents_ != null);
  }

  void loadContents() {
    if (!(this.uContents_ != null)) {
      return;
    } else {
      if (!this.isContentsLoaded()) {
        this.oContentsContainer_.addWidget(this.uContents_);
        this.uContents_ = null;
        this.signalsConnected_ = false;
        this.connectSignals();
      }
    }
  }

  void setParentMenu(WMenu menu) {
    this.menu_ = menu;
    this.updateInternalPath();
    if (menu != null && menu.isPopup() && this.subMenu_ != null && this.subMenu_.isPopup()) {
      this.subMenu_
          .getWebWidget()
          .setZIndex(Math.max(menu.getZIndex() + 1000, this.subMenu_.getZIndex()));
    }
  }

  private void selectNotLoaded() {
    if (!this.isContentsLoaded()) {
      this.select();
    }
  }

  private void selectVisual() {
    if (this.menu_ != null && this.selectable_) {
      this.menu_.selectVisual(this);
    }
  }

  private void undoSelectVisual() {
    if (this.menu_ != null && this.selectable_) {
      this.menu_.undoSelectVisual();
    }
  }
  // private void connectClose() ;
  private void connectSignals() {
    if (!this.signalsConnected_) {
      this.signalsConnected_ = true;
      if (!(this.oContents_ != null) || this.isContentsLoaded()) {
        // this.implementStateless(WMenuItem.selectVisual,WMenuItem.undoSelectVisual);
      }
      WAnchor a = this.getAnchor();
      if (a != null) {
        AbstractSignal as;
        boolean selectFromCheckbox = false;
        if (this.checkBox_ != null && !this.checkBox_.clicked().isPropagationPrevented()) {
          as = this.checkBox_.changed();
          this.checkBox_
              .checked()
              .addListener(
                  this,
                  () -> {
                    WMenuItem.this.setCheckBox();
                  });
          this.checkBox_
              .unChecked()
              .addListener(
                  this,
                  () -> {
                    WMenuItem.this.setUnCheckBox();
                  });
          selectFromCheckbox = true;
        } else {
          as = a.clicked();
        }
        if (this.checkBox_ != null) {
          a.setLink(new WLink());
        }
        if (this.uContents_ != null) {
          as.addListener(
              this,
              () -> {
                WMenuItem.this.selectNotLoaded();
              });
        } else {
          as.addListener(
              this,
              () -> {
                WMenuItem.this.selectVisual();
              });
          if (!selectFromCheckbox) {
            as.addListener(
                this,
                () -> {
                  WMenuItem.this.select();
                });
          }
        }
      }
    }
  }

  void setItemPadding(boolean padding) {
    if (!(this.checkBox_ != null) && !(this.icon_ != null)) {
      WAnchor a = this.getAnchor();
      if (a != null) {
        a.toggleStyleClass("Wt-padded", padding);
      }
    }
  }
  // private void contentsDestroyed() ;
  private void setCheckBox() {
    this.setChecked(true);
    this.select();
  }

  private void setUnCheckBox() {
    this.setChecked(false);
    this.select();
  }

  WWidget takeContentsForStack() {
    if (!(this.uContents_ != null)) {
      return null;
    } else {
      if (this.loadPolicy_ == ContentLoading.Lazy) {
        WWidget result = this.uContentsContainer_;
        this.uContentsContainer_ = null;
        return result;
      } else {
        final WWidget result = this.uContents_;
        this.uContents_ = null;
        return result;
      }
    }
  }

  WWidget getContentsInStack() {
    if (this.oContentsContainer_ != null && !(this.uContentsContainer_ != null)) {
      return this.oContentsContainer_;
    } else {
      if (this.oContents_ != null && !(this.uContents_ != null)) {
        return this.oContents_;
      } else {
        return null;
      }
    }
  }

  void returnContentsInStack(WWidget widget) {
    if (this.oContentsContainer_ != null) {
      if (!(this.uContents_ != null)) {
        this.uContents_ = this.oContentsContainer_.removeWidget(this.oContents_);
      }
      this.oContentsContainer_ = (WContainerWidget) null;
    } else {
      this.uContents_ = widget;
    }
  }
}
