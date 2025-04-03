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
 * The interface for a second factor authentication process.
 *
 * <p>This class defines the interface to be used when implementing a second factor in the
 * authentication flow. Currently, it is strongly advised (by among others <a
 * href="https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html#quick-recommendations">OWASP</a>)
 * that a second factor is added to any authentication module.
 *
 * <p>When a developer wishes to enable MFA they will need to set the {@link
 * AuthService#setMfaProvider(String provider) AuthService#setMfaProvider()}, and optionally {@link
 * AuthService#setMfaRequired(boolean require) AuthService#setMfaRequired()}. This will enable JWt
 * to display the MFA step to users logging into the system. This will be shown after a user:
 *
 * <ul>
 *   <li>logs in (with username/password)
 *   <li>is authenticated with a login cookie
 * </ul>
 *
 * <p>The MFA step can be either the setup view, which holds the initial state for the MFA method.
 * For JWt&apos;s default, this means a QR code with the TOTP secret encoded. This then adds an
 * {@link Identity} to the user, by which they can be authenticated against a certain provider. The
 * secret key is also displayed, so that it can be copied into a password manager or authenticator
 * app.
 *
 * <p>For this to work, a developer needs to implement {@link AbstractMfaProcess#createSetupView()
 * createSetupView()}. The method creates the widget that displays the view. This view tells the
 * user how to set up the MFA step. The default {@link TotpProcess} will bind the content to a
 * template. But developers are free to use their own way (like showing a pop-up) This will require
 * the developer to override the {@link AuthWidget#createMfaView()}.
 *
 * <p>After this setup, the process can be repeated but a simpler view is often desired here, one
 * that no longer hold any configuration state, but simply asks for a token of authentication. This
 * is where {@link AbstractMfaProcess#createInputView() createInputView()} comes into play. This
 * functions the exact same way as the above setup view counterpart, but shows less information.
 * Again, for JWt&apos;s default implementation, using TOTP, this means a 6 (depending on
 * configuration, see: {@link AuthService#setMfaCodeLength(int length)
 * AuthService#setMfaCodeLength()}) digit code, that is generated from their secret key will be
 * asked of the user. The initial (QR) code that serves as the way to generate the TOTP keys, will
 * no longer be displayed.
 *
 * <p>A successful match of the second factor will then result in an actual login ({@link
 * Login#login(User user, LoginState state) Login#login()}) (see the note).
 *
 * <p>To use your own widget in the normal authentication flow, one also needs to override {@link
 * AuthWidget#createMfaProcess()}, so that it will create the correct widget. By default this will
 * create the {@link TotpProcess}.
 *
 * <p>
 *
 * <p><i><b>Note: </b>The {@link Login#changed()} signal will be fired both when the user logs in
 * with username/password, and when the MFA step is completed successfully. If your application
 * listens to this signal to determine some state or logic, you should check whether the login has
 * taken place fully (based on the LoginState ({@link Login#getState()})). For convenience it&apos;s
 * a good idea to make your custom widget fire a signal when it tries to authenticate </i>
 *
 * @see AuthenticationResult
 */
public abstract class AbstractMfaProcess extends WObject {
  private static Logger logger = LoggerFactory.getLogger(AbstractMfaProcess.class);

  /** Constructor. */
  public AbstractMfaProcess(
      final AuthService authService, final AbstractUserDatabase users, final Login login) {
    super();
    this.throttlingDelay_ = 0;
    this.baseAuth_ = authService;
    this.users_ = users;
    this.login_ = login;
    this.mfaThrottle_ = null;
  }
  /**
   * Returns the name of the provider for the process.
   *
   * <p>
   *
   * @see AuthService#setMfaProvider(String provider)
   */
  public String getProvider() {
    return this.baseAuth_.getMfaProvider();
  }
  /**
   * Processes the (initial) environment.
   *
   * <p>This can be called to tell the widget to look through the environment for the relevant
   * cookies. It will handle the side-effect of finding such a cookie, and it still being valid. The
   * user will be logged in, in a weak state ({@link LoginState#Weak}), and the authenticated()
   * signal will be fired, with an {@link AuthenticationStatus#Success}.
   */
  public void processEnvironment() {
    User user = this.getProcessMfaToken();
    if (user.isValid()) {
      this.getLogin().login(user, LoginState.Weak);
      return;
    }
  }
  /**
   * Creates the view that displays the MFA configuration step.
   *
   * <p>This is the view that is shown to a user if they do not have MFA enabled yet. This will
   * often show more information to them, telling them how the feature is to be used and activated.
   *
   * <p>The state of whether a user has MFA enabled or not can be decided in two ways:
   *
   * <ul>
   *   <li>the feature is enabled ({@link AuthService#getMfaProvider()} isn&apos;t empty) and they
   *       have an identity for the provider. By default JWt&apos;s TOTP implementation will take
   *       the {@link Identity#MultiFactor} name as the provider.
   *   <li>the feature is enabled AND required ({@link AuthService#isMfaRequired()} is set to <code>
   *       true</code>)
   * </ul>
   */
  public abstract WWidget createSetupView();
  /**
   * Creates the view that displays the MFA input step.
   *
   * <p>The user already has an identity attached to their record. This step now needs valid input
   * from them to continue.
   *
   * <p>
   *
   * @see AbstractMfaProcess#createSetupView()
   */
  public abstract WWidget createInputView();
  /**
   * Sets the instance that manages throttling.
   *
   * <p>Throtteling is an additional safety measure. It ensures that the MFA process cannot be
   * brute-forced.
   *
   * <p>Setting the throttler, will allow for it to be configured ({@link
   * AbstractMfaProcess#configureThrottling(WInteractWidget button) configureThrottling()}), and
   * updated ({@link AbstractMfaProcess#updateThrottling(WInteractWidget button)
   * updateThrottling()}) if applicable.
   */
  public void setMfaThrottle(AuthThrottle authThrottle) {
    this.mfaThrottle_ = authThrottle;
  }
  /**
   * Retrieves the current {@link User}&apos;s identity for the provider.
   *
   * <p>This is simply a method that retrieves the current {@link User}&apos;s identity, given the
   * provider the process specified (see {@link AbstractMfaProcess#getProvider() getProvider()}).
   * This can be accessed by calling {@link User#getIdentity(String provider) User#getIdentity()} as
   * well.
   *
   * <p>The method will return the identity, if it exists, or an empty string if it does not. It
   * will also log (to &quot;warn&quot;) in the latter case.
   *
   * <p>
   *
   * @see AbstractMfaProcess#createUserIdentity(CharSequence identityValue)
   */
  protected WString getUserIdentity() {
    String currentIdentityValue = this.getLogin().getUser().getIdentity(this.getProvider());
    if (currentIdentityValue.length() == 0) {
      logger.warn(
          new StringWriter()
              .append("userIdentity: No identity value for the provider was found. (provider = '")
              .append(this.getProvider())
              .append("').")
              .toString());
    }
    logger.debug(
        new StringWriter()
            .append("userIdentity: A valid identity for the provider was found. (provider = '")
            .append(this.getProvider())
            .append("').")
            .toString());
    return new WString(currentIdentityValue);
  }
  /**
   * Adds an {@link Identity} to the current {@link User} with the given value.
   *
   * <p>The identity will be created with the specified {@link AbstractMfaProcess#getProvider()
   * getProvider()} on the process. And the actual identity will be <code>identityValue</code>.
   *
   * <p>This is again a method that offers very basic functionality, calling: {@link
   * User#getIdentity(String provider) User#getIdentity()} and {@link User#addIdentity(String
   * provider, String identity) User#addIdentity()}, with some logging added to it.
   *
   * <p>
   *
   * <p><i><b>Note: </b>This will store the value in plaintext in the database. Should your chosen
   * method of MFA want this to be stored in a more secure manner, the developer will have to do
   * this manually (or the database itself should be encrypted). </i>
   *
   * @see AbstractMfaProcess#getUserIdentity()
   */
  protected boolean createUserIdentity(final CharSequence identityValue) {
    User user = this.getLogin().getUser();
    String currentIdentityValue = user.getIdentity(this.getProvider());
    if (currentIdentityValue.length() != 0) {
      logger.warn(
          new StringWriter()
              .append(
                  "createUserIdentity: The value for the identity was not empty. This identity was not changed.")
              .toString());
      return false;
    }
    user.addIdentity(this.getProvider(), identityValue.toString());
    logger.info(
        new StringWriter()
            .append("createUserIdentity: Adding new identity for provider; ")
            .append(this.getProvider())
            .toString());
    return true;
  }
  /**
   * Processes an MFA authentication token.
   *
   * <p>If a token is present in the browser, going by the name found in {@link
   * AuthService#getMfaTokenCookieName()}, and that is still valid (see {@link
   * AuthService#getMfaTokenValidity()}), the {@link User} can be retrieved from that token. This
   * identifies the user uniquely, ensuring their MFA verification step can be skipped for a certain
   * period.
   */
  protected User getProcessMfaToken() {
    WApplication app = WApplication.getInstance();
    final WEnvironment env = app.getEnvironment();
    if (this.getBaseAuth().isAuthTokensEnabled()) {
      String token = env.getCookie(this.getBaseAuth().getMfaTokenCookieName());
      if (token != null) {
        logger.info(
            new StringWriter()
                .append("processMfaToken: Processing auth token for MFA for user: ")
                .append(this.getLogin().getUser().getId())
                .toString());
        AuthTokenResult result =
            this.getBaseAuth().processAuthToken(token, this.getUsers(), AuthTokenType.MFA);
        switch (result.getState()) {
          case Valid:
            {
              logger.info(
                  new StringWriter()
                      .append(
                          "processMfaToken: Found valid match for auth token for MFA for user: ")
                      .append(this.getLogin().getUser().getId())
                      .toString());
              if (result.getNewToken().length() != 0) {
                logger.debug(
                    new StringWriter()
                        .append("processMfaToken: Renewing auth token for MFA.")
                        .toString());
                app.setCookie(
                    this.getBaseAuth().getMfaTokenCookieName(),
                    result.getNewToken(),
                    result.getNewTokenValidity(),
                    this.getBaseAuth().getMfaTokenCookieDomain(),
                    "",
                    app.getEnvironment().getUrlScheme().equals("https"));
              }
              String identity = result.getUser().getIdentity(this.getProvider());
              if (identity.length() != 0) {
                if (this.getLogin().getUser().equals(result.getUser())) {
                  logger.debug(
                      new StringWriter()
                          .append(
                              "processMfaToken: Found token with matching user to previous login action.")
                          .toString());
                  return result.getUser();
                } else {
                  logger.debug(
                      new StringWriter()
                          .append(
                              "processMfaToken: Found token with DIFFERENT user to previous login action. This can occur when multiple users use the same device.")
                          .toString());
                  return new User();
                }
              } else {
                logger.debug(
                    new StringWriter()
                        .append("Found no valid identity for the provider: ")
                        .append(this.getProvider())
                        .toString());
                return new User();
              }
            }
          case Invalid:
            {
              logger.info(
                  new StringWriter()
                      .append(
                          "processMfaToken: Found no valid match for auth token for MFA for user:")
                      .append(this.getLogin().getUser().getId())
                      .append(", removing token")
                      .toString());
              app.setCookie(
                  this.getBaseAuth().getMfaTokenCookieName(),
                  "",
                  0,
                  this.getBaseAuth().getMfaTokenCookieDomain(),
                  "",
                  app.getEnvironment().getUrlScheme().equals("https"));
              return new User();
            }
        }
      }
    }
    return new User();
  }
  /**
   * Creates an MFA authentication token.
   *
   * <p>A token (with the correct prefix for MFA) is created and persisted in the database. A cookie
   * is created in the <code>user&apos;s</code> browser. This token can later be used by {@link
   * AbstractMfaProcess#getProcessMfaToken() getProcessMfaToken()} to identify the {@link User},
   * allowing them to skip the MFA step.
   */
  protected void setRememberMeCookie(User user) {
    WApplication app = WApplication.getInstance();
    int duration = this.getBaseAuth().getMfaTokenValidity() * 60;
    logger.info(
        new StringWriter()
            .append("Setting auth token for MFA named: ")
            .append(this.getBaseAuth().getMfaTokenCookieName())
            .append(" with validity (in seconds): ")
            .append(String.valueOf(duration))
            .toString());
    javax.servlet.http.Cookie cookie =
        new javax.servlet.http.Cookie(
            this.getBaseAuth().getMfaTokenCookieName(),
            this.getBaseAuth().createAuthToken(user, AuthTokenType.MFA));
    cookie.setMaxAge(duration);
    cookie.setDomain(this.getBaseAuth().getMfaTokenCookieDomain());
    cookie.setSecure(app.getEnvironment().getUrlScheme().equals("https"));
    app.setCookie(cookie);
  }
  /**
   * Configures client-side throttling on the process.
   *
   * <p>If attempt throttling is enabled, then this may also be indicated client-side using
   * JavaScript by disabling the login button and showing a count-down indicator. This method
   * initializes this JavaScript utility function for a login button.
   *
   * <p>If throttling is enabled, it may be necessary for a custom implementation to manage this
   * state itself. This is to allow developers the freedom to define their own MFA processes.
   *
   * <p>Look at {@link TotpProcess#verifyCode(WTemplate view, boolean throttle)
   * TotpProcess#verifyCode()} for an example.
   *
   * <p>
   *
   * @see AbstractMfaProcess#updateThrottling(WInteractWidget button)
   */
  protected void configureThrottling(WInteractWidget button) {
    if (this.getMfaThrottle() != null) {
      this.mfaThrottle_.initializeThrottlingMessage(button);
    }
  }
  /**
   * Updates client-side login throttling on the process.
   *
   * <p>This should be called after a MFA authentication event takes place, if you want to reflect
   * throttling using a client-side count-down indicator on the button.
   *
   * <p>You need to call {@link AbstractMfaProcess#configureThrottling(WInteractWidget button)
   * configureThrottling()} before you can do this.
   *
   * <p>If throttling is enabled, it may be necessary for a custom implementation to manage this
   * state itself. This is to allow developers the freedom to define their own MFA processes.
   *
   * <p>Look at {@link TotpProcess#verifyCode(WTemplate view, boolean throttle)
   * TotpProcess#verifyCode()} for an example.
   */
  protected void updateThrottling(WInteractWidget button) {
    if (this.getMfaThrottle() != null) {
      this.mfaThrottle_.updateThrottlingMessage(button, this.throttlingDelay_);
    }
  }

  protected AuthService getBaseAuth() {
    return this.baseAuth_;
  }

  protected AbstractUserDatabase getUsers() {
    return this.users_;
  }

  protected Login getLogin() {
    return this.login_;
  }

  protected int throttlingDelay_;

  protected AuthThrottle getMfaThrottle() {
    return this.mfaThrottle_;
  }

  private final AuthService baseAuth_;
  private final AbstractUserDatabase users_;
  private final Login login_;
  private AuthThrottle mfaThrottle_;
}
