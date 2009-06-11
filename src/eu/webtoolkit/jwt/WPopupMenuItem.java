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
 * An item in a popup menu
 * 
 * 
 * An item may have a text, icon, and can be checkable or lead to a submenu.
 * <p>
 * When the mouse hovers over the item, its class is changed from
 * &quot;unselected&quot; to &quot;selected&quot;.
 * <p>
 * 
 * @see WPopupMenu
 */
public class WPopupMenuItem extends WCompositeWidget {
	/**
	 * Create a new item with given text.
	 * 
	 * @see WPopupMenu#addItem(CharSequence text)
	 */
	public WPopupMenuItem(CharSequence text) {
		super();
		this.text_ = null;
		this.checkBox_ = null;
		this.subMenu_ = null;
		this.data_ = null;
		this.separator_ = false;
		this.triggered_ = new Signal(this);
		this.create();
		this.setText(text);
	}

	/**
	 * Create a new item with given icon and text.
	 * 
	 * The icon is displayed left to the text.
	 * <p>
	 * <p>
	 * <i><b>Note:</b>The icon should have a width of 16 pixels.</i>
	 * </p>
	 * 
	 * @see WPopupMenu#addItem(String iconPath, CharSequence text)
	 */
	public WPopupMenuItem(String iconPath, CharSequence text) {
		super();
		this.text_ = null;
		this.checkBox_ = null;
		this.subMenu_ = null;
		this.data_ = null;
		this.separator_ = false;
		this.triggered_ = new Signal(this);
		this.create();
		this.setText(text);
		if (iconPath.length() != 0) {
			this.setIcon(iconPath);
		}
	}

	/**
	 * Destructor.
	 */
	public void remove() {
		if (this.subMenu_ != null)
			this.subMenu_.remove();
		super.remove();
	}

	/**
	 * Set the item text.
	 * 
	 * @see WPopupMenuItem#setIcon(String path)
	 */
	public void setText(CharSequence text) {
		if (!(this.text_ != null)) {
			this.text_ = new WText(this.impl_);
			this.text_
					.setMargin(new WLength(ICON_WIDTH), EnumSet.of(Side.Left));
			this.text_.setAttributeValue("style", "padding-right: "
					+ String.valueOf(SUBMENU_ARROW_WIDTH) + "px");
		}
		this.text_.setText(text);
	}

	/**
	 * Returns the item text.
	 * 
	 * @see WPopupMenuItem#setText(CharSequence text)
	 */
	public WString getText() {
		return this.text_.getText();
	}

	/**
	 * Set the item icon path.
	 * 
	 * The icon should have a width of 16 pixels.
	 * <p>
	 * 
	 * @see WPopupMenuItem#setText(CharSequence text)
	 */
	public void setIcon(String path) {
		this.getDecorationStyle().setBackgroundImage(path,
				WCssDecorationStyle.Repeat.NoRepeat, EnumSet.of(Side.CenterY));
		this.setAttributeValue("style", "background-position: 3px center");
	}

	/**
	 * Returns the item icon path.
	 * 
	 * @see WPopupMenuItem#setIcon(String path)
	 */
	public String getIcon() {
		return this.getDecorationStyle().getBackgroundImage();
	}

	/**
	 * Set if the item is checkable.
	 * 
	 * When an item is checkable, a checkbox is displayed to the left of the
	 * item text (instead of an icon).
	 * <p>
	 * 
	 * @see WPopupMenuItem#setChecked(boolean how)
	 * @see WPopupMenuItem#isChecked()
	 */
	public void setCheckable(boolean how) {
		if (this.isCheckable() != how) {
			if (how) {
				this.text_.setMargin(new WLength(ICON_WIDTH - CHECKBOX_WIDTH),
						EnumSet.of(Side.Left));
				this.checkBox_ = new WCheckBox(false, (WContainerWidget) null);
				this.impl_.insertWidget(0, this.checkBox_);
			} else {
				if (this.checkBox_ != null)
					this.checkBox_.remove();
				this.text_.setMargin(new WLength(ICON_WIDTH), EnumSet
						.of(Side.Left));
			}
		}
	}

	/**
	 * Returns whether the item is checkable.
	 * 
	 * @see WPopupMenuItem#setCheckable(boolean how)
	 */
	public boolean isCheckable() {
		return this.checkBox_ != null;
	}

