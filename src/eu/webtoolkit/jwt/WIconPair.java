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
 * A widget that shows one of two icons depending on its state.
 * <p>
 * 
 * This is a utility class that simply manages two images, only one of which is
 * shown at a single time, which reflects the current &apos;state&apos;.
 * <p>
 * The widget may react to click events, by changing state.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * This widget does not provide styling, and can be styled using inline or
 * external CSS as appropriate. The image may be styled via the
 * <code>&lt;img&gt;</code> elements.
 */
public class WIconPair extends WCompositeWidget {
	/**
	 * Construct an icon pair from the two icons.
	 * <p>
	 * The constructor takes the URL of the two icons. When
	 * <code>clickIsSwitch</code> is set <code>true</code>, clicking on the icon
	 * will switch state.
	 */
	public WIconPair(String icon1URI, String icon2URI, boolean clickIsSwitch,
			WContainerWidget parent) {
		super(parent);
		this.impl_ = new WContainerWidget();
		this.icon1_ = new WImage(icon1URI, this.impl_);
		this.icon2_ = new WImage(icon2URI, this.impl_);
		this.setImplementation(this.impl_);
		this.impl_.setLoadLaterWhenInvisible(false);
		this.setInline(true);
		this.icon2_.hide();
		if (clickIsSwitch) {
			this.icon1_.clicked().preventPropagation();
			this.icon2_.clicked().preventPropagation();
			this.icon1_.clicked().addListener(this,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent e1) {
							WIconPair.this.showIcon2();
						}
					});
			this.icon2_.clicked().addListener(this,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent e1) {
							WIconPair.this.showIcon1();
						}
					});
			this.getDecorationStyle().setCursor(Cursor.PointingHandCursor);
		}
	}

	/**
	 * Construct an icon pair from the two icons.
	 * <p>
	 * Calls
	 * {@link #WIconPair(String icon1URI, String icon2URI, boolean clickIsSwitch, WContainerWidget parent)
	 * this(icon1URI, icon2URI, true, (WContainerWidget)null)}
	 */
	public WIconPair(String icon1URI, String icon2URI) {
		this(icon1URI, icon2URI, true, (WContainerWidget) null);
	}

	/**
	 * Construct an icon pair from the two icons.
	 * <p>
	 * Calls
	 * {@link #WIconPair(String icon1URI, String icon2URI, boolean clickIsSwitch, WContainerWidget parent)
	 * this(icon1URI, icon2URI, clickIsSwitch, (WContainerWidget)null)}
	 */
	public WIconPair(String icon1URI, String icon2URI, boolean clickIsSwitch) {
		this(icon1URI, icon2URI, clickIsSwitch, (WContainerWidget) null);
	}

	/**
	 * Sets the state, which determines the visible icon.
	 * <p>
	 * The first icon has number 0, and the second icon has number 1.
	 * <p>
	 * The default state is 0.
	 * <p>
	 * 
	 * @see WIconPair#getState()
	 */
	public void setState(int num) {
		if (num == 0) {
			this.icon1_.show();
			this.icon2_.hide();
		} else {
			this.icon1_.hide();
			this.icon2_.show();
		}
	}

	/**
	 * Returns the current state.
	 * <p>
	 * 
	 * @see WIconPair#setState(int num)
	 */
	public int getState() {
		return this.icon1_.isHidden() ? 1 : 0;
	}

	/**
	 * Returns the first icon image.
	 */
	public WImage getIcon1() {
		return this.icon1_;
	}

	/**
	 * Returns the second icon image.
	 */
	public WImage getIcon2() {
		return this.icon2_;
	}

	/**
	 * Sets the state to 0 (show icon 1).
	 * <p>
	 * 
	 * @see WIconPair#setState(int num)
	 */
	public void showIcon1() {
		this.setState(0);
	}

	/**
	 * Sets the state to 1 (show icon 2).
	 * <p>
	 * 
	 * @see WIconPair#setState(int num)
	 */
	public void showIcon2() {
		this.setState(1);
	}

	/**
	 * Signal emitted when clicked while in state 0 (icon 1 is shown).
	 * <p>
	 * Equivalent to: <blockquote>
	 * 
	 * <pre>
	 * icon1().clicked()
	 * </pre>
	 * 
	 * </blockquote>
	 */
	public EventSignal1<WMouseEvent> icon1Clicked() {
		return this.icon1_.clicked();
	}

	/**
	 * Signal emitted when clicked while in state 1 (icon 2 is shown).
	 * <p>
	 * Equivalent to: <blockquote>
	 * 
	 * <pre>
	 * icon2().clicked()
	 * </pre>
	 * 
	 * </blockquote>
	 */
	public EventSignal1<WMouseEvent> icon2Clicked() {
		return this.icon2_.clicked();
	}

	private WContainerWidget impl_;
	private WImage icon1_;
	private WImage icon2_;
}
