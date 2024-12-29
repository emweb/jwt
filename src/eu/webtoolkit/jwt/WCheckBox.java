/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
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
 * A user control that represents a check box.
 *
 * <p>By default, a checkbox can have two states: {@link CheckState#Checked} or {@link
 * CheckState#Unchecked}, which can be inspected using {@link WAbstractToggleButton#isChecked()},
 * and set using {@link WAbstractToggleButton#setChecked()}.
 *
 * <p>A checkbox may also provide a third state, {@link CheckState#PartiallyChecked}, which is
 * useful to indicate that it is neither checked nor unchecked. JWt will use native browser support
 * for this HTML5 extension when available (Safari and MS IE), and use an image-based workaround
 * otherwise. You may enable support for the third state using {@link WCheckBox#setTristate(boolean
 * tristate) setTristate()}, and use {@link WCheckBox#setCheckState(CheckState state)
 * setCheckState()} and {@link WCheckBox#getCheckState() getCheckState()} to read all three states.
 * Once a tri-state checkbox is clicked, it cycles through the states {@link CheckState#Checked} and
 * {@link CheckState#Unchecked}.
 *
 * <p>A label is added as a sibling of the checkbox to the same parent.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * WGroupBox box = new WGroupBox("In-flight options");
 *
 * WCheckBox w1 = new WCheckBox("Vegetarian diet", box);
 * box.addWidget(new WBreak());
 * WCheckBox w2 = new WCheckBox("WIFI access", box);
 * box.addWidget(new WBreak());
 * WCheckBox w3 = new WCheckBox("AC plug", box);
 *
 * w1.setChecked(false);
 * w2.setChecked(true);
 * w3.setChecked(true);
 *
 * }</pre>
 *
 * <p>WCheckBox is an {@link WWidget#setInline(boolean inlined) inline } widget.
 *
 * <p>
 *
 * <h3>CSS</h3>
 *
 * <p>This widget is rendered using an HTML <code>&lt;input type=&quot;checkbox&quot;&gt;</code>
 * tag. When a label is specified, the input element is nested in a <code>&lt;label&gt;</code>.
 *
 * <p>This widget does not provide styling, and can be styled using inline or external CSS as
 * appropriate.
 *
 * <p>
 *
 * @see WAbstractToggleButton
 */
public class WCheckBox extends WAbstractToggleButton {
  private static Logger logger = LoggerFactory.getLogger(WCheckBox.class);

  /**
   * Creates a checkbox without label.
   *
   * <p>A checkbox created by this constructor will not contain a placeholder for a label, and
   * therefore it is not possible to assign a label to it later through {@link
   * WAbstractToggleButton#setText(CharSequence text) WAbstractToggleButton#setText()}.
   */
  public WCheckBox(WContainerWidget parentContainer) {
    super();
    this.triState_ = false;
    this.partialStateSelectable_ = false;
    this.jslot_ = null;
    this.setFormObject(true);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a checkbox without label.
   *
   * <p>Calls {@link #WCheckBox(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WCheckBox() {
    this((WContainerWidget) null);
  }
  /** Creates a checkbox with given label. */
  public WCheckBox(final CharSequence text, WContainerWidget parentContainer) {
    super(text, (WContainerWidget) null);
    this.triState_ = false;
    this.partialStateSelectable_ = false;
    this.jslot_ = null;
    this.setFormObject(true);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a checkbox with given label.
   *
   * <p>Calls {@link #WCheckBox(CharSequence text, WContainerWidget parentContainer) this(text,
   * (WContainerWidget)null)}
   */
  public WCheckBox(final CharSequence text) {
    this(text, (WContainerWidget) null);
  }
  /**
   * Makes a tristate checkbox.
   *
   * <p>
   *
   * <p><i><b>Note: </b>You should enable tristate functionality right after construction and this
   * cannot be modified later. </i>
   */
  public void setTristate(boolean tristate) {
    this.triState_ = tristate;
    if (this.triState_) {
      if (!this.supportsIndeterminate(WApplication.getInstance().getEnvironment())) {
        this.updateJSlot();
      }
    }
  }
  /**
   * Makes a tristate checkbox.
   *
   * <p>Calls {@link #setTristate(boolean tristate) setTristate(true)}
   */
  public final void setTristate() {
    setTristate(true);
  }
  /**
   * enable or disable cycling throught partial state
   *
   * <p>
   *
   * @see WCheckBox#isPartialStateSelectable()
   */
  public void setPartialStateSelectable(boolean t) {
    if (t && !this.isTristate()) {
      this.setTristate(true);
    }
    this.partialStateSelectable_ = t;
    this.updateJSlot();
    this.updateNextState();
  }
  /**
   * return partial state cycling
   *
   * <p>
   *
   * @see WCheckBox#setPartialStateSelectable(boolean t)
   */
  public boolean isPartialStateSelectable() {
    return this.partialStateSelectable_;
  }
  /**
   * Returns whether the checkbox is tristate.
   *
   * <p>
   *
   * @see WCheckBox#setTristate(boolean tristate)
   */
  public boolean isTristate() {
    return this.triState_;
  }
  /**
   * Sets the check state.
   *
   * <p>Unless it is a tri-state checkbox, only {@link CheckState#Checked} and {@link
   * CheckState#Unchecked} are valid states.
   */
  public void setCheckState(CheckState state) {
    super.setCheckState(state);
    this.updateNextState();
  }
  /**
   * Returns the check state.
   *
   * <p>
   *
   * @see WCheckBox#setCheckState(CheckState state)
   * @see WAbstractToggleButton#isChecked()
   */
  public CheckState getCheckState() {
    return this.state_;
  }

  void updateInput(final DomElement input, boolean all) {
    if (all) {
      input.setAttribute("type", "checkbox");
    }
  }

  protected void updateJSlot() {
    this.jslot_ = (JSlot) null;
    JSlot slot = null;
    String partialOn = "";
    String partialOff = "";
    if (!this.supportsIndeterminate(WApplication.getInstance().getEnvironment())) {
      partialOff = "obj.style.opacity='';";
      partialOn = "obj.style.opacity='0.5';";
      if (this.triState_ && !this.partialStateSelectable_) {
        slot = new JSlot("function(obj, e) { " + partialOff + "}", this);
      }
    } else {
      partialOn = "obj.indeterminate=true;";
      partialOff = "obj.indeterminate=false;";
    }
    if (this.partialStateSelectable_) {
      StringWriter ss = new StringWriter();
      ss.append("function(obj, e) {\n")
          .append("if(obj.nextState == 'c'){\n")
          .append("obj.checked=true;")
          .append(partialOff)
          .append(" obj.nextState='u';")
          .append("} else if( obj.nextState=='i') {\n")
          .append("obj.nextState='c';")
          .append(partialOn)
          .append(" } else if( obj.nextState=='u') {\n")
          .append("obj.nextState='i';")
          .append("obj.checked=false;")
          .append(partialOff)
          .append(" } else obj.nextState='i';")
          .append("}");
      slot = new JSlot(ss.toString(), this);
    }
    if (slot != null) {
      this.changed().addListener(slot);
      this.jslot_ = slot;
    }
  }

  protected void updateNextState() {
    String nextState = "";
    switch (this.state_) {
      case Checked:
        nextState = "u";
        break;
      case Unchecked:
        nextState = "i";
        break;
      case PartiallyChecked:
        nextState = "c";
        break;
    }
    if (this.partialStateSelectable_) {
      this.doJavaScript(this.getJsRef() + ".nextState='" + nextState + "';");
    } else {
      this.doJavaScript(this.getJsRef() + ".nextState=null;");
    }
  }

  private boolean triState_;
  private boolean partialStateSelectable_;
  private JSlot jslot_;
}
