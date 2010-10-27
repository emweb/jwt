/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.EnumSet;
import eu.webtoolkit.jwt.utils.EnumUtils;

/**
 * A spin box.
 * <p>
 * 
 * The spin box provides a control for entering a number. It consists of a line
 * edit, and buttons which allow to increase or decrease the value.
 * <p>
 * WSpinBox is an {@link WWidget#setInline(boolean inlined) inline} widget.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * Using HTML4, the widget is implemented using a &lt;input
 * type=&quot;text&quot;&gt; The element can be styled using the
 * <code>Wt-spinbox</code> style. It may be styled through the current theme, or
 * you can override the style using internal or external CSS as appropriate.
 * <p>
 * <p>
 * <i><b>Note: </b>With the advent of HTML5, this widget will be implemented
 * using the native HTML5 control when available. </i>
 * </p>
 */
public class WSpinBox extends WLineEdit {
	/**
	 * Creates a spin-box.
	 * <p>
	 * The range is (0-99) and the step size 1.
	 */
	public WSpinBox(WContainerWidget parent) {
		super(parent);
		this.min_ = 0;
		this.max_ = 99;
		this.step_ = 1;
		this.changed_ = false;
		this.valueChanged_ = new Signal1<Double>();
		this.setStyleClass("Wt-spinbox");
		this.changed().addListener(this, new Signal.Listener() {
			public void trigger() {
				WSpinBox.this.onChange();
			}
		});
		AbstractEventSignal b = this.mouseMoved();
		AbstractEventSignal c = this.keyWentDown();
		this.connectJavaScript(this.mouseMoved(), "mouseMove");
		this.connectJavaScript(this.mouseWentUp(), "mouseUp");
		this.connectJavaScript(this.mouseWentDown(), "mouseDown");
		this.connectJavaScript(this.mouseWentOut(), "mouseOut");
		this.connectJavaScript(this.keyWentDown(), "keyDown");
		this.updateValidator();
		this.setValue(0);
	}

	/**
	 * Creates a spin-box.
	 * <p>
	 * Calls {@link #WSpinBox(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WSpinBox() {
		this((WContainerWidget) null);
	}

	/**
	 * Sets the minimum value.
	 * <p>
	 * The default value is 0.
	 */
	public void setMinimum(double minimum) {
		this.min_ = minimum;
		this.changed_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		this.updateValidator();
	}

	/**
	 * Returns the minimum value.
	 * <p>
	 * 
	 * @see WSpinBox#setMinimum(double minimum)
	 */
	public double getMinimum() {
		return this.min_;
	}

	/**
	 * Sets the maximum value.
	 * <p>
	 * The default value is 99.
	 */
	public void setMaximum(double maximum) {
		this.max_ = maximum;
		this.changed_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		this.updateValidator();
	}

	/**
	 * Returns the maximum value.
	 * <p>
	 * 
	 * @see WSpinBox#setMaximum(double maximum)
	 */
	public double getMaximum() {
		return this.max_;
	}

	/**
	 * Sets the range.
	 * <p>
	 * 
	 * @see WSpinBox#setMinimum(double minimum)
	 * @see WSpinBox#setMaximum(double maximum)
	 */
	public void setRange(double minimum, double maximum) {
		this.min_ = minimum;
		this.max_ = maximum;
		this.changed_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		this.updateValidator();
	}

	/**
	 * Sets the step value.
	 * <p>
	 * The default value is 1.
	 */
	public void setSingleStep(double step) {
		this.step_ = step;
		this.changed_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		this.updateValidator();
	}

	/**
	 * Returns the step value.
	 */
	public double getSingleStep() {
		return this.step_;
	}

	/**
	 * Sets the value.
	 * <p>
	 * <code>value</code> must be a value between {@link WSpinBox#getMinimum()
	 * getMinimum()} and {@link WSpinBox#getMaximum() getMaximum()}.
	 * <p>
	 * The default value is 0
	 */
	public void setValue(double value) {
		if ((int) value == value) {
			this.setText(String.valueOf((int) value));
		} else {
			this.setText(String.valueOf(value));
		}
		this.valueChanged_.trigger(value);
		this.changed_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
	}

