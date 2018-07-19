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
 * A widget that provides a single line edit.
 * <p>
 * 
 * To act upon text changes, connect a slot to the {@link WFormWidget#changed()
 * WFormWidget#changed()} signal. This signal is emitted when the user changed
 * the content, and subsequently removes the focus from the line edit.
 * <p>
 * To act upon editing, connect a slot to the
 * {@link WInteractWidget#keyWentUp() WInteractWidget#keyWentUp()} signal
 * because the {@link WInteractWidget#keyPressed() WInteractWidget#keyPressed()}
 * signal is fired before the line edit has interpreted the keypress to change
 * its text.
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
	private static Logger logger = LoggerFactory.getLogger(WLineEdit.class);

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
	 * Enumeration that describes options for input masks.
	 * <p>
	 * 
	 * @see WLineEdit#setInputMask(String mask, EnumSet flags)
	 */
	public enum InputMaskFlag {
		/**
		 * Keep the input mask when blurred.
		 */
		KeepMaskWhileBlurred;

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
		this.displayContent_ = "";
		this.textSize_ = 10;
		this.maxLength_ = -1;
		this.echoMode_ = WLineEdit.EchoMode.Normal;
		this.autoComplete_ = true;
		this.flags_ = new BitSet();
		this.maskChanged_ = false;
		this.mask_ = "";
		this.inputMask_ = "";
		this.raw_ = "";
		this.spaceChar_ = ' ';
		this.inputMaskFlags_ = EnumSet.noneOf(WLineEdit.InputMaskFlag.class);
		this.case_ = "";
		this.javaScriptDefined_ = false;
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
	public WLineEdit(final String text, WContainerWidget parent) {
		super(parent);
		this.content_ = "";
		this.displayContent_ = "";
		this.textSize_ = 10;
		this.maxLength_ = -1;
		this.echoMode_ = WLineEdit.EchoMode.Normal;
		this.autoComplete_ = true;
		this.flags_ = new BitSet();
		this.maskChanged_ = false;
		this.mask_ = "";
		this.inputMask_ = "";
		this.raw_ = "";
		this.spaceChar_ = ' ';
		this.inputMaskFlags_ = EnumSet.noneOf(WLineEdit.InputMaskFlag.class);
		this.case_ = "";
		this.javaScriptDefined_ = false;
		this.setInline(true);
		this.setFormObject(true);
		this.setText(text);
	}

	/**
	 * Creates a line edit with given content and optional parent.
	 * <p>
	 * Calls {@link #WLineEdit(String text, WContainerWidget parent) this(text,
	 * (WContainerWidget)null)}
	 */
	public WLineEdit(final String text) {
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
			this.repaint(EnumSet.of(RepaintFlag.RepaintSizeAffected));
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
	public void setText(final String text) {
		String newDisplayText = this.inputText(text);
		String newText = this.removeSpaces(newDisplayText);
		if (this.maskChanged_ || !this.content_.equals(newText)
				|| !this.displayContent_.equals(newDisplayText)) {
			this.content_ = newText;
			this.displayContent_ = newDisplayText;
			if (this.isRendered() && this.inputMask_.length() != 0) {
				this.doJavaScript("jQuery.data(" + this.getJsRef()
						+ ", 'lobj').setValue("
						+ WWebWidget.jsStringLiteral(newDisplayText) + ");");
			}
			this.flags_.set(BIT_CONTENT_CHANGED);
			this.repaint();
			this.validate();
			this.applyEmptyText();
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
	 * Returns the displayed text.
	 * <p>
	 * If {@link WLineEdit#getEchoMode() getEchoMode()} is set to Normal, and no
	 * input mask is defined, this returns the same as
	 * {@link WLineEdit#getText() getText()}.
	 * <p>
	 * If an input mask is defined, then the text is returned including space
	 * characters.
	 * <p>
	 * If {@link WLineEdit#getEchoMode() getEchoMode()} is set to Password, then
	 * a string of asterisks is returned equal to the length of the text.
	 * <p>
	 * 
	 * @see WLineEdit#setText(String text)
	 */
	public String getDisplayText() {
		if (this.echoMode_ == WLineEdit.EchoMode.Normal) {
			return this.displayContent_;
		} else {
			String text = this.displayContent_;
			StringWriter result = new StringWriter();
			for (int i = 0; i < result.toString().length(); i++) {
				result.append('*');
			}
			return result.toString();
		}
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
			this.repaint();
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
			this.repaint();
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
	 * Sets (built-in browser) autocomplete support.
	 * <p>
	 * Depending on the user agent, this may assist the user in filling in text
	 * for common input fields (e.g. address information) based on some
	 * heuristics.
	 * <p>
	 * The default value is <code>true</code>.
	 */
	public void setAutoComplete(boolean enabled) {
		if (this.autoComplete_ != enabled) {
			this.autoComplete_ = enabled;
			this.flags_.set(BIT_AUTOCOMPLETE_CHANGED);
			this.repaint();
		}
	}

	/**
	 * Returns auto-completion support.
	 * <p>
	 * 
	 * @see WLineEdit#setAutoComplete(boolean enabled)
	 */
	public boolean isAutoComplete() {
		return this.autoComplete_;
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
			String result = StringUtils.unicodeSubstring(this.getText(),
					app.getSelectionStart(),
					app.getSelectionEnd() - app.getSelectionStart());
			return result;
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
	 * Selects length characters starting from the start position.
	 * <p>
	 */
	public void setSelection(int start, int length) {
		String s = String.valueOf(start);
		String e = String.valueOf(start + length);
		this.doJavaScript("Wt3_3_11.setUnicodeSelectionRange("
				+ this.getJsRef() + "," + s + "," + e + ")");
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

	/**
	 * Returns the current value.
	 * <p>
	 * Returns {@link WLineEdit#getText() getText()}.
	 */
	public String getValueText() {
		return this.getText();
	}

	/**
	 * Sets the current value.
	 * <p>
	 * Calls {@link WLineEdit#setText(String text) setText()}.
	 */
	public void setValueText(final String value) {
		this.setText(value);
	}

	/**
	 * Returns the input mask.
	 * <p>
	 * 
	 * @see WLineEdit#setInputMask(String mask, EnumSet flags)
	 */
	public String getInputMask() {
		return this.inputMask_;
	}

	/**
	 * Sets the input mask.
	 * <p>
	 * If no input mask is supplied, or the given input mask is empty, no input
	 * mask is applied.
	 * <p>
	 * The following characters can be used in the input mask:
	 * <table border="1" cellspacing="3" cellpadding="3">
	 * <tr>
	 * <th>Character</th>
	 * <th>Description</th>
	 * </tr>
	 * <tr>
	 * <td>A</td>
	 * <td>ASCII alphabetic character: A-Z, a-z (required)</td>
	 * </tr>
	 * <tr>
	 * <td>a</td>
	 * <td>ASCII alphabetic character: A-Z, a-z (optional)</td>
	 * </tr>
	 * <tr>
	 * <td>N</td>
	 * <td>ASCII alphanumeric character: A-Z, a-z, 0-9 (required)</td>
	 * </tr>
	 * <tr>
	 * <td>n</td>
	 * <td>ASCII alphanumeric character: A-Z, a-z, 0-9 (optional)</td>
	 * </tr>
	 * <tr>
	 * <td>X</td>
	 * <td>Any character (required)</td>
	 * </tr>
	 * <tr>
	 * <td>x</td>
	 * <td>Any character (optional)</td>
	 * </tr>
	 * <tr>
	 * <td>9</td>
	 * <td>Digit: 0-9 (required)</td>
	 * </tr>
	 * <tr>
	 * <td>null</td>
	 * <td>Digit: 0-9 (optional)</td>
	 * </tr>
	 * <tr>
	 * <td>D</td>
	 * <td>Nonzero digit: 1-9 (required)</td>
	 * </tr>
	 * <tr>
	 * <td>d</td>
	 * <td>Nonzero digit: 1-9 (optional)</td>
	 * </tr>
	 * <tr>
	 * <td>#</td>
	 * <td>Digit or sign: 0-9, -, + (required)</td>
	 * </tr>
	 * <tr>
	 * <td>H</td>
	 * <td>Hexadecimal character: A-F, a-f, 0-9 (required)</td>
	 * </tr>
	 * <tr>
	 * <td>h</td>
	 * <td>Hexadecimal character: A-F, a-f, 0-9 (optional)</td>
	 * </tr>
	 * <tr>
	 * <td>B</td>
	 * <td>Binary digit: 0-1 (required)</td>
	 * </tr>
	 * <tr>
	 * <td>b</td>
	 * <td>Binary digit: 0-1 (optional)</td>
	 * </tr>
	 * </table>
	 * The distinction between required and optional characters won&apos;t be
	 * apparent on the client side, but will affect the result of
	 * {@link WLineEdit#validate() validate()}.
	 * <p>
	 * There are also a few special characters, that won&apos;t be checked
	 * against, but modify the value in some way:
	 * <table border="1" cellspacing="3" cellpadding="3">
	 * <tr>
	 * <th>Character</th>
	 * <th>Description</th>
	 * </tr>
	 * <tr>
	 * <td>&gt;</td>
	 * <td>The following characters are uppercased</td>
	 * </tr>
	 * <tr>
	 * <td>&lt;</td>
	 * <td>The following characters are lowercased</td>
	 * </tr>
	 * <tr>
	 * <td>!</td>
	 * <td>The casing of the following characters remains the same</td>
	 * </tr>
	 * </table>
	 * A backslash (&apos;\&apos;) can be used to escape any of the mask
	 * characters or modifiers, so that they can be used verbatim in the input
	 * mask.
	 * <p>
	 * If the mask ends with a semicolon (&apos;;&apos;) followed by a
	 * character, this character will be used on the client side to display
	 * spaces. This defaults to the space (&apos; &apos;) character. The space
	 * character will be removed from the value of this WLineEdit.
	 * <p>
	 * Examples:
	 * <table border="1" cellspacing="3" cellpadding="3">
	 * <tr>
	 * <th>Input mask</th>
	 * <th>Notes</th>
	 * </tr>
	 * <tr>
	 * <td>
	 * 
	 * <pre>
	 * 009.009.009.009;_
	 * </pre>
	 * 
	 * </td>
	 * <td>IP address. Spaces are denoted by &apos;_&apos;. Will validate if
	 * there is at least one digit per segment.</td>
	 * </tr>
	 * <tr>
	 * <td>
	 * 
	 * <pre>
	 * 9999 - 99 - 99
	 * </pre>
	 * 
	 * </td>
	 * <td>Date, in yyyy-MM-dd notation. Spaces are denoted by &apos; &apos;.
	 * Will validate if all digits are filled in.</td>
	 * </tr>
	 * <tr>
	 * <td>
	 * 
	 * <pre>
	 * &gt;HH:HH:HH:HH:HH:HH;_
	 * </pre>
	 * 
	 * </td>
	 * <td>MAC address. Spaces are denoted by &apos;_&apos;. Will validate if
	 * all hexadecimal characters are filled in. All characters will be
	 * formatted in uppercase.</td>
	 * </tr>
	 * </table>
	 * <p>
	 * Input masks are enforced by JavaScript on the client side. Without
	 * JavaScript or using {@link WLineEdit#setText(String text) setText()},
	 * however, non-compliant strings can be entered. This does not result in an
	 * error: any non-compliant characters will be removed from the input and
	 * this action will be logged.
	 */
	public void setInputMask(final String mask,
			EnumSet<WLineEdit.InputMaskFlag> flags) {
		this.inputMaskFlags_ = EnumSet.copyOf(flags);
		if (!this.inputMask_.equals(mask)) {
			this.inputMask_ = mask;
			this.mask_ = "";
			this.raw_ = "";
			this.case_ = "";
			this.spaceChar_ = ' ';
			String textBefore = "";
			if (this.inputMask_.length() != 0) {
				textBefore = this.getDisplayText();
				this.processInputMask();
				this.setText(textBefore);
			}
			if (this.isRendered() && this.javaScriptDefined_) {
				String space = "";
				space += this.spaceChar_;
				this.doJavaScript("jQuery.data(" + this.getJsRef()
						+ ", 'lobj').setInputMask("
						+ WWebWidget.jsStringLiteral(this.mask_) + ","
						+ WWebWidget.jsStringLiteral(this.raw_) + ","
						+ WWebWidget.jsStringLiteral(this.displayContent_)
						+ "," + WWebWidget.jsStringLiteral(this.case_) + ","
						+ WWebWidget.jsStringLiteral(space) + ", true);");
			} else {
				if (this.inputMask_.length() != 0) {
					this.repaint();
				}
			}
		}
	}

	/**
	 * Sets the input mask.
	 * <p>
	 * Calls {@link #setInputMask(String mask, EnumSet flags) setInputMask(mask,
	 * EnumSet.of(flag, flags))}
	 */
	public final void setInputMask(final String mask,
			WLineEdit.InputMaskFlag flag, WLineEdit.InputMaskFlag... flags) {
		setInputMask(mask, EnumSet.of(flag, flags));
	}

	/**
	 * Sets the input mask.
	 * <p>
	 * Calls {@link #setInputMask(String mask, EnumSet flags) setInputMask("",
	 * EnumSet.noneOf(WLineEdit.InputMaskFlag.class))}
	 */
	public final void setInputMask() {
		setInputMask("", EnumSet.noneOf(WLineEdit.InputMaskFlag.class));
	}

	/**
	 * Sets the input mask.
	 * <p>
	 * Calls {@link #setInputMask(String mask, EnumSet flags) setInputMask(mask,
	 * EnumSet.noneOf(WLineEdit.InputMaskFlag.class))}
	 */
	public final void setInputMask(final String mask) {
		setInputMask(mask, EnumSet.noneOf(WLineEdit.InputMaskFlag.class));
	}

	public WValidator.State validate() {
		if (this.inputMask_.length() != 0 && !this.isValidateInputMask()) {
			return WValidator.State.Invalid;
		} else {
			return super.validate();
		}
	}

	/**
	 * Event signal emitted when the text in the input field changed.
	 * <p>
	 * This signal is emitted whenever the text contents has changed. Unlike the
	 * {@link WFormWidget#changed() WFormWidget#changed()} signal, the signal is
	 * fired on every change, not only when the focus is lost. Unlike the
	 * {@link WInteractWidget#keyPressed() WInteractWidget#keyPressed()} signal,
	 * this signal is fired also for other events that change the text, such as
	 * paste actions.
	 * <p>
	 * 
	 * @see WInteractWidget#keyPressed()
	 * @see WFormWidget#changed()
	 */
	public EventSignal textInput() {
		return this.voidEventSignal(INPUT_SIGNAL, true);
	}

	private static String INPUT_SIGNAL = "input";
	private String content_;
	private String displayContent_;
	private int textSize_;
	private int maxLength_;
	private WLineEdit.EchoMode echoMode_;
	private boolean autoComplete_;
	private static final int BIT_CONTENT_CHANGED = 0;
	private static final int BIT_TEXT_SIZE_CHANGED = 1;
	private static final int BIT_MAX_LENGTH_CHANGED = 2;
	private static final int BIT_ECHO_MODE_CHANGED = 3;
	private static final int BIT_AUTOCOMPLETE_CHANGED = 4;
	BitSet flags_;
	private static final String SKIPPABLE_MASK_CHARS = "anx0d#hb";
	private boolean maskChanged_;
	private String mask_;
	private String inputMask_;
	private String raw_;
	private char spaceChar_;
	private EnumSet<WLineEdit.InputMaskFlag> inputMaskFlags_;
	private String case_;
	private boolean javaScriptDefined_;

	private String removeSpaces(final String text) {
		if (this.raw_.length() != 0 && text.length() != 0) {
			String result = text;
			int i = 0;
			for (int j = 0; j < this.raw_.length(); ++i, ++j) {
				while (j < this.raw_.length()
						&& result.charAt(j) == this.spaceChar_
						&& this.mask_.charAt(j) != '_') {
					++j;
				}
				if (j < this.raw_.length()) {
					if (i != j) {
						result = StringUtils.put(result, i, result.charAt(j));
					}
				} else {
					--i;
				}
			}
			result = result.substring(0, 0 + i);
			return result;
		} else {
			return text;
		}
	}

	private String inputText(final String text) {
		if (this.raw_.length() != 0 && text.length() != 0) {
			String newText = text;
			String result = this.raw_;
			char chr;
			boolean hadIgnoredChar = false;
			int j = 0;
			int i = 0;
			for (i = 0; i < newText.length(); ++i) {
				int previousJ = j;
				chr = newText.charAt(i);
				while (j < this.mask_.length() && !this.acceptChar(chr, j)) {
					++j;
				}
				if (j == this.mask_.length()) {
					j = previousJ;
					hadIgnoredChar = true;
				} else {
					if (this.raw_.charAt(j) != chr) {
						if (this.case_.charAt(j) == '>') {
							chr = Character.toUpperCase(chr);
						} else {
							if (this.case_.charAt(j) == '<') {
								chr = Character.toLowerCase(chr);
							}
						}
						result = StringUtils.put(result, j, chr);
					}
					++j;
				}
			}
			if (hadIgnoredChar) {
				logger.info(new StringWriter().append(
						"Input mask: not all characters in input '" + text
								+ "' complied with input mask "
								+ this.inputMask_
								+ " and were ignored. Result is '" + result
								+ "'.").toString());
			}
			return result;
		}
		return text;
	}

	private void processInputMask() {
		if (this.inputMask_.charAt(this.inputMask_.length() - 2) == ';') {
			this.spaceChar_ = this.inputMask_
					.charAt(this.inputMask_.length() - 1);
			this.inputMask_ = this.inputMask_.substring(0,
					0 + this.inputMask_.length() - 2);
		}
		;
		;
		;
		char mode = '!';
		for (int i = 0; i < this.inputMask_.length(); ++i) {
			char currentChar = this.inputMask_.charAt(i);
			if (currentChar == '>' || currentChar == '<' || currentChar == '!') {
				mode = (char) currentChar;
			} else {
				if ("AaNnXx90Dd#HhBb".indexOf(currentChar) != -1) {
					this.mask_ += (char) currentChar;
					this.raw_ += this.spaceChar_;
					this.case_ += mode;
				} else {
					if (currentChar == '\\') {
						++i;
					}
					this.mask_ += '_';
					this.raw_ += this.inputMask_.charAt(i);
					this.case_ += mode;
				}
			}
		}
	}

	private boolean acceptChar(char chr, int position) {
		if (position >= this.mask_.length()) {
			return false;
		}
		if (this.raw_.charAt(position) == chr) {
			return true;
		}
		switch (this.mask_.charAt(position)) {
		case 'a':
		case 'A':
			return chr >= 'a' && chr <= 'z' || chr >= 'A' && chr <= 'Z';
		case 'n':
		case 'N':
			return chr >= 'a' && chr <= 'z' || chr >= 'A' && chr <= 'Z'
					|| chr >= '0' && chr <= '9';
		case 'x':
		case 'X':
			return true;
		case '0':
		case '9':
			return chr >= '0' && chr <= '9';
		case 'd':
		case 'D':
			return chr >= '1' && chr <= '9';
		case '#':
			return chr >= '0' && chr <= '9' || (chr == '-' || chr == '+');
		case 'h':
		case 'H':
			return chr >= 'A' && chr <= 'F' || chr >= 'a' && chr <= 'f'
					|| chr >= '0' && chr <= '9';
		case 'b':
		case 'B':
			return chr == '0' || chr == '1';
		}
		return false;
	}

	private void defineJavaScript() {
		if (this.javaScriptDefined_) {
			return;
		}
		this.javaScriptDefined_ = true;
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/WLineEdit.js", wtjs1());
		String space = "";
		space += this.spaceChar_;
		String jsObj = "new Wt3_3_11.WLineEdit("
				+ app.getJavaScriptClass()
				+ ","
				+ this.getJsRef()
				+ ","
				+ WWebWidget.jsStringLiteral(this.mask_)
				+ ","
				+ WWebWidget.jsStringLiteral(this.raw_)
				+ ","
				+ WWebWidget.jsStringLiteral(this.displayContent_)
				+ ","
				+ WWebWidget.jsStringLiteral(this.case_)
				+ ","
				+ WWebWidget.jsStringLiteral(space)
				+ ","
				+ (!EnumUtils.mask(this.inputMaskFlags_,
						WLineEdit.InputMaskFlag.KeepMaskWhileBlurred).isEmpty() ? "0x1"
						: "0x0") + ");";
		this.setJavaScriptMember(" WLineEdit", jsObj);
		final AbstractEventSignal b = this.mouseMoved();
		final AbstractEventSignal c = this.keyWentDown();
		this.connectJavaScript(this.keyWentDown(), "keyDown");
		this.connectJavaScript(this.keyPressed(), "keyPressed");
		this.connectJavaScript(this.focussed(), "focussed");
		this.connectJavaScript(this.blurred(), "blurred");
		this.connectJavaScript(this.clicked(), "clicked");
	}

	private void connectJavaScript(final AbstractEventSignal s,
			final String methodName) {
		String jsFunction = "function(lobj, event) {var o = jQuery.data("
				+ this.getJsRef() + ", 'lobj');if (o) o." + methodName
				+ "(lobj, event);}";
		s.addListener(jsFunction);
	}

	private boolean isValidateInputMask() {
		String toCheck = this.content_;
		if (toCheck.length() == 0) {
			toCheck = this.raw_;
		}
		List<Integer> p1 = new ArrayList<Integer>();
		List<Integer> p2 = new ArrayList<Integer>();
		List<Integer> positions = p1;
		List<Integer> nextPositions = p2;
		positions.add(0);
		for (int i = 0; i < toCheck.length(); ++i) {
			for (int j = 0; j < positions.size(); ++j) {
				int currentPosition = positions.get(j);
				if (currentPosition < this.mask_.length()) {
					if (SKIPPABLE_MASK_CHARS.indexOf(this.mask_
							.charAt(currentPosition)) != -1
							&& (j + 1 == positions.size() || positions
									.get(j + 1) != currentPosition + 1)) {
						positions.add(currentPosition + 1);
					}
					if (this.acceptChar(toCheck.charAt(i), currentPosition)
							&& (nextPositions.isEmpty() || nextPositions
									.get(nextPositions.size() - 1) != currentPosition + 1)) {
						nextPositions.add(currentPosition + 1);
					}
				}
			}
			List<Integer> tmp = positions;
			positions = nextPositions;
			nextPositions = tmp;
			nextPositions.clear();
			if (positions.size() == 0) {
				return false;
			}
		}
		while (positions.size() > 0) {
			for (int j = 0; j < positions.size(); ++j) {
				int currentPosition = positions.get(j);
				if (currentPosition == this.mask_.length()) {
					return true;
				}
				if (SKIPPABLE_MASK_CHARS.indexOf(this.mask_
						.charAt(currentPosition)) != -1
						&& (nextPositions.isEmpty() || nextPositions
								.get(nextPositions.size() - 1) != currentPosition + 1)) {
					nextPositions.add(currentPosition + 1);
				}
			}
			List<Integer> tmp = positions;
			positions = nextPositions;
			nextPositions = tmp;
			nextPositions.clear();
		}
		return false;
	}

	void updateDom(final DomElement element, boolean all) {
		if (all || this.flags_.get(BIT_CONTENT_CHANGED)) {
			String t = this.content_;
			if (this.mask_.length() != 0
					&& !EnumUtils.mask(this.inputMaskFlags_,
							WLineEdit.InputMaskFlag.KeepMaskWhileBlurred)
							.isEmpty()) {
				t = this.displayContent_;
			}
			if (!all || t.length() != 0) {
				element.setProperty(Property.PropertyValue, t);
			}
			this.flags_.clear(BIT_CONTENT_CHANGED);
		}
		if (all || this.flags_.get(BIT_ECHO_MODE_CHANGED)) {
			element.setAttribute("type",
					this.echoMode_ == WLineEdit.EchoMode.Normal ? "text"
							: "password");
			this.flags_.clear(BIT_ECHO_MODE_CHANGED);
		}
		if (all || this.flags_.get(BIT_AUTOCOMPLETE_CHANGED)) {
			if (!all || !this.autoComplete_) {
				element.setAttribute("autocomplete",
						this.autoComplete_ == true ? "on" : "off");
			}
			this.flags_.clear(BIT_AUTOCOMPLETE_CHANGED);
		}
		if (all || this.flags_.get(BIT_TEXT_SIZE_CHANGED)) {
			element.setAttribute("size", String.valueOf(this.textSize_));
			this.flags_.clear(BIT_TEXT_SIZE_CHANGED);
		}
		if (all || this.flags_.get(BIT_MAX_LENGTH_CHANGED)) {
			if (!all || this.maxLength_ > 0) {
				element.setAttribute("maxLength",
						String.valueOf(this.maxLength_));
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

	protected void getDomChanges(final List<DomElement> result, WApplication app) {
		if (app.getEnvironment().agentIsIE()
				&& this.flags_.get(BIT_ECHO_MODE_CHANGED)) {
			DomElement e = DomElement.getForUpdate(this,
					this.getDomElementType());
			DomElement d = this.createDomElement(app);
			app.getTheme().apply(this.getSelfWidget(), d, 0);
			e.replaceWith(d);
			result.add(e);
		} else {
			super.getDomChanges(result, app);
		}
	}

	protected void setFormData(final WObject.FormData formData) {
		if (this.flags_.get(BIT_CONTENT_CHANGED) || this.isReadOnly()) {
			return;
		}
		if (!(formData.values.length == 0)) {
			final String value = formData.values[0];
			this.displayContent_ = this.inputText(value);
			this.content_ = this.removeSpaces(this.displayContent_);
		}
	}

	protected int boxPadding(Orientation orientation) {
		final WEnvironment env = WApplication.getInstance().getEnvironment();
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
		final WEnvironment env = WApplication.getInstance().getEnvironment();
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

	protected void render(EnumSet<RenderFlag> flags) {
		if (this.mask_.length() != 0 && !this.javaScriptDefined_) {
			this.defineJavaScript();
		}
		super.render(flags);
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"WLineEdit",
				"function(K,c,g,i,m,s,k,t){function n(){y.trigger(\"input\")}function j(){if(g===\"\")return c.value;var a=c.value,b=\"\",d=\"\",f=0,h=0;for(f=0;f<a.length;f++){d=a.charAt(f);if(d!==k&&g.charAt(f)!==\"_\")h+=1;if(d!==k||g.charAt(f)===\"_\")b+=d}return h>0?b:\"\"}function u(a){return g.charAt(a)===\"_\"}function l(a){e.setSelectionRange(c,a,a+1)}function o(a){for(;u(a);)a++;l(a);return a}function z(a){for(;a>0&&u(a);)a--;l(a);return a}function A(a,b){if(b>=g.length)return false; switch(g.charAt(b)){case \"a\":case \"A\":return a>=v&&a<=B||a>=w&&a<=C;case \"n\":case \"N\":return a>=v&&a<=B||a>=w&&a<=C||a>=p&&a<=q;case \"X\":case \"x\":return true;case \"0\":case \"9\":return a>=p&&a<=q;case \"d\":case \"D\":return a>=D&&a<=q;case \"#\":return a>=p&&a<=q||a===M||a===N;case \"h\":case \"H\":return a>=w&&a<=O||a>=p&&a<=q||a>=v&&a<=P;case \"b\":case \"B\":return a===p||a===D}return false}function r(a){var b=c.value.substring(0,a.start),d=c.value.substring(a.end),f=i.substring(a.start,a.end);c.value=b+f+d; e.setSelectionRange(c,a.start,a.start)}function x(a,b,d){var f=b=b;if(!d&&i.charAt(b)!==a&&!A(a.charCodeAt(0),b)&&b-1>=0&&i.charAt(b-1)===a)return b;for(;i.charAt(b)!==a&&!A(a.charCodeAt(0),b)&&b<g.length;)b++;if(b===g.length)return f;d=c.value.substring(0,b);f=c.value.substring(b+1);if(i.charAt(b)!==a)if(s.charAt(b)===\">\")a=a.toUpperCase();else if(s.charAt(b)===\"<\")a=a.toLowerCase();c.value=d+a+f;b++;return b}function E(a){if(!(g===\"\"||c.readOnly)){e.cancelEvent(a,e.CancelDefaultAction);var b=j(), d=e.getSelectionRange(c);d.start!==d.end&&r(d);var f=undefined;if(window.clipboardData&&window.clipboardData.getData)f=window.clipboardData.getData(\"Text\");else if(a.clipboardData&&a.clipboardData.getData)f=a.clipboardData.getData(\"text/plain\");else{c.value!==b&&n();return}a=\"\";var h=0;for(d=d.start;h<f.length;h++){a=f.charAt(h);d=x(a,d,true)}o(d);b!==j()&&n()}}function F(a){if(!(g===\"\"||c.readOnly)){e.cancelEvent(a,e.CancelDefaultAction);var b=j(),d=e.getSelectionRange(c);if(d.start!==d.end){var f= c.value.substring(d.start,d.end);if(window.clipboardData&&window.clipboardData.setData)window.clipboardData.setData(\"Text\",f);else a.clipboardData&&a.clipboardData.setData&&a.clipboardData.setData(\"text/plain\",f);r(d)}b!==j()&&n()}}function G(){if(!(g===\"\"||c.readOnly))if(c.value===\"\"){c.value=i;o(0)}}var v=\"a\".charCodeAt(0),P=\"f\".charCodeAt(0),B=\"z\".charCodeAt(0),w=\"A\".charCodeAt(0),O=\"F\".charCodeAt(0),C=\"Z\".charCodeAt(0),M=\"-\".charCodeAt(0),N=\"+\".charCodeAt(0),p=\"0\".charCodeAt(0),D=\"1\".charCodeAt(0), q=\"9\".charCodeAt(0);jQuery.data(c,\"lobj\",this);var H=this,e=K.WT,y=$(c);this.getValue=j;this.setValue=function(a){m=a;if(g===\"\")c.value=a;else if(!(t&1)&&!e.hasFocus(c))c.value=this.getValue();else{var b=e.getSelectionRange(c),d=-1;c.value=i;for(var f=0,h=0,I=\"\";f<a.length;f++){I=a.charAt(f);if(b.start===b.end&&b.start===f)d=h;h=x(I,h,true)}if(e.hasFocus(c))if(d!==-1)l(d);else a.length==0&&l(0)}};this.setInputMask=function(a,b,d,f,h){g=a;i=b;s=f;k=h;H.setValue(d)};g!==\"\"&&this.setInputMask(g,i,m, s,k);this.keyDown=function(a,b){if(!(g===\"\"||c.readOnly)){a=j();switch(b.keyCode){case 39:e.cancelEvent(b,e.CancelDefaultAction);b=e.getSelectionRange(c);b.end-b.start<=1?o(b.start+1):e.setSelectionRange(c,b.end,b.end);break;case 37:e.cancelEvent(b,e.CancelDefaultAction);b=e.getSelectionRange(c);b.end-b.start<=1?z(b.start-1):e.setSelectionRange(c,b.start,b.start);break;case 36:e.cancelEvent(b,e.CancelDefaultAction);o(0);break;case 35:e.cancelEvent(b,e.CancelDefaultAction);e.setSelectionRange(c,g.length, g.length);break;case 46:e.cancelEvent(b,e.CancelDefaultAction);b=e.getSelectionRange(c);if(b.end-b.start<=1){b=b.start;if(b<g.length&&!u(b)){var d=c.value.substring(0,b),f=c.value.substring(b+1);c.value=d+k+f;e.setSelectionRange(c,b,b)}}else r(b);break;case 8:e.cancelEvent(b,e.CancelDefaultAction);b=e.getSelectionRange(c);if(b.end-b.start<=1){b=b.start-1;if(b>=0){b=z(b);if(!u(b)){d=c.value.substring(0,b);f=c.value.substring(b+1);c.value=d+k+f;l(b)}}}else r(b);break}a!==j()&&n()}};this.keyPressed= function(a,b){if(!(g===\"\"||c.readOnly)){a=j();var d=b.charCode||b.keyCode;if(!(d===0||d===13||d===10)){e.cancelEvent(b,e.CancelDefaultAction);b=e.getSelectionRange(c);b.start<b.end&&r(b);b=x(String.fromCharCode(d),b.start);o(b);a!==j()&&n()}}};var J=this.getValue();this.focussed=function(){if(!(g===\"\"||c.readOnly||t&1)){J=this.getValue();setTimeout(function(){H.setValue(m)},0)}};this.blurred=function(){if(!(g===\"\"||c.readOnly||t&1)){m=c.value;c.value=this.getValue();c.value!==J&&y.change()}};this.clicked= function(){if(!(g===\"\"||c.readOnly)){var a=e.getSelectionRange(c);a.start===a.end&&l(a.start)}};if(c.addEventListener)c.addEventListener(\"paste\",E,false);else c.attachEvent&&c.attachEvent(\"onpaste\",E);if(c.addEventListener)c.addEventListener(\"cut\",F,false);else c.attachEvent&&c.attachEvent(\"oncut\",F);if(c.addEventListener)c.addEventListener(\"input\",G,false);else c.attachEvent&&c.attachEvent(\"oninput\",G);c.wtEncodeValue=function(){return g===\"\"||t&1||e.hasFocus(c)?c.value:m}}");
	}
}
