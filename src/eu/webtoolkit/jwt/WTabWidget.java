/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * A widget that organizes contents in tab panes.
 * <p>
 * 
 * This widget combines a horizontal {@link WMenu} with a {@link WStackedWidget}
 * , and a tab-like look.
 * <p>
 * A tab widget will place the tab bar on top of the contents, and fit the
 * contents below it.
 * <p>
 * Usage example:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * WTabWidget examples = new WTabWidget(this);
 * 
 * examples.addTab(helloWorldExample(), &quot;Hello World&quot;);
 * examples.addTab(chartExample(), &quot;Charts&quot;);
 * examples.addTab(new WText(&quot;A WText&quot;), &quot;WText&quot;);
 * 
 * examples.currentChanged().addListener(this, new Signal.Listener() {
 * 	public void trigger() {
 * 		//custom code
 * 	}
 * });
 * examples.setInternalPathEnabled();
 * examples.setInternalBasePath(&quot;/examples&quot;);
 * </pre>
 * 
 * </blockquote>
 * <p>
 * <h3>CSS</h3>
 * <p>
 * The tab widget is styled by the current CSS theme. The look (of the header)
 * can be overridden using the <code>Wt-tabs</code> CSS class and the following
 * selectors:
 * <p>
 * <div class="fragment">
 * 
 * <pre class="fragment">
 * .Wt-tabs ul        : the list
 * .Wt-tabs li        : a list item
 * .Wt-tabs span      : outer span of a list item
 * .Wt-span span span : inner span of a list item
 * </pre>
 * 
 * </div>
 * <p>
 * <table border="0" align="center" cellspacing="3" cellpadding="3">
 * <tr>
 * <td><div align="center"> <img src="doc-files//WTabWidget-default-1.png"
 * alt="An example WTabWidget (default)">
 * <p>
 * <strong>An example WTabWidget (default)</strong>
 * </p>
 * </div></td>
 * <td><div align="center"> <img src="doc-files//WTabWidget-polished-1.png"
 * alt="An example WTabWidget (polished)">
 * <p>
 * <strong>An example WTabWidget (polished)</strong>
 * </p>
 * </div></td>
 * </tr>
 * </table>
 */
public class WTabWidget extends WCompositeWidget {
	/**
	 * When should the contents be loaded ?
	 */
	public enum LoadPolicy {
		/**
		 * Lazy loading: on first use.
		 */
		LazyLoading,
		/**
		 * Pre-loading: before first use.
		 */
		PreLoading;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Creates a new tab widget.
	 */
	public WTabWidget(WContainerWidget parent) {
		super(parent);
		this.currentChanged_ = new Signal1<Integer>(this);
		this.tabClosed_ = new Signal1<Integer>();
		this.contentsWidgets_ = new ArrayList<WWidget>();
		this.create(EnumSet.of(AlignmentFlag.AlignJustify));
	}

	/**
	 * Creates a new tab widget.
	 * <p>
	 * Calls {@link #WTabWidget(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WTabWidget() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates a new tab widget (indicating layout alignment)
	 * (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated Since JWt 3.1.1, the <code>layoutAlignment</code> is no
	 *             longer needed and its value is ignored
	 */
	public WTabWidget(EnumSet<AlignmentFlag> layoutAlignment,
			WContainerWidget parent) {
		super(parent);
		this.currentChanged_ = new Signal1<Integer>(this);
		this.tabClosed_ = new Signal1<Integer>();
		this.contentsWidgets_ = new ArrayList<WWidget>();
		this.create(layoutAlignment);
	}

	/**
	 * Creates a new tab widget (indicating layout alignment)
	 * (<b>deprecated</b>).
	 * <p>
	 * Calls {@link #WTabWidget(EnumSet layoutAlignment, WContainerWidget parent)
	 * this(layoutAlignment, (WContainerWidget)null)}
	 */
	public WTabWidget(EnumSet<AlignmentFlag> layoutAlignment) {
		this(layoutAlignment, (WContainerWidget) null);
	}

	/**
	 * Adds a new tab, with <i>child</i> as content, and the given label.
	 * <p>
	 * Returns the menu item that implements the tab item.
	 */
	public WMenuItem addTab(WWidget child, CharSequence label,
			WTabWidget.LoadPolicy loadPolicy) {
		WMenuItem.LoadPolicy policy = WMenuItem.LoadPolicy.PreLoading;
		switch (loadPolicy) {
		case PreLoading:
			policy = WMenuItem.LoadPolicy.PreLoading;
			break;
		case LazyLoading:
			policy = WMenuItem.LoadPolicy.LazyLoading;
			break;
		}
		WMenuItem result = new TabWidgetItem(label, child, policy);
		this.menu_.addItem(result);
		this.contentsWidgets_.add(child);
		return result;
	}

	/**
	 * Adds a new tab, with <i>child</i> as content, and the given label.
	 * <p>
	 * Returns
	 * {@link #addTab(WWidget child, CharSequence label, WTabWidget.LoadPolicy loadPolicy)
	 * addTab(child, label, WTabWidget.LoadPolicy.LazyLoading)}
	 */
	public final WMenuItem addTab(WWidget child, CharSequence label) {
		return addTab(child, label, WTabWidget.LoadPolicy.LazyLoading);
	}

