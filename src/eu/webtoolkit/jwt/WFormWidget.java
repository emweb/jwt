/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.BitSet;
import java.util.EnumSet;

/**
 * An abstract widget that corresponds to an HTML form element
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
		this.flags_ = new BitSet();
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
			this.setStyleClass(this.validate() == WValidator.State.Valid ? ""
					: "Wt-invalid");
		} else {
			this.setStyleClass("");
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
	 * Gives focus to this widget.
	 * <p>
	 * Giving focus to an input element only works when JavaScript is enabled.
	 */
	public void setFocus() {
		this.flags_.set(BIT_GOT_FOCUS);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyIEMobile));
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
	private static final int BIT_ENABLED_CHANGED = 0;
	private static final int BIT_GOT_FOCUS = 1;
	private static final int BIT_INITIAL_FOCUS = 2;
	private static final int BIT_READONLY = 3;
	private static final int BIT_READONLY_CHANGED = 4;
	BitSet flags_;

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
					.setJavaScript("function(self, event){var v="
							+ validateJS
							+ ";self.className= v.valid ? '' : 'Wt-invalid';if (v.valid) self.removeAttribute('title');else self.setAttribute('title', v.message);}");
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
					.setJavaScript("function(self,e){var c=String.fromCharCode((typeof e.charCode!=='undefined')?e.charCode:e.keyCode);if(/"
							+ inputFilter
							+ "/.test(c)) return true; else{Wt3_1_1.cancelEvent(e);}}");
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
			element.setProperty(Property.PropertyDisabled,
					this.isEnabled() ? "false" : "true");
			this.flags_.clear(BIT_ENABLED_CHANGED);
		}
		if (this.flags_.get(BIT_READONLY_CHANGED) || all) {
			element.setProperty(Property.PropertyReadOnly,
					this.isReadOnly() ? "true" : "false");
			this.flags_.clear(BIT_READONLY_CHANGED);
		}
		if (this.isEnabled()) {
			if (all && this.flags_.get(BIT_GOT_FOCUS)) {
				this.flags_.set(BIT_INITIAL_FOCUS);
			}
			if (this.flags_.get(BIT_GOT_FOCUS) || all
					&& this.flags_.get(BIT_INITIAL_FOCUS)) {
				element.callMethod("focus()");
				this.flags_.clear(BIT_GOT_FOCUS);
			}
		}
		super.updateDom(element, all);
	}

	void propagateRenderOk(boolean deep) {
		this.flags_.clear(BIT_ENABLED_CHANGED);
		super.propagateRenderOk(deep);
	}

	// protected AbstractEventSignal.LearningListener
	// getStateless(<pointertomember or dependentsizedarray>
	// methodpointertomember or dependentsizedarray>) ;
	protected void propagateSetEnabled(boolean enabled) {
		this.flags_.set(BIT_ENABLED_CHANGED);
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
		super.propagateSetEnabled(enabled);
	}

	String getFormName() {
		return this.getId();
	}

	static String CHANGE_SIGNAL = "M_change";
	private static String SELECT_SIGNAL = "select";
	private static String FOCUS_SIGNAL = "focus";
	private static String BLUR_SIGNAL = "blur";
}
