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

/**
 * A single item in a menu.
 * <p>
 * 
 * The item determines the look and behaviour of a single item in a
 * {@link WMenu}.
 * <p>
 * By default, for enabled menu items a {@link WMenuItem} uses a {@link WAnchor}
 * widget. For disabled menu items it uses a {@link WText} widget. If item is
 * closeable, {@link WMenuItem} puts these widgets and extra {@link WText}
 * widget (for a close icon) in a {@link WContainerWidget}. When the menu
 * participates in application internal paths (see
 * {@link WMenu#setInternalPathEnabled(String basePath)
 * WMenu#setInternalPathEnabled()}), the anchor references the bookmark URL
 * corresponding to the {@link WMenuItem#getPathComponent() getPathComponent()}
 * for the item (see {@link WApplication#getBookmarkUrl()
 * WApplication#getBookmarkUrl()}).
 * <p>
 * To provide another look for the menu items (such as perhaps adding an icon),
 * you can specialize this class, and reimplement the virtual methods:
 * <p>
 * <ul>
 * <li>{@link WMenuItem#createItemWidget() createItemWidget()}: to provide
 * another widget to represent the item.</li>
 * <li>{@link WMenuItem#updateItemWidget(WWidget itemWidget) updateItemWidget()}
 * : to update the widget to reflect item changes, triggered by for example
 * {@link WMenuItem#setText(CharSequence text) setText()} and
 * {@link WMenuItem#setPathComponent(String path) setPathComponent()}.</li>
 * <li>optionally, {@link WMenuItem#activateSignal() activateSignal()}: to bind
 * the event for activating the item to something else than the clicked event.</li>
 * <li>optionally, {@link WMenuItem#closeSignal() closeSignal()}: to bind the
 * event for closing the item to something else than the clicked event.</li>
 * <li>optionally, {@link WMenuItem#renderSelected(boolean selected)
 * renderSelected()}: if you need to do additional styling to reflect a
 * selection, other than changing style classes.</li>
 * <li>optionally, {@link WMenuItem#renderHidden(boolean hidden) renderHidden()}
 * : if you need to do additionanl styling to reflect a hide, other than hiding
 * (see {@link WWebWidget#setHidden(boolean hidden) WWebWidget#setHidden()}).</li>
 * </ul>
 * <p>
 * To provide another look for the close icon you can override
 * <code>Wt-closeicon</code> CSS class (see {@link WMenu} for more details).
 * <p>
 * 
 * @see WMenu
 * @see WMenu#addItem(WMenuItem item)
 */
