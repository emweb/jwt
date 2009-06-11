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
 * A menu item that contains a nested sub menu
 * 
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
	 * Create a new item.
	 * 
	 * @see WMenuItem#WMenuItem(CharSequence text, WWidget contents,
	 *      WMenuItem.LoadPolicy policy)
	 */
	public WSubMenuItem(CharSequence text, WWidget contents,
			WMenuItem.LoadPolicy policy) {
		super(text, contents, policy);
	}

	public WSubMenuItem(CharSequence text, WWidget contents) {
		this(text, contents, WMenuItem.LoadPolicy.LazyLoading);
	}

	/**
	 * Set a sub menu.
	 * 
	 * Ownership of the <i>subMenu</i> is transferred to the item. In most
	 * cases, the sub menu would use the same contents stack as the parent menu.
	 * <p>
	 * The default submenu is 0, in which case the item behaves as a plain
	 * {@link WMenuItem}.
	 * <p>
	 * <p>
	 * <i><b>Note:</b>A sub menu can only be set before the item is added to a
	 * menu. </i>
	 * </p>
	 */
	public void setSubMenu(WMenu subMenu) {
		this.subMenu_ = subMenu;
	}

	/**
	 * Return the sub menu.
	 * 
	 * @see WSubMenuItem#setSubMenu(WMenu subMenu)
	 */
	public WMenu getSubMenu() {
		return this.subMenu_;
	}

	protected WWidget createItemWidget() {
		if (this.subMenu_ != null) {
			WContainerWidget contents = new WContainerWidget();
			WWidget anchor = super.createItemWidget();
			contents.addWidget(anchor);
			contents.addWidget(this.subMenu_);
			this.subMenu_.hide();
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
		if (this.subMenu_ != null) {
			this.subMenu_.setHidden(!selected);
		}
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

	private WMenu subMenu_;
}
