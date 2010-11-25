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

import eu.webtoolkit.jwt.servlet.UploadedFile;

/**
 * An internal session event.
 * <p>
 * 
 * The request controller notifies the application to react to a request using
 * {@link WApplication#notify(WEvent e) WApplication#notify()}.
 */
public class WEvent {
	private WEvent() {
		this.handler = null;
		this.renderOnly = false;
	}

	WEvent(WebSession.Handler aHandler, boolean doRenderOnly) {
		this.handler = aHandler;
		this.renderOnly = doRenderOnly;
	}

	WEvent(WebSession.Handler aHandler) {
		this.handler = aHandler;
		this.renderOnly = false;
	}

	WebSession.Handler handler;
	boolean renderOnly;
	static UploadedFile uf;
}
