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
 * A base model class for authentication-related forms.
 *
 * <p>This class manages the the auth services and the user database which an authentication model
 * will use to implement a form..
 */
public class FormBaseModel extends WFormModel {
  private static Logger logger = LoggerFactory.getLogger(FormBaseModel.class);

  /** {@link Login} name field. */
  public static final String LoginNameField = "user-name";
  /** Constructor. */
  public FormBaseModel(final AuthService baseAuth, final AbstractUserDatabase users) {
    super();
    this.baseAuth_ = baseAuth;
    this.users_ = users;
    this.passwordAuth_ = null;
    this.oAuth_ = new ArrayList<OAuthService>();
    WApplication app = WApplication.getInstance();
    app.getBuiltinLocalizedStrings().useBuiltin(WtServlet.AuthStrings_xml);
  }
  /**
   * Returns the authentication base service.
   *
   * <p>This returns the service passed through the constructor.
   */
  public AuthService getBaseAuth() {
    return this.baseAuth_;
  }
  /** Returns the user database. */
  public AbstractUserDatabase getUsers() {
    return this.users_;
  }
  /**
   * Adds a password authentication service.
   *
   * <p>This enables password-based registration, including choosing a proper password.
   *
   * <p>Only one password authentication service can be configured.
   *
   * <p>
   *
   * @see FormBaseModel#addOAuth(OAuthService auth)
   */
  public void addPasswordAuth(AbstractPasswordService auth) {
    this.passwordAuth_ = auth;
  }
  /**
   * Returns the password authentication service.
   *
   * <p>
   *
   * @see FormBaseModel#addPasswordAuth(AbstractPasswordService auth)
   */
  public AbstractPasswordService getPasswordAuth() {
    return this.passwordAuth_;
  }
  /**
   * Adds an OAuth authentication service provider.
   *
   * <p>This enables OAuth-based registration. More than one OAuth authentication service can be
   * configured: one for each supported third-party OAuth identity provider.
   *
   * <p>
   *
   * @see FormBaseModel#addPasswordAuth(AbstractPasswordService auth)
   */
  public void addOAuth(OAuthService auth) {
    CollectionUtils.add(this.oAuth_, auth);
  }
  /**
   * Adds a list of OAuth authentication service providers.
   *
   * <p>
   *
   * @see FormBaseModel#addOAuth(OAuthService auth)
   */
  public void addOAuth(final List<OAuthService> auth) {
    for (int i = 0; i < auth.size(); ++i) {
      this.addOAuth(auth.get(i));
    }
  }
  /**
   * Returns the list of OAuth authentication service providers.
   *
   * <p>
   *
   * @see FormBaseModel#addOAuth(OAuthService auth)
   */
  public List<OAuthService> getOAuth() {
    return this.oAuth_;
  }

  public WString label(String field) {
    if (field == LoginNameField
        && this.baseAuth_.getIdentityPolicy() == IdentityPolicy.EmailAddress) {
      field = "email";
    }
    return WString.tr("Wt.Auth." + field);
  }
  /**
   * Logs the user in.
   *
   * <p>Logs in the user, after checking whether the user can actually be logged in. A valid user
   * may be refused to login if its account is disabled (see {@link User#getStatus()}) or if
   * it&apos;s email address is unconfirmed and email confirmation is required.
   *
   * <p>Returns whether the user could be logged in.
   */
  public boolean loginUser(final Login login, final User user, LoginState state) {
    if (!user.isValid()) {
      return false;
    }
    if (user.getStatus() == AccountStatus.Disabled) {
      this.setValidation(
          LoginNameField,
          new WValidator.Result(ValidationState.Invalid, WString.tr("Wt.Auth.account-disabled")));
      login.login(user, LoginState.Disabled);
      return false;
    } else {
      if (this.getBaseAuth().isEmailVerificationRequired() && user.getEmail().length() == 0) {
        this.setValidation(
            LoginNameField,
            new WValidator.Result(ValidationState.Invalid, WString.tr("Wt.Auth.email-unverified")));
        login.login(user, LoginState.Disabled);
        return false;
      } else {
        login.login(user, state);
        return true;
      }
    }
  }
  /**
   * Logs the user in.
   *
   * <p>Returns {@link #loginUser(Login login, User user, LoginState state) loginUser(login, user,
   * LoginState.Strong)}
   */
  public final boolean loginUser(final Login login, final User user) {
    return loginUser(login, user, LoginState.Strong);
  }

  protected void setValid(String field) {
    this.setValid(field, WString.Empty);
  }

  protected void setValid(String field, final CharSequence message) {
    this.setValidation(
        field,
        new WValidator.Result(
            ValidationState.Valid,
            (message.length() == 0) ? WString.tr("Wt.Auth.valid") : message));
  }

  private final AuthService baseAuth_;
  private final AbstractUserDatabase users_;
  private AbstractPasswordService passwordAuth_;
  private List<OAuthService> oAuth_;
}
