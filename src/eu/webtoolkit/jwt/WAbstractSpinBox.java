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
 * An abstract spin box.
 * <p>
 * 
 * Although the element can be rendered using a native HTML5 control, by default
 * it is rendered using an HTML4 compatibility workaround which is implemented
 * using JavaScript and CSS, as most browsers do not yet implement the HTML5
 * native element.
 */
public abstract class WAbstractSpinBox extends WLineEdit {
	private static Logger logger = LoggerFactory
			.getLogger(WAbstractSpinBox.class);

	/**
	 * Configures whether a native HTML5 control should be used.
	 * <p>
	 * When <code>native</code>, the new &quot;number&quot; input element,
	 * specified by HTML5 and when implemented by the browser, is used rather
	 * than the built-in element. The native control is styled by the browser
	 * (usually in sync with the OS) rather than through the theme chosen.
	 * <p>
	 * The default is <code>false</code> (as native support is now well
	 * implemented).
	 */
	public void setNativeControl(boolean nativeControl) {
		this.preferNative_ = nativeControl;
	}

	/**
	 * Returns whether a native HTML5 control is used.
	 * <p>
	 * Taking into account the preference for a native control, configured using
	 * {@link WAbstractSpinBox#setNativeControl(boolean nativeControl)
	 * setNativeControl()}, this method returns whether a native control is
	 * actually being used.
	 */
	public boolean isNativeControl() {
		if (this.preferNative_) {
			final WEnvironment env = WApplication.getInstance()
					.getEnvironment();
			if (env.agentIsChrome()
					&& env.getAgent().getValue() >= WEnvironment.UserAgent.Chrome5
							.getValue()
					|| env.agentIsSafari()
					&& env.getAgent().getValue() >= WEnvironment.UserAgent.Safari4
							.getValue()
					|| env.agentIsOpera()
					&& env.getAgent().getValue() >= WEnvironment.UserAgent.Opera10
							.getValue()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets a prefix.
	 * <p>
	 * Option to set a prefix string shown in front of the value, e.g.:
	 * <p>
	 * 
	 * <pre>
	 * {@code
	 *      spinBox.setPrefix("$ ");
	 *   }
	 * </pre>
	 * <p>
	 * The default prefix is empty.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>Not supported by the native controls. </i>
	 * </p>
	 */
	public void setPrefix(final CharSequence prefix) {
		if (!this.prefix_.equals(prefix)) {
			this.prefix_ = WString.toWString(prefix);
			this.setText(this.getTextFromValue().toString());
		}
	}

	/**
	 * Returns the prefix.
	 * <p>
	 * 
	 * @see WAbstractSpinBox#setPrefix(CharSequence prefix)
	 */
	public WString getPrefix() {
		return this.prefix_;
	}

	/**
	 * Sets a suffix.
	 * <p>
	 * Option to set a suffix string shown to the right of the value, e.g.:
	 * <p>
	 * 
	 * <pre>
	 * {@code
	 *      spinBox.setSuffix(" crates");
	 *   }
	 * </pre>
	 * <p>
	 * The default suffix is empty.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>Not supported by the native controls. </i>
	 * </p>
	 */
	public void setSuffix(final CharSequence suffix) {
		if (!this.suffix_.equals(suffix)) {
			this.suffix_ = WString.toWString(suffix);
			this.setText(this.getTextFromValue().toString());
		}
	}

	/**
	 * Returns the suffix.
	 * <p>
	 * 
	 * @see WAbstractSpinBox#setSuffix(CharSequence suffix)
	 */
	public WString getSuffix() {
		return this.suffix_;
	}

	public void setText(final String text) {
		this.parseValue(text);
		super.setText(this.getTextFromValue().toString());
	}

	public WValidator.State validate() {
		return super.validate();
	}

	/**
	 * Constructor.
	 */
	protected WAbstractSpinBox(WContainerWidget parent) {
		super(parent);
		this.changed_ = false;
		this.valueChangedConnection_ = false;
		this.preferNative_ = false;
		this.setup_ = false;
		this.prefix_ = new WString();
		this.suffix_ = new WString();
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls {@link #WAbstractSpinBox(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	protected WAbstractSpinBox() {
		this((WContainerWidget) null);
	}

	void updateDom(final DomElement element, boolean all) {
		if (all || this.changed_) {
			if (!all) {
				if (!this.isNativeControl()) {
					this.doJavaScript("jQuery.data(" + this.getJsRef()
							+ ", 'obj').update(" + this.getJsMinMaxStep() + ","
							+ String.valueOf(this.getDecimals()) + ");");
				} else {
					this.setValidator(this.createValidator());
				}
			}
		}
		this.changed_ = false;
		super.updateDom(element, all);
		if (all && this.isNativeControl()) {
			element.setAttribute("type", "number");
		}
	}

	protected void render(EnumSet<RenderFlag> flags) {
		if (!this.setup_
				&& !EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			this.setup();
		}
		super.render(flags);
	}

	void setFormData(final WObject.FormData formData) {
		super.setFormData(formData);
		this.parseValue(this.getText());
	}

	void propagateRenderOk(boolean deep) {
		this.changed_ = false;
		super.propagateRenderOk(deep);
	}

	abstract String getJsMinMaxStep();

	abstract int getDecimals();

	abstract boolean parseNumberValue(final String text);

	abstract WString getTextFromValue();

	abstract WValidator createValidator();

	protected abstract WValidator.Result getValidateRange();

	protected int boxPadding(Orientation orientation) {
		if (!this.isNativeControl() && orientation == Orientation.Horizontal) {
			return super.boxPadding(orientation) + 8;
		} else {
			return super.boxPadding(orientation);
		}
	}

	boolean changed_;
	boolean valueChangedConnection_;
	private boolean preferNative_;
	private boolean setup_;
	private WString prefix_;
	private WString suffix_;

	private void defineJavaScript() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WSpinBox.js", wtjs1());
		String jsObj = "new Wt3_3_1.WSpinBox(" + app.getJavaScriptClass() + ","
				+ this.getJsRef() + "," + String.valueOf(this.getDecimals())
				+ ","
				+ WString.toWString(this.getPrefix()).getJsStringLiteral()
				+ ","
				+ WString.toWString(this.getSuffix()).getJsStringLiteral()
				+ "," + this.getJsMinMaxStep() + ");";
		this.setJavaScriptMember(" WSpinBox", jsObj);
	}

	private void connectJavaScript(final AbstractEventSignal s,
			final String methodName) {
		String jsFunction = "function(obj, event) {var o = jQuery.data("
				+ this.getJsRef() + ", 'obj');if (o) o." + methodName
				+ "(obj, event);}";
		s.addListener(jsFunction);
	}

	private void setup() {
		this.setup_ = true;
		boolean useNative = this.isNativeControl();
		if (!useNative) {
			this.defineJavaScript();
			final AbstractEventSignal b = this.mouseMoved();
			final AbstractEventSignal c = this.keyWentDown();
			this.connectJavaScript(this.mouseMoved(), "mouseMove");
			this.connectJavaScript(this.mouseWentUp(), "mouseUp");
			this.connectJavaScript(this.mouseWentDown(), "mouseDown");
			this.connectJavaScript(this.mouseWentOut(), "mouseOut");
			this.connectJavaScript(this.keyWentDown(), "keyDown");
			this.connectJavaScript(this.keyWentUp(), "keyUp");
			this.setValidator(new SpinBoxValidator(this));
		}
	}

	boolean parseValue(final String text) {
		String textUtf8 = text;
		boolean valid = true;
		if (!this.isNativeControl()) {
			valid = false;
			String prefixUtf8 = this.prefix_.toString();
			String suffixUtf8 = this.suffix_.toString();
			if (textUtf8.startsWith(prefixUtf8)) {
				textUtf8 = textUtf8.substring(prefixUtf8.length());
				if (textUtf8.endsWith(suffixUtf8)) {
					textUtf8 = textUtf8.substring(0, 0 + textUtf8.length()
							- suffixUtf8.length());
					valid = true;
				}
			}
		}
		if (valid) {
			valid = textUtf8.length() > 0;
		}
		if (valid) {
			valid = this.parseNumberValue(textUtf8);
		}
		return valid;
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WSpinBox",
				"function(u,c,l,m,f,g,h,i){function j(){return!!c.getAttribute(\"readonly\")}function n(){var a=c.value;if(a.substr(0,m.length)==m){a=a.substr(m.length);if(a.length>f.length&&a.substr(a.length-f.length,f.length)==f){a=a.substr(0,a.length-f.length);return Number(a)}}return null}function p(a){if(a>h)a=h;else if(a<g)a=g;c.value=m+a.toFixed(l)+f;o=true}function q(){var a=n();if(a!==null){a+=i;p(a)}}function r(){var a=n();if(a!==null){a-=i;p(a)}}jQuery.data(c, \"obj\",this);var d=u.WT,e=$(c),k=null,s,o=false,t=null;this.update=function(a,b,v,w){g=a;h=b;i=v;l=w;t=new (l==0?d.WIntValidator:d.WDoubleValidator)(true,g,h,\"Must be a number\",\"Must be a number\",\"The number must be at least \"+g,\"The number may be at most \"+h)};this.mouseOut=function(){e.removeClass(\"dn\").removeClass(\"up\")};this.mouseMove=function(a,b){if(!j())if(k){a=d.pageCoordinates(b).y-k.y;b=s;if(b!==null){b-=a*i;p(b)}}else{a=d.widgetCoordinates(c,b);if(e.hasClass(\"dn\")||e.hasClass(\"up\"))e.removeClass(\"dn\").removeClass(\"up\"); if(a.x>c.offsetWidth-16){b=c.offsetHeight/2;if(a.y>=b-1&&a.y<=b+1)c.style.cursor=\"crosshair\";else{c.style.cursor=\"default\";a.y<b-1?e.addClass(\"up\"):e.addClass(\"dn\")}}else if(c.style.cursor!=\"\")c.style.cursor=\"\"}};this.mouseDown=function(a,b){d.capture(null);if(!j())if(c.style.cursor==\"crosshair\"){d.capture(null);d.capture(c);e.addClass(\"unselectable\");k=d.pageCoordinates(b);s=n()}else{a=d.widgetCoordinates(c,b);if(a.x>c.offsetWidth-16){d.cancelEvent(b);d.capture(c);e.addClass(\"unselectable\");a.y< c.offsetHeight/2?d.eventRepeat(function(){q()}):d.eventRepeat(function(){r()})}}};this.mouseUp=function(a){e.removeClass(\"unselectable\");if(!j()){if(o||k!=null){k=null;a.onchange()}d.stopRepeat()}};this.keyDown=function(a,b){if(!j())if(b.keyCode==40)d.eventRepeat(function(){r()});else b.keyCode==38&&d.eventRepeat(function(){q()})};this.keyUp=function(a){if(!j()){if(o){o=false;a.onchange()}d.stopRepeat()}};this.validate=function(){var a=n();if(a===null)a=\"a\";return t.validate(a)};this.update(g,h,i, l)}");
	}
}
