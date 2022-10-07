/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract spin box.
 *
 * <p>Although the element can be rendered using a native HTML5 control, by default it is rendered
 * using an HTML4 compatibility workaround which is implemented using JavaScript and CSS, as most
 * browsers do not yet implement the HTML5 native element.
 */
public abstract class WAbstractSpinBox extends WLineEdit {
  private static Logger logger = LoggerFactory.getLogger(WAbstractSpinBox.class);

  /**
   * Configures whether a native HTML5 control should be used.
   *
   * <p>When <code>native</code>, the new &quot;number&quot; input element, specified by HTML5 and
   * when implemented by the browser, is used rather than the built-in element. The native control
   * is styled by the browser (usually in sync with the OS) rather than through the theme chosen.
   *
   * <p>The default is <code>false</code> (as native support is now well implemented).
   */
  public void setNativeControl(boolean nativeControl) {
    this.preferNative_ = nativeControl;
  }
  /**
   * Returns whether a native HTML5 control is used.
   *
   * <p>Taking into account the preference for a native control, configured using {@link
   * WAbstractSpinBox#setNativeControl(boolean nativeControl) setNativeControl()}, this method
   * returns whether a native control is actually being used.
   */
  public boolean isNativeControl() {
    if (this.preferNative_) {
      if (super.getInputMask().length() != 0) {
        return false;
      }
      final WEnvironment env = WApplication.getInstance().getEnvironment();
      if (env.agentIsChrome()
              && (int) env.getAgent().getValue() >= (int) UserAgent.Chrome5.getValue()
          || env.agentIsSafari()
              && (int) env.getAgent().getValue() >= (int) UserAgent.Safari4.getValue()
          || env.agentIsOpera()
              && (int) env.getAgent().getValue() >= (int) UserAgent.Opera10.getValue()) {
        return true;
      }
    }
    return false;
  }
  /**
   * Sets a prefix.
   *
   * <p>Option to set a prefix string shown in front of the value, e.g.:
   *
   * <p>
   *
   * <pre>{@code
   * spinBox.setPrefix("$ ");
   *
   * }</pre>
   *
   * <p>The default prefix is empty.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Not supported by the native controls. </i>
   */
  public void setPrefix(final CharSequence prefix) {
    if (!(this.prefix_.toString().equals(prefix.toString()))) {
      this.prefix_ = WString.toWString(prefix);
      this.setText(this.getTextFromValue());
      this.changed_ = true;
      this.repaint();
    }
  }
  /**
   * Returns the prefix.
   *
   * <p>
   *
   * @see WAbstractSpinBox#setPrefix(CharSequence prefix)
   */
  public WString getPrefix() {
    return this.prefix_;
  }
  /**
   * Sets a suffix.
   *
   * <p>Option to set a suffix string shown to the right of the value, e.g.:
   *
   * <p>
   *
   * <pre>{@code
   * spinBox.setSuffix(" crates");
   *
   * }</pre>
   *
   * <p>The default suffix is empty.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Not supported by the native controls. </i>
   */
  public void setSuffix(final CharSequence suffix) {
    if (!(this.suffix_.toString().equals(suffix.toString()))) {
      this.suffix_ = WString.toWString(suffix);
      this.setText(this.getTextFromValue());
      this.changed_ = true;
      this.repaint();
    }
  }
  /**
   * Returns the suffix.
   *
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

  public ValidationState validate() {
    return super.validate();
  }

  public void refresh() {
    if (this.isRendered()) {
      this.doJavaScript(
          this.getJsRef()
              + ".wtObj.setLocale("
              + jsStringLiteral(LocaleUtils.getDecimalPoint(LocaleUtils.getCurrentLocale()))
              + ","
              + jsStringLiteral(LocaleUtils.getGroupSeparator(LocaleUtils.getCurrentLocale()))
              + ");");
    }
    super.refresh();
  }

  public JSignal2<Integer, Integer> jsValueChanged() {
    return this.jsValueChanged_;
  }
  /** Constructor. */
  protected WAbstractSpinBox(WContainerWidget parentContainer) {
    super();
    this.changed_ = false;
    this.valueChangedConnection_ = false;
    this.preferNative_ = false;
    this.setup_ = false;
    this.prefix_ = new WString();
    this.suffix_ = new WString();
    this.jsValueChanged_ = new JSignal2<Integer, Integer>(this, "spinboxValueChanged", true) {};
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #WAbstractSpinBox(WContainerWidget parentContainer)
   * this((WContainerWidget)null)}
   */
  protected WAbstractSpinBox() {
    this((WContainerWidget) null);
  }

