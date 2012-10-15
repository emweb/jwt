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
 * A calendar.
 * <p>
 * 
 * The calendar provides navigation by month and year, and indicates the current
 * day.
 * <p>
 * You can listen for single click or double click events on a calendar cell
 * using the {@link WCalendar#clicked() clicked()} and
 * {@link WCalendar#activated() activated()} methods.
 * <p>
 * The calendar may be configured to allow selection of single or multiple days
 * using {@link WCalendar#setSelectionMode(SelectionMode mode)
 * setSelectionMode()}, and you may listen for changes in the selection using
 * the {@link WCalendar#selectionChanged() selectionChanged()} signals.
 * Selection can also be entirely disabled in which case you can implement your
 * own selection handling by listening for cell click events.
 * <p>
 * Cell rendering may be customized by reimplementing
 * {@link WCalendar#renderCell(WWidget widget, WDate date) renderCell()}.
 * <p>
 * Internationalization is provided by the internationalization features of the
 * {@link eu.webtoolkit.jwt.WDate} class.
 * <p>
 * Here is a snapshot, taken on 19/01/2010 (shown as today), and 14/01/2010
 * currently selected.
 * <table border="0" align="center" cellspacing="3" cellpadding="3">
 * <tr>
 * <td><div align="center"> <img src="doc-files//WCalendar-default-1.png"
 * alt="WCalendar with default look">
 * <p>
 * <strong>WCalendar with default look</strong>
 * </p>
 * </div></td>
 * <td><div align="center"> <img src="doc-files//WCalendar-polished-1.png"
 * alt="WCalendar with polished look">
 * <p>
 * <strong>WCalendar with polished look</strong>
 * </p>
 * </div></td>
 * </tr>
 * </table>
 * <p>
 * <h3>CSS</h3>
 * <p>
 * The calendar is styled by the current CSS theme. The look can be overridden
 * using the <code>Wt-calendar</code> CSS class and the following selectors:
 * <p>
 * <div class="fragment">
 * 
 * <pre class="fragment">
 * .Wt-cal table       : The table
 * .Wt-cal table.d1    : The table (single letter day headers)
 * .Wt-cal table.d3    : The table (three letter day headers)
 * .Wt-cal table.dlong : The table (long day headers)
 * 
 * .Wt-cal caption	    : The caption (containing the navigation buttons)
 * .Wt-cal-year        : The caption year in-place-edit
 * 
 * .Wt-cal th          : Header cell (week day)
 * 
 * .Wt-cal td          : Day cell
 * .Wt-cal-oom         : Out-of-month day
 * .Wt-cal-oor         : Out-of-range day (day &lt; bottom or day &gt; top)
 * .Wt-cal-sel         : Selected day
 * .Wt-cal-now         : Today day
 * </pre>
 * 
 * </div>
 */
public class WCalendar extends WCompositeWidget {
	private static Logger logger = LoggerFactory.getLogger(WCalendar.class);

	/**
	 * The format of the horizontal header.
	 */
	public enum HorizontalHeaderFormat {
		/**
		 * First letter of a day (e.g. &apos;M&apos; for Monday).
		 */
		SingleLetterDayNames,
		/**
		 * First 3 letters of a day (e.g. &apos;Mon&apos; for Monday).
		 */
		ShortDayNames,
		/**
		 * Full day name.
		 */
		LongDayNames;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Creates a new calendar.
	 * <p>
	 * Constructs a new calendar with English day/month names. The calendar
	 * shows the current day, and has an empty selection.
	 */
	public WCalendar(WContainerWidget parent) {
		super(parent);
		this.selection_ = new HashSet<WDate>();
		this.selectionChanged_ = new Signal(this);
		this.activated_ = new Signal1<WDate>(this);
		this.clicked_ = new Signal1<WDate>(this);
		this.currentPageChanged_ = new Signal2<Integer, Integer>(this);
		this.bottom_ = null;
		this.top_ = null;
		this.create();
	}

	/**
	 * Creates a new calendar.
	 * <p>
	 * Calls {@link #WCalendar(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WCalendar() {
		this((WContainerWidget) null);
	}

	/**
	 * Sets the selection mode.
	 * <p>
	 * The default selection mode is {@link SelectionMode#SingleSelection
	 * SingleSelection}.
	 */
	public void setSelectionMode(SelectionMode mode) {
		if (this.selectionMode_ != mode) {
			if (mode != SelectionMode.ExtendedSelection
					&& this.selection_.size() > 1) {
				this.selection_.clear();
				this.renderMonth();
			}
			this.selectionMode_ = mode;
		}
	}

	/**
	 * Browses to the same month in the previous year.
	 * <p>
	 * Displays the same month in the previous year. This does not affect the
	 * selection.
	 * <p>
	 * This will emit the {@link WCalendar#currentPageChanged()
	 * currentPageChanged()} singal.
	 */
	public void browseToPreviousYear() {
		--this.currentYear_;
		this.emitCurrentPageChanged();
		this.renderMonth();
	}

	/**
	 * Browses to the previous month.
	 * <p>
	 * Displays the previous month. This does not affect the selection.
	 * <p>
	 * This will emit the {@link WCalendar#currentPageChanged()
	 * currentPageChanged()} singal.
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
	 * <p>
	 * Displays the same month in the next year. This does not change the
	 * current selection.
	 * <p>
	 * This will emit the {@link WCalendar#currentPageChanged()
	 * currentPageChanged()} singal.
	 */
	public void browseToNextYear() {
		++this.currentYear_;
		this.emitCurrentPageChanged();
		this.renderMonth();
	}

	/**
	 * Browses to the next month.
	 * <p>
	 * Displays the next month. This does not change the current selection.
	 * <p>
	 * This will emit the {@link WCalendar#currentPageChanged()
	 * currentPageChanged()} singal.
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
	 * <p>
	 * Displays the month which contains the given date. This does not change
	 * the current selection.
	 * <p>
	 * This will emit the {@link WCalendar#currentPageChanged()
	 * currentPageChanged()} signal if another month is displayed.
	 */
	public void browseTo(WDate date) {
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
	 * <p>
	 * Returns the month (1-12) that is currently displayed.
	 */
	public int getCurrentMonth() {
		return this.currentMonth_;
	}

	/**
	 * Returns the current year displayed.
	 * <p>
	 * Returns the year that is currently displayed.
	 */
	public int getCurrentYear() {
		return this.currentYear_;
	}

	/**
	 * Clears the current selection.
	 * <p>
	 * Clears the current selection. Will result in a
	 * {@link WCalendar#getSelection() getSelection()} that is empty().
	 */
	public void clearSelection() {
		this.selection_.clear();
		this.renderMonth();
	}

	/**
	 * Selects a date.
	 * <p>
	 * Select one date. Both in single or multiple selection mode, this results
	 * in a {@link WCalendar#getSelection() getSelection()} that contains
	 * exactly one date.
	 */
	public void select(WDate date) {
		this.selection_.clear();
		this.selection_.add(date);
		this.renderMonth();
	}

	/**
	 * Selects multiple dates.
	 * <p>
	 * Select multiple dates. In multiple selection mode, this results in a
	 * {@link WCalendar#getSelection() getSelection()} that contains exactly the
	 * given dates. In single selection mode, at most one date is set.
	 */
	public void select(Set<WDate> dates) {
		if (this.selectionMode_ == SelectionMode.ExtendedSelection) {
			this.selection_ = dates;
			this.renderMonth();
		} else {
			if (this.selectionMode_ == SelectionMode.SingleSelection) {
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
	 * <p>
	 * The default horizontal header format is
	 * {@link WCalendar.HorizontalHeaderFormat#ShortDayNames}.
	 */
	public void setHorizontalHeaderFormat(
			WCalendar.HorizontalHeaderFormat format) {
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
			logger
					.error(new StringWriter()
							.append(
									"setHorizontalHeaderFormat(): improper horizontal header format.")
							.toString());
			format = WCalendar.HorizontalHeaderFormat.SingleLetterDayNames;
			d = "d1";
		}
		this.horizontalHeaderFormat_ = format;
		this.impl_.bindString("table-class", d, TextFormat.XHTMLUnsafeText);
		this.setFirstDayOfWeek(this.firstDayOfWeek_);
	}

	/**
	 * Returns the horizontal header format.
	 * <p>
	 * 
	 * @see WCalendar#setHorizontalHeaderFormat(WCalendar.HorizontalHeaderFormat
	 *      format)
	 */
	public WCalendar.HorizontalHeaderFormat getHorizontalHeaderFormat() {
		return this.horizontalHeaderFormat_;
	}

	/**
	 * Sets the first day of the week.
	 * <p>
	 * Possible values or 1 to 7, as accepted by
	 * {@link WDate#getShortDayName(int weekday, boolean localized)
	 * WDate#getShortDayName()}.
	 * <p>
	 * The default value is 1 (&quot;Monday&quot;).
	 */
	public void setFirstDayOfWeek(int dayOfWeek) {
		this.firstDayOfWeek_ = dayOfWeek;
		for (int i = 0; i < 7; ++i) {
			int day = (i + this.firstDayOfWeek_ - 1) % 7 + 1;
			WString title = new WString(WDate.getLongDayName(day));
			this.impl_.bindString("t" + String.valueOf(i), title,
					TextFormat.XHTMLUnsafeText);
			WString abbr = new WString();
			switch (this.horizontalHeaderFormat_) {
			case SingleLetterDayNames:
				abbr = new WString(WDate.getShortDayName(day).substring(0,
						0 + 1));
				break;
			case ShortDayNames:
				abbr = new WString(WDate.getShortDayName(day));
				break;
			case LongDayNames:
				abbr = new WString(WDate.getLongDayName(day));
				break;
			}
			this.impl_.bindString("d" + String.valueOf(i), abbr,
					TextFormat.XHTMLUnsafeText);
		}
		this.renderMonth();
	}

	/**
	 * Returns the current selection.
	 * <p>
	 * Returns the set of dates currently selected. In single selection mode,
	 * this set contains 0 or 1 dates.
	 */
	public Set<WDate> getSelection() {
		return this.selection_;
	}

	/**
	 * Signal emitted when the user changes the selection.
	 * <p>
	 * Emitted after the user has changed the current selection.
	 */
	public Signal selectionChanged() {
		return this.selectionChanged_;
	}

	/**
	 * Signal emitted when the user double-clicks a date.
	 * <p>
	 * You may want to connect to this signal to treat a double click as the
	 * selection of a date.
	 */
	public Signal1<WDate> activated() {
		return this.activated_;
	}

	/**
	 * Signal emitted when the user clicks a date.
	 * <p>
	 * You may want to connect to this signal if you want to provide a custom
	 * selection handling.
	 */
	public Signal1<WDate> clicked() {
		return this.clicked_;
	}

	/**
	 * Signal emitted when the current month is changed.
	 * <p>
	 * The method is emitted both when the change is done through the user
	 * interface or via the public API. The two parameters are respectively the
	 * new year and month.
	 */
	public Signal2<Integer, Integer> currentPageChanged() {
		return this.currentPageChanged_;
	}

	/**
	 * Configures single or multiple selection mode (<b> deprecated </b>).
	 * <p>
	 * In single selection mode, only one date may be selected: the
	 * {@link WCalendar#getSelection() getSelection()} will be empty or contain
	 * exactly one item.
	 * <p>
	 * 
	 * @deprecated use {@link WCalendar#setSelectionMode(SelectionMode mode)
	 *             setSelectionMode()} instead.
	 */
	public void setMultipleSelection(boolean multiple) {
		this.setSelectionMode(multiple ? SelectionMode.ExtendedSelection
				: SelectionMode.SingleSelection);
	}

	/**
	 * Signal emitted when the user has double clicked on a date
	 * (<b>deprecated</b>).
	 * <p>
	 * This signal indicates that he user has selected a new date, which is only
	 * available when in single selection mode.
	 * <p>
	 * 
	 * @deprecated use {@link WCalendar#activated() activated()} instead.
	 */
	public Signal1<WDate> selected() {
		return this.activated_;
	}

	/**
	 * Configures the calendar to use single click for activation
	 * (<b>deprecated</b>).
	 * <p>
	 * By default, double click will trigger activate(). Use this method if you
	 * want a single click to trigger activate() (and the now deprecated
	 * {@link WCalendar#selected() selected()} method). This only applies to a
	 * single-selection calendar.
	 * <p>
	 * 
	 * @deprecated listen to the {@link WCalendar#clicked() clicked()} signal if
	 *             you want to react to a single click, or
	 *             {@link WCalendar#activated() activated()} signal if you want
	 *             to react to a double click.
	 * @see WCalendar#setMultipleSelection(boolean multiple)
	 */
	public void setSingleClickSelect(boolean single) {
		this.singleClickSelect_ = single;
	}

	/**
	 * Sets the length for the abbreviated day of week (<b> deprecated </b>).
	 * <p>
	 * The <code>chars</code> may be 1 or 3, which render &quot;Monday&quot; as
	 * respectively &quot;M&quot; or &quot;Mon&quot;.
	 * <p>
	 * The default length is 3.
	 * <p>
	 * 
	 * @deprecated use
	 *             {@link WCalendar#setHorizontalHeaderFormat(WCalendar.HorizontalHeaderFormat format)
	 *             setHorizontalHeaderFormat()} instead.
	 */
	public void setDayOfWeekLength(int chars) {
		this
				.setHorizontalHeaderFormat(chars == 3 ? WCalendar.HorizontalHeaderFormat.ShortDayNames
						: WCalendar.HorizontalHeaderFormat.SingleLetterDayNames);
	}

	/**
	 * Sets the bottom of the valid date range.
	 * <p>
	 * The default bottom is null.
	 */
	public void setBottom(WDate bottom) {
		if (!(this.bottom_ == bottom || (this.bottom_ != null && this.bottom_
				.equals(bottom)))) {
			this.bottom_ = bottom;
			this.renderMonth();
		}
	}

	/**
	 * Returns the bottom date of the valid range.
	 */
	public WDate getBottom() {
		return this.bottom_;
	}

	/**
	 * Sets the top of the valid date range.
	 * <p>
	 * The default top is null.
	 */
	public void setTop(WDate top) {
		if (!(this.top_ == top || (this.top_ != null && this.top_.equals(top)))) {
			this.top_ = top;
			this.renderMonth();
		}
	}

	/**
	 * Returns the top date of the valid range.
	 */
	public WDate getTop() {
		return this.top_;
	}

	void render(EnumSet<RenderFlag> flags) {
		if (this.needRenderMonth_) {
			boolean create = this.cellClickMapper_ == null;
			String buf;
			if (create) {
				this.cellClickMapper_ = new WSignalMapper1<WCalendar.Coordinate>(
						this);
				this.cellClickMapper_.mapped().addListener(this,
						new Signal1.Listener<WCalendar.Coordinate>() {
							public void trigger(WCalendar.Coordinate e1) {
								WCalendar.this.cellClicked(e1);
							}
						});
				this.cellDblClickMapper_ = new WSignalMapper1<WCalendar.Coordinate>(
						this);
				this.cellDblClickMapper_.mapped().addListener(this,
						new Signal1.Listener<WCalendar.Coordinate>() {
							public void trigger(WCalendar.Coordinate e1) {
								WCalendar.this.cellDblClicked(e1);
							}
						});
			}
			int m = this.currentMonth_ - 1;
			if (this.monthEdit_.getCurrentIndex() != m) {
				this.monthEdit_.setCurrentIndex(m);
			}
			int y = this.currentYear_;
			buf = String.valueOf(y);
			if (!this.yearEdit_.getText().toString().equals(buf)) {
				this.yearEdit_.setText(new WString(buf));
			}
			WDate todayd = WDate.getCurrentDate();
			WDate today = new WDate(todayd.getYear(), todayd.getMonth(), todayd
					.getDay());
			WDate d = new WDate(this.currentYear_, this.currentMonth_, 1);
			d = d.addDays(-1);
			WDate.Day gw = WDate.Day.fromInt(this.firstDayOfWeek_ % 7);
			d = WDate.getPreviousWeekday(d, gw);
			for (int i = 0; i < 6; ++i) {
				for (int j = 0; j < 7; ++j) {
					buf = String.valueOf(i * 7 + j);
					String cell = "c" + buf;
					WDate date = new WDate(d.getYear(), d.getMonth(), d
							.getDay());
					WWidget w = this.impl_.resolveWidget(cell);
					WWidget rw = this.renderCell(w, date);
					this.impl_.bindWidget(cell, rw);
					WInteractWidget iw = ((rw.getWebWidget()) instanceof WInteractWidget ? (WInteractWidget) (rw
							.getWebWidget())
							: null);
					if (iw != null && iw != w) {
						this.cellClickMapper_.mapConnect(iw.clicked(),
								new WCalendar.Coordinate(i, j));
						this.cellDblClickMapper_.mapConnect(iw.doubleClicked(),
								new WCalendar.Coordinate(i, j));
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
	 * <p>
	 * The default implementation creates a {@link WText}
	 * <p>
	 * You may want to reimplement this method if you wish to customize how a
	 * cell is rendered. When <code>widget</code> is <code>null</code>, a new
	 * widget should be created and returned. Otherwise, you may either modify
	 * the passed <code>widget</code>, or return a new widget. If you return a
	 * new widget, the prevoius widget will be deleted.
	 */
	protected WWidget renderCell(WWidget widget, WDate date) {
		WText t = ((widget) instanceof WText ? (WText) (widget) : null);
		if (!(t != null)) {
			t = new WText();
			t.setInline(false);
			t.setTextFormat(TextFormat.PlainText);
		}
		String buf;
		buf = String.valueOf(date.getDay());
		t.setText(new WString(buf));
		String styleClass = "";
		if (!(this.bottom_ == null) && date.before(this.bottom_)
				|| !(this.top_ == null) && date.after(this.top_)) {
			styleClass += " Wt-cal-oor";
		} else {
			if (date.getMonth() != this.getCurrentMonth()) {
				styleClass += " Wt-cal-oom";
			}
		}
		if (this.isSelected(date)) {
			styleClass += " Wt-cal-sel";
		}
		if ((date == WDate.getCurrentDate() || (date != null && date
				.equals(WDate.getCurrentDate())))) {
			if (!this.isSelected(date)) {
				styleClass += " Wt-cal-now";
			}
			t.setToolTip("Today");
		} else {
			t.setToolTip("");
		}
		t.setStyleClass(styleClass);
		return t;
	}

	/**
	 * Returns whether a date is selected.
	 * <p>
	 * This is a convenience method that can be used when reimplementing
	 * {@link WCalendar#renderCell(WWidget widget, WDate date) renderCell()}.
	 */
	protected boolean isSelected(WDate d) {
		return this.selection_.contains(d) != false;
	}

	private SelectionMode selectionMode_;
	private boolean singleClickSelect_;
	private int currentYear_;
	private int currentMonth_;
	private WCalendar.HorizontalHeaderFormat horizontalHeaderFormat_;
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
		private static Logger logger = LoggerFactory
				.getLogger(Coordinate.class);

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
	private WSignalMapper1<WCalendar.Coordinate> cellClickMapper_;
	private WSignalMapper1<WCalendar.Coordinate> cellDblClickMapper_;

	private void create() {
		this.selectionMode_ = SelectionMode.SingleSelection;
		this.singleClickSelect_ = false;
		this.horizontalHeaderFormat_ = WCalendar.HorizontalHeaderFormat.ShortDayNames;
		this.firstDayOfWeek_ = 1;
		this.cellClickMapper_ = null;
		this.cellDblClickMapper_ = null;
		this.clicked().addListener(this, new Signal1.Listener<WDate>() {
			public void trigger(WDate e1) {
				WCalendar.this.selectInCurrentMonth(e1);
			}
		});
		WDate currentDay = WDate.getCurrentDate();
		this.currentYear_ = currentDay.getYear();
		this.currentMonth_ = currentDay.getMonth();
		StringBuilder text = new StringBuilder();
		text
				.append("<table class=\"${table-class}\" cellspacing=\"0\" cellpadding=\"0\"><caption>${nav-prev} ${month} ${year} ${nav-next}</caption><tr>");
		for (int j = 0; j < 7; ++j) {
			text.append("<th title=\"${t").append(j).append(
					"}\" scope=\"col\">${d").append(j).append("}</th>");
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
		this.setImplementation(this.impl_ = new WTemplate());
		this.impl_.setTemplateText(new WString(text.toString()),
				TextFormat.XHTMLUnsafeText);
		this.impl_.setStyleClass("Wt-cal");
		this.setSelectable(false);
		WText prevMonth = new WText(tr("Wt.WCalendar.PrevMonth"));
		prevMonth.setStyleClass("Wt-cal-navbutton");
		prevMonth.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WCalendar.this.browseToPreviousMonth();
					}
				});
		WText nextMonth = new WText(tr("Wt.WCalendar.NextMonth"));
		nextMonth.setStyleClass("Wt-cal-navbutton");
		nextMonth.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WCalendar.this.browseToNextMonth();
					}
				});
		this.monthEdit_ = new WComboBox();
		for (int i = 0; i < 12; ++i) {
			this.monthEdit_.addItem(WDate.getLongMonthName(i + 1));
		}
		this.monthEdit_.activated().addListener(this,
				new Signal1.Listener<Integer>() {
					public void trigger(Integer e1) {
						WCalendar.this.monthChanged(e1);
					}
				});
		this.yearEdit_ = new WInPlaceEdit("");
		this.yearEdit_.setButtonsEnabled(false);
		this.yearEdit_.getLineEdit().setTextSize(4);
		this.yearEdit_.setStyleClass("Wt-cal-year");
		this.yearEdit_.valueChanged().addListener(this,
				new Signal1.Listener<WString>() {
					public void trigger(WString e1) {
						WCalendar.this.yearChanged(e1);
					}
				});
		this.impl_.bindWidget("nav-prev", prevMonth);
		this.impl_.bindWidget("nav-next", nextMonth);
		this.impl_.bindWidget("month", this.monthEdit_);
		this.impl_.bindWidget("year", this.yearEdit_);
		this.setHorizontalHeaderFormat(this.horizontalHeaderFormat_);
		this.setFirstDayOfWeek(this.firstDayOfWeek_);
	}

	private void renderMonth() {
		this.needRenderMonth_ = true;
		if (this.isRendered()) {
			this.askRerender();
		}
	}

	private void emitCurrentPageChanged() {
		this.currentPageChanged()
				.trigger(this.currentYear_, this.currentMonth_);
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
		} catch (NumberFormatException e) {
		}
	}

	private WDate dateForCell(int week, int dayOfWeek) {
		WDate d = new WDate(this.currentYear_, this.currentMonth_, 1);
		d = d.addDays(-1);
		WDate.Day gw = WDate.Day.fromInt(this.firstDayOfWeek_ % 7);
		d = WDate.getPreviousWeekday(d, gw);
		d = d.addDays(week * 7 + dayOfWeek);
		return d;
	}

	private void selectInCurrentMonth(WDate d) {
		if (d.getMonth() == this.currentMonth_
				&& this.selectionMode_ != SelectionMode.NoSelection) {
			if (this.selectionMode_ == SelectionMode.ExtendedSelection) {
				if (this.isSelected(d)) {
					this.selection_.remove(d);
				} else {
					this.selection_.add(d);
				}
				this.selectionChanged().trigger();
				this.renderMonth();
			} else {
				this.selection_.clear();
				this.selection_.add(d);
				this.selectionChanged().trigger();
				this.renderMonth();
			}
		}
	}

	private void cellClicked(WCalendar.Coordinate weekday) {
		WDate dt = this.dateForCell(weekday.i, weekday.j);
		this.clicked().trigger(
				new WDate(dt.getYear(), dt.getMonth(), dt.getDay()));
		if (this.selectionMode_ != SelectionMode.ExtendedSelection
				&& this.singleClickSelect_) {
			this.activated().trigger(
					new WDate(dt.getYear(), dt.getMonth(), dt.getDay()));
		}
	}

	private void cellDblClicked(WCalendar.Coordinate weekday) {
		WDate dt = this.dateForCell(weekday.i, weekday.j);
		this.clicked().trigger(
				new WDate(dt.getYear(), dt.getMonth(), dt.getDay()));
		this.activated().trigger(
				new WDate(dt.getYear(), dt.getMonth(), dt.getDay()));
	}
}
