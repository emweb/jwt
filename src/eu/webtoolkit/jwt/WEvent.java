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
 * {@link WApplication#notify(WEvent e) notify()}.
 */
public class WEvent {
	WEvent(WebSession.Handler aHandler, WebRenderer.ResponseType aResponseType,
			boolean doRenderOnly) {
		this.handler = aHandler;
		this.responseType = aResponseType;
		this.renderOnly = doRenderOnly;
	}

	WEvent(WebSession.Handler aHandler, WebRenderer.ResponseType aResponseType) {
		this.handler = aHandler;
		this.responseType = aResponseType;
		this.renderOnly = false;
	}

	WebSession.Handler handler;
	WebRenderer.ResponseType responseType;
	boolean renderOnly;
}
