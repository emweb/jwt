/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.EnumSet;
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
		 * Left button.
		 */
		LeftButton,
		/**
		 * Middle button.
		 */
		MiddleButton,
		/**
		 * Right button.
		 */
		RightButton;

		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * A mouse coordinate
	 */
	public static class Coordinates {
		/**
		 * X coordinate.
		 */
		public int x;
		/**
		 * Y coordinate.
		 */
		public int y;

		public Coordinates(int x_, int y_) {
			this.x = x_;
			this.y = y_;
		}
	}

	/**
	 * Returns the button.
	 */
	public WMouseEvent.Button getButton() {
		return this.jsEvent_.right ? WMouseEvent.Button.RightButton
				: WMouseEvent.Button.LeftButton;
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
	public WMouseEvent.Coordinates getDocument() {
		return new WMouseEvent.Coordinates(this.jsEvent_.documentX,
				this.jsEvent_.documentY);
	}

	/**
	 * Returns the mouse position relative to the window.
	 * <p>
	 * This differs from documentX() only through scrolling through the
	 * document.
	 */
	public WMouseEvent.Coordinates getWindow() {
		return new WMouseEvent.Coordinates(this.jsEvent_.clientX,
				this.jsEvent_.clientY);
	}

	/**
	 * Returns the mouse position relative to the screen.
	 */
	public WMouseEvent.Coordinates getScreen() {
		return new WMouseEvent.Coordinates(this.jsEvent_.screenX,
				this.jsEvent_.screenY);
	}

	/**
	 * Returns the mouse position relative to the widget.
	 */
	public WMouseEvent.Coordinates getWidget() {
		return new WMouseEvent.Coordinates(this.jsEvent_.widgetX,
				this.jsEvent_.widgetY);
	}

	/**
	 * Returns the distance over which the mouse has been dragged.
	 * <p>
	 * This is only defined for a {@link WInteractWidget#mouseWentUp()
	 * WInteractWidget#mouseWentUp() } event.
	 */
	public WMouseEvent.Coordinates getDragDelta() {
		return new WMouseEvent.Coordinates(this.jsEvent_.dragDX,
				this.jsEvent_.dragDY);
	}

	public WAbstractEvent createFromJSEvent(JavaScriptEvent jsEvent) {
		return new WMouseEvent(jsEvent);
	}

	public WMouseEvent(JavaScriptEvent jsEvent) {
		super();
		this.jsEvent_ = jsEvent;
	}

	protected JavaScriptEvent jsEvent_;

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

	static WMouseEvent templateEvent = new WMouseEvent();
}