	/**
	 * Set a sub menu for the item.
	 * 
	 * Sets a submenu for the item. Ownership of the submenu is transferred to
	 * the item.
	 * <p>
	 * 
	 * @see WPopupMenuItem#getPopupMenu()
	 */
	public void setPopupMenu(WPopupMenu menu) {
		if (this.subMenu_ != null)
			this.subMenu_.remove();
		this.subMenu_ = menu;
		String resources = WApplication.getResourcesUrl();
		if (this.subMenu_ != null) {
			this.subMenu_.parentItem_ = this;
			this.text_.getDecorationStyle().setBackgroundImage(
					resources + "right-arrow.gif",
					WCssDecorationStyle.Repeat.NoRepeat,
					EnumSet.of(Side.Right, Side.CenterY));
		}
	}

	/**
	 * Returns the sub menu.
	 * 
	 * @see WPopupMenuItem#setPopupMenu(WPopupMenu menu)
	 */
	public WPopupMenu getPopupMenu() {
		return this.subMenu_;
	}

	/**
	 * Change the checked state.
	 * 
	 * This is only used when {@link WPopupMenuItem#isCheckable()} == true.
	 * <p>
	 * 
	 * @see WPopupMenuItem#setCheckable(boolean how)
	 * @see WPopupMenuItem#isCheckable()
	 */
	public void setChecked(boolean how) {
		if (this.checkBox_ != null) {
			this.checkBox_.setChecked(how);
		}
	}

	/**
	 * Return the checked state.
	 * 
	 * This is only used when {@link WPopupMenuItem#isCheckable()} == true.
	 * <p>
	 * 
	 * @see WPopupMenuItem#setChecked(boolean how)
	 * @see WPopupMenuItem#isCheckable()
	 */
	public boolean isChecked() {
		return this.checkBox_ != null ? this.checkBox_.isChecked() : false;
	}

	/**
	 * Associated additional data with the item.
	 */
	public void setData(Object data) {
		this.data_ = data;
	}

	/**
	 * Returns additional data of the item.
	 */
	public Object getData() {
		return this.data_;
	}

	public WCheckBox getCheckBox() {
		return this.checkBox_;
	}

	/**
	 * Signal emitted when an item is activated.
	 */
	public Signal triggered() {
		return this.triggered_;
	}

	public void load() {
		super.load();
		this.impl_.mouseWentOver().addListener(this.getParentMenu(),
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WPopupMenuItem.this.getParentMenu().show();
					}
				});
		this.impl_.mouseWentOver().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WPopupMenuItem.this.renderOver();
					}
				});
		this.impl_.mouseWentOver().setNotExposed();
	}

	WPopupMenuItem(boolean anon1) {
		super();
		this.text_ = null;
		this.checkBox_ = null;
		this.subMenu_ = null;
		this.data_ = null;
		this.separator_ = true;
		this.triggered_ = new Signal(this);
		this.setImplementation(this.impl_ = new WContainerWidget());
		this.setStyleClass("separator");
	}

	private WContainerWidget impl_;
	private WText text_;
	private WCheckBox checkBox_;
	private WPopupMenu subMenu_;
	private Object data_;
	private boolean separator_;
	private Signal triggered_;

	private void create() {
		this.setImplementation(this.impl_ = new WContainerWidget());
		// this.implementStateless(WPopupMenuItem.renderOver,WPopupMenuItem.renderOut);
		this.impl_.mouseWentUp().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WPopupMenuItem.this.onMouseUp();
					}
				});
		this.setStyleClass("notselected");
	}

	private void renderOver() {
		this.renderSelected(true);
	}

	void renderOut() {
		this.renderSelected(false);
	}

	private void renderSelected(boolean selected) {
		if (this.separator_) {
			return;
		}
		this.setStyleClass(selected ? "selected" : "notselected");
		if (this.subMenu_ != null) {
			if (selected) {
				this.subMenu_.popup(this);
			} else {
				this.subMenu_.show();
				this.subMenu_.hide();
			}
		}
	}

	private void onMouseUp() {
		if (this.subMenu_ != null) {
			return;
		}
		if (this.checkBox_ != null) {
			this.checkBox_.setChecked(!this.checkBox_.isChecked());
		}
		this.triggered_.trigger();
		this.getTopLevelMenu().done(this);
	}

	private WPopupMenu getParentMenu() {
		return ((this.getParent().getParent()) instanceof WPopupMenu ? (WPopupMenu) (this
				.getParent().getParent())
				: null);
	}

	WPopupMenu getTopLevelMenu() {
		return this.getParentMenu().getTopLevelMenu();
	}

	static int ICON_WIDTH = 24;
	static int CHECKBOX_WIDTH = 20;
	static int SUBMENU_ARROW_WIDTH = 24;
}
