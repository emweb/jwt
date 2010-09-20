/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.List;

/**
 * A widget that shows a menu of options
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
 * <blockquote>
 * 
 * <pre>
 * // create the stack where the contents will be located
 * WStackedWidget contents = new WStackedWidget(contentsParent);
 * 
 * // create a menu
 * WMenu menu = new WMenu(contents, Orientation.Vertical, menuParent);
 * menu.setRenderAsList(true);
 * 
 * // add four items using the default lazy loading policy.
 * menu.addItem(&quot;Introduction&quot;, new WText(&quot;intro&quot;));
 * menu.addItem(&quot;Download&quot;, new WText(&quot;Not yet available&quot;));
 * menu.addItem(&quot;Demo&quot;, new DemoWidget());
 * menu.addItem(new WMenuItem(&quot;Demo2&quot;, new DemoWidget()));
 * </pre>
 * 
 * </blockquote>
 * <p>
 * Historically, a menu was implemented as a table, but
 * {@link WMenu#setRenderAsList(boolean enable) rendering as a list} (
 * <code>&lt;ul&gt;</code>) is preferred, as it is the norm form
 * implementations.
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
 * Styling a menu will be different depending of the rendering mode (table,
 * list). Conventionally, menus like this are styled as a list (
 * {@link WMenu#setRenderAsList(boolean enable) setRenderAsList()}).
 * <p>
 * You will want to differentiate between unselected and selected items based on
 * the <code>&quot;item&quot;</code> and <code>&quot;itemselected&quot;</code>
 * style classes as well as between unselected and selected closeable items
 * based on the <code>&quot;citem&quot;</code> and
 * <code>&quot;citemselected&quot;</code> style classes.
 * <p>
 * For menus without closeable items a styling is pretty simple. For example,
 * assuming you set a <code>&quot;menu&quot;</code> style class for your menu,
 * you can style the items using:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * .menu * .item {
 *   cursor: pointer; cursor: hand;
 *   color: blue;
 *   text-decoration: underline;
 * }
 * 
 * .menu * .itemselected {
 *   color: blue;
 *   text-decoration: underline;
 *   font-weight: bold;  
 * }
 * </pre>
 * 
 * </blockquote>
 * <p>
 * For menus which supports closing and/or enabling disabling of items, the
 * styling is more complex. The close icon is styled by the current CSS theme
 * and can be overriden by using <code>&quot;Wt-closeicon&quot;</code> CSS
 * class. The look of the menu, assuming the default implementation for
 * {@link WMenuItem}, can be customized by using the following selectors (again
 * assuming a menu with as styleclass <code>&quot;menu&quot;</code>):
 * <p>
 * <div class="fragment">
 * 
 * <pre class="fragment">
 * .Wt-closeicon                     : a close icon
 * 
 * .menu                             : the list (you custom menu class)
 * .menu li                          : a list item
 * .menu span                        : spans of a list item
 * 
 * # not closable items:
 * .menu a.item span                 : the inner span of the label of an enabled item
 * .menu span.item                   : the label of a disabled item
 * .menu a.itemselected span         : the inner span the label of an enabled selected item
 * .menu span.itemselected           : the label of a disabled selected item
 * 
 * # closable items:
 * .menu .citem a.label span         : the inner span of the label of an enabled item
 * .menu .citem span.label           : the label of a disabled item
 * .menu .citemselected a.label span : the inner span the label of an enabled selected item
 * .menu .citemselected span.label   : the label of a disabled selected item
 * </pre>
 * 
 * </div>
 * <p>
 * Example of styling a menu with closeable items:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * .menu {
 *   overflow: auto;
 * }
 * 
 * .menu li {
 *   display: inline;
 * }
 * 
 * .menu span {
 *   display: block; float: left;
 * }
 * 
 * .menu a.item span {
 *   cursor: pointer; cursor: hand;
 *   color: blue;
 *   margin-right: 5px;
 * }
 * 
 * .menu span.item {
 *   color: gray;
 *   margin-right: 5px;
 * }
 * 
 * .menu a.itemselected span {
 *   color: black;
 *   font-weight: bold;
 *   text-decoration: none;
 *   margin-right: 5px;
 * }
 * 
 * .menu span.itemselected {
 *   color: gray;
 *   font-weight: bold;
 *   text-decoration: none;
 *   margin-right: 5px;
 * }
 * 
 * .menu .citem a.label span {
 *   cursor: pointer; cursor: hand;
 *   color: blue;
 * }
 * 
 * .menu .citem span.label {
 *   color: gray;
 * }
 * 
 * .menu .citemselected a.label span {
 *   color: black;
 *   font-weight: bold;
 *   text-decoration: none;
 * }
 * 
 * .menu .citemselected span.label {
 *   color: gray;
 *   font-weight: bold;
 *   text-decoration: none;
 * }
 * </pre>
 * 
 * </blockquote>
 * <p>
 * 
 * @see WMenuItem
 */
