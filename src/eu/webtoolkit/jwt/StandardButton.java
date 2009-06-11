package eu.webtoolkit.jwt;


/**
 * Enumeration that indicates a standard button.
 * 
 * Multiple buttons may be specified by logically or&apos;ing these values
 * together, e.g. <code>
 Ok | Cancel
</code>
 * <p>
 * 
 * @see WMessageBox
 */
public enum StandardButton {
	/**
	 * No button.
	 */
	NoButton,
	/**
	 * An OK button.
	 */
	Ok,
	/**
	 * A Cancel button.
	 */
	Cancel,
	/**
	 * A Yes button.
	 */
	Yes,
	/**
	 * A No button.
	 */
	No,
	/**
	 * An Abort button.
	 */
	Abort,
	/**
	 * A Retry button.
	 */
	Retry,
	/**
	 * An Ignore button.
	 */
	Ignore,
	/**
	 * A Yes-to-All button.
	 */
	YesAll,
	/**
	 * A No-to-All button.
	 */
	NoAll;

	public int getValue() {
		return ordinal();
	}
}
