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
import java.util.List;
import eu.webtoolkit.jwt.servlet.WebRequest;

/**
 * A class providing details for a drop event.
 * <p>
 * 
 * @see WWidget#dropEvent(WDropEvent event)
 */
public class WDropEvent {
	/**
	 * Constructor.
	 */
	public WDropEvent(WObject source, String mimeType, WMouseEvent mouseEvent) {
		this.dropSource_ = source;
		this.dropMimeType_ = mimeType;
		this.mouseEvent_ = mouseEvent;
	}

	/**
	 * Returns the source of the drag&amp;drop operation.
	 * <p>
	 * The source is the widget that was set draggable using
	 * {@link WInteractWidget#setDraggable(String mimeType, WWidget dragWidget, boolean isDragWidgetOnly, WObject sourceObject)
	 * WInteractWidget#setDraggable()}.
	 */
	public WObject getSource() {
		return this.dropSource_;
	}

	/**
	 * Returns the mime type of this drop event.
	 */
	public String getMimeType() {
		return this.dropMimeType_;
	}

	/**
	 * Returns the original mouse event.
	 */
	public WMouseEvent getMouseEvent() {
		return this.mouseEvent_;
	}

	private WObject dropSource_;
	private String dropMimeType_;
	private WMouseEvent mouseEvent_;

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
}
