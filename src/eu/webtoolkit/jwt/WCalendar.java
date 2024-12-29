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
 * A calendar.
 *
 * <p>The calendar provides navigation by month and year, and indicates the current day.
 *
 * <p>You can listen for single click or double click events on a calendar cell using the {@link
 * WCalendar#clicked() clicked()} and {@link WCalendar#activated() activated()} methods.
 *
 * <p>The calendar may be configured to allow selection of single or multiple days using {@link
 * WCalendar#setSelectionMode(SelectionMode mode) setSelectionMode()}, and you may listen for
 * changes in the selection using the {@link WCalendar#selectionChanged() selectionChanged()}
 * signals. Selection can also be entirely disabled in which case you can implement your own
 * selection handling by listening for cell click events.
 *
 * <p>Cell rendering may be customized by reimplementing {@link WCalendar#renderCell(WWidget widget,
 * WDate date) renderCell()}.
 *
 * <p>Internationalization is provided by the internationalization features of the {@link
 * eu.webtoolkit.jwt.WDate} class.
 *
 * <p>Here is a snapshot, taken on 19/01/2010 (shown as today), and 14/01/2010 currently selected.
 *
 * <table border="1" cellspacing="3" cellpadding="3">
 * <tr><td><div align="center">
 * <img src="doc-files/WCalendar-default-1.png">
 * <p>
 * <strong>WCalendar with default look</strong></p>
 * </div>
 *
 *
 * </td><td><div align="center">
 * <img src="doc-files/WCalendar-polished-1.png">
 * <p>
 * <strong>WCalendar with polished look</strong></p>
 * </div>
 *
 *
 * </td></tr>
 * </table>
 */
public class WCalendar extends WCompositeWidget {
  private static Logger logger = LoggerFactory.getLogger(WCalendar.class);

  /**
   * Creates a new calendar.
   *
   * <p>Constructs a new calendar with English day/month names. The calendar shows the current day,
   * and has an empty selection.
   */
  public WCalendar(WContainerWidget parentContainer) {
    super();
    this.selection_ = new HashSet<WDate>();
    this.selectionChanged_ = new Signal();
    this.activated_ = new Signal1<WDate>();
    this.clicked_ = new Signal1<WDate>();
    this.currentPageChanged_ = new Signal2<Integer, Integer>();
    this.bottom_ = null;
    this.top_ = null;
    this.create();
    this.impl_.addStyleClass("Wt-calendar");
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Creates a new calendar.
   *
   * <p>Calls {@link #WCalendar(WContainerWidget parentContainer) this((WContainerWidget)null)}
   */
  public WCalendar() {
    this((WContainerWidget) null);
  }
  /**
   * Sets the selection mode.
   *
   * <p>The default selection mode is SingleSelection.
   */
  public void setSelectionMode(SelectionMode mode) {
    if (this.selectionMode_ != mode) {
      if (mode != SelectionMode.Extended && this.selection_.size() > 1) {
        this.selection_.clear();
        this.renderMonth();
      }
      this.selectionMode_ = mode;
    }
  }
  /**
   * Browses to the same month in the previous year.
   *
   * <p>Displays the same month in the previous year. This does not affect the selection.
   *
   * <p>This will emit the {@link WCalendar#currentPageChanged() currentPageChanged()} singal.
   */
  public void browseToPreviousYear() {
    --this.currentYear_;
    this.emitCurrentPageChanged();
    this.renderMonth();
  }
  /**
   * Browses to the previous month.
   *
   * <p>Displays the previous month. This does not affect the selection.
   *
   * <p>This will emit the {@link WCalendar#currentPageChanged() currentPageChanged()} singal.
   */
  public void browseToPreviousMonth() {
    if (--this.currentMonth_ == 0) {
      this.currentMonth_ = 12;
      --this.currentYear_;
    }
    this.emitCurrentPageChanged();
    this.renderMonth();
  }
  /**
   * Browses to the same month in the next year.
   *
   * <p>Displays the same month in the next year. This does not change the current selection.
   *
   * <p>This will emit the {@link WCalendar#currentPageChanged() currentPageChanged()} singal.
   */
  public void browseToNextYear() {
    ++this.currentYear_;
    this.emitCurrentPageChanged();
    this.renderMonth();
  }
  /**
   * Browses to the next month.
   *
   * <p>Displays the next month. This does not change the current selection.
   *
   * <p>This will emit the {@link WCalendar#currentPageChanged() currentPageChanged()} singal.
   */
  public void browseToNextMonth() {
    if (++this.currentMonth_ == 13) {
      this.currentMonth_ = 1;
      ++this.currentYear_;
    }
    this.emitCurrentPageChanged();
    this.renderMonth();
  }
  /**
   * Browses to a date.
   *
   * <p>Displays the month which contains the given date. This does not change the current
   * selection.
   *
   * <p>This will emit the {@link WCalendar#currentPageChanged() currentPageChanged()} signal if
   * another month is displayed.
   */
  public void browseTo(final WDate date) {
    boolean rerender = false;
    if (this.currentYear_ != date.getYear()) {
      this.currentYear_ = date.getYear();
      rerender = true;
    }
    if (this.currentMonth_ != date.getMonth()) {
      this.currentMonth_ = date.getMonth();
      rerender = true;
    }
    if (rerender) {
      this.emitCurrentPageChanged();
      this.renderMonth();
    }
  }
  /**
   * Returns the current month displayed.
   *
   * <p>Returns the month (1-12) that is currently displayed.
   */
  public int getCurrentMonth() {
    return this.currentMonth_;
  }
  /**
   * Returns the current year displayed.
   *
   * <p>Returns the year that is currently displayed.
   */
  public int getCurrentYear() {
    return this.currentYear_;
  }
  /**
   * Clears the current selection.
   *
   * <p>Clears the current selection. Will result in a {@link WCalendar#getSelection()
   * getSelection()} that is empty().
   */
  public void clearSelection() {
    this.selection_.clear();
    this.renderMonth();
  }
  /**
   * Selects a date.
   *
   * <p>Select one date. Both in single or multiple selection mode, this results in a {@link
   * WCalendar#getSelection() getSelection()} that contains exactly one date.
   */
  public void select(final WDate date) {
    this.selection_.clear();
    this.selection_.add(date);
    this.renderMonth();
  }
  /**
   * Selects multiple dates.
   *
   * <p>Select multiple dates. In multiple selection mode, this results in a {@link
   * WCalendar#getSelection() getSelection()} that contains exactly the given dates. In single
   * selection mode, at most one date is set.
   */
  public void select(final Set<WDate> dates) {
    if (this.selectionMode_ == SelectionMode.Extended) {
      this.selection_ = dates;
      this.renderMonth();
    } else {
      if (this.selectionMode_ == SelectionMode.Single) {
        if (dates.isEmpty()) {
          this.clearSelection();
        } else {
          this.select(dates.iterator().next());
        }
      }
    }
  }
  /**
   * Sets the horizontal header format.
   *
   * <p>The default horizontal header format is {@link CalendarHeaderFormat#ShortDayNames}.
   */
  public void setHorizontalHeaderFormat(CalendarHeaderFormat format) {
    String d = "";
    switch (format) {
      case SingleLetterDayNames:
        d = "d1";
        break;
      case ShortDayNames:
        d = "d3";
        break;
      case LongDayNames:
        d = "dlong";
        break;
      default:
        logger.error(
            new StringWriter()
                .append("setHorizontalHeaderFormat(): improper horizontal header format.")
                .toString());
        format = CalendarHeaderFormat.SingleLetterDayNames;
        d = "d1";
    }
    this.horizontalHeaderFormat_ = format;
    this.impl_.bindString("table-class", d, TextFormat.UnsafeXHTML);
    this.setFirstDayOfWeek(this.firstDayOfWeek_);
  }
  /**
   * Returns the horizontal header format.
   *
   * <p>
   *
   * @see WCalendar#setHorizontalHeaderFormat(CalendarHeaderFormat format)
   */
  public CalendarHeaderFormat getHorizontalHeaderFormat() {
    return this.horizontalHeaderFormat_;
  }
  /**
   * Sets the first day of the week.
   *
   * <p>Possible values are 1 to 7. The default value is 1 (&quot;Monday&quot;).
   */
  public void setFirstDayOfWeek(int dayOfWeek) {
    this.firstDayOfWeek_ = dayOfWeek;
    for (int i = 0; i < 7; ++i) {
      int day = (i + this.firstDayOfWeek_ - 1) % 7 + 1;
      WString title = WDate.getLongDayName(day);
      this.impl_.bindString("t" + String.valueOf(i), title, TextFormat.UnsafeXHTML);
      WString abbr = new WString();
      switch (this.horizontalHeaderFormat_) {
        case SingleLetterDayNames:
          abbr = new WString(WDate.getShortDayName(day).toString().substring(0, 0 + 1));
          break;
        case ShortDayNames:
          abbr = WDate.getShortDayName(day);
          break;
        case LongDayNames:
          abbr = WDate.getLongDayName(day);
          break;
      }
      this.impl_.bindString("d" + String.valueOf(i), abbr, TextFormat.UnsafeXHTML);
    }
    this.renderMonth();
  }
  /**
   * Returns the current selection.
   *
   * <p>Returns the set of dates currently selected. In single selection mode, this set contains 0
   * or 1 dates.
   */
  public Set<WDate> getSelection() {
    return this.selection_;
  }
  /**
   * Signal emitted when the user changes the selection.
   *
   * <p>Emitted after the user has changed the current selection.
   */
  public Signal selectionChanged() {
    return this.selectionChanged_;
  }
  /**
   * Signal emitted when the user double-clicks a date.
   *
   * <p>You may want to connect to this signal to treat a double click as the selection of a date.
   */
  public Signal1<WDate> activated() {
    return this.activated_;
  }
  /**
   * Signal emitted when the user clicks a date.
   *
   * <p>You may want to connect to this signal if you want to provide a custom selection handling.
   */
  public Signal1<WDate> clicked() {
    return this.clicked_;
  }
  /**
   * Signal emitted when the current month is changed.
   *
   * <p>The method is emitted both when the change is done through the user interface or via the
   * public API. The two parameters are respectively the new year and month.
   */
  public Signal2<Integer, Integer> currentPageChanged() {
    return this.currentPageChanged_;
  }
  /**
   * Configures the calendar to use single click for activation.
   *
   * <p>By default, double click will trigger activate(). Use this method if you want a single click
   * to trigger activate() (and the now deprecated selected() method). This only applies to a
   * single-selection calendar.
   *
   * <p>If selectionMode() is set to SingleSelection, this will cause the selection to change on a
   * single click instead of a double click.
   *
   * <p>Instead of enabling single click, you can also listen to the {@link WCalendar#clicked()
   * clicked()} signal to process a single click.
   *
   * <p>
   *
   * @see WCalendar#setSelectionMode(SelectionMode mode)
   */
  public void setSingleClickSelect(boolean single) {
    this.singleClickSelect_ = single;
  }
  /**
   * Sets the bottom of the valid date range.
   *
   * <p>The default bottom is null.
   */
  public void setBottom(final WDate bottom) {
    if (!(this.bottom_ == bottom || (this.bottom_ != null && this.bottom_.equals(bottom)))) {
      this.bottom_ = bottom;
      this.renderMonth();
    }
  }
  /** Returns the bottom date of the valid range. */
  public WDate getBottom() {
    return this.bottom_;
  }
  /**
   * Sets the top of the valid date range.
   *
   * <p>The default top is null.
   */
  public void setTop(final WDate top) {
    if (!(this.top_ == top || (this.top_ != null && this.top_.equals(top)))) {
      this.top_ = top;
      this.renderMonth();
    }
  }
  /** Returns the top date of the valid range. */
  public WDate getTop() {
    return this.top_;
  }

  public void load() {
    super.load();
    if (WApplication.getInstance().getEnvironment().hasAjax()) {
      this.monthEdit_.enable();
    }
  }

  protected void render(EnumSet<RenderFlag> flags) {
    if (this.needRenderMonth_) {
      String buf;
      int m = this.currentMonth_ - 1;
      if (this.monthEdit_.getCurrentIndex() != m) {
        this.monthEdit_.setCurrentIndex(m);
      }
      int y = this.currentYear_;
      buf = String.valueOf(y);
      if (!this.yearEdit_.getText().toString().equals(buf)) {
        this.yearEdit_.setText(new WString(buf));
      }
      WDate d = new WDate(this.currentYear_, this.currentMonth_, 1);
      d = d.addDays(-1);
      d = WDate.getPreviousWeekday(d, this.firstDayOfWeek_);
      for (int i = 0; i < 6; ++i) {
        for (int j = 0; j < 7; ++j) {
          buf = String.valueOf(i * 7 + j);
          String cell = "c" + buf;
          WDate date = new WDate(d.getYear(), d.getMonth(), d.getDay());
          WWidget w = this.impl_.resolveWidget(cell);
          WWidget rw = this.renderCell(w, date);
          WInteractWidget iw = ObjectUtils.cast(rw.getWebWidget(), WInteractWidget.class);
          if (rw != w) {
            this.impl_.bindWidget(cell, rw);
          }
          if (iw != null && iw != w) {
            if (this.clicked().isConnected()
                || this.selectionMode_ == SelectionMode.Extended
                || this.selectionMode_ != SelectionMode.Extended
                    && this.singleClickSelect_
                    && this.activated().isConnected()) {
              final WCalendar.Coordinate c = new WCalendar.Coordinate(i, j);
              iw.clicked()
                  .addListener(
                      this,
                      () -> {
                        WCalendar.this.cellClicked(c);
                      });
            }
            if (this.selectionMode_ != SelectionMode.Extended
                && !this.singleClickSelect_
                && (this.activated().isConnected() || this.selectionChanged().isConnected())) {
              final WCalendar.Coordinate c = new WCalendar.Coordinate(i, j);
              iw.doubleClicked()
                  .addListener(
                      this,
                      () -> {
                        WCalendar.this.cellDblClicked(c);
                      });
            }
          }
          d = d.addDays(1);
        }
      }
      this.needRenderMonth_ = false;
    }
    super.render(flags);
  }
  /**
   * Creates or updates a widget that renders a cell.
   *
   * <p>The default implementation creates a {@link WText}
   *
   * <p>You may want to reimplement this method if you wish to customize how a cell is rendered.
   * When <code>widget</code> is <code>null</code>, a new widget should be created and returned.
   * Otherwise, you may either modify the passed <code>widget</code>, or return a new widget. If you
   * return a new widget, the prevoius widget will be deleted.
   */
  protected WWidget renderCell(WWidget widget, final WDate date) {
    WText t = ObjectUtils.cast(widget, WText.class);
    if (!(t != null)) {
      t = new WText();
      t.setInline(false);
      t.setTextFormat(TextFormat.Plain);
    }
    String buf;
    buf = String.valueOf(date.getDay());
    t.setText(new WString(buf));
    String styleClass = "";
    if (this.isInvalid(date)) {
      styleClass += " Wt-cal-oor";
    } else {
      if (date.getMonth() != this.getCurrentMonth()) {
        styleClass += " Wt-cal-oom";
      }
    }
    if (this.isSelected(date)) {
      styleClass += " Wt-cal-sel";
    }
    WDate currentDate = WDate.getCurrentDate();
    if (date.getDay() == currentDate.getDay()
        && date.getMonth() == currentDate.getMonth()
        && date.getYear() == currentDate.getYear()) {
      if (!this.isSelected(date)) {
        styleClass += " Wt-cal-now";
      }
      t.setToolTip(WString.tr("Wt.WCalendar.today"));
    } else {
      t.setToolTip("");
    }
    t.setStyleClass(styleClass);
    return t;
  }
  /**
   * Returns whether a date is selected.
   *
   * <p>This is a convenience method that can be used when reimplementing {@link
   * WCalendar#renderCell(WWidget widget, WDate date) renderCell()}.
   */
  protected boolean isSelected(final WDate d) {
    return this.selection_.contains(d) != false;
  }

  protected void enableAjax() {
    super.enableAjax();
    this.monthEdit_.enable();
  }

  private SelectionMode selectionMode_;
  private boolean singleClickSelect_;
  private int currentYear_;
  private int currentMonth_;
  private CalendarHeaderFormat horizontalHeaderFormat_;
  private int firstDayOfWeek_;
  private Set<WDate> selection_;
  private boolean needRenderMonth_;
  private Signal selectionChanged_;
  private Signal1<WDate> activated_;
  private Signal1<WDate> clicked_;
  private Signal2<Integer, Integer> currentPageChanged_;
  private WDate bottom_;
  private WDate top_;

  static class Coordinate {
    private static Logger logger = LoggerFactory.getLogger(Coordinate.class);

    public int i;
    public int j;

    public Coordinate() {
      this.i = 0;
      this.j = 0;
    }

    public Coordinate(int x, int y) {
      this.i = x;
      this.j = y;
    }
  }

  private WTemplate impl_;
  private WComboBox monthEdit_;
  private WInPlaceEdit yearEdit_;

  private void create() {
    this.selectionMode_ = SelectionMode.Single;
    this.singleClickSelect_ = false;
    this.horizontalHeaderFormat_ = CalendarHeaderFormat.ShortDayNames;
    this.firstDayOfWeek_ = 1;
    WDate currentDay = WDate.getCurrentDate();
    this.currentYear_ = currentDay.getYear();
    this.currentMonth_ = currentDay.getMonth();
    StringBuilder text = new StringBuilder();
    text.append(
        "<table class=\"days ${table-class}\" cellspacing=\"0\" cellpadding=\"0\"><tr><th class=\"caption\">${nav-prev}</th><th class=\"caption\"colspan=\"5\">${month} ${year}</th><th class=\"caption\">${nav-next}</th></tr><tr>");
    for (int j = 0; j < 7; ++j) {
      text.append("<th title=\"${t")
          .append(j)
          .append("}\" scope=\"col\">${d")
          .append(j)
          .append("}</th>");
    }
    text.append("</tr>");
    for (int i = 0; i < 6; ++i) {
      text.append("<tr>");
      for (int j = 0; j < 7; ++j) {
        text.append("<td>${c").append(i * 7 + j).append("}</td>");
      }
      text.append("</tr>");
    }
    text.append("</table>");
    WTemplate t = new WTemplate();
    this.impl_ = t;
    this.setImplementation(t);
    this.impl_.setTemplateText(new WString(text.toString()), TextFormat.UnsafeXHTML);
    this.impl_.setStyleClass("Wt-cal");
    this.setSelectable(false);
    WText prevMonth = new WText(tr("Wt.WCalendar.PrevMonth"), (WContainerWidget) null);
    prevMonth.setStyleClass("Wt-cal-navbutton");
    prevMonth
        .clicked()
        .addListener(
            this,
            (WMouseEvent e1) -> {
              WCalendar.this.browseToPreviousMonth();
            });
    WText nextMonth = new WText(tr("Wt.WCalendar.NextMonth"), (WContainerWidget) null);
    nextMonth.setStyleClass("Wt-cal-navbutton");
    nextMonth
        .clicked()
        .addListener(
            this,
            (WMouseEvent e1) -> {
              WCalendar.this.browseToNextMonth();
            });
    WComboBox monthEdit = new WComboBox();
    this.monthEdit_ = monthEdit;
    monthEdit.setInline(true);
    for (int i = 0; i < 12; ++i) {
      monthEdit.addItem(WDate.getLongMonthName(i + 1));
    }
    monthEdit
        .activated()
        .addListener(
            this,
            (Integer e1) -> {
              WCalendar.this.monthChanged(e1);
            });
    monthEdit.setDisabled(!WApplication.getInstance().getEnvironment().hasAjax());
    WInPlaceEdit yearEdit = new WInPlaceEdit("", (WContainerWidget) null);
    this.yearEdit_ = yearEdit;
    yearEdit.setButtonsEnabled(false);
    yearEdit.getLineEdit().setTextSize(4);
    yearEdit.setStyleClass("Wt-cal-year");
    yearEdit
        .valueChanged()
        .addListener(
            this,
            (WString e1) -> {
              WCalendar.this.yearChanged(e1);
            });
    this.impl_.bindWidget("nav-prev", prevMonth);
    this.impl_.bindWidget("nav-next", nextMonth);
    this.impl_.bindWidget("month", monthEdit);
    this.impl_.bindWidget("year", yearEdit);
    this.setHorizontalHeaderFormat(this.horizontalHeaderFormat_);
    this.setFirstDayOfWeek(this.firstDayOfWeek_);
  }

  private void renderMonth() {
    this.needRenderMonth_ = true;
    if (this.isRendered()) {
      this.scheduleRender();
    }
  }

  private void emitCurrentPageChanged() {
    this.currentPageChanged().trigger(this.currentYear_, this.currentMonth_);
  }

  private void monthChanged(int newMonth) {
    ++newMonth;
    if (this.currentMonth_ != newMonth && (newMonth >= 1 && newMonth <= 12)) {
      this.currentMonth_ = newMonth;
      this.emitCurrentPageChanged();
      this.renderMonth();
    }
  }

  private void yearChanged(CharSequence yearStr) {
    try {
      int year = Integer.parseInt(yearStr.toString());
      if (this.currentYear_ != year && (year >= 1900 && year <= 2200)) {
        this.currentYear_ = year;
        this.emitCurrentPageChanged();
        this.renderMonth();
      }
    } catch (final RuntimeException e) {
    }
  }

  private WDate dateForCell(int week, int dayOfWeek) {
    WDate d = new WDate(this.currentYear_, this.currentMonth_, 1);
    d = d.addDays(-1);
    d = WDate.getPreviousWeekday(d, this.firstDayOfWeek_);
    d = d.addDays(week * 7 + dayOfWeek);
    return d;
  }

  private void selectInCurrentMonth(final WDate d) {
    if (d.getMonth() == this.currentMonth_ && this.selectionMode_ != SelectionMode.None) {
      if (this.selectionMode_ == SelectionMode.Extended) {
        if (this.isSelected(d)) {
          this.selection_.remove(d);
        } else {
          this.selection_.add(d);
        }
      } else {
        this.selection_.clear();
        this.selection_.add(d);
      }
      this.renderMonth();
      this.selectionChanged().trigger();
    }
  }

  private boolean isInvalid(final WDate dt) {
    return !(this.bottom_ == null) && dt.before(this.bottom_)
        || !(this.top_ == null) && dt.after(this.top_);
  }

  private void cellClicked(WCalendar.Coordinate weekday) {
    WDate dt = this.dateForCell(weekday.i, weekday.j);
    if (this.isInvalid(dt)) {
      return;
    }
    this.selectInCurrentMonth(dt);
    this.clicked().trigger(dt);
    if (this.selectionMode_ != SelectionMode.Extended && this.singleClickSelect_) {
      this.activated().trigger(dt);
    }
  }

  private void cellDblClicked(WCalendar.Coordinate weekday) {
    WDate dt = this.dateForCell(weekday.i, weekday.j);
    if (this.isInvalid(dt)) {
      return;
    }
    this.selectInCurrentMonth(dt);
    if (this.selectionMode_ != SelectionMode.Extended && !this.singleClickSelect_) {
      this.activated().trigger(dt);
    }
  }
}
