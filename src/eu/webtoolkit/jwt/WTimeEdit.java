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
 * A Time field editor.
 *
 * <p>Styling through CSS is not applicable. A native HTML5 control can be used by means of {@link
 * WTimeEdit#setNativeControl(boolean nativeControl) setNativeControl()}.
 *
 * <p>
 *
 * <p><i><b>Note: </b>Using the native HTML5 control forces the format <code>HH:mm</code> or <code>
 * HH:mm:ss</code>. The user will not be aware of this, since the control offers them a view
 * dependent on their locale. </i>
 *
 * @see WTime
 * @see WTimeValidator
 * @see WTimeEdit#setNativeControl(boolean nativeControl)
 */
public class WTimeEdit extends WLineEdit {
  private static Logger logger = LoggerFactory.getLogger(WTimeEdit.class);

  /** Creates a new time edit. */
  public WTimeEdit(WContainerWidget parentContainer) {
    super();
    this.popup_ = null;
    this.uTimePicker_ = null;
    this.oTimePicker_ = null;
    this.nativeControl_ = false;
    this.changed()
        .addListener(
            this,
            () -> {
              WTimeEdit.this.setFromLineEdit();
            });
    this.init();
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a new time edit.
   *
   * <p>Calls {@link #WTimeEdit(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WTimeEdit() {
    this((WContainerWidget) null);
  }

  public void remove() {
    super.remove();
  }
  /**
   * Sets the time.
   *
   * <p>Does nothing if the current time is <code>Null</code>.
   *
   * <p>
   *
   * @see WTimeEdit#getTime()
   */
  public void setTime(final WTime time) {
    if (!(time == null)) {
      this.setText(time.toString(this.getFormat()));
      if (!this.isNativeControl()) {
        this.oTimePicker_.setTime(time);
      }
    }
  }
  /**
   * Returns the time.
   *
   * <p>Returns an invalid time (for which {@link WTime#isValid()} returns <code>false</code>) if
   * the time could not be parsed using the current {@link WTimeEdit#getFormat() getFormat()}.
   *
   * <p>
   *
   * @see WTimeEdit#setTime(WTime time)
   * @see WLineEdit#getText()
   */
  public WTime getTime() {
    return WTime.fromString(this.getText(), this.getFormat());
  }
  /**
   * Sets the format of the Time.
   *
   * <p>This sets the format in the validator.
   *
   * <p>When the native HTML5 control is used, the format is limited to <code>HH:mm:ss</code> or
   * <code>HH:mm</code>. Changing to another format has no effect.
   *
   * <p>
   *
   * @see WTimeValidator#setFormat(String format)
   * @see WTimeEdit#setNativeControl(boolean nativeControl)
   */
  public void setFormat(final String format) {
    WTimeValidator tv = this.getTimeValidator();
    if (tv != null) {
      if (this.isNativeControl() && !(format.equals(HM_FORMAT) || format.equals(HMS_FORMAT))) {
        logger.warn(
            new StringWriter()
                .append(
                    "setFormat() ignored since nativeControl() is true and the format isn't HH:mm, or HH:mm:ss")
                .toString());
        return;
      }
      WTime t = this.getTime();
      tv.setFormat(format);
      if (!this.isNativeControl()) {
        this.oTimePicker_.configure();
      }
      this.setTime(t);
    } else {
      logger.warn(
          new StringWriter()
              .append("setFormat() ignored since validator is not WTimeValidator")
              .toString());
    }
  }
  /** Returns the format. */
  public String getFormat() {
    WTimeValidator tv = this.getTimeValidator();
    if (tv != null) {
      return tv.getFormat();
    } else {
      logger.warn(
          new StringWriter()
              .append("format() is bogus since validator is not WTimeValidator.")
              .toString());
      return "";
    }
  }

  public void setHidden(boolean hidden, final WAnimation animation) {
    super.setHidden(hidden, animation);
    if (this.popup_ != null) {
      this.popup_.setHidden(hidden, animation);
    }
  }
  /**
   * Changes whether the native HTML5 control is used.
   *
   * <p>When enabled the browser&apos;s native time input (<code>&lt;input type=&quot;time&quot;&gt;
   * </code>) will be used if available. This should provide a better experience on mobile browsers.
   * This option is set to false by default.
   *
   * <p>Calling native control after the widget is rendered is not supported.
   *
   * <p>Setting native control to true requires both <code>HH:mm:ss</code> and <code>HH:mm</code> to
   * be valid formats. The format can be set by either the validator, or directly with {@link
   * WTimeEdit#setFormat(String format) setFormat()}.
   *
   * <p>Once the format is set, the step will be automatically calculated. This indicates the
   * minimum increment in seconds or minutes that is valid input.
   *
   * <p>When setting native control to true the setters for the steps will no longer do anything.
   *
   * <p>
   *
   * @see WTimeEdit#isNativeControl()
   * @see WTimeEdit#setFormat(String format)
   * @see WTimeEdit#setHourStep(int step)
   * @see WTimeEdit#setMinuteStep(int step)
   * @see WTimeEdit#setSecondStep(int step)
   * @see WTimeEdit#setMillisecondStep(int step)
   * @see WTimeEdit#setWrapAroundEnabled(boolean enabled)
   */
  public void setNativeControl(boolean nativeControl) {
    WTimeValidator tv = this.getTimeValidator();
    if (nativeControl
        && (this.getFormat().equals(HM_FORMAT) || this.getFormat().equals(HMS_FORMAT))) {
      if (tv != null) {
        tv.setFormat(this.getFormat());
        if (this.getFormat().equals(HM_FORMAT)) {
          tv.setStep(Duration.ofSeconds(60));
        } else {
          if (this.getFormat().equals(HMS_FORMAT)) {
            tv.setStep(Duration.ofSeconds(1));
          }
        }
      }
    } else {
      if (nativeControl) {
        this.setFormat(HM_FORMAT);
      }
    }
    if (nativeControl) {
      this.uTimePicker_ = (WTimePicker) null;
    } else {
      this.init();
    }
    this.nativeControl_ = nativeControl;
  }
  /**
   * Returns whether a native HTML5 control is used.
   *
   * <p>When active, the format of the input it limited to <code>HH:mm</code> or <code>HH:mm:ss
   * </code>. The step is set to <code>60</code>, or <code>1</code> respectively, specifying the
   * granularity of the input to a minute or a second.
   *
   * <p>
   *
   * @see WTimeEdit#setNativeControl(boolean nativeControl)
   */
  public boolean isNativeControl() {
    return this.nativeControl_;
  }
  /** Sets the lower limit of the valid time range. */
  public void setBottom(final WTime bottom) {
    WTimeValidator tv = this.getTimeValidator();
    if (tv != null) {
      tv.setBottom(bottom);
    }
  }
  /** Returns the lower limit of the valid time range. */
  public WTime getBottom() {
    WTimeValidator tv = this.getTimeValidator();
    if (tv != null) {
      return tv.getBottom();
    }
    return null;
  }
  /** Sets the upper limit of the valid time range. */
  public void setTop(final WTime top) {
    WTimeValidator tv = this.getTimeValidator();
    if (tv != null) {
      tv.setTop(top);
    }
  }
  /** Returns the upper limit of the valid time range. */
  public WTime getTop() {
    WTimeValidator tv = this.getTimeValidator();
    if (tv != null) {
      return tv.getTop();
    }
    return null;
  }
  /**
   * Sets the step size for the hours.
   *
   * <p>It has no effect if a native HTML5 control is used.
   *
   * <p>
   *
   * @see WTimeEdit#setNativeControl(boolean nativeControl)
   */
  public void setHourStep(int step) {
    if (this.isNativeControl()) {
      return;
    }
    this.oTimePicker_.setHourStep(step);
  }
  /** Returns the step size for the hours. */
  public int getHourStep() {
    if (this.isNativeControl()) {
      return 0;
    }
    return this.oTimePicker_.getHourStep();
  }
  /**
   * Sets the step size for the minutes.
   *
   * <p>It has no effect if a native HTML5 control is used.
   *
   * <p>
   *
   * @see WTimeEdit#setNativeControl(boolean nativeControl)
   */
  public void setMinuteStep(int step) {
    if (this.isNativeControl()) {
      return;
    }
    this.oTimePicker_.setMinuteStep(step);
  }
  /** Returns the step size for the minutes. */
  public int getMinuteStep() {
    if (this.isNativeControl()) {
      return 0;
    }
    return this.oTimePicker_.getMinuteStep();
  }
  /**
   * Sets the step size for the seconds.
   *
   * <p>It has no effect if a native HTML5 control is used.
   *
   * <p>
   *
   * @see WTimeEdit#setNativeControl(boolean nativeControl)
   */
  public void setSecondStep(int step) {
    if (this.isNativeControl()) {
      return;
    }
    this.oTimePicker_.setSecondStep(step);
  }
  /** Returns the step size for the seconds. */
  public int getSecondStep() {
    if (this.isNativeControl()) {
      return 0;
    }
    return this.oTimePicker_.getSecondStep();
  }
  /**
   * Sets the step size for the milliseconds.
   *
   * <p>It has no effect if a native HTML5 control is used.
   *
   * <p>
   *
   * @see WTimeEdit#setNativeControl(boolean nativeControl)
   */
  public void setMillisecondStep(int step) {
    if (this.isNativeControl()) {
      return;
    }
    this.oTimePicker_.setMillisecondStep(step);
  }
  /** Returns the step size for the milliseconds. */
  public int getMillisecondStep() {
    if (this.isNativeControl()) {
      return 0;
    }
    return this.oTimePicker_.getMillisecondStep();
  }
  /**
   * Enables or disables wraparound.
   *
   * <p>It has no effect if a native HTML5 control is used.
   *
   * <p>Wraparound is enabled by default
   */
  public void setWrapAroundEnabled(boolean enabled) {
    if (this.isNativeControl()) {
      return;
    }
    this.oTimePicker_.setWrapAroundEnabled(enabled);
  }
  /** Returns whether wraparound is enabled. */
  public boolean isWrapAroundEnabled() {
    if (this.isNativeControl()) {
      return true;
    }
    return this.oTimePicker_.isWrapAroundEnabled();
  }

  public void load() {
    boolean wasLoaded = this.isLoaded();
    super.load();
    if (wasLoaded) {
      return;
    }
    String TEMPLATE = "${timePicker}";
    WTemplate t = new WTemplate(new WString(TEMPLATE), (WContainerWidget) null);
    t.bindWidget("timePicker", this.uTimePicker_);
    this.popup_ = new WPopupWidget(t);
    if (this.isHidden()) {
      this.popup_.setHidden(true);
    }
    this.popup_.setAnchorWidget(this);
    this.popup_.setTransient(true);
    this.scheduleThemeStyleApply(
        WApplication.getInstance().getTheme(), this.popup_, WidgetThemeRole.TimePickerPopup);
    this.escapePressed()
        .addListener(
            this.popup_,
            () -> {
              WTimeEdit.this.popup_.hide();
            });
    this.escapePressed()
        .addListener(
            this,
            () -> {
              WTimeEdit.this.setFocus();
            });
  }
  /**
   * Returns the validator.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Using the validator to change the format while a native control is being
   * used will break the native control. If a native control is used, do not call
   * WTimeValidator()::setFormat(), instead use {@link WTimeEdit#setFormat(String format)
   * setFormat()}. </i>
   *
   * @see WTimeValidator#WTimeValidator()
   * @see WTimeEdit#setFormat(String format)
   */
  public WTimeValidator getTimeValidator() {
    return ObjectUtils.cast(super.getValidator(), WTimeValidator.class);
  }

  protected void render(EnumSet<RenderFlag> flags) {
    if (flags.contains(RenderFlag.Full) && !this.isNativeControl()) {
      this.defineJavaScript();
    }
    super.render(flags);
  }

  protected void propagateSetEnabled(boolean enabled) {
    super.propagateSetEnabled(enabled);
  }

  void updateDom(final DomElement element, boolean all) {
    String step = this.getStep();
    if (step != null) {
      element.setAttribute("step", step);
    }
    if (this.isNativeControl() && this.hasValidatorChanged()) {
      WTimeValidator tv = this.getTimeValidator();
      final WTime bottom = tv.getBottom();
      if ((bottom != null && bottom.isValid())) {
        element.setAttribute("min", bottom.toString(this.getFormat()));
      } else {
        element.removeAttribute("min");
      }
      final WTime top = tv.getTop();
      if ((top != null && top.isValid())) {
        element.setAttribute("max", top.toString(this.getFormat()));
      } else {
        element.removeAttribute("max");
      }
    }
    super.updateDom(element, all);
  }

  protected void validatorChanged() {
    WTimeValidator tv = this.getTimeValidator();
    WTime currentValue = null;
    if (tv != null) {
      currentValue = WTime.fromString(this.getText());
      if (!(currentValue != null && currentValue.isValid())) {
        currentValue = WTime.fromString(this.getText(), HM_FORMAT);
      }
      if (this.oTimePicker_ != null) {
        this.setFormat(tv.getFormat());
      }
      if (this.getStep() != null) {
        if (this.getStep().equals("60")) {
          tv.setStep(Duration.ofSeconds(60));
        } else {
          if (this.getStep().equals("1")) {
            tv.setStep(Duration.ofSeconds(1));
          }
        }
      }
    }
    if (this.isNativeControl()) {
      this.setTime(currentValue);
    }
    super.validatorChanged();
  }

  protected String getType() {
    return this.isNativeControl() ? "time" : super.getType();
  }

  private WPopupWidget popup_;
  private WTimePicker uTimePicker_;
  private WTimePicker oTimePicker_;
  private boolean nativeControl_;

  private void init() {
    this.setValidator(new WTimeValidator());
    this.uTimePicker_ = new WTimePicker(this);
    this.oTimePicker_ = this.uTimePicker_;
    this.oTimePicker_
        .selectionChanged()
        .addListener(
            this,
            () -> {
              WTimeEdit.this.setFromTimePicker();
            });
    this.oTimePicker_.setWrapAroundEnabled(true);
  }

  private void setFromTimePicker() {
    this.setTime(this.oTimePicker_.getTime());
    this.textInput().trigger();
    this.changed().trigger();
  }

  private void setFromLineEdit() {
    WTime t = WTime.fromString(this.getText(), this.getFormat());
    if ((t != null && t.isValid()) && !this.isNativeControl()) {
      this.oTimePicker_.setTime(t);
    }
  }

  private void defineJavaScript() {
    WApplication app = WApplication.getInstance();
    app.loadJavaScript("js/WTimeEdit.js", wtjs1());
    String jsObj =
        "new Wt4_12_1.WTimeEdit("
            + app.getJavaScriptClass()
            + ","
            + this.getJsRef()
            + ","
            + jsStringLiteral(this.popup_.getId())
            + ");";
    this.setJavaScriptMember(" WTimeEdit", jsObj);
    final AbstractEventSignal b = this.mouseMoved();
    final AbstractEventSignal c = this.keyWentDown();
    this.connectJavaScript(this.mouseMoved(), "mouseMove");
    this.connectJavaScript(this.mouseWentUp(), "mouseUp");
    this.connectJavaScript(this.mouseWentDown(), "mouseDown");
    this.connectJavaScript(this.mouseWentOut(), "mouseOut");
  }

  private void connectJavaScript(final AbstractEventSignal s, final String methodName) {
    String jsFunction =
        "function(dobj, event) {var o = "
            + this.getJsRef()
            + ";if(o && o.wtDObj) o.wtDObj."
            + methodName
            + "(dobj, event);}";
    s.addListener(jsFunction);
  }

  private String getStep() {
    if (!this.isNativeControl()) {
      return null;
    }
    if (this.getFormat().equals(HM_FORMAT)) {
      return "60";
    } else {
      if (this.getFormat().equals(HMS_FORMAT)) {
        return "1";
      } else {
        return null;
      }
    }
  }

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WTimeEdit",
        "(function(t,s,i){const o=\"hover\",e=\"active\",n=\"unselectable\";s.wtDObj=this;const c=t.WT;function u(){return s.readOnly}function r(){s.classList.remove(e)}function a(){const t=c.$(i).wtPopup;t.bindHide(r);t.show(s,c.Vertical,!0,!0)}this.mouseOut=function(t,i){s.classList.remove(o)};this.mouseMove=function(t,i){if(u())return;const e=c.widgetCoordinates(s,i).x>s.offsetWidth-40;s.classList.toggle(o,e)};this.mouseDown=function(t,i){if(u())return;if(c.widgetCoordinates(s,i).x>s.offsetWidth-40){s.classList.add(n);s.classList.add(e)}};this.mouseUp=function(t,i){s.classList.remove(n);c.widgetCoordinates(s,i).x>s.offsetWidth-40&&a()}})");
  }

  private static final String HM_FORMAT = "HH:mm";
  private static final String HMS_FORMAT = "HH:mm:ss";
}
