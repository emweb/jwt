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
 * A single item in a menu.
 */
public class WMenuItem extends WContainerWidget {
	private static Logger logger = LoggerFactory.getLogger(WMenuItem.class);

	/**
	 * Enumeration that determines when contents should be loaded.
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
	 * Creates a new item with given label.
	 * <p>
	 * The optional contents is a widget that will be shown in the {@link WMenu}
	 * contents stack when the item is selected. For this widget, a load
	 * <code>policy</code> can be indicated which specifies whether the contents
	 * widgets is transmitted only when it the item is activated for the first
	 * time (LazyLoading) or transmitted prior to first rendering.
	 * <p>
	 * If the menu supports internal path navigation, then a default
	 * {@link WMenuItem#getPathComponent() getPathComponent()} will be derived
	 * from the <code>label</code>, and can be customized using
	 * {@link WMenuItem#setPathComponent(String path) setPathComponent()}.
	 */
	public WMenuItem(CharSequence text, WWidget contents,
			WMenuItem.LoadPolicy policy) {
		super();
		this.separator_ = false;
		this.triggered_ = new Signal1<WMenuItem>(this);
		this.pathComponent_ = "";
		this.create("", text, contents, policy);
	}

	/**
	 * Creates a new item with given label.
	 * <p>
	 * Calls
	 * {@link #WMenuItem(CharSequence text, WWidget contents, WMenuItem.LoadPolicy policy)
	 * this(text, (WWidget)null, WMenuItem.LoadPolicy.LazyLoading)}
	 */
	public WMenuItem(CharSequence text) {
		this(text, (WWidget) null, WMenuItem.LoadPolicy.LazyLoading);
	}

	/**
	 * Creates a new item with given label.
	 * <p>
	 * Calls
	 * {@link #WMenuItem(CharSequence text, WWidget contents, WMenuItem.LoadPolicy policy)
	 * this(text, contents, WMenuItem.LoadPolicy.LazyLoading)}
	 */
	public WMenuItem(CharSequence text, WWidget contents) {
		this(text, contents, WMenuItem.LoadPolicy.LazyLoading);
	}

	public WMenuItem(String iconPath, CharSequence text, WWidget contents,
			WMenuItem.LoadPolicy policy) {
		super();
		this.separator_ = false;
		this.triggered_ = new Signal1<WMenuItem>(this);
		this.pathComponent_ = "";
		this.create(iconPath, text, contents, policy);
	}

	public WMenuItem(String iconPath, CharSequence text) {
		this(iconPath, text, (WWidget) null, WMenuItem.LoadPolicy.LazyLoading);
	}

	public WMenuItem(String iconPath, CharSequence text, WWidget contents) {
		this(iconPath, text, contents, WMenuItem.LoadPolicy.LazyLoading);
	}

	public void remove() {
		if (!this.isContentsLoaded()) {
			if (this.contents_ != null)
				this.contents_.remove();
		}
		if (this.subMenu_ != null)
			this.subMenu_.remove();
		super.remove();
	}

