/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
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

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
