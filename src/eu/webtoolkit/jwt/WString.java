package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.List;

/**
 * A localized string class.
 * <p>
 * JWt uses WString to store text that may be displayed in the browser.
 * <p>
 * A WString can either be a <i>literal</i> string or a <i>localized</i> string. A localized string is resolved using
 * {@link WApplication#getLocalizedStrings()}, taking into account the {@link WApplication#getLocale()}.
 * <p>
 * To create a literal string, use {@link #WString(String)}. To create a localized string you should use {@link WString#tr(String)}
 * or {@link WObject#tr(String)}. The actual value of a string can be obtained using {@link #toString()} or {@link #getValue()}.
 * <p>
 * A WString can substitute place holders by arguments that are specified using the {@link #arg(CharSequence)}, {@link #arg(double)} or
 * {@link #arg(int)} methods. Place holders are numbered: {<i>n</i>} denotes the <i>n</i>'th place holder.
 * 
 * @see WApplication#getLocalizedStrings()
 * @see WApplication#getLocale()
 */
public class WString implements Comparable<WString>, CharSequence {
	public static final WString Empty = new WString();

	private String key;
	private String value;
	private ArrayList<String> arguments;

	/**
	 * Creates an empty string.
	 */
	public WString() {
		this.value = "";
		this.key = null;
		this.arguments = null;
	}

	/**
	 * Creates a literal string.
	 * 
	 * @param s
	 */
	public WString(String s) {
		set(s);
	}

	/**
	 * Creates a literal string.
	 * 
	 * @param s
	 */
	public WString(CharSequence s) {
		set(s.toString());
	}

	/**
	 * Sets a literal string.
	 * 
	 * @param s
	 */
	void set(String s) {
		this.value = s;
		this.key = null;
		this.arguments = null;
	}

	/**
	 * Creates a literal string from a single character.
	 * 
	 * @param c
	 */
	public WString(char c) {
		this(c + "");
	}

	private WString(String key, boolean b) {
		this.value = "";
		this.key = key;
		this.arguments = null;
	}

	/**
	 * Creates a literal string from a char array.
	 * 
	 * @param buf
	 */
	public WString(char[] buf) {
		this(new String(buf));
	}

	public boolean equals(WString other) {
		return toString().equals(other.toString());
	}

	/**
	 * Appends a string, converting the string to a literal string if necessary.
	 * 
	 * @param other
	 * @return <code>this</code>
	 */
	public WString append(WString other) {
		return append(other.getValue());
	}

	/**
	 * Appends a string, converting the string to a literal string if necessary.
	 * 
	 * @param other
	 * @return <code>this</code>
	 */
	public WString append(String other) {
		makeLiteral();
		value += other;
		return this;
	}

	/**
	 * Appends a string, converting the string to a literal string if necessary.
	 * 
	 * @param buf
	 * @return <code>this</code>
	 */
	public WString append(char[] buf) {
		return this.append(new String(buf));
	}

	/**
	 * Returns whether the string is empty.
	 * 
	 * @return whether the string is empty.
	 */
	public boolean isEmpty() {
		return getValue().length() == 0;
	}

	/**
	 * Returns the value.
	 * <p>
	 * A localized string is resolved using the {@link WApplication#getLocalizedStrings()}.
	 * <p>
	 * Arguments place holders are substituted with actual arguments. 
	 * 
	 * @return the value.
	 */
	public String getValue() {
		String result = value;

		if (key != null)
			result = WApplication.getInstance().getLocalizedStrings().resolveKey(key);

		if (arguments != null) {
			for (int i = 0; i < arguments.size(); ++i) {
				String key = '{' + String.valueOf(i + 1) + '}';
				String arg = arguments.get(i);
				if (arg == null)
					arg = "null";
				result = result.replace(key, arg);
			}
		}

		return result;
	}

	public int compareTo(WString arg0) {
		return getValue().compareTo(arg0.getValue());
	}

	/**
	 * Creates a localized string.
	 * 
	 * @param key the key which is used to resolve within a locale.
	 * @return the localized string.
	 * 
	 * @see WLocalizedStrings#resolveKey(String)
	 */
	public static WString tr(String key) {
		return new WString(key, true);
	}

	/**
	 * Returns whether the string is literal.
	 * 
	 * @return whether the string is literal (versus localized).
	 */
	public boolean isLiteral() {
		return key == null;
	}

	/**
	 * Returns the key for a localized string.
	 * 
	 * @return the key, or <code>null</code> if the string is literal.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Substitutes the next positional argument with a string value.
	 * <p>
	 * In the string, the <i>n</i>-th argument is referred to as using {<i>n</i>}.
	 * <p>
	 * For example: the string "<tt>{1} bought {2} apples in the shop.</tt>" with first argument value "<tt>Bart</tt>"
	 * and second argument value <tt>5</tt> becomes: "<tt>Bart bought 5 apples in the shop.</tt>"
	 * 
	 * @param value the argument.
	 * @return <code>this</code>
	 */
	public WString arg(CharSequence value) {
		if (arguments == null)
			arguments = new ArrayList<String>();
		arguments.add(value == null ? "" : value.toString());

		return this;
	}