public class WMenu extends WCompositeWidget {
	/**
	 * Creates a new menu.
	 * <p>
	 * Construct a menu with given <code>orientation</code>. The menu is not
	 * associated with a contents stack, and thus you will want to react to the
	 * {@link WMenu#itemSelected() itemSelected()} signal to react to menu
	 * changes.
	 */
	public WMenu(Orientation orientation, WContainerWidget parent) {
		super(parent);
		this.contentsStack_ = null;
		this.orientation_ = orientation;
		this.internalPathEnabled_ = false;
		this.emitPathChange_ = false;
		this.basePath_ = "";
		this.previousInternalPath_ = "";
		this.itemSelected_ = new Signal1<WMenuItem>(this);
		this.itemSelectRendered_ = new Signal1<WMenuItem>(this);
		this.itemClosed_ = new Signal1<WMenuItem>(this);
		this.items_ = new ArrayList<WMenuItem>();
		this.current_ = -1;
		this.setRenderAsList(false);
	}

	/**
	 * Creates a new menu.
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
	 * Construct a menu to manage the widgets in <code>contentsStack</code>, and
	 * sets the menu <code>orientation</code>.
	 * <p>
	 * Each menu item will manage a single widget in the
	 * <code>contentsStack</code>, making it the current widget when the menu
	 * item is activated.
	 */
	public WMenu(WStackedWidget contentsStack, Orientation orientation,
			WContainerWidget parent) {
		super(parent);
		this.contentsStack_ = contentsStack;
		this.orientation_ = orientation;
		this.internalPathEnabled_ = false;
		this.emitPathChange_ = false;
		this.basePath_ = "";
		this.previousInternalPath_ = "";
		this.itemSelected_ = new Signal1<WMenuItem>(this);
		this.itemSelectRendered_ = new Signal1<WMenuItem>(this);
		this.itemClosed_ = new Signal1<WMenuItem>(this);
		this.items_ = new ArrayList<WMenuItem>();
		this.current_ = -1;
		this.setRenderAsList(false);
	}

	/**
	 * Creates a new menu.
	 * <p>
	 * Calls
	 * {@link #WMenu(WStackedWidget contentsStack, Orientation orientation, WContainerWidget parent)
	 * this(contentsStack, orientation, (WContainerWidget)null)}
	 */
	public WMenu(WStackedWidget contentsStack, Orientation orientation) {
		this(contentsStack, orientation, (WContainerWidget) null);
	}

