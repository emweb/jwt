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
 * A widget that shows a menu of options.
 *
 * <p>The WMenu widget offers menu navigation.
 *
 * <p>Typically, a menu is used in conjunction with a {@link WStackedWidget} (but can be used
 * without too), where different &apos;contents&apos; are stacked upon each other. Each choice in
 * the menu (which is implemented as a {@link WMenuItem}) corresponds to a tab in the contents
 * stack. The contents stack may contain other items, and could be shared with other {@link WMenu}
 * instances.
 *
 * <p>When using nested menus, you can use the currentWidgetChanged() signal to react to the change
 * of widget selected while knowing what widget was selected as the {@link WMenu#itemSelected()
 * itemSelected()} signal from the sub-menu is only emited when the widget selected by the submenu
 * is changed.
 *
 * <p>When used without a contents stack, you can react to menu item selection using the {@link
 * WMenu#itemSelected() itemSelected()} signal, to implement some custom handling of item selection.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * // create the stack where the contents will be located
 * WStackedWidget contents = new WStackedWidget(contentsParent);
 *
 * // create a menu
 * WMenu menu = new WMenu(contents);
 *
 * // add four items using the default lazy loading policy.
 * menu.addItem("Introduction", new WText("intro"));
 * menu.addItem("Download", new WText("Not yet available"));
 * menu.addItem("Demo", new DemoWidget());
 * menu.addItem(new WMenuItem("Demo2", new DemoWidget()));
 *
 * // bind the function to call when a new item is selected
 * contents.currentWidgetChanged().connect((newSelection) . {
 * if (newSelection instanceof Wt.WText){
 * logger.info(new StringWriter().append("Text selected: ").append((WText)newSelection).text());
 * }
 * else if (newSelection instanceof DemoWidget){
 * logger.info(new StringWriter().append("Testing a demo");
 * }
 * }
 * );
 *
 * }</pre>
 *
 * <p>After construction, the first entry will be selected. At any time, it is possible to select a
 * particular item using {@link WMenu#select(WMenuItem item) select()}.
 *
 * <p>Each item of WMenu may be closeable (see {@link WMenuItem#setCloseable(boolean closeable)
 * setCloseable()}. Like selection, at any time, it is possible to close a particular item using
 * {@link WMenu#close(WMenuItem item) close()}. You can react to close of item by using the {@link
 * WMenu#itemClosed() itemClosed()} signal.
 *
 * <p>The WMenu implementation offers fine-grained control on how contents should be preloaded. By
 * default, all contents is lazy-loaded, only when needed. To improve response time, an item may
 * also be preloaded (using {@link WMenu#addItem(CharSequence name, WWidget contents, ContentLoading
 * policy) addItem()}). In that case, the item will be loaded in the background, before its first
 * use. In any case, once the contents corresponding to a menu item is loaded, subsequent navigation
 * to it is handled entirely client-side.
 *
 * <p>The WMenu may participate in the application&apos;s internal path, which lets menu items
 * correspond to internal URLs, see {@link WMenu#setInternalPathEnabled(String basePath)
 * setInternalPathEnabled()}.
 *
 * <p>The look of the items may be defined through style sheets. The default {@link WMenuItem}
 * implementation uses four style classes to distinguish between inactivated, activated, closeable
 * inactivated and closeable activated menu items: <code>&quot;item&quot;</code>, <code>
 * &quot;itemselected&quot;</code>, <code>&quot;citem&quot;</code>, <code>&quot;citemselected&quot;
 * </code>. By using CSS nested selectors, a different style may be defined for items in a different
 * menu.
 *
 * <p>You may customize the rendering and behaviour of menu entries by specializing {@link
 * WMenuItem}.
 *
 * <p>
 *
 * <h3>CSS</h3>
 *
 * <p>The menu is rendered as a &lt;ul&gt;. Unless you use the bootstrap theme, you will want to
 * customize the menu using inline or external styles to hide the bullets and provide the
 * appropriate horizontal or vertical layout.
 *
 * <p>
 *
 * @see WMenuItem
 */
public class WMenu extends WCompositeWidget {
  private static Logger logger = LoggerFactory.getLogger(WMenu.class);

  /**
   * Creates a new menu.
   *
   * <p>The menu is not associated with a contents stack, and thus you will want to react to the
   * {@link WMenu#itemSelected() itemSelected()} signal to react to menu changes.
   */
  public WMenu(WContainerWidget parentContainer) {
    super();
    this.ul_ = null;
    this.contentsStack_ = null;
    this.internalPathEnabled_ = false;
    this.emitPathChange_ = false;
    this.basePath_ = "";
    this.previousInternalPath_ = "";
    this.parentItem_ = null;
    this.itemSelected_ = new Signal1<WMenuItem>();
    this.itemSelectRendered_ = new Signal1<WMenuItem>();
    this.itemClosed_ = new Signal1<WMenuItem>();
    this.current_ = -1;
    this.previousStackIndex_ = -1;
    this.needSelectionEventUpdate_ = false;
    this.setImplementation(this.ul_ = new WContainerWidget());
    this.ul_.setList(true);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a new menu.
   *
   * <p>Calls {@link #WMenu(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WMenu() {
    this((WContainerWidget) null);
  }
  /**
   * Creates a new menu.
   *
   * <p>Construct a menu to manage the widgets in <code>contentsStack</code>.
   *
   * <p>Each menu item will manage a single widget in the <code>contentsStack</code>, making it the
   * current widget when the menu item is activated.
   */
  public WMenu(WStackedWidget contentsStack, WContainerWidget parentContainer) {
    super();
    this.ul_ = null;
    this.contentsStack_ = contentsStack;
    this.internalPathEnabled_ = false;
    this.emitPathChange_ = false;
    this.basePath_ = "";
    this.previousInternalPath_ = "";
    this.parentItem_ = null;
    this.itemSelected_ = new Signal1<WMenuItem>();
    this.itemSelectRendered_ = new Signal1<WMenuItem>();
    this.itemClosed_ = new Signal1<WMenuItem>();
    this.current_ = -1;
    this.previousStackIndex_ = -1;
    this.needSelectionEventUpdate_ = false;
    if (this.contentsStack_ != null) {
      this.contentsStack_
          .childrenChanged()
          .addListener(
              this,
              () -> {
                WMenu.this.updateSelectionEvent();
              });
    }
    this.setImplementation(this.ul_ = new WContainerWidget());
    this.ul_.setList(true);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a new menu.
   *
   * <p>Calls {@link #WMenu(WStackedWidget contentsStack, WContainerWidget parentContainer)
   * this(contentsStack, (WContainerWidget)null)}
   */
  public WMenu(WStackedWidget contentsStack) {
    this(contentsStack, (WContainerWidget) null);
  }
  /** Destructor. */
  public void remove() {
    super.remove();
  }
  /**
   * Adds an item.
   *
   * <p>Use this version of {@link WMenu#addItem(CharSequence name, WWidget contents, ContentLoading
   * policy) addItem()} if you do not want to specify an icon for this menu item.
   *
   * <p>Returns the corresponding {@link WMenuItem}.
   *
   * <p>
   */
  public WMenuItem addItem(final CharSequence name, WWidget contents, ContentLoading policy) {
    return this.addItem("", name, contents, policy);
  }
  /**
   * Adds an item.
   *
   * <p>Returns {@link #addItem(CharSequence name, WWidget contents, ContentLoading policy)
   * addItem(name, null, ContentLoading.Lazy)}
   */
  public final WMenuItem addItem(final CharSequence name) {
    return addItem(name, null, ContentLoading.Lazy);
  }
  /**
   * Adds an item.
   *
   * <p>Returns {@link #addItem(CharSequence name, WWidget contents, ContentLoading policy)
   * addItem(name, contents, ContentLoading.Lazy)}
   */
  public final WMenuItem addItem(final CharSequence name, WWidget contents) {
    return addItem(name, contents, ContentLoading.Lazy);
  }
  /**
   * Adds an item.
   *
   * <p>Adds a menu item with given <code>contents</code>, which is added to the menu&apos;s
   * associated contents stack.
   *
   * <p><code>contents</code> may be <code>null</code> for two reasons:
   *
   * <ul>
   *   <li>if the menu is not associated with a contents stack, then you cannot associate a menu
   *       item with a contents widget
   *   <li>or, you may have one or more items which which are not associated with a contents widget
   *       in the contents stack.
   * </ul>
   *
   * <p>Returns the corresponding {@link WMenuItem}.
   *
   * <p>
   */
  public WMenuItem addItem(
      final String iconPath, final CharSequence name, WWidget contents, ContentLoading policy) {
    return this.addItem(new WMenuItem(iconPath, name, contents, policy));
  }
  /**
   * Adds an item.
   *
   * <p>Returns {@link #addItem(String iconPath, CharSequence name, WWidget contents, ContentLoading
   * policy) addItem(iconPath, name, null, ContentLoading.Lazy)}
   */
  public final WMenuItem addItem(final String iconPath, final CharSequence name) {
    return addItem(iconPath, name, null, ContentLoading.Lazy);
  }
  /**
   * Adds an item.
   *
   * <p>Returns {@link #addItem(String iconPath, CharSequence name, WWidget contents, ContentLoading
   * policy) addItem(iconPath, name, contents, ContentLoading.Lazy)}
   */
  public final WMenuItem addItem(final String iconPath, final CharSequence name, WWidget contents) {
    return addItem(iconPath, name, contents, ContentLoading.Lazy);
  }
  // public WMenuItem  addItem(final CharSequence text, T  target, <pointertomember or
  // dependentsizedarray> methodpointertomember or dependentsizedarray>) ;
  // public WMenuItem  addItem(final String iconPath, final CharSequence text, T  target,
  // <pointertomember or dependentsizedarray> methodpointertomember or dependentsizedarray>) ;
  /**
   * Adds a submenu, with given text.
   *
   * <p>Adds an item with text <code>text</code>, that leads to a submenu <code>menu</code>.
   *
   * <p>
   */
  public WMenuItem addMenu(final CharSequence text, WMenu menu) {
    return this.addMenu("", text, menu);
  }
  /**
   * Adds a submenu, with given icon and text.
   *
   * <p>Adds an item with given text and icon, that leads to a submenu <code>menu</code>.
   *
   * <p>
   */
  public WMenuItem addMenu(final String iconPath, final CharSequence text, WMenu menu) {
    WMenuItem item = this.addItem(iconPath, text, (WWidget) null, ContentLoading.Lazy);
    item.setMenu(menu);
    return item;
  }
  /**
   * Adds an item.
   *
   * <p>Adds a menu item. Use this form to add specialized {@link WMenuItem} implementations.
   *
   * <p>
   */
  public WMenuItem addItem(WMenuItem item) {
    return this.insertItem(this.getUl().getCount(), item);
  }
  /**
   * inserts an item.
   *
   * <p>Use this version of {@link WMenu#insertItem(int index, CharSequence name, WWidget contents,
   * ContentLoading policy) insertItem()} if you do not want to specify an icon for this menu item.
   *
   * <p>Returns the corresponding {@link WMenuItem}.
   *
   * <p>
   */
  public WMenuItem insertItem(
      int index, final CharSequence name, WWidget contents, ContentLoading policy) {
    return this.insertItem(index, "", name, contents, policy);
  }
  /**
   * inserts an item.
   *
   * <p>Returns {@link #insertItem(int index, CharSequence name, WWidget contents, ContentLoading
   * policy) insertItem(index, name, null, ContentLoading.Lazy)}
   */
  public final WMenuItem insertItem(int index, final CharSequence name) {
    return insertItem(index, name, null, ContentLoading.Lazy);
  }
  /**
   * inserts an item.
   *
   * <p>Returns {@link #insertItem(int index, CharSequence name, WWidget contents, ContentLoading
   * policy) insertItem(index, name, contents, ContentLoading.Lazy)}
   */
  public final WMenuItem insertItem(int index, final CharSequence name, WWidget contents) {
    return insertItem(index, name, contents, ContentLoading.Lazy);
  }
  /**
   * inserts an item.
   *
   * <p>inserts a menu item with given <code>contents</code>, which is inserted to the menu&apos;s
   * associated contents stack.
   *
   * <p><code>contents</code> may be <code>null</code> for two reasons:
   *
   * <ul>
   *   <li>if the menu is not associated with a contents stack, then you cannot associate a menu
   *       item with a contents widget
   *   <li>or, you may have one or more items which which are not associated with a contents widget
   *       in the contents stack.
   * </ul>
   *
   * <p>Returns the corresponding {@link WMenuItem}.
   *
   * <p>
   */
  public WMenuItem insertItem(
      int index,
      final String iconPath,
      final CharSequence name,
      WWidget contents,
      ContentLoading policy) {
    return this.insertItem(index, new WMenuItem(iconPath, name, contents, policy));
  }
  /**
   * inserts an item.
   *
   * <p>Returns {@link #insertItem(int index, String iconPath, CharSequence name, WWidget contents,
   * ContentLoading policy) insertItem(index, iconPath, name, null, ContentLoading.Lazy)}
   */
  public final WMenuItem insertItem(int index, final String iconPath, final CharSequence name) {
    return insertItem(index, iconPath, name, null, ContentLoading.Lazy);
  }
  /**
   * inserts an item.
   *
   * <p>Returns {@link #insertItem(int index, String iconPath, CharSequence name, WWidget contents,
   * ContentLoading policy) insertItem(index, iconPath, name, contents, ContentLoading.Lazy)}
   */
  public final WMenuItem insertItem(
      int index, final String iconPath, final CharSequence name, WWidget contents) {
    return insertItem(index, iconPath, name, contents, ContentLoading.Lazy);
  }
  // public WMenuItem  insertItem(int index, final CharSequence text, T  target, <pointertomember or
  // dependentsizedarray> methodpointertomember or dependentsizedarray>) ;
  // public WMenuItem  insertItem(int index, final String iconPath, final CharSequence text, T
  // target, <pointertomember or dependentsizedarray> methodpointertomember or dependentsizedarray>)
  // ;
  /**
   * inserts a submenu, with given text.
   *
   * <p>inserts an item with text <code>text</code>, that leads to a submenu <code>menu</code>.
   *
   * <p>
   */
  public WMenuItem insertMenu(int index, final CharSequence text, WMenu menu) {
    return this.insertMenu(index, "", text, menu);
  }
  /**
   * inserts a submenu, with given icon and text.
   *
   * <p>inserts an item with given text and icon, that leads to a submenu <code>menu</code>.
   *
   * <p>
   */
  public WMenuItem insertMenu(
      int index, final String iconPath, final CharSequence text, WMenu menu) {
    WMenuItem item = this.insertItem(index, iconPath, text, (WWidget) null);
    item.setMenu(menu);
    return item;
  }
  /**
   * Inserts an item.
   *
   * <p>Inserts a menu item. Use this form to insert specialized {@link WMenuItem} implementations.
   *
   * <p>
   */
  public WMenuItem insertItem(int index, WMenuItem item) {
    item.setParentMenu(this);
    WMenuItem result = item;
    this.getUl().insertWidget(index, item);
    if (this.contentsStack_ != null) {
      WWidget contentsPtr = result.takeContentsForStack();
      if (contentsPtr != null) {
        WWidget contents = contentsPtr;
        this.contentsStack_.addWidget(contentsPtr);
        this.contentsStack_.setLoadPolicy(this.contentsStack_.getCount() - 1, result.loadPolicy_);
        if (this.contentsStack_.getCount() == 1) {
          this.setCurrent(0);
          if (this.isLoaded()) {
            this.getCurrentItem().loadContents();
          }
          this.contentsStack_.setCurrentWidget(contents);
          this.renderSelected(result, true);
        } else {
          this.renderSelected(result, false);
        }
      } else {
        this.renderSelected(result, false);
      }
    } else {
      this.renderSelected(result, false);
    }
    this.itemPathChanged(result);
    return result;
  }
  /**
   * Adds a separator to the menu.
   *
   * <p>Adds a separator the menu.
   */
  public WMenuItem addSeparator() {
    return this.addItem(new WMenuItem(true, WString.Empty));
  }
  /** Adds a section header to the menu. */
  public WMenuItem addSectionHeader(final CharSequence text) {
    return this.addItem(new WMenuItem(false, text));
  }
  /**
   * Removes an item.
   *
   * <p>Removes the given item.
   *
   * <p>
   *
   * @see WMenu#addItem(CharSequence name, WWidget contents, ContentLoading policy)
   */
  public WMenuItem removeItem(WMenuItem item) {
    WMenuItem result = null;
    WContainerWidget items = this.getUl();
    if (item.getParent() == items) {
      int itemIndex = items.getIndexOf(item);
      result = WidgetUtils.remove(items, item);
      if (this.contentsStack_ != null && item.getContentsInStack() != null) {
        item.returnContentsInStack(this.contentsStack_.removeWidget(item.getContentsInStack()));
      }
      item.setParentMenu((WMenu) null);
      if (itemIndex <= this.current_ && this.current_ >= 0) {
        --this.current_;
      }
      this.select(this.current_, true);
    }
    return result;
  }
  /**
   * Moves an item.
   *
   * <p>Moves the item at the index <code>fromIndex</code> to the index <code>toIndex</code>.
   */
  public void moveItem(int fromIndex, int toIndex) {
    this.moveItem(this.itemAt(fromIndex), toIndex);
  }
  /**
   * Moves an item.
   *
   * <p>Moves the item at the <code>item</code> to the index <code>toIndex</code>.
   */
  public void moveItem(WMenuItem item, int toIndex) {
    if (item != null) {
      boolean needReload = item.loadPolicy_ == ContentLoading.Lazy && item.isContentsLoaded();
      WMenuItem realItem = this.removeItem(item);
      if (realItem != null) {
        this.insertItem(toIndex, realItem);
        if (needReload) {
          item.loadContents();
        }
      }
    }
  }
  /**
   * Selects an item.
   *
   * <p>Select the menu item <code>item</code>.
   *
   * <p>When <code>item</code> is <code>null</code>, the current selection is removed.
   *
   * <p>
   *
   * @see WMenu#select(int index)
   * @see WMenu#getCurrentItem()
   * @see WMenuItem#select()
   */
  public void select(WMenuItem item) {
    this.select(this.indexOf(item), true);
  }
  /**
   * Selects an item.
   *
   * <p>Menu items in a menu with <code>N</code> items are numbered from 0 to <code>N</code> - 1.
   *
   * <p>Using a value of -1 removes the current selection.
   *
   * <p>
   *
   * @see WMenu#select(WMenuItem item)
   * @see WMenu#getCurrentIndex()
   */
  public void select(int index) {
    this.select(index, true);
  }
  /**
   * Signal which indicates that a new item was selected.
   *
   * <p>This signal is emitted when an item was selected. It is emitted both when the user activated
   * an item, or when {@link WMenu#select(WMenuItem item) select()} was invoked.
   *
   * <p>
   *
   * @see WMenu#itemSelectRendered()
   */
  public Signal1<WMenuItem> itemSelected() {
    return this.itemSelected_;
  }
  /**
   * Signal which indicates that a new selected item is rendered.
   *
   * <p>This signal is similar to {@link WMenu#itemSelected() itemSelected()}, but is emitted from
   * within a stateless slot. Therefore, any slot connected to this signal will be optimized to
   * client-side JavaScript, and must support the contract of a stateless slot (i.e., be
   * idempotent).
   *
   * <p>If you are unsure what is the difference with the {@link WMenu#itemSelected()
   * itemSelected()} signal, you&apos;ll probably need the latter instead.
   *
   * <p>
   *
   * @see WMenu#itemSelected()
   */
  public Signal1<WMenuItem> itemSelectRendered() {
    return this.itemSelectRendered_;
  }
  /**
   * Closes an item.
   *
   * <p>Close the menu item <code>item</code>. Only {@link WMenuItem#setCloseable(boolean closeable)
   * closeable} items can be closed.
   *
   * <p>
   *
   * @see WMenu#close(int index)
   * @see WMenuItem#close()
   */
  public void close(WMenuItem item) {
    if (item.isCloseable()) {
      item.hide();
      this.itemClosed_.trigger(item);
    }
  }
  /**
   * Closes an item.
   *
   * <p>Menu items in a menu with <code>N</code> items are numbered from 0 to <code>N</code> - 1.
   *
   * <p>
   *
   * @see WMenu#close(WMenuItem item)
   */
  public void close(int index) {
    this.close(this.itemAt(index));
  }
  /**
   * Returns the items.
   *
   * <p>Returns the list of menu items in this menu.
   *
   * <p>
   *
   * @see WMenu#itemAt(int index)
   */
  public List<WMenuItem> getItems() {
    List<WMenuItem> result = new ArrayList<WMenuItem>();
    ;

    for (int i = 0; i < this.getCount(); ++i) {
      result.add(this.itemAt(i));
    }
    return result;
  }
  /**
   * Signal which indicates that an item was closed.
   *
   * <p>This signal is emitted when an item was closed. It is emitted both when the user closes an
   * item, or when {@link WMenu#close(WMenuItem item) close()} was invoked.
   */
  public Signal1<WMenuItem> itemClosed() {
    return this.itemClosed_;
  }
  /**
   * Hides an item.
   *
   * <p>Hides the menu item <code>item</code>. By default, all menu items are visible.
   *
   * <p>If the item was currently selected, then the next item to be selected is determined by
   * {@link WMenu#nextAfterHide(int index) nextAfterHide()}.
   *
   * <p>
   *
   * @see WMenu#setItemHidden(int index, boolean hidden)
   * @see WWidget#hide()
   */
  public void setItemHidden(WMenuItem item, boolean hidden) {
    item.setHidden(hidden);
  }
  /**
   * Hides an item.
   *
   * <p>Menu items in a menu with <code>N</code> items are numbered from 0 to <code>N</code> - 1.
   *
   * <p>
   *
   * @see WMenu#setItemHidden(WMenuItem item, boolean hidden)
   */
  public void setItemHidden(int index, boolean hidden) {
    this.itemAt(index).setHidden(hidden);
  }
  /**
   * Returns whether the item widget of the given item is hidden.
   *
   * <p>
   *
   * @see WMenu#setItemHidden(WMenuItem item, boolean hidden)
   */
  public boolean isItemHidden(WMenuItem item) {
    return item.isHidden();
  }
  /**
   * Returns whether the item widget of the given index is hidden.
   *
   * <p>Menu items in a menu with <code>N</code> items are numbered from 0 to <code>N</code> - 1.
   *
   * <p>
   *
   * @see WMenu#setItemHidden(WMenuItem item, boolean hidden)
   */
  public boolean isItemHidden(int index) {
    return this.isItemHidden(this.itemAt(index));
  }
  /**
   * Disables an item.
   *
   * <p>Disables the menu item <code>item</code>. Only an item that is enabled can be selected. By
   * default, all menu items are enabled.
   *
   * <p>
   *
   * @see WMenu#setItemDisabled(int index, boolean disabled)
   * @see WMenuItem#setDisabled(boolean disabled)
   */
  public void setItemDisabled(WMenuItem item, boolean disabled) {
    item.setDisabled(disabled);
  }
  /**
   * Disables an item.
   *
   * <p>Menu items in a menu with <code>N</code> items are numbered from 0 to <code>N</code> - 1.
   *
   * <p>
   *
   * @see WMenu#setItemDisabled(WMenuItem item, boolean disabled)
   */
  public void setItemDisabled(int index, boolean disabled) {
    this.setItemDisabled(this.itemAt(index), disabled);
  }
  /**
   * Returns whether the item widget of the given item is disabled.
   *
   * <p>
   *
   * @see WMenu#setItemDisabled(WMenuItem item, boolean disabled)
   */
  public boolean isItemDisabled(WMenuItem item) {
    return item.isDisabled();
  }
  /**
   * Returns whether the item widget of the given index is disabled.
   *
   * <p>Menu items in a menu with <code>N</code> items are numbered from 0 to <code>N</code> - 1.
   *
   * <p>
   *
   * @see WMenu#setItemDisabled(WMenuItem item, boolean disabled)
   */
  public boolean isItemDisabled(int index) {
    return this.isItemDisabled(this.itemAt(index));
  }
  /**
   * Returns the currently selected item.
   *
   * <p>
   *
   * @see WMenu#getCurrentIndex()
   * @see WMenu#select(WMenuItem item)
   */
  public WMenuItem getCurrentItem() {
    return this.current_ >= 0 ? this.itemAt(this.current_) : null;
  }
  /**
   * Returns the index of the currently selected item.
   *
   * <p>
   *
   * @see WMenu#getCurrentItem()
   * @see WMenu#select(int index)
   */
  public int getCurrentIndex() {
    return this.current_;
  }
  /**
   * Enables internal paths for items.
   *
   * <p>The menu participates in the internal path by changing the internal path when an item has
   * been selected, and listening for path changes to react to path selections. As a consequence
   * this allows the user to bookmark the current menu selection and revisit it later, use
   * back/forward buttons to navigate through history of visited menu items, and allows indexing of
   * pages.
   *
   * <p>For each menu item, {@link WMenuItem#getPathComponent() getPathComponent()} is appended to
   * the <code>basePath</code>, which defaults to the internal path ({@link
   * WApplication#getInternalPath()}). A &apos;/&apos; is appended to the base path, to turn it into
   * a folder, if needed.
   *
   * <p>By default, menu interaction does not change the application internal path.
   *
   * <p>
   *
   * @see WMenuItem#setPathComponent(String path)
   */
  public void setInternalPathEnabled(final String basePath) {
    WApplication app = WApplication.getInstance();
    this.basePath_ = basePath.length() == 0 ? app.getInternalPath() : basePath;
    this.basePath_ = StringUtils.append(StringUtils.prepend(this.basePath_, '/'), '/');
    if (!this.internalPathEnabled_) {
      this.internalPathEnabled_ = true;
      app.internalPathChanged()
          .addListener(
              this,
              (String e1) -> {
                WMenu.this.handleInternalPathChange(e1);
              });
    }
    this.previousInternalPath_ = app.getInternalPath();
    this.internalPathChanged(app.getInternalPath());
    this.updateItemsInternalPath();
  }
  /**
   * Enables internal paths for items.
   *
   * <p>Calls {@link #setInternalPathEnabled(String basePath) setInternalPathEnabled("")}
   */
  public final void setInternalPathEnabled() {
    setInternalPathEnabled("");
  }
  /**
   * Returns whether the menu generates internal paths entries.
   *
   * <p>
   *
   * @see WMenu#setInternalPathEnabled(String basePath)
   */
  public boolean isInternalPathEnabled() {
    return this.internalPathEnabled_;
  }
  /**
   * Sets the internal base path.
   *
   * <p>A &apos;/&apos; is appended to turn it into a folder, if needed.
   *
   * <p>
   *
   * @see WMenu#setInternalPathEnabled(String basePath)
   * @see WMenu#getInternalBasePath()
   */
  public void setInternalBasePath(final String basePath) {
    this.setInternalPathEnabled(basePath);
  }
  /**
   * Returns the internal base path.
   *
   * <p>The default value is the application&apos;s internalPath ({@link
   * WApplication#getInternalPath()}) that was recorded when {@link
   * WMenu#setInternalPathEnabled(String basePath) setInternalPathEnabled()} was called, and
   * together with each {@link WMenuItem#getPathComponent() getPathComponent()} determines the paths
   * for each item.
   *
   * <p>For example, if {@link WMenu#getInternalBasePath() getInternalBasePath()} is <code>
   * &quot;/examples/&quot;</code> and pathComponent() for a particular item is <code>
   * &quot;charts/&quot;</code>, then the internal path for that item will be <code>
   * &quot;/examples/charts/&quot;</code>.
   *
   * <p>
   *
   * @see WMenu#setInternalPathEnabled(String basePath)
   */
  public String getInternalBasePath() {
    return this.basePath_;
  }
  /** Returns the contents stack associated with the menu. */
  public WStackedWidget getContentsStack() {
    return this.contentsStack_;
  }
  /** Returns the item count. */
  public int getCount() {
    return this.getUl().getCount();
  }
  /**
   * Returns the item by index.
   *
   * <p>
   *
   * @see WMenu#indexOf(WMenuItem item)
   */
  public WMenuItem itemAt(int index) {
    return ObjectUtils.cast(this.getUl().getWidget(index), WMenuItem.class);
  }
  /**
   * Returns the index of an item.
   *
   * <p>
   *
   * @see WMenu#itemAt(int index)
   */
  public int indexOf(WMenuItem item) {
    return this.getUl().getIndexOf(item);
  }
  /**
   * Returns the parent item (for a submenu)
   *
   * <p>This is the item with which this menu is associated as a submenu (if any).
   */
  public WMenuItem getParentItem() {
    return this.parentItem_;
  }

  public void load() {
    boolean wasLoaded = this.isLoaded();
    super.load();
    if (wasLoaded) {
      return;
    }
    if (this.getCurrentItem() != null) {
      this.getCurrentItem().loadContents();
    }
  }

  protected void render(EnumSet<RenderFlag> flags) {
    if (this.needSelectionEventUpdate_) {
      for (int i = 0; i < this.getCount(); ++i) {
        this.itemAt(i).resetLearnedSlots();
      }
      this.needSelectionEventUpdate_ = false;
    }
    super.render(flags);
  }
  /**
   * Handling of internal path changes.
   *
   * <p>This methods makes the menu react to internal path changes (and also the initial internal
   * path).
   *
   * <p>You may want to reimplement this if you want to customize the internal path handling.
   */
  void internalPathChanged(final String path) {
    WApplication app = WApplication.getInstance();
    if (app.internalPathMatches(this.basePath_)) {
      String subPath = app.internalSubPath(this.basePath_);
      int bestI = -1;
      int bestMatchLength = -1;
      for (int i = 0; i < this.getCount(); ++i) {
        if (!this.itemAt(i).isEnabled() || this.itemAt(i).isHidden()) {
          continue;
        }
        int matchLength = match(subPath, this.itemAt(i).getPathComponent());
        if (matchLength > bestMatchLength) {
          bestMatchLength = matchLength;
          bestI = i;
        }
      }
      if (bestI != -1) {
        this.itemAt(bestI).setFromInternalPath(path);
      } else {
        if (subPath.length() != 0) {
          logger.warn(
              new StringWriter().append("unknown path: '").append(subPath).append("'").toString());
        } else {
          this.select(-1, false);
        }
      }
    }
  }
  /**
   * Returns the index of the item to be selected after hides.
   *
   * <p>Returns the index of the item to be selected after the item with given index will be hidden.
   *
   * <p>By default, if the given index is an index of currently selected item, returns an index of
   * the first visible item to the right of it. If it is not found, returns the index of the first
   * visible item to the left of it. If there are no visible items around the currently selected
   * item, returns the index of currently selected item.
   *
   * <p>You may want to reimplement this if you want to customize the algorithm of determining the
   * index of the item to be selected after hiding the item with given index.
   */
  protected int nextAfterHide(int index) {
    if (this.current_ == index) {
      for (int i = this.current_ + 1; i < this.getCount(); ++i) {
        if (!this.isItemHidden(i) && this.itemAt(i).isEnabled()) {
          return i;
        }
      }
      for (int i = this.current_ - 1; i >= 0; --i) {
        if (!this.isItemHidden(i) && this.itemAt(i).isEnabled()) {
          return i;
        }
      }
    }
    return this.current_;
  }

  protected WContainerWidget getUl() {
    return this.ul_;
  }

  protected void renderSelected(WMenuItem item, boolean selected) {
    item.renderSelected(selected);
  }

  protected void setCurrent(int index) {
    this.current_ = index;
  }
  /**
   * Selects an item.
   *
   * <p>This is the internal function that implements the selection logic, including optional
   * internal path change (if <code>changePath</code> is <code>true</code>). The latter may be
   * <code>false</code> in case an internal path change itself is the reason for selection.
   */
  void select(int index, boolean changePath) {
    if (this.parentItem_ != null) {
      WMenu parentItemMenu = this.parentItem_.getParentMenu();
      if (parentItemMenu.getCurrentItem() != this.parentItem_ && this.parentItem_.isSelectable()) {
        parentItemMenu.select(parentItemMenu.indexOf(this.parentItem_), false);
      }
    }
    int last = this.current_;
    this.setCurrent(index);
    this.selectVisual(this.current_, changePath, true);
    if (index != -1) {
      WMenuItem item = this.itemAt(index);
      item.show();
      if (this.isLoaded()) {
        item.loadContents();
      }
      WMenu self = this;
      if (changePath && this.emitPathChange_) {
        WApplication app = WApplication.getInstance();
        app.internalPathChanged().trigger(app.getInternalPath());
        if (!(self != null)) {
          return;
        }
        this.emitPathChange_ = false;
      }
      if (last != index) {
        item.triggered().trigger(item);
        if (self != null) {
          if (this.getUl().getIndexOf(item) != -1) {
            this.itemSelected_.trigger(item);
          } else {
            this.select(-1);
          }
        }
      }
    }
  }

  private WContainerWidget ul_;
  WStackedWidget contentsStack_;
  private boolean internalPathEnabled_;
  private boolean emitPathChange_;
  private String basePath_;
  private String previousInternalPath_;
  WMenuItem parentItem_;
  private Signal1<WMenuItem> itemSelected_;
  private Signal1<WMenuItem> itemSelectRendered_;
  private Signal1<WMenuItem> itemClosed_;

  private void handleInternalPathChange(final String path) {
    if (!(this.parentItem_ != null) || !this.parentItem_.isInternalPathEnabled()) {
      this.internalPathChanged(path);
    }
  }

  private int current_;
  private int previousStackIndex_;
  private boolean needSelectionEventUpdate_;

  private void updateItemsInternalPath() {
    for (int i = 0; i < this.getCount(); ++i) {
      WMenuItem item = this.itemAt(i);
      item.updateInternalPath();
    }
    this.updateSelectionEvent();
  }

  void itemPathChanged(WMenuItem item) {
    if (this.internalPathEnabled_ && item.isInternalPathEnabled()) {
      WApplication app = WApplication.getInstance();
      if (app.internalPathMatches(this.basePath_ + item.getPathComponent())) {
        item.setFromInternalPath(app.getInternalPath());
      }
    }
  }

  void selectVisual(WMenuItem item) {
    this.selectVisual(this.indexOf(item), true, true);
  }

  void undoSelectVisual() {
    String prevPath = this.previousInternalPath_;
    int prevStackIndex = this.previousStackIndex_;
    this.selectVisual(this.current_, true, true);
    if (this.internalPathEnabled_) {
      WApplication app = WApplication.getInstance();
      app.setInternalPath(prevPath);
    }
    if (this.contentsStack_ != null) {
      this.contentsStack_.setCurrentIndex(prevStackIndex);
    }
  }

  void selectVisual(int index, boolean changePath, boolean showContents) {
    if (this.contentsStack_ != null) {
      this.previousStackIndex_ = this.contentsStack_.getCurrentIndex();
    }
    WMenuItem item = index >= 0 ? this.itemAt(index) : null;
    if (changePath && this.internalPathEnabled_ && index != -1 && item.isInternalPathEnabled()) {
      WApplication app = WApplication.getInstance();
      this.previousInternalPath_ = app.getInternalPath();
      String newPath = this.basePath_ + item.getPathComponent();
      if (!newPath.equals(app.getInternalPath())) {
        this.emitPathChange_ = true;
      }
      app.setInternalPath(newPath);
    }
    for (int i = 0; i < this.getCount(); ++i) {
      this.renderSelected(this.itemAt(i), (int) i == index);
    }
    if (index == -1) {
      return;
    }
    if (showContents && this.contentsStack_ != null) {
      WWidget contents = item.getContentsInStack();
      if (contents != null) {
        this.contentsStack_.setCurrentWidget(contents);
      }
    }
    this.itemSelectRendered_.trigger(item);
  }

  void onItemHidden(int index, boolean hidden) {
    if (hidden) {
      int nextItem = this.nextAfterHide(index);
      if (nextItem != this.current_) {
        this.select(nextItem);
      }
    }
  }

  void updateSelectionEvent() {
    this.needSelectionEventUpdate_ = true;
    this.scheduleRender();
  }

  static int match(final String path, final String component) {
    if (component.length() > path.length()) {
      return -1;
    }
    int length = Math.min(component.length(), path.length());
    int current = -1;
    for (int i = 0; i < length; ++i) {
      if (component.charAt(i) != path.charAt(i)) {
        return current;
      } else {
        if (component.charAt(i) == '/') {
          current = i;
        }
      }
    }
    return length;
  }
}
