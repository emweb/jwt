package eu.webtoolkit.jwt;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;

class WEvent {
	public enum EventType {
		EmitSignal, Refresh, Render, HashChange;

		public int getValue() {
			return ordinal();
		}
	}

	public WebSession.Handler handler;
	public WEvent.EventType type;

	public WebSession getSession() {
		return this.handler.getSession();
	}

	public WebRenderer.ResponseType responseType;
	public String hash;

	public WEvent(WebSession.Handler aHandler, WEvent.EventType aType,
			WebRenderer.ResponseType aResponseType) {
		this.handler = aHandler;
		this.type = aType;
		this.responseType = aResponseType;
		this.hash = "";
	}

	public WEvent(WebSession.Handler aHandler, WEvent.EventType aType) {
		this(aHandler, aType, WebRenderer.ResponseType.FullResponse);
	}

	public WEvent(WebSession.Handler aHandler, WEvent.EventType aType,
			String aHash) {
		this.handler = aHandler;
		this.type = aType;
		this.responseType = WebRenderer.ResponseType.FullResponse;
		this.hash = aHash;
	}
}
