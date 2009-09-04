/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.Map;

/**
 * A user control that represents a radio button
 * <p>
 * 
 * Use a {@link WButtonGroup} to group together radio buttons that reflect
 * options that are mutually exclusive.
 * <p>
 * Usage example:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * enum Vote { Republican, Democrate, NoVote };
 * 
 *  // use a group box as widget container for 3 radio buttons, with a title
 *  WGroupBox container = new WGroupBox(&quot;USA elections vote&quot;);
 * 		 
 *  // use a button group to logically group the 3 options
 *  WButtonGroup group = new WButtonGroup(this);
 * 		 
 *  WRadioButton button;
 *  button = new WRadioButton(&quot;I voted Republican&quot;, container);
 *  new WBreak(container);
 *  group.addButton(button, Vote.Republican.ordinal());
 *  button = new WRadioButton(&quot;I voted Democrat&quot;, container);
 *  new WBreak(container);
 *  group.addButton(button, Vote.Democrate.ordinal());
 * 
 *  button = new WRadioButton(&quot;I didn't vote&quot;, container);
 *  new WBreak(container);
 *  group.addButton(button, Vote.NoVote.ordinal());
 * 		 
 *  group.setCheckedButton(group.button(Vote.NoVote.ordinal()));
 * </pre>
 * 
 * </blockquote>
 * <p>
 * The widget corresponds to the HTML
 * <code>&lt;input type=&quot;radio&quot;&gt;</code> tag.
 * <p>
 * WRadioButton is an {@link WWidget#setInline(boolean inlined) inline} widget.
 * <p>
 * 
 * @see WAbstractToggleButton
 * @see WButtonGroup
 */
public class WRadioButton extends WAbstractToggleButton {
	/**
	 * Create an unchecked radio button with empty label and optional parent.
	 */
	public WRadioButton(WContainerWidget parent) {
		super(parent);
		this.buttonGroup_ = null;
		this.setFormObject(true);
	}

	/**
	 * Create an unchecked radio button with empty label and optional parent.
	 * <p>
	 * Calls {@link #WRadioButton(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WRadioButton() {
		this((WContainerWidget) null);
	}

	/**
	 * Create an unchecked radio button with given text and optional parent.
	 */
	public WRadioButton(CharSequence text, WContainerWidget parent) {
		super(text, parent);
		this.buttonGroup_ = null;
		this.setFormObject(true);
	}

	/**
	 * Create an unchecked radio button with given text and optional parent.
	 * <p>
	 * Calls {@link #WRadioButton(CharSequence text, WContainerWidget parent)
	 * this(text, (WContainerWidget)null)}
	 */
	public WRadioButton(CharSequence text) {
		this(text, (WContainerWidget) null);
	}

	/**
	 * Delete a radio button.
	 */
	public void remove() {
		if (this.buttonGroup_ != null) {
			this.buttonGroup_.removeButton(this);
		}
		super.remove();
	}

	/**
	 * Return the button group.
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

	protected void updateDom(DomElement element, boolean all) {
		if (all) {
			element.setAttribute("type", "radio");
			if (this.buttonGroup_ != null) {
				element.setAttribute("name", this.buttonGroup_.getId());
				element.setAttribute("value", this.getId());
			}
		}
		super.updateDom(element, all);
	}

	protected void getFormObjects(Map<String, WObject> formObjects) {
		if (this.buttonGroup_ != null) {
			formObjects.put(this.buttonGroup_.getId(), this.buttonGroup_);
		}
		super.getFormObjects(formObjects);
	}

	protected void setFormData(WObject.FormData formData) {
		if (this.stateChanged_) {
			return;
		}
		if (!formData.values.isEmpty()) {
			String value = formData.values.get(0);
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
