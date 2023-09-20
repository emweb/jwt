/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for grouping radio buttons logically together.
 *
 * <p>A button group manages a set of {@link WRadioButton radio buttons}, making them exclusive of
 * each other.
 *
 * <p>It is not a widget, but instead provides only a logical grouping. Radio buttons are aware of
 * the group in which they have been added, see {@link WRadioButton#getGroup()}. When a button is
 * deleted, it is automatically removed its button group.
 *
 * <p>It allows you to associate id&apos;s with each button, which you may use to identify a
 * particular button. The special value of -1 is reserved to indicate <i>no button</i>.
 *
 * <p>
 *
 * @see WRadioButton
 */
public class WButtonGroup extends WObject {
  private static Logger logger = LoggerFactory.getLogger(WButtonGroup.class);

  /**
   * Creates a new empty button group.
   *
   * <p>
   *
   * <p><i><b>Note: </b>The {@link WRadioButton WRadioButtons} associated with this {@link
   * WButtonGroup} keep a shared_ptr to this {@link WButtonGroup}. Therefore, you should store a
   * {@link WButtonGroup} in a shared_ptr (e.g. construct it with make_shared) before adding any
   * radio buttons to it. </i>
   */
  public WButtonGroup() {
    super();
    this.buttons_ = new ArrayList<WButtonGroup.Button>();
    this.checkedChanged_ = new Signal1<WRadioButton>();
    this.checkedChangedConnected_ = false;
  }
  /**
   * Adds a radio button to the group.
   *
   * <p>You can assign an id to the button. If <code>id</code> is -1, then a unique id will be
   * generated.
   *
   * <p>
   *
   * <p><i><b>Note: </b>{@link WButtonGroup} should be owned by a shared_ptr before addButton is
   * called on it! </i>
   *
   * @see WButtonGroup#removeButton(WRadioButton button)
   */
  public void addButton(WRadioButton button, int id) {
    WButtonGroup.Button b = new WButtonGroup.Button();
    b.button = button;
    b.id = id != -1 ? id : this.generateId();
    this.buttons_.add(b);
    button.setGroup(this);
    if (this.checkedChangedConnected_) {
      button
          .changed()
          .addListener(
              this,
              () -> {
                WButtonGroup.this.onButtonChange();
              });
    }
  }
  /**
   * Adds a radio button to the group.
   *
   * <p>Calls {@link #addButton(WRadioButton button, int id) addButton(button, - 1)}
   */
  public final void addButton(WRadioButton button) {
    addButton(button, -1);
  }
  /**
   * Removes a radio button from the group.
   *
   * <p>
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
  /**
   * Returns the button for the given id.
   *
   * <p>Returns <code>null</code> if no button exists for the given id.
   *
   * <p>
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
   * <p>
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

  public String getId() {
    return super.getId();
  }
  /** Returns the buttons in this group. */
  public List<WRadioButton> getButtons() {
    List<WRadioButton> buttons = new ArrayList<WRadioButton>();
    for (int i = 0; i < this.buttons_.size(); ++i) {
      buttons.add(this.buttons_.get(i).button);
    }
    return buttons;
  }
  /** Returns the number of radiobuttons in this group. */
  public int getCount() {
    return this.buttons_.size();
  }
  /**
   * Returns the id of the checked button.
   *
   * <p>Returns the id of the currently checked button, or -1 if no button is currently checked.
   */
  public int getCheckedId() {
    int idx = this.getSelectedButtonIndex();
    return idx == -1 ? -1 : this.buttons_.get(idx).id;
  }
  /**
   * Sets the currently checked radiobutton.
   *
   * <p>The button <code>button</code> of this group is checked. A value of <code>null</code> will
   * uncheck all radiobuttons.
   *
   * <p>Initially, no button is checked.
   *
   * <p>
   *
   * @see WButtonGroup#getCheckedId()
   */
  public void setCheckedButton(WRadioButton button) {
    for (int i = 0; i < this.buttons_.size(); ++i) {
      WRadioButton b = this.buttons_.get(i).button;
      if (b == button && !b.isChecked()) {
        b.setChecked(true);
      } else {
        if (b != button && b.isChecked()) {
          b.setChecked(false);
        }
      }
    }
  }
  /**
   * Returns the checked radiobutton.
   *
   * <p>If there is no radiobutton currently checked this function returns <code>null</code>.
   *
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
   * <p>Sets the <code>idx&apos;th</code> radiobutton checked. A value of -1 will uncheck all
   * radiobuttons.
   *
   * <p>Initially, no button is checked.
   */
  public void setSelectedButtonIndex(int idx) {
    this.setCheckedButton(idx != -1 ? this.buttons_.get(idx).button : null);
  }
  /**
   * Returns the index of the checked radiobutton.
   *
   * <p>The index reflects the order in which the buttons have been added to the button group. Use
   * {@link WButtonGroup#getCheckedId() getCheckedId()} if you want to know the id of the button
   * that is currently checked. If there is no radiobutton selected this function returns -1.
   *
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
  /**
   * Signal emitted when a button was checked.
   *
   * <p>The argument passed is the new {@link WButtonGroup#getCheckedButton() getCheckedButton()}.
   */
  public Signal1<WRadioButton> checkedChanged() {
    if (!this.checkedChangedConnected_) {
      this.checkedChangedConnected_ = true;
      for (int i = 0; i < this.buttons_.size(); ++i) {
        this.buttons_
            .get(i)
            .button
            .changed()
            .addListener(
                this,
                () -> {
                  WButtonGroup.this.onButtonChange();
                });
      }
    }
    return this.checkedChanged_;
  }

  static class Button {
    private static Logger logger = LoggerFactory.getLogger(Button.class);

    public WRadioButton button;
    public int id;
  }

  private List<WButtonGroup.Button> buttons_;
  private Signal1<WRadioButton> checkedChanged_;
  private boolean checkedChangedConnected_;

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

  private void onButtonChange() {
    this.checkedChanged_.trigger(this.getCheckedButton());
  }

  protected void setFormData(final WObject.FormData formData) {
    if (!(formData.values.length == 0)) {
      final String value = formData.values[0];
      for (int i = 0; i < this.buttons_.size(); ++i) {
        if (value.equals(this.buttons_.get(i).button.getId())) {
          if (this.buttons_.get(i).button.flags_.get(WAbstractToggleButton.BIT_STATE_CHANGED)) {
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
