/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.List;
import eu.webtoolkit.jwt.utils.StringUtils;

/**
 * A widget that shows a menu of options
 * <p>
 * 
 * The WMenu widget offers menu navigation in conjunction with a
 * {@link WStackedWidget}, where different &apos;contents&apos; are stacked upon
 * each other. Each choice in the menu (which is implemented as a
 * {@link WMenuItem}) corresponds to a tab in the contents stack. The contents
 * stack may contain other items, and could be shared with other {@link WMenu}
 * instances. (the old restriction of a dedicated contents stack has been
 * removed since Wt 2.2.1).
 * <p>
 * Usage example:
 * <p>
 * <code>
 // create the stack where the contents will be located <br> 
 WStackedWidget contents = new WStackedWidget(contentsParent); <br> 
		  <br> 
 // create a menu <br> 
 WMenu menu = new WMenu(contents, Orientation.Vertical, menuParent); <br> 
		  <br> 
 // add four items using the default lazy loading policy. <br> 
 menu.addItem(&quot;Introduction&quot;, new WText(tr(&quot;intro&quot;)); <br> 
 menu.addItem(&quot;Download&quot;, new WText(&quot;Not yet available&quot;)); <br> 
 menu.addItem(&quot;Demo&quot;, new DemoWidget()); <br> 
 menu.addItem(new WMenuItem(&quot;Demo2&quot;, new DemoWidget()));
</code>
 * <p>
 * After contruction, the first entry will be selected. At any time, it is
 * possible to select a particular item using
 * {@link WMenu#select(WMenuItem item)}.
 * <p>
 * The WMenu implementation offers fine-grained control on how contents should
 * be preloaded. By default, all contents is lazy-loaded, only when needed. To
 * improve response time, an item may also be preloaded (using
 * {@link WMenu#addItem(CharSequence name, WWidget contents, WMenuItem.LoadPolicy policy)}
 * ). In that case, the item will be loaded in the background, before its first
 * use. In any case, once the contents corresponding to a menu item is loaded,
 * subsequent navigation to it is handled entirely client-side.
 * <p>
 * The WMenu may participate in the application&apos;s internal path, which lets
 * menu items correspond to internal URLs, see
 * {@link WMenu#setInternalPathEnabled(String basePath)}.
 * <p>
 * The layout of the menu may be Horizontal or Vertical. The look of the items
 * may be defined through style sheets. The default {@link WMenuItem}
 * implementation uses two style classes to distinguish between activated and
 * inactivated menu items: &quot;item&quot; and &quot;itemselected&quot;. By
 * using CSS nested selectors, a different style may be defined for items in a
 * different menu.
 * <p>
 * For example, the (old) Wt homepage used the following CSS rules to style the
 * two menu (which both are assigned the style class .menu):
 * <p>
 * <code>
.menu * .item { <br> 
  cursor: pointer; cursor: hand; <br> 
  color: blue; <br> 
  text-decoration: underline; <br> 
} <br> 
 <br> 
.menu * .itemselected { <br> 
  color: blue; <br> 
  text-decoration: underline; <br> 
  font-weight: bold;   <br> 
}
</code>
 * <p>
 * You may customize the rendering and behaviour of menu entries by specializing
 * {@link WMenuItem}.
 * <p>
 * 
 * @see WMenuItem
 */
public class WMenu extends WCompositeWidget {
	/**
	 * Construct a new menu.
	 * <p>
	 * Construct a menu to manage the widgets in <i>contentsStack</i>, and sets
	 * the menu <i>orientation</i>.
	 * <p>
	 * Each menu item will manage a single widget in the <i>contentsStack</i>,
	 * making it the current widget when the menu item is activated.
	 */
	public WMenu(WStackedWidget contentsStack, Orientation orientation,
			WContainerWidget parent) {
		super(parent);
		this.contentsStack_ = contentsStack;
		this.orientation_ = orientation;
		this.internalPathEnabled_ = false;
		this.basePath_ = "";
		this.previousInternalPath_ = "";
		this.itemSelected_ = new Signal1<WMenuItem>(this);
		this.itemSelectRendered_ = new Signal1<WMenuItem>(this);
		this.items_ = new ArrayList<WMenuItem>();
		this.current_ = -1;
		this.setRenderAsList(false);
	}

