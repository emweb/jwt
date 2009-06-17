package eu.webtoolkit.jwt;


/**
 * Enumeration for keyboard modifiers.
 * <p>
 * 
 * @see WMouseEvent#getModifiers()
 * @see WKeyEvent#getModifiers()
 */
public enum KeyboardModifier {
	/**
	 * No modifiers.
	 */
	NoModifier,
	/**
	 * Shift key pressed.
	 */
	ShiftModifier,
	/**
	 * Control key pressed.
	 */
	ControlModifier,
	/**
	 * Alt key pressed.
	 */
	AltModifier,
	/**
	 * Meta key pressed (&quot;Windows&quot; or &quot;Command&quot; (Mac) key).
	 */
	MetaModifier;

	public int getValue() {
		return ordinal();
	}
}
