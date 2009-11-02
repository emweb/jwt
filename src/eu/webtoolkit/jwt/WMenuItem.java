/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * A single item in a menu
 * <p>
 * 
 * The item determines the look and behaviour of a single item in a
 * {@link WMenu}.
 * <p>
 * By default, a {@link WMenuItem} is implemented using a {@link WAnchor}
 * widget. When the menu participates in application internal paths (see
 * {@link WMenu#setInternalPathEnabled(String basePath)
 * WMenu#setInternalPathEnabled()}), the anchor references the bookmark URL
 * corresponding to the {@link WMenuItem#getPathComponent() getPathComponent()}
 * for the item (see {@link WApplication#getBookmarkUrl()
 * WApplication#getBookmarkUrl()}.
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
 * <li>optionally, {@link WMenuItem#renderSelected(boolean selected)
 * renderSelected()}: if you need to do additional styling to reflect a
 * selection, other than changing style classes.</li>
 * </ul>
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
	 * Create a new item.
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
		this.text_ = new WString();
		this.pathComponent_ = "";
		this.customPathComponent_ = false;
		this.setText(text);
		if (policy == WMenuItem.LoadPolicy.PreLoading) {
			// this.implementStateless(WMenuItem.selectVisual,WMenuItem.undoSelectVisual);
		} else {
			this.contentsContainer_ = new WContainerWidget();
			;
			this.contentsContainer_.resize(WLength.Auto, new WLength(100,
					WLength.Unit.Percentage));
		}
	}

	/**
	 * Create a new item.
	 * <p>
	 * Calls
	 * {@link #WMenuItem(CharSequence text, WWidget contents, WMenuItem.LoadPolicy policy)
	 * this(text, contents, WMenuItem.LoadPolicy.LazyLoading)}
	 */
	public WMenuItem(CharSequence text, WWidget contents) {
		this(text, contents, WMenuItem.LoadPolicy.LazyLoading);
	}

	/**
	 * Set the text for this item.
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
	 * Return the text for this item.
	 * <p>
	 * 
	 * @see WMenuItem#setText(CharSequence text)
	 */
	public WString getText() {
		return this.text_;
	}

	/**
	 * Set the path component for this item.
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
	 * Returns the menu.
	 */
	public WMenu getMenu() {
		return this.menu_;
	}

	/**
	 * Returs the contents widget for this item.
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
		this.contents_ = null;
		return result;
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
			this.connectActivate();
		}
		return this.itemWidget_;
	}

	/**
	 * Select this item.
	 */
	public void select() {
		this.menu_.select(this);
	}

	/**
	 * Create the widget that represents the item.
	 * <p>
	 * The default implementation will simply return a {@link WAnchor}. A call
	 * to {@link WMenuItem#createItemWidget() createItemWidget()} is immediately
	 * followed by {@link WMenuItem#updateItemWidget(WWidget itemWidget)
	 * updateItemWidget()}.
	 * <p>
	 * If you reimplement this method, you should probably also reimplement
	 * {@link WMenuItem#updateItemWidget(WWidget itemWidget) updateItemWidget()}.
	 */
	protected WWidget createItemWidget() {
		WAnchor result = new WAnchor();
		result.setWordWrap(false);
		return result;
	}

	/**
	 * Update the widget that represents the item.
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
		WAnchor a = ((itemWidget) instanceof WAnchor ? (WAnchor) (itemWidget)
				: null);
		if (a != null) {
			a.setText(this.getText());
			String url = "";
			if (this.menu_.isInternalPathEnabled()) {
				url = WApplication.getInstance().getBookmarkUrl(
						this.menu_.getInternalBasePath()
								+ this.getPathComponent());
			} else {
				url = "#";
			}
			a.setRef(url);
			a.clicked().setPreventDefault(true);
		}
	}

	/**
	 * Render the item as selected or unselected.
	 * <p>
	 * The default implementation sets the styleclass for
	 * {@link WMenuItem#getItemWidget() getItemWidget()} to &apos;item&apos; for
	 * an unselected, and &apos;itemselected&apos; for a selected item.
	 * <p>
	 * Note that this method is called from within a stateless slot
	 * implementation, and thus should be stateless as well.
	 */
	protected void renderSelected(boolean selected) {
		this.getItemWidget().setStyleClass(selected ? "itemselected" : "item");
	}

	/**
	 * Returns the signal used to activate the item.
	 * <p>
	 * The default implementation will tries to cast the
	 * {@link WMenuItem#getItemWidget() getItemWidget()} to a
	 * {@link WInteractWidget} and returns the {@link WInteractWidget#clicked()
	 * clicked signal}.
	 */
	protected AbstractSignal activateSignal() {
		WInteractWidget wi = ((this.itemWidget_.getWebWidget()) instanceof WInteractWidget ? (WInteractWidget) (this.itemWidget_
				.getWebWidget())
				: null);
		if (wi != null) {
			return wi.clicked();
		} else {
			throw new WtException(
					"WMenuItem::activateSignal(): could not dynamic_cast itemWidget() to a WInteractWidget");
		}
	}

	private WWidget itemWidget_;
	private WContainerWidget contentsContainer_;
	private WWidget contents_;
	private WMenu menu_;
	private WString text_;
	private String pathComponent_;
	private boolean customPathComponent_;

	void loadContents() {
		if (this.contentsContainer_ != null
				&& this.contentsContainer_.getCount() == 0) {
			this.contentsContainer_.addWidget(this.contents_);
			// this.implementStateless(WMenuItem.selectVisual,WMenuItem.undoSelectVisual);
			this.connectActivate();
		}
	}

	void setMenu(WMenu menu) {
		this.menu_ = menu;
	}

	private void selectNotLoaded() {
		if (this.contentsContainer_ != null
				&& this.contentsContainer_.getCount() == 0) {
			this.select();
		}
	}

	private void selectVisual() {
		this.menu_.selectVisual(this);
	}

	private void undoSelectVisual() {
		this.menu_.undoSelectVisual();
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
}
