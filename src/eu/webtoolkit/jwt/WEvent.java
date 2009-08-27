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
 * The request controller notifies the application to react to a request using
 * {@link WApplication#notify(WEvent e)}.
 */
public class WEvent {
	public WEvent(WebSession.Handler aHandler,
			WebRenderer.ResponseType aResponseType) {
		this.handler = aHandler;
		this.responseType = aResponseType;
	}

	WebSession getSession() {
		return this.handler.getSession();
	}

	WebSession.Handler handler;
	WebRenderer.ResponseType responseType;
}
