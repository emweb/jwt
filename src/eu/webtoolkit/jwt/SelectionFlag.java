package eu.webtoolkit.jwt;


/**
 * Enumeration that indicates how to change a selection.
 * 
 * @see WTreeView#select(WModelIndex index, SelectionFlag option)
 */
public enum SelectionFlag {
	/**
	 * Add to selection.
	 */
	Select(1),
	/**
	 * Remove from selection.
	 */
	Deselect(2),
	/**
	 * Toggle in selection.
	 */
	ToggleSelect(3),
	/**
	 * Clear selection and add single item.
	 */
	ClearAndSelect(4);

	private int value;

	SelectionFlag(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
