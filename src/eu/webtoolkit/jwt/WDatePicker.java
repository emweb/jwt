/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.EnumSet;
import java.util.Set;

/**
 * A date picker
 * <p>
 * 
 * A date picker shows a line edit and an icon which when clicked popups a
 * {@link WCalendar} for editing the date. Any date entered in the line edit is
 * reflected in the calendar, and vice-versa.
 * <p>
 * Each of these widgets may be accessed individually (
 * {@link WDatePicker#getLineEdit() getLineEdit()},
 * {@link WDatePicker#getCalendar() getCalendar()}, and
 * {@link WDatePicker#getDisplayWidget() getDisplayWidget()}) and there is a
 * constructor that allows you to specify an existing line edit and display
 * widget.
 * <p>
 * The date format used by default is <code>&quot;dd/MM/yyyy&quot;</code> and
 * can be changed using {@link WDatePicker#setFormat(String format) setFormat()}
 * . At any time, the date set may be read using {@link WDatePicker#getDate()
 * getDate()}, or can be changed using {@link WDatePicker#setDate(WDate date)
 * setDate()}.
 * <p>
 * <div align="center"> <img src="doc-files//WDatePicker-1.png"
 * alt="Example of WDatePicker with visible calendar.">
 * <p>
 * <strong>Example of WDatePicker with visible calendar.</strong>
 * </p>
 * </div>
 */
public class WDatePicker extends WCompositeWidget {
	/**
	 * Create a new date picker.
	 * <p>
	 * This constructor creates a line edit with an icon that leads to a popup
	 * calendar. A {@link WDateValidator} is configured for the line edit.
	 */
	public WDatePicker(WContainerWidget parent) {
		super(parent);
		this.format_ = "";
		this.positionJS_ = new JSlot();
		this.createDefault(false);
	}

	/**
	 * Create a new date picker.
	 * <p>
	 * Calls {@link #WDatePicker(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WDatePicker() {
		this((WContainerWidget) null);
	}

	/**
	 * Create a new date picker.
	 * <p>
	 * This constructor creates a line edit with an icon that leads to a popup
	 * calendar. A {@link WDateValidator} is configured for the line edit.
	 * <p>
	 * <code>i18n</code> is passed to the {@link WCalendar} constructor.
	 */
	public WDatePicker(boolean i18n, WContainerWidget parent) {
		super(parent);
		this.format_ = "";
		this.positionJS_ = new JSlot();
		this.createDefault(i18n);
	}

	/**
	 * Create a new date picker.
	 * <p>
	 * Calls {@link #WDatePicker(boolean i18n, WContainerWidget parent)
	 * this(i18n, (WContainerWidget)null)}
	 */
	public WDatePicker(boolean i18n) {
		this(i18n, (WContainerWidget) null);
	}

	/**
	 * Create a new date picker for existing line edit and with custom display
	 * widget.
	 * <p>
	 * The <code>displayWidget</code> is a button or image which much be clicked
	 * to open the date picker. This widget will become owned by the picker.
	 * <p>
	 * The <code>forEdit</code> argument is the lineEdit that works in
	 * conjunction with the date picker. This widget does not become part of the
	 * date picker, and may be located anywhere else.
	 * <p>
	 * <code>i18n</code> is passed to the {@link WCalendar} constructor.
	 */
	public WDatePicker(WInteractWidget displayWidget, WLineEdit forEdit,
			boolean i18n, WContainerWidget parent) {
		super(parent);
		this.format_ = "";
		this.positionJS_ = new JSlot();
		this.create(displayWidget, forEdit, i18n);
	}

	/**
	 * Create a new date picker for existing line edit and with custom display
	 * widget.
	 * <p>
	 * Calls
	 * {@link #WDatePicker(WInteractWidget displayWidget, WLineEdit forEdit, boolean i18n, WContainerWidget parent)
	 * this(displayWidget, forEdit, false, (WContainerWidget)null)}
	 */
	public WDatePicker(WInteractWidget displayWidget, WLineEdit forEdit) {
		this(displayWidget, forEdit, false, (WContainerWidget) null);
	}

	/**
	 * Create a new date picker for existing line edit and with custom display
	 * widget.
	 * <p>
	 * Calls
	 * {@link #WDatePicker(WInteractWidget displayWidget, WLineEdit forEdit, boolean i18n, WContainerWidget parent)
	 * this(displayWidget, forEdit, i18n, (WContainerWidget)null)}
	 */
	public WDatePicker(WInteractWidget displayWidget, WLineEdit forEdit,
			boolean i18n) {
		this(displayWidget, forEdit, i18n, (WContainerWidget) null);
	}

	/**
	 * Set the format used for parsing or writing the date in the line edit.
	 * <p>
	 * Sets the format used for representing the date in the line edit. If the
	 * line edit has a {@link WDateValidator} configured for it, then also there
	 * the format is updated.
	 * <p>
	 * The default format is <code>&apos;dd/MM/yyyy&apos;</code>.
	 * <p>
	 * 
	 * @see WDatePicker#getFormat()
	 * @see WDate#toString()
	 */
	public void setFormat(String format) {
		this.format_ = format;
		WDateValidator dv = ((this.forEdit_.getValidator()) instanceof WDateValidator ? (WDateValidator) (this.forEdit_
				.getValidator())
				: null);
		if (dv != null) {
			dv.setFormat(format);
		}
	}

	/**
	 * Returns the format.
	 * <p>
	 * 
	 * @see WDatePicker#setFormat(String format)
	 */
	public String getFormat() {
		return this.format_;
	}

	/**
	 * The calendar widget.
	 * <p>
	 * Returns the calendar widget.
	 */
	public WCalendar getCalendar() {
		return this.calendar_;
	}

