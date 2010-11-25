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

/**
 * A widget that renders (XHTML) text.
 * <p>
 * 
 * The text is provided through a {@link WString}, which may either hold a
 * literal text, or a key to localized text which is looked up in locale
 * dependent XML files (see {@link WString#tr(String key) WString#tr()}).
 * <p>
 * Use {@link WText#setTextFormat(TextFormat textFormat) setTextFormat()} to
 * configure the textFormat of the text. The default textFormat is
 * {@link TextFormat#XHTMLText}, which allows XHMTL markup to be included in the
 * text. Tags and attributes that indicate &quot;active&quot; content are not
 * allowed and stripped out, to avoid security risks exposed by JavaScript such
 * as the common web-based <a
 * href="http://en.wikipedia.org/wiki/Cross_site_scriptingCross-Site">Cross-Site
 * Scripting (XSS)</a> malicious attack. XSS is the situation where one user of
 * your web application is able to execute a script in another user&apos;s
 * browser while your application only intended to display a message entered by
 * the mailicious user to the other user. To defeat this attack, JWt assumes
 * that content in a WText is intended to be passive, and not contain any
 * scripting elements.
 * <p>
 * The {@link TextFormat#PlainText} format will display the text literally
 * (escaping any HTML special characters).
 * <p>
 * In some situations, {@link TextFormat#XHTMLUnsafeText} may be useful to
 * explicitly allow scripting content. Like XHTMLText, it allows XHTML markup,
 * but it also allows potentially dangerous tags and attributes. Use this if
 * you&apos;re sure that a user cannot interfere with the text set, and
 * XHTMLText is too limiting.
 * <p>
 * WText is by default {@link WWidget#setInline(boolean inlined) inline}, unless
 * the XHTML contents starts with an element such as <code>&lt;div&gt;</code>,
 * <code>&lt;h&gt;</code> or <code>&lt;p&gt;</code> that is displayed as a
 * block, in which case the widget will also display as a block.
 * <p>
 * <h3>CSS</h3>
 * <p>
 * The widget corresponds to an HTML <code>&lt;span&gt;</code> tag or an HTML
 * <code>&lt;div&gt;</code> depending on whether the widget is inline.
 * <p>
 * This widget does not provide styling, and can be styled using inline or
 * external CSS as appropriate.
 * <p>
 * 
 * @see WApplication#setLocale(Locale locale)
 */
public class WText extends WInteractWidget {
	/**
	 * Creates a text widget with an empty text.
	 */
	public WText(WContainerWidget parent) {
		super(parent);
		this.text_ = new WString();
		this.textFormat_ = TextFormat.XHTMLText;
		this.wordWrap_ = true;
		this.textChanged_ = false;
		this.wordWrapChanged_ = false;
		this.paddingsChanged_ = false;
		this.padding_ = null;
		;
	}

	/**
	 * Creates a text widget with an empty text.
	 * <p>
	 * Calls {@link #WText(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WText() {
		this((WContainerWidget) null);
	}

	/**
	 * Creates a text widget with given text.
	 * <p>
	 * The textFormat is set to {@link TextFormat#XHTMLText}, unless the
	 * <code>text</code> is literal (not created using
	 * {@link WString#tr(String key) WString#tr()}) and it could not be parsed
	 * as valid XML. In that case the textFormat is set to
	 * {@link TextFormat#PlainText}.
	 * <p>
	 * Therefore, if you wish to use {@link TextFormat#XHTMLText}, but cannot be
	 * sure about <code>text</code> being valid XML, you should verify that the
	 * {@link WText#getTextFormat() getTextFormat()} is
	 * {@link TextFormat#XHTMLText} after construction.
	 * <p>
	 * The XML parser will silently discard malicious tags and attributes for
	 * literal {@link TextFormat#XHTMLText} text.
	 */
	public WText(CharSequence text, WContainerWidget parent) {
		super(parent);
		this.text_ = new WString();
		this.textFormat_ = TextFormat.XHTMLText;
		this.wordWrap_ = true;
		this.textChanged_ = false;
		this.wordWrapChanged_ = false;
		this.paddingsChanged_ = false;
		this.padding_ = null;
		;
		this.setText(text);
	}

	/**
	 * Creates a text widget with given text.
	 * <p>
	 * Calls {@link #WText(CharSequence text, WContainerWidget parent)
	 * this(text, (WContainerWidget)null)}
	 */
	public WText(CharSequence text) {
		this(text, (WContainerWidget) null);
	}

