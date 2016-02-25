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
 * A Time field editor.
 * <p>
 * 
 * @see WTimePicker
 * @see WTime
 * @see WTimeValidator Styling through CSS is not applicable.
 */
public class WTimeEdit extends WLineEdit {
	private static Logger logger = LoggerFactory.getLogger(WTimeEdit.class);

	/**
	 * Creates a new time edit.
	 */
	public WTimeEdit(WContainerWidget parent) {
		super(parent);
		this.setValidator(new WTimeValidator(this));
		this.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				WTimeEdit.this.setFromLineEdit();
			}
		});
		String TEMPLATE = "${timePicker}";
		WTemplate t = new WTemplate(new WString(TEMPLATE));
		this.popup_ = new WPopupWidget(t, this);
		this.popup_.setAnchorWidget(this);
		this.popup_.setTransient(true);
		this.timePicker_ = new WTimePicker(this);
		this.timePicker_.selectionChanged().addListener(this,
				new Signal.Listener() {
					public void trigger() {
						WTimeEdit.this.setFromTimePicker();
					}
				});
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

	/**
	 * Creates a new time edit.
	 * <p>
	 * Calls {@link #WTimeEdit(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WTimeEdit() {
		this((WContainerWidget) null);
	}

	/**
	 * Sets the time.
	 * <p>
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
	 * Returns an invalid time (for which {@link } returns <code>false</code>) if
	 * the time could not be parsed using the current
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
		this.popup_.setHidden(hidden, animation);
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

	protected void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			this.defineJavaScript();
		}
		super.render(flags);
	}

	protected void propagateSetEnabled(boolean enabled) {
		super.propagateSetEnabled(enabled);
	}

	/**
	 * Sets the value from the time scroller to the line edit.
	 */
	protected void setFromTimePicker() {
		this.setTime(this.timePicker_.getTime());
	}

	/**
	 * Sets the value from the line edit to the time scroller.
	 */
	protected void setFromLineEdit() {
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
		String jsObj = "new Wt3_3_5.WTimeEdit(" + app.getJavaScriptClass()
				+ "," + this.getJsRef() + "," + this.popup_.getJsRef() + ");";
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
		String jsFunction = "function(dobj, event) {var o = jQuery.data("
				+ this.getJsRef() + ", 'dobj');if(o) o." + methodName
				+ "(dobj, event);}";
		s.addListener(jsFunction);
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WTimeEdit",
				"function(g,a,h){function f(){return a.readOnly}function i(){var b=$(\"#\"+h.id).get(0);return jQuery.data(b,\"popup\")}function j(){c.removeClass(\"active\")}function k(){var b=i();b.bindHide(j);b.show(a,e.Vertical)}jQuery.data(a,\"dobj\",this);var e=g.WT,c=$(a);this.mouseOut=function(){c.removeClass(\"hover\")};this.mouseMove=function(b,d){if(!f())if(e.widgetCoordinates(a,d).x>a.offsetWidth-40)c.addClass(\"hover\");else c.hasClass(\"hover\")&&c.removeClass(\"hover\")}; this.mouseDown=function(b,d){f()||e.widgetCoordinates(a,d).x>a.offsetWidth-40&&c.addClass(\"unselectable\").addClass(\"active\")};this.mouseUp=function(b,d){c.removeClass(\"unselectable\");e.widgetCoordinates(a,d).x>a.offsetWidth-40&&k()}}");
	}
}
