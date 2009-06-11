package eu.webtoolkit.jwt;


/**
 * Enumeration that specifies an option for rendering a view item.
 * 
 * @see WAbstractItemDelegate#update(WWidget widget, WModelIndex index, EnumSet
 *      flags)
 */
public enum ViewItemRenderFlag {
	/**
	 * Render as selected.
	 */
	RenderSelected,
	/**
	 * Render in editing mode.
	 */
	RenderEditing;

	public int getValue() {
		return ordinal();
	}
}
