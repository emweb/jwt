/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A calendar
 * <p>
 * 
 * The calendar provides navigation by month and year, and indicates the current
 * day.
 * <p>
 * The calendar may be configured to allow selection of single or multiple days,
 * and you may listen for changes in the selection using the
 * {@link WCalendar#selectionChanged() selectionChanged()} or
 * {@link WCalendar#selected() selected()} signals.
 * <p>
 * Internationalization may be provided by indicating i18n == true in the
 * constructor, and providing the appropriate messages for months (with keys
 * from {@link WDate#getLongMonthName(int month) WDate#getLongMonthName()}) and
 * days (with keys from {@link WDate#getShortDayName(int weekday)
 * WDate#getShortDayName()}) in your message resource bundle.
 * <p>
 * Here is a snapshot of the default look, taken on 31/08/2007 (shown as today),
 * and 11/08/2007 currently selected. <div align="center"> <img
 * src="doc-files//WCalendar-1.png" alt="WCalendar with default look">
 * <p>
 * <strong>WCalendar with default look</strong>
 * </p>
 * </div> <h3>CSS</h3>
 * <p>
 * The look can be overridden using the following style class selectors:
 * <p>
 * <div class="fragment">
 * 
 * <pre class="fragment">
 * .Wt-cal-table           : The table
 * 
 * .Wt-cal-header          : Header cell (week day)
 * .Wt-cal-header-weekend  : Header cell (weekend day)
 * 
 * .Wt-cal-table td        : In-month day cell
 * .Wt-cal-oom             : Out-of-month day cell
 * .Wt-cal-sel             : Selected day cell
 * .Wt-cal-now             : Today day cell
 * </pre>
 * 
 * </div>
 */
public class WCalendar extends WCompositeWidget {
	/**
	 * Create a new calendar.
	 * <p>
	 * Constructs a new calendar, with optional support for
	 * internationalization. The calendar shows the current day, and has an
	 * empty selection.
	 */
	public WCalendar(boolean i18n, WContainerWidget parent) {
		super(parent);
		this.i18n_ = i18n;
		this.multipleSelection_ = false;
		this.singleClickSelect_ = false;
		this.selection_ = new HashSet<WDate>();
		this.selectionChanged_ = new Signal(this);
		this.selected_ = new Signal1<WDate>(this);
		this.cellClickMapper_ = null;
		this.cellDblClickMapper_ = null;
		WDate currentDay = WDate.getCurrentDate();
		this.currentYear_ = currentDay.getYear();
		this.currentMonth_ = currentDay.getMonth();
		this.create();
	}

	/**
	 * Create a new calendar.
	 * <p>
	 * Calls {@link #WCalendar(boolean i18n, WContainerWidget parent)
	 * this(false, (WContainerWidget)null)}
	 */
	public WCalendar() {
		this(false, (WContainerWidget) null);
	}

	/**
	 * Create a new calendar.
	 * <p>
	 * Calls {@link #WCalendar(boolean i18n, WContainerWidget parent) this(i18n,
	 * (WContainerWidget)null)}
	 */
	public WCalendar(boolean i18n) {
		this(i18n, (WContainerWidget) null);
	}

	/**
	 * Configure single or multiple selection mode.
	 * <p>
	 * In single selection mode, only one date may be selected: the
	 * {@link WCalendar#getSelection() getSelection()} will be empty or contain
	 * exactly one item.
	 */
	public void setMultipleSelection(boolean multiple) {
		if (multiple != this.multipleSelection_) {
			if (!multiple && this.selection_.size() > 1) {
				this.selection_.clear();
				this.renderMonth();
			}
			this.multipleSelection_ = multiple;
		}
	}

	/**
	 * Browse to the same month in the previous year.
	 * <p>
	 * Displays the same month in the previous year. This does not change the
	 * current selection.
	 */
	public void browseToPreviousYear() {
		--this.currentYear_;
		this.renderMonth();
	}

	/**
	 * Browse to the previous month.
	 * <p>
	 * Displays the previous month. This does not change the current selection.
	 */
	public void browseToPreviousMonth() {
		if (--this.currentMonth_ == 0) {
			this.currentMonth_ = 12;
			--this.currentYear_;
		}
		this.renderMonth();
	}

	/**
	 * Browse to the same month in the next year.
	 * <p>
	 * Displays the same month in the next year. This does not change the
	 * current selection.
	 */
	public void browseToNextYear() {
		++this.currentYear_;
		this.renderMonth();
	}

	/**
	 * Browse to the next month.
	 * <p>
	 * Displays the next month. This does not change the current selection.
	 */
	public void browseToNextMonth() {
		if (++this.currentMonth_ == 13) {
			this.currentMonth_ = 1;
			++this.currentYear_;
		}
		this.renderMonth();
	}

	/**
	 * Browse to a date.
	 * <p>
	 * Displays the month which contains the given date. This does not change
	 * the current selection.
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
	 * Clear the current selection.
	 * <p>
	 * Clears the current selection. Will result in a
	 * {@link WCalendar#getSelection() getSelection()} that is empty().
	 */
	public void clearSelection() {
		this.selection_.clear();
		this.renderMonth();
	}

	/**
	 * Select a date.
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
	 * Select multiple dates.
	 * <p>
	 * Select multiple dates. In multiple selection mode, this results in a
	 * {@link WCalendar#getSelection() getSelection()} that contains exactly the
	 * given dates. In single selection mode, at most one date is set.
	 */
	public void select(Set<WDate> dates) {
		if (this.multipleSelection_) {
			this.selection_ = dates;
			this.renderMonth();
		} else {
			if (dates.isEmpty()) {
				this.clearSelection();
			} else {
				this.select(dates.iterator().next());
			}
		}
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
	 * Signal emitted when the user has double clicked on a date.
	 * <p>
	 * This signal indicates that he user has selected a new date, which is only
	 * available when in single selection mode.
	 */
	public Signal1<WDate> selected() {
		return this.selected_;
	}

	/**
	 * Configure the calendar to use single click to select.
	 * <p>
	 * This only applies to a single-selection calendar.
	 * <p>
	 * 
	 * @see WCalendar#setMultipleSelection(boolean multiple)
	 */
	public void setSingleClickSelect(boolean single) {
		this.singleClickSelect_ = single;
	}

	void render() {
		if (this.needRenderMonth_) {
			boolean create = this.cellClickMapper_ == null;
			String buf;
			WApplication app = null;
			if (create) {
				this.cellClickMapper_ = new WSignalMapper1<WCalendar.Coordinate>(
						this);
				this.cellClickMapper_.mapped().addListener(this,
						new Signal1.Listener<WCalendar.Coordinate>() {
							public void trigger(WCalendar.Coordinate e1) {
								WCalendar.this.cellClicked(e1);
							}
						});
				if (!this.singleClickSelect_) {
					this.cellDblClickMapper_ = new WSignalMapper1<WCalendar.Coordinate>(
							this);
					this.cellDblClickMapper_.mapped().addListener(this,
							new Signal1.Listener<WCalendar.Coordinate>() {
								public void trigger(WCalendar.Coordinate e1) {
									WCalendar.this.cellDblClicked(e1);
								}
							});
				}
				app = WApplication.getInstance();
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
			WDate nowd = WDate.getCurrentDate();
			WDate now = new WDate(nowd.getYear(), nowd.getMonth(), nowd
					.getDay());
			WDate d = new WDate(this.currentYear_, this.currentMonth_, 1);
			d = d.addDays(-1);
			WDate.Days gw = WDate.Days.Monday;
			d = WDate.getPreviousWeekday(d, gw);
			for (int i = 0; i < 6; ++i) {
				for (int j = 0; j < 7; ++j) {
					WTableCell cell = this.calendar_.getElementAt(i + 1, j);
					if (create) {
						WText t = new WText(cell);
						t.setTextFormat(TextFormat.PlainText);
						buf = String.valueOf(d.getDay());
						t.setText(new WString(buf));
						WInteractWidget w = app.getEnvironment()
								.hasJavaScript() ? (WInteractWidget) cell : t;
						this.cellClickMapper_.mapConnect(w.clicked(),
								new WCalendar.Coordinate(i, j));
						if (this.cellDblClickMapper_ != null) {
							this.cellDblClickMapper_.mapConnect(w
									.doubleClicked(), new WCalendar.Coordinate(
									i, j));
						}
					} else {
						WText t = ((cell.getChildren().get(0)) instanceof WText ? (WText) (cell
								.getChildren().get(0))
								: null);
						buf = String.valueOf(d.getDay());
						t.setText(new WString(buf));
					}
					WDate date = new WDate(d.getYear(), d.getMonth(), d
							.getDay());
					String styleClass = "";
					if (this.isSelected(date)) {
						styleClass += " Wt-cal-sel";
					}
					if (d.getMonth() != this.currentMonth_) {
						styleClass += " Wt-cal-oom";
					}
					if (d.equals(now)) {
						styleClass += " Wt-cal-now";
					}
					cell.setStyleClass(styleClass);
					d = d.addDays(1);
				}
			}
			this.needRenderMonth_ = false;
		}
		super.render();
	}

	private boolean i18n_;
	private boolean multipleSelection_;
	private boolean singleClickSelect_;
	private int currentYear_;
	private int currentMonth_;
	private Set<WDate> selection_;
	private boolean needRenderMonth_;
	private Signal selectionChanged_;
	private Signal1<WDate> selected_;

	static class Coordinate {
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

	private WContainerWidget layout_;
	private WComboBox monthEdit_;
	private WInPlaceEdit yearEdit_;
	private WTable calendar_;
	private WSignalMapper1<WCalendar.Coordinate> cellClickMapper_;
	private WSignalMapper1<WCalendar.Coordinate> cellDblClickMapper_;

	private void create() {
		this.setImplementation(this.layout_ = new WContainerWidget());
		WApplication app = WApplication.getInstance();
		this.layout_.setStyleClass("Wt-cal");
		WContainerWidget navigation = new WContainerWidget(this.layout_);
		navigation.setStyleClass("Wt-cal-navigation");
		WText prevYear = new WText("<<", TextFormat.PlainText, navigation);
		prevYear.setStyleClass("Wt-cal-navbutton");
		prevYear.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WCalendar.this.browseToPreviousYear();
					}
				});
		WText prevMonth = new WText("<", TextFormat.PlainText, navigation);
		prevMonth.setStyleClass("Wt-cal-navbutton");
		prevMonth.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WCalendar.this.browseToPreviousMonth();
					}
				});
		this.monthEdit_ = new WComboBox(navigation);
		for (int i = 0; i < 12; ++i) {
			this.monthEdit_.addItem(this.i18n_ ? tr(WDate
					.getLongMonthName(i + 1)) : new WString(WDate
					.getLongMonthName(i + 1)));
		}
		this.monthEdit_.activated().addListener(this,
				new Signal1.Listener<Integer>() {
					public void trigger(Integer e1) {
						WCalendar.this.monthChanged(e1);
					}
				});
		this.yearEdit_ = new WInPlaceEdit("", navigation);
		this.yearEdit_.getLineEdit().setTextSize(4);
		this.yearEdit_.setStyleClass("Wt-cal-year");
		this.yearEdit_.valueChanged().addListener(this,
				new Signal1.Listener<WString>() {
					public void trigger(WString e1) {
						WCalendar.this.yearChanged(e1);
					}
				});
		WText nextMonth = new WText(">", TextFormat.PlainText, navigation);
		nextMonth.setStyleClass("Wt-cal-navbutton");
		nextMonth.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WCalendar.this.browseToNextMonth();
					}
				});
		WText nextYear = new WText(">>", TextFormat.PlainText, navigation);
		nextYear.setStyleClass("Wt-cal-navbutton");
		nextYear.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WCalendar.this.browseToNextYear();
					}
				});
		this.calendar_ = new WTable(this.layout_);
		this.calendar_.setStyleClass("Wt-cal-table");
		for (int i = 0; i < 7; ++i) {
			new WText(this.i18n_ ? tr(WDate.getShortDayName(i + 1))
					: new WString(WDate.getShortDayName(i + 1)), this.calendar_
					.getElementAt(0, i));
			this.calendar_.getElementAt(0, i).setStyleClass(
					i < 5 ? "Wt-cal-header" : "Wt-cal-header-weekend");
		}
		this.renderMonth(true);
	}

	private void renderMonth(boolean create) {
		this.needRenderMonth_ = true;
		if (this.isRendered()) {
			this.askRerender();
		}
	}

	private final void renderMonth() {
		renderMonth(false);
	}

	private void monthChanged(int newMonth) {
		++newMonth;
		if (this.currentMonth_ != newMonth && (newMonth >= 1 && newMonth <= 12)) {
			this.currentMonth_ = newMonth;
			this.renderMonth();
		}
	}

	private void yearChanged(CharSequence yearStr) {
		try {
			int year = Integer.parseInt(yearStr.toString());
			if (this.currentYear_ != year && (year >= 1900 && year <= 2200)) {
				this.currentYear_ = year;
				this.renderMonth();
			}
		} catch (NumberFormatException e) {
		}
	}

	private WDate dateForCell(int week, int dayOfWeek) {
		WDate d = new WDate(this.currentYear_, this.currentMonth_, 1);
		d = d.addDays(-1);
		WDate.Days gw = WDate.Days.Monday;
		d = WDate.getPreviousWeekday(d, gw);
		d = d.addDays(week * 7 + dayOfWeek);
		return d;
	}

	private boolean isSelected(WDate d) {
		return this.selection_.contains(d) != false;
	}

	private void cellClicked(WCalendar.Coordinate weekday) {
		if (!this.multipleSelection_ && this.singleClickSelect_) {
			this.cellDblClicked(weekday);
			return;
		}
		WDate dt = this.dateForCell(weekday.i, weekday.j);
		this.selectInCurrentMonth(dt);
	}

	private boolean selectInCurrentMonth(WDate dt) {
		if (dt.getMonth() == this.currentMonth_) {
			WDate d = new WDate(dt.getYear(), dt.getMonth(), dt.getDay());
			if (this.multipleSelection_) {
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
			return true;
		} else {
			return false;
		}
	}

	private void cellDblClicked(WCalendar.Coordinate weekday) {
		WDate dt = this.dateForCell(weekday.i, weekday.j);
		if (this.selectInCurrentMonth(dt)) {
			if (!this.multipleSelection_) {
				this.selected().trigger(
						new WDate(dt.getYear(), dt.getMonth(), dt.getDay()));
			}
		}
	}
}
