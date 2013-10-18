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
 * A toolbar.
 * <p>
 * 
 * By default, a toolbar is rendered as &quot;compact&quot; leaving no margin
 * between buttons. By adding a separator or a split button, the toolbar also
 * supports separation between buttons.
 */
public class WToolBar extends WCompositeWidget {
	private static Logger logger = LoggerFactory.getLogger(WToolBar.class);

	/**
	 * Constructor.
	 */
	public WToolBar(WContainerWidget parent) {
		super(parent);
		this.compact_ = true;
		this.lastGroup_ = null;
		this.setImplementation(this.impl_ = new WContainerWidget());
		this.setStyleClass("btn-group");
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls {@link #WToolBar(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WToolBar() {
		this((WContainerWidget) null);
	}

	/**
	 * Set vertical or horizontal orientation.
	 * <p>
	 * Use bootstrap btn-group-vertical style for vertical orientation.
	 */
	public void setOrientation(Orientation orientation) {
		if (orientation == Orientation.Vertical) {
			this.addStyleClass("btn-group-vertical");
		} else {
			this.removeStyleClass("btn-group-vertical");
		}
	}

	/**
	 * Adds a button.
	 */
	public void addButton(WPushButton button, AlignmentFlag alignmentFlag) {
		if (this.compact_) {
			this.impl_.addWidget(button);
			if (alignmentFlag == AlignmentFlag.AlignRight) {
				button.setAttributeValue("style", "float:right;");
			}
		} else {
			if (alignmentFlag == AlignmentFlag.AlignRight) {
				this.getLastGroup().setAttributeValue("style", "float:right;");
			}
			this.getLastGroup().addWidget(button);
		}
	}

	/**
	 * Adds a button.
	 * <p>
	 * Calls {@link #addButton(WPushButton button, AlignmentFlag alignmentFlag)
	 * addButton(button, AlignmentFlag.AlignLeft)}
	 */
	public final void addButton(WPushButton button) {
		addButton(button, AlignmentFlag.AlignLeft);
	}

	/**
	 * Adds a split button.
	 * <p>
	 * When adding a split button, the toolbar automatically becomes
	 * non-compact, since otherwise the split button functionality cannot be
	 * distinguished from other buttons.
	 * <p>
	 * 
	 * @see WToolBar#setCompact(boolean compact)
	 */
	public void addButton(WSplitButton button) {
		this.setCompact(false);
		this.lastGroup_ = null;
		this.impl_.addWidget(button);
	}

	/**
	 * Adds a separator.
	 * <p>
	 * The toolbar automatically becomes non-compact.
	 * <p>
	 * 
	 * @see WToolBar#setCompact(boolean compact)
	 */
	public void addSeparator() {
		this.setCompact(false);
		this.lastGroup_ = null;
	}

	/**
	 * Returns the number of buttons.
	 * <p>
	 * 
	 * @see WToolBar#widget(int index)
	 */
	public int getCount() {
		if (this.compact_) {
			return this.impl_.getCount();
		} else {
			int result = 0;
			for (int i = 0; i < this.impl_.getCount(); ++i) {
				WWidget w = this.impl_.getWidget(i);
				if (((w) instanceof WSplitButton ? (WSplitButton) (w) : null) != null) {
					++result;
				} else {
					WContainerWidget group = ((w) instanceof WContainerWidget ? (WContainerWidget) (w)
							: null);
					result += group.getCount();
				}
			}
			return result;
		}
	}

	/**
	 * Returns a button.
	 * <p>
	 * The returned button is a {@link WPushButton} or {@link WSplitButton}.
	 */
	public WWidget widget(int index) {
		if (this.compact_) {
			return this.impl_.getWidget(index);
		} else {
			int current = 0;
			for (int i = 0; i < this.impl_.getCount(); ++i) {
				WWidget w = this.impl_.getWidget(i);
				if (((w) instanceof WSplitButton ? (WSplitButton) (w) : null) != null) {
					if (index == current) {
						return w;
					}
					++current;
				} else {
					WContainerWidget group = ((w) instanceof WContainerWidget ? (WContainerWidget) (w)
							: null);
					if (index < current + group.getCount()) {
						return group.getWidget(index - current);
					}
					current += group.getCount();
				}
			}
			return null;
		}
	}

	/**
	 * Sets the toolbar to be rendered compact.
	 * <p>
	 * The default value is <code>true</code>, but <code>setCompact(true)</code>
	 * is called automatically when calling
	 * {@link WToolBar#addButton(WSplitButton button) addButton()} or
	 * {@link WToolBar#addSeparator() addSeparator()}.
	 */
	public void setCompact(boolean compact) {
		if (compact != this.compact_) {
			this.compact_ = compact;
			if (compact) {
				if (this.impl_.getCount() > 0) {
					logger.info(new StringWriter().append(
							"setCompact(true): not implemented").toString());
				}
				this.setStyleClass("btn-group");
			} else {
				this.setStyleClass("btn-toolbar");
				WContainerWidget group = new WContainerWidget();
				group.setStyleClass("btn-group");
				while (this.impl_.getCount() > 0) {
					WWidget w = this.impl_.getWidget(0);
					this.impl_.removeWidget(w);
					group.addWidget(w);
				}
				this.impl_.addWidget(group);
				this.lastGroup_ = group;
			}
		}
	}

	/**
	 * Returns whether the toolbar was rendered compact.
	 * <p>
	 * 
	 * @see WToolBar#setCompact(boolean compact)
	 */
	public boolean isCompact() {
		return this.compact_;
	}

	private boolean compact_;
	private WContainerWidget impl_;
	private WContainerWidget lastGroup_;

	private WContainerWidget getLastGroup() {
		if (!(this.lastGroup_ != null)) {
			this.lastGroup_ = new WContainerWidget(this.impl_);
			this.lastGroup_.addStyleClass("btn-group");
		}
		return this.lastGroup_;
	}
}