	/**
	 * Removes a tab item.
	 * <p>
	 * The widget itself is not deleted.
	 * <p>
	 * 
	 * @see WMenu#removeItem(WMenuItem item)
	 */
	public void removeTab(WWidget child) {
		int tabIndex = this.getIndexOf(child);
		if (tabIndex != -1) {
			this.contentsWidgets_.remove(0 + tabIndex);
			WMenuItem item = this.menu_.getItems().get(tabIndex);
			this.menu_.removeItem(item);
			item.getTakeContents();
			;
		}
	}

	/**
	 * Returns the number of tabs.
	 */
	public int getCount() {
		return this.contentsWidgets_.size();
	}

	/**
	 * Returns the content widget at the given tab <i>index</i>.
	 */
	public WWidget getWidget(int index) {
		return this.contentsWidgets_.get(index);
	}

	/**
	 * Returns the index of the tab of the given content widget.
	 * <p>
	 * If the widget is not in this tab widget, then -1 is returned.
	 */
	public int getIndexOf(WWidget widget) {
		return this.contentsWidgets_.indexOf(widget);
	}

	/**
	 * Activates the tab at <i>index</i>.
	 */
	public void setCurrentIndex(int index) {
		this.menu_.select(index);
	}

	/**
	 * Returns the index of the activated tab.
	 */
	public int getCurrentIndex() {
		return this.menu_.getCurrentIndex();
	}

	/**
	 * Activates the tab showing the given <i>widget</i>.
	 */
	public void setCurrentWidget(WWidget widget) {
		this.setCurrentIndex(this.getIndexOf(widget));
	}

	/**
	 * Returns the widget of the activated tab.
	 */
	public WWidget getCurrentWidget() {
		return this.menu_.getCurrentItem().getContents();
	}

	/**
	 * Enables or disables a tab.
	 * <p>
	 * Enables or disables the tab at <code>index</code>. A disabled tab cannot
	 * be activated.
	 */
	public void setTabEnabled(int index, boolean enable) {
		TabWidgetItem item = ((this.menu_.getItems().get(index)) instanceof TabWidgetItem ? (TabWidgetItem) (this.menu_
				.getItems().get(index))
				: null);
		item.setDisabled(!enable);
	}

	/**
	 * Returns whether a tab is enabled.
	 * <p>
	 */
	public boolean isTabEnabled(int index) {
		TabWidgetItem item = ((this.menu_.getItems().get(index)) instanceof TabWidgetItem ? (TabWidgetItem) (this.menu_
				.getItems().get(index))
				: null);
		return !item.isDisabled();
	}

	/**
	 * Hides or shows a tab.
	 * <p>
	 * Hides or shows the tab at <code>index</code>.
	 */
	public void setTabHidden(int index, boolean hidden) {
		TabWidgetItem item = ((this.menu_.getItems().get(index)) instanceof TabWidgetItem ? (TabWidgetItem) (this.menu_
				.getItems().get(index))
				: null);
		item.setHidden(hidden);
	}

	/**
	 * Returns whether a tab is hidden.
	 */
	public boolean isTabHidden(int index) {
		TabWidgetItem item = ((this.menu_.getItems().get(index)) instanceof TabWidgetItem ? (TabWidgetItem) (this.menu_
				.getItems().get(index))
				: null);
		return item.isHidden();
	}

	/**
	 * Make it possible to close a tab interactively or by
	 * {@link WTabWidget#closeTab(int index) closeTab}.
	 */
	public void setTabCloseable(int index, boolean closeable) {
		TabWidgetItem item = ((this.menu_.getItems().get(index)) instanceof TabWidgetItem ? (TabWidgetItem) (this.menu_
				.getItems().get(index))
				: null);
		item.setCloseable(closeable);
	}

	/**
	 * Returns whether a tab is closeable.
	 */
	public boolean isTabCloseable(int index) {
		TabWidgetItem item = ((this.menu_.getItems().get(index)) instanceof TabWidgetItem ? (TabWidgetItem) (this.menu_
				.getItems().get(index))
				: null);
		return item.isCloseable();
	}

	/**
	 * Changes the label for a tab.
	 */
	public void setTabText(int index, CharSequence label) {
		TabWidgetItem item = ((this.menu_.getItems().get(index)) instanceof TabWidgetItem ? (TabWidgetItem) (this.menu_
				.getItems().get(index))
				: null);
		item.setText(label);
	}

	/**
	 * Returns the label for a tab.
	 */
	public WString getTabText(int index) {
		TabWidgetItem item = ((this.menu_.getItems().get(index)) instanceof TabWidgetItem ? (TabWidgetItem) (this.menu_
				.getItems().get(index))
				: null);
		return item.getText();
	}

