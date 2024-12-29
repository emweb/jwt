/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.auth.mfa;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.auth.*;
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
 * A process managing the TOTP setup and validation.
 *
 * <p>This process allows the {@link User} that is trying to log in an extra step to validate their
 * identity. They will have to provide the TOTP code as an extra step to validate their log-in
 * additionally.
 *
 * <p>This process&apos; functionality is twofold. If a {@link User} does not have TOTP set-up yet,
 * they will be shown the QR code and the secret key. They can then add this to an authenticator app
 * or extension of their choice ({@link TotpProcess#createSetupView() createSetupView()}).
 *
 * <p>If they have it enabled already, they will simply be asked to provide a TOTP code to verify
 * their identity as a second factor ({@link TotpProcess#createInputView() createInputView()}).
 *
 * <p>This process will also look in the environment for cookies, so that the MFA step can also be
 * remembered ({@link TotpProcess#processEnvironment() processEnvironment()}). If the user logs in
 * using a device they use often, they can opt to remember the login, which creates a cookie (see
 * {@link AuthService#setMfaTokenCookieName(String name) AuthService#setMfaTokenCookieName()}, and
 * {@link AuthService#setMfaTokenValidity(int validity) AuthService#setMfaTokenValidity()}). If this
 * cookie remains in the user&apos;s browser, and in the local database (in the
 * &quot;auth_token&quot; table, by default), the user&apos;s TOTP step can be skipped for a certain
 * time (see {@link AbstractMfaProcess#getProcessMfaToken()}).
 *
 * <p>If a developer wants to force all users to use this functionality, they can do so by enabling
 * {@link AuthService#setMfaRequired(boolean require) AuthService#setMfaRequired()}.
 *
 * <p>Whether or not this process is executed when logging in, is managed by {@link
 * AuthModel#hasMfaStep(User user) AuthModel#hasMfaStep()}.
 *
 * <p>
 *
 * @see AuthService#setMfaRequired(boolean require)
 */
public class TotpProcess extends AbstractMfaProcess {
  private static Logger logger = LoggerFactory.getLogger(TotpProcess.class);

  /**
   * Constructs the {@link TotpProcess} holding the TOTP &quot;login&quot;.
   *
   * <p>For the provided authentication service <code>authService</code> this will either request a
   * TOTP code from the user as a second factor, or initiate the process to add the TOTP secret to
   * their record, allowing for future TOTP code requests.
   *
   * <p>Optionally, if authentication tokens are enabled (see {@link
   * AuthService#setAuthTokensEnabled(boolean enabled, String cookieName, String cookieDomain)
   * AuthService#setAuthTokensEnabled()}), this step can be temporarily bypassed, for as long as the
   * token is valid (see {@link AuthService#getMfaTokenValidity()}).
   */
  public TotpProcess(
      final AuthService authService, final AbstractUserDatabase users, final Login login) {
    super(authService, users, login);
    this.currentSecretKey_ = "";
    this.authenticated_ = new Signal1<AuthenticationResult>();
  }
  /**
   * Processes the (initial) environment.
   *
   * <p>This can be called to tell the widget to look through the environment for the relevent
   * cookies. It will handle the side-effect of finding such a cookie, and it still being valid. The
   * user will be logged in, in a weak state ({@link LoginState#Weak}), and the {@link
   * TotpProcess#authenticated() authenticated()} signal will be fired, with an {@link
   * AuthenticationStatus#Success}.
   */
  public void processEnvironment() {
    User user = this.getProcessMfaToken();
    if (user.isValid()) {
      this.getLogin().login(user, LoginState.Weak);
      this.authenticated_.trigger(new AuthenticationResult(AuthenticationStatus.Success));
    }
  }
  /**
   * Creates the view to manage the TOTP code.
   *
   * <p>This either adds a new code to the user, or expects a code to be entered based on their
   * existing TOTP secret key.
   */
  public WWidget createSetupView() {
    WTemplate setupView = this.createBaseView();
    this.bindQRCode(setupView);
    this.bindCodeInput(setupView);
    this.bindRememberMe(setupView);
    this.bindLoginButton(setupView);
    return setupView;
  }
  /** Creates the view to input the TOTP code. */
  public WWidget createInputView() {
    WTemplate inputView = this.createBaseView();
    this.bindCodeInput(inputView);
    this.bindRememberMe(inputView);
    this.bindLoginButton(inputView);
    return inputView;
  }
  /**
   * {@link Signal} emitted upon an authentication event.
   *
   * <p>This event can be a success, failure, or error.
   *
   * <p>The additional string can provide more information on the attempt, indicating the type of
   * error, or the reason for the failure. The status and message are both stored in an instance of
   * the {@link AuthenticationResult}.
   *
   * <p>This can be used to reliably check whether the user has logged in with MFA. Previously the
   * {@link Login#changed()} signal had been fired, when the user logged in, but it could still be
   * that the state was not {@link LoginState#Weak} or {@link LoginState#Strong}, but {@link
   * LoginState#RequiresMfa}. This signal can be listened to, to ensure that, on success, the user
   * will actually be logged in.
   *
   * <p>Side-effects to the login can then be attached to this signal.
   */
  public Signal1<AuthenticationResult> authenticated() {
    return this.authenticated_;
  }

  private WLineEdit codeEdit_ = null;
  private WCheckBox rememberMeField_ = null;
  private String currentSecretKey_;
  private Signal1<AuthenticationResult> authenticated_;

  private WTemplate createBaseView() {
    WTemplate baseView = new WTemplate(WTemplate.tr("Wt.Auth.template.totp"));
    baseView.addFunction("id", WTemplate.Functions.id);
    baseView.addFunction("tr", WTemplate.Functions.tr);
    baseView.addFunction("block", WTemplate.Functions.block);
    return baseView;
  }

  private void bindQRCode(WTemplate view) {
    WString totpSecretKey = this.getUserIdentity();
    if ((totpSecretKey.length() == 0)) {
      this.currentSecretKey_ = Totp.generateSecretKey();
      view.setCondition("if:no-secret-key", true);
    } else {
      this.currentSecretKey_ = totpSecretKey.toString();
    }
    view.bindWidget(
        "qr-code",
        new TotpQrCode(
            this.getCurrentSecretKey(),
            this.getBaseAuth().getMfaProvider(),
            this.getLogin().getUser().getEmail(),
            this.getBaseAuth().getMfaCodeLength()));
    view.bindString("secret-key", this.getCurrentSecretKey());
  }

  private void bindCodeInput(WTemplate view) {
    this.codeEdit_ = (WLineEdit) view.bindWidget("totp-code", new WLineEdit());
    this.codeEdit_.setFocus(true);
    this.codeEdit_
        .enterPressed()
        .addListener(
            this,
            () -> {
              TotpProcess.this.verifyCode(view);
            });
    view.bindString("totp-code-info", WString.tr("Wt.Auth.totp-code-info"));
    WString totpSecretKey = this.getUserIdentity();
    if (this.currentSecretKey_.length() == 0 && !(totpSecretKey.length() == 0)) {
      this.currentSecretKey_ = totpSecretKey.toString();
    } else {
      if (this.currentSecretKey_.length() == 0 && (totpSecretKey.length() == 0)) {
        logger.error(
            new StringWriter()
                .append(
                    "createCodeInput: No secret key was set, or could be retrieved from the database.")
                .toString());
      }
    }
  }

  private void bindRememberMe(WTemplate view) {
    view.setCondition("if:remember-me", true);
    this.rememberMeField_ = (WCheckBox) view.bindWidget("remember-me", new WCheckBox());
    int days = this.getBaseAuth().getMfaTokenValidity() / 24 / 60;
    WDate currentDateTime = WDate.getCurrentServerDate();
    WDate expirationDateTime = currentDateTime.addDays(days);
    WString info =
        WString.tr("Wt.Auth.remember-me-info.dynamic")
            .arg(currentDateTime.getTimeTo(expirationDateTime, 1));
    view.bindString("remember-me-info", info);
  }

  private void bindLoginButton(WTemplate view) {
    WPushButton login =
        (WPushButton) view.bindWidget("login", new WPushButton(WString.tr("Wt.Auth.login")));
    login
        .clicked()
        .addListener(
            this,
            () -> {
              TotpProcess.this.verifyCode(view);
            });
    if (this.getMfaThrottle() != null) {
      this.configureThrottling(login);
    }
  }

  private void verifyCode(WTemplate view) {
    String code = this.codeEdit_.getText();
    boolean validation =
        Totp.validateCode(
            this.getCurrentSecretKey(),
            code,
            this.getBaseAuth().getMfaCodeLength(),
            Duration.ofSeconds(WDate.getCurrentServerDate().getDate().getTime() / 1000));
    logger.info(
        new StringWriter()
            .append("verifyCode(): The validation resulted in ")
            .append(validation ? "success" : "failure")
            .append(" for user: ")
            .append(this.getLogin().getUser().getId())
            .toString());
    if (this.getMfaThrottle() != null) {
      this.throttlingDelay_ = this.getMfaThrottle().delayForNextAttempt(this.getLogin().getUser());
      if (this.throttlingDelay_ > 0) {
        validation = false;
      }
    }
    try (AbstractUserDatabase.Transaction t = this.getUsers().startTransaction(); ) {
      this.getLogin().getUser().setAuthenticated(validation);
      t.commit();
      if (!validation) {
        if (this.throttlingDelay_ > 0) {
          this.update(view);
          this.authenticated_.trigger(
              new AuthenticationResult(
                  AuthenticationStatus.Failure, WString.tr("Wt.Auth.totp-code-info-throttle")));
          return;
        }
        this.update(view);
        this.authenticated_.trigger(
            new AuthenticationResult(
                AuthenticationStatus.Failure, WString.tr("Wt.Auth.totp-code-info-invalid")));
      } else {
        this.createUserIdentity(this.getCurrentSecretKey());
        if (this.rememberMeField_.isChecked()) {
          this.setRememberMeCookie(this.getLogin().getUser());
        }
        this.getLogin().login(this.getLogin().getUser());
        this.authenticated_.trigger(new AuthenticationResult(AuthenticationStatus.Success));
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void update(WTemplate view) {
    this.codeEdit_.addStyleClass("is-invalid Wt-invalid");
    view.bindString("totp-code-info", WString.tr("Wt.Auth.totp-code-info-invalid"));
    view.bindString("label", "error has-error");
    if (this.getMfaThrottle() != null) {
      WInteractWidget login = (WInteractWidget) view.resolveWidget("login");
      this.updateThrottling(login);
    }
  }

  private String getCurrentSecretKey() {
    return this.currentSecretKey_;
  }
}
