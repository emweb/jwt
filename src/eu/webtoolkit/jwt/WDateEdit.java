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
 * A date edit.
 * <p>
 * 
 * A date picker is a line edit with support for date entry (using an icon and a
 * calendar).
 * <p>
 * A {@link WDateValidator} is used to validate date entry.
 * <p>
 * In many cases, it provides a more convenient implementation of a date picker
 * compared to {@link WDatePicker} since it is implemented as a line edit. This
 * also makes the implementation ready for a native HTML5 control.
 */
public class WDateEdit extends WLineEdit {
	private static Logger logger = LoggerFactory.getLogger(WDateEdit.class);

	/**
	 * Creates a new date edit.
	 */
	public WDateEdit(WContainerWidget parent) {
		super(parent);
		this.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				WDateEdit.this.setFromLineEdit();
			}
		});
		String TEMPLATE = "${calendar}";
		WTemplate t = new WTemplate(new WString(TEMPLATE));
		this.popup_ = new WPopupWidget(t, this);
		this.popup_.setAnchorWidget(this);
		this.popup_.setTransient(true);
		this.calendar_ = new WCalendar();
		this.calendar_.setSingleClickSelect(true);
		this.calendar_.activated().addListener(this.popup_,
				new Signal1.Listener<WDate>() {
					public void trigger(WDate e1) {
						WDateEdit.this.popup_.hide();
					}
				});
		this.calendar_.activated().addListener(this,
				new Signal1.Listener<WDate>() {
					public void trigger(WDate e1) {
						WDateEdit.this.setFocus();
					}
				});
		this.calendar_.selectionChanged().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						WDateEdit.this.setFromCalendar();
					}
				});
		t.bindWidget("calendar", this.calendar_);
		WApplication.getInstance().getTheme()
				.apply(this, this.popup_, WidgetThemeRole.DatePickerPopupRole);
		this.escapePressed().addListener(this.popup_, new Signal.Listener() {
			public void trigger() {
				WDateEdit.this.popup_.hide();
			}
		});
		this.escapePressed().addListener(this, new Signal.Listener() {
			public void trigger() {
				WDateEdit.this.setFocus();
			}
		});
		this.setValidator(new WDateValidator("dd/MM/yyyy", this));
	}

	/**
	 * Creates a new date edit.
	 * <p>
	 * Calls {@link #WDateEdit(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WDateEdit() {
		this((WContainerWidget) null);
	}

	/**
	 * Sets the date.
	 * <p>
	 * Does nothing if the current date is <code>Null</code>.
	 * <p>
	 */
	public void setDate(final WDate date) {
		if (!(date == null)) {
			this.setText(date.toString(this.getFormat()));
			this.calendar_.select(date);
			this.calendar_.browseTo(date);
		}
	}

	/**
	 * Returns the date.
	 * <p>
	 * Reads the current date.
	 * <p>
	 * Returns <code>null</code> if the date could not be parsed using the
	 * current {@link }. <br>
	 * <p>
	 * 
	 * @see WDateEdit#setDate(WDate date)
	 */
	public WDate getDate() {
		return WDate.fromString(this.getText(), this.getFormat());
	}

	/**
	 * Returns the validator.
	 * <p>
	 * Most of the configuration of the date edit is stored in the validator.
	 */
	public WDateValidator getValidator() {
		return ((super.getValidator()) instanceof WDateValidator ? (WDateValidator) (super
				.getValidator()) : null);
	}

	/**
	 * Sets the format used for representing the date.
	 * <p>
	 * This sets the format in the validator.
	 * <p>
	 * The default format is <code>&apos;dd/MM/yyyy&apos;</code>.
	 * <p>
	 */
	public void setFormat(final String format) {
		WDateValidator dv = this.getValidator();
		if (dv != null) {
			WDate d = this.getDate();
			dv.setFormat(format);
			this.setDate(d);
		} else {
			logger.warn(new StringWriter()
					.append("setFormat() ignored since validator is not a WDateValidator")
					.toString());
		}
	}

	/**
	 * Returns the format.
	 * <p>
	 * 
	 * @see WDateEdit#setFormat(String format)
	 */
	public String getFormat() {
		WDateValidator dv = this.getValidator();
		if (dv != null) {
			return dv.getFormat();
		} else {
			logger.warn(new StringWriter()
					.append("format() is bogus  since validator is not a WDateValidator")
					.toString());
			return "";
		}
	}

	/**
	 * Sets the lower limit of the valid date range.
	 * <p>
	 * This sets the lower limit of the valid date range in the validator.
	 * <p>
	 */
	public void setBottom(final WDate bottom) {
		WDateValidator dv = this.getValidator();
		if (dv != null) {
			dv.setBottom(bottom);
		}
		this.calendar_.setBottom(bottom);
	}

	/**
	 * Returns the lower limit of the valid date range.
	 * <p>
	 * 
	 * @see WDateEdit#setBottom(WDate bottom)
	 */
	public WDate getBottom() {
		return this.calendar_.getBottom();
	}

	/**
	 * Sets the upper limit of the valid date range.
	 * <p>
	 * This sets the upper limit of the valid date range in the validator.
	 * <p>
	 */
	public void setTop(final WDate top) {
		WDateValidator dv = this.getValidator();
		if (dv != null) {
			dv.setTop(top);
		}
		this.calendar_.setTop(top);
	}

	/**
	 * Returns the upper limit of the valid range.
	 * <p>
	 * 
	 * @see WDateEdit#setTop(WDate top)
	 */
	public WDate getTop() {
		return this.calendar_.getTop();
	}

	/**
	 * Returns the calendar widget.
	 * <p>
	 * The calendar may be 0 (e.g. when using a native date entry widget).
	 */
	public WCalendar getCalendar() {
		return this.calendar_;
	}

	/**
	 * Hide/unhide the widget.
	 */
	public void setHidden(boolean hidden, final WAnimation animation) {
		super.setHidden(hidden, animation);
		if (hidden) {
			this.popup_.setHidden(hidden, animation);
		}
	}

	protected void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			this.defineJavaScript();
			WDateValidator dv = this.getValidator();
			if (dv != null) {
				this.setTop(dv.getTop());
				this.setBottom(dv.getBottom());
				this.setFormat(dv.getFormat());
			}
		}
		super.render(flags);
	}

	protected void propagateSetEnabled(boolean enabled) {
		super.propagateSetEnabled(enabled);
	}

	/**
	 * Sets the value from the calendar to the line edit.
	 */
	protected void setFromCalendar() {
		if (!this.calendar_.getSelection().isEmpty()) {
			WDate calDate = this.calendar_.getSelection().iterator().next();
			this.setText(calDate.toString(this.getFormat()));
			this.changed().trigger();
		}
	}

	/**
	 * Sets the value from the line edit to the calendar.
	 */
	protected void setFromLineEdit() {
		WDate d = WDate.fromString(this.getText(), this.getFormat());
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

	private WPopupWidget popup_;
	private WCalendar calendar_;

	private void defineJavaScript() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WDateEdit.js", wtjs1());
		String jsObj = "new Wt3_3_5.WDateEdit(" + app.getJavaScriptClass()
				+ "," + this.getJsRef() + "," + this.popup_.getJsRef() + ");";
		this.setJavaScriptMember(" WDateEdit", jsObj);
		final AbstractEventSignal b = this.mouseMoved();
		final AbstractEventSignal c = this.keyWentDown();
		this.connectJavaScript(this.mouseMoved(), "mouseMove");
		this.connectJavaScript(this.mouseWentUp(), "mouseUp");
		this.connectJavaScript(this.mouseWentDown(), "mouseDown");
		this.connectJavaScript(this.mouseWentOut(), "mouseOut");
	}

	private void connectJavaScript(final AbstractEventSignal s,
			final String methodName) {
		String jsFunction = "function(dobj, event) {var o = jQuery.data("
				+ this.getJsRef() + ", 'dobj');if (o) o." + methodName
				+ "(dobj, event);}";
		s.addListener(jsFunction);
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WDateEdit",
				"function(g,a,h){function f(){return a.readOnly}function i(){var b=$(\"#\"+h.id).get(0);return jQuery.data(b,\"popup\")}function j(){c.removeClass(\"active\")}function k(){var b=i();b.bindHide(j);b.show(a,e.Vertical)}jQuery.data(a,\"dobj\",this);var e=g.WT,c=$(a);this.mouseOut=function(){c.removeClass(\"hover\")};this.mouseMove=function(b,d){if(!f())if(e.widgetCoordinates(a,d).x>a.offsetWidth-40)c.addClass(\"hover\");else c.hasClass(\"hover\")&&c.removeClass(\"hover\")}; this.mouseDown=function(b,d){f()||e.widgetCoordinates(a,d).x>a.offsetWidth-40&&c.addClass(\"unselectable\").addClass(\"active\")};this.mouseUp=function(b,d){c.removeClass(\"unselectable\");e.widgetCoordinates(a,d).x>a.offsetWidth-40&&k()}}");
	}
}
