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
 * A scrollbar attached to a scroll area.
 * <p>
 * 
 * A {@link WScrollArea} always has two scrollbars (even if they are not
 * visible, for example when the policy is
 * {@link WScrollArea.ScrollBarPolicy#ScrollBarAlwaysOff}). Using the
 * {@link WScrollBar#tie(WScrollBar one, WScrollBar two) tie()} functions, it is
 * possible to tie to scrollbars together, so that they will scroll together.
 * <p>
 * 
 * @see WScrollArea
 */
public class WScrollBar extends WObject {
	private static Logger logger = LoggerFactory.getLogger(WScrollBar.class);

	/**
	 * Returns the orientation of this scrollbar.
	 */
	public Orientation getOrientation() {
		return this.orientation_;
	}

	/**
	 * Ties two scrollbars together.
	 * <p>
	 * The effect of this call is that these scrollbars will keep their
	 * positions synchronised.
	 */
	public static void tie(WScrollBar one, WScrollBar two) {
		one.ties_.add(two);
		two.ties_.add(one);
		one.tiesChanged_ = true;
		two.tiesChanged_ = true;
		one.scrollArea_.scrollBarChanged();
		two.scrollArea_.scrollBarChanged();
	}

	/**
	 * Unties two scrollbars, that were previously tied together.
	 * <p>
	 * Undo a {@link WScrollBar#tie(WScrollBar one, WScrollBar two) tie()};
	 */
	public static void unTie(WScrollBar one, WScrollBar two) {
		one.ties_.remove(two);
		two.ties_.remove(one);
		one.tiesChanged_ = true;
		two.tiesChanged_ = true;
		one.scrollArea_.scrollBarChanged();
		two.scrollArea_.scrollBarChanged();
	}

	/**
	 * Sets the scrollbar value (in pixels).
	 * <p>
	 * This will move the scrollbar to the given value.
	 */
	public void setValue(int value) {
		this.value_ = value;
		this.valueSet_ = true;
		this.scrollArea_.scrollBarChanged();
	}

	WScrollBar(WScrollArea area, Orientation orientation) {
		super();
		this.scrollArea_ = area;
		this.orientation_ = orientation;
		this.ties_ = new ArrayList<WScrollBar>();
		this.tiesChanged_ = false;
		this.valueSet_ = false;
	}

	private WScrollArea scrollArea_;
	private Orientation orientation_;
	private List<WScrollBar> ties_;
	boolean tiesChanged_;
	private int value_;
	private boolean valueSet_;

	void updateDom(DomElement element, boolean all) {
		if (this.valueSet_) {
			String side = this.orientation_ == Orientation.Horizontal ? "Left"
					: "Top";
			element.callMethod("scroll" + side + " = "
					+ String.valueOf(this.value_) + ";");
			this.valueSet_ = false;
		}
		if (this.tiesChanged_ || all) {
			String jsCode = "";
			for (int i = 0; i < this.ties_.size(); ++i) {
				String tieElement = "Wt3_1_11.getElement('"
						+ this.ties_.get(i).scrollArea_.getId() + "')";
				String scrollElement = "Wt3_1_11.getElement('"
						+ this.scrollArea_.getId() + "')";
				String side = this.orientation_ == Orientation.Horizontal ? "Left"
						: "Top";
				jsCode += tieElement + ".scroll" + side + "=" + scrollElement
						+ ".scroll" + side + ";";
			}
			if (!all || jsCode.length() != 0) {
				element.addEvent("scroll", jsCode);
			}
			this.tiesChanged_ = false;
		}
	}
}
