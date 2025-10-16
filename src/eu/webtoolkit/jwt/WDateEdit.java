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
 * A date edit.
 *
 * <p>A date picker is a line edit with support for date entry (using an icon and a calendar).
 *
 * <p>A {@link WDateValidator} is used to validate date entry.
 *
 * <p>In many cases, it provides a more convenient implementation of a date picker compared to
 * {@link WDatePicker} since it is implemented as a line edit, and a {@link WDateEdit} can be
 * configured as a {@link WDateEdit#setNativeControl(boolean nativeControl) native HTML5 control}.
 *
 * <p>When the native HTML5 control is used, the format is limited to <code>yyyy-MM-dd</code>.
 * Changing to another format has no effect.
 */
public class WDateEdit extends WLineEdit {
  private static Logger logger = LoggerFactory.getLogger(WDateEdit.class);

  /** Creates a new date edit. */
  public WDateEdit(WContainerWidget parentContainer) {
    super();
    this.popup_ = null;
    this.uCalendar_ = null;
    this.oCalendar_ = null;
    this.customFormat_ = false;
    this.nativeControl_ = false;
    this.changed()
        .addListener(
            this,
            () -> {
              WDateEdit.this.setFromLineEdit();
            });
    this.init();
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a new date edit.
   *
   * <p>Calls {@link #WDateEdit(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WDateEdit() {
    this((WContainerWidget) null);
  }

  public void remove() {
    super.remove();
  }
  /**
   * Changes whether a native HTML5 control is used.
   *
   * <p>When enabled the browser&apos;s native date input (&lt;input type=&quot;date&quot;&gt;) will
   * be used, if available. This should provide a better experience on mobile browsers. This option
   * is set to false by default.
   *
   * <p>Setting native control to true limits the format to &quot;yyyy-MM-dd&quot;. Note that this
   * is the format that the widget returns, not the format the user will see. This format is decided
   * by the browser based on the user&apos;s locale.
   *
   * <p>There is no support for changing whether a native control is used after the widget is
   * rendered.
   *
   * <p>
   *
   * @see WDateEdit#isNativeControl()
   */
  public void setNativeControl(boolean nativeControl) {
    this.setFormat(YMD_FORMAT);
    this.nativeControl_ = nativeControl;
    if (nativeControl) {
      this.uCalendar_ = (WCalendar) null;
      this.popup_ = (WPopupWidget) null;
    } else {
      this.flags_.clear(BIT_LOADED);
      this.init();
      this.load();
    }
  }
  /**
   * Returns whether a native HTML5 control is used.
   *
   * <p>Taking into account the preference for a native control, configured using {@link
   * WDateEdit#setNativeControl(boolean nativeControl) setNativeControl()}, this method returns
   * whether a native control is actually being used.
   *
   * <p>
   *
   * @see WDateEdit#setNativeControl(boolean nativeControl)
   */
  public boolean isNativeControl() {
    return this.nativeControl_;
  }
  /**
   * Sets the date.
   *
   * <p>Does nothing if the current date is <code>Null</code>.
   *
   * <p>
   *
   * @see WDateEdit#getDate()
   */
  public void setDate(final WDate date) {
    if (this.isNativeControl()) {
      this.setText(date.toString(this.getFormat()));
      return;
    }
    if (!(date == null)) {
      this.setText(date.toString(this.getFormat()));
      this.oCalendar_.select(date);
      this.oCalendar_.browseTo(date);
    }
  }
  /**
   * Returns the date.
   *
   * <p>Reads the current date.
   *
   * <p>Returns <code>null</code> if the date could not be parsed using the current {@link
   * WDateEdit#getFormat() getFormat()}. <br>
   *
   * <p>
   *
   * @see WDateEdit#setDate(WDate date)
   * @see WLineEdit#getText()
   */
  public WDate getDate() {
    return WDate.fromString(this.getText(), this.getFormat());
  }
  /**
   * Returns the validator.
   *
   * <p>Most of the configuration of the date edit is stored in the validator.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Using the validator to change the format while a native control is being
   * used will break the native control. If a native control is used, do not call
   * WDateValidator()::setFormat(), instead use {@link WDateEdit#setFormat(String format)
   * setFormat()}. </i>
   *
   * @see WDateValidator#WDateValidator()
   * @see WDateEdit#setFormat(String format)
   */
  public WDateValidator getDateValidator() {
    return ObjectUtils.cast(super.getValidator(), WDateValidator.class);
  }
  /**
   * Sets the format used for representing the date.
   *
   * <p>This sets the format in the validator.
   *
   * <p>The default format is based on the current WLocale.
   *
   * <p>The format is set and limited to &quot;yyyy-MM-dd&quot; when using the native HTML5 control.
   * Changing to another format has no effect.
   *
   * <p>
   *
   * @see WDateValidator#setFormat(String format)
   * @see WDateEdit#setNativeControl(boolean nativeControl)
   */
  public void setFormat(final String format) {
    WDateValidator dv = this.getDateValidator();
    if (dv != null) {
      if (!this.isNativeControl()) {
        WDate d = this.getDate();
        dv.setFormat(format);
        this.setDate(d);
        this.customFormat_ = true;
      } else {
        logger.warn(
            new StringWriter()
                .append("setFormat() ignored since nativeControl() is true")
                .toString());
      }
    } else {
      logger.warn(
          new StringWriter()
              .append("setFormat() ignored since validator is not a WDateValidator")
              .toString());
    }
  }
  /**
   * Returns the format.
   *
   * <p>
   *
   * @see WDateEdit#setFormat(String format)
   */
  public String getFormat() {
    WDateValidator dv = this.getDateValidator();
    if (dv != null) {
      return dv.getFormat();
    } else {
      logger.warn(
          new StringWriter()
              .append("format() is bogus  since validator is not a WDateValidator")
              .toString());
      return "";
    }
  }
  /**
   * Sets the lower limit of the valid date range.
   *
   * <p>This sets the lower limit of the valid date range in the validator.
   *
   * <p>
   *
   * @see WDateValidator#setBottom(WDate bottom)
   */
  public void setBottom(final WDate bottom) {
    WDateValidator dv = this.getDateValidator();
    if (dv != null) {
      dv.setBottom(bottom);
    } else {
      if (!this.isNativeControl()) {
        this.oCalendar_.setBottom(bottom);
      }
    }
  }
  /**
   * Returns the lower limit of the valid date range.
   *
   * <p>
   *
   * @see WDateEdit#setBottom(WDate bottom)
   */
  public WDate getBottom() {
    if (this.isNativeControl()) {
      WDateValidator dv = this.getDateValidator();
      if (dv != null) {
        return dv.getBottom();
      }
      return null;
    }
    return this.oCalendar_.getBottom();
  }
  /**
   * Sets the upper limit of the valid date range.
   *
   * <p>This sets the upper limit of the valid date range in the validator.
   *
   * <p>
   *
   * @see WDateValidator#setTop(WDate top)
   */
  public void setTop(final WDate top) {
    WDateValidator dv = this.getDateValidator();
    if (dv != null) {
      dv.setTop(top);
    } else {
      if (!this.isNativeControl()) {
        this.oCalendar_.setTop(top);
      }
    }
  }
  /**
   * Returns the upper limit of the valid range.
   *
   * <p>
   *
   * @see WDateEdit#setTop(WDate top)
   */
  public WDate getTop() {
    if (this.isNativeControl()) {
      WDateValidator dv = this.getDateValidator();
      if (dv != null) {
        return dv.getTop();
      }
      return null;
    }
    return this.oCalendar_.getTop();
  }
  /**
   * Returns the calendar widget.
   *
   * <p>The calendar may be 0 (e.g. when using a native date entry widget).
   */
  public WCalendar getCalendar() {
    return this.oCalendar_;
  }
  /** Hide/unhide the widget. */
  public void setHidden(boolean hidden, final WAnimation animation) {
    super.setHidden(hidden, animation);
    if (this.popup_ != null && hidden) {
      this.popup_.setHidden(hidden, animation);
    }
  }

  public void load() {
    boolean wasLoaded = this.isLoaded();
    super.load();
    if (wasLoaded || this.isNativeControl()) {
      return;
    }
    String TEMPLATE = "${calendar}";
    WTemplate t = new WTemplate(new WString(TEMPLATE), (WContainerWidget) null);
    WTemplate temp = t;
    this.popup_ = new WPopupWidget(t);
    if (this.isHidden()) {
      this.popup_.setHidden(true);
    }
    this.popup_.setAnchorWidget(this);
    this.popup_.setTransient(true);
    this.oCalendar_
        .activated()
        .addListener(
            this.popup_,
            (WDate e1) -> {
              WDateEdit.this.popup_.hide();
            });
    temp.bindWidget("calendar", this.uCalendar_);
    this.scheduleThemeStyleApply(
        WApplication.getInstance().getTheme(), this.popup_, WidgetThemeRole.DatePickerPopup);
    this.escapePressed()
        .addListener(
            this.popup_,
            () -> {
              WDateEdit.this.popup_.hide();
            });
    this.escapePressed()
        .addListener(
            this,
            () -> {
              WDateEdit.this.setFocusTrue();
            });
  }

  public void refresh() {
    super.refresh();
    WDateValidator dv = this.getDateValidator();
    if (!this.customFormat_ && dv != null) {
      WDate d = this.getDate();
      dv.setFormat(LocaleUtils.getDateFormat(WApplication.getInstance().getLocale()));
      this.setDate(d);
    } else {
      logger.warn(
          new StringWriter()
              .append("setFormat() ignored since validator is not a WDateValidator")
              .toString());
    }
  }

  protected void render(EnumSet<RenderFlag> flags) {
    if (flags.contains(RenderFlag.Full) && !this.isNativeControl()) {
      this.defineJavaScript();
      WDateValidator dv = this.getDateValidator();
      if (dv != null) {
        this.setTop(dv.getTop());
        this.setBottom(dv.getBottom());
      }
    }
    super.render(flags);
  }

  protected void propagateSetEnabled(boolean enabled) {
    super.propagateSetEnabled(enabled);
  }

  protected void validatorChanged() {
    WDateValidator dv = this.getDateValidator();
    if (dv != null && !this.isNativeControl()) {
      this.oCalendar_.setBottom(dv.getBottom());
      this.oCalendar_.setTop(dv.getTop());
    }
    super.validatorChanged();
  }

  protected String getType() {
    return this.isNativeControl() ? "date" : super.getType();
  }

  void updateDom(final DomElement element, final boolean all) {
    if (this.isNativeControl() && this.hasValidatorChanged()) {
      WDateValidator dv = this.getDateValidator();
      if (dv != null) {
        final WDate bottom = dv.getBottom();
        if ((bottom != null)) {
          element.setAttribute("min", bottom.toString(YMD_FORMAT));
        } else {
          element.removeAttribute("min");
        }
        final WDate top = dv.getTop();
        if ((top != null)) {
          element.setAttribute("max", top.toString(YMD_FORMAT));
        } else {
          element.removeAttribute("max");
        }
      }
    }
    super.updateDom(element, all);
  }
  /** Sets the value from the calendar to the line edit. */
  protected void setFromCalendar() {
    if (this.isNativeControl()) {
      return;
    }
    if (!this.oCalendar_.getSelection().isEmpty()) {
      WDate calDate = this.oCalendar_.getSelection().iterator().next();
      this.setText(calDate.toString(this.getFormat()));
      this.textInput().trigger();
      this.changed().trigger();
    }
  }
  /** Sets the value from the line edit to the calendar. */
  protected void setFromLineEdit() {
    if (this.isNativeControl()) {
      return;
    }
    WDate d = WDate.fromString(this.getText(), this.getFormat());
    if ((d != null)) {
      if (this.oCalendar_.getSelection().isEmpty()) {
        this.oCalendar_.select(d);
        this.oCalendar_.selectionChanged().trigger();
      } else {
        WDate j = this.oCalendar_.getSelection().iterator().next();
        if (!(j == d || (j != null && j.equals(d)))) {
          this.oCalendar_.select(d);
          this.oCalendar_.selectionChanged().trigger();
        }
      }
      this.oCalendar_.browseTo(d);
    }
  }

  private WPopupWidget popup_;
  private WCalendar uCalendar_;
  private WCalendar oCalendar_;
  private boolean customFormat_;
  private boolean nativeControl_;

  private void init() {
    this.uCalendar_ = new WCalendar();
    this.oCalendar_ = this.uCalendar_;
    this.oCalendar_.setSingleClickSelect(true);
    this.oCalendar_
        .activated()
        .addListener(
            this,
            (WDate e1) -> {
              WDateEdit.this.setFocusTrue();
            });
    this.oCalendar_
        .selectionChanged()
        .addListener(
            this,
            () -> {
              WDateEdit.this.setFromCalendar();
            });
    this.setValidator(
        new WDateValidator(LocaleUtils.getDateFormat(WApplication.getInstance().getLocale())));
  }

  private void defineJavaScript() {
    WApplication app = WApplication.getInstance();
    app.loadJavaScript("js/WDateEdit.js", wtjs1());
    String jsObj =
        "new Wt4_12_1.WDateEdit("
            + app.getJavaScriptClass()
            + ","
            + this.getJsRef()
            + ","
            + jsStringLiteral(this.popup_.getId())
            + ");";
    this.setJavaScriptMember(" WDateEdit", jsObj);
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
            + ";if (o && o.wtDObj) o.wtDObj."
            + methodName
            + "(dobj, event);}";
    s.addListener(jsFunction);
  }

  private void setFocusTrue() {
    this.setFocus(true);
  }

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WDateEdit",
        "(function(t,s,i){const o=\"hover\",e=\"active\",n=\"unselectable\";s.wtDObj=this;const c=t.WT;function u(){return s.readOnly}function a(){s.classList.remove(e)}function r(){const t=c.$(i).wtPopup;t.bindHide(a);t.show(s,c.Vertical,!0,!0)}this.mouseOut=function(t,i){s.classList.remove(o)};this.mouseMove=function(t,i){if(u())return;const e=c.widgetCoordinates(s,i).x>s.offsetWidth-40;s.classList.toggle(o,e)};this.mouseDown=function(t,i){if(u())return;if(c.widgetCoordinates(s,i).x>s.offsetWidth-40){s.classList.add(n);s.classList.add(e)}};this.mouseUp=function(t,i){s.classList.remove(n);c.widgetCoordinates(s,i).x>s.offsetWidth-40&&r()}})");
  }

  private static final String YMD_FORMAT = "yyyy-MM-dd";
}
