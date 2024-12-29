/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.auth;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class FacebookProcess extends OAuthProcess {
  private static Logger logger = LoggerFactory.getLogger(FacebookProcess.class);

  public FacebookProcess(final FacebookService auth, final String scope) {
    super(auth, scope);
    this.httpClient_ = null;
  }

  public void getIdentity(final OAuthAccessToken token) {
    this.httpClient_ = new HttpClient();
    this.httpClient_.setTimeout(Duration.ofSeconds(15));
    this.httpClient_.setMaximumResponseSize(10 * 1024);
    this.httpClient_
        .done()
        .addListener(
            this,
            (Exception event1, HttpMessage event2) -> {
              FacebookProcess.this.handleMe(event1, event2);
            });
    this.httpClient_.get(
        "https://graph.facebook.com/me?fields=name,id,email&access_token=" + token.getValue());
  }

  private HttpClient httpClient_;

  private void handleMe(Exception err, final HttpMessage response) {
    if (err == null && response.getStatus() == 200) {
      com.google.gson.JsonObject me = new com.google.gson.JsonObject();
      try {
        me =
            (com.google.gson.JsonObject) new com.google.gson.JsonParser().parse(response.getBody());
      } catch (com.google.gson.JsonParseException pe) {
      }
      boolean ok = me != null;
      if (!ok) {
        logger.error(
            new StringWriter()
                .append("could not parse Json: '")
                .append(response.getBody())
                .append("'")
                .toString());
        this.setError(WString.tr("Wt.Auth.FacebookService.badjson"));
        this.authenticated().trigger(Identity.Invalid);
      } else {
        String id = me.get("id").getAsString();
        String userName = me.get("name").getAsString();
        String email = JsonUtils.orIfNullString(me.get("email"), "");
        boolean emailVerified = !JsonUtils.isNull(me.get("email"));
        this.authenticated()
            .trigger(new Identity(this.getService().getName(), id, userName, email, emailVerified));
      }
    } else {
      if (err == null) {
        logger.error(
            new StringWriter()
                .append("user info request returned: ")
                .append(String.valueOf(response.getStatus()))
                .toString());
        logger.error(new StringWriter().append("with: ").append(response.getBody()).toString());
      } else {
        logger.error(new StringWriter().append("handleMe(): ").append(err.getMessage()).toString());
      }
      this.setError(WString.tr("Wt.Auth.FacebookService.badresponse"));
      this.authenticated().trigger(Identity.Invalid);
    }
    WApplication.getInstance().triggerUpdate();
    WApplication.getInstance().enableUpdates(false);
  }
}
