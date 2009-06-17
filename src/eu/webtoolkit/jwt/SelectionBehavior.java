package eu.webtoolkit.jwt;


/**
 * Enumeration that indicates what is being selected.
 * <p>
 * 
 * @see WTreeView#setSelectionBehavior(SelectionBehavior behavior)
 */
public enum SelectionBehavior {
	/**
	 * Select single items.
	 */
	SelectItems(0),
	/**
	 * Select only rows.
	 */
	SelectRows(1);

	private int value;

	SelectionBehavior(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
