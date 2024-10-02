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

class OAuthWidget extends WImage {
  private static Logger logger = LoggerFactory.getLogger(OAuthWidget.class);

  public OAuthWidget(final OAuthService oAuthService, WContainerWidget parentContainer) {
    super(new WLink("css/oauth-" + oAuthService.getName() + ".png"), (WContainerWidget) null);
    this.process_ = null;
    this.authenticated_ = new Signal2<OAuthProcess, Identity>();
    this.setToolTip(oAuthService.getDescription());
    this.setStyleClass("Wt-auth-icon");
    this.setVerticalAlignment(AlignmentFlag.Middle);
    this.process_ = oAuthService.createProcess(oAuthService.getAuthenticationScope());
    final EventSignal1<WMouseEvent> clickedSignal = this.clicked();
    this.process_.connectStartAuthenticate(clickedSignal);
    this.process_
        .authenticated()
        .addListener(
            this,
            (Identity e1) -> {
              OAuthWidget.this.oAuthDone(e1);
            });
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public OAuthWidget(final OAuthService oAuthService) {
    this(oAuthService, (WContainerWidget) null);
  }

  public Signal2<OAuthProcess, Identity> authenticated() {
    return this.authenticated_;
  }

  private OAuthProcess process_;
  private Signal2<OAuthProcess, Identity> authenticated_;

  private void oAuthDone(final Identity identity) {
    this.authenticated_.trigger(this.process_, identity);
  }
}
