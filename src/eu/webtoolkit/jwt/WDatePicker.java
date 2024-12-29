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
 * A date picker.
 *
 * <p>A date picker shows a line edit and an icon which when clicked popups a {@link WCalendar} for
 * editing the date. Any date entered in the line edit is reflected in the calendar, and vice-versa.
 *
 * <p>Each of these widgets may be accessed individually ({@link WDatePicker#getLineEdit()
 * getLineEdit()}, {@link WDatePicker#getCalendar() getCalendar()}, and {@link
 * WDatePicker#getDisplayWidget() getDisplayWidget()}) and there is a constructor that allows you to
 * specify an existing line edit and display widget.
 *
 * <p>The date format used by default is <code>&quot;dd/MM/yyyy&quot;</code> and can be changed
 * using {@link WDatePicker#setFormat(String format) setFormat()}. At any time, the date set may be
 * read using {@link WDatePicker#getDate() getDate()}, or can be changed using {@link
 * WDatePicker#setDate(WDate date) setDate()}.
 *
 * <p>
 *
 * <h3>i18n</h3>
 *
 * <p>Internationalization of {@link WDatePicker} is mostly handled through the internationalization
 * mechanism of {@link eu.webtoolkit.jwt.WDate}.
 *
 * <p>
 *
 * @deprecated The date picker is deprecated in favor of {@link WDateEdit}
 */
public class WDatePicker extends WCompositeWidget {
  private static Logger logger = LoggerFactory.getLogger(WDatePicker.class);

  /**
   * Create a new date picker.
   *
   * <p>This constructor creates a line edit with an icon that leads to a popup calendar. A {@link
   * WDateValidator} is configured for the line edit.
   */
  public WDatePicker(WContainerWidget parentContainer) {
    super();
    this.format_ = "";
    this.popup_ = null;
    this.popupClosed_ = new Signal();
    this.changed_ = new Signal();
    this.positionJS_ = new JSlot();
    this.create((WInteractWidget) null, (WLineEdit) null);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Create a new date picker.
   *
   * <p>Calls {@link #WDatePicker(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WDatePicker() {
    this((WContainerWidget) null);
  }
  /**
   * Create a new date picker for a line edit.
   *
   * <p>This constructor creates an icon that leads to a popup calendar.
   *
   * <p>The <code>forEdit</code> argument is the lineEdit that works in conjunction with the date
   * picker. This widget does not become part of the date picker, and may be located anywhere else.
   */
  public WDatePicker(WLineEdit forEdit, WContainerWidget parentContainer) {
    super();
    this.format_ = "";
    this.popup_ = null;
    this.popupClosed_ = new Signal();
    this.changed_ = new Signal();
    this.positionJS_ = new JSlot();
    this.create((WInteractWidget) null, forEdit);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Create a new date picker for a line edit.
   *
   * <p>Calls {@link #WDatePicker(WLineEdit forEdit, WContainerWidget parentContainer) this(forEdit,
   * (WContainerWidget)null)}
   */
  public WDatePicker(WLineEdit forEdit) {
    this(forEdit, (WContainerWidget) null);
  }
  /**
   * Create a new date picker for existing line edit and with custom display widget.
   *
   * <p>The <code>displayWidget</code> is a button or image which much be clicked to open the date
   * picker.
   *
   * <p>The <code>forEdit</code> argument is the lineEdit that works in conjunction with the date
   * picker.
   */
  public WDatePicker(
      WInteractWidget displayWidget, WLineEdit forEdit, WContainerWidget parentContainer) {
    super();
    this.format_ = "";
    this.popup_ = null;
    this.popupClosed_ = new Signal();
    this.changed_ = new Signal();
    this.positionJS_ = new JSlot();
    this.create(displayWidget, forEdit);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Create a new date picker for existing line edit and with custom display widget.
   *
   * <p>Calls {@link #WDatePicker(WInteractWidget displayWidget, WLineEdit forEdit, WContainerWidget
   * parentContainer) this(displayWidget, forEdit, (WContainerWidget)null)}
   */
  public WDatePicker(WInteractWidget displayWidget, WLineEdit forEdit) {
    this(displayWidget, forEdit, (WContainerWidget) null);
  }
  /** Destructor. */
  public void remove() {
    super.remove();
  }
  /**
   * Returns the validator.
   *
   * <p>Most of the configuration of the date edit is stored in the validator.
   */
  public WDateValidator getDateValidator() {
    return ObjectUtils.cast(this.forEdit_.getValidator(), WDateValidator.class);
  }
  /**
   * Sets the format used for parsing or writing the date in the line edit.
   *
   * <p>Sets the format used for representing the date in the line edit. If the line edit has a
   * {@link WDateValidator} configured for it, then also there the format is updated.
   *
   * <p>The default format is <code>&apos;dd/MM/yyyy&apos;</code>.
   *
   * <p>
   *
   * @see WDatePicker#getFormat()
   * @see WDate#toString()
   */
  public void setFormat(final String format) {
    WDate d = this.getDate();
    this.format_ = format;
    WDateValidator dv = this.getDateValidator();
    if (dv != null) {
      dv.setFormat(format);
    }
    this.setDate(d);
  }
  /**
   * Returns the format.
   *
   * <p>
   *
   * @see WDatePicker#setFormat(String format)
   */
  public String getFormat() {
    return this.format_;
  }
  /**
   * The calendar widget.
   *
   * <p>Returns the calendar widget.
   */
  public WCalendar getCalendar() {
    return this.calendar_;
  }
  /**
   * The line edit.
   *
   * <p>Returns the line edit which works in conjunction with this date picker.
   */
  public WLineEdit getLineEdit() {
    return this.forEdit_;
  }
  /**
   * The display widget.
   *
   * <p>Returns the icon which activates the popup.
   */
  public WInteractWidget getDisplayWidget() {
    return this.displayWidget_;
  }
  /**
   * The popup widget.
   *
   * <p>Returns the popup widget that contains the calendar.
   */
  public WPopupWidget getPopupWidget() {
    return this.popup_;
  }
  /**
   * The current date.
   *
   * <p>Reads the current date from the {@link WDatePicker#getLineEdit() getLineEdit()}.
   *
   * <p>Returns <code>null</code> if the date could not be parsed using the current {@link
   * WDatePicker#getFormat() getFormat()}. <br>
   *
   * <p>
   *
   * @see WDatePicker#setDate(WDate date)
   * @see WLineEdit#getText()
   */
  public WDate getDate() {
    return WDate.fromString(this.forEdit_.getText(), this.format_);
  }
  /**
   * Sets the current date.
   *
   * <p>Does nothing if the current date is <code>Null</code>.
   *
   * <p>
   *
   * @see WDatePicker#getDate()
   */
  public void setDate(final WDate date) {
    if (!(date == null)) {
      this.forEdit_.setText(date.toString(this.format_));
      this.calendar_.select(date);
      this.calendar_.browseTo(date);
    }
  }
  /**
   * Sets whether the widget is enabled.
   *
   * <p>This is the oppositie of {@link WDatePicker#setDisabled(boolean disabled) setDisabled()}.
   */
  public void setEnabled(boolean enabled) {
    this.setDisabled(!enabled);
  }

  public void setDisabled(boolean disabled) {
    super.setDisabled(disabled);
    this.forEdit_.setDisabled(disabled);
    this.displayWidget_.setHidden(disabled);
  }
  /** Hide/unhide the widget. */
  public void setHidden(boolean hidden, final WAnimation animation) {
    super.setHidden(hidden, animation);
    this.forEdit_.setHidden(hidden, animation);
    this.displayWidget_.setHidden(hidden, animation);
  }
  /** Sets the bottom of the valid date range. */
  public void setBottom(final WDate bottom) {
    WDateValidator dv = this.getDateValidator();
    if (dv != null) {
      dv.setBottom(bottom);
      this.calendar_.setBottom(bottom);
    }
  }
  /** Returns the bottom date of the valid range. */
  public WDate getBottom() {
    WDateValidator dv = this.getDateValidator();
    if (dv != null) {
      return dv.getBottom();
    } else {
      return null;
    }
  }
  /** Sets the top of the valid date range. */
  public void setTop(final WDate top) {
    WDateValidator dv = this.getDateValidator();
    if (dv != null) {
      dv.setTop(top);
      this.calendar_.setTop(top);
    }
  }
  /** Returns the top date of the valid range. */
  public WDate getTop() {
    WDateValidator dv = this.getDateValidator();
    if (dv != null) {
      return dv.getTop();
    } else {
      return null;
    }
  }
  /**
   * Signal emitted when the value has changed.
   *
   * <p>This signal is emitted when a new date has been entered (either through the line edit, or
   * through the calendar popup).
   */
  public Signal changed() {
    return this.changed_;
  }
  /** Shows or hides the popup. */
  public void setPopupVisible(boolean visible) {
    this.popup_.setHidden(!visible);
  }
  /**
   * A signal which indicates that the popup has been closed.
   *
   * <p>The signal is only fired when the popup has been closed by the user.
   */
  public Signal popupClosed() {
    return this.popupClosed_;
  }

  protected void render(EnumSet<RenderFlag> flags) {
    if (flags.contains(RenderFlag.Full)) {
      WDateValidator dv = this.getDateValidator();
      if (dv != null) {
        this.setTop(dv.getTop());
        this.setBottom(dv.getBottom());
        this.setFormat(dv.getFormat());
      }
    }
    super.render(flags);
  }

  private String format_;
  private WInteractWidget displayWidget_;
  private WLineEdit forEdit_;
  private WContainerWidget layout_;
  private WPopupWidget popup_;
  private WCalendar calendar_;
  private Signal popupClosed_;
  private Signal changed_;
  private JSlot positionJS_;

  private void create(WInteractWidget displayWidget, WLineEdit forEdit) {
    WContainerWidget layout = new WContainerWidget();
    this.layout_ = layout;
    this.setImplementation(layout);
    if (!(forEdit != null)) {
      forEdit = new WLineEdit((WContainerWidget) this.layout_);
    }
    if (!(displayWidget != null)) {
      displayWidget = new WImage();
      WApplication.getInstance()
          .getTheme()
          .apply(this, displayWidget, WidgetThemeRole.DatePickerIcon);
    }
    this.displayWidget_ = displayWidget;
    this.forEdit_ = forEdit;
    this.forEdit_.setVerticalAlignment(AlignmentFlag.Middle);
    this.forEdit_
        .changed()
        .addListener(
            this,
            () -> {
              WDatePicker.this.setFromLineEdit();
            });
    this.format_ = "dd/MM/yyyy";
    this.layout_.setInline(true);
    this.layout_.addWidget(displayWidget);
    this.layout_.setAttributeValue("style", "white-space: nowrap");
    this.calendar_ = new WCalendar();
    this.calendar_.setSingleClickSelect(true);
    this.calendar_
        .activated()
        .addListener(
            this,
            (WDate e1) -> {
              WDatePicker.this.onPopupHidden();
            });
    this.calendar_
        .selectionChanged()
        .addListener(
            this,
            () -> {
              WDatePicker.this.setFromCalendar();
            });
    String TEMPLATE = "${calendar}";
    WTemplate temp;
    WTemplate t = temp = new WTemplate(new WString(TEMPLATE), (WContainerWidget) null);
    this.popup_ = new WPopupWidget(t);
    temp.escapePressed()
        .addListener(
            this.popup_,
            () -> {
              WDatePicker.this.popup_.hide();
            });
    temp.escapePressed()
        .addListener(
            this.forEdit_,
            () -> {
              WDatePicker.this.forEdit_.setFocus();
            });
    temp.bindWidget("calendar", this.calendar_);
    this.popup_.setAnchorWidget(this.displayWidget_, Orientation.Horizontal);
    this.popup_.setTransient(true);
    this.calendar_
        .activated()
        .addListener(
            this.popup_,
            (WDate e1) -> {
              WDatePicker.this.popup_.hide();
            });
    WApplication.getInstance().getTheme().apply(this, this.popup_, WidgetThemeRole.DatePickerPopup);
    this.displayWidget_
        .clicked()
        .addListener(
            this.popup_,
            (WMouseEvent e1) -> {
              WDatePicker.this.popup_.show();
            });
    this.displayWidget_
        .clicked()
        .addListener(
            this,
            (WMouseEvent e1) -> {
              WDatePicker.this.setFromLineEdit();
            });
    if (!(this.forEdit_.getValidator() != null)) {
      this.forEdit_.setValidator(new WDateValidator(this.format_));
    }
  }

  private void setFromCalendar() {
    if (!this.calendar_.getSelection().isEmpty()) {
      final WDate calDate = this.calendar_.getSelection().iterator().next();
      this.forEdit_.setText(calDate.toString(this.format_));
      this.forEdit_.textInput().trigger();
      this.forEdit_.changed().trigger();
    }
    this.changed_.trigger();
  }

  private void setFromLineEdit() {
    WDate d = WDate.fromString(this.forEdit_.getText(), this.format_);
    if ((d != null)) {
      if (this.calendar_.getSelection().isEmpty()) {
        this.calendar_.select(d);
        this.calendar_.selectionChanged().trigger();
      } else {
        WDate j = this.calendar_.getSelection().iterator().next();
        if (!(j == d || (j != null && j.equals(d)))) {
          this.calendar_.select(d);
          this.calendar_.selectionChanged().trigger();
        }
      }
      this.calendar_.browseTo(d);
    }
  }

  private void onPopupHidden() {
    this.forEdit_.setFocus(true);
    this.popupClosed();
  }
}
