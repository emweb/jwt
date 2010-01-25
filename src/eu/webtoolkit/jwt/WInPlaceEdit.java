/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * A widget that provides in-place-editable text
 * <p>
 * 
 * The WInPlaceEdit provides a text that may be edited in place by the user by
 * clicking on it. When clicked, the text turns into a line edit, with
 * optionally a save and cancel button (see
 * {@link WInPlaceEdit#setButtonsEnabled(boolean enabled) setButtonsEnabled()}).
 * <p>
 * When the user saves the edit, the {@link WInPlaceEdit#valueChanged()
 * valueChanged()} signal is emitted.
 * <p>
 * Usage example:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * WContainerWidget w = new WContainerWidget();
 * new WText(&quot;Name: &quot;, w);
 * WInPlaceEdit edit = new WInPlaceEdit(&quot;Bob Smith&quot;, w);
 * edit.setStyleClass(&quot;inplace&quot;);
 * </pre>
 * 
 * </blockquote>
 * <p>
 * This code will produce an edit that looks like: <div align="center"> <img
 * src="doc-files//WInPlaceEdit-1.png" alt="WInPlaceEdit text mode">
 * <p>
 * <strong>WInPlaceEdit text mode</strong>
 * </p>
 * </div> When the text is clicked, the edit will expand to become: <div
 * align="center"> <img src="doc-files//WInPlaceEdit-2.png"
 * alt="WInPlaceEdit edit mode">
 * <p>
 * <strong>WInPlaceEdit edit mode</strong>
 * </p>
 * </div> <h3>CSS</h3>
 * <p>
 * A {@link WInPlaceEdit} widget renders as a <code>&lt;span&gt;</code>
 * containing a {@link WText}, a {@link WLineEdit} and optional buttons (
 * {@link WPushButton}). All these widgets may be styled as such. It does not
 * provide style information.
 * <p>
 * In particular, you may want to provide a visual indication that the text is
 * editable e.g. using a hover effect:
 * <p>
 * CSS stylesheet: <blockquote>
 * 
 * <pre>
 * .inplace span:hover {
 *     background-color: gray;
 *  }
 * </pre>
 * 
 * </blockquote>
 */
public class WInPlaceEdit extends WCompositeWidget {
	/**
	 * Creates an in-place edit with the given text.
	 */
	public WInPlaceEdit(CharSequence text, WContainerWidget parent) {
		super(parent);
		this.valueChanged_ = new Signal1<WString>(this);
		this.c1_ = new AbstractSignal.Connection();
		this.c2_ = new AbstractSignal.Connection();
		this.setImplementation(this.impl_ = new WContainerWidget());
		this.setInline(true);
		this.text_ = new WText(text, TextFormat.PlainText, this.impl_);
		this.text_.getDecorationStyle().setCursor(Cursor.ArrowCursor);
		this.edit_ = new WLineEdit(text.toString(), this.impl_);
		this.edit_.setTextSize(20);
		this.save_ = null;
		this.cancel_ = null;
		this.edit_.hide();
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
		this.text_.clicked().addListener(this.edit_,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WInPlaceEdit.this.edit_.setFocus();
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
		this.edit_.escapePressed().addListener(this.edit_,
				new Signal.Listener() {
					public void trigger() {
						WInPlaceEdit.this.edit_.hide();
					}
				});
		this.edit_.escapePressed().addListener(this.text_,
				new Signal.Listener() {
					public void trigger() {
						WInPlaceEdit.this.text_.show();
					}
				});
		this.edit_.escapePressed().addListener(this, new Signal.Listener() {
			public void trigger() {
				WInPlaceEdit.this.cancel();
			}
		});
		this.edit_.escapePressed().setPreventDefault(true);
		this.edit_.blurred().addListener(this.edit_, new Signal.Listener() {
			public void trigger() {
				WInPlaceEdit.this.edit_.hide();
			}
		});
		this.edit_.blurred().addListener(this.text_, new Signal.Listener() {
			public void trigger() {
				WInPlaceEdit.this.text_.show();
			}
		});
		this.edit_.blurred().addListener(this, new Signal.Listener() {
			public void trigger() {
				WInPlaceEdit.this.cancel();
			}
		});
		this.setButtonsEnabled();
	}

	/**
	 * Creates an in-place edit with the given text.
	 * <p>
	 * Calls {@link #WInPlaceEdit(CharSequence text, WContainerWidget parent)
	 * this(text, (WContainerWidget)null)}
	 */
	public WInPlaceEdit(CharSequence text) {
		this(text, (WContainerWidget) null);
	}

	/**
	 * Returns the current value.
	 * <p>
	 * 
	 * @see WInPlaceEdit#setText(CharSequence text)
	 */
	public WString getText() {
		return this.text_.getText();
	}

	/**
	 * Sets the current value.
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
	 * You may use this for example to set a validator on the line edit.
	 */
	public WLineEdit getLineEdit() {
		return this.edit_;
	}

	/**
	 * Returns the save button.
	 * <p>
	 * This method returns <code>null</code> if the buttons were disabled.
	 * <p>
	 * 
	 * @see WInPlaceEdit#getCancelButton()
	 * @see WInPlaceEdit#setButtonsEnabled(boolean enabled)
	 */
	public WPushButton getSaveButton() {
		return this.save_;
	}

	/**
	 * Returns the cancel button.
	 * <p>
	 * This method returns <code>null</code> if the buttons were disabled.
	 * <p>
	 * 
	 * @see WInPlaceEdit#getSaveButton()
	 * @see WInPlaceEdit#setButtonsEnabled(boolean enabled)
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

	/**
	 * Displays the Save and &apos;Cancel&apos; button during editing.
	 * <p>
	 * By default, the Save and Cancel buttons are shown. Call this function
	 * with <code>enabled</code> = <code>false</code> to only show a line edit.
	 * In this mode, the enter key has the effect of the save button and the
	 * escape key has the effect of the cancel button.
	 */
	public void setButtonsEnabled(boolean enabled) {
		if (this.c1_.isConnected()) {
			this.c1_.disconnect();
		}
		if (this.c2_.isConnected()) {
			this.c2_.disconnect();
		}
		if (enabled) {
			this.save_ = new WPushButton("Save", this.impl_);
			this.cancel_ = new WPushButton("Cancel", this.impl_);
			this.save_.hide();
			this.cancel_.hide();
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
			this.edit_.escapePressed().addListener(this.save_,
					new Signal.Listener() {
						public void trigger() {
							WInPlaceEdit.this.save_.hide();
						}
					});
			this.edit_.escapePressed().addListener(this.cancel_,
					new Signal.Listener() {
						public void trigger() {
							WInPlaceEdit.this.cancel_.hide();
						}
					});
			this.edit_.blurred().addListener(this.save_, new Signal.Listener() {
				public void trigger() {
					WInPlaceEdit.this.save_.hide();
				}
			});
			this.edit_.blurred().addListener(this.cancel_,
					new Signal.Listener() {
						public void trigger() {
							WInPlaceEdit.this.cancel_.hide();
						}
					});
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
			this.cancel_.clicked().addListener(this,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent e1) {
							WInPlaceEdit.this.cancel();
						}
					});
		} else {
			if (this.save_ != null)
				this.save_.remove();
			this.save_ = null;
			if (this.cancel_ != null)
				this.cancel_.remove();
			this.cancel_ = null;
			this.c1_ = this.edit_.blurred().addListener(this.edit_,
					new Signal.Listener() {
						public void trigger() {
							WInPlaceEdit.this.edit_.disable();
						}
					});
			this.c2_ = this.edit_.blurred().addListener(this,
					new Signal.Listener() {
						public void trigger() {
							WInPlaceEdit.this.save();
						}
					});
		}
	}

	/**
	 * Displays the Save and &apos;Cancel&apos; button during editing.
	 * <p>
	 * Calls {@link #setButtonsEnabled(boolean enabled) setButtonsEnabled(true)}
	 */
	public final void setButtonsEnabled() {
		setButtonsEnabled(true);
	}

	private void save() {
		this.edit_.hide();
		this.text_.show();
		this.text_.setText(this.edit_.getText());
		this.edit_.enable();
		this.valueChanged().trigger(new WString(this.edit_.getText()));
	}

	private void cancel() {
		this.edit_.setText(this.text_.getText().toString());
	}

	private Signal1<WString> valueChanged_;
	private WContainerWidget impl_;
	private WText text_;
	private WLineEdit edit_;
	private WPushButton save_;
	private WPushButton cancel_;
	private AbstractSignal.Connection c1_;
	private AbstractSignal.Connection c2_;
}