	/**
	 * Construct a new menu.
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
			/* delete this.items_.get(i) */;
		}
		super.remove();
	}

	/**
	 * Add an item.
	 * <p>
	 * Adds a menu item with given <i>contents</i>, which is added to the
	 * menu&apos;s associated contents stack.
	 * <p>
	 * <i>contents</i> may be 0, in which case no contents in the contents stack
	 * is associated with the menu item.
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
	 * Add an item.
	 * <p>
	 * Returns
	 * {@link #addItem(CharSequence name, WWidget contents, WMenuItem.LoadPolicy policy)
	 * addItem(name, contents, WMenuItem.LoadPolicy.LazyLoading)}
	 */
	public final WMenuItem addItem(CharSequence name, WWidget contents) {
		return addItem(name, contents, WMenuItem.LoadPolicy.LazyLoading);
	}

	/**
	 * Add an item.
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
			WContainerWidget li = new WContainerWidget(p);
			li.addWidget(item.getItemWidget());
		} else {
			WTable layout = ((this.impl_) instanceof WTable ? (WTable) (this.impl_)
					: null);
			WTableCell parent = layout.getElementAt(
					this.orientation_ == Orientation.Vertical ? this.items_
							.size() : 0, 0);
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
		return item;
	}

	/**
	 * Remove an item.
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
				System.err
						.append(
								"WMenu::removeItem() only implemented when renderAsList == true")
						.append('\n');
			}
			this.contentsStack_.removeWidget(item.getContents());
			item.setMenu((WMenu) null);
			if (itemIndex <= this.current_ && this.current_ > 0) {
				--this.current_;
			}
			for (int i = 0; i < this.items_.size(); ++i) {
				this.items_.get(i).resetLearnedSlots();
			}
			this.select(this.current_);
		}
	}

	/**
	 * Select an item.
	 * <p>
	 * Select the menu item <i>item</i>.
	 * <p>
	 * 
	 * @see WMenu#select(int index)
	 * @see WMenu#getCurrentItem()
	 */
	public void select(WMenuItem item) {
		this.select(this.indexOf(item));
	}

	/**
	 * Select an item.
	 * <p>
	 * Menu items in a menu with <i>N</i> items are numbered from 0 to <i>N</i>
	 * - 1.
	 * <p>
	 * 
	 * @see WMenu#select(WMenuItem item)
	 * @see WMenu#getCurrentIndex()
	 */
	public void select(int index) {
		this.selectVisual(index);
		if (index != -1) {
			this.items_.get(index).loadContents();
			this.itemSelected_.trigger(this.items_.get(this.current_));
		}
	}

	/**
	 * Signal which indicates that a new item was selected.
	 * <p>
	 * This signal is emitted when an item was selected. It is emitted both when
	 * the user activated an item, or when {@link WMenu#select(WMenuItem item)}
	 * was invoked.
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
	 * the contract of a stateless slot.
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
	 * Returns the items.
	 * <p>
	 * Returns the list of menu items in this menu.
	 */
	public List<WMenuItem> getItems() {
		return this.items_;
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
	 * Render using an HTML list.
	 * <p>
	 * By default, the the menu is rendered using an HTML &lt;table&gt; element
	 * for layout. Setting this option enables rendering using &lt;ul&gt; and
	 * &lt;il&gt; elements, as is commonly done for CSS-based designs.
	 * <p>
	 * <p>
	 * <i><b>Note:</b>You cannot use this method after items have been added to
	 * the menu. </i>
	 * </p>
	 */
	public void setRenderAsList(boolean enable) {
		if (enable) {
			WContainerWidget c = new WContainerWidget();
			c.setList(true);
			c.setOverflow(WContainerWidget.Overflow.OverflowAuto);
			this.setImplementation(this.impl_ = c);
		} else {
			this.setImplementation(this.impl_ = new WTable());
		}
		this.renderAsList_ = enable;
	}

	/**
	 * Return whether the menu is rendered as an HTML list.
	 * <p>
	 * 
	 * @see WMenu#setRenderAsList(boolean enable)
	 */
	public boolean renderAsList() {
		return this.renderAsList_;
	}

	/**
	 * Enable internal paths for items.
	 * <p>
	 * The menu participates in the internal path by changing the internal path
	 * when an item has been selected, and listening for path changes to react
	 * to path selections. As a consequence this allows the user to bookmark the
	 * current menu selection and revisit it later, use back/forward buttons to
	 * navigate through history of visited menu items, and allows indexing of
	 * pages.
	 * <p>
	 * For each menu item, {@link WMenuItem#getPathComponent()} is appended to
	 * the internal base path ({@link WMenu#getInternalBasePath()}), which
	 * defaults to the internal path ({@link WApplication#getInternalPath()})
	 * but may be changed using
	 * {@link WMenu#setInternalBasePath(String basePath)}, with a &apos;/&apos;
	 * appended to turn it into a folder, if needed.
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
			this.internalPathChanged(app.getInternalPath());
			this.updateItems();
		}
	}

	/**
	 * Enable internal paths for items.
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
	 * Set the internal base path.
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
				this.internalPathChanged(app.getInternalPath());
				this.updateItems();
			}
		}
	}

	/**
	 * Returns the internal base path.
	 * <p>
	 * The default value is the application&apos;s internalPath (
	 * {@link WApplication#getInternalPath()}) that was recorded when
	 * {@link WMenu#setInternalPathEnabled(String basePath)} was called, and
	 * together with each {@link WMenuItem#getPathComponent()} determines the
	 * paths for each item.
	 * <p>
	 * For example, if {@link WMenu#getInternalBasePath()} is
	 * <code>&quot;/examples/&quot;</code> and pathComponent() for a particular
	 * item is <code>&quot;charts/&quot;</code>, then the internal path for that
	 * item will be <code>&quot;/examples/charts/&quot;</code>.
	 * <p>
	 * 
	 * @see WMenu#setInternalPathEnabled(String basePath)
	 */
	public String getInternalBasePath() {
		return this.basePath_;
	}

	protected void enableAjax() {
		if (this.internalPathEnabled_) {
			this.updateItems();
		}
		super.enableAjax();
	}

	private WWidget impl_;
	private WStackedWidget contentsStack_;
	private Orientation orientation_;
	private boolean renderAsList_;
	private boolean internalPathEnabled_;
	private String basePath_;
	private String previousInternalPath_;
	private Signal1<WMenuItem> itemSelected_;
	private Signal1<WMenuItem> itemSelectRendered_;
	private List<WMenuItem> items_;
	private int current_;
	private int previousCurrent_;
	private int previousStackIndex_;

	private int indexOf(WMenuItem item) {
		return this.items_.indexOf(item);
	}

	void selectVisual(WMenuItem item) {
		this.selectVisual(this.indexOf(item));
	}

	private void selectVisual(int index) {
		this.previousCurrent_ = this.current_;
		this.previousStackIndex_ = this.contentsStack_.getCurrentIndex();
		this.current_ = index;
		for (int i = 0; i < this.items_.size(); ++i) {
			this.items_.get(i).renderSelected((int) i == this.current_);
		}
		if (index == -1) {
			return;
		}
		WWidget contents = this.items_.get(this.current_).getContents();
		if (contents != null) {
			this.contentsStack_.setCurrentWidget(contents);
		}
		if (this.internalPathEnabled_) {
			WApplication app = WApplication.getInstance();
			this.previousInternalPath_ = app.getInternalPath();
			String newPath = this.basePath_;
			String pc = this.items_.get(this.current_).getPathComponent();
			if (pc.length() == 0) {
				if (newPath.length() > 1) {
					newPath = newPath.substring(0, 0 + newPath.length() - 1);
				}
			} else {
				newPath += pc;
			}
			if (newPath.equals(this.basePath_)
					|| !app.internalPathMatches(newPath)) {
				app.setInternalPath(newPath);
			}
		}
		this.itemSelectRendered_.trigger(this.items_.get(this.current_));
	}

	void undoSelectVisual() {
		String prevPath = this.previousInternalPath_;
		int prevStackIndex = this.previousStackIndex_;
		this.selectVisual(this.previousCurrent_);
		if (this.internalPathEnabled_) {
			WApplication.getInstance().setInternalPath(prevPath);
		}
		this.contentsStack_.setCurrentIndex(prevStackIndex);
	}

	private void internalPathChanged(String path) {
		WApplication app = WApplication.getInstance();
		if (app.internalPathMatches(this.basePath_)) {
			this.setFromState(app.getInternalPathNextPart(this.basePath_));
		}
	}

	private void setFromState(String value) {
		String v = value;
		for (int i = 0; i < this.items_.size(); ++i) {
			if (this.items_.get(i).getPathComponent().equals(v)
					|| this.items_.get(i).getPathComponent().equals(v + '/')) {
				if (this.contentsStack_.getCurrentWidget() != this.items_
						.get(i).getContents()) {
					this.select(i);
				}
				return;
			}
		}
		if (value.length() != 0) {
			WApplication.getInstance().log("error").append(
					"WMenu: unknown path: '").append(value).append("'");
		}
		this.select(-1);
	}

	private void updateItems() {
		for (int i = 0; i < this.items_.size(); ++i) {
			WMenuItem item = this.items_.get(i);
			item.updateItemWidget(item.getItemWidget());
			item.resetLearnedSlots();
		}
	}
}
