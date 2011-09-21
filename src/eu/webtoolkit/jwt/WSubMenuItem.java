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
 * A menu item that contains a nested sub menu.
 * <p>
 * 
 * This class specializes menu item to have an optional sub menu.
 * <p>
 * When the item is shown and hidden when the item is selected respectively
 * deselected.
 * <p>
 * 
 * @see WMenuItem
 * @see WMenu
 */
public class WSubMenuItem extends WMenuItem {
	/**
	 * Creates a new item.
	 * <p>
	 * 
	 * @see WMenuItem#WMenuItem(CharSequence text, WWidget contents,
	 *      WMenuItem.LoadPolicy policy)
	 */
	public WSubMenuItem(CharSequence text, WWidget contents,
			WMenuItem.LoadPolicy policy) {
		super(text, contents, policy);
		this.subMenu_ = null;
	}

	/**
	 * Creates a new item.
	 * <p>
	 * Calls
	 * {@link #WSubMenuItem(CharSequence text, WWidget contents, WMenuItem.LoadPolicy policy)
	 * this(text, contents, WMenuItem.LoadPolicy.LazyLoading)}
	 */
	public WSubMenuItem(CharSequence text, WWidget contents) {
		this(text, contents, WMenuItem.LoadPolicy.LazyLoading);
	}

	/**
	 * Sets a sub menu.
	 * <p>
	 * Ownership of the <code>subMenu</code> is transferred to the item. In most
	 * cases, the sub menu would use the same contents stack as the parent menu.
	 * <p>
	 * The default submenu is <code>null</code>, in which case the item behaves
	 * as a plain {@link WMenuItem}.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>A sub menu can only be set before the item is added to a
	 * menu. </i>
	 * </p>
	 */
	public void setSubMenu(WMenu subMenu) {
		this.subMenu_ = subMenu;
		this.subMenu_.setSubMenu(true);
	}

	/**
	 * Returns the sub menu.
	 * <p>
	 * 
	 * @see WSubMenuItem#setSubMenu(WMenu subMenu)
	 */
	public WMenu getSubMenu() {
		return this.subMenu_;
	}

	public String getPathComponent() {
		return super.getPathComponent() + "/";
	}

	protected WWidget createItemWidget() {
		if (this.subMenu_ != null) {
			WContainerWidget contents = new WContainerWidget();
			WWidget anchor = super.createItemWidget();
			contents.addWidget(anchor);
			contents.addWidget(this.subMenu_);
			return contents;
		} else {
			return super.createItemWidget();
		}
	}

	protected void updateItemWidget(WWidget itemWidget) {
		if (this.subMenu_ != null) {
			WContainerWidget contents = ((itemWidget) instanceof WContainerWidget ? (WContainerWidget) (itemWidget)
					: null);
			WWidget anchor = contents.getWidget(0);
			super.updateItemWidget(anchor);
		} else {
			super.updateItemWidget(itemWidget);
		}
	}

	protected void renderSelected(boolean selected) {
		super.renderSelected(selected);
	}

	protected AbstractSignal activateSignal() {
		if (this.subMenu_ != null) {
			WContainerWidget contents = ((this.getItemWidget()) instanceof WContainerWidget ? (WContainerWidget) (this
					.getItemWidget())
					: null);
			WInteractWidget wi = ((contents.getWidget(0).getWebWidget()) instanceof WInteractWidget ? (WInteractWidget) (contents
					.getWidget(0).getWebWidget())
					: null);
			return wi.clicked();
		} else {
			return super.activateSignal();
		}
	}

	void setFromInternalPath(String path) {
		super.setFromInternalPath(path);
		if (this.subMenu_ != null && this.subMenu_.isInternalPathEnabled()) {
			this.subMenu_.internalPathChanged(path);
		}
	}

	private WMenu subMenu_;
	// private void subItemSelected() ;
}
