/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
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
 * A Time field editor.
 * <p>
 * 
 * @see WTime
 * @see WTimeValidator
 * 
 *      Styling through CSS is not applicable.
 */
public class WTimeEdit extends WLineEdit {
	private static Logger logger = LoggerFactory.getLogger(WTimeEdit.class);

	/**
	 * Creates a new time edit.
	 */
	public WTimeEdit(WContainerWidget parent) {
		super(parent);
		this.popup_ = null;
		this.setValidator(new WTimeValidator(this));
		this.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				WTimeEdit.this.setFromLineEdit();
			}
		});
		this.timePicker_ = new WTimePicker(this);
		this.timePicker_.selectionChanged().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						WTimeEdit.this.setFromTimePicker();
					}
				});
		this.timePicker_.setWrapAroundEnabled(true);
	}

	/**
	 * Creates a new time edit.
	 * <p>
	 * Calls {@link #WTimeEdit(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WTimeEdit() {
		this((WContainerWidget) null);
	}

	public void remove() {
		if (!(this.popup_ != null)) {
			if (this.timePicker_ != null)
				this.timePicker_.remove();
		}
		super.remove();
	}

	/**
	 * Sets the time.
	 * <p>
	 * 
	 * Does nothing if the current time is <code>Null</code>.
	 * <p>
	 * 
	 * @see WTimeEdit#getTime()
	 */
	public void setTime(final WTime time) {
		if (!(time == null)) {
			this.setText(time.toString(this.getFormat()));
			this.timePicker_.setTime(time);
		}
	}

	/**
	 * Returns the time.
	 * <p>
	 * 
	 * Returns an invalid time (for which {@link WTime#isValid()} returns
	 * <code>false</code>) if the time could not be parsed using the current
	 * {@link WTimeEdit#getFormat() getFormat()}.
	 * <p>
	 * 
	 * @see WTimeEdit#setTime(WTime time)
	 * @see WLineEdit#getText()
	 */
	public WTime getTime() {
		return WTime.fromString(this.getText(), this.getFormat());
	}

	/**
	 * Returns the validator.
	 * <p>
	 * 
	 * @see WTimeValidator
	 */
	public WTimeValidator getValidator() {
		return ((super.getValidator()) instanceof WTimeValidator ? (WTimeValidator) (super
				.getValidator()) : null);
	}

	/**
	 * Sets the format of the Time.
	 */
	public void setFormat(final String format) {
		WTimeValidator tv = this.getValidator();
		if (tv != null) {
			WTime t = this.getTime();
			tv.setFormat(format);
			this.timePicker_.configure();
			this.setTime(t);
		} else {
			logger.warn(new StringWriter()
					.append("setFormat() ignored since validator is not WTimeValidator")
					.toString());
		}
	}

	/**
	 * Returns the format.
	 */
	public String getFormat() {
		WTimeValidator tv = this.getValidator();
		if (tv != null) {
			return tv.getFormat();
		} else {
			logger.warn(new StringWriter().append(
					"format() is bogus since validator is not WTimeValidator.")
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
	 * Sets the lower limit of the valid time range.
	 */
	public void setBottom(final WTime bottom) {
		WTimeValidator tv = this.getValidator();
		if (tv != null) {
			tv.setBottom(bottom);
		}
	}

	/**
	 * Returns the lower limit of the valid time range.
	 */
	public WTime getBottom() {
		WTimeValidator tv = this.getValidator();
		if (tv != null) {
			return tv.getBottom();
		}
		return null;
	}

	/**
	 * Sets the upper limit of the valid time range.
	 */
	public void setTop(final WTime top) {
		WTimeValidator tv = this.getValidator();
		if (tv != null) {
			tv.setTop(top);
		}
	}

	/**
	 * Returns the upper limit of the valid time range.
	 */
	public WTime getTop() {
		WTimeValidator tv = this.getValidator();
		if (tv != null) {
			return tv.getTop();
		}
		return null;
	}

	/**
	 * Sets the step size for the hours.
	 */
	public void setHourStep(int step) {
		this.timePicker_.setHourStep(step);
	}

	/**
	 * Returns the step size for the hours.
	 */
	public int getHourStep() {
		return this.timePicker_.getHourStep();
	}

	/**
	 * Sets the step size for the minutes.
	 */
	public void setMinuteStep(int step) {
		this.timePicker_.setMinuteStep(step);
	}

	/**
	 * Returns the step size for the minutes.
	 */
	public int getMinuteStep() {
		return this.timePicker_.getMinuteStep();
	}

	/**
	 * Sets the step size for the seconds.
	 */
	public void setSecondStep(int step) {
		this.timePicker_.setSecondStep(step);
	}

	/**
	 * Returns the step size for the seconds.
	 */
	public int getSecondStep() {
		return this.timePicker_.getSecondStep();
	}

	/**
	 * Sets the step size for the milliseconds.
	 */
	public void setMillisecondStep(int step) {
		this.timePicker_.setMillisecondStep(step);
	}

	/**
	 * Returns the step size for the milliseconds.
	 */
	public int getMillisecondStep() {
		return this.timePicker_.getMillisecondStep();
	}

	/**
	 * Enables or disables wraparound.
	 * <p>
	 * 
	 * Wraparound is enabled by default
	 */
	public void setWrapAroundEnabled(boolean enabled) {
		this.timePicker_.setWrapAroundEnabled(enabled);
	}

	/**
	 * Returns whether wraparound is enabled.
	 */
	public boolean isWrapAroundEnabled() {
		return this.timePicker_.isWrapAroundEnabled();
	}

	public void load() {
		boolean wasLoaded = this.isLoaded();
		super.load();
		if (wasLoaded) {
			return;
		}
		String TEMPLATE = "${timePicker}";
		WTemplate t = new WTemplate(new WString(TEMPLATE));
		this.popup_ = new WPopupWidget(t, this);
		if (this.isHidden()) {
			this.popup_.setHidden(true);
		}
		this.popup_.setAnchorWidget(this);
		this.popup_.setTransient(true);
		t.bindWidget("timePicker", this.timePicker_);
		WApplication.getInstance().getTheme()
				.apply(this, this.popup_, WidgetThemeRole.TimePickerPopupRole);
		this.escapePressed().addListener(this.popup_, new Signal.Listener() {
			public void trigger() {
				WTimeEdit.this.popup_.hide();
			}
		});
		this.escapePressed().addListener(this, new Signal.Listener() {
			public void trigger() {
				WTimeEdit.this.setFocus();
			}
		});
	}

	protected void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			this.defineJavaScript();
		}
		super.render(flags);
	}

	protected void propagateSetEnabled(boolean enabled) {
		super.propagateSetEnabled(enabled);
	}

	private void setFromTimePicker() {
		this.setTime(this.timePicker_.getTime());
		this.textInput().trigger();
		this.changed().trigger();
	}

	private void setFromLineEdit() {
		WTime t = WTime.fromString(this.getText(), this.getFormat());
		if ((t != null && t.isValid())) {
			this.timePicker_.setTime(t);
		}
	}

	private WPopupWidget popup_;
	private WTimePicker timePicker_;

	private void defineJavaScript() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WTimeEdit.js", wtjs1());
		String jsObj = "new Wt3_5_1.WTimeEdit(" + app.getJavaScriptClass()
				+ "," + this.getJsRef() + ","
				+ jsStringLiteral(this.popup_.getId()) + ");";
		this.setJavaScriptMember(" WTimeEdit", jsObj);
		final AbstractEventSignal b = this.mouseMoved();
		final AbstractEventSignal c = this.keyWentDown();
		this.connectJavaScript(this.mouseMoved(), "mouseMove");
		this.connectJavaScript(this.mouseWentUp(), "mouseUp");
		this.connectJavaScript(this.mouseWentDown(), "mouseDown");
		this.connectJavaScript(this.mouseWentOut(), "mouseOut");
	}

	private void connectJavaScript(final AbstractEventSignal s,
			final String methodName) {
		String jsFunction = "function(dobj, event) {var o = " + this.getJsRef()
				+ ";if(o && o.wtDObj) o.wtDObj." + methodName
				+ "(dobj, event);}";
		s.addListener(jsFunction);
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WTimeEdit",
				"function(g,a,h){function f(){return a.readOnly}function i(){return $(\"#\"+h).get(0).wtPopup}function j(){b.removeClass(\"active\")}function k(){var c=i();c.bindHide(j);c.show(a,e.Vertical)}a.wtDObj=this;var e=g.WT,b=$(a);this.mouseOut=function(){b.removeClass(\"hover\")};this.mouseMove=function(c,d){if(!f())if(e.widgetCoordinates(a,d).x>a.offsetWidth-40)b.addClass(\"hover\");else b.hasClass(\"hover\")&&b.removeClass(\"hover\")};this.mouseDown=function(c, d){f()||e.widgetCoordinates(a,d).x>a.offsetWidth-40&&b.addClass(\"unselectable\").addClass(\"active\")};this.mouseUp=function(c,d){b.removeClass(\"unselectable\");e.widgetCoordinates(a,d).x>a.offsetWidth-40&&k()}}");
	}
}
