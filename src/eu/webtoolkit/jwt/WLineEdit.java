/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.BitSet;
import java.util.EnumSet;

/**
 * A widget that provides a single line edit
 * <p>
 * 
 * To act upon text changes, connect a slot to the {@link WFormWidget#changed()}
 * signal. This signal is emitted when the user changed the content, and
 * subsequently removes the focus from the line edit.
 * <p>
 * To act upon editing, connect a slot to the
 * {@link WInteractWidget#keyWentUp()} signal.
 * <p>
 * At all times, the current content may be accessed with the
 * {@link WLineEdit#getText()} method.
 * <p>
 * You may specify a maximum length for the input using
 * {@link WLineEdit#setMaxLength(int chars)}. If you wish to provide more
 * detailed input validation, you may set a validator using the
 * {@link WFormWidget#setValidator(WValidator validator)} method. Validators
 * provide, in general, both client-side validation (as visual feed-back only)
 * and server-side validation when calling {@link WLineEdit#validate()}.
 * <p>
 * The widget corresponds to the HTML
 * <code>&lt;input type=&quot;text&quot;&gt;</code> or
 * <code>&lt;input type=&quot;password&quot;&gt;</code> tag.
 * <p>
 * WLineEdit is an {@link WWidget#setInline(boolean inlined) inline} widget.
 * <p>
 * 
 * @see WTextArea
 */
public class WLineEdit extends WFormWidget {
	/**
	 * Enumeration that describes how the contents is displayed.
	 * <p>
	 * 
	 * @see WLineEdit#setEchoMode(WLineEdit.EchoMode echoMode)
	 */
	public enum EchoMode {
		/**
		 * Characters are shown.
		 */
		Normal,
		/**
		 * Hide the contents as for a password.
		 */
		Password;

		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Construct a line edit with empty content and optional parent.
	 */
	public WLineEdit(WContainerWidget parent) {
		super(parent);
		this.content_ = "";
		this.textSize_ = 10;
		this.maxLength_ = -1;
		this.echoMode_ = WLineEdit.EchoMode.Normal;
		this.flags_ = new BitSet();
		this.setInline(true);
		this.setFormObject(true);
	}

	/**
	 * Construct a line edit with empty content and optional parent.
	 * <p>
	 * Calls {@link #WLineEdit(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WLineEdit() {
		this((WContainerWidget) null);
	}

	/**
	 * Construct a line edit with given content and optional parent.
	 */
	public WLineEdit(String text, WContainerWidget parent) {
		super(parent);
		this.content_ = text;
		this.textSize_ = 10;
		this.maxLength_ = -1;
		this.echoMode_ = WLineEdit.EchoMode.Normal;
		this.flags_ = new BitSet();
		this.setInline(true);
		this.setFormObject(true);
	}

	/**
	 * Construct a line edit with given content and optional parent.
	 * <p>
	 * Calls {@link #WLineEdit(String text, WContainerWidget parent) this(text,
	 * (WContainerWidget)null)}
	 */
	public WLineEdit(String text) {
		this(text, (WContainerWidget) null);
	}

	/**
	 * Specify the width of the line edit in number of characters.
	 * <p>
	 * This specifies the width of the line edit that is roughly equivalent with
	 * <i>chars</i> characters. This does not limit the maximum length of a
	 * string that may be entered, which may be set using
	 * {@link WLineEdit#setMaxLength(int chars)}.
	 * <p>
	 * The default value is 10.
	 */
	public void setTextSize(int chars) {
		if (this.textSize_ != chars) {
			this.textSize_ = chars;
			this.flags_.set(BIT_TEXT_SIZE_CHANGED);
			this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
		}
	}

	/**
	 * Get the current width of the line edit in number of characters.
	 * <p>
	 * 
	 * @see WLineEdit#setTextSize(int chars)
	 */
	public int getTextSize() {
		return this.textSize_;
	}

