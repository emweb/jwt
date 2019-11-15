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
 * A split button.
 * <p>
 * 
 * A split button combines a button and a drop down menu. Typically, the button
 * represents an action, with related alternative actions accessible from the
 * drop down menu.
 */
public class WSplitButton extends WCompositeWidget {
	private static Logger logger = LoggerFactory.getLogger(WSplitButton.class);

	/**
	 * Constructor.
	 */
	public WSplitButton(WContainerWidget parent) {
		super(parent);
		this.init(WString.Empty);
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls {@link #WSplitButton(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WSplitButton() {
		this((WContainerWidget) null);
	}

	/**
	 * Constructor passing the label.
	 */
	public WSplitButton(final CharSequence label, WContainerWidget parent) {
		super(parent);
		this.init(label);
	}

	/**
	 * Constructor passing the label.
	 * <p>
	 * Calls {@link #WSplitButton(CharSequence label, WContainerWidget parent)
	 * this(label, (WContainerWidget)null)}
	 */
	public WSplitButton(final CharSequence label) {
		this(label, (WContainerWidget) null);
	}

	/**
	 * Returns the action button.
	 * <p>
	 * 
	 * This is the button that represents the main action.
	 */
	public WPushButton getActionButton() {
		return ((this.impl_.widget(0)) instanceof WPushButton ? (WPushButton) (this.impl_
				.widget(0)) : null);
	}

	/**
	 * Returns the drop down button.
	 * <p>
	 * 
	 * This represents the button that represents the drop-down action.
	 */
	public WPushButton getDropDownButton() {
		return ((this.impl_.widget(1)) instanceof WPushButton ? (WPushButton) (this.impl_
				.widget(1)) : null);
	}

	/**
	 * Sets the menu for the drop-down button.
	 */
	public void setMenu(WPopupMenu popupMenu) {
		this.getDropDownButton().setMenu(popupMenu);
	}

	/**
	 * Returns the menu for the drop-down button.
	 */
	public WPopupMenu getMenu() {
		return this.getDropDownButton().getMenu();
	}

	private WToolBar impl_;

	private void init(final CharSequence label) {
		this.setImplementation(this.impl_ = new WToolBar());
		this.impl_.setInline(true);
		this.impl_.addButton(new WPushButton(label));
		this.impl_.addButton(new WPushButton());
		this.getDropDownButton().setStyleClass("dropdown-toggle");
	}
}
