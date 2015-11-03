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
		this.minuteStep_ = 1;
		this.secondStep_ = 1;
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
		this.minuteStep_ = 1;
		this.secondStep_ = 1;
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

	public WTime getTime() {
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		try {
			hours = Integer.parseInt(this.hourText_.getText().toString());
			minutes = Integer.parseInt(this.minuteText_.getText().toString());
			seconds = Integer.parseInt(this.secondText_.getText().toString());
		} catch (final NumberFormatException ex) {
			logger.error(new StringWriter().append(
					"boost::bad_lexical_cast caught in WTimePicker::time()")
					.toString());
		}
		return new WTime(hours, minutes, seconds);
	}

	/**
	 * sets the time
	 */
	public void setTime(final WTime time) {
		if (!time.isValid()) {
			logger.error(new StringWriter().append("Time is invalid!")
					.toString());
			return;
		}
		String hoursStr = "0";
		String minutesStr = "00";
		String secondsStr = "00";
		try {
			hoursStr = time.toString("hh");
			minutesStr = time.toString("mm");
			secondsStr = time.toString("ss");
		} catch (final NumberFormatException ex) {
			logger.error(new StringWriter().append(
					"boost::bad_lexical_cast caught in WTimePicker::time()")
					.toString());
		}
		this.hourText_.setText(hoursStr);
		this.minuteText_.setText(minutesStr);
		this.secondText_.setText(secondsStr);
	}

	/**
	 * returns the minutes step
	 */
	public int getMinuteStep() {
		return this.minuteStep_;
	}

	/**
	 * sets the minute step
	 */
	public void setMinuteStep(int step) {
		this.minuteStep_ = step;
	}

	/**
	 * sets the second step
	 */
	public void setSecondStep(int step) {
		this.secondStep_ = step;
	}

	/**
	 * {@link Signal} emitted when the value is changed.
	 */
	public Signal selectionChanged() {
		return this.selectionChanged_;
	}

	private int minuteStep_;
	private int secondStep_;

	private void init(final WTime time) {
		StringBuilder text = new StringBuilder();
		text.append("<table><tr><th>${incrementHour}</th><th></th><th>${incrementMinute}</th><th></th><th>${incrementSecond}</th></tr><tr><td valign=\"middle\" align=\"center\">${hourText}</td><td valign=\"middle\" align=\"center\">:</td><td valign=\"middle\" align=\"center\">${minuteText}</td><td valign=\"middle\" align=\"center\">:</td><td valign=\"middle\" align=\"center\">${secondText}</td></tr><tr><th>${decrementHour}</th><th></th><th>${decrementMinute}</th><th></th><th>${decrementSecond}</th></tr></table>");
		WTemplate impl = new WTemplate();
		this.setImplementation(impl);
		impl.setTemplateText(new WString(text.toString()));
		WIcon.loadIconFont();
		WPushButton incHourButton = new WPushButton();
		incHourButton.addStyleClass("fa fa-arrow-up");
		WPushButton decHourButton = new WPushButton();
		decHourButton.addStyleClass("fa fa-arrow-down");
		WPushButton incMinuteButton = new WPushButton();
		incMinuteButton.addStyleClass("fa fa-arrow-up");
		WPushButton decMinuteButton = new WPushButton();
		decMinuteButton.addStyleClass("fa fa-arrow-down");
		WPushButton incSecondButton = new WPushButton();
		incSecondButton.addStyleClass("fa fa-arrow-up");
		WPushButton decSecondButton = new WPushButton();
		decSecondButton.addStyleClass("fa fa-arrow-down");
		this.hourText_ = new WText("0");
		this.hourText_.setInline(false);
		this.hourText_.setTextAlignment(AlignmentFlag.AlignCenter);
		this.minuteText_ = new WText("00");
		this.minuteText_.setInline(false);
		this.minuteText_.setTextAlignment(AlignmentFlag.AlignCenter);
		this.secondText_ = new WText("00");
		this.secondText_.setInline(false);
		this.secondText_.setTextAlignment(AlignmentFlag.AlignCenter);
		impl.bindWidget("incrementHour", incHourButton);
		impl.bindWidget("decrementHour", decHourButton);
		impl.bindWidget("hourText", this.hourText_);
		impl.bindWidget("minuteText", this.minuteText_);
		impl.bindWidget("secondText", this.secondText_);
		impl.bindWidget("incrementMinute", incMinuteButton);
		impl.bindWidget("decrementMinute", decMinuteButton);
		impl.bindWidget("incrementSecond", incSecondButton);
		impl.bindWidget("decrementSecond", decSecondButton);
		incHourButton.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WTimePicker.this.incrementHours();
					}
				});
		decHourButton.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WTimePicker.this.decrementHours();
					}
				});
		incMinuteButton.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WTimePicker.this.incrementMinutes();
					}
				});
		decMinuteButton.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WTimePicker.this.decrementMinutes();
					}
				});
		incSecondButton.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WTimePicker.this.incrementSeconds();
					}
				});
		decSecondButton.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WTimePicker.this.decrementSeconds();
					}
				});
	}

	private final void init() {
		init(new WTime());
	}

	private WText hourText_;
	private WText minuteText_;
	private WText secondText_;

	private void incrementMinutes() {
		String str = this.minuteText_.getText().toString();
		int curVal = 0;
		if (str.length() != 0) {
			try {
				curVal = Integer.parseInt(str);
			} catch (final NumberFormatException ex) {
				logger.error(new StringWriter()
						.append("boost::bad_lexical_cast caught in WTimePicker::time()")
						.toString());
			}
		}
		if ((curVal += this.minuteStep_) >= 60) {
			curVal -= 60;
		}
		try {
			str = String.valueOf(curVal);
			if (str.length() == 1) {
				str = "0" + str;
			}
		} catch (final NumberFormatException ex) {
			logger.error(new StringWriter().append(
					"boost::bad_lexical_cast caught in WTimePicker::time()")
					.toString());
		}
		this.minuteText_.setText(str);
		this.selectionChanged_.trigger();
	}

	private void decrementMinutes() {
		String str = this.minuteText_.getText().toString();
		int curVal = 0;
		if (str.length() != 0) {
			try {
				curVal = Integer.parseInt(str);
			} catch (final NumberFormatException ex) {
				logger.error(new StringWriter()
						.append("boost::bad_lexical_cast caught in WTimePicker::time()")
						.toString());
			}
		}
		if ((curVal -= this.minuteStep_) < 0) {
			curVal += 60;
		}
		try {
			str = String.valueOf(curVal);
			if (str.length() == 1) {
				str = "0" + str;
			}
		} catch (final NumberFormatException ex) {
			logger.error(new StringWriter().append(
					"boost::bad_lexical_cast caught in WTimePicker::time()")
					.toString());
		}
		this.minuteText_.setText(str);
		this.selectionChanged_.trigger();
	}

	private void incrementHours() {
		String str = this.hourText_.getText().toString();
		int curVal = 0;
		if (str.length() != 0) {
			try {
				curVal = Integer.parseInt(str);
			} catch (final NumberFormatException ex) {
				logger.error(new StringWriter()
						.append("boost::bad_lexical_cast caught in WTimePicker::time()")
						.toString());
			}
		}
		if (curVal + 1 < 24) {
			curVal++;
		} else {
			curVal -= 23;
		}
		try {
			str = String.valueOf(curVal);
			if (str.length() == 1) {
				str = "0" + str;
			}
		} catch (final NumberFormatException ex) {
			logger.error(new StringWriter().append(
					"boost::bad_lexical_cast caught in WTimePicker::time()")
					.toString());
		}
		this.hourText_.setText(str);
		this.selectionChanged_.trigger();
	}

	private void decrementHours() {
		String str = this.hourText_.getText().toString();
		int curVal = 0;
		if (str.length() != 0) {
			try {
				curVal = Integer.parseInt(str);
			} catch (final NumberFormatException ex) {
				logger.error(new StringWriter()
						.append("boost::bad_lexical_cast caught in WTimePicker::time()")
						.toString());
			}
		}
		if (curVal - 1 >= 0) {
			curVal--;
		} else {
			curVal += 23;
		}
		try {
			str = String.valueOf(curVal);
			if (str.length() == 1) {
				str = "0" + str;
			}
		} catch (final NumberFormatException ex) {
			logger.error(new StringWriter().append(
					"boost::bad_lexical_cast caught in WTimePicker::time()")
					.toString());
		}
		this.hourText_.setText(str);
		this.selectionChanged_.trigger();
	}

	private void incrementSeconds() {
		String str = this.secondText_.getText().toString();
		int curVal = 0;
		if (str.length() != 0) {
			try {
				curVal = Integer.parseInt(str);
			} catch (final NumberFormatException ex) {
				logger.error(new StringWriter()
						.append("boost::bad_lexical_cast caught in WTimePicker::time()")
						.toString());
			}
		}
		if ((curVal += this.secondStep_) >= 60) {
			curVal -= 60;
		}
		try {
			str = String.valueOf(curVal);
			if (str.length() == 1) {
				str = "0" + str;
			}
		} catch (final NumberFormatException ex) {
			logger.error(new StringWriter().append(
					"boost::bad_lexical_cast caught in WTimePicker::time()")
					.toString());
		}
		this.secondText_.setText(str);
		this.selectionChanged_.trigger();
	}

	private void decrementSeconds() {
		String str = this.secondText_.getText().toString();
		int curVal = 0;
		if (str.length() != 0) {
			try {
				curVal = Integer.parseInt(str);
			} catch (final NumberFormatException ex) {
				logger.error(new StringWriter()
						.append("boost::bad_lexical_cast caught in WTimePicker::time()")
						.toString());
			}
		}
		if ((curVal -= this.secondStep_) < 0) {
			curVal += 60;
		}
		try {
			str = String.valueOf(curVal);
			if (str.length() == 1) {
				str = "0" + str;
			}
		} catch (final NumberFormatException ex) {
			logger.error(new StringWriter().append(
					"boost::bad_lexical_cast caught in WTimePicker::time()")
					.toString());
		}
		this.secondText_.setText(str);
		this.selectionChanged_.trigger();
	}

	private Signal selectionChanged_;
}
