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
 * A widget which group widgets into a frame with a title.
 * <p>
 * 
 * This is typically used in a form to group certain form elements together.
 * <p>
 * Usage example:
 * <p>
 * 
 * <pre>
 * {@code
 *  enum Vote { Republican , Democrate , NoVote };
 *  
 *  // use a group box as widget container for 3 radio buttons, with a title
 *  WGroupBox container = new WGroupBox("USA elections vote");
 *  		 
 *  // use a button group to logically group the 3 options
 *  WButtonGroup group = new WButtonGroup(this);
 * 		 
 *  WRadioButton button;
 *  button = new WRadioButton("I voted Republican", container);
 *  new WBreak(container);
 *  group.addButton(button, Vote.Republican.ordinal());
 * 
 *  button = new WRadioButton("I voted Democrat", container);
 *  new WBreak(container);
 *  group.addButton(button, Vote.Democrate.ordinal());
 * 
 *  button = new WRadioButton("I didn't vote", container);
 *  new WBreak(container);
 *  group.addButton(button, Vote.NoVote.ordinal());
 * 		 
 *  group.setCheckedButton(group.button(Vote.NoVote.ordinal()));
 * }
 * </pre>
 * <p>
 * Like {@link WContainerWidget}, WGroupBox is by default displayed as a
 * {@link WWidget#setInline(boolean inlined) block}.
 * <p>
 * <div align="center"> <img src="doc-files//WGroupBox-1.png"
 * alt="WGroupBox example">
 * <p>
 * <strong>WGroupBox example</strong>
 * </p>
 * </div> <h3>CSS</h3>
 * <p>
 * The widget corresponds to the HTML <code>&lt;fieldset&gt;</code> tag, and the
 * title in a nested <code>&lt;legend&gt;</code> tag. This widget does not
 * provide styling, and can be styled using inline or external CSS as
 * appropriate.
 */
public class WGroupBox extends WContainerWidget {
	private static Logger logger = LoggerFactory.getLogger(WGroupBox.class);

	/**
	 * Creates a groupbox with empty title.
	 */
	public WGroupBox(WContainerWidget parent) {
		super(parent);
		this.title_ = new WString();
		this.titleChanged_ = false;
		this.init();
	}

	/**
	 * Creates a groupbox with empty title.
	 * <p>
	 * Calls {@link #WGroupBox(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WGroupBox() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates a groupbox with given title message.
	 */
	public WGroupBox(CharSequence title, WContainerWidget parent) {
		super(parent);
		this.title_ = WString.toWString(title);
		this.titleChanged_ = false;
		this.init();
	}

	/**
	 * Creates a groupbox with given title message.
	 * <p>
	 * Calls {@link #WGroupBox(CharSequence title, WContainerWidget parent)
	 * this(title, (WContainerWidget)null)}
	 */
	public WGroupBox(CharSequence title) {
		this(title, (WContainerWidget) null);
	}

	/**
	 * Returns the title.
	 */
	public WString getTitle() {
		return this.title_;
	}

	/**
	 * Returns the title.
	 */
	public void setTitle(CharSequence title) {
		this.title_ = WString.toWString(title);
		this.titleChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	public void refresh() {
		if (this.title_.refresh()) {
			this.titleChanged_ = true;
			this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		}
		super.refresh();
	}

	DomElementType getDomElementType() {
		return DomElementType.DomElement_FIELDSET;
	}

	void updateDom(DomElement element, boolean all) {
		if (all || this.titleChanged_) {
			DomElement legend;
			if (all) {
				legend = DomElement.createNew(DomElementType.DomElement_LEGEND);
				legend.setId(this.getId() + "l");
			} else {
				legend = DomElement.getForUpdate(this.getId() + "l",
						DomElementType.DomElement_LEGEND);
			}
			legend.setProperty(Property.PropertyInnerHTML, escapeText(
					this.title_).toString());
			element.addChild(legend);
			this.titleChanged_ = false;
		}
		super.updateDom(element, all);
	}

	void propagateRenderOk(boolean deep) {
		this.titleChanged_ = false;
		super.propagateRenderOk(deep);
	}

	int getFirstChildIndex() {
		return 1;
	}

	private WString title_;
	private boolean titleChanged_;

	private void init() {
		this.setJavaScriptMember(WT_GETPS_JS, StdWidgetItemImpl
				.getSecondGetPSJS());
	}
}