	/**
	 * Returns the value.
	 */
	public double getValue() {
		try {
			return Double.parseDouble(this.getText());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * A signal that indicates when the value has changed.
	 * <p>
	 * This signal is emitted when {@link WSpinBox#setValue(double value)
	 * setValue()} is called.
	 * <p>
	 * 
	 * @see WSpinBox#setValue(double value)
	 */
	public Signal1<Double> valueChanged() {
		return this.valueChanged_;
	}

	void updateDom(DomElement element, boolean all) {
		if (this.changed_) {
			element.callJavaScript("jQuery.data(" + this.getJsRef()
					+ ", 'obj').update(" + String.valueOf(this.min_) + ','
					+ String.valueOf(this.max_) + ','
					+ String.valueOf(this.step_) + ");");
			this.changed_ = false;
		}
		super.updateDom(element, all);
	}

	void propagateRenderOk(boolean deep) {
		this.changed_ = false;
		super.propagateRenderOk(deep);
	}

	void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()) {
			this.defineJavaScript();
		}
		super.render(flags);
	}

	private double min_;
	private double max_;
	private double step_;
	private boolean changed_;
	private Signal1<Double> valueChanged_;

	private void onChange() {
		this.valueChanged_.trigger(this.getValue());
	}

	private void defineJavaScript() {
		WApplication app = WApplication.getInstance();
		String THIS_JS = "js/WSpinBox.js";
		if (!app.isJavaScriptLoaded(THIS_JS)) {
			app.doJavaScript(wtjs1(app), false);
			app.setJavaScriptLoaded(THIS_JS);
		}
		app.doJavaScript("new Wt3_1_6.WSpinBox(" + app.getJavaScriptClass()
				+ "," + this.getJsRef() + "," + String.valueOf(this.min_) + ","
				+ String.valueOf(this.max_) + "," + String.valueOf(this.step_)
				+ ");");
		this.changed_ = false;
	}

	private void connectJavaScript(AbstractEventSignal s, String methodName) {
		String jsFunction = "function(obj, event) {var o = jQuery.data("
				+ this.getJsRef() + ", 'obj');if (o) o." + methodName
				+ "(obj, event);}";
		s.addListener(jsFunction);
	}

	private void updateValidator() {
		WValidator v = this.getValidator();
		if (!(v != null)) {
			this.setValidator(new WDoubleValidator(this.min_, this.max_));
		} else {
			WDoubleValidator dv = ((v) instanceof WDoubleValidator ? (WDoubleValidator) (v)
					: null);
			dv.setRange(this.min_, this.max_);
		}
	}

	static String wtjs1(WApplication app) {
		return "Wt3_1_6.WSpinBox = function(n,b,h,i,f){function j(a){if(a>i)a=i;else if(a<h)a=h;b.value=a}function k(){var a=Number(b.value);a+=f;j(a);b.onchange()}function l(){var a=Number(b.value);a-=f;j(a);b.onchange()}jQuery.data(b,\"obj\",this);var d=n.WT,g=$(b),e=null,m;this.update=function(a,c,o){h=a;i=c;f=o};this.mouseOut=function(){g.removeClass(\"Wt-spinbox-dn\").removeClass(\"Wt-spinbox-up\")};this.mouseMove=function(a,c){if(e){a=d.pageCoordinates(c).y-e.y;c=m;c-=a*f;j(c)}else{a=d.widgetCoordinates(b, c);g.removeClass(\"Wt-spinbox-dn\").removeClass(\"Wt-spinbox-up\");if(a.x>b.offsetWidth-16){c=b.offsetHeight/2;if(a.y>=c-1&&a.y<=c+1)b.style.cursor=\"crosshair\";else{b.style.cursor=\"default\";a.y<c-1?g.addClass(\"Wt-spinbox-up\"):g.addClass(\"Wt-spinbox-dn\")}}else b.style.cursor=\"\"}};this.mouseDown=function(a,c){if(b.style.cursor==\"crosshair\"){d.capture(null);d.capture(b);e=d.pageCoordinates(c);m=Number(b.value)}else if(d.widgetCoordinates(b,c).x>b.offsetWidth-16){d.cancelEvent(c);d.capture(b)}};this.keyDown= function(a,c){if(c.keyCode==40)l();else c.keyCode==38&&k()};this.mouseUp=function(a,c){if(e!=null){e=null;a.onchange()}else{a=d.widgetCoordinates(b,c);if(a.x>b.offsetWidth-16)a.y<b.offsetHeight/2?k():l()}}};";
	}
}
