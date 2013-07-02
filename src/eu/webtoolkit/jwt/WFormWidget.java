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
 * An abstract widget that corresponds to an HTML form element.
 * <p>
 * 
 * A WFormWidget may receive focus, can be disabled, and can have a label that
 * acts as proxy for getting focus. It provides signals which reflect changes to
 * its value, or changes to its focus.
 * <p>
 * Form widgets also have built-in support for validation, using
 * {@link WFormWidget#setValidator(WValidator validator) setValidator()}. If the
 * validator provide client-side validation, then an invalid validation state is
 * reflected using the style class <code>&quot;Wt-invalid&quot;</code>. All
 * validators provided by JWt implement client-side validation.
 * <p>
 * On the server-side, use {@link WFormWidget#validate() validate()} method to
 * validate the content using a validator previously set.
 */
public abstract class WFormWidget extends WInteractWidget {
	private static Logger logger = LoggerFactory.getLogger(WFormWidget.class);

	/**
	 * Creates a WFormWidget with an optional parent.
	 */
	public WFormWidget(WContainerWidget parent) {
		super(parent);
		this.label_ = null;
		this.validator_ = null;
		this.validateJs_ = null;
		this.filterInput_ = null;
		this.removeEmptyText_ = null;
		this.emptyText_ = new WString();
		this.flags_ = new BitSet();
		this.tabIndex_ = 0;
		this.validated_ = new Signal1<WValidator.Result>();
		this.validationToolTip_ = new WString();
	}

	/**
	 * Creates a WFormWidget with an optional parent.
	 * <p>
	 * Calls {@link #WFormWidget(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WFormWidget() {
		this((WContainerWidget) null);
	}

	/**
	 * Destructor.
	 * <p>
	 * If a label was associated with the widget, its buddy is reset to
	 * <code>null</code>.
	 */
	public void remove() {
		if (this.label_ != null) {
			this.label_.setBuddy((WFormWidget) null);
		}
		if (this.validator_ != null) {
			this.validator_.removeFormWidget(this);
		}
		;
		;
		;
		super.remove();
	}

	/**
	 * Returns the label associated with this widget.
	 * <p>
	 * Returns the label (if there is one) that acts as a proxy for this widget.
	 * <p>
	 * 
	 * @see WLabel#setBuddy(WFormWidget buddy)
	 */
	public WLabel getLabel() {
		return this.label_;
	}

	/**
	 * Sets the hidden state of this widget.
	 * <p>
	 * If the widget has a label, it is hidden and shown together with this
	 * widget.
	 */
	public void setHidden(boolean hidden, WAnimation animation) {
		if (this.label_ != null) {
			this.label_.setHidden(hidden, animation);
		}
		super.setHidden(hidden, animation);
	}

	/**
	 * Returns the current value.
	 * <p>
	 * This returns the current value as a string.
	 */
	public abstract String getValueText();

	/**
	 * Sets the value text.
	 * <p>
	 * This sets the current value from a string value.
	 */
	public abstract void setValueText(String value);

	/**
	 * Sets a validator for this field.
	 * <p>
	 * The validator is used to validate the current input.
	 * <p>
	 * If the validator has no parent yet, then ownership is transferred to the
	 * form field, and thus the validator will be deleted together with the form
	 * field.
	 * <p>
	 * The default value is <code>null</code>.
	 * <p>
	 * 
	 * @see WFormWidget#validate()
	 */
	public void setValidator(WValidator validator) {
		boolean firstValidator = !(this.validator_ != null);
		if (this.validator_ != null) {
			this.validator_.removeFormWidget(this);
		}
		this.validator_ = validator;
		if (this.validator_ != null) {
			this.validator_.addFormWidget(this);
			if (firstValidator) {
				this.setToolTip(this.getToolTip());
			}
			this.validatorChanged();
		} else {
			WApplication.getInstance().getTheme().applyValidationStyle(this,
					new WValidator.Result(),
					ValidationStyleFlag.ValidationNoStyle);
			;
			this.validateJs_ = null;
			;
			this.filterInput_ = null;
		}
	}

	/**
	 * Returns the validator.
	 */
	public WValidator getValidator() {
		return this.validator_;
	}

	/**
	 * Validates the field.
	 * <p>
	 * 
	 * @see WFormWidget#validated()
	 */
	public WValidator.State validate() {
		if (this.getValidator() != null) {
			WValidator.Result result = this.getValidator().validate(
					this.getValueText());
			WApplication.getInstance().getTheme().applyValidationStyle(this,
					result,
					EnumSet.of(ValidationStyleFlag.ValidationInvalidStyle));
			if (!this.validationToolTip_.equals(result.getMessage())) {
				this.validationToolTip_ = result.getMessage();
				this.flags_.set(BIT_VALIDATION_CHANGED);
				this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
			}
			this.validated_.trigger(result);
			return result.getState();
		} else {
			return WValidator.State.Valid;
		}
	}

	/**
	 * Sets whether the widget is enabled.
	 * <p>
	 * A widget that is disabled cannot receive focus or user interaction.
	 * <p>
	 * This is the opposite of {@link WWebWidget#setDisabled(boolean disabled)
	 * WWebWidget#setDisabled()}.
	 */
	public void setEnabled(boolean enabled) {
		this.setDisabled(!enabled);
	}

	/**
	 * Gives focus.
	 * <p>
	 * Giving focus to an input element only works when JavaScript is enabled.
	 */
	public void setFocus() {
		this.setFocus(true);
	}

	/**
	 * Changes focus.
	 * <p>
	 * When using <code>focus</code> = <code>false</code>, you can undo a
	 * previous {@link WFormWidget#setFocus() setFocus()} call.
	 */
	public void setFocus(boolean focus) {
		this.flags_.set(BIT_GOT_FOCUS, focus);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyIEMobile));
		WApplication app = WApplication.getInstance();
		if (focus) {
			app.setFocus(this.getId(), -1, -1);
		} else {
			if (app.getFocus().equals(this.getId())) {
				app.setFocus("", -1, -1);
			}
		}
	}

	/**
	 * Returns whether this widget has focus.
	 */
	public boolean hasFocus() {
		return WApplication.getInstance().getFocus().equals(this.getId());
	}

	public void setTabIndex(int index) {
		this.tabIndex_ = index;
		this.flags_.set(BIT_TABINDEX_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	public int getTabIndex() {
		return this.tabIndex_;
	}

	/**
	 * Sets the element read-only.
	 * <p>
	 * A read-only form element cannot be edited, but the contents can still be
	 * selected.
	 * <p>
	 * By default, a form element area is not read-only.
	 * <p>
	 * 
	 * @see WFormWidget#setEnabled(boolean enabled)
	 */
	public void setReadOnly(boolean readOnly) {
		this.flags_.set(BIT_READONLY, readOnly);
		this.flags_.set(BIT_READONLY_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	/**
	 * Returns whether the form element is read-only.
	 * <p>
	 * 
	 * @see WFormWidget#setReadOnly(boolean readOnly)
	 */
	public boolean isReadOnly() {
		return this.flags_.get(BIT_READONLY);
	}

	/**
	 * Sets the placeholder text (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated use
	 *             {@link WFormWidget#setPlaceholderText(CharSequence placeholderText)
	 *             setPlaceholderText()} instead
	 */
	public void setEmptyText(CharSequence emptyText) {
		this.setPlaceholderText(emptyText);
	}

	/**
	 * Returns the placeholder text (<b>deprecated</b>).
	 * <p>
	 * 
	 * @deprecated use {@link WFormWidget#getPlaceholderText()
	 *             getPlaceholderText()} instead.
	 */
	public WString getEmptyText() {
		return this.getPlaceholderText();
	}

	/**
	 * Sets the placeholder text.
	 * <p>
	 * This sets the text that is shown when the field is empty.
	 */
	public void setPlaceholderText(CharSequence placeholderText) {
		this.emptyText_ = WString.toWString(placeholderText);
		WApplication app = WApplication.getInstance();
		WEnvironment env = app.getEnvironment();
		if (env.hasAjax()) {
			if (!(this.emptyText_.length() == 0)) {
				if (!this.flags_.get(BIT_JS_OBJECT)) {
					this.defineJavaScript();
				} else {
					this.updateEmptyText();
				}
				if (!(this.removeEmptyText_ != null)) {
					this.removeEmptyText_ = new JSlot(this);
					this.focussed().addListener(this.removeEmptyText_);
					this.blurred().addListener(this.removeEmptyText_);
					this.keyWentDown().addListener(this.removeEmptyText_);
					String jsFunction = "function(obj, event) {jQuery.data("
							+ this.getJsRef() + ", 'obj').applyEmptyText();}";
					this.removeEmptyText_.setJavaScript(jsFunction);
				}
			} else {
				;
				this.removeEmptyText_ = null;
			}
		} else {
			this.setToolTip(placeholderText);
		}
	}

	/**
	 * Returns the placeholder text.
	 * <p>
	 * 
	 * @see WFormWidget#setPlaceholderText(CharSequence placeholderText)
	 */
	public WString getPlaceholderText() {
		return this.emptyText_;
	}

	/**
	 * Signal emitted when the value was changed.
	 */
	public EventSignal changed() {
		return this.voidEventSignal(CHANGE_SIGNAL, true);
	}

	/**
	 * Signal emitted when ??
	 */
	public EventSignal selected() {
		return this.voidEventSignal(SELECT_SIGNAL, true);
	}

	/**
	 * Signal emitted when the widget lost focus.
	 */
	public EventSignal blurred() {
		return this.voidEventSignal(BLUR_SIGNAL, true);
	}

	/**
	 * Signal emitted when the widget recieved focus.
	 */
	public EventSignal focussed() {
		return this.voidEventSignal(FOCUS_SIGNAL, true);
	}

	/**
	 * Signal emitted when the widget is being validated.
	 * <p>
	 * This signal may be useful to react to a changed validation state.
	 * <p>
	 * 
	 * @see WFormWidget#validate()
	 */
	public Signal1<WValidator.Result> validated() {
		return this.validated_;
	}

	public void refresh() {
		if (this.emptyText_.refresh()) {
			this.updateEmptyText();
		}
		super.refresh();
	}

	public void setToolTip(CharSequence text, TextFormat textFormat) {
		super.setToolTip(text, textFormat);
		if (this.validator_ != null && textFormat == TextFormat.PlainText) {
			this.setJavaScriptMember("defaultTT", WString.toWString(text)
					.getJsStringLiteral());
		}
	}

	WLabel label_;
	WValidator validator_;
	JSlot validateJs_;
	JSlot filterInput_;
	JSlot removeEmptyText_;
	WString emptyText_;
	static String CHANGE_SIGNAL = "M_change";

	void applyEmptyText() {
		if (this.isRendered() && !(this.emptyText_.length() == 0)) {
			this.doJavaScript("jQuery.data(" + this.getJsRef()
					+ ", 'obj').applyEmptyText();");
		}
	}

	protected void enableAjax() {
		if (!(this.emptyText_.length() == 0)
				&& this.getToolTip().equals(this.emptyText_)) {
			this.setToolTip("");
			this.setEmptyText(this.emptyText_);
		}
		super.enableAjax();
	}

	private static String SELECT_SIGNAL = "select";
	private static String FOCUS_SIGNAL = "focus";
	private static String BLUR_SIGNAL = "blur";
	private static final int BIT_ENABLED_CHANGED = 0;
	private static final int BIT_GOT_FOCUS = 1;
	private static final int BIT_INITIAL_FOCUS = 2;
	private static final int BIT_READONLY = 3;
	private static final int BIT_READONLY_CHANGED = 4;
	private static final int BIT_TABINDEX_CHANGED = 5;
	private static final int BIT_JS_OBJECT = 6;
	private static final int BIT_VALIDATION_CHANGED = 7;
	BitSet flags_;
	private int tabIndex_;
	private Signal1<WValidator.Result> validated_;
	private WString validationToolTip_;

	private void undoSetFocus() {
	}

	void setLabel(WLabel label) {
		if (this.label_ != null) {
			WLabel l = this.label_;
			this.label_ = null;
			l.setBuddy((WFormWidget) null);
		}
		this.label_ = label;
		if (this.label_ != null) {
			this.label_.setHidden(this.isHidden());
		}
	}

	void validatorChanged() {
		String validateJS = this.validator_.getJavaScriptValidate();
		if (validateJS.length() != 0) {
			this.setJavaScriptMember("wtValidate", validateJS);
			if (!(this.validateJs_ != null)) {
				this.validateJs_ = new JSlot();
				this.validateJs_
						.setJavaScript("function(o){Wt3_3_0.validate(o)}");
				this.keyWentUp().addListener(this.validateJs_);
				this.changed().addListener(this.validateJs_);
				if (this.getDomElementType() != DomElementType.DomElement_SELECT) {
					this.clicked().addListener(this.validateJs_);
				}
			}
		} else {
			;
			this.validateJs_ = null;
		}
		String inputFilter = this.validator_.getInputFilter();
		if (inputFilter.length() != 0) {
			if (!(this.filterInput_ != null)) {
				this.filterInput_ = new JSlot();
				this.keyPressed().addListener(this.filterInput_);
			}
			StringUtils.replace(inputFilter, '/', "\\/");
			this.filterInput_.setJavaScript("function(o,e){Wt3_3_0.filter(o,e,"
					+ jsStringLiteral(inputFilter) + ")}");
		} else {
			;
			this.filterInput_ = null;
		}
		this.validate();
	}

	private void defineJavaScript(boolean force) {
		if (force || !this.flags_.get(BIT_JS_OBJECT)) {
			this.flags_.set(BIT_JS_OBJECT);
			if (!this.isRendered()) {
				return;
			}
			WApplication app = WApplication.getInstance();
			app.loadJavaScript("js/WFormWidget.js", wtjs1());
			this.setJavaScriptMember(" WFormWidget", "new Wt3_3_0.WFormWidget("
					+ app.getJavaScriptClass() + "," + this.getJsRef() + ","
					+ WString.toWString(this.emptyText_).getJsStringLiteral()
					+ ");");
		}
	}

	private final void defineJavaScript() {
		defineJavaScript(false);
	}

	private void updateEmptyText() {
		if (this.isRendered()) {
			this.doJavaScript("jQuery.data(" + this.getJsRef()
					+ ", 'obj').setEmptyText("
					+ WString.toWString(this.emptyText_).getJsStringLiteral()
					+ ");");
		}
	}

	void updateDom(DomElement element, boolean all) {
		WEnvironment env = WApplication.getInstance().getEnvironment();
		boolean onChangeHandledElsewhere = ((this) instanceof WAbstractToggleButton ? (WAbstractToggleButton) (this)
				: null) != null;
		if (!onChangeHandledElsewhere) {
			EventSignal s = this.voidEventSignal(CHANGE_SIGNAL, false);
			if (s != null) {
				this.updateSignalConnection(element, s, "change", all);
			}
		}
		if (this.flags_.get(BIT_ENABLED_CHANGED) || all) {
			if (!all || !this.isEnabled()) {
				element.setProperty(Property.PropertyDisabled,
						this.isEnabled() ? "false" : "true");
			}
			if (!all && this.isEnabled() && env.agentIsIE()) {
			}
			this.flags_.clear(BIT_ENABLED_CHANGED);
		}
		if (this.flags_.get(BIT_READONLY_CHANGED) || all) {
			if (!all || this.isReadOnly()) {
				element.setProperty(Property.PropertyReadOnly, this
						.isReadOnly() ? "true" : "false");
			}
			this.flags_.clear(BIT_READONLY_CHANGED);
		}
		if (this.flags_.get(BIT_TABINDEX_CHANGED) || all) {
			if (!all || this.tabIndex_ != 0) {
				element.setProperty(Property.PropertyTabIndex, String
						.valueOf(this.tabIndex_));
			}
			this.flags_.clear(BIT_TABINDEX_CHANGED);
		}
		if (this.isEnabled()) {
			if (all && this.flags_.get(BIT_GOT_FOCUS)) {
				this.flags_.set(BIT_INITIAL_FOCUS);
			}
			if (this.flags_.get(BIT_GOT_FOCUS) || all
					&& this.flags_.get(BIT_INITIAL_FOCUS)) {
				element.callJavaScript("setTimeout(function() {var f = "
						+ this.getJsRef()
						+ ";if (f) try { f.focus(); } catch (e) { } }, "
						+ (env.agentIsIElt(9) ? "500" : "10") + ");");
				this.flags_.clear(BIT_GOT_FOCUS);
			}
		}
		super.updateDom(element, all);
		if (this.flags_.get(BIT_VALIDATION_CHANGED)) {
			if ((this.validationToolTip_.length() == 0)) {
				element.setAttribute("title", this.getToolTip().toString());
			} else {
				element.setAttribute("title", this.validationToolTip_
						.toString());
			}
		}
	}

	void propagateRenderOk(boolean deep) {
		this.flags_.clear(BIT_ENABLED_CHANGED);
		this.flags_.clear(BIT_TABINDEX_CHANGED);
		this.flags_.clear(BIT_VALIDATION_CHANGED);
		super.propagateRenderOk(deep);
	}

	// protected AbstractEventSignal.LearningListener
	// getStateless(<pointertomember or dependentsizedarray>
	// methodpointertomember or dependentsizedarray>) ;
	protected void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()
				&& this.flags_.get(BIT_JS_OBJECT)) {
			this.defineJavaScript(true);
		}
		super.render(flags);
	}

	protected void propagateSetEnabled(boolean enabled) {
		this.flags_.set(BIT_ENABLED_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
		super.propagateSetEnabled(enabled);
	}

	String getFormName() {
		return this.getId();
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WFormWidget",
				"function(d,a,b){jQuery.data(a,\"obj\",this);var c=d.WT;this.applyEmptyText=function(){if(c.hasFocus(a)){if($(a).hasClass(\"Wt-edit-emptyText\")){if(!c.isIE&&a.oldtype)a.type=a.oldtype;$(a).removeClass(\"Wt-edit-emptyText\");a.value=\"\"}}else if(a.value==\"\"){if(a.type==\"password\")if(c.isIE)return;else{a.oldtype=\"password\";a.type=\"text\"}$(a).addClass(\"Wt-edit-emptyText\");a.value=b}};this.setEmptyText=function(e){b=e;if($(a).hasClass(\"Wt-edit-emptyText\"))a.value= b};this.applyEmptyText()}");
	}
}
