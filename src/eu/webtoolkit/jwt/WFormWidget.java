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
	public void setHidden(boolean hidden) {
		if (this.label_ != null) {
			this.label_.setHidden(hidden);
		}
		super.setHidden(hidden);
	}

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
		if (this.validator_ != null) {
			this.validator_.removeFormWidget(this);
		}
		this.validator_ = validator;
		if (this.validator_ != null) {
			this.validator_.addFormWidget(this);
			this.validatorChanged();
			this.validate();
		} else {
			this.removeStyleClass("Wt-invalid", true);
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
	 */
	public WValidator.State validate() {
		return WValidator.State.Valid;
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
	 * Sets the empty text to be shown when the field is empty.
	 * <p>
	 * 
	 * @see WFormWidget#getEmptyText()
	 */
	public void setEmptyText(CharSequence emptyText) {
		this.emptyText_ = WString.toWString(emptyText);
		WApplication app = WApplication.getInstance();
		WEnvironment env = app.getEnvironment();
		if (env.hasAjax()) {
			if (!(this.emptyText_.length() == 0)) {
				String THIS_JS = "js/WFormWidget.js";
				if (!app.isJavaScriptLoaded(THIS_JS)) {
					app.doJavaScript(wtjs1(app), false);
					app.setJavaScriptLoaded(THIS_JS);
				}
				if (!(this.removeEmptyText_ != null)) {
					this.removeEmptyText_ = new JSlot(this);
					this.focussed().addListener(this.removeEmptyText_);
					this.blurred().addListener(this.removeEmptyText_);
					this.keyWentDown().addListener(this.removeEmptyText_);
					String jsFunction = "function(obj, event) {jQuery.data("
							+ this.getJsRef() + ", 'obj').updateEmptyText();}";
					this.removeEmptyText_.setJavaScript(jsFunction);
				}
			} else {
				;
			}
		} else {
			this.setToolTip(emptyText);
		}
	}

	/**
	 * Returns the empty text to be shown when the field is empty.
	 * <p>
	 * 
	 * @see WFormWidget#setEmptyText(CharSequence emptyText)
	 */
	public WString getEmptyText() {
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

	WLabel label_;
	WValidator validator_;
	JSlot validateJs_;
	JSlot filterInput_;
	JSlot removeEmptyText_;
	WString emptyText_;

	void updateEmptyText() {
		if (!(this.emptyText_.length() == 0) && this.isRendered()) {
			WApplication.getInstance().doJavaScript(
					"jQuery.data(" + this.getJsRef()
							+ ", 'obj').updateEmptyText();");
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

	private static final int BIT_ENABLED_CHANGED = 0;
	private static final int BIT_GOT_FOCUS = 1;
	private static final int BIT_INITIAL_FOCUS = 2;
	private static final int BIT_READONLY = 3;
	private static final int BIT_READONLY_CHANGED = 4;
	private static final int BIT_TABINDEX_CHANGED = 5;
	BitSet flags_;
	private int tabIndex_;

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
		String validateJS = this.validator_.javaScriptValidate(this.getJsRef());
		if (validateJS.length() != 0) {
			if (!(this.validateJs_ != null)) {
				this.validateJs_ = new JSlot(this);
				this.keyWentUp().addListener(this.validateJs_);
				this.changed().addListener(this.validateJs_);
				this.clicked().addListener(this.validateJs_);
			}
			this.validateJs_
					.setJavaScript("function(self, event) {var v="
							+ validateJS
							+ ";if (v.valid) {self.removeAttribute('title');$(self).removeClass('Wt-invalid');} else {self.setAttribute('title', v.message);$(self).addClass('Wt-invalid');}}");
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
			this.filterInput_
					.setJavaScript("function(self,e){\nvar c=\nString.fromCharCode((typeof e.charCode!=='undefined') ?e.charCode : e.keyCode);\nif(/"
							+ inputFilter
							+ "/.test(c))\nreturn true;\nelse\nWt3_1_7a.cancelEvent(e);\n}");
		} else {
			;
			this.filterInput_ = null;
		}
	}

	void updateDom(DomElement element, boolean all) {
		WEnvironment env = WApplication.getInstance().getEnvironment();
		if (!env.agentIsIE()
				|| !(((this) instanceof WAbstractToggleButton ? (WAbstractToggleButton) (this)
						: null) != null)) {
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
						+ this.getJsRef() + ";if (f) f.focus(); }, "
						+ (env.agentIsIElt(9) ? "500" : "10") + ");");
				this.flags_.clear(BIT_GOT_FOCUS);
			}
		}
		super.updateDom(element, all);
	}

	void propagateRenderOk(boolean deep) {
		this.flags_.clear(BIT_ENABLED_CHANGED);
		this.flags_.clear(BIT_TABINDEX_CHANGED);
		super.propagateRenderOk(deep);
	}

	// protected AbstractEventSignal.LearningListener
	// getStateless(<pointertomember or dependentsizedarray>
	// methodpointertomember or dependentsizedarray>) ;
	void render(EnumSet<RenderFlag> flags) {
		if (!EnumUtils.mask(flags, RenderFlag.RenderFull).isEmpty()
				&& !(this.emptyText_.length() == 0)) {
			WApplication app = WApplication.getInstance();
			WEnvironment env = app.getEnvironment();
			if (env.hasAjax()) {
				app.doJavaScript("new Wt3_1_7a.WFormWidget("
						+ app.getJavaScriptClass() + "," + this.getJsRef()
						+ "," + "'" + this.emptyText_.toString() + "');");
			}
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

	static String wtjs1(WApplication app) {
		return "Wt3_1_7a.WFormWidget = function(c,a,d){jQuery.data(a,\"obj\",this);var b=c.WT;this.updateEmptyText=function(){if(b.hasFocus(a)){if($(a).hasClass(\"Wt-edit-emptyText\")){if(!b.isIE&&a.oldtype)a.type=a.oldtype;$(a).removeClass(\"Wt-edit-emptyText\");a.value=\"\"}}else if(a.value==\"\"){if(a.type==\"password\")if(b.isIE)return;else{a.oldtype=\"password\";a.type=\"text\"}$(a).addClass(\"Wt-edit-emptyText\");a.value=d}};this.updateEmptyText()};";
	}

	static String CHANGE_SIGNAL = "M_change";
	private static String SELECT_SIGNAL = "select";
	private static String FOCUS_SIGNAL = "focus";
	private static String BLUR_SIGNAL = "blur";
}
