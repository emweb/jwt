package eu.webtoolkit.jwt;


/**
 * Enumeration that indicates the text format.
 * 
 * @see WText#setTextFormat(TextFormat textFormat)
 */
public enum TextFormat {
	/**
	 * Format text as XSS-safe XHTML markup&apos;ed text.
	 */
	XHTMLText,
	/**
	 * Format text as XHTML markup&apos;ed text.
	 */
	XHTMLUnsafeText,
	/**
	 * Format text as plain text.
	 */
	PlainText;

	public int getValue() {
		return ordinal();
	}
}
