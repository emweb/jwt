/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
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

class ApplicationEvent {
	private static Logger logger = LoggerFactory
			.getLogger(ApplicationEvent.class);

	public ApplicationEvent() {
		this.sessionId = "";
	}

	public ApplicationEvent(final String aSessionId, final Runnable aFunction,
			final Runnable aFallbackFunction) {
		this.sessionId = aSessionId;
		this.function = aFunction;
		this.fallbackFunction = aFallbackFunction;
	}

	public ApplicationEvent(final String aSessionId, final Runnable aFunction) {
		this(aSessionId, aFunction, (Runnable) null);
	}

	public boolean isEmpty() {
		return !(this.function != null);
	}

	public String sessionId;
	public Runnable function;
	public Runnable fallbackFunction;
}