	/**
	 * Destructor.
	 */
	public void remove() {
		for (int i = 0; i < this.items_.size(); ++i) {
			this.items_.get(i).setMenu((WMenu) null);
			;
		}
		super.remove();
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
	public WMenuItem addItem(CharSequence name, WWidget contents,
			WMenuItem.LoadPolicy policy) {
		return this.addItem(new WMenuItem(name, contents, policy));
	}

	/**
	 * Adds an item.
	 * <p>
	 * Returns
	 * {@link #addItem(CharSequence name, WWidget contents, WMenuItem.LoadPolicy policy)
	 * addItem(name, contents, WMenuItem.LoadPolicy.LazyLoading)}
	 */
	public final WMenuItem addItem(CharSequence name, WWidget contents) {
		return addItem(name, contents, WMenuItem.LoadPolicy.LazyLoading);
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
	public WMenuItem addItem(WMenuItem item) {
		item.setMenu(this);
		this.items_.add(item);
		if (this.renderAsList_) {
			WContainerWidget p = ((this.impl_) instanceof WContainerWidget ? (WContainerWidget) (this.impl_)
					: null);
			WContainerWidget li = new WContainerWidget();
			p.insertWidget(p.getCount(), li);
			li.addWidget(item.getItemWidget());
		} else {
			WTable layout = ((this.impl_) instanceof WTable ? (WTable) (this.impl_)
					: null);
			WTableCell parent = layout.getElementAt(
					this.orientation_ == Orientation.Vertical ? this.items_
							.size() - 1 : 0, 0);
			WWidget w = item.getItemWidget();
			parent.addWidget(w);
			if (this.orientation_ == Orientation.Horizontal) {
				w.setInline(true);
				new WText(" ", parent);
			}
		}
		for (int i = 0; i < this.items_.size(); ++i) {
			this.items_.get(i).resetLearnedSlots();
		}
		if (this.contentsStack_ != null) {
			WWidget contents = item.getContents();
			if (contents != null) {
				this.contentsStack_.addWidget(contents);
			}
			if (this.contentsStack_.getCount() == 1) {
				this.current_ = 0;
				if (contents != null) {
					this.contentsStack_.setCurrentWidget(contents);
				}
				this.items_.get(0).renderSelected(true);
				this.items_.get(0).loadContents();
			} else {
				item.renderSelected(false);
			}
		} else {
			item.renderSelected(false);
		}
		item.renderHidden(item.isHidden());
		if (this.internalPathEnabled_) {
			WApplication app = WApplication.getInstance();
			if (app.internalPathMatches(this.basePath_
					+ item.getPathComponent())) {
				this.select(this.items_.size() - 1, false);
			}
		}
		return item;
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
		int itemIndex = this.indexOf(item);
		if (itemIndex != -1) {
			this.items_.remove(0 + itemIndex);
			if (this.renderAsList_) {
				WContainerWidget li = ((item.getItemWidget().getParent()) instanceof WContainerWidget ? (WContainerWidget) (item
						.getItemWidget().getParent())
						: null);
				li.removeWidget(item.getItemWidget());
				if (li != null)
					li.remove();
			} else {
				WTableCell parent = ((item.getItemWidget().getParent()) instanceof WTableCell ? (WTableCell) (item
						.getItemWidget().getParent())
						: null);
				if (this.orientation_ == Orientation.Horizontal) {
					WWidget itemWidget = item.getItemWidget();
					WWidget separator = parent.getWidget(parent
							.getIndexOf(itemWidget) + 1);
					parent.removeWidget(itemWidget);
					if (separator != null)
						separator.remove();
				} else {
					WTable table = parent.getTable();
					parent.removeWidget(item.getItemWidget());
					table.deleteRow(parent.getRow());
				}
			}
			if (this.contentsStack_ != null && item.getContents() != null) {
				this.contentsStack_.removeWidget(item.getContents());
			}
			item.setMenu((WMenu) null);
			if (itemIndex <= this.current_ && this.current_ >= 0) {
				--this.current_;
			}
			for (int i = 0; i < this.items_.size(); ++i) {
				this.items_.get(i).resetLearnedSlots();
			}
			this.select(this.current_, true);
		}
	}

	/**
	 * Selects an item.
	 * <p>
	 * Select the menu item <code>item</code>.
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
		this.close(this.indexOf(item));
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
		WMenuItem item = this.items_.get(index);
		if (item.isCloseable()) {
			item.hide();
			this.itemClosed_.trigger(item);
		}
	}

	/**
	 * Returns the items.
	 * <p>
	 * Returns the list of menu items in this menu.
	 */
	public List<WMenuItem> getItems() {
		return this.items_;
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
	 * @see WMenuItem#hide()
	 */
	public void setItemHidden(WMenuItem item, boolean hidden) {
		this.setItemHidden(this.indexOf(item), hidden);
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
		this.items_.get(index).setHidden(hidden);
	}

	/**
	 * Returns whether the item widget of the given item is hidden.
	 * <p>
	 * 
	 * @see WMenu#setItemHidden(WMenuItem item, boolean hidden)
	 */
	public boolean isItemHidden(WMenuItem item) {
		return this.isItemHidden(this.indexOf(item));
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
		return this.items_.get(index).isHidden();
	}

	/**
	 * Disables an item.
	 * <p>
	 * Disables the menu item <code>item</code>. Only an item that is enabled
	 * can be selected. By default, all menu items are enabled.
	 * <p>
	 * 
	 * @see WMenu#setItemDisabled(int index, boolean disabled)
	 * @see WMenuItem#setDisabled(boolean disabled)
	 */
	public void setItemDisabled(WMenuItem item, boolean disabled) {
		this.setItemDisabled(this.indexOf(item), disabled);
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
		this.items_.get(index).setDisabled(disabled);
	}

	/**
	 * Returns whether the item widget of the given item is disabled.
	 * <p>
	 * 
	 * @see WMenu#setItemDisabled(WMenuItem item, boolean disabled)
	 */
	public boolean isItemDisabled(WMenuItem item) {
		return this.isItemDisabled(this.indexOf(item));
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
		return this.items_.get(index).isDisabled();
	}

	/**
	 * Returns the currently selected item.
	 * <p>
	 * 
	 * @see WMenu#getCurrentIndex()
	 * @see WMenu#select(WMenuItem item)
	 */
	public WMenuItem getCurrentItem() {
		return this.current_ >= 0 ? this.items_.get(this.current_) : null;
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
	 * Returns the orientation.
	 * <p>
	 * The orientation is set at time of construction.
	 */
	public Orientation getOrientation() {
		return this.orientation_;
	}

	/**
	 * Renders using an HTML list.
	 * <p>
	 * By default, the the menu is rendered using an HTML
	 * <code>&lt;table&gt;</code> element for layout. Setting this option
	 * enables rendering using <code>&lt;ul&gt;</code> and
	 * <code>&lt;il&gt;</code> elements, as is commonly done for CSS-based
	 * designs.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>You cannot use this method after items have been added to
	 * the menu. </i>
	 * </p>
	 */
	public void setRenderAsList(boolean enable) {
		if (enable) {
			WContainerWidget c = new WContainerWidget();
			c.setList(true);
			this.setImplementation(this.impl_ = c);
		} else {
			this.setImplementation(this.impl_ = new WTable());
		}
		this.renderAsList_ = enable;
	}

	/**
	 * Returns whether the menu is rendered as an HTML list.
	 * <p>
	 * 
	 * @see WMenu#setRenderAsList(boolean enable)
	 */
	public boolean renderAsList() {
		return this.renderAsList_;
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
	 * WMenuItem#getPathComponent()} is appended to the internal base path (
	 * {@link WMenu#getInternalBasePath() getInternalBasePath()}), which
	 * defaults to the internal path ({@link WApplication#getInternalPath()
	 * WApplication#getInternalPath()}) but may be changed using
	 * {@link WMenu#setInternalBasePath(String basePath) setInternalBasePath()},
	 * with a &apos;/&apos; appended to turn it into a folder, if needed.
	 * <p>
	 * By default, menu interaction does not change the application internal
	 * path.
	 * <p>
	 * 
	 * @see WMenuItem#setPathComponent(String path)
	 */
	public void setInternalPathEnabled(String basePath) {
		if (!this.internalPathEnabled_) {
			this.internalPathEnabled_ = true;
			WApplication app = WApplication.getInstance();
			this.basePath_ = StringUtils.terminate(basePath.length() == 0 ? app
					.getInternalPath() : basePath, '/');
			app.internalPathChanged().addListener(this,
					new Signal1.Listener<String>() {
						public void trigger(String e1) {
							WMenu.this.internalPathChanged(e1);
						}
					});
			this.previousInternalPath_ = app.getInternalPath();
			this.internalPathChanged(app.getInternalPath());
			this.updateItems();
		}
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
	public void setInternalBasePath(String basePath) {
		String bp = StringUtils.terminate(basePath, '/');
		if (!this.basePath_.equals(bp)) {
			this.basePath_ = bp;
			if (this.internalPathEnabled_) {
				WApplication app = WApplication.getInstance();
				this.previousInternalPath_ = app.getInternalPath();
				this.internalPathChanged(app.getInternalPath());
				this.updateItems();
			}
		}
	}

	/**
	 * Returns the internal base path.
	 * <p>
	 * The default value is the application&apos;s internalPath (
	 * {@link WApplication#getInternalPath() WApplication#getInternalPath()})
	 * that was recorded when
	 * {@link WMenu#setInternalPathEnabled(String basePath)
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

	protected void enableAjax() {
		for (int i = 0; i < this.items_.size(); ++i) {
			WMenuItem item = this.items_.get(i);
			item.enableAjax();
		}
		super.enableAjax();
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
	void internalPathChanged(String path) {
		WApplication app = WApplication.getInstance();
		if (app.internalPathMatches(this.basePath_)) {
			String value = app.getInternalPathNextPart(this.basePath_);
			for (int i = 0; i < this.items_.size(); ++i) {
				if (this.items_.get(i).getPathComponent().equals(value)
						|| this.items_.get(i).getPathComponent().equals(
								value + '/')) {
					this.items_.get(i).setFromInternalPath(path);
					return;
				}
			}
			if (value.length() != 0) {
				WApplication.getInstance().log("error").append(
						"WMenu: unknown path: '").append(value).append("'");
			} else {
				this.select(-1, false);
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
			for (int i = this.current_ + 1; i < this.items_.size(); ++i) {
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

	private WWidget impl_;
	WStackedWidget contentsStack_;
	private Orientation orientation_;
	private boolean renderAsList_;
	private boolean internalPathEnabled_;
	private boolean emitPathChange_;
	private String basePath_;
	private String previousInternalPath_;
	private Signal1<WMenuItem> itemSelected_;
	private Signal1<WMenuItem> itemSelectRendered_;
	private Signal1<WMenuItem> itemClosed_;
	private List<WMenuItem> items_;

	void select(int index, boolean changePath) {
		this.selectVisual(index, changePath);
		if (index != -1) {
			if (this.isItemHidden(index)) {
				this.setItemHidden(index, false);
			}
			this.items_.get(index).loadContents();
			this.itemSelected_.trigger(this.items_.get(this.current_));
			if (changePath && this.emitPathChange_) {
				WApplication app = WApplication.getInstance();
				app.internalPathChanged().trigger(app.getInternalPath());
				this.emitPathChange_ = false;
			}
		}
	}

	private int current_;
	private int previousCurrent_;
	private int previousStackIndex_;

	int indexOf(WMenuItem item) {
		return this.items_.indexOf(item);
	}

	void selectVisual(WMenuItem item) {
		this.selectVisual(this.indexOf(item), true);
	}

	private void selectVisual(int index, boolean changePath) {
		this.previousCurrent_ = this.current_;
		if (this.contentsStack_ != null) {
			this.previousStackIndex_ = this.contentsStack_.getCurrentIndex();
		}
		this.current_ = index;
		if (changePath && this.internalPathEnabled_ && this.current_ != -1) {
			WApplication app = WApplication.getInstance();
			this.previousInternalPath_ = app.getInternalPath();
			String newPath = this.basePath_
					+ this.items_.get(this.current_).getPathComponent();
			if (!newPath.equals(app.getInternalPath())) {
				this.emitPathChange_ = true;
			}
			app.setInternalPath(newPath);
		}
		for (int i = 0; i < this.items_.size(); ++i) {
			this.items_.get(i).renderSelected((int) i == this.current_);
		}
		if (index == -1) {
			return;
		}
		if (this.contentsStack_ != null) {
			WWidget contents = this.items_.get(this.current_).getContents();
			if (contents != null) {
				this.contentsStack_.setCurrentWidget(contents);
			}
		}
		this.itemSelectRendered_.trigger(this.items_.get(this.current_));
	}

	void undoSelectVisual() {
		String prevPath = this.previousInternalPath_;
		int prevStackIndex = this.previousStackIndex_;
		this.selectVisual(this.previousCurrent_, true);
		if (this.internalPathEnabled_) {
			WApplication app = WApplication.getInstance();
			app.setInternalPath(prevPath);
		}
		if (this.contentsStack_ != null) {
			this.contentsStack_.setCurrentIndex(prevStackIndex);
		}
	}

	private void recreateItem(int index) {
		WMenuItem item = this.items_.get(index);
		if (this.renderAsList_) {
			WContainerWidget li = ((item.getItemWidget().getParent()) instanceof WContainerWidget ? (WContainerWidget) (item
					.getItemWidget().getParent())
					: null);
			li.addWidget(item.getRecreateItemWidget());
		} else {
			WTableCell parent = ((item.getItemWidget().getParent()) instanceof WTableCell ? (WTableCell) (item
					.getItemWidget().getParent())
					: null);
			if (this.orientation_ == Orientation.Horizontal) {
				final int pos = parent.getIndexOf(item.getItemWidget());
				WWidget newItemWidget = item.getRecreateItemWidget();
				parent.insertWidget(pos, newItemWidget);
				newItemWidget.setInline(true);
			} else {
				parent.addWidget(item.getRecreateItemWidget());
			}
		}
		item.renderSelected(this.current_ == index);
		item.renderHidden(item.isHidden());
		for (int i = 0; i < this.items_.size(); ++i) {
			this.items_.get(i).resetLearnedSlots();
		}
	}

	void recreateItem(WMenuItem item) {
		this.recreateItem(this.indexOf(item));
	}

	private void doSetHiddenItem(int index, boolean hidden) {
		if (hidden) {
			int nextItem = this.nextAfterHide(index);
			if (nextItem != this.current_) {
				this.select(nextItem);
			}
		}
		this.items_.get(index).renderHidden(hidden);
	}

	void doSetHiddenItem(WMenuItem item, boolean hidden) {
		this.doSetHiddenItem(this.indexOf(item), hidden);
	}

	private void updateItems() {
		for (int i = 0; i < this.items_.size(); ++i) {
			WMenuItem item = this.items_.get(i);
			item.updateItemWidget(item.getItemWidget());
			item.resetLearnedSlots();
		}
	}
}
