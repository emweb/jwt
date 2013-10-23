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

class GoogleProcess extends OAuthProcess {
	private static Logger logger = LoggerFactory.getLogger(GoogleProcess.class);

	public GoogleProcess(final GoogleService service, final String scope) {
		super(service, scope);
	}

	public void getIdentity(final OAuthAccessToken token) {
		HttpClient client = new HttpClient(this);
		client.setTimeout(15);
		client.setMaximumResponseSize(10 * 1024);
		client.done().addListener(this,
				new Signal2.Listener<Exception, HttpMessage>() {
					public void trigger(Exception event1, HttpMessage event2) {
						GoogleProcess.this.handleMe(event1, event2);
					}
				});
		List<org.apache.http.Header> headers = new ArrayList<org.apache.http.Header>();
		headers.add(new org.apache.http.message.BasicHeader("Authorization",
				"OAuth " + token.getValue()));
		String UserInfoUrl = "https://www.googleapis.com/oauth2/v1/userinfo";
		client.get(UserInfoUrl, headers);
	}

	private void handleMe(Exception err, final HttpMessage response) {
		if (err == null && response.getStatus() == 200) {
			logger.info(new StringWriter().append("user info: ").append(
					response.getBody()).toString());
			com.google.gson.JsonObject userInfo = new com.google.gson.JsonObject();
			try {
				userInfo = (com.google.gson.JsonObject) new com.google.gson.JsonParser()
						.parse(response.getBody());
			} catch (com.google.gson.JsonParseException pe) {
			}
			boolean ok = userInfo != null;
			if (!ok) {
				logger.error(new StringWriter().append(
						"could not parse Json: '").append(response.getBody())
						.append("'").toString());
				this.setError(WString.tr("Wt.Auth.GoogleService.badjson"));
				this.authenticated().trigger(Identity.Invalid);
			} else {
				String id = userInfo.get("id").getAsString();
				String userName = userInfo.get("name").getAsString();
				String email = JsonUtils.orIfNullString(userInfo.get("email"),
						"");
				boolean emailVerified = JsonUtils.orIfNullBoolean(userInfo
						.get("verified_email"), false);
				this.authenticated().trigger(
						new Identity(this.getService().getName(), id, userName,
								email, emailVerified));
			}
		} else {
			this.setError(WString.tr("Wt.Auth.GoogleService.badresponse"));
			if (err == null) {
				logger.error(new StringWriter().append(
						"user info request returned: ").append(
						String.valueOf(response.getStatus())).toString());
				logger.error(new StringWriter().append("with: ").append(
						response.getBody()).toString());
			}
			this.authenticated().trigger(Identity.Invalid);
		}
	}
}
