package eu.webtoolkit.jwt;


/**
 * Enumeration that specifies where the target of an anchor should be displayed.
 * 
 * @see WAnchor#setTarget(AnchorTarget target)
 */
public enum AnchorTarget {
	/**
	 * Show Instead of the application.
	 */
	TargetSelf,
	/**
	 * Show in the top level frame of the application window.
	 */
	TargetThisWindow,
	/**
	 * Show in a separate new tab or window.
	 */
	TargetNewWindow;

	public int getValue() {
		return ordinal();
	}
}