	/**
	 * Creates a text widget with given text and format.
	 * <p>
	 * If <i>textFormat</i> is {@link TextFormat#XHTMLText} and
	 * <code>text</code> is not literal (not created using
	 * {@link WString#tr(String key) WString#tr()}), then if the
	 * <code>text</code> could not be parsed as valid XML, the textFormat is
	 * changed to {@link TextFormat#PlainText}.
	 * <p>
	 * Therefore, if you wish to use {@link TextFormat#XHTMLText}, but cannot be
	 * sure about <code>text</code> being valid XML, you should verify that the
	 * {@link WText#getTextFormat() getTextFormat()} is
	 * {@link TextFormat#XHTMLText} after construction.
	 * <p>
	 * The XML parser will silently discard malicious tags and attributes for
	 * literal {@link TextFormat#XHTMLText} text.
	 */
	public WText(CharSequence text, TextFormat format, WContainerWidget parent) {
		super(parent);
		this.text_ = new WString();
		this.textFormat_ = format;
		this.wordWrap_ = true;
		this.textChanged_ = false;
		this.wordWrapChanged_ = false;
		this.paddingsChanged_ = false;
		this.padding_ = null;
		;
		this.setText(text);
	}

	/**
	 * Creates a text widget with given text and format.
	 * <p>
	 * Calls
	 * {@link #WText(CharSequence text, TextFormat format, WContainerWidget parent)
	 * this(text, format, (WContainerWidget)null)}
	 */
	public WText(CharSequence text, TextFormat format) {
		this(text, format, (WContainerWidget) null);
	}

	/**
	 * Destructor.
	 */
	public void remove() {
		;
		super.remove();
	}

	/**
	 * Returns the text.
	 * <p>
	 * When a literal XHTMLFormatted text was set, this may differ from the text
	 * that was set since malicious tags/attributes may have been stripped.
	 * <p>
	 * 
	 * @see WText#setText(CharSequence text)
	 */
	public WString getText() {
		return this.text_;
	}

	/**
	 * Sets the text.
	 * <p>
	 * When the current format is {@link TextFormat#XHTMLText}, and
	 * <code>text</code> is literal (not created using
	 * {@link WString#tr(String key) WString#tr()}), it is parsed using an XML
	 * parser which discards malicious tags and attributes silently. When the
	 * parser encounters an XML parse error, the textFormat is changed to
	 * {@link TextFormat#PlainText}.
	 * <p>
	 * Returns whether the text could be set using the current textFormat. A
	 * return value of <code>false</code> indicates that the textFormat was
	 * changed in order to be able to accept the new text.
	 * <p>
	 * 
	 * @see WText#getText()
	 * @see WText#setText(CharSequence text)
	 */
	public boolean setText(CharSequence text) {
		if (canOptimizeUpdates() && text.equals(this.text_)) {
			return true;
		}
		this.text_ = WString.toWString(text);
		boolean textok = this.isCheckWellFormed();
		if (!textok) {
			this.textFormat_ = TextFormat.PlainText;
		}
		this.textChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		return textok;
	}

	/**
	 * Sets the text format.
	 * <p>
	 * The textFormat controls how the string should be interpreted: either as
	 * plain text, which is displayed literally, or as XHTML-markup.
	 * <p>
	 * When changing the textFormat to {@link TextFormat#XHTMLText}, and the
	 * current text is literal (not created using {@link WString#tr(String key)
	 * WString#tr()}), the current text is parsed using an XML parser which
	 * discards malicious tags and attributes silently. When the parser
	 * encounters an XML parse error, the textFormat is left unchanged, and this
	 * method returns false.
	 * <p>
	 * Returns whether the textFormat could be set for the current text.
	 * <p>
	 * The default format is {@link TextFormat#XHTMLText}.
	 */
	public boolean setTextFormat(TextFormat textFormat) {
		if (this.textFormat_ != textFormat) {
			TextFormat oldTextFormat = this.textFormat_;
			this.textFormat_ = textFormat;
			boolean textok = this.isCheckWellFormed();
			if (!textok) {
				this.textFormat_ = oldTextFormat;
			}
			return textok;
		} else {
			return true;
		}
	}

	/**
	 * Returns the text format.
	 * <p>
	 * 
	 * @see WText#setTextFormat(TextFormat textFormat)
	 */
	public TextFormat getTextFormat() {
		return this.textFormat_;
	}

	/**
	 * Configures word wrapping.
	 * <p>
	 * When <code>wordWrap</code> is <code>true</code>, the widget may break
	 * lines, creating a multi-line text. When <code>wordWrap</code> is
	 * <code>false</code>, the text will displayed on a single line, unless the
	 * text contains end-of-lines (for {@link TextFormat#PlainText}) or &lt;br
	 * /&gt; tags or other block-level tags (for {@link TextFormat#XHTMLText}).
	 * <p>
	 * The default value is <code>true</code>.
	 * <p>
	 * 
	 * @see WText#isWordWrap()
	 */
	public void setWordWrap(boolean wordWrap) {
		if (this.wordWrap_ != wordWrap) {
			this.wordWrap_ = wordWrap;
			this.wordWrapChanged_ = true;
			this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
		}
	}

	/**
	 * Returns whether the widget may break lines.
	 * <p>
	 * 
	 * @see WText#setWordWrap(boolean wordWrap)
	 */
	public boolean isWordWrap() {
		return this.wordWrap_;
	}

