/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.EnumSet;
import eu.webtoolkit.jwt.servlet.WebRequest;
import eu.webtoolkit.jwt.utils.EnumUtils;

/**
 * A class providing details for a keyboard event.
 * <p>
 * 
 * A key event is associated with the {@link WInteractWidget#keyWentDown()},
 * {@link WInteractWidget#keyWentUp()} and {@link WInteractWidget#keyPressed()}
 * signals.
 */
public class WKeyEvent implements WAbstractEvent {
	/**
	 * Default constructor.
	 */
	public WKeyEvent() {
		super();
		this.jsEvent_ = new JavaScriptEvent();
	}

	/**
	 * Returns the key code key that was pressed or released.
	 * <p>
	 * The key code corresponds to the actual key on the keyboard, rather than
	 * the generated character.
	 * <p>
	 * All three types of key events provide this information.
	 * <p>
	 * 
	 * @see WKeyEvent#getModifiers()
	 * @see WKeyEvent#getCharCode()
	 */
	public Key getKey() {
		int key = this.jsEvent_.keyCode;
		if (key == 0) {
			key = this.jsEvent_.charCode;
		}
		return EnumUtils.keyFromValue(key);
	}

	/**
	 * Returns keyboard modifiers.
	 * <p>
	 * The result is a logical OR of {@link KeyboardModifier KeyboardModifier}
	 * flags.
	 * <p>
	 * All three types of key events provide this information.
	 * <p>
	 * 
	 * @see WKeyEvent#getKey()
	 * @see WKeyEvent#getCharCode()
	 */
	public EnumSet<KeyboardModifier> getModifiers() {
		return this.jsEvent_.modifiers;
	}

	/**
	 * Returns the unicode character code.
	 * <p>
	 * This is only defined for a {@link WInteractWidget#keyPressed()
	 * keyPressed} event, and returns the unicode character code of a character
	 * that is entered.
	 * <p>
	 * For the {@link WInteractWidget#keyWentDown() keyWentDown} and
	 * {@link WInteractWidget#keyWentUp() keyWentUp} events, &apos;0&apos; is
	 * returned.
	 * <p>
	 * The {@link WKeyEvent#getCharCode()} may be different from
	 * {@link WKeyEvent#getKey()}. For example, a {@link Key#Key_M Key_M} key
	 * may correspond to &apos;m&apos; or &apos;M&apos; character, depending on
	 * whether the shift key is pressed simultaneously.
	 * <p>
	 * 
	 * @see WKeyEvent#getKey()
	 * @see WKeyEvent#getText()
	 */
	public int getCharCode() {
		return this.jsEvent_.charCode != 0 ? this.jsEvent_.charCode
				: this.jsEvent_.keyCode;
	}

	/**
	 * The (unicode) text that this key generated.
	 * <p>
	 * This is only defined for a {@link WInteractWidget#keyPressed()
	 * keyPressed} event, and returns a string that holds exactly one unicode
	 * character, which corresponds to {@link WKeyEvent#getCharCode()}.
	 * <p>
	 * For the {@link WInteractWidget#keyWentDown() keyWentDown} and
	 * {@link WInteractWidget#keyWentUp() keyWentUp} events, an empty string is
	 * returned.
	 * <p>
	 * 
	 * @see WKeyEvent#getCharCode()
	 */
	public String getText() {
		return "" + (char) this.getCharCode();
	}

	public WAbstractEvent createFromJSEvent(JavaScriptEvent jsEvent) {
		return new WKeyEvent(jsEvent);
	}

	public WKeyEvent(JavaScriptEvent jsEvent) {
		super();
		this.jsEvent_ = jsEvent;
	}

	private JavaScriptEvent jsEvent_;

	static int parseIntParameter(WebRequest request, String name, int ifMissing) {
		String p;
		if ((p = request.getParameter(name)) != null) {
			try {
				return Integer.parseInt(p);
			} catch (NumberFormatException ee) {
				WApplication.getInstance().log("error").append(
						"Could not cast event property '").append(name).append(
						": ").append(p).append("' to int");
				return ifMissing;
			}
		} else {
			return ifMissing;
		}
	}

	static String getStringParameter(WebRequest request, String name) {
		String p;
		if ((p = request.getParameter(name)) != null) {
			return p;
		} else {
			return "";
		}
	}

	static WKeyEvent templateEvent = new WKeyEvent();
}
