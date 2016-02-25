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
 * A Time Picker.
 * <p>
 * 
 * @see WTimeEdit
 * @see WTime
 * @see WTimeValidator Styling through CSS is not applicable.
 */
public class WTimePicker extends WCompositeWidget {
	private static Logger logger = LoggerFactory.getLogger(WTimePicker.class);

	/**
	 * Creates a new time picker.
	 */
	public WTimePicker(WContainerWidget parent) {
		super(parent);
		this.format_ = "";
		this.selectionChanged_ = new Signal(this);
		this.init();
	}

	/**
	 * Creates a new time picker.
	 * <p>
	 * Calls {@link #WTimePicker(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WTimePicker() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates a new time picker.
	 */
	public WTimePicker(final WTime time, WContainerWidget parent) {
		super(parent);
		this.format_ = "";
		this.selectionChanged_ = new Signal();
		this.init(time);
	}

	/**
	 * Creates a new time picker.
	 * <p>
	 * Calls {@link #WTimePicker(WTime time, WContainerWidget parent) this(time,
	 * (WContainerWidget)null)}
	 */
	public WTimePicker(final WTime time) {
		this(time, (WContainerWidget) null);
	}

	/**
	 * Creates a new time picker.
	 */
	public WTimePicker(WTimeEdit timeEdit, WContainerWidget parent) {
		super(parent);
		this.format_ = "";
		this.timeEdit_ = timeEdit;
		this.selectionChanged_ = new Signal();
		this.init();
	}

	/**
	 * Creates a new time picker.
	 * <p>
	 * Calls {@link #WTimePicker(WTimeEdit timeEdit, WContainerWidget parent)
	 * this(timeEdit, (WContainerWidget)null)}
	 */
	public WTimePicker(WTimeEdit timeEdit) {
		this(timeEdit, (WContainerWidget) null);
	}

	/**
	 * Creates a new time picker.
	 */
	public WTimePicker(final WTime time, WTimeEdit timeEdit,
			WContainerWidget parent) {
		super(parent);
		this.format_ = "";
		this.timeEdit_ = timeEdit;
		this.selectionChanged_ = new Signal();
		this.init(time);
	}

	/**
	 * Creates a new time picker.
	 * <p>
	 * Calls
	 * {@link #WTimePicker(WTime time, WTimeEdit timeEdit, WContainerWidget parent)
	 * this(time, timeEdit, (WContainerWidget)null)}
	 */
	public WTimePicker(final WTime time, WTimeEdit timeEdit) {
		this(time, timeEdit, (WContainerWidget) null);
	}

	/**
	 * Returns the time.
	 */
	public WTime getTime() {
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		int milliseconds = 0;
		try {
			hours = Integer.parseInt(this.sbhour_.getText());
			minutes = Integer.parseInt(this.sbminute_.getText());
			seconds = Integer.parseInt(this.sbsecond_.getText());
			if (this.isFormatMs()) {
				milliseconds = Integer.parseInt(this.sbmillisecond_.getText());
			}
			if (this.isFormatAp()) {
				if (this.cbAP_.getCurrentIndex() == 1) {
					if (hours != 12) {
						hours += 12;
					}
				} else {
					if (hours == 12) {
						hours = 0;
					}
				}
			}
		} catch (final NumberFormatException ex) {
			logger.error(new StringWriter().append(
					"boost::bad_lexical_cast caught in WTimePicker::time()")
					.toString());
		}
		return new WTime(hours, minutes, seconds, milliseconds);
	}

	/**
	 * Sets the time.
	 */
	public void setTime(final WTime time) {
		if (!(time != null && time.isValid())) {
			logger.error(new StringWriter().append("Time is invalid!")
					.toString());
			return;
		}
		int hours = 0;
		if (this.isFormatAp()) {
			hours = time.getPmHour();
			if (time.getHour() < 12) {
				this.cbAP_.setCurrentIndex(0);
			} else {
				this.cbAP_.setCurrentIndex(1);
			}
		} else {
			hours = time.getHour();
		}
		int minutes = time.getMinute();
		int seconds = time.getSecond();
		int millisecond = time.getMsec();
		this.sbhour_.setValue(hours);
		this.sbminute_.setValue(minutes);
		this.sbsecond_.setValue(seconds);
		if (this.isFormatMs()) {
			this.sbmillisecond_.setValue(millisecond);
		}
	}

