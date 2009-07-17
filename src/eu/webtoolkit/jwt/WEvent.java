/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;


/**
 * An internal session event
 * <p>
 * 
 * The request controller notifies the application to react to browser events,
 * or to render itself, using {@link WApplication#notify(WEvent e)}.
 */
public class WEvent {
	public enum EventType {
		EmitSignal, Refresh, Render, HashChange;

		public int getValue() {
			return ordinal();
		}
	}

	WebSession.Handler handler;
	public WEvent.EventType type;

	WebSession getSession() {
		return this.handler.getSession();
	}

	WebRenderer.ResponseType responseType;
	String hash;

	WEvent(WebSession.Handler aHandler, WEvent.EventType aType,
			WebRenderer.ResponseType aResponseType) {
		this.handler = aHandler;
		this.type = aType;
		this.responseType = aResponseType;
		this.hash = "";
	}

	WEvent(WebSession.Handler aHandler, WEvent.EventType aType) {
		this(aHandler, aType, WebRenderer.ResponseType.FullResponse);
	}

	WEvent(WebSession.Handler aHandler, WEvent.EventType aType, String aHash) {
		this.handler = aHandler;
		this.type = aType;
		this.responseType = WebRenderer.ResponseType.FullResponse;
		this.hash = aHash;
	}
}
