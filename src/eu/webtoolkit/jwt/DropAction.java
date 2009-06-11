package eu.webtoolkit.jwt;


/**
 * Enumeration that indicates a drop action.
 * 
 * @see WAbstractItemModel#dropEvent(WDropEvent e, DropAction action, int row,
 *      int column, WModelIndex parent)
 */
public enum DropAction {
	/**
	 * Copy the selection.
	 */
	CopyAction,
	/**
	 * Move the selection (deleting originals).
	 */
	MoveAction;

	public int getValue() {
		return ordinal();
	}
}
