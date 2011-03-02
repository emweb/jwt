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

/**
 * An abstract spin box.
 * <p>
 * 
 * <h3>CSS</h3>
 * <p>
 * Using HTML4, the widget is implemented using a &lt;input
 * type=&quot;text&quot;&gt; The element can be styled using the
 * <code>Wt-spinbox</code> style. It may be styled through the current theme, or
 * you can override the style using internal or external CSS as appropriate.
 */
public abstract class WAbstractSpinBox extends WLineEdit {
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
			WEnvironment env = WApplication.getInstance().getEnvironment();
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
	 * <blockquote>
	 * 
	 * <pre>
	 * spinBox.setPrefix(&quot;$ &quot;);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * The default prefix is empty.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>Not supported by the native controls. </i>
	 * </p>
	 */
	public void setPrefix(CharSequence prefix) {
		this.prefix_ = WString.toWString(prefix);
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
	 * <blockquote>
	 * 
	 * <pre>
	 * spinBox.setSuffix(&quot; crates&quot;);
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * The default suffix is empty.
	 * <p>
	 * <p>
	 * <i><b>Note: </b>Not supported by the native controls. </i>
	 * </p>
	 */
	public void setSuffix(CharSequence suffix) {
		this.suffix_ = WString.toWString(suffix);
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

	public void setText(String text) {
		this.parseValue(text);
		super.setText(this.getTextFromValue().toString());
	}

	/**
	 * Constructor.
	 */
	protected WAbstractSpinBox(WContainerWidget parent) {
		super(parent);
		this.changed_ = false;
		this.valueChangedConnection_ = false;
		this.preferNative_ = false;
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

	void updateDom(DomElement element, boolean all) {
		if (all || this.changed_) {
			if (!all) {
				if (!this.isNativeControl()) {
					WApplication.getInstance().doJavaScript(
							"jQuery.data(" + this.getJsRef()
									+ ", 'obj').update("
									+ this.getJsMinMaxStep() + ");");
				} else {
					this.setValidator(this.getCreateValidator());
				}
			}
		}
		this.changed_ = false;
		super.updateDom(element, all);
		if (all && this.isNativeControl()) {
			element.setAttribute("type", "number");
		}
	}

	void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			boolean useNative = this.isNativeControl();
			this.setup(useNative);
		}
		super.render(flags);
	}

	void setFormData(WObject.FormData formData) {
		super.setFormData(formData);
		this.parseValue(this.getText());
	}

	void propagateRenderOk(boolean deep) {
		this.changed_ = false;
		super.propagateRenderOk(deep);
	}

	protected abstract String getJsMinMaxStep();

	protected abstract int getDecimals();

	protected abstract boolean parseNumberValue(String text);

	protected abstract WString getTextFromValue();

	protected abstract WValidator getCreateValidator();

	protected int boxPadding(Orientation orientation) {
		if (!this.isNativeControl() && orientation == Orientation.Horizontal) {
			return 16;
		} else {
			return super.boxPadding(orientation);
		}
	}

	protected boolean changed_;
	protected boolean valueChangedConnection_;
	private boolean preferNative_;
	private WString prefix_;
	private WString suffix_;

	private void defineJavaScript() {
		WApplication app = WApplication.getInstance();
		String THIS_JS = "js/WSpinBox.js";
		if (!app.isJavaScriptLoaded(THIS_JS)) {
			app.doJavaScript(wtjs1(app), false);
			app.setJavaScriptLoaded(THIS_JS);
		}
		String jsObj = "new Wt3_1_8.WSpinBox(" + app.getJavaScriptClass() + ","
				+ this.getJsRef() + "," + String.valueOf(this.getDecimals())
				+ ","
				+ WString.toWString(this.getPrefix()).getJsStringLiteral()
				+ ","
				+ WString.toWString(this.getSuffix()).getJsStringLiteral()
				+ "," + this.getJsMinMaxStep() + ");";
		this.setJavaScriptMember("_a", "0;" + jsObj);
	}

	private void connectJavaScript(AbstractEventSignal s, String methodName) {
		String jsFunction = "function(obj, event) {var o = jQuery.data("
				+ this.getJsRef() + ", 'obj');if (o) o." + methodName
				+ "(obj, event);}";
		s.addListener(jsFunction);
	}

	private void setup(boolean useNative) {
		if (useNative) {
			this.setValidator(this.getCreateValidator());
		} else {
			this.defineJavaScript();
			this.setStyleClass("Wt-spinbox");
			AbstractEventSignal b = this.mouseMoved();
			AbstractEventSignal c = this.keyWentDown();
			this.connectJavaScript(this.mouseMoved(), "mouseMove");
			this.connectJavaScript(this.mouseWentUp(), "mouseUp");
			this.connectJavaScript(this.mouseWentDown(), "mouseDown");
			this.connectJavaScript(this.mouseWentOut(), "mouseOut");
			this.connectJavaScript(this.keyWentDown(), "keyDown");
			this.connectJavaScript(this.keyWentUp(), "keyUp");
			this.setValidator(new SpinBoxValidator(this));
		}
	}

	boolean parseValue(String text) {
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
			valid = this.parseNumberValue(textUtf8);
		}
		return valid;
	}

