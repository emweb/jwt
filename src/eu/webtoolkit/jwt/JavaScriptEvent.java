/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import eu.webtoolkit.jwt.servlet.WebRequest;

class JavaScriptEvent {
	public int clientX;
	public int clientY;
	public int documentX;
	public int documentY;
	public int screenX;
	public int screenY;
	public int widgetX;
	public int widgetY;
	public int dragDX;
	public int dragDY;
	public boolean right;
	public int keyCode;
	public int charCode;
	public EnumSet<KeyboardModifier> modifiers;
	public int scrollX;
	public int scrollY;
	public int viewportWidth;
	public int viewportHeight;
	public String type;
	public String tid;
	public String response;
	public List<String> userEventArgs;

	public void get(WebRequest request, String se) {
		this.type = getStringParameter(request, se + "type");
		this.type = this.type.toLowerCase();
		this.clientX = parseIntParameter(request, se + "clientX", 0);
		this.clientY = parseIntParameter(request, se + "clientY", 0);
		this.documentX = parseIntParameter(request, se + "documentX", 0);
		this.documentY = parseIntParameter(request, se + "documentY", 0);
		this.screenX = parseIntParameter(request, se + "screenX", 0);
		this.screenY = parseIntParameter(request, se + "screenY", 0);
		this.widgetX = parseIntParameter(request, se + "widgetX", 0);
		this.widgetY = parseIntParameter(request, se + "widgetY", 0);
		this.dragDX = parseIntParameter(request, se + "dragdX", 0);
		this.dragDY = parseIntParameter(request, se + "dragdY", 0);
		this.modifiers.clear();
		if (request.getParameter(se + "altKey") != null) {
			this.modifiers.add(KeyboardModifier.AltModifier);
		}
		if (request.getParameter(se + "ctrlKey") != null) {
			this.modifiers.add(KeyboardModifier.ControlModifier);
		}
		if (request.getParameter(se + "shiftKey") != null) {
			this.modifiers.add(KeyboardModifier.ShiftModifier);
		}
		if (request.getParameter(se + "metaKey") != null) {
			this.modifiers.add(KeyboardModifier.MetaModifier);
		}
		this.keyCode = parseIntParameter(request, se + "keyCode", 0);
		this.charCode = parseIntParameter(request, se + "charCode", 0);
		String p;
		this.right = (p = request.getParameter(se + "right")) != null ? p
				.equals("true") : false;
		this.scrollX = parseIntParameter(request, se + "scrollX", 0);
		this.scrollY = parseIntParameter(request, se + "scrollY", 0);
		this.viewportWidth = parseIntParameter(request, se + "width", 0);
		this.viewportHeight = parseIntParameter(request, se + "height", 0);
		this.response = getStringParameter(request, se + "response");
		int uean = parseIntParameter(request, se + "an", 0);
		this.userEventArgs.clear();
		for (int i = 0; i < uean; ++i) {
			this.userEventArgs.add(getStringParameter(request, se + "a"
					+ String.valueOf(i)));
		}
	}

	public JavaScriptEvent() {
		this.modifiers = EnumSet.noneOf(KeyboardModifier.class);
		this.type = "";
		this.tid = "";
		this.response = "";
		this.userEventArgs = new ArrayList<String>();
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
}