public class WMenuItem extends WObject {
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
	 * Creates a new item.
	 * <p>
	 * The text specifies the item text. The contents is the widget that must be
	 * shown in the {@link WMenu} contents stack when the item is selected.
	 * <p>
	 * The load policy specifies whether the contents widgets is transmitted
	 * only when it the item is activated for the first time (LazyLoading) or
	 * transmitted prior to first rendering.
	 * <p>
	 * The {@link WMenuItem#getPathComponent() getPathComponent()} is derived
	 * from <code>text</code>, and can be customized using
	 * {@link WMenuItem#setPathComponent(String path) setPathComponent()}.
	 * <p>
	 * <code>contents</code> may be 0, in which case no contents is associated
	 * with the item in the contents stack.
	 */
	public WMenuItem(CharSequence text, WWidget contents,
			WMenuItem.LoadPolicy policy) {
		super();
		this.itemWidget_ = null;
		this.contentsContainer_ = null;
		this.contents_ = contents;
		this.menu_ = null;
		this.text_ = new WString();
		this.tip_ = new WString();
		this.pathComponent_ = "";
		this.customPathComponent_ = false;
		this.closeable_ = false;
		this.disabled_ = false;
		this.hidden_ = false;
		this.setText(text);
		if (policy == WMenuItem.LoadPolicy.PreLoading) {
			// this.implementStateless(WMenuItem.selectVisual,WMenuItem.undoSelectVisual);
		} else {
			if (this.contents_ != null) {
				this.contentsContainer_ = new WContainerWidget();
				this.contentsContainer_.setJavaScriptMember("wtResize",
						StdGridLayoutImpl.getChildrenResizeJS());
				this.addChild(this.contents_);
				;
				this.contentsContainer_.resize(WLength.Auto, new WLength(100,
						WLength.Unit.Percentage));
			}
		}
	}

	/**
	 * Creates a new item.
	 * <p>
	 * Calls
	 * {@link #WMenuItem(CharSequence text, WWidget contents, WMenuItem.LoadPolicy policy)
	 * this(text, contents, WMenuItem.LoadPolicy.LazyLoading)}
	 */
	public WMenuItem(CharSequence text, WWidget contents) {
		this(text, contents, WMenuItem.LoadPolicy.LazyLoading);
	}

	/**
	 * Sets the text for this item.
	 * <p>
	 * Unless a custom path component was defined, the
	 * {@link WMenuItem#getPathComponent() getPathComponent()} is also updated
	 * based on the new text.
	 * <p>
	 * The item widget is updated using
	 * {@link WMenuItem#updateItemWidget(WWidget itemWidget) updateItemWidget()}.
	 * <p>
	 * 
	 * @see WMenuItem#setPathComponent(String path)
	 */
	public void setText(CharSequence text) {
		this.text_ = WString.toWString(text);
		if (!this.customPathComponent_) {
			String result = "";
			if (this.text_.isLiteral()) {
				result = this.text_.toString();
			} else {
				result = this.text_.getKey();
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
			this.pathComponent_ = result;
		}
		if (this.itemWidget_ != null) {
			this.updateItemWidget(this.itemWidget_);
		}
	}

	/**
	 * Returns the text for this item.
	 * <p>
	 * 
	 * @see WMenuItem#setText(CharSequence text)
	 */
	public WString getText() {
		return this.text_;
	}

	/**
	 * Sets the path component for this item.
	 * <p>
	 * The path component is used by the menu item in the application internal
	 * path (see
	 * {@link WApplication#setInternalPath(String path, boolean emitChange)
	 * WApplication#setInternalPath()}), when internal paths are enabled (see
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
		if (this.itemWidget_ != null) {
			this.updateItemWidget(this.itemWidget_);
		}
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
	 * Make it possible to close this item interactively or by
	 * {@link WMenuItem#close() close()}.
	 * <p>
	 * 
	 * @see WMenuItem#close()
	 * @see WMenuItem#isCloseable()
	 */
	public void setCloseable(boolean closeable) {
		this.closeable_ = closeable;
		if (this.menu_ != null) {
			this.menu_.recreateItem(this);
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
	 * @see WMenuItem#hide()
	 */
	public void close() {
		if (this.menu_ != null) {
			this.menu_.close(this);
		}
	}

	/**
	 * Sets whether the item widget is hidden.
	 * <p>
	 * Hides or show the item widget.
	 * <p>
	 * 
	 * @see WMenuItem#hide()
	 * @see WMenuItem#show()
	 * @see WMenuItem#isHidden()
	 */
	public void setHidden(boolean hidden) {
		this.hidden_ = hidden;
		if (this.menu_ != null) {
			this.menu_.doSetHiddenItem(this, this.hidden_);
		}
	}

	/**
	 * Returns whether the item widget is hidden.
	 * <p>
	 * 
	 * @see WMenuItem#setHidden(boolean hidden)
	 */
	public boolean isHidden() {
		return this.hidden_;
	}

	/**
	 * Hides the item widget.
	 * <p>
	 * This calls {@link WMenuItem#setHidden(boolean hidden) setHidden(true)}.
	 * <p>
	 * 
	 * @see WMenuItem#show()
	 */
	public void hide() {
		this.setHidden(true);
	}

	/**
	 * Shows the item widget.
	 * <p>
	 * If the item was previously closed it will be shown.
	 * <p>
	 * This calls {@link WMenuItem#setHidden(boolean hidden) setHidden(false)}.
	 * <p>
	 * 
	 * @see WMenuItem#hide()
	 * @see WMenuItem#select()
	 */
	public void show() {
		this.setHidden(false);
	}

	/**
	 * Enables or disables an item.
	 * <p>
	 * A disabled item cannot be activated.
	 * <p>
	 * 
	 * @see WMenuItem#enable()
	 * @see WMenuItem#disable()
	 */
	public void setDisabled(boolean disabled) {
		this.disabled_ = disabled;
		if (this.menu_ != null) {
			this.menu_.recreateItem(this);
		}
	}

	/**
	 * Returns whether an item is enabled.
	 * <p>
	 * 
	 * @see WMenuItem#setDisabled(boolean disabled)
	 */
	public boolean isDisabled() {
		return this.disabled_;
	}

	/**
	 * Enables the item.
	 * <p>
	 * This calls {@link WMenuItem#setDisabled(boolean disabled)
	 * setDisabled(false)}.
	 * <p>
	 * 
	 * @see WMenuItem#disable()
	 */
	public void enable() {
		this.setDisabled(false);
	}

	/**
	 * Disables the item.
	 * <p>
	 * This calls {@link WMenuItem#setDisabled(boolean disabled)
	 * setDisabled(true)}.
	 * <p>
	 * 
	 * @see WMenuItem#enable()
	 */
	public void disable() {
		this.setDisabled(true);
	}

	/**
	 * Sets a tooltip.
	 * <p>
	 * The tooltip is displayed when the cursor hovers over the label of the
	 * item, i.e. {@link WAnchor} or {@link WText}, depending on whether the
	 * item is enabled or not (see {@link WMenuItem#createItemWidget()
	 * createItemWidget()}).
	 * <p>
	 * 
	 * @see WMenuItem#getToolTip()
	 */
	public void setToolTip(CharSequence tip) {
		this.tip_ = WString.toWString(tip);
		if (this.itemWidget_ != null) {
			this.updateItemWidget(this.itemWidget_);
		}
	}

	/**
	 * Returns the tooltip.
	 */
	public WString getToolTip() {
		return this.tip_;
	}

	/**
	 * Returns the menu.
	 */
	public WMenu getMenu() {
		return this.menu_;
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
		if (!this.isContentsLoaded()) {
			this.removeChild(this.contents_);
		}
		this.contents_ = null;
		return result;
	}

	public void purgeContents() {
		this.contentsContainer_ = null;
		this.contents_ = null;
	}

	/**
	 * Returns the widget that represents the item.
	 * <p>
	 * This returns the item widget, creating it using
	 * {@link WMenuItem#createItemWidget() createItemWidget()} if necessary.
	 */
	public WWidget getItemWidget() {
		if (!(this.itemWidget_ != null)) {
			this.itemWidget_ = this.createItemWidget();
			this.updateItemWidget(this.itemWidget_);
			this.connectSignals();
		}
		return this.itemWidget_;
	}

	/**
	 * Selects this item.
	 * <p>
	 * If the item was previously closed it will be shown.
	 * <p>
	 * 
	 * @see WMenuItem#close()
	 */
	public void select() {
		if (this.menu_ != null) {
			this.menu_.select(this);
		}
	}

	/**
	 * Creates the widget that represents the item.
	 * <p>
	 * The default implementation will return:
	 * <ul>
	 * <li>a {@link WAnchor} if item is not closeable and enabled;</li>
	 * <li>a {@link WText} if item is not closeable and disabled;</li>
	 * <li>a {@link WContainerWidget} with {@link WAnchor} or {@link WText} (the
	 * label of enabled or disabled item accordingly) and {@link WText} (the
	 * close icon) inside if item is closeable.</li>
	 * </ul>
	 * <p>
	 * A call to {@link WMenuItem#createItemWidget() createItemWidget()} is
	 * immediately followed by
	 * {@link WMenuItem#updateItemWidget(WWidget itemWidget) updateItemWidget()}.
	 * <p>
	 * If you reimplement this method, you should probably also reimplement
	 * {@link WMenuItem#updateItemWidget(WWidget itemWidget) updateItemWidget()}.
	 */
	protected WWidget createItemWidget() {
		WAnchor enabledLabel = null;
		WText disabledLabel = null;
		if (!this.disabled_) {
			enabledLabel = new WAnchor();
			enabledLabel.setWordWrap(false);
		} else {
			disabledLabel = new WText("");
			disabledLabel.setWordWrap(false);
		}
		if (this.closeable_) {
			WText closeIcon = new WText("");
			closeIcon.setStyleClass("Wt-closeicon");
			WContainerWidget c = new WContainerWidget();
			c.setInline(true);
			if (enabledLabel != null) {
				enabledLabel.setStyleClass("label");
				c.addWidget(enabledLabel);
			} else {
				disabledLabel.setStyleClass("label");
				c.addWidget(disabledLabel);
			}
			c.addWidget(closeIcon);
			return c;
		} else {
			if (enabledLabel != null) {
				return enabledLabel;
			} else {
				return disabledLabel;
			}
		}
	}

	/**
	 * Updates the widget that represents the item.
	 * <p>
	 * The default implementation will cast the <code>itemWidget</code> to a
	 * {@link WAnchor}, and set the anchor&apos;s text and destination according
	 * to {@link WMenuItem#getText() getText()} and
	 * {@link WMenuItem#getPathComponent() getPathComponent()}.
	 * <p>
	 * 
	 * @see WMenuItem#createItemWidget()
	 */
	protected void updateItemWidget(WWidget itemWidget) {
		WAnchor enabledLabel = null;
		WText disabledLabel = null;
		if (this.closeable_) {
			WContainerWidget c = ((itemWidget) instanceof WContainerWidget ? (WContainerWidget) (itemWidget)
					: null);
			if (!this.disabled_) {
				enabledLabel = ((c.getChildren().get(0)) instanceof WAnchor ? (WAnchor) (c
						.getChildren().get(0))
						: null);
			} else {
				disabledLabel = ((c.getChildren().get(0)) instanceof WText ? (WText) (c
						.getChildren().get(0))
						: null);
			}
		} else {
			if (!this.disabled_) {
				enabledLabel = ((itemWidget) instanceof WAnchor ? (WAnchor) (itemWidget)
						: null);
			} else {
				disabledLabel = ((itemWidget) instanceof WText ? (WText) (itemWidget)
						: null);
			}
		}
		if (enabledLabel != null) {
			enabledLabel.setText(this.getText());
			String url = "";
			if (this.menu_ != null && this.menu_.isInternalPathEnabled()) {
				String internalPath = this.menu_.getInternalBasePath()
						+ this.getPathComponent();
				WApplication app = WApplication.getInstance();
				if (app.getEnvironment().hasAjax()
						|| app.getEnvironment().agentIsSpiderBot()) {
					url = app.getBookmarkUrl(internalPath);
				} else {
					url = app.getSession().getMostRelativeUrl(internalPath);
				}
			} else {
				url = "#";
			}
			enabledLabel.setRef(url);
			enabledLabel.setToolTip(this.getToolTip());
			enabledLabel.clicked().preventDefaultAction();
		} else {
			disabledLabel.setText(this.getText());
			disabledLabel.setToolTip(this.getToolTip());
		}
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
	protected void renderSelected(boolean selected) {
		if (this.closeable_) {
			this.getItemWidget().setStyleClass(
					selected ? "citemselected" : "citem");
		} else {
			this.getItemWidget().setStyleClass(
					selected ? "itemselected" : "item");
		}
	}

	/**
	 * Renders the item as hidden or closed.
	 * <p>
	 * The default implementation hides the item widget (including all its
	 * descendant widgets).
	 */
	protected void renderHidden(boolean hidden) {
		this.getItemWidget().setHidden(hidden);
	}

	/**
	 * Returns the signal used to activate the item.
	 * <p>
	 * The default implementation will tries to cast the
	 * {@link WMenuItem#getItemWidget() getItemWidget()} or its first child if
	 * item is {@link WMenuItem#setCloseable(boolean closeable) closeable} to a
	 * {@link WInteractWidget} and returns the {@link WInteractWidget#clicked()
	 * clicked signal}.
	 */
	protected AbstractSignal activateSignal() {
		WWidget w = null;
		if (this.closeable_) {
			WContainerWidget c = ((this.itemWidget_) instanceof WContainerWidget ? (WContainerWidget) (this.itemWidget_)
					: null);
			w = c.getChildren().get(0);
		} else {
			w = this.itemWidget_;
		}
		WInteractWidget wi = ((w.getWebWidget()) instanceof WInteractWidget ? (WInteractWidget) (w
				.getWebWidget())
				: null);
		if (wi != null) {
			return wi.clicked();
		} else {
			throw new WtException(
					"WMenuItem::activateSignal(): could not dynamic_cast itemWidget() or itemWidget()->children()[0] to a WInteractWidget");
		}
	}

	/**
	 * Returns the signal used to close the item.
	 * <p>
	 * The default implementation will tries to cast the
	 * {@link WMenuItem#getItemWidget() getItemWidget()} (or its second child if
	 * item is {@link WMenuItem#setCloseable(boolean closeable) closeable}) to a
	 * {@link WInteractWidget} and returns the {@link WInteractWidget#clicked()
	 * clicked signal}.
	 */
	protected AbstractSignal closeSignal() {
		WContainerWidget c = ((this.itemWidget_) instanceof WContainerWidget ? (WContainerWidget) (this.itemWidget_)
				: null);
		WInteractWidget ci = ((c.getChildren().get(1)) instanceof WInteractWidget ? (WInteractWidget) (c
				.getChildren().get(1))
				: null);
		if (ci != null) {
			return ci.clicked();
		} else {
			throw new WtException(
					"WMenuItem::closeSignal(): could not dynamic_cast itemWidget()->children()[1] to a WInteractWidget");
		}
	}

	void setFromInternalPath(String path) {
		if (this.menu_.contentsStack_ != null
				&& this.menu_.contentsStack_.getCurrentWidget() != this
						.getContents()) {
			this.menu_.select(this.menu_.indexOf(this), false);
		}
	}

	/**
	 * Progresses to an Ajax-enabled widget.
	 * <p>
	 * This method is called when the progressive bootstrap method is used, and
	 * support for AJAX has been detected. The default behavior will upgrade the
	 * menu and the contents event handling to use AJAX instead of full page
	 * reloads.
	 * <p>
	 * You may want to reimplement this method if you want to make changes to
	 * widget when AJAX is enabled.
	 * <p>
	 * 
	 * @see WMenu#enableAjax()
	 */
	protected void enableAjax() {
		if (!this.isContentsLoaded()) {
			this.contents_.enableAjax();
		}
		if (this.menu_.isInternalPathEnabled()) {
			this.updateItemWidget(this.getItemWidget());
			this.resetLearnedSlots();
		}
	}

	private WWidget itemWidget_;
	private WContainerWidget contentsContainer_;
	private WWidget contents_;
	private WMenu menu_;
	private WString text_;
	private WString tip_;
	private String pathComponent_;
	private boolean customPathComponent_;
	private boolean closeable_;
	private boolean disabled_;
	private boolean hidden_;

	private boolean isContentsLoaded() {
		return !(this.contentsContainer_ != null)
				|| this.contentsContainer_.getCount() == 1;
	}

	void loadContents() {
		if (!this.isContentsLoaded()) {
			this.removeChild(this.contents_);
			this.contentsContainer_.addWidget(this.contents_);
			// this.implementStateless(WMenuItem.selectVisual,WMenuItem.undoSelectVisual);
			this.connectActivate();
		}
	}

	void setMenu(WMenu menu) {
		this.menu_ = menu;
	}

	private void selectNotLoaded() {
		if (!this.isContentsLoaded()) {
			this.select();
		}
	}

	private void selectVisual() {
		if (this.menu_ != null) {
			this.menu_.selectVisual(this);
		}
	}

	private void undoSelectVisual() {
		if (this.menu_ != null) {
			this.menu_.undoSelectVisual();
		}
	}

	private void connectActivate() {
		AbstractSignal as = this.activateSignal();
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

	private void connectClose() {
		AbstractSignal cs = this.closeSignal();
		cs.addListener(this, new Signal.Listener() {
			public void trigger() {
				WMenuItem.this.close();
			}
		});
	}

	private void connectSignals() {
		if (!this.disabled_) {
			if (this.isContentsLoaded()) {
				// this.implementStateless(WMenuItem.selectVisual,WMenuItem.undoSelectVisual);
			}
			this.connectActivate();
		}
		if (this.closeable_) {
			this.connectClose();
		}
	}

	WWidget getRecreateItemWidget() {
		if (this.itemWidget_ != null)
			this.itemWidget_.remove();
		this.itemWidget_ = null;
		return this.getItemWidget();
	}
}
