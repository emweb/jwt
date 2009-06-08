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
 * A single item in a menu
 * 
 * 
 * The item determines the look and behaviour of a single item in a
 * {@link WMenu}.
 * <p>
 * By default, a {@link WMenuItem} is implemented using a {@link WAnchor}
 * widget. When the menu participates in application internal paths (see
 * {@link WMenu#setInternalPathEnabled()}), the anchor references the bookmark
 * URL corresponding to the {@link WMenuItem#getPathComponent()} for the item
 * (see {@link WApplication#getBookmarkUrl()}.
 * <p>
 * To provide another look for the menu items (such as perhaps adding an icon),
 * you can specialize this class, and reimplement the virtual methods:
 * <p>
 * <ul>
 * <li>{@link WMenuItem#createItemWidget()}: to provide another widget to
 * represent the item.</li>
 * <li>{@link WMenuItem#updateItemWidget(WWidget itemWidget)}: to update the
 * widget to reflect item changes, triggered by for example
 * {@link WMenuItem#setText(CharSequence text)} and
 * {@link WMenuItem#setPathComponent(String path)}.</li>
 * <li>optionally, {@link WMenuItem#activateSignal()}: to bind the event for
 * activating the item to something else than the clicked event.</li>
 * <li>optionally, {@link WMenuItem#renderSelected(boolean selected)}: if you
 * need to do additional styling to reflect a selection, other than changing
 * style classes.</li>
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

		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Create a new item.
	 * 
	 * The text specifies the item text. The contents is the widget that must be
	 * shown in the {@link WMenu} contents stack when the item is selected.
	 * <p>
	 * The load policy specifies whether the contents widgets is transmitted
	 * only when it the item is activated for the first time (LazyLoading) or
	 * transmitted prior to first rendering.
	 * <p>
	 * The {@link WMenuItem#getPathComponent()} is derived from <i>text</i>, and
	 * can be customized using {@link WMenuItem#setPathComponent(String path)}.
	 * <p>
	 * <i>contents</i> may be 0, in which case no contents is associated with
	 * the item in the contents stack.
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
			this.contentsContainer_.resize(WLength.Auto, new WLength(100,
					WLength.Unit.Percentage));
		}
	}

	public WMenuItem(CharSequence text, WWidget contents) {
		this(text, contents, WMenuItem.LoadPolicy.LazyLoading);
	}

	public void destroy() {
		if (this.menu_ != null) {
			this.menu_.removeItem(this);
		}
		if (this.contents_ != null && this.contents_.getParent() == null) {
			if (this.contents_ != null)
				this.contents_.remove();
		}
	}

	/**
	 * Set the text for this item.
	 * 
	 * Unless a custom path component was defined, the
	 * {@link WMenuItem#getPathComponent()} is also updated based on the new
	 * text.
	 * <p>
	 * The item widget is updated using
	 * {@link WMenuItem#updateItemWidget(WWidget itemWidget)}.
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
	 * 
	 * @see WMenuItem#setText(CharSequence text)
	 */
	public WString getText() {
		return this.text_;
	}

	/**
	 * Set the path component for this item.
	 * 
	 * The path component is used by the menu item in the application internal
	 * path (see
	 * {@link WApplication#setInternalPath(String path, boolean emitChange)}),
	 * when internal paths are enabled (see
	 * {@link WMenu#setInternalPathEnabled()}) for the menu.
	 * <p>
	 * You may specify an empty <i>path</i> to let a menu item be the
	 * &quot;default&quot; menu option.
	 * <p>
	 * For example, if {@link WMenu#getInternalBasePath()} is
	 * <code>&quot;/examples/&quot;</code> and
	 * {@link WMenuItem#getPathComponent()} for is
	 * <code>&quot;charts/&quot;</code>, then the internal path for the item
	 * will be <code>&quot;/examples/charts/&quot;</code>.
	 * <p>
	 * By default, the path is automatically derived from
	 * {@link WMenuItem#getText()}. If a {@link WString#literal() literal text}
	 * is used, the path is based on the text itself, otherwise on the
	 * {@link WString#key() key}. It is converted to lower case, and replacing
	 * white space and special characters with &apos;_&apos;.
	 * <p>
	 * 
	 * @see WMenuItem#setText(CharSequence text)
	 * @see WMenu#setInternalPathEnabled()
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
	 * 
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
	 * 
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

	public WWidget getTakeContents() {
		WWidget result = this.contents_;
		this.contents_ = null;
		return result;
	}

	/**
	 * Returns the widget that represents the item.
	 * 
	 * This returns the item widget, creating it using
	 * {@link WMenuItem#createItemWidget()} if necessary.
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
	 * 
	 * The default implementation will simply return a {@link WAnchor}. A call
	 * to {@link WMenuItem#createItemWidget()} is immediately followed by
	 * {@link WMenuItem#updateItemWidget(WWidget itemWidget)}.
	 * <p>
	 * If you reimplement this method, you should probably also reimplement
	 * {@link WMenuItem#updateItemWidget(WWidget itemWidget)}.
	 */
	protected WWidget createItemWidget() {
		WAnchor result = new WAnchor();
		result.setWordWrap(false);
		return result;
	}

	/**
	 * Update the widget that represents the item.
	 * 
	 * The default implementation will cast the <i>itemWidget</i> to a
	 * {@link WAnchor}, and set the anchor&apos;s text and destination according
	 * to {@link WMenuItem#getText()} and {@link WMenuItem#getPathComponent()}.
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
				url = WApplication.instance().getBookmarkUrl(
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
	 * 
	 * The default implementation sets the styleclass for
	 * {@link WMenuItem#getItemWidget()} to &apos;item&apos; for an unselected,
	 * and &apos;itemselected&apos; for a selected item.
	 * <p>
	 * Note that this method is called from within a stateless slot
	 * implementation, and thus should be stateless as well.
	 */
	protected void renderSelected(boolean selected) {
		this.getItemWidget().setStyleClass(selected ? "itemselected" : "item");
	}

	/**
	 * Returns the signal used to activate the item.
	 * 
	 * The default implementation will tries to cast the
	 * {@link WMenuItem#getItemWidget()} to a {@link WInteractWidget} and
	 * returns the {@link WInteractWidget#clicked() clicked signal}.
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
			this.select();
		}
	}

	void setMenu(WMenu menu) {
		this.menu_ = menu;
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
					WMenuItem.this.loadContents();
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