	/**
	 * {@link Signal} emitted when the value is changed.
	 */
	public Signal selectionChanged() {
		return this.selectionChanged_;
	}

	public void configure() {
		WTemplate container = ((this.getImplementation()) instanceof WTemplate ? (WTemplate) (this
				.getImplementation()) : null);
		container.bindWidget("hour", this.sbhour_);
		container.bindWidget("minute", this.sbminute_);
		container.bindWidget("second", this.sbsecond_);
		if (this.isFormatMs()) {
			container.bindWidget("millisecond", this.sbmillisecond_);
		} else {
			container.takeWidget("millisecond");
			container.bindEmpty("millisecond");
		}
		if (this.isFormatAp()) {
			this.sbhour_.setRange(1, 12);
			container.bindWidget("ampm", this.cbAP_);
		} else {
			container.takeWidget("ampm");
			container.bindEmpty("ampm");
			this.sbhour_.setRange(0, 23);
		}
	}

	private void init(final WTime time) {
		WTemplate container = new WTemplate();
		this.setImplementation(container);
		container.addStyleClass("form-inline");
		container.setTemplateText(tr("Wt.WTimePicker.template"));
		this.createWidgets();
		this.configure();
	}

	private final void init() {
		init(null);
	}

	private String format_;
	private WSpinBox sbhour_;
	private WSpinBox sbminute_;
	private WSpinBox sbsecond_;
	private WSpinBox sbmillisecond_;
	private WComboBox cbAP_;
	private WTimeEdit timeEdit_;

	private void createWidgets() {
		this.sbhour_ = new WSpinBox();
		this.sbhour_.setWidth(new WLength(70));
		this.sbhour_.setSingleStep(1);
		this.sbhour_.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				WTimePicker.this.hourValueChanged();
			}
		});
		this.sbminute_ = new WSpinBox();
		this.sbminute_.setWidth(new WLength(70));
		this.sbminute_.setRange(0, 59);
		this.sbminute_.setSingleStep(1);
		this.sbminute_.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				WTimePicker.this.minuteValueChanged();
			}
		});
		this.sbsecond_ = new WSpinBox();
		this.sbsecond_.setWidth(new WLength(70));
		this.sbsecond_.setRange(0, 59);
		this.sbsecond_.setSingleStep(1);
		this.sbsecond_.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				WTimePicker.this.secondValueChanged();
			}
		});
		this.sbmillisecond_ = new WSpinBox();
		this.sbmillisecond_.setWidth(new WLength(70));
		this.sbmillisecond_.setRange(0, 999);
		this.sbmillisecond_.setSingleStep(1);
		this.sbmillisecond_.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				WTimePicker.this.msecValueChanged();
			}
		});
		this.cbAP_ = new WComboBox();
		this.cbAP_.setWidth(new WLength(70));
		this.cbAP_.addItem("AM");
		this.cbAP_.addItem("PM");
		this.cbAP_.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				WTimePicker.this.ampmValueChanged();
			}
		});
	}

	private void hourValueChanged() {
		if (this.sbhour_.validate() == WValidator.State.Valid) {
			this.selectionChanged_.trigger();
		}
	}

	private void minuteValueChanged() {
		if (this.sbminute_.validate() == WValidator.State.Valid) {
			this.selectionChanged_.trigger();
		}
	}

	private void secondValueChanged() {
		if (this.sbsecond_.validate() == WValidator.State.Valid) {
			this.selectionChanged_.trigger();
		}
	}

	private void msecValueChanged() {
		if (this.sbmillisecond_.validate() == WValidator.State.Valid) {
			this.selectionChanged_.trigger();
		}
	}

	private void ampmValueChanged() {
		if (this.cbAP_.validate() == WValidator.State.Valid) {
			this.selectionChanged_.trigger();
		}
	}

	private boolean isFormatAp() {
		return WTime.usesAmPm(this.timeEdit_.getFormat());
	}

	private boolean isFormatMs() {
		String format = this.timeEdit_.getFormat();
		return WTime.fromString(new WTime(4, 5, 6, 123).toString(format),
				format).getMsec() == 123;
	}

	private Signal selectionChanged_;
}
