package eu.webtoolkit.jwt;


/**
 * A widget that provides in-place-editable text
 * <p>
 * 
 * The WInPlaceEdit provides a text that may be edited in place by the user by
 * clicking on it. When clicked, the text turns into a line edit with a save and
 * cancel button.
 * <p>
 * When the user saves the edit, the {@link WInPlaceEdit#valueChanged()} signal
 * is emitted.
 * <p>
 * Usage example:
 * <p>
 * <code>
 WContainerWidget w = new WContainerWidget(); <br> 
 new WText(&quot;Name: &quot;, w); <br> 
 WInPlaceEdit edit = new WInPlaceEdit(&quot;Bob Smith&quot;, w); <br> 
 edit.setStyleClass(&quot;inplace&quot;);
</code>
 * <p>
 * CSS stylesheet: <code>
 .inplace span:hover { <br> 
    background-color: gray; <br> 
 }
</code>
 * <p>
 * This code will produce an edit that looks like: <div align="center"> <img
 * src="/WInPlaceEdit-1.png" alt="WInPlaceEdit text mode">
 * <p>
 * <strong>WInPlaceEdit text mode</strong>
 * </p>
 * </div> When the text is clicked, the edit will expand to become: <div
 * align="center"> <img src="/WInPlaceEdit-2.png" alt="WInPlaceEdit edit mode">
 * <p>
 * <strong>WInPlaceEdit edit mode</strong>
 * </p>
 * </div>
 */
public class WInPlaceEdit extends WCompositeWidget {
	/**
	 * Create an in-place edit with the given text.
	 */
	public WInPlaceEdit(CharSequence text, WContainerWidget parent) {
		super(parent);
		this.valueChanged_ = new Signal1<WString>(this);
		this.setImplementation(this.impl_ = new WContainerWidget());
		this.setInline(true);
		this.text_ = new WText(text, TextFormat.PlainText, this.impl_);
		this.text_.getDecorationStyle().setCursor(Cursor.ArrowCursor);
		this.edit_ = new WLineEdit(text.toString(), this.impl_);
		this.edit_.setTextSize(20);
		this.save_ = new WPushButton("Save", this.impl_);
		this.cancel_ = new WPushButton("Cancel", this.impl_);
		this.edit_.hide();
		this.save_.hide();
		this.cancel_.hide();
		this.text_.clicked().addListener(this.text_,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WInPlaceEdit.this.text_.hide();
					}
				});
		this.text_.clicked().addListener(this.edit_,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WInPlaceEdit.this.edit_.show();
					}
				});
		this.text_.clicked().addListener(this.save_,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WInPlaceEdit.this.save_.show();
					}
				});
		this.text_.clicked().addListener(this.cancel_,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WInPlaceEdit.this.cancel_.show();
					}
				});
		this.text_.clicked().addListener(this.edit_,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WInPlaceEdit.this.edit_.setFocus();
					}
				});
		this.edit_.enterPressed().addListener(this.save_,
				new Signal.Listener() {
					public void trigger() {
						WInPlaceEdit.this.save_.hide();
					}
				});
		this.edit_.enterPressed().addListener(this.cancel_,
				new Signal.Listener() {
					public void trigger() {
						WInPlaceEdit.this.cancel_.hide();
					}
				});
		this.edit_.enterPressed().addListener(this.edit_,
				new Signal.Listener() {
					public void trigger() {
						WInPlaceEdit.this.edit_.disable();
					}
				});
		this.edit_.enterPressed().addListener(this, new Signal.Listener() {
			public void trigger() {
				WInPlaceEdit.this.save();
			}
		});
		this.edit_.enterPressed().setPreventDefault(true);
		this.save_.clicked().addListener(this.save_,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WInPlaceEdit.this.save_.hide();
					}
				});
		this.save_.clicked().addListener(this.cancel_,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WInPlaceEdit.this.cancel_.hide();
					}
				});
		this.save_.clicked().addListener(this.edit_,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WInPlaceEdit.this.edit_.disable();
					}
				});
		this.save_.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WInPlaceEdit.this.save();
					}
				});
		this.cancel_.clicked().addListener(this.save_,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WInPlaceEdit.this.save_.hide();
					}
				});
		this.cancel_.clicked().addListener(this.cancel_,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WInPlaceEdit.this.cancel_.hide();
					}
				});
		this.cancel_.clicked().addListener(this.edit_,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WInPlaceEdit.this.edit_.hide();
					}
				});
		this.cancel_.clicked().addListener(this.text_,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WInPlaceEdit.this.text_.show();
					}
				});
	}

	/**
	 * Create an in-place edit with the given text.
	 * <p>
	 * Calls {@link #WInPlaceEdit(CharSequence text, WContainerWidget parent)
	 * this(text, (WContainerWidget)null)}
	 */
	public WInPlaceEdit(CharSequence text) {
		this(text, (WContainerWidget) null);
	}

	/**
	 * Returns the current text value.
	 * <p>
	 * 
	 * @see WInPlaceEdit#setText(CharSequence text)
	 */
	public WString getText() {
		return this.text_.getText();
	}

	/**
	 * Set the current text.
	 * <p>
	 * 
	 * @see WInPlaceEdit#getText()
	 */
	public void setText(CharSequence text) {
		this.text_.setText(text);
		this.edit_.setText(text.toString());
	}

	/**
	 * Returns the line edit.
	 * <p>
	 * You may for example set a validator on the line edit.
	 */
	public WLineEdit getLineEdit() {
		return this.edit_;
	}

	/**
	 * Returns the save button.
	 * <p>
	 * 
	 * @see WInPlaceEdit#getCancelButton()
	 */
	public WPushButton getSaveButton() {
		return this.save_;
	}

	/**
	 * Returns the cancel button.
	 * <p>
	 * 
	 * @see WInPlaceEdit#getSaveButton()
	 */
	public WPushButton getCancelButton() {
		return this.cancel_;
	}

	/**
	 * Signal emitted when the value has been changed.
	 * <p>
	 * The signal argument provides the new value.
	 */
	public Signal1<WString> valueChanged() {
		return this.valueChanged_;
	}

	private void save() {
		this.edit_.hide();
		this.text_.show();
		this.text_.setText(this.edit_.getText());
		this.edit_.enable();
		this.valueChanged().trigger(new WString(this.edit_.getText()));
	}

	private Signal1<WString> valueChanged_;
	private WContainerWidget impl_;
	private WText text_;
	private WLineEdit edit_;
	private WPushButton save_;
	private WPushButton cancel_;
}
