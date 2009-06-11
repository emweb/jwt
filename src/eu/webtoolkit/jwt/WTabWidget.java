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
 * A widget that organizes contents in tab panes
 * 
 * 
 * This widget combines a horizontal {@link WMenu} with a {@link WStackedWidget}
 * , and a tab-like look.
 * <p>
 * This widget uses the following resources:
 * <ul>
 * <li>
 * <i>resourcesURL</i>/tab_b.gif</li>
 * <li>
 * <i>resourcesURL</i>/tab_l.gif</li>
 * <li>
 * <i>resourcesURL</i>/tab_r.gif</li>
 * </ul>
 * <p>
 * These files may be found in the resources/ folder of the Wt distribution.
 * <p>
 * The default value for <i>resourcesURL</i> is &quot;resources/&quot;. This
 * value may be overridden with any valid URL which points to the location where
 * these files may be found, by configuring the <i>resourcesURL</i> property in
 * your Wt configuration file.
 * <p>
 * Usage example:
 * <p>
 * <code>
 WTabWidget examples = new WTabWidget(this); <br> 
	  <br> 
 examples.addTab(helloWorldExample(), &quot;Hello World&quot;); <br> 
 examples.addTab(chartExample(), &quot;Charts&quot;); <br> 
 examples.addTab(new WText(&quot;A WText&quot;), &quot;WText&quot;); <br> 
	  <br> 
 examples.currentChanged().addListener(this, new Signal.Listener(){ <br> 
	public void trigger() { <br> 
		//custom code <br> 
	} <br> 
  }); <br> 
 examples.setInternalPathEnabled(); <br> 
 examples.setInternalBasePath(&quot;/examples&quot;);		
