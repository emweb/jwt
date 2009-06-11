package eu.webtoolkit.jwt;


/**
 * Enumeration that indiciates a standard icon.
 * 
 * <p>
 * <i><b>Note:</b>Not used yet. </i>
 * </p>
 */
public enum Icon {
	/**
	 * No icon.
	 */
	NoIcon(0),
	/**
	 * An information icon <i>(not implemented)</i>.
	 */
	Information(1),
	/**
	 * An warning icon <i>(not implemented)</i>.
	 */
	Warning(2),
	/**
	 * An critical icon <i>(not implemented)</i>.
	 */
	Critical(3),
	/**
	 * An question icon <i>(not implemented)</i>.
	 */
	Question(4);

	private int value;

	Icon(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