	/**
	 * Sets the text for this item.
	 * <p>
	 * Unless a custom path component was defined, the
	 * {@link WMenuItem#getPathComponent() getPathComponent()} is also updated
	 * based on the new text.
	 * <p>
	 * The item widget is updated using updateItemWidget().
	 * <p>
	 * 
	 * @see WMenuItem#setPathComponent(String path)
	 */
	public void setText(CharSequence text) {
		if (!(this.text_ != null)) {
			this.text_ = new WText(this.getAnchor());
			this.text_.setTextFormat(TextFormat.PlainText);
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
						result = StringUtils.put(result, i, Character
								.toLowerCase((char) result.charAt(i)));
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
	 * <p>
	 * The icon should have a width of 16 pixels.
	 * <p>
	 * 
	 * @see WMenuItem#setText(CharSequence text)
	 */
	public void setIcon(String path) {
		if (!(this.icon_ != null)) {
			WAnchor a = this.getAnchor();
			if (!(a != null)) {
				return;
			}
			this.icon_ = new WText(" ");
			a.insertWidget(0, this.icon_);
			WApplication app = WApplication.getInstance();
			app.getTheme().apply(this, this.icon_,
					WidgetThemeRole.MenuItemIconRole);
		}
		this.icon_.getDecorationStyle().setBackgroundImage(new WLink(path));
	}

	/**
	 * Returns the item icon path.
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
	 * <p>
	 * When an item is checkable, a checkbox is displayed to the left of the
	 * item text (instead of an icon).
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
				WApplication app = WApplication.getInstance();
				app.getTheme().apply(this, this.checkBox_,
						WidgetThemeRole.MenuItemCheckBoxRole);
			} else {
				if (this.checkBox_ != null)
					this.checkBox_.remove();
			}
		}
	}

	/**
	 * Returns whether the item is checkable.
	 * <p>
	 * 
	 * @see WMenuItem#setCheckable(boolean checkable)
	 */
	public boolean isCheckable() {
		return this.checkBox_ != null;
	}

	/**
	 * Sets the path component for this item.
	 * <p>
	 * The path component is used by the menu item in the application internal
	 * path (see {@link WApplication#makeAbsoluteUrl(String url)
	 * WApplication#makeAbsoluteUrl()}), when internal paths are enabled (see
	 * {@link WMenu#setInternalPathEnabled(String basePath)
	 * WMenu#setInternalPathEnabled()}) for the menu.
	 * <p>
	 * You may specify an empty <code>path</code> to let a menu item be the
	 * &quot;default&quot; menu option.
	 * <p>
	 * For example, if {@link WMenu#getInternalBasePath()
	 * WMenu#getInternalBasePath()} is <code>&quot;/examples/&quot;</code> and
	 * {@link WMenuItem#getPathComponent() getPathComponent()} for is
	 * <code>&quot;charts/&quot;</code>, then the internal path for the item
	 * will be <code>&quot;/examples/charts/&quot;</code>.
	 * <p>
	 * By default, the path is automatically derived from
	 * {@link WMenuItem#getText() getText()}. If a {@link WString#isLiteral()
	 * literal text} is used, the path is based on the text itself, otherwise on
	 * the {@link WString#getKey() key}. It is converted to lower case, and
	 * replacing white space and special characters with &apos;_&apos;.
	 * <p>
	 * 
	 * @see WMenuItem#setText(CharSequence text)
	 * @see WMenu#setInternalPathEnabled(String basePath)
	 */
	public void setPathComponent(String path) {
		this.customPathComponent_ = true;
		this.pathComponent_ = path;
		this.updateInternalPath();
		if (this.menu_ != null) {
			this.menu_.itemPathChanged(this);
		}
	}

	/**
	 * Returns the path component for this item.
	 * <p>
	 * You may want to reimplement this to customize the path component set by
	 * the item in the application internal path.
	 * <p>
	 * 
	 * @see WMenuItem#setPathComponent(String path)
	 */
	public String getPathComponent() {
		return this.pathComponent_;
	}

	/**
	 * Sets the associated link.
	 * <p>
	 */
	public void setLink(WLink link) {
		WAnchor a = this.getAnchor();
		if (a != null) {
			a.setLink(link);
		}
	}

	/**
	 * Returns the associated link.
	 * <p>
	 * 
	 * @see WMenuItem#setLink(WLink link)
	 */
	public WLink getLink() {
		WAnchor a = this.getAnchor();
		if (a != null) {
			return a.getLink();
		} else {
			return new WLink("");
		}
	}

	/**
	 * Sets the link target.
	 * <p>
	 * 
	 * @see WMenuItem#setLink(WLink link)
	 */
	public void setLinkTarget(AnchorTarget target) {
		WAnchor a = this.getAnchor();
		if (a != null) {
			a.setTarget(target);
		}
	}

	/**
	 * Returns the link target.
	 * <p>
	 * 
	 * @see WMenuItem#setLinkTarget(AnchorTarget target)
	 */
	public AnchorTarget getLinkTarget() {
		WAnchor a = this.getAnchor();
		if (a != null) {
			return this.getAnchor().getTarget();
		} else {
			return AnchorTarget.TargetSelf;
		}
	}

	/**
	 * Sets a sub menu.
	 * <p>
	 * Ownership of the <code>subMenu</code> is transferred to the item. In most
	 * cases, the sub menu would use the same contents stack as the parent menu.
	 */
	public void setMenu(WMenu menu) {
		this.subMenu_ = menu;
		this.subMenu_.parentItem_ = this;
		this.addWidget(this.subMenu_);
		WPopupMenu popup = ((this.getMenu()) instanceof WPopupMenu ? (WPopupMenu) (this
				.getMenu())
				: null);
		if (popup != null) {
			popup.setJavaScriptMember("wtNoReparent", "true");
			this.setSelectable(false);
			popup.setButton(this.getAnchor());
			this.updateInternalPath();
		}
		this.addStyleClass("submenu");
	}

	/**
	 * Returns the menu.
	 * <p>
	 * 
	 * @see WMenuItem#setMenu(WMenu menu)
	 */
	public WMenu getMenu() {
		return this.subMenu_;
	}

	/**
	 * Sets the checked state.
	 * <p>
	 * This is only used when {@link WMenuItem#isCheckable() isCheckable()} ==
	 * <code>true</code>.
	 * <p>
	 * 
	 * @see WMenuItem#setCheckable(boolean checkable)
	 * @see WMenuItem#isCheckable()
	 */
	public void setChecked(boolean checked) {
		if (this.isCheckable()) {
			WCheckBox cb = ((this.getAnchor().getWidget(0)) instanceof WCheckBox ? (WCheckBox) (this
					.getAnchor().getWidget(0))
					: null);
			cb.setChecked(checked);
		}
	}

	/**
	 * Returns the checked state.
	 * <p>
	 * This is only used when {@link WMenuItem#isCheckable() isCheckable()} ==
	 * <code>true</code>.
	 * <p>
	 * 
	 * @see WMenuItem#setChecked(boolean checked)
	 * @see WMenuItem#isCheckable()
	 */
	public boolean isChecked() {
		if (this.isCheckable()) {
			WCheckBox cb = ((this.getAnchor().getWidget(0)) instanceof WCheckBox ? (WCheckBox) (this
					.getAnchor().getWidget(0))
					: null);
			return cb.isChecked();
		} else {
			return false;
		}
	}

	/**
	 * Sets whether the menu item can be selected.
	 * <p>
	 * Only a menu item that can be selected can be the result of a popup menu
	 * selection.
	 * <p>
	 * The default value is <code>true</code> for a normal menu item, and
	 * <code>false</code> for a menu item that has a submenu.
	 * <p>
	 * An item that is selectable but is disabled can still not be selected.
	 */
	public void setSelectable(boolean selectable) {
		this.selectable_ = selectable;
	}

	/**
	 * Returns whether the menu item can be selected.
	 * <p>
	 * 
	 * @see WMenuItem#setSelectable(boolean selectable)
	 */
	public boolean isSelectable() {
		return this.selectable_;
	}

	/**
	 * Sets associated additional data with the item.
	 * <p>
	 * You can use this to associate model information with a menu item.
	 */
	public void setData(Object data) {
		this.data_ = data;
	}

	/**
	 * Returns additional data of the item.
	 * <p>
	 * 
	 * @see WMenuItem#setData(Object data)
	 */
	public Object getData() {
		return this.data_;
	}

	public WCheckBox getCheckBox() {
		return this.checkBox_;
	}

	/**
	 * Make it possible to close this item interactively or by
	 * {@link WMenuItem#close() close()}.
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
				app.getTheme().apply(this, closeIcon,
						WidgetThemeRole.MenuItemCloseRole);
				closeIcon.clicked().addListener(this,
						new Signal1.Listener<WMouseEvent>() {
							public void trigger(WMouseEvent e1) {
								WMenuItem.this.close();
							}
						});
			} else {
				if (this.getWidget(0) != null)
					this.getWidget(0).remove();
			}
		}
	}

	/**
	 * Returns whether the item is closeable.
	 * <p>
	 * 
	 * @see WMenuItem#setCloseable(boolean closeable)
	 */
	public boolean isCloseable() {
		return this.closeable_;
	}

	/**
	 * Closes this item.
	 * <p>
	 * Hides the item widget and emits {@link WMenu#itemClosed()
	 * WMenu#itemClosed()} signal. Only closeable items can be closed.
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

	/**
	 * Returns the contents widget for this item.
	 * <p>
	 * The contents widget is the widget in the {@link WStackedWidget}
	 * associated with this item.
	 */
	public WWidget getContents() {
		if (this.contentsContainer_ != null) {
			return this.contentsContainer_;
		} else {
			return this.contents_;
		}
	}

	WWidget getTakeContents() {
		WWidget result = this.contents_;
		if (this.isContentsLoaded()) {
			if (this.contentsContainer_ != null) {
				this.contentsContainer_.removeWidget(this.contents_);
			}
		}
		this.contents_ = null;
		return result;
	}

	// public WWidget getItemWidget() ;
	/**
	 * Selects this item.
	 * <p>
	 * If the item was previously closed it will be shown.
	 * <p>
	 * 
	 * @see WMenuItem#close()
	 */
	public void select() {
		if (this.menu_ != null && this.selectable_) {
			this.menu_.select(this);
		}
	}

	/**
	 * Signal emitted when an item is activated.
	 * <p>
	 * Returns this item as argument.
	 * <p>
	 */
	public Signal1<WMenuItem> triggered() {
		return this.triggered_;
	}

	/**
	 * Returns whether this item is a separator.
	 * <p>
	 * 
	 * @see WMenu#addSeparator()
	 */
	public boolean isSeparator() {
		return this.separator_;
	}

	/**
	 * Returns whether this item is a section header.
	 * <p>
	 * 
	 * @see WMenu#addSectionHeader(CharSequence text)
	 */
	public boolean isSectionHeader() {
		WAnchor a = this.getAnchor();
		return !this.separator_ && !(a != null) && !(this.subMenu_ != null)
				&& this.text_ != null;
	}

	/**
	 * Renders the item as selected or unselected.
	 * <p>
	 * The default implementation sets the styleclass for
	 * {@link WMenuItem#getItemWidget() getItemWidget()} to &apos;item&apos; for
	 * an unselected not closeable, &apos;itemselected&apos; for selected not
	 * closeable, &apos;citem&apos; for an unselected closeable and
	 * &apos;citemselected&apos; for selected closeable item.
	 * <p>
	 * Note that this method is called from within a stateless slot
	 * implementation, and thus should be stateless as well.
	 */
	public void renderSelected(boolean selected) {
		WApplication app = WApplication.getInstance();
		String active = app.getTheme().getActiveClass();
		if (active.equals("Wt-selected")) {
			this.setStyleClass(selected ? "itemselected" : "item");
		} else {
			this.toggleStyleClass(active, selected, true);
		}
	}

	void setFromInternalPath(String path) {
		if (this.menu_.contentsStack_ != null
				&& this.menu_.contentsStack_.getCurrentWidget() != this
						.getContents()) {
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
		super.enableAjax();
	}

	protected void render(EnumSet<RenderFlag> flags) {
		this.connectSignals();
		super.render(flags);
	}

	WMenuItem(boolean separator, CharSequence text) {
		super();
		this.separator_ = true;
		this.triggered_ = new Signal1<WMenuItem>(this);
		this.pathComponent_ = "";
		this.create("", WString.Empty, (WWidget) null,
				WMenuItem.LoadPolicy.LazyLoading);
		this.separator_ = separator;
		this.selectable_ = false;
		if (!(text.length() == 0)) {
			this.text_ = new WText(text, TextFormat.PlainText, this);
		}
	}

	private WContainerWidget contentsContainer_;
	private WWidget contents_;
	private WMenu menu_;
	private WMenu subMenu_;
	private WText icon_;
	private WText text_;
	private WCheckBox checkBox_;
	private Object data_;
	private boolean separator_;
	private boolean selectable_;
	private boolean signalsConnected_;
	private Signal1<WMenuItem> triggered_;
	private String pathComponent_;
	private boolean customPathComponent_;
	private boolean closeable_;

	private void create(String iconPath, CharSequence text, WWidget contents,
			WMenuItem.LoadPolicy policy) {
		this.contentsContainer_ = null;
		this.contents_ = contents;
		this.menu_ = null;
		this.customPathComponent_ = false;
		this.closeable_ = false;
		this.selectable_ = true;
		this.text_ = null;
		this.icon_ = null;
		this.checkBox_ = null;
		this.subMenu_ = null;
		this.data_ = null;
		if (this.contents_ != null && policy != WMenuItem.LoadPolicy.PreLoading) {
			this.contentsContainer_ = new WContainerWidget();
			this.contentsContainer_.setJavaScriptMember("wtResize",
					StdWidgetItemImpl.getChildrenResizeJS());
			this.contentsContainer_.resize(WLength.Auto, new WLength(100,
					WLength.Unit.Percentage));
		}
		if (!this.separator_) {
			WAnchor a = new WAnchor(this);
			WApplication app = WApplication.getInstance();
			if (app.getEnvironment().getAgent() == WEnvironment.UserAgent.IE6) {
				a.setLink(new WLink("javascript:false"));
			}
		}
		this.signalsConnected_ = false;
		if (iconPath.length() != 0) {
			this.setIcon(iconPath);
		}
		if (!this.separator_) {
			this.setText(text);
		}
	}

	private WAnchor getAnchor() {
		for (int i = 0; i < this.getCount(); ++i) {
			WAnchor result = ((this.getWidget(i)) instanceof WAnchor ? (WAnchor) (this
					.getWidget(i))
					: null);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	void purgeContents() {
		this.contentsContainer_ = null;
		this.contents_ = null;
	}

	void updateInternalPath() {
		if (this.menu_ != null
				&& this.menu_.isInternalPathEnabled()
				&& !(((this.getMenu()) instanceof WPopupMenu ? (WPopupMenu) (this
						.getMenu())
						: null) != null)) {
			String internalPath = this.menu_.getInternalBasePath()
					+ this.getPathComponent();
			WLink link = new WLink(WLink.Type.InternalPath, internalPath);
			WAnchor a = this.getAnchor();
			if (a != null) {
				a.setLink(link);
			}
		} else {
			WAnchor a = this.getAnchor();
			if (a != null) {
				if (WApplication.getInstance().getEnvironment().getAgent() == WEnvironment.UserAgent.IE6) {
					a.setLink(new WLink("#"));
				} else {
					a.setLink(new WLink());
				}
			}
		}
	}

	private boolean isContentsLoaded() {
		return !(this.contentsContainer_ != null)
				|| this.contentsContainer_.getCount() == 1;
	}

	void loadContents() {
		if (!this.isContentsLoaded()) {
			this.contentsContainer_.addWidget(this.contents_);
			this.signalsConnected_ = false;
			this.connectSignals();
		}
	}

	void setParentMenu(WMenu menu) {
		this.menu_ = menu;
		this.updateInternalPath();
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
			if (this.isContentsLoaded()) {
				// this.implementStateless(WMenuItem.selectVisual,WMenuItem.undoSelectVisual);
			}
			WAnchor a = this.getAnchor();
			if (a != null) {
				AbstractSignal as;
				if (this.checkBox_ != null) {
					as = a.mouseWentUp();
					a.setLink(new WLink());
				} else {
					as = a.clicked();
				}
				if (this.contentsContainer_ != null
						&& this.contentsContainer_.getCount() == 0) {
					as.addListener(this, new Signal.Listener() {
						public void trigger() {
							WMenuItem.this.selectNotLoaded();
						}
					});
				} else {
					as.addListener(this, new Signal.Listener() {
						public void trigger() {
							WMenuItem.this.selectVisual();
						}
					});
					as.addListener(this, new Signal.Listener() {
						public void trigger() {
							WMenuItem.this.select();
						}
					});
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
}
