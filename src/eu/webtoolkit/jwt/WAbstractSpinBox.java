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
			if (super.getInputMask().length() != 0) {
				return false;
			}
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
	 *   {@code
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
			this.setText(this.getTextFromValue());
			this.changed_ = true;
			this.repaint();
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
	 *   {@code
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
			this.setText(this.getTextFromValue());
			this.changed_ = true;
			this.repaint();
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
		super.setText(this.getTextFromValue());
	}

	public WValidator.State validate() {
		return super.validate();
	}

	public void refresh() {
		this.doJavaScript("jQuery.data("
				+ this.getJsRef()
				+ ", 'obj').setLocale("
				+ jsStringLiteral(LocaleUtils.getDecimalPoint(LocaleUtils
						.getCurrentLocale()))
				+ ","
				+ jsStringLiteral(LocaleUtils.getGroupSeparator(LocaleUtils
						.getCurrentLocale())) + ");");
		super.refresh();
	}

	public JSignal2<Integer, Integer> jsValueChanged() {
		return this.jsValueChanged_;
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
		this.jsValueChanged_ = new JSignal2<Integer, Integer>(this,
				"spinboxValueChanged", true) {
		};
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
					this.doJavaScript("jQuery.data("
							+ this.getJsRef()
							+ ", 'obj').configure("
							+ String.valueOf(this.getDecimals())
							+ ","
							+ WString.toWString(this.getPrefix())
									.getJsStringLiteral()
							+ ","
							+ WString.toWString(this.getSuffix())
									.getJsStringLiteral() + ","
							+ this.getJsMinMaxStep() + ");");
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
		if (this.jsValueChanged().needsUpdate(true)) {
			StringBuilder function = new StringBuilder();
			function.append("jQuery.data(" + this.getJsRef()
					+ ",'obj').jsValueChanged=");
			if (this.jsValueChanged().isConnected()) {
				function.append("function(oldv, v){")
						.append("var o=null;var e=null;")
						.append(this.jsValueChanged().createCall("oldv", "v"))
						.append("};");
			} else {
				function.append("function() {};");
			}
			this.doJavaScript(function.toString());
		}
		super.render(flags);
	}

	protected void setFormData(final WObject.FormData formData) {
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

	protected abstract String getTextFromValue();

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
		StringBuilder ss = new StringBuilder();
		ss.append("new Wt3_3_8.WSpinBox(")
				.append(app.getJavaScriptClass())
				.append(",")
				.append(this.getJsRef())
				.append(",")
				.append(this.getDecimals())
				.append(",")
				.append(WString.toWString(this.getPrefix())
						.getJsStringLiteral())
				.append(",")
				.append(WString.toWString(this.getSuffix())
						.getJsStringLiteral())
				.append(",")
				.append(this.getJsMinMaxStep())
				.append(",")
				.append(jsStringLiteral(LocaleUtils.getDecimalPoint(LocaleUtils
						.getCurrentLocale())))
				.append(",")
				.append(jsStringLiteral(LocaleUtils
						.getGroupSeparator(LocaleUtils.getCurrentLocale())))
				.append(");");
		this.setJavaScriptMember(" WSpinBox", ss.toString());
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
			if (!(this.prefix_.length() == 0) || !(this.suffix_.length() == 0)) {
				this.setValidator(new SpinBoxValidator(this));
			}
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

	private JSignal2<Integer, Integer> jsValueChanged_;

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WSpinBox",
				"function(C,c,u,j,g,e,i,n,r,s){function o(){return c.readOnly}function t(){var a=f.data(\"lobj\"),b=\"\";if(a!==undefined){b=a.getValue();if(b===\"\")b=j+\"0\"+g}else b=c.value;if(b.substr(0,j.length)==j){b=b.substr(j.length);if(b.length>g.length&&b.substr(b.length-g.length,g.length)==g){b=b.substr(0,b.length-g.length);if(s)b=b.split(s).join(\"\");b=b.replace(r,\".\");return Number(b)}}return null}function v(a){var b=f.data(\"lobj\");if(a>i)if(w){range=i-e; a=e+(a-e)%(range+1)}else a=i;else if(a<e)if(w){range=i-e;a=i-(Math.abs(a-e)-1)%(range+1)}else a=e;var h=a.toFixed(u);h=h.replace(\".\",r);var l=h.indexOf(r),k=\"\";if(l!==-1){for(var m=0;m<l;m++){k+=h.charAt(m);if(m<l-1&&(l-m-1)%3===0)k+=s}k+=h.substr(l)}else k=h;h=c.value;if(b!==undefined)b.setValue(j+k+g);else c.value=j+k+g;p=true;D.jsValueChanged(h,a)}function y(){var a=t();if(a!==null){a+=n;v(a)}}function z(){var a=t();if(a!==null){a-=n;v(a)}}jQuery.data(c,\"obj\",this);var D=this,d=C.WT,f=$(c),q=null, A,p=false,B=null,x=false,w=false;this.setIsDoubleSpinBox=function(a){x=a;this.configure(u,j,g,e,i,n)};this.setWrapAroundEnabled=function(a){w=a};this.configure=function(a,b,h,l,k,m){u=a;j=b;g=h;e=l;i=k;n=m;B=new (x||typeof d.WIntValidator===\"undefined\"?d.WDoubleValidator:d.WIntValidator)(true,e,i,\"Must be a number\",\"Must be a number\",\"The number must be at least \"+e,\"The number may be at most \"+i)};this.mouseOut=function(){f.removeClass(\"dn\").removeClass(\"up\")};this.mouseMove=function(a,b){if(!o())if(q){a= d.pageCoordinates(b).y-q.y;b=A;if(b!==null){b-=a*n;v(b)}}else{a=d.widgetCoordinates(c,b);if(f.hasClass(\"dn\")||f.hasClass(\"up\"))f.removeClass(\"dn\").removeClass(\"up\");if(a.x>c.offsetWidth-16){b=c.offsetHeight/2;if(a.y>=b-1&&a.y<=b+1)c.style.cursor=\"crosshair\";else{c.style.cursor=\"default\";a.y<b-1?f.addClass(\"up\"):f.addClass(\"dn\")}}else if(c.style.cursor!=\"\")c.style.cursor=\"\"}};this.mouseDown=function(a,b){d.capture(null);if(!o())if(c.style.cursor==\"crosshair\"){d.capture(null);d.capture(c);f.addClass(\"unselectable\"); q=d.pageCoordinates(b);A=t()}else{a=d.widgetCoordinates(c,b);if(a.x>c.offsetWidth-16){d.cancelEvent(b);d.capture(c);f.addClass(\"unselectable\");a.y<c.offsetHeight/2?d.eventRepeat(function(){y()}):d.eventRepeat(function(){z()})}}};this.mouseUp=function(a){f.removeClass(\"unselectable\");if(!o()){if(p||q!=null){q=null;p=false;a.onchange()}d.stopRepeat()}};this.keyDown=function(a,b){if(!o())if(b.keyCode==40)d.eventRepeat(function(){z()});else b.keyCode==38&&d.eventRepeat(function(){y()})};this.keyUp=function(a){if(!o()){if(p){p= false;a.onchange()}d.stopRepeat()}};this.setLocale=function(a,b){r=a;s=b};this.validate=function(){var a=t();if(a===null)a=\"a\";return B.validate(a)};this.jsValueChanged=function(){};this.setIsDoubleSpinBox(x)}");
	}
}
