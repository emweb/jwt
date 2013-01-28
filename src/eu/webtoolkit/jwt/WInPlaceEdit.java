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
 * A widget that provides in-place-editable text.
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
 * 
 * <pre>
 * {
 * 	&#064;code
 * 	WContainerWidget w = new WContainerWidget();
 * 	new WText(&quot;Name: &quot;, w);
 * 	WInPlaceEdit edit = new WInPlaceEdit(&quot;Bob Smith&quot;, w);
 * 	edit.setStyleClass(&quot;inplace&quot;);
 * }
 * </pre>
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
 * CSS stylesheet:
 * 
 * <pre>
 * {@code
 *  .inplace span:hover {
 *     background-color: gray;
 *  }
 * }
 * </pre>
 */
public class WInPlaceEdit extends WCompositeWidget {
	private static Logger logger = LoggerFactory.getLogger(WInPlaceEdit.class);

	/**
	 * Creates an in-place edit with the given text.
	 */
	public WInPlaceEdit(CharSequence text, WContainerWidget parent) {
		super(parent);
		this.valueChanged_ = new Signal1<WString>(this);
		this.emptyText_ = new WString();
		this.c1_ = new AbstractSignal.Connection();
		this.c2_ = new AbstractSignal.Connection();
		this.setImplementation(this.impl_ = new WContainerWidget());
		this.setInline(true);
		this.text_ = new WText(WString.Empty, TextFormat.PlainText, this.impl_);
		this.text_.getDecorationStyle().setCursor(Cursor.ArrowCursor);
		this.editing_ = new WContainerWidget(this.impl_);
		this.editing_.setInline(true);
		this.editing_.hide();
		this.editing_.addStyleClass("input-append");
		this.edit_ = new WLineEdit(this.editing_);
		this.edit_.setTextSize(20);
		this.save_ = null;
		this.cancel_ = null;
		this.text_.clicked().addListener(this.text_,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WInPlaceEdit.this.text_.hide();
					}
				});
		this.text_.clicked().addListener(this.editing_,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						WInPlaceEdit.this.editing_.show();
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
		this.edit_.enterPressed().preventDefaultAction();
		this.edit_.escapePressed().addListener(this.editing_,
				new Signal.Listener() {
					public void trigger() {
						WInPlaceEdit.this.editing_.hide();
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
		this.edit_.escapePressed().preventDefaultAction();
		this.setButtonsEnabled();
		this.setText(text);
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
		return new WString(this.edit_.getText());
	}

	/**
	 * Sets the current value.
	 * <p>
	 * 
	 * @see WInPlaceEdit#getText()
	 */
	public void setText(CharSequence text) {
		if (!(text.length() == 0)) {
			this.text_.setText(text);
			this.empty_ = false;
		} else {
			this.text_.setText(this.emptyText_);
			this.empty_ = true;
		}
		this.edit_.setText(text.toString());
	}

	/**
	 * Sets the empty text to be shown when the field is empty and not being
	 * edited.
	 * <p>
	 * 
	 * @see WInPlaceEdit#getEmptyText()
	 */
	public void setEmptyText(CharSequence emptyText) {
		this.emptyText_ = WString.toWString(emptyText);
		if (this.empty_) {
			this.text_.setText(this.emptyText_);
		}
	}

	/**
	 * Returns the empty text to be shown when the field is empty and not being
	 * edited.
	 * <p>
	 * 
	 * @see WInPlaceEdit#setEmptyText(CharSequence emptyText)
	 */
	public WString getEmptyText() {
		return this.emptyText_;
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
	 * Returns the {@link WText} widget that renders the current string.
	 * <p>
	 * You may use this for example to set the text format of the displayed
	 * string.
	 */
	public WText getTextWidget() {
		return this.text_;
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
	 * <p>
	 * In this mode, the enter key or any event that causes focus to be lost
	 * saves the value while the escape key cancels the editing.
	 */
	public void setButtonsEnabled(boolean enabled) {
		if (this.c1_.isConnected()) {
			this.c1_.disconnect();
		}
		if (this.c2_.isConnected()) {
			this.c2_.disconnect();
		}
		if (enabled) {
			this.save_ = new WPushButton(tr("Wt.WInPlaceEdit.Save"),
					this.editing_);
			this.cancel_ = new WPushButton(tr("Wt.WInPlaceEdit.Cancel"),
					this.editing_);
			this.save_.clicked().addListener(this.edit_,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent e1) {
							WInPlaceEdit.this.edit_.disable();
						}
					});
			this.save_.clicked().addListener(this.save_,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent e1) {
							WInPlaceEdit.this.save_.disable();
						}
					});
			this.save_.clicked().addListener(this.cancel_,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent e1) {
							WInPlaceEdit.this.cancel_.disable();
						}
					});
			this.save_.clicked().addListener(this,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent e1) {
							WInPlaceEdit.this.save();
						}
					});
			this.cancel_.clicked().addListener(this.editing_,
					new Signal1.Listener<WMouseEvent>() {
						public void trigger(WMouseEvent e1) {
							WInPlaceEdit.this.editing_.hide();
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
		this.editing_.hide();
		this.text_.show();
		this.edit_.enable();
		if (this.save_ != null) {
			this.save_.enable();
		}
		if (this.cancel_ != null) {
			this.cancel_.enable();
		}
		boolean changed = this.empty_ ? this.edit_.getText().length() != 0
				: !this.edit_.getText().equals(this.text_.getText().toString());
		if (changed) {
			this.setText(this.edit_.getText());
			this.valueChanged().trigger(new WString(this.edit_.getText()));
		}
	}

	private void cancel() {
		this.edit_.setText((this.empty_ ? WString.Empty : this.text_.getText())
				.toString());
	}

	private Signal1<WString> valueChanged_;
	private WContainerWidget impl_;
	private WContainerWidget editing_;
	private WText text_;
	private WString emptyText_;
	private WLineEdit edit_;
	private WPushButton save_;
	private WPushButton cancel_;
	private AbstractSignal.Connection c1_;
	private AbstractSignal.Connection c2_;
	private boolean empty_;
}
