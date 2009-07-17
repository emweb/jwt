/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.servlet.WebRequest;

/**
 * A class providing details for a scroll event.
 * <p>
 * 
 * @see WContainerWidget#scrolled()
 */
public class WScrollEvent implements WAbstractEvent {
	/**
	 * Default constructor.
	 */
	public WScrollEvent() {
		super();
		this.jsEvent_ = new JavaScriptEvent();
	}

	/**
	 * Returns the current horizontal scroll position.
	 * <p>
	 * 
	 * @see WScrollEvent#getScrollY()
	 * @see WScrollEvent#getViewportWidth()
	 */
	public int getScrollX() {
		return this.jsEvent_.scrollX;
	}

	/**
	 * Returns the current vertical scroll position.
	 * <p>
	 * 
	 * @see WScrollEvent#getScrollX()
	 * @see WScrollEvent#getViewportHeight()
	 */
	public int getScrollY() {
		return this.jsEvent_.scrollY;
	}

	/**
	 * Returns the current horizontal viewport width.
	 * <p>
	 * Returns the current viewport width.
	 * <p>
	 * 
	 * @see WScrollEvent#getViewportHeight()
	 * @see WScrollEvent#getScrollX()
	 */
	public int getViewportWidth() {
		return this.jsEvent_.viewportWidth;
	}

	/**
	 * Returns the current horizontal viewport height.
	 * <p>
	 * Returns the current viewport height.
	 * <p>
	 * 
	 * @see WScrollEvent#getViewportWidth()
	 * @see WScrollEvent#getScrollY()
	 */
	public int getViewportHeight() {
		return this.jsEvent_.viewportHeight;
	}

	public WAbstractEvent createFromJSEvent(JavaScriptEvent jsEvent) {
		return new WScrollEvent(jsEvent);
	}

	private JavaScriptEvent jsEvent_;

	private WScrollEvent(JavaScriptEvent jsEvent) {
		super();
		this.jsEvent_ = jsEvent;
	}

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

	static WScrollEvent templateEvent = new WScrollEvent();
}
