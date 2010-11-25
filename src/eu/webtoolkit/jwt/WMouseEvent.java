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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import eu.webtoolkit.jwt.servlet.WebRequest;

/**
 * A class providing details for a mouse event.
 * <p>
 * 
 * @see WInteractWidget#clicked()
 * @see WInteractWidget#doubleClicked()
 * @see WInteractWidget#mouseWentDown()
 * @see WInteractWidget#mouseWentUp()
 * @see WInteractWidget#mouseWentOver()
 * @see WInteractWidget#mouseMoved()
 */
public class WMouseEvent implements WAbstractEvent {
	/**
	 * Default constructor.
	 */
	public WMouseEvent() {
		super();
		this.jsEvent_ = new JavaScriptEvent();
	}

	/**
	 * Enumeration for the mouse button.
	 */
	public enum Button {
		/**
		 * No button.
		 */
		NoButton(0),
		/**
		 * Left button.
		 */
		LeftButton(1),
		/**
		 * Middle button.
		 */
		MiddleButton(2),
		/**
		 * Right button.
		 */
		RightButton(4);

		private int value;

		Button(int value) {
			this.value = value;
		}

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return value;
		}
	}

	/**
	 * Returns the button.
	 * <p>
	 * If multiple buttons are currently pressed for a mouse moved or mouse
	 * dragged event, then the one with the smallest numerical value is
	 * returned.
	 */
	public WMouseEvent.Button getButton() {
		return WMouseEvent.Button.values()[this.jsEvent_.button];
	}

	/**
	 * Returns keyboard modifiers.
	 * <p>
	 * The result is a logical OR of {@link KeyboardModifier KeyboardModifier}
	 * flags.
	 */
	public EnumSet<KeyboardModifier> getModifiers() {
		return this.jsEvent_.modifiers;
	}

	/**
	 * Returns the mouse position relative to the document.
	 */
	public Coordinates getDocument() {
		return new Coordinates(this.jsEvent_.documentX, this.jsEvent_.documentY);
	}

	/**
	 * Returns the mouse position relative to the window.
	 * <p>
	 * This differs from documentX() only through scrolling through the
	 * document.
	 */
	public Coordinates getWindow() {
		return new Coordinates(this.jsEvent_.clientX, this.jsEvent_.clientY);
	}

	/**
	 * Returns the mouse position relative to the screen.
	 */
	public Coordinates getScreen() {
		return new Coordinates(this.jsEvent_.screenX, this.jsEvent_.screenY);
	}

	/**
	 * Returns the mouse position relative to the widget.
	 */
	public Coordinates getWidget() {
		return new Coordinates(this.jsEvent_.widgetX, this.jsEvent_.widgetY);
	}

	/**
	 * Returns the distance over which the mouse has been dragged.
	 * <p>
	 * This is only defined for a {@link WInteractWidget#mouseWentUp()
	 * WInteractWidget#mouseWentUp()} event.
	 */
	public Coordinates getDragDelta() {
		return new Coordinates(this.jsEvent_.dragDX, this.jsEvent_.dragDY);
	}

	/**
	 * Returns the scroll wheel delta.
	 * <p>
	 * This is 1 when wheel was scrolled up or -1 when wheel was scrolled down.
	 * <p>
	 * This is only defined for a {@link WInteractWidget#mouseWheel()
	 * WInteractWidget#mouseWheel()} event.
	 */
	public int getWheelDelta() {
		return this.jsEvent_.wheelDelta;
	}

	public WAbstractEvent createFromJSEvent(JavaScriptEvent jsEvent) {
		return new WMouseEvent(jsEvent);
	}

	WMouseEvent(JavaScriptEvent jsEvent) {
		super();
		this.jsEvent_ = jsEvent;
	}

	JavaScriptEvent jsEvent_;

	static int asInt(String v) {
		return Integer.parseInt(v);
	}

	static int parseIntParameter(WebRequest request, String name, int ifMissing) {
		String p;
		if ((p = request.getParameter(name)) != null) {
			try {
				return asInt(p);
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

	static void decodeTouches(String str, List<Touch> result) {
		if (str.length() == 0) {
			return;
		}
		List<String> s = new ArrayList<String>();
		s = new ArrayList<String>(Arrays.asList(str.split(";")));
		if (s.size() % 9 != 0) {
			WApplication.getInstance().log("error").append(
					"Could not parse touches array '").append(str).append("'");
			return;
		}
		try {
			for (int i = 0; i < s.size(); i += 9) {
				result.add(new Touch(asInt(s.get(i + 0)), asInt(s.get(i + 1)),
						asInt(s.get(i + 2)), asInt(s.get(i + 3)), asInt(s
								.get(i + 4)), asInt(s.get(i + 5)), asInt(s
								.get(i + 6)), asInt(s.get(i + 7)), asInt(s
								.get(i + 8))));
			}
		} catch (NumberFormatException ee) {
			WApplication.getInstance().log("error").append(
					"Could not parse touches array '").append(str).append("'");
			return;
		}
	}

	static WMouseEvent templateEvent = new WMouseEvent();
}