	/**
	 * The line edit.
	 * <p>
	 * Returns the line edit which works in conjunction with this date picker.
	 */
	public WLineEdit getLineEdit() {
		return this.forEdit_;
	}

	/**
	 * The display widget.
	 * <p>
	 * Returns the widget which is displayed to activate the calendar.
	 */
	public WInteractWidget getDisplayWidget() {
		return this.displayWidget_;
	}

	/**
	 * The current date.
	 * <p>
	 * Reads the current date from the {@link WDatePicker#getLineEdit()
	 * getLineEdit()}.
	 * <p>
	 * Returns <code>null</code> if the date could not be parsed using the
	 * current {@link WDatePicker#getFormat() getFormat()}. <br>
	 * <p>
	 * 
	 * @see WDatePicker#setDate(WDate date)
	 * @see WDate#fromString(String s)
	 * @see WLineEdit#getText()
	 */
	public WDate getDate() {
		return WDate.fromString(this.forEdit_.getText(), this.format_);
	}

	/**
	 * Set the current date.
	 * <p>
	 * Does nothing if the current date is <code>Null</code>.
	 * <p>
	 * 
	 * @see WDatePicker#getDate()
	 */
	public void setDate(WDate date) {
		if (!(date == null)) {
			this.forEdit_.setText(date.toString(this.format_));
			this.calendar_.select(date);
			this.calendar_.browseTo(date);
		}
	}

	/**
	 * Change the enabled state of the widget.
	 * <p>
	 * A widget that is disabled cannot receive focus or user interaction.
	 */
	public void setEnabled(boolean enabled) {
		this.forEdit_.setEnabled(enabled);
		this.displayWidget_.setHidden(!enabled);
	}

	/**
	 * Enable the widget.
	 * <p>
	 * 
	 * @see WDatePicker#setEnabled(boolean enabled)
	 */
	public void enable() {
		this.setEnabled(true);
	}

	/**
	 * Disable the widget.
	 * <p>
	 * 
	 * @see WDatePicker#setEnabled(boolean enabled)
	 */
	public void disable() {
		this.setEnabled(false);
	}

	private String format_;
	private WInteractWidget displayWidget_;
	private WLineEdit forEdit_;
	private WContainerWidget layout_;
	private WContainerWidget popup_;
	private WCalendar calendar_;
	private JSlot positionJS_;

	private void createDefault(boolean i18n) {
		WImage icon = new WImage(WApplication.getResourcesUrl()
				+ "calendar_edit.png");
		WLineEdit lineEdit = new WLineEdit();
		this.create(icon, lineEdit, i18n);
		this.layout_.insertWidget(0, lineEdit);
		lineEdit.setValidator(new WDateValidator(this.format_, this));
	}

	private void create(WInteractWidget displayWidget, WLineEdit forEdit,
			boolean i18n) {
		this.setImplementation(this.layout_ = new WContainerWidget());
		this.displayWidget_ = displayWidget;
		this.forEdit_ = forEdit;
		this.format_ = "dd/MM/yyyy";
		String CSS_RULES_NAME = "Wt::WDatePicker";
		WApplication app = WApplication.getInstance();
		if (!app.getStyleSheet().isDefined(CSS_RULES_NAME)) {
			app
					.getStyleSheet()
					.addRule(
							".Wt-popup",
							"background-color: #EEEEEE;border: 1px solid #000000;padding: 2px;",
							CSS_RULES_NAME);
		}
		this.layout_.setInline(true);
		this.layout_.addWidget(displayWidget);
		this.layout_.addWidget(this.popup_ = new WContainerWidget());
		this.calendar_ = new WCalendar(i18n, this.popup_);
		this.calendar_.selected().addListener(this.popup_,
				new Signal1.Listener<WDate>() {
					public void trigger(WDate e1) {
						WDatePicker.this.popup_.hide();
					}
				});
		this.calendar_.selectionChanged().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						WDatePicker.this.setFromCalendar();
					}
				});
		WContainerWidget buttonContainer = new WContainerWidget(this.popup_);
		buttonContainer.setContentAlignment(EnumSet
				.of(AlignmentFlag.AlignCenter));
		WPushButton closeButton = new WPushButton(i18n ? tr("Close")
				: new WString("Close"), buttonContainer);
		closeButton.clicked().addListener(this.popup_,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WDatePicker.this.popup_.hide();
					}
				});
		this.popup_.hide();
		this.popup_.setPopup(true);
		this.popup_.setPositionScheme(PositionScheme.Absolute);
		this.popup_.setStyleClass("Wt-popup");
		this.popup_.escapePressed().addListener(this.popup_,
				new Signal.Listener() {
					public void trigger() {
						WDatePicker.this.popup_.hide();
					}
				});
		displayWidget.clicked().addListener(this.popup_,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WDatePicker.this.popup_.show();
					}
				});
		this.positionJS_
				.setJavaScript("function() { Wt3_0_0.positionAtWidget('"
						+ this.popup_.getId() + "','" + displayWidget.getId()
						+ "');}");
		displayWidget.clicked().addListener(this.positionJS_);
		displayWidget.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WDatePicker.this.setFromLineEdit();
					}
				});
	}

	private void setFromCalendar() {
		if (!this.calendar_.getSelection().isEmpty()) {
			WDate calDate = this.calendar_.getSelection().iterator().next();
			this.forEdit_.setText(calDate.toString(this.format_));
			this.forEdit_.changed().trigger();
		}
	}

	private void setFromLineEdit() {
		WDate d = WDate.fromString(this.forEdit_.getText(), this.format_);
		if ((d != null)) {
			this.calendar_.select(d);
			this.calendar_.browseTo(d);
		}
	}
}