	static String wtjs1(WApplication app) {
		String s = "function(t,c,j,k,e,f,g,h){function l(){var a=c.value;if(a.substr(0,k.length)==k){a=a.substr(k.length);if(a.length>e.length&&a.substr(a.length-e.length,e.length)==e){a=a.substr(0,a.length-e.length);return Number(a)}}return null}function o(a){if(a>g)a=g;else if(a<f)a=f;c.value=k+a.toFixed(j)+e;m=true}function p(){var a=l();if(a!==null){a+=h;o(a)}}function q(){var a=l();if(a!==null){a-=h;o(a)}}jQuery.data(c,\"obj\",this);var d=t.WT,n=$(c),i=null,r,m=false,s=null; this.update=function(a,b,u,v){f=a;g=b;h=u;j=v;s=new (j==0?d.WIntValidator:d.WDoubleValidator)(true,f,g,\"Must be a number\",\"Must be a number\",\"The number must be at least \"+f,\"The number may be at most \"+g)};this.mouseOut=function(){n.removeClass(\"Wt-spinbox-dn\").removeClass(\"Wt-spinbox-up\")};this.mouseMove=function(a,b){if(i){a=d.pageCoordinates(b).y-i.y;b=r;if(b!==null){b-=a*h;o(b)}}else{a=d.widgetCoordinates(c,b);n.removeClass(\"Wt-spinbox-dn\").removeClass(\"Wt-spinbox-up\");if(a.x>c.offsetWidth-16){b= c.offsetHeight/2;if(a.y>=b-1&&a.y<=b+1)c.style.cursor=\"crosshair\";else{c.style.cursor=\"default\";a.y<b-1?n.addClass(\"Wt-spinbox-up\"):n.addClass(\"Wt-spinbox-dn\")}}else c.style.cursor=\"\"}};this.mouseDown=function(a,b){if(c.style.cursor==\"crosshair\"){d.capture(null);d.capture(c);i=d.pageCoordinates(b);r=l()}else{a=d.widgetCoordinates(c,b);if(a.x>c.offsetWidth-16){d.cancelEvent(b);d.capture(c);a.y<c.offsetHeight/2?d.eventRepeat(function(){p()}):d.eventRepeat(function(){q()})}}};this.mouseUp=function(a){if(m|| i!=null){i=null;a.onchange()}d.stopRepeat()};this.keyDown=function(a,b){if(b.keyCode==40)d.eventRepeat(function(){q()});else b.keyCode==38&&d.eventRepeat(function(){p()})};this.keyUp=function(a){if(m){m=false;a.onchange()}d.stopRepeat()};this.validate=function(){var a=l();if(a===null)a=\"a\";return s.validate(a)};this.update(f,g,h,j)}";
		if ("ctor.WSpinBox".indexOf(".prototype") != -1) {
			return "Wt3_1_8.ctor.WSpinBox = " + s + ";";
		} else {
			if ("ctor.WSpinBox".substring(0, 5).compareTo(
					"ctor.".substring(0, 5)) == 0) {
				return "Wt3_1_8." + "ctor.WSpinBox".substring(5) + " = " + s
						+ ";";
			} else {
				return "Wt3_1_8.ctor.WSpinBox = function() { (" + s
						+ ").apply(Wt3_1_8, arguments) };";
			}
		}
	}
}
