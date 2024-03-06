/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.auth;

import eu.webtoolkit.jwt.*;
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
 * Model for implementing an authentication view.
 *
 * <p>This model implements the logic for authenticating a user (the &quot;login&quot; interface).
 * It implements traditional username/password registration, and third party identification methods
 * (although for the latter, it doesn&apos;t really do anything).
 *
 * <p>The model exposes three fields:
 *
 * <ul>
 *   <li>LoginNameField: the login name (used as an identity for the {@link Identity#LoginName}
 *       provider)
 *   <li>PasswordField: the password
 *   <li>RememberMeField: whether the login should be remembered with an authentication cookie (if
 *       that is configured in the {@link AuthService}).
 * </ul>
 *
 * <p>When the model validates correctly ({@link AuthModel#validate() validate()} returns <code>true
 * </code>), the entered credentials are correct. At that point you can use the {@link
 * AuthModel#login(Login login) login()} utility function to login the identified user.
 *
 * <p>The model can also be used when the user is already known (e.g. to implement password
 * confirmation before a critical operation). In that case you can set a value for the
 * LoginNameField and make this field invisible or read-only.
 *
 * <p>The model also provides the client-side JavaScript logic to indicate password attempt
 * throttling ({@link AuthModel#configureThrottling(WInteractWidget button) configureThrottling()}
 * and {@link AuthModel#updateThrottling(WInteractWidget button) updateThrottling()}).
 *
 * <p>
 *
 * @see AuthWidget
 */
public class AuthModel extends FormBaseModel {
  private static Logger logger = LoggerFactory.getLogger(AuthModel.class);

  /** Password field. */
  public static final String PasswordField = "password";
  /** Remember-me field. */
  public static final String RememberMeField = "remember-me";
  /**
   * Constructor.
   *
   * <p>Creates a new authentication model, using a basic authentication service and user database.
   */
  public AuthModel(final AuthService baseAuth, final AbstractUserDatabase users) {
    super(baseAuth, users);
    this.throttlingDelay_ = 0;
    this.reset();
  }

  public void reset() {
    if (this.getBaseAuth().getIdentityPolicy() == IdentityPolicy.EmailAddress) {
      this.addField(LoginNameField, WString.tr("Wt.Auth.email-info"));
    } else {
      this.addField(LoginNameField, WString.tr("Wt.Auth.user-name-info"));
    }
    this.addField(PasswordField, WString.tr("Wt.Auth.password-info"));
    int days = this.getBaseAuth().getAuthTokenValidity() / 24 / 60;
    WString info = new WString();
    if (days % 7 != 0) {
      info = WString.trn("Wt.Auth.remember-me-info.days", days).arg(days);
    } else {
      info = WString.trn("Wt.Auth.remember-me-info.weeks", days / 7).arg(days / 7);
    }
    this.addField(RememberMeField, info);
    this.setValidation(RememberMeField, new WValidator.Result(ValidationState.Valid, info));
  }

  public boolean isVisible(String field) {
    if (field == RememberMeField) {
      return this.getBaseAuth().isAuthTokensEnabled();
    } else {
      return super.isVisible(field);
    }
  }

  public boolean validateField(String field) {
    if (field == RememberMeField) {
      return true;
    }
    User user =
        this.getUsers().findWithIdentity(Identity.LoginName, this.valueText(LoginNameField));
    if (field == LoginNameField) {
      if (user.isValid()) {
        if (this.getBaseAuth().isEmailVerificationRequired() && user.getEmail().length() == 0) {
          this.setValidation(
              LoginNameField,
              new WValidator.Result(
                  ValidationState.Invalid, WString.tr("Wt.Auth.email-unverified")));
        } else {
          this.setValid(LoginNameField);
        }
      } else {
        this.setValidation(
            LoginNameField,
            new WValidator.Result(
                ValidationState.Invalid, WString.tr("Wt.Auth.user-name-invalid")));
        this.throttlingDelay_ = 0;
      }
      return user.isValid();
    } else {
      if (field == PasswordField) {
        if (user.isValid()) {
          PasswordResult r =
              this.getPasswordAuth().verifyPassword(user, this.valueText(PasswordField));
          switch (r) {
            case PasswordInvalid:
              this.setValidation(
                  PasswordField,
                  new WValidator.Result(
                      ValidationState.Invalid, WString.tr("Wt.Auth.password-invalid")));
              if (this.getPasswordAuth().isAttemptThrottlingEnabled()) {
                this.throttlingDelay_ = this.getPasswordAuth().delayForNextAttempt(user);
              }
              return false;
            case LoginThrottling:
              this.setValidation(
                  PasswordField,
                  new WValidator.Result(
                      ValidationState.Invalid, WString.tr("Wt.Auth.password-info")));
              this.setValidated(PasswordField, false);
              this.throttlingDelay_ = this.getPasswordAuth().delayForNextAttempt(user);
              logger.warn(
                  new StringWriter()
                      .append("secure:")
                      .append("throttling: ")
                      .append(String.valueOf(this.throttlingDelay_))
                      .append(" seconds for ")
                      .append(user.getIdentity(Identity.LoginName))
                      .toString());
              return false;
            case PasswordValid:
              this.setValid(PasswordField);
              return true;
          }
          return false;
        } else {
          return false;
        }
      } else {
        return false;
      }
    }
  }

  public boolean validate() {
    try (AbstractUserDatabase.Transaction t = this.getUsers().startTransaction(); ) {
      boolean result = super.validate();
      if (t != null) {
        t.commit();
      }
      return result;
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  /**
   * Initializes client-side login throttling.
   *
   * <p>If login attempt throttling is enabled, then this may also be indicated client-side using
   * JavaScript by disabling the login button and showing a count-down indicator. This method
   * initializes this JavaScript utlity function for a login button.
   *
   * <p>
   *
   * @see AuthModel#updateThrottling(WInteractWidget button)
   */
  public void configureThrottling(WInteractWidget button) {
    if (this.getPasswordAuth() != null && this.getPasswordAuth().isAttemptThrottlingEnabled()) {
      WApplication app = WApplication.getInstance();
      app.loadJavaScript("js/AuthModel.js", wtjs1());
      button.setJavaScriptMember(
          " AuthThrottle",
          "new Wt4_10_4.AuthThrottle(Wt4_10_4,"
              + button.getJsRef()
              + ","
              + WString.toWString(WString.tr("Wt.Auth.throttle-retry")).getJsStringLiteral()
              + ");");
    }
  }
  /**
   * Updates client-side login throttling.
   *
   * <p>This should be called after a call to attemptPasswordLogin(), if you want to reflect
   * throttling using a client-side count-down indicator in the button.
   *
   * <p>You need to call {@link AuthModel#configureThrottling(WInteractWidget button)
   * configureThrottling()} before you can do this.
   */
  public void updateThrottling(WInteractWidget button) {
    if (this.getPasswordAuth() != null && this.getPasswordAuth().isAttemptThrottlingEnabled()) {
      StringBuilder s = new StringBuilder();
      s.append(button.getJsRef())
          .append(".wtThrottle.reset(")
          .append(this.throttlingDelay_)
          .append(");");
      button.doJavaScript(s.toString());
    }
  }
  /**
   * Logs the user in.
   *
   * <p>Logs in the user after a successful call to {@link AuthModel#validate() validate()}. To
   * avoid mishaps, you should call this method immediately after a call to {@link
   * AuthModel#validate() validate()}.
   *
   * <p>Returns whether the user could be logged in.
   */
  public boolean login(final Login login) {
    if (this.isValid()) {
      AuthModel self = this;
      User user =
          this.getUsers().findWithIdentity(Identity.LoginName, this.valueText(LoginNameField));
      Object v = this.getValue(RememberMeField);
      if (this.loginUser(login, user)) {
        this.reset();
        if ((v != null) && ((Boolean) v) == true) {
          this.setRememberMeCookie(user);
        }
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }
  /**
   * Logs the user out.
   *
   * <p>This also removes the remember-me cookie for the user.
   */
  public void logout(final Login login) {
    if (login.isLoggedIn()) {
      if (this.getBaseAuth().isAuthTokensEnabled()) {
        WApplication app = WApplication.getInstance();
        app.removeCookie(this.getBaseAuth().getAuthTokenCookieName());
      }
      login.logout();
    }
  }
  /**
   * Processes an email token.
   *
   * <p>This simply calls {@link AuthService#processEmailToken(String token, AbstractUserDatabase
   * users) AuthService#processEmailToken()}.
   */
  public EmailTokenResult processEmailToken(final String token) {
    return this.getBaseAuth().processEmailToken(token, this.getUsers());
  }
  /**
   * Creates a token and stores it in a cookie.
   *
   * <p>This enables automatic authentication in a next session.
   */
  public void setRememberMeCookie(final User user) {
    WApplication app = WApplication.getInstance();
    AuthService s = this.getBaseAuth();
    app.setCookie(
        s.getAuthTokenCookieName(),
        s.createAuthToken(user),
        s.getAuthTokenValidity() * 60,
        s.getAuthTokenCookieDomain(),
        "",
        app.getEnvironment().getUrlScheme().equals("https"));
  }
  /**
   * Detects and processes an authentication token.
   *
   * <p>This returns a user that was identified with an authentication token found in the
   * application environment, or an invalid {@link User} object if this feature is not configured,
   * or no valid cookie was found.
   *
   * <p>
   *
   * @see AuthService#processAuthToken(String token, AbstractUserDatabase users)
   */
  public User processAuthToken() {
    WApplication app = WApplication.getInstance();
    final WEnvironment env = app.getEnvironment();
    if (this.getBaseAuth().isAuthTokensEnabled()) {
      String token = env.getCookie(this.getBaseAuth().getAuthTokenCookieName());
      if (token != null) {
        AuthTokenResult result = this.getBaseAuth().processAuthToken(token, this.getUsers());
        switch (result.getState()) {
          case Valid:
            {
              if (result.getNewToken().length() != 0) {
                app.setCookie(
                    this.getBaseAuth().getAuthTokenCookieName(),
                    result.getNewToken(),
                    result.getNewTokenValidity(),
                    "",
                    "",
                    app.getEnvironment().getUrlScheme().equals("https"));
              }
              return result.getUser();
            }
          case Invalid:
            app.setCookie(
                this.getBaseAuth().getAuthTokenCookieName(),
                "",
                0,
                "",
                "",
                app.getEnvironment().getUrlScheme().equals("https"));
            return new User();
        }
      }
    }
    return new User();
  }
  /**
   * Returns whether to allow resending the email verification.
   *
   * <p>Returns true when email verification is required and the user was not yet verified. In this
   * case, a user would be stuck if the verification email was lost.
   */
  public boolean isShowResendEmailVerification() {
    if (!this.getBaseAuth().isEmailVerificationRequired()) {
      return false;
    }
    User user =
        this.getUsers().findWithIdentity(Identity.LoginName, this.valueText(LoginNameField));
    return user.isValid() && user.getEmail().length() == 0;
  }

  private int throttlingDelay_;

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "AuthThrottle",
        "(function(t,l,e){l.wtThrottle=this;let n=null,i=null,r=0;function s(){clearInterval(n);n=null;t.setHtml(l,i);l.disabled=!1;i=null}function u(){if(0===r)s();else{t.setHtml(l,e.replace(\"{1}\",r));--r}}this.reset=function(t){n&&s();i=l.innerHTML;r=t;if(r){n=setInterval(u,1e3);l.disabled=!0;u()}}})");
  }
}
