package eu.webtoolkit.jwt;


/**
 * A user control that represents a check box
 * <p>
 * 
 * By default, a checkbox can have two states: {@link CheckState#Checked} or
 * {@link CheckState#Unchecked}, which can be inspected using
 * {@link WAbstractToggleButton#isChecked()}, and set using
 * {@link WAbstractToggleButton#setChecked()}.
 * <p>
 * A checkbox may also provide a third state,
 * {@link CheckState#PartiallyChecked}, which is useful to indicate that it is
 * neither checked or unchecked. Wt will use native browser support for this
 * HTML5 extension when available (Safari and MS IE), and use an image-based
 * workaround otherwise. You may enable support for the third state using
 * {@link WCheckBox#setTristate(boolean tristate)}, and use
 * {@link WCheckBox#setCheckState(CheckState state)} and
 * {@link WCheckBox#getCheckState()} to read all three states.
 * <p>
 * A label is added as a sibling of the checkbox to the same parent.
 * <p>
 * Usage example:
 * <p>
 * <code>
 WGroupBox box = new WGroupBox(&quot;In-flight options&quot;); <br> 
		  <br> 
 WCheckBox w1 = new WCheckBox(&quot;Vegetarian diet&quot;, box); <br> 
 box.addWidget(new WBreak()); <br> 
 WCheckBox w2 = new WCheckBox(&quot;WIFI access&quot;, box); <br> 
 box.addWidget(new WBreak()); <br> 
 WCheckBox w3 = new WCheckBox(&quot;AC plug&quot;, box); <br> 
		  <br> 
 w1.setChecked(false); <br> 
 w2.setChecked(true); <br> 
 w3.setChecked(true);
</code>
 * <p>
 * The widget corresponds to the HTML
 * <code>&lt;input type=&quot;checkbox&quot;&gt;</code> tag.
 * <p>
 * WCheckBox is an {@link WWidget#setInline(boolean inlined) inline} widget.
 * <p>
 * 
 * @see WAbstractToggleButton
 */
public class WCheckBox extends WAbstractToggleButton {
	/**
	 * Create a checkbox with empty label.
	 */
	public WCheckBox(WContainerWidget parent) {
		super(parent);
		this.triState_ = false;
		this.safariWorkaround_ = false;
		this.setFormObject(true);
	}

	/**
	 * Create a checkbox with empty label.
	 * <p>
	 * Calls {@link #WCheckBox(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WCheckBox() {
		this((WContainerWidget) null);
	}

	/**
	 * Create a checkbox with given label.
	 */
	public WCheckBox(CharSequence text, WContainerWidget parent) {
		super(text, parent);
		this.triState_ = false;
		this.safariWorkaround_ = false;
		this.setFormObject(true);
	}

	/**
	 * Create a checkbox with given label.
	 * <p>
	 * Calls {@link #WCheckBox(CharSequence text, WContainerWidget parent)
	 * this(text, (WContainerWidget)null)}
	 */
	public WCheckBox(CharSequence text) {
		this(text, (WContainerWidget) null);
	}

	/**
	 * Make a tristate checkbox.
	 * <p>
	 * <p>
	 * <i><b>Note:</b>You should enable tristate functionality right after
	 * construction and this cannot be modified later. </i>
	 * </p>
	 */
	public void setTristate(boolean tristate) {
		this.triState_ = tristate;
		if (this.triState_) {
			if (this.needTristateImageWorkaround()) {
				EventSignal imgClick = this.voidEventSignal(
						UNDETERMINATE_CLICK_SIGNAL, false);
				if (!(imgClick != null)) {
					imgClick = this.voidEventSignal(UNDETERMINATE_CLICK_SIGNAL,
							true);
					imgClick.addListener(this, new Signal.Listener() {
						public void trigger() {
							WCheckBox.this.setUnChecked();
						}
					});
					imgClick.addListener(this, new Signal.Listener() {
						public void trigger() {
							WCheckBox.this.gotUndeterminateClick();
						}
					});
				}
			} else {
				if (WApplication.getInstance().getEnvironment().agentIsSafari()
						&& !this.safariWorkaround_) {
					this.clicked().addListener(safariWorkaroundJS);
					this.safariWorkaround_ = true;
				}
			}
		}
	}

	/**
	 * Make a tristate checkbox.
	 * <p>
	 * Calls {@link #setTristate(boolean tristate) setTristate(true)}
	 */
	public final void setTristate() {
		setTristate(true);
	}

	/**
	 * Returns whether the checkbox is tristate.
	 * <p>
	 * 
	 * @see WCheckBox#setTristate(boolean tristate)
	 */
	public boolean isTristate() {
		return this.triState_;
	}

	/**
	 * Set the check state.
	 * <p>
	 * Unless it is a tri-state checkbox, only {@link CheckState#Checked} and
	 * {@link CheckState#Unchecked} are valid states.
	 */
	public void setCheckState(CheckState state) {
		super.setCheckState(state);
	}

	/**
	 * Returns the check state.
	 * <p>
	 * 
	 * @see WCheckBox#setCheckState(CheckState state)
	 * @see WAbstractToggleButton#isChecked()
	 */
	public CheckState getCheckState() {
		return this.state_;
	}

	protected void updateDom(DomElement element, boolean all) {
		if (all) {
			element.setAttribute("type", "checkbox");
		}
		super.updateDom(element, all);
	}

	protected DomElementType getDomElementType() {
		if (this.triState_) {
			if (this.needTristateImageWorkaround()) {
				return DomElementType.DomElement_SPAN;
			} else {
				return DomElementType.DomElement_INPUT;
			}
		} else {
			return DomElementType.DomElement_INPUT;
		}
	}

	private boolean triState_;
	private boolean safariWorkaround_;

	private boolean needTristateImageWorkaround() {
		WApplication app = WApplication.getInstance();
		boolean supportIndeterminate = app.getEnvironment().hasJavaScript()
				&& (app.getEnvironment().agentIsIE()
						|| app.getEnvironment().agentIsSafari() || app
						.getEnvironment().agentIsGecko()
						&& app.getEnvironment().getAgent().getValue() >= WEnvironment.UserAgent.Firefox3_2
								.getValue());
		return !supportIndeterminate;
	}

	private void gotUndeterminateClick() {
		this.setUnChecked();
		this.unChecked().trigger();
		this.changed().trigger();
	}

	static JSlot safariWorkaroundJS = new JSlot(
			"function(obj, e) { obj.onchange(); };");
}
