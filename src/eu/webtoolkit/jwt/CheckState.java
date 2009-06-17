package eu.webtoolkit.jwt;


/**
 * Enumeration for the check state of a check box.
 * <p>
 * 
 * @see WCheckBox
 */
public enum CheckState {
	/**
	 * Unchecked.
	 */
	Unchecked,
	/**
	 * Partially checked (for a tri-state checkbox).
	 */
	PartiallyChecked,
	/**
	 * Checked.
	 */
	Checked;

	public int getValue() {
		return ordinal();
	}
}