	/**
	 * Sets the tooltip for a tab.
	 * <p>
	 * The tooltip is shown when the user hovers over the label.
	 */
	public void setTabToolTip(int index, CharSequence tip) {
		TabWidgetItem item = ((this.menu_.getItems().get(index)) instanceof TabWidgetItem ? (TabWidgetItem) (this.menu_
				.getItems().get(index))
				: null);
		item.setToolTip(tip);
	}

	/**
	 * Returns the tooltip for a tab.
	 */
	public WString getTabToolTip(int index) {
		TabWidgetItem item = ((this.menu_.getItems().get(index)) instanceof TabWidgetItem ? (TabWidgetItem) (this.menu_
				.getItems().get(index))
				: null);
		return item.getToolTip();
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
	 * {@link WMenu#getInternalBasePath() WMenu#getInternalBasePath()}), which
	 * defaults to the internal path ({@link WApplication#getInternalPath()
	 * WApplication#getInternalPath()}) but may be changed using
	 * {@link WMenu#setInternalBasePath(String basePath)
	 * WMenu#setInternalBasePath()}, with a &apos;/&apos; appended to turn it
	 * into a folder, if needed.
	 * <p>
	 * By default, menu interaction does not change the application internal
	 * path.
	 * <p>
	 * 
	 * @see WMenuItem#setPathComponent(String path)
	 */
	public void setInternalPathEnabled(String basePath) {
		this.menu_.setInternalPathEnabled(basePath);
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
	 * Returns whether internal paths are enabled.
	 * <p>
	 * 
	 * @see WMenu#setInternalPathEnabled(String basePath)
	 */
	public boolean isInternalPathEnabled() {
		return this.menu_.isInternalPathEnabled();
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
	public void setInternalBasePath(String path) {
		this.menu_.setInternalBasePath(path);
	}

	/**
	 * Returns the internal base path.
	 * <p>
	 * The default value is the application&apos;s internalPath (
	 * {@link WApplication#getInternalPath() WApplication#getInternalPath()})
	 * that was recorded when
	 * {@link WMenu#setInternalPathEnabled(String basePath)
	 * WMenu#setInternalPathEnabled()} was called, and together with each
	 * {@link WMenuItem#getPathComponent() WMenuItem#getPathComponent()}
	 * determines the paths for each item.
	 * <p>
	 * For example, if {@link WMenu#getInternalBasePath()
	 * WMenu#getInternalBasePath()} is <code>&quot;/examples/&quot;</code> and
	 * pathComponent() for a particular item is <code>&quot;charts/&quot;</code>
	 * , then the internal path for that item will be
	 * <code>&quot;/examples/charts/&quot;</code>.
	 * <p>
	 * 
	 * @see WMenu#setInternalPathEnabled(String basePath)
	 */
	public String getInternalBasePath() {
		return this.menu_.getInternalBasePath();
	}

	/**
	 * Signal emitted when the user activates a tab.
	 * <p>
	 * The index of the newly activated tab is passed as an argument.
	 */
	public Signal1<Integer> currentChanged() {
		return this.currentChanged_;
	}

	/**
	 * Closes a tab at <code>index</code>.
	 */
	public void closeTab(int index) {
		this.setTabHidden(index, true);
		this.tabClosed_.trigger(index);
	}

	/**
	 * Signal emitted when the user closes a tab.
	 * <p>
	 * The index of the closed tab is passed as an argument.
	 */
	public Signal1<Integer> tabClosed() {
		return this.tabClosed_;
	}

	private Signal1<Integer> currentChanged_;
	private Signal1<Integer> tabClosed_;
	private WContainerWidget layout_;
	private WMenu menu_;
	private List<WWidget> contentsWidgets_;

	private void create(EnumSet<AlignmentFlag> layoutAlignment) {
		this.setImplementation(this.layout_ = new WContainerWidget());
		;
		this.menu_ = new WMenu(new WStackedWidget(), Orientation.Horizontal);
		this.menu_.setRenderAsList(true);
		WBreak clear = new WBreak();
		clear.setStyleClass("Wt-tabs-clear");
		WContainerWidget menuDiv = new WContainerWidget();
		menuDiv.setStyleClass("Wt-tabs");
		menuDiv.addWidget(this.menu_);
		menuDiv.addWidget(clear);
		this.layout_.addWidget(menuDiv);
		this.layout_.addWidget(this.menu_.getContentsStack());
		this
				.setJavaScriptMember(
						WT_RESIZE_JS,
						""
								+ "function(self, w, h) {self.style.height= h + 'px';var c = self.firstChild;var t = self.lastChild;h -= c.offsetHeight;if (h > 0)t."
								+ WT_RESIZE_JS + "(t, w, h);};");
		this.menu_.itemSelected().addListener(this,
				new Signal1.Listener<WMenuItem>() {
					public void trigger(WMenuItem e1) {
						WTabWidget.this.onItemSelected(e1);
					}
				});
	}

	private final void create(AlignmentFlag layoutAlignmen,
			AlignmentFlag... layoutAlignment) {
		create(EnumSet.of(layoutAlignmen, layoutAlignment));
	}

	private void onItemSelected(WMenuItem item) {
		this.currentChanged_.trigger(this.menu_.getCurrentIndex());
	}
	// private void setJsSize() ;
}