  void updateDom(final DomElement element, boolean all) {
    if (all || this.changed_) {
      if (!all) {
        if (!this.isNativeControl()) {
          this.doJavaScript(
              this.getJsRef()
                  + ".wtObj.configure("
                  + String.valueOf(this.getDecimals())
                  + ","
                  + WString.toWString(this.getPrefix()).getJsStringLiteral()
                  + ","
                  + WString.toWString(this.getSuffix()).getJsStringLiteral()
                  + ","
                  + this.getJsMinMaxStep()
                  + ");");
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
    if (!this.setup_ && flags.contains(RenderFlag.Full)) {
      this.setup();
    }
    if (this.jsValueChanged().needsUpdate(true)) {
      StringBuilder function = new StringBuilder();
      function.append(this.getJsRef()).append(".wtObj.jsValueChanged=");
      if (this.jsValueChanged().isConnected()) {
        function
            .append("function(oldv, v){")
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
    ss.append("new Wt4_8_1.WSpinBox(")
        .append(app.getJavaScriptClass())
        .append(",")
        .append(this.getJsRef())
        .append(",")
        .append(this.getDecimals())
        .append(",")
        .append(WString.toWString(this.getPrefix()).getJsStringLiteral())
        .append(",")
        .append(WString.toWString(this.getSuffix()).getJsStringLiteral())
        .append(",")
        .append(this.getJsMinMaxStep())
        .append(",")
        .append(jsStringLiteral(LocaleUtils.getDecimalPoint(LocaleUtils.getCurrentLocale())))
        .append(",")
        .append(jsStringLiteral(LocaleUtils.getGroupSeparator(LocaleUtils.getCurrentLocale())))
        .append(");");
    this.setJavaScriptMember(" WSpinBox", ss.toString());
  }

  private void connectJavaScript(final AbstractEventSignal s, final String methodName) {
    String jsFunction =
        "function(obj, event) {var o = "
            + this.getJsRef()
            + ";if (o && o.wtObj) o.wtObj."
            + methodName
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
          textUtf8 = textUtf8.substring(0, 0 + textUtf8.length() - suffixUtf8.length());
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
        "(function(e,t,s,n,a,o,i,l,r,u){var f=\"Must be a number\",c=\"The number must be at least \",h=\"The number may be at most \",d=\"dn\",v=\"up\",p=\"unselectable\";t.wtObj=this;var g,y=this,b=e.WT,C=\"crosshair\",m=$(t),W=null,x=!1,R=null,w=!1,j=!1;function E(){return t.readOnly}function V(){var e=t.wtLObj,s=\"\";void 0!==e?\"\"===(s=e.getValue())&&(s=n+\"0\"+a):s=t.value;if(s.substr(0,n.length)==n&&(s=s.substr(n.length)).length>a.length&&s.substr(s.length-a.length,a.length)==a){s=s.substr(0,s.length-a.length);u&&(s=s.split(u).join(\"\"));s=s.replace(r,\".\");return Number(s)}return null}function D(e){var l=t.wtLObj;if(e>i)if(j){range=i-o;e=o+(e-o)%(range+1)}else e=i;else if(e<o)if(j){range=i-o;e=i-(Math.abs(e-o)-1)%(range+1)}else e=o;var f=e.toFixed(s),c=(f=f.replace(\".\",r)).indexOf(r),h=\"\";if(-1!==c){for(var d=0;d<c;d++){h+=f.charAt(d);d<c-1&&(c-d-1)%3==0&&(h+=u)}h+=f.substr(c)}else h=f;var v=t.value;void 0!==l?l.setValue(n+h+a):t.value=n+h+a;x=!0;y.jsValueChanged(v,e)}function O(){var e=V();null!==e&&D(e+=l)}function M(){var e=V();null!==e&&D(e-=l)}this.setIsDoubleSpinBox=function(e){w=e;this.configure(s,n,a,o,i,l)};this.setWrapAroundEnabled=function(e){j=e};this.configure=function(e,t,r,u,d,v){s=e;n=t;a=r;o=u;i=d;l=v;var p=w||void 0===b.WIntValidator;R=p?new b.WDoubleValidator(!0,!1,o,i,\".\",\"\",f,f,c+o,h+i):new b.WIntValidator(!0,o,i,\"\",f,f,c+o,h+i)};this.mouseOut=function(e,t){m.removeClass(d).removeClass(v)};this.mouseMove=function(e,s){if(!E())if(W){var n=b.pageCoordinates(s).y-W.y,a=g;null!==a&&D(a-=n*l)}else{var o=b.widgetCoordinates(t,s);(m.hasClass(d)||m.hasClass(v))&&m.removeClass(d).removeClass(v);var i=-1;\"object\"==typeof b.theme&&\"bootstrap\"===b.theme.type&&(i=b.theme.version);if(i>=4&&o.x>t.offsetWidth-30&&o.x<t.offsetWidth-10){var r=t.offsetHeight/2;if(o.y>=r-3&&o.y<=r+3)t.style.cursor=C;else{t.style.cursor=\"default\";o.y<r-1?m.addClass(v):m.addClass(d)}}else if(i<4&&o.x>t.offsetWidth-22){r=t.offsetHeight/2;if(o.y>=r-3&&o.y<=r+3)t.style.cursor=C;else{t.style.cursor=\"default\";o.y<r-1?m.addClass(v):m.addClass(d)}}else\"\"!=t.style.cursor&&(t.style.cursor=\"\")}};this.mouseDown=function(e,s){b.capture(null);if(!E())if(t.style.cursor==C){b.capture(null);b.capture(t);m.addClass(p);W=b.pageCoordinates(s);g=V()}else{var n=b.widgetCoordinates(t,s),a=-1;\"object\"==typeof b.theme&&\"bootstrap\"===b.theme.type&&(a=b.theme.version);if(a>=5&&n.x>t.offsetWidth-30&&n.x<t.offsetWidth-10){b.cancelEvent(s);b.capture(t);m.addClass(p);var o=t.offsetHeight/2;n.y<o?b.eventRepeat((function(){O()})):b.eventRepeat((function(){M()}))}else if(a<4&&n.x>t.offsetWidth-22){b.cancelEvent(s);b.capture(t);m.addClass(p);o=t.offsetHeight/2;n.y<o?b.eventRepeat((function(){O()})):b.eventRepeat((function(){M()}))}}};this.mouseUp=function(e,t){m.removeClass(p);if(!E()){if(x||null!=W){W=null;x=!1;e.onchange()}b.stopRepeat()}};this.keyDown=function(e,t){E()||(40==t.keyCode?b.eventRepeat((function(){M()})):38==t.keyCode&&b.eventRepeat((function(){O()})))};this.keyUp=function(e,t){if(!E()){if(x){x=!1;e.onchange()}b.stopRepeat()}};this.setLocale=function(e,t){r=e;u=t};this.validate=function(e){var t=V();null===t&&(t=\"a\");return R.validate(t)};this.jsValueChanged=function(){};this.setIsDoubleSpinBox(w)})");
  }
}
