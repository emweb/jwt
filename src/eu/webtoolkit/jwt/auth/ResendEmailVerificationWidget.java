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
 * A widget to resend the email verification email.
 *
 * <p>The widget renders the <code>&quot;Wt.Auth.template.resend-email-verification&quot;</code>
 * template. It prompts for the email address and if it matches the unverified email invokes {@link
 * AuthService#verifyEmailAddress(User user, String address) AuthService#verifyEmailAddress()}.
 *
 * <p>
 *
 * @see AuthWidget#getCreateResendEmailVerificationView()
 */
public class ResendEmailVerificationWidget extends WTemplate {
  private static Logger logger = LoggerFactory.getLogger(ResendEmailVerificationWidget.class);

  /** Constructor. */
  public ResendEmailVerificationWidget(
      final User user, final AuthService auth, WContainerWidget parentContainer) {
    super(tr("Wt.Auth.template.resend-email-verification"), (WContainerWidget) null);
    this.user_ = user;
    this.baseAuth_ = auth;
    this.addFunction("id", Functions.id);
    this.addFunction("tr", Functions.tr);
    this.addFunction("block", Functions.block);
    WLineEdit email = new WLineEdit();
    this.bindWidget("email", email);
    email.setFocus(true);
    this.bindEmpty("email-info");
    WPushButton okButton = new WPushButton(tr("Wt.Auth.send"));
    this.bindWidget("send-button", okButton);
    WPushButton cancelButton = new WPushButton(tr("Wt.WMessageBox.Cancel"));
    this.bindWidget("cancel-button", cancelButton);
    okButton
        .clicked()
        .addListener(
            this,
            (WMouseEvent e1) -> {
              ResendEmailVerificationWidget.this.send();
            });
    cancelButton
        .clicked()
        .addListener(
            this,
            (WMouseEvent e1) -> {
              ResendEmailVerificationWidget.this.cancel();
            });
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #ResendEmailVerificationWidget(User user, AuthService auth, WContainerWidget
   * parentContainer) this(user, auth, (WContainerWidget)null)}
   */
  public ResendEmailVerificationWidget(final User user, final AuthService auth) {
    this(user, auth, (WContainerWidget) null);
  }
  /**
   * Resend the email verification email.
   *
   * <p>If the prompted email matches the unverified email address, the verification email is sent.
   */
  protected void send() {
    try {
      WFormWidget email = (WFormWidget) this.resolveWidget("email");
      String emailValue = email.getValueText();
      boolean emailMatches = emailValue.equals(this.user_.getUnverifiedEmail());
      if (emailMatches) {
        this.baseAuth_.verifyEmailAddress(this.user_, emailValue);
        {
          WWidget toRemove = this.removeFromParent();
          if (toRemove != null) toRemove.remove();
        }

      } else {
        this.bindString("email-info", tr("Wt.Auth.resend-email-error"));
        WValidator.Result validation =
            new WValidator.Result(ValidationState.Invalid, tr("Wt.Auth.resend-email-error"));
        WApplication.getInstance()
            .getTheme()
            .applyValidationStyle(email, validation, ValidationStyleFlag.ValidationAllStyles);
      }
    } catch (Exception e) {
      logger.info("Ignoring exception {}", e.getMessage(), e);
    }
  }
  /** Removes this widget from the parent. */
  protected void cancel() {
    {
      WWidget toRemove = this.removeFromParent();
      if (toRemove != null) toRemove.remove();
    }
  }

  private User user_;
  private final AuthService baseAuth_;
}
