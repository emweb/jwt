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
 * {@link WFormWidget#changed()} signal, or connect a slot to the
 * {@link WAbstractToggleButton#checked()} or
 * {@link WAbstractToggleButton#unChecked()} signals.
 * <p>
 * The current state (checked or unchecked) may be inspected using the
 * {@link WAbstractToggleButton#isChecked()} method.
 */
public class WAbstractToggleButton extends WFormWidget {
	/**
	 * Create an unchecked toggle button without label.
	 */
	protected WAbstractToggleButton(WContainerWidget parent) {
		super(parent);
		this.state_ = CheckState.Unchecked;
		this.stateChanged_ = false;
	}

	/**
	 * Create an unchecked toggle button without label.
	 * <p>
	 * Calls {@link #WAbstractToggleButton(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	protected WAbstractToggleButton() {
		this((WContainerWidget) null);
	}

	/**
	 * Create an unchecked toggle button with given text label.
	 */
	protected WAbstractToggleButton(CharSequence text, WContainerWidget parent) {
		super(parent);
		this.state_ = CheckState.Unchecked;
		this.stateChanged_ = false;
		WLabel label = new WLabel(text);
		label.setBuddy(this);
	}

	/**
	 * Create an unchecked toggle button with given text label.
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
		WLabel l = this.getLabel();
		if (l != null && !(l.getParent() != null)) {
			if (l != null)
				l.remove();
		}
		super.remove();
	}

	/**
	 * Change the text of the label.
	 */
	public void setText(CharSequence text) {
		WLabel l = this.getLabel();
		if (!(l != null)) {
			l = new WLabel(text);
			l.setBuddy(this);
		}
		l.setText(text);
	}

	/**
	 * Get the text of the label.
	 */
	public WString getText() {
		if (this.getLabel() != null) {
			return this.getLabel().getText();
		} else {
			return new WString();
		}
	}

	/**
	 * Returns the state of the button.
	 */
	public boolean isChecked() {
		return this.state_ == CheckState.Checked;
	}

	/**
	 * Change the state of the button.
	 * <p>
	 * Does not emit one of the {@link WAbstractToggleButton#checked()} or
	 * {@link WAbstractToggleButton#unChecked()} signals.
	 * <p>
	 * 
	 * @see WAbstractToggleButton#setChecked()
	 * @see WAbstractToggleButton#setUnChecked()
	 */
	public void setChecked(boolean how) {
		this.setCheckState(how ? CheckState.Checked : CheckState.Unchecked);
	}

	/**
	 * Set the button checked.
	 * <p>
	 * Does not emit the {@link WAbstractToggleButton#checked()} signal.
	 * <p>
	 * 
	 * @see WAbstractToggleButton#setChecked(boolean how)
	 */
	public void setChecked() {
		this.prevState_ = this.state_;
		this.setChecked(true);
	}

	/**
	 * Set the button unChecked.
	 * <p>
	 * Does not emit the {@link WAbstractToggleButton#unChecked()} signal.
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
	 */
	public EventSignal checked() {
		return this.voidEventSignal(CHECKED_SIGNAL, true);
	}

	/**
	 * Signal emitted when the button gets unChecked.
	 */
	public EventSignal unChecked() {
		return this.voidEventSignal(UNCHECKED_SIGNAL, true);
	}

	protected CheckState state_;

	protected DomElement createDomElement(WApplication app) {
		DomElement result = DomElement.createNew(this.getDomElementType());
		this.setId(result, app);
		DomElement input = result;
		if (result.getType() == DomElementType.DomElement_SPAN) {
			input = DomElement.createNew(DomElementType.DomElement_INPUT);
			input.setName("in" + this.getId());
			if (this.isUseImageWorkaround()) {
				DomElement img = DomElement
						.createNew(DomElementType.DomElement_IMG);
				img.setId("im" + this.getId());
				String src = WApplication.getResourcesUrl();
				WEnvironment env = app.getEnvironment();
				if (env.getUserAgent().indexOf("Mac OS X") != -1) {
					src += "indeterminate-macosx.png";
				} else {
					if (env.agentIsOpera()) {
						src += "indeterminate-opera.png";
					} else {
						if (env.getUserAgent().indexOf("Windows") != -1) {
							src += "indeterminate-windows.png";
						} else {
							src += "indeterminate-linux.png";
						}
					}
				}
				img.setProperty(Property.PropertySrc, fixRelativeUrl(src));
				img.setAttribute("class", "Wt-indeterminate");
				EventSignal imgClick = this.voidEventSignal(
						UNDETERMINATE_CLICK_SIGNAL, true);
				img.setEventSignal("click", imgClick);
				imgClick.updateOk();
				if (this.state_ == CheckState.PartiallyChecked) {
					input.setProperty(Property.PropertyStyleDisplay, "none");
				} else {
					img.setProperty(Property.PropertyStyleDisplay, "none");
				}
				result.addChild(img);
			}
		}
		this.updateDom(input, true);
		if (result != input) {
			result.addChild(input);
			WLabel l = this.getLabel();
			if (l != null && !(l.getParent() != null)) {
				result.addChild(((WWebWidget) l).createDomElement(app));
			}
		}
		return result;
	}

	protected void getDomChanges(List<DomElement> result, WApplication app) {
		DomElementType type = this.getDomElementType();
		if (type == DomElementType.DomElement_SPAN) {
			DomElement input = DomElement.getForUpdate("in" + this.getId(),
					DomElementType.DomElement_INPUT);
			if (this.isUseImageWorkaround()) {
				EventSignal imgClick = this.voidEventSignal(
						UNDETERMINATE_CLICK_SIGNAL, true);
				if (this.stateChanged_ || imgClick.needUpdate()) {
					DomElement img = DomElement.getForUpdate("im"
							+ this.getId(), DomElementType.DomElement_IMG);
					if (this.stateChanged_) {
						img
								.setProperty(
										Property.PropertyStyleDisplay,
										this.state_ == CheckState.PartiallyChecked ? "inline"
												: "none");
						input
								.setProperty(
										Property.PropertyStyleDisplay,
										this.state_ == CheckState.PartiallyChecked ? "none"
												: "inline");
					}
					if (imgClick.needUpdate()) {
						img.setEventSignal("click", imgClick);
						imgClick.updateOk();
					}
					result.add(img);
				}
			}
			this.updateDom(input, false);
			result.add(input);
			WLabel l = this.getLabel();
			if (l != null && !(l.getParent() != null)) {
				((WWebWidget) l).getDomChanges(result, app);
			}
		} else {
			DomElement e = DomElement.getForUpdate(this, this
					.getDomElementType());
			this.updateDom(e, false);
			result.add(e);
		}
	}

	protected void updateDom(DomElement element, boolean all) {
		if (this.stateChanged_ || all) {
			element.setProperty(Property.PropertyChecked,
					this.state_ == CheckState.Checked ? "true" : "false");
			if (this.getDomElementType() == DomElementType.DomElement_INPUT) {
				element.setProperty(Property.PropertyIndeterminate,
						this.state_ == CheckState.PartiallyChecked ? "true"
								: "false");
			}
			this.stateChanged_ = false;
		}
		WEnvironment env = WApplication.getInstance().getEnvironment();
		EventSignal check = this.voidEventSignal(CHECKED_SIGNAL, false);
		EventSignal uncheck = this.voidEventSignal(UNCHECKED_SIGNAL, false);
		EventSignal change = this.voidEventSignal(CHANGE_SIGNAL, false);
		EventSignal1<WMouseEvent> click = this.mouseEventSignal(CLICK_SIGNAL,
				false);
		boolean needUpdateClickedSignal = click != null && click.needUpdate()
				|| env.agentIsIE() && change != null && change.needUpdate()
				|| check != null && check.needUpdate() || uncheck != null
				&& uncheck.needUpdate();
		super.updateDom(element, all);
		if (needUpdateClickedSignal || all) {
			String dom = "Wt2_99_2.getElement('" + element.getId() + "')";
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
				if (env.agentIsIE() && change.isConnected()) {
					actions.add(new DomElement.EventAction("", change
							.getJavaScript(), change.encodeCmd(), change
							.isExposedSignal()));
				}
				change.updateOk();
			}
			if (click != null) {
				if (click.isConnected()) {
					actions.add(new DomElement.EventAction("", click
							.getJavaScript(), click.encodeCmd(), click
							.isExposedSignal()));
				}
				click.updateOk();
			}
			if (!(all && actions.isEmpty())) {
				element.setEvent("click", actions);
			}
		}
	}

	protected void getFormObjects(Map<String, WObject> formObjects) {
		formObjects.put(this.getFormName(), this);
	}

	protected void setFormData(WObject.FormData formData) {
		if (this.stateChanged_) {
			return;
		}
		if (!formData.values.isEmpty()) {
			if (formData.values.get(0).equals("indeterminate")) {
				this.state_ = CheckState.PartiallyChecked;
			} else {
				this.state_ = !formData.values.get(0).equals("0") ? CheckState.Checked
						: CheckState.Unchecked;
			}
		} else {
			if (this.isEnabled() && this.isVisible()) {
				this.state_ = CheckState.Unchecked;
			}
		}
	}

	protected void propagateRenderOk(boolean deep) {
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

	protected DomElementType getDomElementType() {
		if (this.isUseImageWorkaround()) {
			return DomElementType.DomElement_SPAN;
		} else {
			WLabel l = this.getLabel();
			if (l != null && !(l.getParent() != null)) {
				return DomElementType.DomElement_SPAN;
			} else {
				return DomElementType.DomElement_INPUT;
			}
		}
	}

	protected boolean isUseImageWorkaround() {
		return false;
	}

	protected String getFormName() {
		if (this.getDomElementType() == DomElementType.DomElement_SPAN
				&& !this.isUseImageWorkaround()) {
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
	static String UNDETERMINATE_CLICK_SIGNAL = "M_click";
}