	/**
	 * Change the content of the line edit.
	 * <p>
	 * The default value is &quot;&quot;.
	 * <p>
	 * 
	 * @see WLineEdit#getText()
	 */
	public void setText(String text) {
		if (!this.content_.equals(text)) {
			this.content_ = text;
			this.flags_.set(BIT_CONTENT_CHANGED);
			this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyIEMobile));
			if (this.getValidator() != null) {
				this
						.setStyleClass(this.validate() == WValidator.State.Valid ? ""
								: "Wt-invalid");
			}
		}
	}

	/**
	 * Get the current content.
	 * <p>
	 * 
	 * @see WLineEdit#setText(String text)
	 */
	public String getText() {
		return this.content_;
	}

	/**
	 * Specify the maximum length of text that can be entered.
	 * <p>
	 * A value &lt;= 0 indicates that there is no limit.
	 * <p>
	 * The default value is -1.
	 */
	public void setMaxLength(int chars) {
		if (this.maxLength_ != chars) {
			this.maxLength_ = chars;
			this.flags_.set(BIT_MAX_LENGTH_CHANGED);
			this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
		}
	}

	/**
	 * Returns the maximum length of text that can be entered.
	 * <p>
	 * 
	 * @see WLineEdit#setMaxLength(int chars)
	 */
	public int getMaxLength() {
		return this.maxLength_;
	}

	/**
	 * Set the echo mode.
	 * <p>
	 * The default echo mode is Normal.
	 */
	public void setEchoMode(WLineEdit.EchoMode echoMode) {
		if (this.echoMode_ != echoMode) {
			this.echoMode_ = echoMode;
			this.flags_.set(BIT_ECHO_MODE_CHANGED);
			this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
		}
	}

	/**
	 * Get the echo mode.
	 * <p>
	 * 
	 * @see WLineEdit#setEchoMode(WLineEdit.EchoMode echoMode)
	 */
	public WLineEdit.EchoMode getEchoMode() {
		return this.echoMode_;
	}

	public WValidator.State validate() {
		if (this.getValidator() != null) {
			return this.getValidator().validate(this.content_);
		} else {
			return WValidator.State.Valid;
		}
	}

	private String content_;
	private int textSize_;
	private int maxLength_;
	private WLineEdit.EchoMode echoMode_;
	private static final int BIT_CONTENT_CHANGED = 0;
	private static final int BIT_TEXT_SIZE_CHANGED = 1;
	private static final int BIT_MAX_LENGTH_CHANGED = 2;
	private static final int BIT_ECHO_MODE_CHANGED = 3;
	private BitSet flags_;

	protected void updateDom(DomElement element, boolean all) {
		if (all || this.flags_.get(BIT_CONTENT_CHANGED)) {
			element.setProperty(Property.PropertyValue, this.content_);
			this.flags_.clear(BIT_CONTENT_CHANGED);
		}
		if (all || this.flags_.get(BIT_ECHO_MODE_CHANGED)) {
			element.setAttribute("type",
					this.echoMode_ == WLineEdit.EchoMode.Normal ? "text"
							: "password");
			this.flags_.clear(BIT_ECHO_MODE_CHANGED);
		}
		if (all || this.flags_.get(BIT_TEXT_SIZE_CHANGED)) {
			element.setAttribute("size", String.valueOf(this.textSize_));
			this.flags_.clear(BIT_TEXT_SIZE_CHANGED);
		}
		if (all || this.flags_.get(BIT_MAX_LENGTH_CHANGED)) {
			if (!all || this.maxLength_ > 0) {
				element.setAttribute("maxLength", String
						.valueOf(this.maxLength_));
			}
			this.flags_.clear(BIT_MAX_LENGTH_CHANGED);
		}
		super.updateDom(element, all);
	}

	protected DomElementType getDomElementType() {
		return DomElementType.DomElement_INPUT;
	}

	protected void propagateRenderOk(boolean deep) {
		this.flags_.clear();
		super.propagateRenderOk(deep);
	}

	protected void setFormData(WObject.FormData formData) {
		if (this.flags_.get(BIT_CONTENT_CHANGED)) {
			return;
		}
		if (!formData.values.isEmpty()) {
			String value = formData.values.get(0);
			this.content_ = value;
		}
	}
}
