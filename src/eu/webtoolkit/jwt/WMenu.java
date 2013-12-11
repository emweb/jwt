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
 * A widget that shows a menu of options.
 * <p>
 * 
 * The WMenu widget offers menu navigation.
 * <p>
 * Typically, a menu is used in conjunction with a {@link WStackedWidget} (but
 * can be used without too), where different &apos;contents&apos; are stacked
 * upon each other. Each choice in the menu (which is implemented as a
 * {@link WMenuItem}) corresponds to a tab in the contents stack. The contents
 * stack may contain other items, and could be shared with other {@link WMenu}
 * instances.
 * <p>
 * When used without a contents stack, you can react to menu item selection
 * using the {@link WMenu#itemSelected() itemSelected()} signal, to implement
 * some custom handling of item selection.
 * <p>
 * Usage example:
 * <p>
 * 
 * <pre>
 * {
 * 	&#064;code
 * 	// create the stack where the contents will be located
 * 	WStackedWidget contents = new WStackedWidget(contentsParent);
 * 
 * 	// create a menu
 * 	WMenu menu = new WMenu(contents, menuParent);
 * 
 * 	// add four items using the default lazy loading policy.
 * 	menu.addItem(&quot;Introduction&quot;, new WText(&quot;intro&quot;));
 * 	menu.addItem(&quot;Download&quot;, new WText(&quot;Not yet available&quot;));
 * 	menu.addItem(&quot;Demo&quot;, new DemoWidget());
 * 	menu.addItem(new WMenuItem(&quot;Demo2&quot;, new DemoWidget()));
 * }
 * </pre>
 * <p>
 * After contruction, the first entry will be selected. At any time, it is
 * possible to select a particular item using
 * {@link WMenu#select(WMenuItem item) select()}.
 * <p>
 * Each item of WMenu may be closeable (see
 * {@link WMenuItem#setCloseable(boolean closeable) WMenuItem#setCloseable()}.
 * Like selection, at any time, it is possible to close a particular item using
 * {@link WMenu#close(WMenuItem item) close()}. You can react to close of item
 * by using the {@link WMenu#itemClosed() itemClosed()} signal.
 * <p>
 * The WMenu implementation offers fine-grained control on how contents should
 * be preloaded. By default, all contents is lazy-loaded, only when needed. To
 * improve response time, an item may also be preloaded (using
 * {@link WMenu#addItem(CharSequence name, WWidget contents, WMenuItem.LoadPolicy policy)
 * addItem()}). In that case, the item will be loaded in the background, before
 * its first use. In any case, once the contents corresponding to a menu item is
 * loaded, subsequent navigation to it is handled entirely client-side.
 * <p>
 * The WMenu may participate in the application&apos;s internal path, which lets
 * menu items correspond to internal URLs, see
 * {@link WMenu#setInternalPathEnabled(String basePath)
 * setInternalPathEnabled()}.
 * <p>
 * The layout of the menu may be Horizontal or Vertical. The look of the items
 * may be defined through style sheets. The default {@link WMenuItem}
 * implementation uses four style classes to distinguish between inactivated,
 * activated, closeable inactivated and closeable activated menu items:
 * <code>&quot;item&quot;</code>, <code>&quot;itemselected&quot;</code>,
 * <code>&quot;citem&quot;</code>, <code>&quot;citemselected&quot;</code>. By
 * using CSS nested selectors, a different style may be defined for items in a
 * different menu.
 * <p>
 * You may customize the rendering and behaviour of menu entries by specializing
 * {@link WMenuItem}.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * The menu is rendered as a &gt;ul&lt;. Unless you use the bootstrap theme, you
 * will want to customize the menu using inline or external styles to hide the
 * bullets and provide the appropriate horizontal or vertical layout.
 * <p>
 * 
 * @see WMenuItem
 */
public class WMenu extends WCompositeWidget {
	private static Logger logger = LoggerFactory.getLogger(WMenu.class);

	/**
	 * Creates a new menu (<b>deprecated</b>).
	 * <p>
	 * Construct a menu with given <code>orientation</code>. The menu is not
	 * associated with a contents stack, and thus you will want to react to the
	 * {@link WMenu#itemSelected() itemSelected()} signal to react to menu
	 * changes.
	 * <p>
	 * 
	 * @deprecated the <code>orientation</code> parameter is ignored, since
	 *             menus are now always rendered using &lt;ul&gt; elements, and
	 *             CSS will determine the orientation. Use
	 *             {@link WMenu#WMenu(WContainerWidget parent) WMenu()} instead.
	 */
	public WMenu(Orientation orientation, WContainerWidget parent) {
		super(parent);
		this.contentsStack_ = null;
		this.basePath_ = "";
		this.previousInternalPath_ = "";
		this.itemSelected_ = new Signal1<WMenuItem>(this);
		this.itemSelectRendered_ = new Signal1<WMenuItem>(this);
		this.itemClosed_ = new Signal1<WMenuItem>(this);
		this.contentsStackConnection_ = new AbstractSignal.Connection();
		this.init();
	}

	/**
	 * Creates a new menu (<b>deprecated</b>).
	 * <p>
	 * Calls {@link #WMenu(Orientation orientation, WContainerWidget parent)
	 * this(orientation, (WContainerWidget)null)}
	 */
	public WMenu(Orientation orientation) {
		this(orientation, (WContainerWidget) null);
	}

	/**
	 * Creates a new menu.
	 * <p>
	 * The menu is not associated with a contents stack, and thus you will want
	 * to react to the {@link WMenu#itemSelected() itemSelected()} signal to
	 * react to menu changes.
	 */
	public WMenu(WContainerWidget parent) {
		super(parent);
		this.contentsStack_ = null;
		this.basePath_ = "";
		this.previousInternalPath_ = "";
		this.itemSelected_ = new Signal1<WMenuItem>(this);
		this.itemSelectRendered_ = new Signal1<WMenuItem>(this);
		this.itemClosed_ = new Signal1<WMenuItem>(this);
		this.contentsStackConnection_ = new AbstractSignal.Connection();
		this.init();
	}

	/**
	 * Creates a new menu.
	 * <p>
	 * Calls {@link #WMenu(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WMenu() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates a new menu (<b>deprecated</b>).
	 * <p>
	 * Construct a menu to manage the widgets in <code>contentsStack</code>, and
	 * sets the menu <code>orientation</code>.
	 * <p>
	 * Each menu item will manage a single widget in the
	 * <code>contentsStack</code>, making it the current widget when the menu
	 * item is activated.
	 * <p>
	 * 
	 * @deprecated the <code>orientation</code> parameter is ignored, since
	 *             menus are now always rendered using &lt;ul&gt; elements, and
	 *             CSS will determine the orientation. Use
	 *             {@link WMenu#WMenu(WStackedWidget contentsStack, WContainerWidget parent)
	 *             WMenu()} instead.
	 */
	public WMenu(WStackedWidget contentsStack, Orientation orientation,
			WContainerWidget parent) {
		super(parent);
		this.contentsStack_ = contentsStack;
		this.basePath_ = "";
		this.previousInternalPath_ = "";
		this.itemSelected_ = new Signal1<WMenuItem>(this);
		this.itemSelectRendered_ = new Signal1<WMenuItem>(this);
		this.itemClosed_ = new Signal1<WMenuItem>(this);
		this.contentsStackConnection_ = new AbstractSignal.Connection();
		this.init();
	}

	/**
	 * Creates a new menu (<b>deprecated</b>).
	 * <p>
	 * Calls
	 * {@link #WMenu(WStackedWidget contentsStack, Orientation orientation, WContainerWidget parent)
	 * this(contentsStack, orientation, (WContainerWidget)null)}
	 */
	public WMenu(WStackedWidget contentsStack, Orientation orientation) {
		this(contentsStack, orientation, (WContainerWidget) null);
	}

	/**
	 * Creates a new menu.
	 * <p>
	 * Construct a menu to manage the widgets in <code>contentsStack</code>.
	 * <p>
	 * Each menu item will manage a single widget in the
	 * <code>contentsStack</code>, making it the current widget when the menu
	 * item is activated.
	 */
	public WMenu(WStackedWidget contentsStack, WContainerWidget parent) {
		super(parent);
		this.contentsStack_ = contentsStack;
		this.basePath_ = "";
		this.previousInternalPath_ = "";
		this.itemSelected_ = new Signal1<WMenuItem>(this);
		this.itemSelectRendered_ = new Signal1<WMenuItem>(this);
		this.itemClosed_ = new Signal1<WMenuItem>(this);
		this.contentsStackConnection_ = new AbstractSignal.Connection();
		this.init();
	}

	/**
	 * Creates a new menu.
	 * <p>
	 * Calls {@link #WMenu(WStackedWidget contentsStack, WContainerWidget parent)
	 * this(contentsStack, (WContainerWidget)null)}
	 */
	public WMenu(WStackedWidget contentsStack) {
		this(contentsStack, (WContainerWidget) null);
	}

	/**
	 * Destructor.
	 */
	public void remove() {
		this.contentsStackConnection_.disconnect();
		super.remove();
	}

	/**
	 * Adds an item.
	 * <p>
	 * Use this version of
	 * {@link WMenu#addItem(CharSequence name, WWidget contents, WMenuItem.LoadPolicy policy)
	 * addItem()} if you do not want to specify an icon for this menu item.
	 * <p>
	 * Returns the corresponding {@link WMenuItem}.
	 * <p>
	 * 
	 * @see WMenu#addItem(String iconPath, CharSequence name, WWidget contents,
	 *      WMenuItem.LoadPolicy policy)
	 * @see WMenu#addItem(WMenuItem item)
	 */
	public WMenuItem addItem(final CharSequence name, WWidget contents,
			WMenuItem.LoadPolicy policy) {
		return this.addItem("", name, contents, policy);
	}

	/**
	 * Adds an item.
	 * <p>
	 * Returns
	 * {@link #addItem(CharSequence name, WWidget contents, WMenuItem.LoadPolicy policy)
	 * addItem(name, (WWidget)null, WMenuItem.LoadPolicy.LazyLoading)}
	 */
	public final WMenuItem addItem(final CharSequence name) {
		return addItem(name, (WWidget) null, WMenuItem.LoadPolicy.LazyLoading);
	}

	/**
	 * Adds an item.
	 * <p>
	 * Returns
	 * {@link #addItem(CharSequence name, WWidget contents, WMenuItem.LoadPolicy policy)
	 * addItem(name, contents, WMenuItem.LoadPolicy.LazyLoading)}
	 */
	public final WMenuItem addItem(final CharSequence name, WWidget contents) {
		return addItem(name, contents, WMenuItem.LoadPolicy.LazyLoading);
	}

	/**
	 * Adds an item.
	 * <p>
	 * Adds a menu item with given <code>contents</code>, which is added to the
	 * menu&apos;s associated contents stack.
	 * <p>
	 * <code>contents</code> may be <code>null</code> for two reasons:
	 * <ul>
	 * <li>if the menu is not associated with a contents stack, then you cannot
	 * associate a menu item with a contents widget</li>
	 * <li>or, you may have one or more items which which are not associated
	 * with a contents widget in the contents stack.</li>
	 * </ul>
	 * <p>
	 * Returns the corresponding {@link WMenuItem}.
	 * <p>
	 * 
	 * @see WMenu#addItem(WMenuItem item)
	 */
	public WMenuItem addItem(final String iconPath, final CharSequence name,
			WWidget contents, WMenuItem.LoadPolicy policy) {
		WMenuItem item = new WMenuItem(iconPath, name, contents, policy);
		this.addItem(item);
		return item;
	}

	/**
	 * Adds an item.
	 * <p>
	 * Returns
	 * {@link #addItem(String iconPath, CharSequence name, WWidget contents, WMenuItem.LoadPolicy policy)
	 * addItem(iconPath, name, (WWidget)null, WMenuItem.LoadPolicy.LazyLoading)}
	 */
	public final WMenuItem addItem(final String iconPath,
			final CharSequence name) {
		return addItem(iconPath, name, (WWidget) null,
				WMenuItem.LoadPolicy.LazyLoading);
	}

	/**
	 * Adds an item.
	 * <p>
	 * Returns
	 * {@link #addItem(String iconPath, CharSequence name, WWidget contents, WMenuItem.LoadPolicy policy)
	 * addItem(iconPath, name, contents, WMenuItem.LoadPolicy.LazyLoading)}
	 */
	public final WMenuItem addItem(final String iconPath,
			final CharSequence name, WWidget contents) {
		return addItem(iconPath, name, contents,
				WMenuItem.LoadPolicy.LazyLoading);
	}

	// public WMenuItem addItem(final CharSequence text, T target,
	// <pointertomember or dependentsizedarray> methodpointertomember or
	// dependentsizedarray>) ;
	// public WMenuItem addItem(final String iconPath, final CharSequence text,
	// T target, <pointertomember or dependentsizedarray> methodpointertomember
	// or dependentsizedarray>) ;
	/**
	 * Adds a submenu, with given text.
	 * <p>
	 * Adds an item with text <code>text</code>, that leads to a submenu
	 * <code>menu</code>.
	 * <p>
	 */
	public WMenuItem addMenu(final CharSequence text, WMenu menu) {
		return this.addMenu("", text, menu);
	}

	/**
	 * Adds a submenu, with given icon and text.
	 * <p>
	 * Adds an item with given text and icon, that leads to a submenu
	 * <code>menu</code>.
	 * <p>
	 */
	public WMenuItem addMenu(final String iconPath, final CharSequence text,
			WMenu menu) {
		WMenuItem item = new WMenuItem(iconPath, text, (WWidget) null,
				WMenuItem.LoadPolicy.LazyLoading);
		item.setMenu(menu);
		this.addItem(item);
		return item;
	}

	/**
	 * Adds an item.
	 * <p>
	 * Adds a menu item. Use this form to add specialized {@link WMenuItem}
	 * implementations.
	 * <p>
	 * 
	 * @see WMenu#addItem(CharSequence name, WWidget contents,
	 *      WMenuItem.LoadPolicy policy)
	 */
	public void addItem(WMenuItem item) {
		this.insertItem(this.getUl().getCount(), item);
	}

	/**
	 * inserts an item.
	 * <p>
	 * Use this version of
	 * {@link WMenu#insertItem(int index, CharSequence name, WWidget contents, WMenuItem.LoadPolicy policy)
	 * insertItem()} if you do not want to specify an icon for this menu item.
	 * <p>
	 * Returns the corresponding {@link WMenuItem}.
	 * <p>
	 * 
	 * @see WMenu#insertItem(int index, String iconPath, CharSequence name,
	 *      WWidget contents, WMenuItem.LoadPolicy policy)
	 * @see WMenu#insertItem(int index, WMenuItem item)
	 */
	public WMenuItem insertItem(int index, final CharSequence name,
			WWidget contents, WMenuItem.LoadPolicy policy) {
		return this.insertItem(index, "", name, contents, policy);
	}

	/**
	 * inserts an item.
	 * <p>
	 * Returns
	 * {@link #insertItem(int index, CharSequence name, WWidget contents, WMenuItem.LoadPolicy policy)
	 * insertItem(index, name, (WWidget)null, WMenuItem.LoadPolicy.LazyLoading)}
	 */
	public final WMenuItem insertItem(int index, final CharSequence name) {
		return insertItem(index, name, (WWidget) null,
				WMenuItem.LoadPolicy.LazyLoading);
	}

	/**
	 * inserts an item.
	 * <p>
	 * Returns
	 * {@link #insertItem(int index, CharSequence name, WWidget contents, WMenuItem.LoadPolicy policy)
	 * insertItem(index, name, contents, WMenuItem.LoadPolicy.LazyLoading)}
	 */
	public final WMenuItem insertItem(int index, final CharSequence name,
			WWidget contents) {
		return insertItem(index, name, contents,
				WMenuItem.LoadPolicy.LazyLoading);
	}

	/**
	 * inserts an item.
	 * <p>
	 * inserts a menu item with given <code>contents</code>, which is inserted
	 * to the menu&apos;s associated contents stack.
	 * <p>
	 * <code>contents</code> may be <code>null</code> for two reasons:
	 * <ul>
	 * <li>if the menu is not associated with a contents stack, then you cannot
	 * associate a menu item with a contents widget</li>
	 * <li>or, you may have one or more items which which are not associated
	 * with a contents widget in the contents stack.</li>
	 * </ul>
	 * <p>
	 * Returns the corresponding {@link WMenuItem}.
	 * <p>
	 * 
	 * @see WMenu#insertItem(int index, WMenuItem item)
	 */
	public WMenuItem insertItem(int index, final String iconPath,
			final CharSequence name, WWidget contents,
			WMenuItem.LoadPolicy policy) {
		WMenuItem item = new WMenuItem(iconPath, name, contents, policy);
		this.insertItem(index, item);
		return item;
	}

	/**
	 * inserts an item.
	 * <p>
	 * Returns
	 * {@link #insertItem(int index, String iconPath, CharSequence name, WWidget contents, WMenuItem.LoadPolicy policy)
	 * insertItem(index, iconPath, name, (WWidget)null,
	 * WMenuItem.LoadPolicy.LazyLoading)}
	 */
	public final WMenuItem insertItem(int index, final String iconPath,
			final CharSequence name) {
		return insertItem(index, iconPath, name, (WWidget) null,
				WMenuItem.LoadPolicy.LazyLoading);
	}

	/**
	 * inserts an item.
	 * <p>
	 * Returns
	 * {@link #insertItem(int index, String iconPath, CharSequence name, WWidget contents, WMenuItem.LoadPolicy policy)
	 * insertItem(index, iconPath, name, contents,
	 * WMenuItem.LoadPolicy.LazyLoading)}
	 */
	public final WMenuItem insertItem(int index, final String iconPath,
			final CharSequence name, WWidget contents) {
		return insertItem(index, iconPath, name, contents,
				WMenuItem.LoadPolicy.LazyLoading);
	}

	// public WMenuItem insertItem(int index, final CharSequence text, T target,
	// <pointertomember or dependentsizedarray> methodpointertomember or
	// dependentsizedarray>) ;
	// public WMenuItem insertItem(int index, final String iconPath, final
	// CharSequence text, T target, <pointertomember or dependentsizedarray>
	// methodpointertomember or dependentsizedarray>) ;
	/**
	 * inserts a submenu, with given text.
	 * <p>
	 * inserts an item with text <code>text</code>, that leads to a submenu
	 * <code>menu</code>.
	 * <p>
	 */
	public WMenuItem insertMenu(int index, final CharSequence text, WMenu menu) {
		return this.insertMenu(index, "", text, menu);
	}

	/**
	 * inserts a submenu, with given icon and text.
	 * <p>
	 * inserts an item with given text and icon, that leads to a submenu
	 * <code>menu</code>.
	 * <p>
	 */
	public WMenuItem insertMenu(int index, final String iconPath,
			final CharSequence text, WMenu menu) {
		WMenuItem item = new WMenuItem(iconPath, text, (WWidget) null,
				WMenuItem.LoadPolicy.LazyLoading);
		item.setMenu(menu);
		this.insertItem(index, item);
		return item;
	}

	/**
	 * Inserts an item.
	 * <p>
	 * Inserts a menu item. Use this form to insert specialized
	 * {@link WMenuItem} implementations.
	 * <p>
	 */
	public void insertItem(int index, WMenuItem item) {
		item.setParentMenu(this);
		this.getUl().insertWidget(index, item);
		if (this.contentsStack_ != null) {
			WWidget contents = item.getContents();
			if (contents != null) {
				this.contentsStack_.addWidget(contents);
				if (this.contentsStack_.getCount() == 1) {
					this.setCurrent(0);
					if (contents != null) {
						this.contentsStack_.setCurrentWidget(contents);
					}
					this.renderSelected(item, true);
					item.loadContents();
				} else {
					this.renderSelected(item, false);
				}
			} else {
				this.renderSelected(item, false);
			}
		} else {
			this.renderSelected(item, false);
		}
		this.itemPathChanged(item);
	}

	/**
	 * Adds a separator to the menu.
	 * <p>
	 * Adds a separator the menu.
	 */
	public void addSeparator() {
		this.addItem(new WMenuItem(true, WString.Empty));
	}

	/**
	 * Adds a section header to the menu.
	 */
	public WMenuItem addSectionHeader(final CharSequence text) {
		WMenuItem result = new WMenuItem(false, text);
		this.addItem(result);
		return result;
	}

	/**
	 * Removes an item.
	 * <p>
	 * Removes the given item. The item and its contents is not deleted.
	 * <p>
	 * 
	 * @see WMenu#addItem(CharSequence name, WWidget contents,
	 *      WMenuItem.LoadPolicy policy)
	 */
	public void removeItem(WMenuItem item) {
		WContainerWidget items = this.getUl();
		if (item.getParent() == items) {
			int itemIndex = items.getIndexOf(item);
			items.removeWidget(item);
			if (this.contentsStack_ != null && item.getContents() != null) {
				this.contentsStack_.removeWidget(item.getContents());
			}
			item.setParentMenu((WMenu) null);
			if (itemIndex <= this.current_ && this.current_ >= 0) {
				--this.current_;
			}
			this.select(this.current_, true);
		}
	}

	/**
	 * Selects an item.
	 * <p>
	 * Select the menu item <code>item</code>.
	 * <p>
	 * When <code>item</code> is <code>null</code>, the current selection is
	 * removed.
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
	 * <p>
	 * Menu items in a menu with <code>N</code> items are numbered from 0 to
	 * <code>N</code> - 1.
	 * <p>
	 * Using a value of -1 removes the current selection.
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
	 * <p>
	 * This signal is emitted when an item was selected. It is emitted both when
	 * the user activated an item, or when {@link WMenu#select(WMenuItem item)
	 * select()} was invoked.
	 * <p>
	 * 
	 * @see WMenu#itemSelectRendered()
	 */
	public Signal1<WMenuItem> itemSelected() {
		return this.itemSelected_;
	}

	/**
	 * Signal which indicates that a new selected item is rendered.
	 * <p>
	 * This signal is similar to {@link WMenu#itemSelected() itemSelected}, but
	 * is emitted from within a stateless slot. Therefore, any slot connected to
	 * this signal will be optimized to client-side JavaScript, and must support
	 * the contract of a stateless slot (i.e., be idempotent).
	 * <p>
	 * If you are unsure what is the difference with the
	 * {@link WMenu#itemSelected() itemSelected} signal, you&apos;ll probably
	 * need the latter instead.
	 * <p>
	 * 
	 * @see WMenu#itemSelected()
	 */
	public Signal1<WMenuItem> itemSelectRendered() {
		return this.itemSelectRendered_;
	}

	/**
	 * Closes an item.
	 * <p>
	 * Close the menu item <code>item</code>. Only
	 * {@link WMenuItem#setCloseable(boolean closeable) closeable} items can be
	 * closed.
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
	 * <p>
	 * Menu items in a menu with <code>N</code> items are numbered from 0 to
	 * <code>N</code> - 1.
	 * <p>
	 * 
	 * @see WMenu#close(WMenuItem item)
	 */
	public void close(int index) {
		this.close(this.itemAt(index));
	}

	/**
	 * Returns the items.
	 * <p>
	 * Returns the list of menu items in this menu.
	 * <p>
	 */
	public List<WMenuItem> getItems() {
		List<WMenuItem> result = new ArrayList<WMenuItem>();
		for (int i = 0; i < this.getCount(); ++i) {
			result.add(this.itemAt(i));
		}
		return result;
	}

	/**
	 * Signal which indicates that an item was closed.
	 * <p>
	 * This signal is emitted when an item was closed. It is emitted both when
	 * the user closes an item, or when {@link WMenu#close(WMenuItem item)
	 * close()} was invoked.
	 */
	public Signal1<WMenuItem> itemClosed() {
		return this.itemClosed_;
	}

	/**
	 * Hides an item.
	 * <p>
	 * Hides the menu item <code>item</code>. By default, all menu items are
	 * visible.
	 * <p>
	 * If the item was currently selected, then the next item to be selected is
	 * determined by {@link WMenu#nextAfterHide(int index) nextAfterHide()}.
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
	 * <p>
	 * Menu items in a menu with <code>N</code> items are numbered from 0 to
	 * <code>N</code> - 1.
	 * <p>
	 * 
	 * @see WMenu#setItemHidden(WMenuItem item, boolean hidden)
	 */
	public void setItemHidden(int index, boolean hidden) {
		this.itemAt(index).setHidden(hidden);
	}

	/**
	 * Returns whether the item widget of the given item is hidden.
	 * <p>
	 * 
	 * @see WMenu#setItemHidden(WMenuItem item, boolean hidden)
	 */
	public boolean isItemHidden(WMenuItem item) {
		return item.isHidden();
	}

	/**
	 * Returns whether the item widget of the given index is hidden.
	 * <p>
	 * Menu items in a menu with <code>N</code> items are numbered from 0 to
	 * <code>N</code> - 1.
	 * <p>
	 * 
	 * @see WMenu#setItemHidden(WMenuItem item, boolean hidden)
	 */
	public boolean isItemHidden(int index) {
		return this.isItemHidden(this.itemAt(index));
	}

	/**
	 * Disables an item.
	 * <p>
	 * Disables the menu item <code>item</code>. Only an item that is enabled
	 * can be selected. By default, all menu items are enabled.
	 * <p>
	 * 
	 * @see WMenu#setItemDisabled(int index, boolean disabled)
	 * @see WWebWidget#setDisabled(boolean disabled)
	 */
	public void setItemDisabled(WMenuItem item, boolean disabled) {
		item.setDisabled(disabled);
	}

	/**
	 * Disables an item.
	 * <p>
	 * Menu items in a menu with <code>N</code> items are numbered from 0 to
	 * <code>N</code> - 1.
	 * <p>
	 * 
	 * @see WMenu#setItemDisabled(WMenuItem item, boolean disabled)
	 */
	public void setItemDisabled(int index, boolean disabled) {
		this.setItemDisabled(this.itemAt(index), disabled);
	}

	/**
	 * Returns whether the item widget of the given item is disabled.
	 * <p>
	 * 
	 * @see WMenu#setItemDisabled(WMenuItem item, boolean disabled)
	 */
	public boolean isItemDisabled(WMenuItem item) {
		return item.isDisabled();
	}

	/**
	 * Returns whether the item widget of the given index is disabled.
	 * <p>
	 * Menu items in a menu with <code>N</code> items are numbered from 0 to
	 * <code>N</code> - 1.
	 * <p>
	 * 
	 * @see WMenu#setItemDisabled(WMenuItem item, boolean disabled)
	 */
	public boolean isItemDisabled(int index) {
		return this.isItemDisabled(this.itemAt(index));
	}

	/**
	 * Returns the currently selected item.
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
	 * <p>
	 * 
	 * @see WMenu#getCurrentItem()
	 * @see WMenu#select(int index)
	 */
	public int getCurrentIndex() {
		return this.current_;
	}

	/**
	 * Returns the orientation (<b>deprecated</b>).
	 * <p>
	 * The orientation is set at time of construction.
	 * <p>
	 * 
	 * @deprecated this function no longer has any use and will be removed.
	 */
	public Orientation getOrientation() {
		return Orientation.Horizontal;
	}

	/**
	 * Renders using an HTML list (<b>deprecated</b>).
	 * <p>
	 * This function no longer has an effect, as a menu is now always rendered
	 * as a list.
	 * <p>
	 * 
	 * @deprecated this function no longer has any use and will be removed.
	 */
	public void setRenderAsList(boolean enable) {
		logger.error(new StringWriter().append(
				"WMenu::setRenderAsList() has been deprecated.").toString());
	}

	/**
	 * Returns whether the menu is rendered as an HTML list (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated this function no longer has any use and will be removed.
	 */
	public boolean renderAsList() {
		return true;
	}

	/**
	 * Enables internal paths for items.
	 * <p>
	 * The menu participates in the internal path by changing the internal path
	 * when an item has been selected, and listening for path changes to react
	 * to path selections. As a consequence this allows the user to bookmark the
	 * current menu selection and revisit it later, use back/forward buttons to
	 * navigate through history of visited menu items, and allows indexing of
	 * pages.
	 * <p>
	 * For each menu item, {@link WMenuItem#getPathComponent()
	 * WMenuItem#getPathComponent()} is appended to the <code>basePath</code>,
	 * which defaults to the internal path (
	 * {@link WApplication#getBookmarkUrl() WApplication#getBookmarkUrl()}). A
	 * &apos;/&apos; is appended to the base path, to turn it into a folder, if
	 * needed.
	 * <p>
	 * By default, menu interaction does not change the application internal
	 * path.
	 * <p>
	 * 
	 * @see WMenuItem#setPathComponent(String path)
	 */
	public void setInternalPathEnabled(final String basePath) {
		WApplication app = WApplication.getInstance();
		this.basePath_ = basePath.length() == 0 ? app.getInternalPath()
				: basePath;
		this.basePath_ = StringUtils.append(StringUtils.prepend(this.basePath_,
				'/'), '/');
		if (!this.internalPathEnabled_) {
			this.internalPathEnabled_ = true;
			app.internalPathChanged().addListener(this,
					new Signal1.Listener<String>() {
						public void trigger(String e1) {
							WMenu.this.handleInternalPathChange(e1);
						}
					});
		}
		this.previousInternalPath_ = app.getInternalPath();
		this.internalPathChanged(app.getInternalPath());
		this.updateItemsInternalPath();
	}

	/**
	 * Enables internal paths for items.
	 * <p>
	 * Calls {@link #setInternalPathEnabled(String basePath)
	 * setInternalPathEnabled("")}
	 */
	public final void setInternalPathEnabled() {
		setInternalPathEnabled("");
	}

	/**
	 * Returns whether the menu generates internal paths entries.
	 * <p>
	 * 
	 * @see WMenu#setInternalPathEnabled(String basePath)
	 */
	public boolean isInternalPathEnabled() {
		return this.internalPathEnabled_;
	}

	/**
	 * Sets the internal base path.
	 * <p>
	 * A &apos;/&apos; is appended to turn it into a folder, if needed.
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
	 * <p>
	 * The default value is the application&apos;s internalPath (
	 * {@link WApplication#getBookmarkUrl() WApplication#getBookmarkUrl()}) that
	 * was recorded when {@link WMenu#setInternalPathEnabled(String basePath)
	 * setInternalPathEnabled()} was called, and together with each
	 * {@link WMenuItem#getPathComponent() WMenuItem#getPathComponent()}
	 * determines the paths for each item.
	 * <p>
	 * For example, if {@link WMenu#getInternalBasePath() getInternalBasePath()}
	 * is <code>&quot;/examples/&quot;</code> and pathComponent() for a
	 * particular item is <code>&quot;charts/&quot;</code>, then the internal
	 * path for that item will be <code>&quot;/examples/charts/&quot;</code>.
	 * <p>
	 * 
	 * @see WMenu#setInternalPathEnabled(String basePath)
	 */
	public String getInternalBasePath() {
		return this.basePath_;
	}

	/**
	 * Returns the contents stack associated with the menu.
	 */
	public WStackedWidget getContentsStack() {
		return this.contentsStack_;
	}

	public int getCount() {
		return this.getUl().getCount();
	}

	public WMenuItem itemAt(int index) {
		return ((this.getUl().getWidget(index)) instanceof WMenuItem ? (WMenuItem) (this
				.getUl().getWidget(index))
				: null);
	}

	int indexOf(WMenuItem item) {
		return this.getUl().getIndexOf(item);
	}

	public WMenuItem getParentItem() {
		return this.parentItem_;
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
	 * <p>
	 * This methods makes the menu react to internal path changes (and also the
	 * initial internal path).
	 * <p>
	 * You may want to reimplement this if you want to customize the internal
	 * path handling.
	 */
	void internalPathChanged(final String path) {
		WApplication app = WApplication.getInstance();
		if (app.internalPathMatches(this.basePath_)) {
			String subPath = app.internalSubPath(this.basePath_);
			int bestI = -1;
			int bestMatchLength = -1;
			for (int i = 0; i < this.getCount(); ++i) {
				int matchLength = match(subPath, this.itemAt(i)
						.getPathComponent());
				if (matchLength > bestMatchLength) {
					bestMatchLength = matchLength;
					bestI = i;
				}
			}
			if (bestI != -1) {
				this.itemAt(bestI).setFromInternalPath(path);
			} else {
				if (subPath.length() != 0) {
					logger.warn(new StringWriter().append("unknown path: '")
							.append(subPath).append("'").toString());
				} else {
					this.select(-1, false);
				}
			}
		}
	}

	/**
	 * Returns the index of the item to be selected after hides.
	 * <p>
	 * Returns the index of the item to be selected after the item with given
	 * index will be hidden.
	 * <p>
	 * By default, if the given index is an index of currently selected item,
	 * returns an index of the first visible item to the right of it. If it is
	 * not found, returns the index of the first visible item to the left of it.
	 * If there are no visible items around the currently selected item, returns
	 * the index of currently selected item.
	 * <p>
	 * You may want to reimplement this if you want to customize the algorithm
	 * of determining the index of the item to be selected after hiding the item
	 * with given index.
	 */
	protected int nextAfterHide(int index) {
		if (this.current_ == index) {
			for (int i = this.current_ + 1; i < this.getCount(); ++i) {
				if (!this.isItemHidden(i)) {
					return i;
				}
			}
			for (int i = this.current_ - 1; i >= 0; --i) {
				if (!this.isItemHidden(i)) {
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
	private AbstractSignal.Connection contentsStackConnection_;

	private void contentsDestroyed() {
		for (int i = 0; i < this.getCount(); ++i) {
			this.itemAt(i).purgeContents();
		}
	}

	private void handleInternalPathChange(final String path) {
		if (!(this.parentItem_ != null)) {
			this.internalPathChanged(path);
		}
	}

	private int current_;
	private int previousStackIndex_;
	private boolean needSelectionEventUpdate_;

	private void init() {
		this.internalPathEnabled_ = false;
		this.emitPathChange_ = false;
		this.parentItem_ = null;
		this.needSelectionEventUpdate_ = false;
		this.current_ = -1;
		if (this.contentsStack_ != null) {
			this.contentsStack_.childrenChanged().addListener(this,
					new Signal.Listener() {
						public void trigger() {
							WMenu.this.updateSelectionEvent();
						}
					});
		}
		this.setImplementation(this.ul_ = new WContainerWidget());
		this.ul_.setList(true);
	}

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
			if (app.internalPathMatches(this.basePath_
					+ item.getPathComponent())) {
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
		if (changePath && this.internalPathEnabled_ && index != -1
				&& item.isInternalPathEnabled()) {
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
			WWidget contents = item.getContents();
			if (contents != null) {
				this.contentsStack_.setCurrentWidget(contents);
			}
		}
		this.itemSelectRendered_.trigger(item);
	}

	private void onItemHidden(int index, boolean hidden) {
		if (hidden) {
			int nextItem = this.nextAfterHide(index);
			if (nextItem != this.current_) {
				this.select(nextItem);
			}
		}
	}

	void select(int index, boolean changePath) {
		int last = this.current_;
		this.setCurrent(index);
		this.selectVisual(this.current_, changePath, true);
		if (index != -1) {
			WMenuItem item = this.itemAt(index);
			item.show();
			item.loadContents();
			WObject.DeletionTracker guard = new WObject.DeletionTracker(this);
			if (changePath && this.emitPathChange_) {
				WApplication app = WApplication.getInstance();
				app.internalPathChanged().trigger(app.getInternalPath());
				if (guard.isDeleted()) {
					return;
				}
				this.emitPathChange_ = false;
			}
			if (last != index) {
				item.triggered().trigger(item);
				if (!guard.isDeleted()) {
					if (this.getUl().getIndexOf(item) != -1) {
						this.itemSelected_.trigger(item);
					} else {
						this.select(-1);
					}
				}
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
