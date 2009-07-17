/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.servlet.WebRequest;

/**
 * A class providing details for a drop event.
 * <p>
 * 
 * @see WWidget#dropEvent(WDropEvent event)
 */
public class WDropEvent {
	public WDropEvent(WObject source, String mimeType, WMouseEvent mouseEvent) {
		this.dropSource_ = source;
		this.dropMimeType_ = mimeType;
		this.mouseEvent_ = mouseEvent;
	}

	/**
	 * The source of the drag&amp;drop operation.
	 * <p>
	 * The source is the widget that was set draggable using
	 * {@link WInteractWidget#setDraggable(String mimeType, WWidget dragWidget, boolean isDragWidgetOnly, WObject sourceObject)}.
	 */
	public WObject getSource() {
		return this.dropSource_;
	}

	/**
	 * The mime type of this drop event.
	 */
	public String getMimeType() {
		return this.dropMimeType_;
	}

	/**
	 * Return the original mouse event.
	 */
	public WMouseEvent getMouseEvent() {
		return this.mouseEvent_;
	}

	private WObject dropSource_;
	private String dropMimeType_;
	private WMouseEvent mouseEvent_;

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
}