	/**
	 * Sets padding inside the widget.
	 * <p>
	 * Setting padding has the effect of adding distance between the widget
	 * children and the border, for a {@link WText} padding is only supported on
	 * the left and/or right.
	 */
	public void setPadding(WLength length, EnumSet<Side> sides) {
		if (!(this.padding_ != null)) {
			this.padding_ = new WLength[2];
			this.padding_[0] = this.padding_[1] = WLength.Auto;
		}
		if (sides.contains(Side.Right)) {
			this.padding_[0] = length;
		}
		if (sides.contains(Side.Left)) {
			this.padding_[1] = length;
		}
		if (sides.contains(Side.Top)) {
			throw new WtException("WText::padding on Top is not supported.");
		}
		if (sides.contains(Side.Bottom)) {
			throw new WtException("WText::padding on Bottom is not supported.");
		}
		this.paddingsChanged_ = true;
		this.repaint(EnumSet.of(RepaintFlag.RepaintPropertyAttribute));
	}

	/**
	 * Sets padding inside the widget.
	 * <p>
	 * Calls {@link #setPadding(WLength length, EnumSet sides)
	 * setPadding(length, EnumSet.of(side, sides))}
	 */
	public final void setPadding(WLength length, Side side, Side... sides) {
		setPadding(length, EnumSet.of(side, sides));
	}

	/**
	 * Sets padding inside the widget.
	 * <p>
	 * Calls {@link #setPadding(WLength length, EnumSet sides)
	 * setPadding(length, EnumSet.of (Side.Left, Side.Right))}
	 */
	public final void setPadding(WLength length) {
		setPadding(length, EnumSet.of(Side.Left, Side.Right));
	}

	/**
	 * Returns the padding set for the widget.
	 * <p>
	 * 
	 * @see WText#setPadding(WLength length, EnumSet sides)
	 */
	public WLength getPadding(Side side) {
		if (!(this.padding_ != null)) {
			return WLength.Auto;
		}
		switch (side) {
		case Top:
			throw new WtException("WText::padding on Top is not supported.");
		case Right:
			return this.padding_[1];
		case Bottom:
			throw new WtException("WText::padding on Bottom is not supported.");
		case Left:
			return this.padding_[3];
		default:
			throw new WtException("WText::padding(Side) with invalid side.");
		}
	}

	public void refresh() {
		if (this.text_.refresh()) {
			this.textChanged_ = true;
			this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		}
		super.refresh();
	}

	private WString text_;
	private TextFormat textFormat_;
	private boolean wordWrap_;
	private boolean textChanged_;
	private boolean wordWrapChanged_;
	private boolean paddingsChanged_;

	private boolean isCheckWellFormed() {
		if (this.textFormat_ == TextFormat.XHTMLText && this.text_.isLiteral()) {
			return removeScript(this.text_);
		} else {
			return true;
		}
	}

	private String getFormattedText() {
		if (this.textFormat_ == TextFormat.PlainText) {
			return escapeText(this.text_, true).toString();
		} else {
			return this.text_.toString();
		}
	}

	private void autoAdjustInline() {
		if (this.textFormat_ != TextFormat.PlainText && this.isInline()) {
			String t = this.text_.toString();
			t = StringUtils.trimLeft(t);
			if (StringUtils.startsWithIgnoreCase(t, "<div")
					|| StringUtils.startsWithIgnoreCase(t, "<p")
					|| StringUtils.startsWithIgnoreCase(t, "<h")) {
				this.setInline(false);
			}
		}
	}

	private WLength[] padding_;

	void render(EnumSet<RenderFlag> flags) {
		if (this.textChanged_) {
			this.autoAdjustInline();
		}
	}

	void updateDom(DomElement element, boolean all) {
		if (this.textChanged_ || all) {
			String text = this.getFormattedText();
			if (this.textChanged_ || text.length() != 0) {
				element.setProperty(Property.PropertyInnerHTML, this
						.getFormattedText());
			}
			this.textChanged_ = false;
		}
		if (this.wordWrapChanged_ || all) {
			if (!all || !this.wordWrap_) {
				element.setProperty(Property.PropertyStyleWhiteSpace,
						this.wordWrap_ ? "normal" : "nowrap");
			}
			this.wordWrapChanged_ = false;
		}
		if (this.paddingsChanged_ || all && this.padding_ != null
				&& !(this.padding_[0].isAuto() && this.padding_[1].isAuto())) {
			element.setProperty(Property.PropertyStylePaddingRight,
					this.padding_[0].getCssText());
			element.setProperty(Property.PropertyStylePaddingLeft,
					this.padding_[1].getCssText());
			this.paddingsChanged_ = false;
		}
		super.updateDom(element, all);
	}

	DomElementType getDomElementType() {
		return this.isInline() ? DomElementType.DomElement_SPAN
				: DomElementType.DomElement_DIV;
	}

	void propagateRenderOk(boolean deep) {
		this.textChanged_ = false;
		this.wordWrapChanged_ = false;
		super.propagateRenderOk(deep);
	}
}
