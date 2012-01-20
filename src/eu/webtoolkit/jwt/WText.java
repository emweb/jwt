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
	private static Logger logger = LoggerFactory.getLogger(WText.class);

	/**
	 * Creates a text widget with an empty text.
	 */
	public WText(WContainerWidget parent) {
		super(parent);
		this.text_ = new WString();
		this.textFormat_ = TextFormat.XHTMLText;
		this.flags_ = new BitSet();
		this.padding_ = null;
		this.flags_.set(BIT_WORD_WRAP);
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
		this.flags_ = new BitSet();
		this.padding_ = null;
		this.flags_.set(BIT_WORD_WRAP);
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
		this.flags_ = new BitSet();
		this.padding_ = null;
		this.flags_.set(BIT_WORD_WRAP);
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
		this.flags_.set(BIT_TEXT_CHANGED);
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
		if (this.flags_.get(BIT_WORD_WRAP) != wordWrap) {
			this.flags_.set(BIT_WORD_WRAP, wordWrap);
			this.flags_.set(BIT_WORD_WRAP_CHANGED);
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
		return this.flags_.get(BIT_WORD_WRAP);
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
		if (!EnumUtils.mask(sides, Side.Right).isEmpty()) {
			this.padding_[0] = length;
		}
		if (!EnumUtils.mask(sides, Side.Left).isEmpty()) {
			this.padding_[1] = length;
		}
		if (!EnumUtils.mask(sides, Side.Top).isEmpty()) {
			logger.error(new StringWriter().append(
					"setPadding(..., Top) is not supported.").toString());
		}
		if (!EnumUtils.mask(sides, Side.Bottom).isEmpty()) {
			logger.error(new StringWriter().append(
					"setPadding(..., Bottom) is not supported.").toString());
		}
		this.flags_.set(BIT_PADDINGS_CHANGED);
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
			logger.error(new StringWriter().append(
					"padding(Top) is not supported.").toString());
			return new WLength();
		case Right:
			return this.padding_[1];
		case Bottom:
			logger.error(new StringWriter().append(
					"padding(Bottom) is not supported.").toString());
		case Left:
			return this.padding_[3];
		default:
			logger.error(new StringWriter().append(
					"padding(Side) with invalid side: ").append(
					String.valueOf((int) side.getValue())).toString());
			return new WLength();
		}
	}

	/**
	 * Enables internal path encoding of anchors in the XHTML text.
	 * <p>
	 * Anchors to internal paths are represented differently depending on the
	 * session implementation (plain HTML, Ajax or HTML5 history). By enabling
	 * this option, anchors which reference an internal path (by referring a URL
	 * of the form <code>href=&quot;#/...&quot;</code>), are re-encoded to link
	 * to the internal path.
	 * <p>
	 * When using {@link TextFormat#XHTMLText} (or
	 * {@link TextFormat#XHTMLUnsafeText}) formatted text, the text is pasted
	 * verbatim in the browser (with the exception of XSS filtering if
	 * applicable). With this option, however, the XHTML text may be transformed
	 * at the cost of an additional XML parsing step.
	 * <p>
	 * The default value is <code>false</code>.
	 * <p>
	 */
	public void setInternalPathEncoding(boolean enabled) {
		if (this.flags_.get(BIT_ENCODE_INTERNAL_PATHS) != enabled) {
			this.flags_.set(BIT_ENCODE_INTERNAL_PATHS, enabled);
			this.flags_.set(BIT_TEXT_CHANGED);
		}
	}

	/**
	 * Returns whether internal paths are encoded.
	 * <p>
	 * 
	 * @see WText#setInternalPathEncoding(boolean enabled)
	 */
	public boolean hasInternalPathEncoding() {
		return this.flags_.get(BIT_ENCODE_INTERNAL_PATHS);
	}

	public void refresh() {
		if (this.text_.refresh()) {
			this.flags_.set(BIT_TEXT_CHANGED);
			this.repaint(EnumSet.of(RepaintFlag.RepaintInnerHtml));
		}
		super.refresh();
	}

	private WString text_;
	private TextFormat textFormat_;
	private static final int BIT_WORD_WRAP = 0;
	private static final int BIT_TEXT_CHANGED = 1;
	private static final int BIT_WORD_WRAP_CHANGED = 2;
	private static final int BIT_PADDINGS_CHANGED = 3;
	private static final int BIT_ENCODE_INTERNAL_PATHS = 4;
	BitSet flags_;

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
			WApplication app = WApplication.getInstance();
			if (this.flags_.get(BIT_ENCODE_INTERNAL_PATHS)
					|| app.getSession().hasSessionIdInUrl()) {
				EnumSet<RefEncoderOption> options = EnumSet
						.noneOf(RefEncoderOption.class);
				if (this.flags_.get(BIT_ENCODE_INTERNAL_PATHS)) {
					options.add(RefEncoderOption.EncodeInternalPaths);
				}
				if (app.getSession().hasSessionIdInUrl()) {
					options.add(RefEncoderOption.EncodeRedirectTrampoline);
				}
				WString result = this.text_;
				RefEncoder.EncodeRefs(result, options);
				return result.toString();
			} else {
				return this.text_.toString();
			}
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

	protected void render(EnumSet<RenderFlag> flags) {
		if (this.flags_.get(BIT_TEXT_CHANGED)) {
			this.autoAdjustInline();
		}
		super.render(flags);
	}

	void updateDom(DomElement element, boolean all) {
		if (this.flags_.get(BIT_TEXT_CHANGED) || all) {
			String text = this.getFormattedText();
			if (this.flags_.get(BIT_TEXT_CHANGED) || text.length() != 0) {
				element.setProperty(Property.PropertyInnerHTML, this
						.getFormattedText());
			}
			this.flags_.clear(BIT_TEXT_CHANGED);
		}
		if (this.flags_.get(BIT_WORD_WRAP_CHANGED) || all) {
			if (!all || !this.flags_.get(BIT_WORD_WRAP)) {
				element.setProperty(Property.PropertyStyleWhiteSpace,
						this.flags_.get(BIT_WORD_WRAP) ? "normal" : "nowrap");
			}
			this.flags_.clear(BIT_WORD_WRAP_CHANGED);
		}
		if (this.flags_.get(BIT_PADDINGS_CHANGED) || all
				&& this.padding_ != null
				&& !(this.padding_[0].isAuto() && this.padding_[1].isAuto())) {
			element.setProperty(Property.PropertyStylePaddingRight,
					this.padding_[0].getCssText());
			element.setProperty(Property.PropertyStylePaddingLeft,
					this.padding_[1].getCssText());
			this.flags_.clear(BIT_PADDINGS_CHANGED);
		}
		super.updateDom(element, all);
	}

	DomElementType getDomElementType() {
		return this.isInline() ? DomElementType.DomElement_SPAN
				: DomElementType.DomElement_DIV;
	}

	void propagateRenderOk(boolean deep) {
		this.flags_.clear(BIT_TEXT_CHANGED);
		this.flags_.clear(BIT_WORD_WRAP_CHANGED);
		this.flags_.clear(BIT_PADDINGS_CHANGED);
		super.propagateRenderOk(deep);
	}
}
