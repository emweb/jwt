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
 * A class providing details for a touch event.
 * <p>
 * 
 * @see WInteractWidget#touchStarted()
 * @see WInteractWidget#touchMoved()
 * @see WInteractWidget#touchEnded()
 */
public class WTouchEvent implements WAbstractEvent {
	/**
	 * Default constructor.
	 */
	public WTouchEvent() {
		super();
		this.jsEvent_ = new JavaScriptEvent();
	}

	/**
	 * Returns a list of {@link Touch Touch} objects for every finger currently
	 * touching the screen.
	 */
	public List<Touch> getTouches() {
		return this.jsEvent_.touches;
	}

	/**
	 * Returns a list of {@link Touch Touch} objects for finger touches that
	 * started out within the same widget.
	 */
	public List<Touch> getTargetTouches() {
		return this.jsEvent_.targetTouches;
	}

	/**
	 * Returns a list of {@link Touch Touch} objects for every finger involved
	 * in the event.
	 */
	public List<Touch> getChangedTouches() {
		return this.jsEvent_.changedTouches;
	}

	public WAbstractEvent createFromJSEvent(JavaScriptEvent jsEvent) {
		return new WTouchEvent(jsEvent);
	}

	WTouchEvent(JavaScriptEvent jsEvent) {
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

	static WTouchEvent templateEvent = new WTouchEvent();
}
