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
 * A user control that represents a radio button
 * 
 * 
 * Use a {@link WButtonGroup} to group together radio buttons that reflect
 * options that are mutually exclusive.
 * <p>
 * Usage example:
 * <p>
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

	public WRadioButton(CharSequence text) {
		this(text, (WContainerWidget) null);
	}

	public WRadioButton(boolean withLabel, WContainerWidget parent) {
		super(withLabel, parent);
		this.buttonGroup_ = null;
		this.setFormObject(true);
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
	 * 
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
				element.setAttribute("name", this.buttonGroup_.getFormName());
				element.setAttribute("value", this.getFormName());
			} else {
				element.setAttribute("name", this.getFormName());
			}
		}
		super.updateDom(element, all);
	}

	protected void getFormObjects(List<WObject> formObjects) {
		if (this.buttonGroup_ != null) {
			int i = formObjects.indexOf(this.buttonGroup_);
			if (i == -1) {
				formObjects.add(this.buttonGroup_);
			}
		}
		formObjects.add(this);
	}

	protected void setFormData(WObject.FormData formData) {
		if (this.stateChanged_) {
			return;
		}
		if (!formData.values.isEmpty()) {
			String value = formData.values.get(0);
			if (value.equals(this.getFormName())) {
				this.buttonGroup_.uncheckOthers(this);
				this.state_ = CheckState.Checked;
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
