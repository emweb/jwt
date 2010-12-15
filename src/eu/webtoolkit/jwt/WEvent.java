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

/**
 * An internal session event.
 * <p>
 * 
 * The request controller notifies the application to react to a request using
 * {@link WApplication#notify(WEvent e) WApplication#notify()}.
 */
public class WEvent {
	/**
	 * Return this event&apos;s {@link EventType EventType}.
	 */
	public EventType getEventType() {
		return this.impl_.handler.getSession().getEventType(this);
	}

	WEvent(WEvent.Impl impl, boolean doRenderOnly) {
		this.impl_ = impl;
		this.renderOnly = doRenderOnly;
	}

	WEvent(WEvent.Impl impl) {
		this.impl_ = impl;
		this.renderOnly = false;
	}

	WEvent.Impl impl_;
	boolean renderOnly;

	static class Impl {
		WebSession.Handler handler;

		Impl(WebSession.Handler aHandler) {
			this.handler = aHandler;
		}

		Impl() {
			this.handler = null;
		}
	}

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

class Impl {
	public WebSession.Handler handler;

	public Impl(WebSession.Handler aHandler) {
		this.handler = aHandler;
	}

	public Impl() {
		this.handler = null;
	}
}
