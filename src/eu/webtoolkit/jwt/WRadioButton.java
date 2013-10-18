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
 * A user control that represents a radio button.
 * <p>
 * 
 * Use a {@link WButtonGroup} to group together radio buttons that reflect
 * options that are mutually exclusive.
 * <p>
 * Usage example:
 * <p>
 * 
 * <pre>
 * {@code
 *  enum Vote { Republican, Democrate, NoVote };
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
 * WRadioButton is an {@link WWidget#setInline(boolean inlined) inline} widget.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * This widget corresponds to the HTML
 * <code>&lt;input type=&quot;radio&quot;&gt;</code> tag. When a label is
 * specified, the input element is nested in a <code>&lt;label&gt;</code>.
 * <p>
 * This widget does not provide styling, and can be styled using inline or
 * external CSS as appropriate.
 * <p>
 * 
 * @see WAbstractToggleButton
 * @see WButtonGroup
 */
public class WRadioButton extends WAbstractToggleButton {
	private static Logger logger = LoggerFactory.getLogger(WRadioButton.class);

	/**
	 * Creates an unchecked radio button with empty label and optional parent.
	 */
	public WRadioButton(WContainerWidget parent) {
		super(parent);
		this.buttonGroup_ = null;
		this.setFormObject(true);
	}

	/**
	 * Creates an unchecked radio button with empty label and optional parent.
	 * <p>
	 * Calls {@link #WRadioButton(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WRadioButton() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates an unchecked radio button with given text and optional parent.
	 */
	public WRadioButton(CharSequence text, WContainerWidget parent) {
		super(text, parent);
		this.buttonGroup_ = null;
		this.setFormObject(true);
	}

	/**
	 * Creates an unchecked radio button with given text and optional parent.
	 * <p>
	 * Calls {@link #WRadioButton(CharSequence text, WContainerWidget parent)
	 * this(text, (WContainerWidget)null)}
	 */
	public WRadioButton(CharSequence text) {
		this(text, (WContainerWidget) null);
	}

	/**
	 * Destructor.
	 */
	public void remove() {
		if (this.buttonGroup_ != null) {
			this.buttonGroup_.removeButton(this);
		}
		super.remove();
	}

	/**
	 * Returns the button group.
	 * <p>
	 * Returns the button group to which this button belongs.
	 * <p>
	 * 
	 * @see WButtonGroup#addButton(WRadioButton button, int id)
	 */
	public WButtonGroup getGroup() {
		return this.buttonGroup_;
	}

	private WButtonGroup buttonGroup_;

	void setGroup(WButtonGroup group) {
		this.buttonGroup_ = group;
	}

	void updateInput(DomElement input, boolean all) {
		if (all) {
			input.setAttribute("type", "radio");
			if (this.buttonGroup_ != null) {
				input.setAttribute("name", this.buttonGroup_.getId());
				input.setAttribute("value", this.getId());
			}
		}
	}

	void getFormObjects(Map<String, WObject> formObjects) {
		if (this.buttonGroup_ != null) {
			formObjects.put(this.buttonGroup_.getId(), this.buttonGroup_);
		}
		super.getFormObjects(formObjects);
	}

	void setFormData(WObject.FormData formData) {
		if (this.stateChanged_ || this.isReadOnly()) {
			return;
		}
		if (!(formData.values.length == 0)) {
			String value = formData.values[0];
			if (value.equals(this.getId())) {
				if (this.buttonGroup_ != null) {
					this.buttonGroup_.uncheckOthers(this);
					this.state_ = CheckState.Checked;
				}
			} else {
				if (!(this.buttonGroup_ != null)) {
					super.setFormData(formData);
				}
			}
		} else {
			if (!(this.buttonGroup_ != null)) {
				super.setFormData(formData);
			}
		}
	}
}
