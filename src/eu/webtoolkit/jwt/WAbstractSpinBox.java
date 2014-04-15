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
			this.setText(this.getTextFromValue());
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
			this.setText(this.getTextFromValue());
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
		String jsObj = "new Wt3_3_2.WSpinBox("
				+ app.getJavaScriptClass()
				+ ","
				+ this.getJsRef()
				+ ","
				+ String.valueOf(this.getDecimals())
				+ ","
				+ WString.toWString(this.getPrefix()).getJsStringLiteral()
				+ ","
				+ WString.toWString(this.getSuffix()).getJsStringLiteral()
				+ ","
				+ this.getJsMinMaxStep()
				+ ","
				+ jsStringLiteral(LocaleUtils.getDecimalPoint(LocaleUtils
						.getCurrentLocale()))
				+ ","
				+ jsStringLiteral(LocaleUtils.getGroupSeparator(LocaleUtils
						.getCurrentLocale())) + ");";
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
				"function(k,t,z,w,c,o,C,g,m,E){var I=0;var y=1;var s=\"Must be a number\";var H=\"The number must be at least \";var x=\"The number may be at most \";var l=\"dn\";var v=\"up\";var j=\"unselectable\";jQuery.data(t,\"obj\",this);var r=this,A=k.WT,i=38,D=40,e=\"crosshair\",B=$(t);var G=null,F,f=false;var d=null;var q=false;function n(){return !!t.getAttribute(\"readonly\")}function u(K){var J=\"\";for(var L=0;L<K.length();L++){J+=K.charAt(L)}}function b(){var K=B.data(\"lobj\");var J=\"\";if(K!==undefined){J=K.getValue();if(J===\"\"){J=w+\"0\"+c}}else{J=t.value}if(J.substr(0,w.length)==w){J=J.substr(w.length);if(J.length>c.length&&J.substr(J.length-c.length,c.length)==c){J=J.substr(0,J.length-c.length);if(E){J=J.split(E).join(\"\")}J=J.replace(m,\".\");return Number(J)}}return null}function a(K){var O=B.data(\"lobj\");if(K>C){K=C}else{if(K<o){K=o}}var N=K.toFixed(z);N=N.replace(\".\",m);var M=N.indexOf(m);var J=\"\";if(M!==-1){for(var L=0;L<M;L++){J+=N.charAt(L);if(L<M-1&&((M-L-1)%3===0)){J+=E}}J+=N.substr(M)}else{J=N}if(O!==undefined){O.setValue(w+J+c)}else{t.value=w+J+c}f=true}function h(){var J=b();if(J!==null){J+=g;a(J)}}function p(){var J=b();if(J!==null){J-=g;a(J)}}this.setIsDoubleSpinBox=function(J){q=J;this.update(o,C,g,z)};this.update=function(N,K,J,L){o=N;C=K;g=J;z=L;var M=q||A.WIntValidator===undefined?A.WDoubleValidator:A.WIntValidator;d=new M(true,o,C,s,s,H+o,x+C)};this.mouseOut=function(K,J){B.removeClass(l).removeClass(v)};this.mouseMove=function(O,M){if(n()){return}if(!G){var N=A.widgetCoordinates(t,M);if(B.hasClass(l)||B.hasClass(v)){B.removeClass(l).removeClass(v)}if(N.x>t.offsetWidth-16){var L=t.offsetHeight/2;if(N.y>=L-1&&N.y<=L+1){t.style.cursor=e}else{t.style.cursor=\"default\";if(N.y<L-1){B.addClass(v)}else{B.addClass(l)}}}else{if(t.style.cursor!=\"\"){t.style.cursor=\"\"}}}else{var J=A.pageCoordinates(M).y-G.y;var K=F;if(K!==null){K=K-J*g;a(K)}}};this.mouseDown=function(M,K){A.capture(null);if(n()){return}if(t.style.cursor==e){A.capture(null);A.capture(t);B.addClass(j);G=A.pageCoordinates(K);F=b()}else{var L=A.widgetCoordinates(t,K);if(L.x>t.offsetWidth-16){A.cancelEvent(K);A.capture(t);B.addClass(j);var J=t.offsetHeight/2;if(L.y<J){A.eventRepeat(function(){h()})}else{A.eventRepeat(function(){p()})}}}};this.mouseUp=function(K,J){B.removeClass(j);if(n()){return}if(f||G!=null){G=null;K.onchange()}A.stopRepeat()};this.keyDown=function(K,J){if(n()){return}if(J.keyCode==D){A.eventRepeat(function(){p()})}else{if(J.keyCode==i){A.eventRepeat(function(){h()})}}};this.keyUp=function(K,J){if(n()){return}if(f){f=false;K.onchange()}A.stopRepeat()};this.setLocale=function(J,K){m=J;E=K};this.validate=function(K){var J=b();if(J===null){J=\"a\"}return d.validate(J)};this.update(o,C,g,z)}");
	}
}
