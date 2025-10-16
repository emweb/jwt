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

/**
 * A widget which initiates a lost-password email.
 *
 * <p>The widget renders the <code>&quot;Wt.Auth.template.lost-password&quot;</code> template. It
 * prompts for an email address and then invokes {@link AuthService#lostPassword(String
 * emailAddress, AbstractUserDatabase users) AuthService#lostPassword()} with the given email
 * address.
 *
 * <p>
 *
 * @see AuthWidget#getCreateLostPasswordView()
 */
public class LostPasswordWidget extends WTemplate {
  private static Logger logger = LoggerFactory.getLogger(LostPasswordWidget.class);

  /** Constructor. */
  public LostPasswordWidget(
      final AbstractUserDatabase users, final AuthService auth, WContainerWidget parentContainer) {
    super(tr("Wt.Auth.template.lost-password"), (WContainerWidget) null);
    this.users_ = users;
    this.baseAuth_ = auth;
    this.addFunction("id", Functions.id);
    this.addFunction("tr", Functions.tr);
    this.addFunction("block", Functions.block);
    WLineEdit email = new WLineEdit();
    this.bindWidget("email", email);
    this.bindString("email-info", tr("Wt.Auth.email-info"));
    email.setFocus(true);
    WPushButton okButton = new WPushButton(tr("Wt.Auth.send"));
    this.bindWidget("send-button", okButton);
    WPushButton cancelButton = new WPushButton(tr("Wt.WMessageBox.Cancel"));
    this.bindWidget("cancel-button", cancelButton);
    okButton
        .clicked()
        .addListener(
            this,
            (WMouseEvent e1) -> {
              LostPasswordWidget.this.send();
            });
    cancelButton
        .clicked()
        .addListener(
            this,
            (WMouseEvent e1) -> {
              LostPasswordWidget.this.cancel();
            });
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #LostPasswordWidget(AbstractUserDatabase users, AuthService auth,
   * WContainerWidget parentContainer) this(users, auth, (WContainerWidget)null)}
   */
  public LostPasswordWidget(final AbstractUserDatabase users, final AuthService auth) {
    this(users, auth, (WContainerWidget) null);
  }

  protected void send() {
    try {
      WFormWidget email = (WFormWidget) this.resolveWidget("email");
      this.baseAuth_.lostPassword(email.getValueText(), this.users_);
      this.cancel();
      WMessageBox box =
          new WMessageBox(
              tr("Wt.Auth.lost-password"),
              tr("Wt.Auth.mail-sent"),
              Icon.None,
              EnumSet.of(StandardButton.Ok));
      box.show();
      final WMessageBox boxPtr = box;
      box.buttonClicked()
          .addListener(
              (WObject) null,
              () -> {
                LostPasswordWidget.this.deleteBox(boxPtr);
              });
    } catch (Exception e) {
      logger.info("Ignoring exception {}", e.getMessage(), e);
    }
  }

  protected void cancel() {
    {
      WWidget toRemove = this.removeFromParent();
      if (toRemove != null) toRemove.remove();
    }
  }

  private final AbstractUserDatabase users_;
  private final AuthService baseAuth_;

  private void deleteBox(WMessageBox box) {
    if (box != null) box.remove();
  }
}
