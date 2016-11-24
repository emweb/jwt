/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.auth;

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

class FacebookProcess extends OAuthProcess {
	private static Logger logger = LoggerFactory
			.getLogger(FacebookProcess.class);

	public FacebookProcess(final FacebookService auth, final String scope) {
		super(auth, scope);
	}

	public void getIdentity(final OAuthAccessToken token) {
		HttpClient client = new HttpClient(this);
		client.setTimeout(15);
		client.setMaximumResponseSize(10 * 1024);
		client.done().addListener(this,
				new Signal2.Listener<Exception, HttpMessage>() {
					public void trigger(Exception event1, HttpMessage event2) {
						FacebookProcess.this.handleMe(event1, event2);
					}
				});
		client.get("https://graph.facebook.com/me?access_token="
				+ token.getValue());
	}

	private void handleMe(Exception err, final HttpMessage response) {
		if (err == null && response.getStatus() == 200) {
			com.google.gson.JsonObject me = new com.google.gson.JsonObject();
			try {
				me = (com.google.gson.JsonObject) new com.google.gson.JsonParser()
						.parse(response.getBody());
			} catch (com.google.gson.JsonParseException pe) {
			}
			boolean ok = me != null;
			if (!ok) {
				logger.error(new StringWriter()
						.append("could not parse Json: '")
						.append(response.getBody()).append("'").toString());
				this.setError(WString.tr("Wt.Auth.FacebookService.badjson"));
				this.authenticated().trigger(Identity.Invalid);
			} else {
				String id = me.get("id").getAsString();
				String userName = me.get("name").getAsString();
				String email = JsonUtils.orIfNullString(me.get("email"), "");
				boolean emailVerified = JsonUtils.orIfNullBoolean(
						me.get("verified"), false);
				this.authenticated().trigger(
						new Identity(this.getService().getName(), id, userName,
								email, emailVerified));
			}
		} else {
			if (err == null) {
				logger.error(new StringWriter()
						.append("user info request returned: ")
						.append(String.valueOf(response.getStatus()))
						.toString());
				logger.error(new StringWriter().append("with: ")
						.append(response.getBody()).toString());
			} else {
				logger.error(new StringWriter().append("handleMe(): ")
						.append(err.getMessage()).toString());
			}
			this.setError(WString.tr("Wt.Auth.FacebookService.badresponse"));
			this.authenticated().trigger(Identity.Invalid);
		}
	}
}
