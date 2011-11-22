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

class JavaScriptEvent {
	private static Logger logger = LoggerFactory
			.getLogger(JavaScriptEvent.class);

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
	public int wheelDelta;
	public int button;
	public int keyCode;
	public int charCode;
	public EnumSet<KeyboardModifier> modifiers;
	public List<Touch> touches;
	public List<Touch> targetTouches;
	public List<Touch> changedTouches;
	public double scale;
	public double rotation;
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
		this.wheelDelta = parseIntParameter(request, se + "wheel", 0);
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
		this.button = parseIntParameter(request, se + "button", 0);
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
		decodeTouches(getStringParameter(request, se + "touches"), this.touches);
		decodeTouches(getStringParameter(request, se + "ttouches"),
				this.targetTouches);
		decodeTouches(getStringParameter(request, se + "ctouches"),
				this.changedTouches);
	}

	public JavaScriptEvent() {
		this.modifiers = EnumSet.noneOf(KeyboardModifier.class);
		this.touches = new ArrayList<Touch>();
		this.targetTouches = new ArrayList<Touch>();
		this.changedTouches = new ArrayList<Touch>();
		this.type = "";
		this.tid = "";
		this.response = "";
		this.userEventArgs = new ArrayList<String>();
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
