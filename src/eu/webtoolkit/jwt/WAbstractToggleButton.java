/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * An abstract base class for radio buttons and check boxes
 * <p>
 * 
 * A toggle button provides a button with a boolean state (checked or
 * unchecked), and a text label.
 * <p>
 * To act on a change of the state, either connect a slot to the
 * {@link WFormWidget#changed() WFormWidget#changed()} signal, or connect a slot
 * to the {@link WAbstractToggleButton#checked() checked()} or
 * {@link WAbstractToggleButton#unChecked() unChecked()} signals.
 * <p>
 * The current state (checked or unchecked) may be inspected using the
 * {@link WAbstractToggleButton#isChecked() isChecked()} method.
 */
public class WAbstractToggleButton extends WFormWidget {
	/**
	 * Creates an unchecked toggle button without label.
	 */
	protected WAbstractToggleButton(WContainerWidget parent) {
		super(parent);
		this.state_ = CheckState.Unchecked;
		this.stateChanged_ = false;
	}

	/**
	 * Creates an unchecked toggle button without label.
	 * <p>
	 * Calls {@link #WAbstractToggleButton(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	protected WAbstractToggleButton() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates an unchecked toggle button with given text label.
	 * <p>
	 * The text label is rendered to the right side of the button.
	 */
	protected WAbstractToggleButton(CharSequence text, WContainerWidget parent) {
		super(parent);
		this.state_ = CheckState.Unchecked;
		this.stateChanged_ = false;
		WLabel label = new WLabel(text);
		label.setBuddy(this);
		this.addChild(label);
	}

	/**
	 * Creates an unchecked toggle button with given text label.
	 * <p>
	 * Calls
	 * {@link #WAbstractToggleButton(CharSequence text, WContainerWidget parent)
	 * this(text, (WContainerWidget)null)}
	 */
	protected WAbstractToggleButton(CharSequence text) {
		this(text, (WContainerWidget) null);
	}

	/**
	 * Destructor.
	 */
	public void remove() {
		super.remove();
	}

	/**
	 * Sets the label text.
	 * <p>
	 * The label is rendered to the right fo the button.
	 */
	public void setText(CharSequence text) {
		WLabel l = this.getLabel();
		if (!(l != null)) {
			l = new WLabel(text);
			l.setBuddy(this);
			this.addChild(l);
		}
		l.setText(text);
	}

	/**
	 * Returns the label text.
	 * <p>
	 * 
	 * @see WAbstractToggleButton#setText(CharSequence text)
	 */
	public WString getText() {
		if (this.getLabel() != null) {
			return this.getLabel().getText();
		} else {
			return new WString();
		}
	}

	/**
	 * Returns the button state.
	 * <p>
	 * 
	 * @see WAbstractToggleButton#setChecked()
	 */
	public boolean isChecked() {
		return this.state_ == CheckState.Checked;
	}

	/**
	 * Sets the button state.
	 * <p>
	 * This method does not emit one of the
	 * {@link WAbstractToggleButton#checked() checked()} or
	 * {@link WAbstractToggleButton#unChecked() unChecked()} signals.
	 * <p>
	 * 
	 * @see WAbstractToggleButton#setChecked()
	 * @see WAbstractToggleButton#setUnChecked()
	 */
	public void setChecked(boolean how) {
		this.setCheckState(how ? CheckState.Checked : CheckState.Unchecked);
	}

	/**
	 * Checks the button.
	 * <p>
	 * Does not emit the {@link WAbstractToggleButton#checked() checked()}
	 * signal.
	 * <p>
	 * 
	 * @see WAbstractToggleButton#setChecked(boolean how)
	 */
	public void setChecked() {
		this.prevState_ = this.state_;
		this.setChecked(true);
	}

	/**
	 * Unchecks the button.
	 * <p>
	 * Does not emit the {@link WAbstractToggleButton#unChecked() unChecked()}
	 * signal.
	 * <p>
	 * 
	 * @see WAbstractToggleButton#setChecked(boolean how)
	 */
	public void setUnChecked() {
		this.prevState_ = this.state_;
		this.setChecked(false);
	}

	/**
	 * Signal emitted when the button gets checked.
	 * <p>
	 * This signal is emitted when the user checks the button.
	 * <p>
	 * You can use the {@link WFormWidget#changed() WFormWidget#changed()}
	 * signal to react to any change of the button state.
	 */
	public EventSignal checked() {
		return this.voidEventSignal(CHECKED_SIGNAL, true);
	}

	/**
	 * Signal emitted when the button gets unChecked.
	 * <p>
	 * This signal is emitted when the user unchecks the button.
	 * <p>
	 * You can use the {@link WFormWidget#changed() WFormWidget#changed()}
	 * signal to react to any change of the button state.
	 */
	public EventSignal unChecked() {
		return this.voidEventSignal(UNCHECKED_SIGNAL, true);
	}

	CheckState state_;

	DomElement createDomElement(WApplication app) {
		DomElement result = DomElement.createNew(this.getDomElementType());
		this.setId(result, app);
		DomElement input = result;
		if (result.getType() == DomElementType.DomElement_SPAN) {
			input = DomElement.createNew(DomElementType.DomElement_INPUT);
			input.setName("in" + this.getId());
		}
		this.updateDomElements(result, input, true);
		if (result != input) {
			result.addChild(input);
			WLabel l = this.getLabel();
			if (l != null && l.getParent() == this) {
				result.addChild(((WWebWidget) l).createDomElement(app));
			}
		}
		return result;
	}

	void getDomChanges(List<DomElement> result, WApplication app) {
		DomElement element = DomElement.getForUpdate(this, this
				.getDomElementType());
		if (element.getType() == DomElementType.DomElement_SPAN) {
			DomElement input = DomElement.getForUpdate("in" + this.getId(),
					DomElementType.DomElement_INPUT);
			this.updateDomElements(element, input, false);
			result.add(element);
			result.add(input);
		} else {
			this.updateDomElements(element, element, false);
			result.add(element);
		}
	}

	protected void updateDomElements(DomElement element, DomElement input,
			boolean all) {
		WEnvironment env = WApplication.getInstance().getEnvironment();
		EventSignal check = this.voidEventSignal(CHECKED_SIGNAL, false);
		EventSignal uncheck = this.voidEventSignal(UNCHECKED_SIGNAL, false);
		EventSignal change = this.voidEventSignal(CHANGE_SIGNAL, false);
		EventSignal1<WMouseEvent> click = this.mouseEventSignal(CLICK_SIGNAL,
				false);
		boolean needUpdateClickedSignal = click != null
				&& click.needsUpdate(all) || env.agentIsIE() && change != null
				&& change.needsUpdate(all) || check != null
				&& check.needsUpdate(all) || uncheck != null
				&& uncheck.needsUpdate(all);
		this.updateDom(input, all);
		if (element != input) {
			element.setProperties(input.getProperties());
			input.clearProperties();
			String v = element.getProperty(Property.PropertyDisabled);
			if (v.length() != 0) {
				input.setProperty(Property.PropertyDisabled, v);
				element.removeProperty(Property.PropertyDisabled);
			}
			v = element.getProperty(Property.PropertyReadOnly);
			if (v.length() != 0) {
				input.setProperty(Property.PropertyReadOnly, v);
				element.removeProperty(Property.PropertyReadOnly);
			}
		}
		if (this.stateChanged_ || all) {
			input.setProperty(Property.PropertyChecked,
					this.state_ == CheckState.Unchecked ? "false" : "true");
			if (this.supportsIndeterminate(env)) {
				input.setProperty(Property.PropertyIndeterminate,
						this.state_ == CheckState.PartiallyChecked ? "true"
								: "false");
			} else {
				input
						.setProperty(
								Property.PropertyStyleOpacity,
								this.state_ == CheckState.PartiallyChecked ? "0.5"
										: "");
			}
			this.stateChanged_ = false;
		}
		if (needUpdateClickedSignal || all) {
			String dom = "o";
			List<DomElement.EventAction> actions = new ArrayList<DomElement.EventAction>();
			if (check != null) {
				if (check.isConnected()) {
					actions.add(new DomElement.EventAction(dom + ".checked",
							check.getJavaScript(), check.encodeCmd(), check
									.isExposedSignal()));
				}
				check.updateOk();
			}
			if (uncheck != null) {
				if (uncheck.isConnected()) {
					actions.add(new DomElement.EventAction("!" + dom
							+ ".checked", uncheck.getJavaScript(), uncheck
							.encodeCmd(), uncheck.isExposedSignal()));
				}
				uncheck.updateOk();
			}
			if (change != null) {
				if (env.agentIsIE() && change.needsUpdate(all)) {
					actions.add(new DomElement.EventAction("", change
							.getJavaScript(), change.encodeCmd(), change
							.isExposedSignal()));
				}
				change.updateOk();
			}
			if (click != null) {
				if (click.needsUpdate(all)) {
					actions.add(new DomElement.EventAction("", click
							.getJavaScript(), click.encodeCmd(), click
							.isExposedSignal()));
				}
				click.updateOk();
			}
			if (!(all && actions.isEmpty())) {
				input.setEvent(CLICK_SIGNAL, actions);
			}
		}
	}

	void getFormObjects(Map<String, WObject> formObjects) {
		formObjects.put(this.getFormName(), this);
	}

	void setFormData(WObject.FormData formData) {
		if (this.stateChanged_) {
			return;
		}
		if (!(formData.values.length == 0)) {
			if (formData.values[0].equals("i")) {
				this.state_ = CheckState.PartiallyChecked;
			} else {
				this.state_ = !formData.values[0].equals("0") ? CheckState.Checked
						: CheckState.Unchecked;
			}
		} else {
			if (this.isEnabled() && this.isVisible()) {
				this.state_ = CheckState.Unchecked;
			}
		}
	}

	void propagateRenderOk(boolean deep) {
		this.stateChanged_ = false;
		EventSignal check = this.voidEventSignal(CHECKED_SIGNAL, false);
		if (check != null) {
			check.updateOk();
		}
		EventSignal uncheck = this.voidEventSignal(UNCHECKED_SIGNAL, false);
		if (uncheck != null) {
			uncheck.updateOk();
		}
		super.propagateRenderOk(deep);
	}

	DomElementType getDomElementType() {
		WLabel l = this.getLabel();
		if (l != null && l.getParent() == this) {
			return DomElementType.DomElement_SPAN;
		} else {
			return DomElementType.DomElement_INPUT;
		}
	}

	protected boolean supportsIndeterminate(WEnvironment env) {
		return env.hasJavaScript()
				&& (env.agentIsIE() || env.agentIsSafari() || env
						.agentIsGecko()
						&& env.getAgent().getValue() >= WEnvironment.UserAgent.Firefox3_6
								.getValue());
	}

	String getFormName() {
		if (this.getDomElementType() == DomElementType.DomElement_SPAN) {
			return "in" + this.getId();
		} else {
			return super.getFormName();
		}
	}

	// protected AbstractEventSignal.LearningListener
	// getStateless(<pointertomember or dependentsizedarray>
	// methodpointertomember or dependentsizedarray>) ;
	boolean stateChanged_;
	private CheckState prevState_;

	private void undoSetChecked() {
		this.setCheckState(this.prevState_);
	}

	private void undoSetUnChecked() {
		this.undoSetChecked();
	}

	void setCheckState(CheckState state) {
		if (canOptimizeUpdates() && state == this.state_) {
			return;
		}
		this.state_ = state;
		this.stateChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyIEMobile));
	}

	private static String CHECKED_SIGNAL = "M_checked";
	private static String UNCHECKED_SIGNAL = "M_unchecked";
}