</code>
 * <p>
 * <div align="center"> <img src="/WTabWidget-1.png"
 * alt="An example WTabWidget">
 * <p>
 * <strong>An example WTabWidget</strong>
 * </p>
 * </div>
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

		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Create a new {@link WTabWidget}.
	 */
	public WTabWidget(WContainerWidget parent) {
		super(parent);
		this.currentChanged_ = new Signal1<Integer>(this);
		this.items_ = new ArrayList<WTabWidget.TabItem>();
		this.create(EnumSet.of(AlignmentFlag.AlignJustify));
	}

	public WTabWidget() {
		this((WContainerWidget) null);
	}

	/**
	 * Create a new {@link WTabWidget} with custom layout alignment.
	 * 
	 * The default constructor will use a layout manager to fit the tab widget
	 * within a parent container, and show scrollbars inside a tab if needed.
	 * <p>
	 * Here you can override the alignment option for the layout manager, e.g.
	 * use {@link AlignmentFlag#AlignTop} | {@link AlignmentFlag#AlignJustify}
	 * to not attempt to constrain the height of the tab widget.
	 */
	public WTabWidget(EnumSet<AlignmentFlag> layoutAlignment,
			WContainerWidget parent) {
		super(parent);
		this.currentChanged_ = new Signal1<Integer>(this);
		this.items_ = new ArrayList<WTabWidget.TabItem>();
		this.create(layoutAlignment);
	}

	public WTabWidget(EnumSet<AlignmentFlag> layoutAlignment) {
		this(layoutAlignment, (WContainerWidget) null);
	}

	/**
	 * Add a new tab, with <i>child</i> as content, and the given label.
	 * 
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
		this.items_.add(new WTabWidget.TabItem());
		this.items_.get(this.items_.size() - 1).enabled = true;
		this.items_.get(this.items_.size() - 1).hidden = false;
		return result;
	}

	public final WMenuItem addTab(WWidget child, CharSequence label) {
		return addTab(child, label, WTabWidget.LoadPolicy.LazyLoading);
	}

	/**
	 * Removes a tab item.
	 * 
	 * The widget itself is not deleted.
	 * <p>
	 * 
	 * @see WMenu#removeItem(WMenuItem item)
	 */
	public void removeTab(WWidget child) {
		WMenuItem item = this.menu_.getItems().get(this.getIndexOf(child));
		this.menu_.removeItem(item);
		item.getTakeContents();
		/* delete item */;
	}

	/**
	 * Return the number of tabs.
	 */
	public int getCount() {
		return this.contents_.getCount();
	}

	/**
	 * Get the content widget at the given tab <i>index</i>.
	 */
	public WWidget getWidget(int index) {
		return this.contents_.getWidget(index);
	}

	/**
	 * Get the index of the given widget.
	 * 
	 * If the widget is not in this tab widget, then -1 is returned.
	 */
	public int getIndexOf(WWidget widget) {
		return this.contents_.getIndexOf(widget);
	}

	/**
	 * Activate the tab at <i>index</i>.
	 */
	public void setCurrentIndex(int index) {
		this.menu_.select(index);
	}

	/**
	 * Get the index of the activated tab.
	 */
	public int getCurrentIndex() {
		return this.menu_.getCurrentIndex();
	}

	/**
	 * Activate the tab showing the given <i>widget</i>.
	 */
	public void setCurrentWidget(WWidget widget) {
		this.setCurrentIndex(this.contents_.getIndexOf(widget));
	}

	/**
	 * Get the widget of the activated tab.
	 */
	public WWidget getCurrentWidget() {
		return this.contents_.getCurrentWidget();
	}

	/**
	 * Enable or disable a tab.
	 * 
	 * Enables or disables the tab at <i>index</i>. A disabled tab cannot be
	 * activated.
	 */
	public void setTabEnabled(int index, boolean enable) {
		this.items_.get(index).enabled = enable;
	}

	/**
	 * Returns if a tab is enabled.
	 */
	public boolean isTabEnabled(int index) {
		return this.items_.get(index).enabled;
	}

	/**
	 * Hide or show a tab.
	 * 
	 * Hides are shows the tab at <i>index</i>.
	 */
	public void setTabHidden(int index, boolean hidden) {
		this.items_.get(index).hidden = hidden;
	}

	/**
	 * Return if a tab is hidden.
	 */
	public boolean isTabHidden(int index) {
		return this.items_.get(index).hidden;
	}

	/**
	 * Change the label for a tab.
	 */
	public void setTabText(int index, CharSequence label) {
		TabWidgetItem item = ((this.menu_.getItems().get(index)) instanceof TabWidgetItem ? (TabWidgetItem) (this.menu_
				.getItems().get(index))
				: null);
		item.setText(label);
	}

	/**
	 * Get the label for a tab.
	 */
	public WString getTabText(int index) {
		TabWidgetItem item = ((this.menu_.getItems().get(index)) instanceof TabWidgetItem ? (TabWidgetItem) (this.menu_
				.getItems().get(index))
				: null);
		return item.getText();
	}

	/**
	 * Set the tooltip for a tab.
	 * 
	 * The tooltip is shown when the user hovers over the label.
	 */
	public void setTabToolTip(int index, CharSequence tip) {
		this.items_.get(index).toolTip = WString.toWString(tip);
	}

	/**
	 * Get the tooltip for a tab.
	 */
	public WString getTabToolTip(int index) {
		return this.items_.get(index).toolTip;
	}

	/**
	 * Enable internal paths for items.
	 * 
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
	public void setInternalPathEnabled() {
		this.menu_.setInternalPathEnabled();
	}

	/**
	 * Returns whether internal paths are enabled.
	 * 
	 * @see WMenu#setInternalPathEnabled()
	 */
	public boolean isInternalPathEnabled() {
		return this.menu_.isInternalPathEnabled();
	}

	/**
	 * Set the internal base path.
	 * 
	 * A &apos;/&apos; is appended to turn it into a folder, if needed.
	 * <p>
	 * 
	 * @see WMenu#setInternalPathEnabled()
	 * @see WMenu#getInternalBasePath()
	 */
	public void setInternalBasePath(String path) {
		this.menu_.setInternalBasePath(path);
	}

	/**
	 * Returns the internal base path.
	 * 
	 * The default value is the application&apos;s internalPath (
	 * {@link WApplication#getInternalPath()}) that was recorded when
	 * {@link WMenu#setInternalPathEnabled()} was called, and together with each
	 * {@link WMenuItem#getPathComponent()} determines the paths for each item.
	 * <p>
	 * For example, if {@link WMenu#getInternalBasePath()} is
	 * <code>&quot;/examples/&quot;</code> and pathComponent() for a particular
	 * item is <code>&quot;charts/&quot;</code>, then the internal path for that
	 * item will be <code>&quot;/examples/charts/&quot;</code>.
	 * <p>
	 * 
	 * @see WMenu#setInternalPathEnabled()
	 */
	public String getInternalBasePath() {
		return this.menu_.getInternalBasePath();
	}

	/**
	 * Signal emitted when the user activates a tab.
	 * 
	 * The index of the newly activated tab is passed as an argument.
	 */
	public Signal1<Integer> currentChanged() {
		return this.currentChanged_;
	}

	private Signal1<Integer> currentChanged_;
	private WContainerWidget layout_;
	private WMenu menu_;
	private WStackedWidget contents_;

	private static class TabItem {
		public boolean enabled;
		public boolean hidden;
		public WString toolTip;
	}

	private List<WTabWidget.TabItem> items_;

	private void create(EnumSet<AlignmentFlag> layoutAlignment) {
		this.setImplementation(this.layout_ = new WContainerWidget());
		String CSS_RULES_NAME = "Wt::WTabWidget";
		WApplication app = WApplication.getInstance();
		if (!app.getStyleSheet().isDefined(CSS_RULES_NAME)) {
			String resourcesURL = WApplication.getResourcesUrl();
			app
					.getStyleSheet()
					.addRule(
							".Wt-tabs",
							"background: transparent url("
									+ resourcesURL
									+ "tab_b.gif) repeat-x scroll center bottom;margin-bottom:4px;zoom: 1;width:100%",
							CSS_RULES_NAME);
			app.getStyleSheet().addRule(".Wt-tabs li", "display: inline;");
			app
					.getStyleSheet()
					.addRule(
							".Wt-tabs ul",
							"margin: 0px;padding-left: 10px;list-style-type: none;list-style-position: outside;");
			app
					.getStyleSheet()
					.addRule(
							".Wt-tabs span",
							"background: transparent url("
									+ resourcesURL
									+ "tab_r.gif) no-repeat scroll right top;border-bottom:1px solid #84B0C7;float:left; display:block;cursor:pointer;cursor:hand;font-size: small; font-weight: bold;");
			app.getStyleSheet().addRule(".Wt-tabs span.itemselected",
					"background-position:100% -150px;");
			app
					.getStyleSheet()
					.addRule(
							".Wt-tabs span span",
							"background: transparent url("
									+ resourcesURL
									+ "tab_l.gif) no-repeat scroll left top;border-bottom: 0px;white-space: nowrap;padding:5px 9px;color:#1A419D;");
			app.getStyleSheet().addRule(".Wt-tabs span.itemselected span",
					"background-position:0% -150px;");
		}
		this.contents_ = new WStackedWidget();
		this.menu_ = new WMenu(this.contents_, Orientation.Horizontal);
		this.menu_.setRenderAsList(true);
		WContainerWidget menuDiv = new WContainerWidget();
		menuDiv.setStyleClass("Wt-tabs");
		menuDiv.addWidget(this.menu_);
		WVBoxLayout box = new WVBoxLayout();
		box.setSpacing(0);
		box.setContentsMargins(0, 0, 0, 0);
		box.addWidget(menuDiv);
		box.addWidget(this.contents_, 1);
		this.layout_.setLayout(box, layoutAlignment);
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
}
