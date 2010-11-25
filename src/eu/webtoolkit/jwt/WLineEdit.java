/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.List;

/**
 * A widget that provides a single line edit.
 * <p>
 * 
 * To act upon text changes, connect a slot to the {@link WFormWidget#changed()
 * WFormWidget#changed()} signal. This signal is emitted when the user changed
 * the content, and subsequently removes the focus from the line edit.
 * <p>
 * To act upon editing, connect a slot to the
 * {@link WInteractWidget#keyWentUp() WInteractWidget#keyWentUp()} signal.
 * <p>
 * At all times, the current content may be accessed with the
 * {@link WLineEdit#getText() getText()} method.
 * <p>
 * You may specify a maximum length for the input using
 * {@link WLineEdit#setMaxLength(int chars) setMaxLength()}. If you wish to
 * provide more detailed input validation, you may set a validator using the
 * {@link WFormWidget#setValidator(WValidator validator)
 * WFormWidget#setValidator()} method. Validators provide, in general, both
 * client-side validation (as visual feed-back only) and server-side validation
 * when calling {@link WLineEdit#validate() validate()}.
 * <p>
 * The widget corresponds to the HTML
 * <code>&lt;input type=&quot;text&quot;&gt;</code> or
 * <code>&lt;input type=&quot;password&quot;&gt;</code> tag.
 * <p>
 * WLineEdit is an {@link WWidget#setInline(boolean inlined) inline} widget.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * The emptyText style can be configured via .Wt-edit-emptyText, other styling
 * can be done using inline or external CSS as appropriate.
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

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Creates a line edit with empty content and optional parent.
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
	 * Creates a line edit with empty content and optional parent.
	 * <p>
	 * Calls {@link #WLineEdit(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WLineEdit() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates a line edit with given content and optional parent.
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
	 * Creates a line edit with given content and optional parent.
	 * <p>
	 * Calls {@link #WLineEdit(String text, WContainerWidget parent) this(text,
	 * (WContainerWidget)null)}
	 */
	public WLineEdit(String text) {
		this(text, (WContainerWidget) null);
	}

	/**
	 * Specifies the width of the line edit in number of characters.
	 * <p>
	 * This specifies the width of the line edit that is roughly equivalent with
	 * <code>chars</code> characters. This does not limit the maximum length of
	 * a string that may be entered, which may be set using
	 * {@link WLineEdit#setMaxLength(int chars) setMaxLength()}.
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
	 * Returns the current width of the line edit in number of characters.
	 * <p>
	 * 
	 * @see WLineEdit#setTextSize(int chars)
	 */
	public int getTextSize() {
		return this.textSize_;
	}

	/**
	 * Sets the content of the line edit.
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
			this.validate();
			this.updateEmptyText();
		}
	}

	/**
	 * Returns the current content.
	 * <p>
	 * 
	 * @see WLineEdit#setText(String text)
	 */
	public String getText() {
		return this.content_;
	}

	/**
	 * Specifies the maximum length of text that can be entered.
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
	 * Sets the echo mode.
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
	 * Returns the echo mode.
	 * <p>
	 * 
	 * @see WLineEdit#setEchoMode(WLineEdit.EchoMode echoMode)
	 */
	public WLineEdit.EchoMode getEchoMode() {
		return this.echoMode_;
	}

	/**
	 * Returns the current selection start.
	 * <p>
	 * Returns -1 if there is no selected text.
	 * <p>
	 * 
	 * @see WLineEdit#hasSelectedText()
	 * @see WLineEdit#getSelectedText()
	 */
	public int getSelectionStart() {
		WApplication app = WApplication.getInstance();
		if (app.getFocus().equals(this.getId())) {
			if (app.getSelectionStart() != -1
					&& app.getSelectionEnd() != app.getSelectionStart()) {
				return app.getSelectionStart();
			} else {
				return -1;
			}
		} else {
			return -1;
		}
	}

	/**
	 * Returns the currently selected text.
	 * <p>
	 * Returns an empty string if there is currently no selected text.
	 * <p>
	 * 
	 * @see WLineEdit#hasSelectedText()
	 */
	public String getSelectedText() {
		if (this.getSelectionStart() != -1) {
			WApplication app = WApplication.getInstance();
			return new WString(this.getText().substring(
					app.getSelectionStart(),
					app.getSelectionEnd() - app.getSelectionStart()))
					.toString();
		} else {
			return WString.Empty.toString();
		}
	}

	/**
	 * Returns whether there is selected text.
	 * <p>
	 */
	public boolean hasSelectedText() {
		return this.getSelectionStart() != -1;
	}

	/**
	 * Returns the current cursor position.
	 * <p>
	 * Returns -1 if the widget does not have the focus.
	 */
	public int getCursorPosition() {
		WApplication app = WApplication.getInstance();
		if (app.getFocus().equals(this.getId())) {
			return app.getSelectionEnd();
		} else {
			return -1;
		}
	}

	public WValidator.State validate() {
		if (this.getValidator() != null) {
			WValidator.State result = this.getValidator().validate(
					this.content_);
			if (result == WValidator.State.Valid) {
				this.removeStyleClass("Wt-invalid", true);
			} else {
				this.addStyleClass("Wt-invalid", true);
			}
			return result;
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
	BitSet flags_;

	void updateDom(DomElement element, boolean all) {
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

	DomElementType getDomElementType() {
		return DomElementType.DomElement_INPUT;
	}

	void propagateRenderOk(boolean deep) {
		this.flags_.clear();
		super.propagateRenderOk(deep);
	}

	void getDomChanges(List<DomElement> result, WApplication app) {
		if (app.getEnvironment().agentIsIE()
				&& this.flags_.get(BIT_ECHO_MODE_CHANGED)) {
			DomElement e = DomElement.getForUpdate(this, this
					.getDomElementType());
			DomElement d = this.createDomElement(app);
			e.replaceWith(d);
			result.add(e);
		} else {
			super.getDomChanges(result, app);
		}
	}

	void setFormData(WObject.FormData formData) {
		if (this.flags_.get(BIT_CONTENT_CHANGED)) {
			return;
		}
		if (!(formData.values.length == 0)) {
			String value = formData.values[0];
			this.content_ = value;
		}
	}

	protected int boxPadding(Orientation orientation) {
		WEnvironment env = WApplication.getInstance().getEnvironment();
		if (env.agentIsIE() || env.agentIsOpera()) {
			return 1;
		} else {
			if (env.getAgent() == WEnvironment.UserAgent.Arora) {
				return 0;
			} else {
				if (env.getUserAgent().indexOf("Mac OS X") != -1) {
					return 1;
				} else {
					if (env.getUserAgent().indexOf("Windows") != -1
							&& !env.agentIsGecko()) {
						return 0;
					} else {
						return 1;
					}
				}
			}
		}
	}

	protected int boxBorder(Orientation orientation) {
		WEnvironment env = WApplication.getInstance().getEnvironment();
		if (env.getUserAgent().indexOf("Mac OS X") != -1 && env.agentIsGecko()) {
			return 3;
		} else {
			if (env.getAgent() == WEnvironment.UserAgent.Arora) {
				return 0;
			} else {
				return 2;
			}
		}
	}
}
