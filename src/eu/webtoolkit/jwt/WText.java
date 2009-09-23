/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.EnumSet;
import eu.webtoolkit.jwt.utils.StringUtils;

/**
 * A widget that renders (XHTML) text
 * <p>
 * 
 * The text is provided through a {@link WString}, which may either hold a
 * literal text, or a key to localized text which is looked up in locale
 * dependent XML files (see {@link WString#tr(String key) WString#tr()}).
 * <p>
 * Use {@link WText#setTextFormat(TextFormat textFormat) setTextFormat()} to
 * configure the textFormat of the text. The default textFormat is
 * Wt::XHMTLText, which allows XHMTL markup to be included in the text. Tags and
 * attributes that indicate &quot;active&quot; content are not allowed and
 * stripped out, to avoid security risks exposed by JavaScript such as the
 * common web-based <a
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
 * The widget corresponds to an HTML <code>&lt;span&gt;</code> tag or an HTML
 * <code>&lt;div&gt;</code> depending on whether the widget is
 * {@link WWidget#setInline(boolean inlined) inline}. WText is by default
 * inline, unless the XHTML contents starts with an element such as
 * <code>&lt;div&gt;</code>, <code>&lt;h&gt;</code> or <code>&lt;p&gt;</code>
 * that is displayed as a block, in which case the widget will also display as a
 * block.
 * <p>
 * 
 * @see WApplication#setLocale(Locale locale)
 */
public class WText extends WInteractWidget {
	/**
	 * Construct a text widget with an empty text.
	 */
	public WText(WContainerWidget parent) {
		super(parent);
		this.text_ = new WString();
		this.textFormat_ = TextFormat.XHTMLText;
		this.wordWrap_ = true;
		this.textChanged_ = false;
		this.wordWrapChanged_ = false;
		;
	}

	/**
	 * Construct a text widget with an empty text.
	 * <p>
	 * Calls {@link #WText(WContainerWidget parent)
	 * this((WContainerWidget)null)}
	 */
	public WText() {
		this((WContainerWidget) null);
	}

	/**
	 * Construct a text widget with given text.
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
		;
		this.setText(text);
	}

	/**
	 * Construct a text widget with given text.
	 * <p>
	 * Calls {@link #WText(CharSequence text, WContainerWidget parent)
	 * this(text, (WContainerWidget)null)}
	 */
	public WText(CharSequence text) {
		this(text, (WContainerWidget) null);
	}

	/**
	 * Construct a text widget with given text and format.
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
		;
		this.setText(text);
	}

	/**
	 * Construct a text widget with given text and format.
	 * <p>
	 * Calls
	 * {@link #WText(CharSequence text, TextFormat format, WContainerWidget parent)
	 * this(text, format, (WContainerWidget)null)}
	 */
	public WText(CharSequence text, TextFormat format) {
		this(text, format, (WContainerWidget) null);
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
	 * Set the text.
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
		this.autoAdjustInline();
		return textok;
	}

	/**
	 * Set the textFormat.
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
	 * Returns the textFormat.
	 * <p>
	 * 
	 * @see WText#setTextFormat(TextFormat textFormat)
	 */
	public TextFormat getTextFormat() {
		return this.textFormat_;
	}

	/**
	 * Configure word wrapping.
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

	public void refresh() {
		if (this.text_.refresh()) {
			this.textChanged_ = true;
			this.autoAdjustInline();
			this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		}
		super.refresh();
	}

	private WString text_;
	private TextFormat textFormat_;
	private boolean wordWrap_;
	private boolean textChanged_;
	private boolean wordWrapChanged_;

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