	/**
	 * Substitutes the next positional argument with an integer value.
	 * <p>
	 * In the string, the <i>n</i>-th argument is referred to as using {<i>n</i>}.
	 * <p>
	 * For example: the string "<tt>{1} bought {2} apples in the shop.</tt>" with first argument value "<tt>Bart</tt>"
	 * and second argument value <tt>5</tt> becomes: "<tt>Bart bought 5 apples in the shop.</tt>"
	 * 
	 * @param value the argument.
	 * @return <code>this</code>
	 */
	public WString arg(int value) {
		if (arguments == null)
			arguments = new ArrayList<String>();
		arguments.add(String.valueOf(value));

		return this;
	}

	/**
	 * Substitutes the next positional argument with a double value.
	 * <p>
	 * In the string, the <i>n</i>-th argument is referred to as using {<i>n</i>}.
	 * <p>
	 * For example: the string "<tt>{1} bought {2} apples in the shop.</tt>" with first argument value "<tt>Bart</tt>"
	 * and second argument value <tt>5</tt> becomes: "<tt>Bart bought 5 apples in the shop.</tt>"
	 * 
	 * @param value the argument.
	 * @return <code>this</code>
	 */
	public WString arg(double value) {
		if (arguments == null)
			arguments = new ArrayList<String>();
		arguments.add(String.valueOf(value));

		return this;
	}

	/**
	 * Returns the arguments.
	 * 
	 * @see WString#arg(CharSequence)
	 */
	public List<String> getArgs() {
		return arguments == null ? stArguments : arguments;
	}

	/**
	 * Refreshes the (localized) strings.
	 * <p>
	 * The localized string is resolved again using {@link WApplication#getLocalizedStrings()}.
	 * 
	 * @return whether the string is (potentially) changed.
	 */
	public boolean refresh() {
		if (isLiteral())
			return false;
		else
			return true;
	}

	/**
	 * Utility method which returns the value as a JavaScript string literal.
	 * <p>
	 * This method escapes the string where needed.
	 * 
	 * @param delimiter the quote delimiter to be used ('\'' or '"')
	 * @return the quoted string.
	 */
	public String getJsStringLiteral(char delimiter) {
		return WWebWidget.jsStringLiteral(getValue(), delimiter);
	}

	/**
	 * Utility method which returns the value as a JavaScript string literal.
	 * <p>
	 * Calls {@link #getJsStringLiteral(char) getJsStringLiteral('\'')}
	 * 
	 * @return the string as a JavaScript string literal.
	 */
	public String getJsStringLiteral() {
		return getJsStringLiteral('\'');
	}

	private void makeLiteral() {
		if (key != null) {
			value = WApplication.getInstance().getLocalizedStrings().resolveKey(key);
			key = null;
		}
	}

	public void changeArg(int argIndex, String value) {
		arguments.set(argIndex, value);
	}

	public void changeArg(int argIndex, int value) {
		this.changeArg(argIndex, value + "");
	}

	public void changeArg(int argIndex, double value) {
		this.changeArg(argIndex, value + "");
	}

	/**
	 * Returns the value of this string.
	 * 
	 * @see #getValue()
	 */
	public String toString() {
		return getValue();
	}

	private static List<String> stArguments;

	/**
	 * Converts a CharSequence to a WString.
	 * <p>
	 * When the char sequence is a WString, it is casted otherwise, a new WString is created.
	 * 
	 * @param charSequence a char sequence.
	 * @return a WString casted or created from the char sequence.
	 */
	public static WString toWString(CharSequence charSequence) {
		return charSequence instanceof WString ? (WString)charSequence : new WString(charSequence);
	}

	/**
	 * Returns the character at a given index.
	 * <p>
	 * <b>It is probably more efficient to call {@link #getValue()} first and then compute on that String.</b>
	 */
	public char charAt(int index) {
		return getValue().charAt(index);
	}

	/**
	 * Returns the string length.
	 * <p>
	 * <b>It is probably more efficient to call {@link #getValue()} first and then compute on that String.</b>
	 */
	public int length() {
		return getValue().length();
	}

	/**
	 * Returns the string length.
	 * 
	 * @return {@link #length()}
	 */
	public int getLength() {
		return length();
	}

	/**
	 * Returns a sub string.
	 * <p>
	 * <b>It is probably more efficient to call {@link #getValue()} first and then compute on that String.</b>
	 */
	public CharSequence subSequence(int start, int end) {
		return getValue().subSequence(start, end);
	}
}
