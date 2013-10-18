/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class providing details for a keyboard event.
 * <p>
 * 
 * A key event is associated with the {@link WInteractWidget#keyWentDown()
 * WInteractWidget#keyWentDown()}, {@link WInteractWidget#keyWentUp()
 * WInteractWidget#keyWentUp()} and {@link WInteractWidget#keyPressed()
 * WInteractWidget#keyPressed()} signals.
 */
public class WKeyEvent implements WAbstractEvent {
	private static Logger logger = LoggerFactory.getLogger(WKeyEvent.class);

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
	 * keyPressed} event, and returns the unicode character code point of a
	 * character that is entered.
	 * <p>
	 * For the {@link WInteractWidget#keyWentDown() keyWentDown} and
	 * {@link WInteractWidget#keyWentUp() keyWentUp} events, &apos;0&apos; is
	 * returned.
	 * <p>
	 * The {@link WKeyEvent#getCharCode() getCharCode()} may be different from
	 * {@link WKeyEvent#getKey() getKey()}. For example, a {@link Key#Key_M
	 * Key_M} key may correspond to &apos;m&apos; or &apos;M&apos; character,
	 * depending on whether the shift key is pressed simultaneously.
	 * <p>
	 * 
	 * @see WKeyEvent#getKey()
	 * @see WKeyEvent#getText()
	 */
	public int getCharCode() {
		return this.jsEvent_.charCode;
	}

	/**
	 * The (unicode) text that this key generated.
	 * <p>
	 * This is only defined for a {@link WInteractWidget#keyPressed()
	 * keyPressed} event, and returns a string that holds exactly one unicode
	 * character, which corresponds to {@link WKeyEvent#getCharCode()
	 * getCharCode()}.
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

	static WKeyEvent templateEvent = new WKeyEvent();

	WKeyEvent(JavaScriptEvent jsEvent) {
		super();
		this.jsEvent_ = jsEvent;
	}

	private JavaScriptEvent jsEvent_;

	static String concat(String prefix, int prefixLength, String s2) {
		return prefix + s2;
	}

	static int asInt(String v) {
		return Integer.parseInt(v);
	}

	static int asUInt(String v) {
		return Integer.parseInt(v);
	}

	static int parseIntParameter(WebRequest request, String name, int ifMissing) {
		String p;
		if ((p = request.getParameter(name)) != null) {
			try {
				return asInt(p);
			} catch (NumberFormatException ee) {
				logger.error(new StringWriter().append(
						"Could not cast event property '").append(name).append(
						": ").append(p).append("' to int").toString());
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

	static void decodeTouches(String str, List<Touch> result) {
		if (str.length() == 0) {
			return;
		}
		List<String> s = new ArrayList<String>();
		s = new ArrayList<String>(Arrays.asList(str.split(";")));
		if (s.size() % 9 != 0) {
			logger.error(new StringWriter().append(
					"Could not parse touches array '").append(str).append("'")
					.toString());
			return;
		}
		try {
			for (int i = 0; i < s.size(); i += 9) {
				result.add(new Touch(asUInt(s.get(i + 0)), asInt(s.get(i + 1)),
						asInt(s.get(i + 2)), asInt(s.get(i + 3)), asInt(s
								.get(i + 4)), asInt(s.get(i + 5)), asInt(s
								.get(i + 6)), asInt(s.get(i + 7)), asInt(s
								.get(i + 8))));
			}
		} catch (NumberFormatException ee) {
			logger.error(new StringWriter().append(
					"Could not parse touches array '").append(str).append("'")
					.toString());
			return;
		}
	}
}
