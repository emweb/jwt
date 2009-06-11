package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for grouping radio buttons logically together
 * 
 * 
 * A button group manages a set of radio buttons, making them exclusive of each
 * other.
 * <p>
 * It is not a widget, but instead provides only a logical grouping. Radio
 * buttons are aware of the group in which they have been added, see
 * {@link WRadioButton#getGroup()}. When a button is deleted, it is
 * automatically removed its button group.
 * <p>
 * It allows you to associate id&apos;s with each button, which you may use to
 * identify a particular button. The special value of -1 is reserved to indicate
 * <i>no button</i>.
 * <p>
 * 
 * @see WRadioButton
 */
public class WButtonGroup extends WObject {
	/**
	 * Create a new empty button group.
	 */
	public WButtonGroup(WObject parent) {
		super(parent);
		this.buttons_ = new ArrayList<WButtonGroup.Button>();
	}

	public WButtonGroup() {
		this((WObject) null);
	}

	/**
	 * Delete a button group.
	 * 
	 * This does not delete the radio buttons, but simply removes them from the
	 * group.
	 */
	public void destroy() {
		for (int i = 0; i < this.buttons_.size(); ++i) {
			this.buttons_.get(i).button.setGroup((WButtonGroup) null);
		}
	}

	/**
	 * Add a radio button to the group.
	 * 
	 * You can assign an id to the button. If <i>id</i> is -1, then a unique id
	 * will be generated.
	 * <p>
	 * 
	 * @see WButtonGroup#removeButton(WRadioButton button)
	 */
	public void addButton(WRadioButton button, int id) {
		WButtonGroup.Button b = new WButtonGroup.Button();
		b.button = button;
		b.id = id != -1 ? id : this.generateId();
		this.buttons_.add(b);
		button.setGroup(this);
	}

	public final void addButton(WRadioButton button) {
		addButton(button, -1);
	}

	/**
	 * Remove a radio button from the group.
	 * 
	 * @see WButtonGroup#addButton(WRadioButton button, int id)
	 */
	public void removeButton(WRadioButton button) {
		for (int i = 0; i < this.buttons_.size(); ++i) {
			if (this.buttons_.get(i).button == button) {
				this.buttons_.remove(0 + i);
				button.setGroup((WButtonGroup) null);
				return;
			}
		}
	}

	// public void removeButton(RadioButton button) ;
	/**
	 * Returns the button for the given id.
	 * 
	 * @see WButtonGroup#getId(WRadioButton button)
	 * @see WButtonGroup#addButton(WRadioButton button, int id)
	 */
	public WRadioButton getButton(int id) {
		for (int i = 0; i < this.buttons_.size(); ++i) {
			if (this.buttons_.get(i).id == id) {
				return this.buttons_.get(i).button;
			}
		}
		return null;
	}

	/**
	 * Returns the id for the given button.
	 * 
	 * @see WButtonGroup#getButton(int id)
	 * @see WButtonGroup#addButton(WRadioButton button, int id)
	 */
	public int getId(WRadioButton button) {
		for (int i = 0; i < this.buttons_.size(); ++i) {
			if (this.buttons_.get(i).button == button) {
				return this.buttons_.get(i).id;
			}
		}
		return -1;
	}

	/**
	 * Returns the buttons in this group.
	 */
	public List<WRadioButton> getButtons() {
		List<WRadioButton> buttons = new ArrayList<WRadioButton>();
		for (int i = 0; i < this.buttons_.size(); ++i) {
			buttons.add(this.buttons_.get(i).button);
		}
		return buttons;
	}

	/**
	 * Returns the number of radiobuttons in this group.
	 */
	public int getCount() {
		return this.buttons_.size();
	}

	/**
	 * Returns the id of the checked button.
	 * 
	 * Returns the id of the currently checked button, or -1 if no button is
	 * currently checked.
	 */
	public int getCheckedId() {
		int idx = this.getSelectedButtonIndex();
		return idx == -1 ? -1 : this.buttons_.get(idx).id;
	}

	/**
	 * Sets the currently checked radiobutton.
	 * 
	 * The button <i>button</i> of this group is checked. A value of 0 will
	 * uncheck all radiobuttons.
	 * <p>
	 * Initially, no button is checked.
	 * <p>
	 * 
	 * @see WButtonGroup#getCheckedId()
	 */
	public void setCheckedButton(WRadioButton button) {
		for (int i = 0; i < this.buttons_.size(); ++i) {
			WRadioButton b = this.buttons_.get(i).button;
			if (b == button && !button.isChecked()) {
				button.setChecked(true);
			} else {
				if (b != button && button.isChecked()) {
					button.setChecked(false);
				}
			}
		}
	}

	/**
	 * Returns the checked radiobutton.
	 * 
	 * If there is no radiobutton currently checked this function returns 0.
	 * <p>
	 * 
	 * @see WButtonGroup#setCheckedButton(WRadioButton button)
	 * @see WButtonGroup#getSelectedButtonIndex()
	 */
	public WRadioButton getCheckedButton() {
		int idx = this.getSelectedButtonIndex();
		return idx != -1 ? this.buttons_.get(idx).button : null;
	}

	/**
	 * Sets the currently checked radiobutton.
	 * 
	 * Sets the <i>idx</i>&apos;th radiobutton checked. A value of -1 will
	 * uncheck all radiobuttons.
	 * <p>
	 * Initially, no button is checked.
	 */
	public void setSelectedButtonIndex(int idx) {
		this.setCheckedButton(idx != -1 ? this.buttons_.get(idx).button : null);
	}

	/**
	 * Returns the index of the checked radiobutton.
	 * 
	 * The index reflects the order in which the buttons have been added to the
	 * button group. Use {@link WButtonGroup#getCheckedId()} if you want to know
	 * the id of the button that is currently checked. If there is no
	 * radiobutton selected this function returns -1.
	 * <p>
	 * 
	 * @see WButtonGroup#getCheckedId()
	 */
	public int getSelectedButtonIndex() {
		for (int i = 0; i < this.buttons_.size(); ++i) {
			if (this.buttons_.get(i).button.isChecked()) {
				return i;
			}
		}
		return -1;
	}

	private static class Button {
		public WRadioButton button;
		public int id;
	}

	private List<WButtonGroup.Button> buttons_;

	void uncheckOthers(WRadioButton button) {
		for (int i = 0; i < this.buttons_.size(); ++i) {
			if (this.buttons_.get(i).button != button) {
				this.buttons_.get(i).button.state_ = CheckState.Unchecked;
			}
		}
	}

	private int generateId() {
		int id = 0;
		for (int i = 0; i < this.buttons_.size(); ++i) {
			id = Math.max(this.buttons_.get(i).id + 1, id);
		}
		return id;
	}

	void setFormData(WObject.FormData formData) {
		if (!formData.values.isEmpty()) {
			String value = formData.values.get(0);
			for (int i = 0; i < this.buttons_.size(); ++i) {
				if (value.equals(this.buttons_.get(i).button.getFormName())) {
					if (this.buttons_.get(i).button.stateChanged_) {
						return;
					}
					this.uncheckOthers(this.buttons_.get(i).button);
					this.buttons_.get(i).button.state_ = CheckState.Checked;
					return;
				}
			}
		}
	}
}
