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
 * An authentication widget.
 *
 * <p>The authentication widget is a widget that provides a login or logout function (depending on
 * whether the user is currently logged in). You can use it for either or both purposes.
 *
 * <p>{@link Login} or logout events are signalled to a {@link Login} object on which this widget
 * acts.
 *
 * <p>The widget also processes environmental information related to authentication:
 *
 * <p>
 *
 * <ul>
 *   <li>email tokens, which are indicated in an internal path. The widget uses dialogs (by default)
 *       to interact with the user to act on the token.
 *   <li>authentication tokens, which are stored in browser cookies, to implement remember-me
 *       functionality.
 * </ul>
 *
 * <p>The {@link AuthWidget#processEnvironment() processEnvironment()} method initiates this
 * process, and should typically be called only at application startup time.
 *
 * <p>The authentication widget is implemented as a View for an {@link AuthModel}, which can be set
 * using {@link AuthWidget#setModel(AuthModel model) setModel()}. The login logic (at this moment
 * only for password-based authentication) is handled by this model.
 *
 * <p>It is very likely that the off-the shelf authentication widget does not satisfy entirely to
 * your taste or functional requirements. The widget uses three methods to allow customization:
 *
 * <p>
 *
 * <ul>
 *   <li>as a {@link WTemplateFormView}, you may change the layout and styling of to your liking.
 *   <li>the authentication logic is delegated to an {@link AuthModel} and can can be specialized or
 *       can be used with a custom view altogether.
 *   <li>the views are created using virtual methods, which may be specialized to create a
 *       customized view or to apply changes to the default view.
 * </ul>
 */
public class AuthWidget extends WTemplateFormView {
  private static Logger logger = LoggerFactory.getLogger(AuthWidget.class);

  /**
   * Constructor.
   *
   * <p>Creates a new authentication widget. This creates an {@link AuthModel} using the given
   * authentication service <code>baseAuth</code> and user database <code>users</code>.
   *
   * <p>The result of authentication changes is propagated to the rest of the application using a
   * <code>login</code> object.
   *
   * <p>Authentication services need to be configured in the {@link AuthWidget#getModel()
   * getModel()}.
   */
  public AuthWidget(
      final AuthService baseAuth,
      final AbstractUserDatabase users,
      final Login login,
      WContainerWidget parentContainer) {
    super(WString.Empty, (WContainerWidget) null);
    this.model_ = new AuthModel(baseAuth, users);
    this.registrationModel_ = null;
    this.login_ = login;
    this.basePath_ = "";
    this.dialog_ = null;
    this.messageBox_ = null;
    this.mfaWidget_ = null;
    this.init();
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #AuthWidget(AuthService baseAuth, AbstractUserDatabase users, Login login,
   * WContainerWidget parentContainer) this(baseAuth, users, login, (WContainerWidget)null)}
   */
  public AuthWidget(
      final AuthService baseAuth, final AbstractUserDatabase users, final Login login) {
    this(baseAuth, users, login, (WContainerWidget) null);
  }
  /**
   * Constructor.
   *
   * <p>Creates a new authentication widget.
   *
   * <p>The result of authentication changes is propagated to the rest of the application using a
   * <code>login</code> object.
   *
   * <p>You need to call {@link AuthWidget#setModel(AuthModel model) setModel()} to configure a
   * model for this view.
   */
  public AuthWidget(final Login login, WContainerWidget parentContainer) {
    super(WString.Empty, (WContainerWidget) null);
    this.model_ = null;
    this.registrationModel_ = null;
    this.login_ = login;
    this.basePath_ = "";
    this.dialog_ = null;
    this.messageBox_ = null;
    this.mfaWidget_ = null;
    this.init();
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #AuthWidget(Login login, WContainerWidget parentContainer) this(login,
   * (WContainerWidget)null)}
   */
  public AuthWidget(final Login login) {
    this(login, (WContainerWidget) null);
  }

  public void remove() {
    this.dialog_ = null;
    this.messageBox_ = null;
    super.remove();
  }
  /**
   * Sets a model.
   *
   * <p>This sets a model to be used for authentication.
   */
  public void setModel(AuthModel model) {
    this.model_ = model;
  }
  /**
   * Returns the model.
   *
   * <p>The model is used only for the login function.
   *
   * <p>
   *
   * @see AuthWidget#setModel(AuthModel model)
   */
  public AuthModel getModel() {
    return this.model_;
  }
  /**
   * Returns the login object.
   *
   * <p>This login object is used to keep track of the user currently authenticated.
   */
  public Login getLogin() {
    return this.login_;
  }
  /**
   * Sets an internal path for authentication services.
   *
   * <p>Only the registration function is made available through an internal path (so that one can
   * redirect a user to the registration page). Other internal paths involved in authentication are
   * configured in the service classes:
   *
   * <ul>
   *   <li>{@link AuthService#setEmailRedirectInternalPath(String internalPath)
   *       AuthService#setEmailRedirectInternalPath()}: email tokens
   *   <li>OAuthService::redirectInternalPath(): an internal path used during the oauth process.
   * </ul>
   */
  public void setInternalBasePath(final String basePath) {
    this.basePath_ = StringUtils.append(StringUtils.prepend(basePath, '/'), '/');
    ;
  }
  /**
   * Returns the internal path.
   *
   * <p>
   *
   * @see AuthWidget#setInternalBasePath(String basePath)
   */
  public String getInternalBasePath() {
    return this.basePath_;
  }
  /**
   * Configures registration capabilities.
   *
   * <p>Although the {@link AuthWidget} itself does not implement a registration view, it may offer
   * a button/link to do so, and calls {@link AuthWidget#registerNewUser() registerNewUser()} when a
   * user wishes to register.
   *
   * <p>Even if registration is not enabled, the result of an {@link OAuthService} login process may
   * be that a new user is identified. Then the {@link AuthWidget#createRegistrationView(Identity
   * id) createRegistrationView()} is also used to present this new user with a registration view,
   * passing the information obtained through OAuth.
   */
  public void setRegistrationEnabled(boolean enabled) {
    this.registrationEnabled_ = enabled;
  }
  /**
   * Starts a new registration process.
   *
   * <p>This calls <code>registerNewUser(0)</code>.
   */
  public void registerNewUser() {
    this.registerNewUser(Identity.Invalid);
  }
  /**
   * Starts a new registration process.
   *
   * <p>This starts a new registration process, and may be called in response to a user action, an
   * internal path change, or an {@link OAuthService} login procedure which identified a new user.
   * In the latter case, the OAuth-provided information is passed as parameter <code>oauth</code>.
   *
   * <p>The default implementation creates a view using {@link
   * AuthWidget#createRegistrationView(Identity id) createRegistrationView()}, and shows it in a
   * dialog using {@link AuthWidget#showDialog(CharSequence title, WWidget contents) showDialog()}.
   */
  public void registerNewUser(final Identity oauth) {
    this.showDialog(tr("Wt.Auth.registration"), this.createRegistrationView(oauth));
  }
  /**
   * Processes the (initial) environment.
   *
   * <p>This method process environmental information that may be relevant to authentication:
   *
   * <p>
   *
   * <ul>
   *   <li>email tokens, which are indicated through an internal path. The widget uses dialogs (by
   *       default) to interact with the user to act on the token.
   *   <li>authentication tokens, which are stored in browser cookies, to implement remember-me
   *       functionality. When logging in using an authentication token, the login is considered
   *       &quot;weak&quot; (since a user may have inadvertently forgotten to logout from a public
   *       computer). You should let the user authenticate using another, primary method before
   *       doing sensitive operations. The {@link AuthWidget#createPasswordPromptDialog(Login login)
   *       createPasswordPromptDialog()} method may be useful for this. This token denotes a regular
   *       username/password login. If the &quot;remember-me&quot; functionality is enabled for it,
   *       and selected, a token will be produced, named according to {@link
   *       AuthService#getAuthTokenCookieName()}, and valid for {@link
   *       AuthService#getAuthTokenValidity()} (in minutes). Both can be set by enabling
   *       authentication tokens with AuthService::setAuthTokenaEnabled(). By default the cookie
   *       will be called &quot;wtauth&quot; and will be valid for two weeks.
   * </ul>
   *
   * <p>
   *
   * @see AuthWidget#letUpdatePassword(User user, boolean promptPassword)
   */
  public void processEnvironment() {
    final WEnvironment env = WApplication.getInstance().getEnvironment();
    if (this.registrationEnabled_) {
      if (this.handleRegistrationPath(env.getInternalPath())) {
        return;
      }
    }
    String emailToken = this.model_.getBaseAuth().parseEmailToken(env.getInternalPath());
    if (emailToken.length() != 0) {
      EmailTokenResult result = this.model_.processEmailToken(emailToken);
      switch (result.getState()) {
        case Invalid:
          this.displayError(tr("Wt.Auth.error-invalid-token"));
          break;
        case Expired:
          this.displayError(tr("Wt.Auth.error-token-expired"));
          break;
        case UpdatePassword:
          this.letUpdatePassword(result.getUser(), false);
          break;
        case EmailConfirmed:
          this.displayInfo(tr("Wt.Auth.info-email-confirmed"));
          User user = result.getUser();
          LoginState state = LoginState.Strong;
          if (this.model_.hasMfaStep(user)) {
            state = LoginState.RequiresMfa;
          }
          this.model_.loginUser(this.login_, user, state);
          if (this.login_.getState() == LoginState.RequiresMfa) {
            if (!(this.mfaWidget_ != null)) {
              this.mfaWidget_ = this.createMfaProcess();
            }
            this.mfaWidget_.processEnvironment();
          }
      }
      if (WApplication.getInstance().getEnvironment().hasAjax()) {
        WApplication.getInstance().setInternalPath("/");
      }
      return;
    }
    User user = this.model_.processAuthToken();
    LoginState state = LoginState.Weak;
    if (this.model_.hasMfaStep(user)) {
      state = LoginState.RequiresMfa;
    }
    this.model_.loginUser(this.login_, user, state);
    if (this.login_.getState() == LoginState.RequiresMfa) {
      if (!(this.mfaWidget_ != null)) {
        this.mfaWidget_ = this.createMfaProcess();
      }
      this.mfaWidget_.processEnvironment();
    }
  }
  /**
   * Lets the user update his password.
   *
   * <p>This creates a view to let the user enter his new password.
   *
   * <p>The default implementation creates a new view using {@link
   * AuthWidget#createUpdatePasswordView(User user, boolean promptPassword)
   * createUpdatePasswordView()} and shows it in a dialog using {@link
   * AuthWidget#showDialog(CharSequence title, WWidget contents) showDialog()}.
   */
  public void letUpdatePassword(final User user, boolean promptPassword) {
    WWidget updatePasswordView = this.createUpdatePasswordView(user, promptPassword);
    UpdatePasswordWidget defaultUpdatePasswordWidget =
        ObjectUtils.cast(updatePasswordView, UpdatePasswordWidget.class);
    this.showDialog(tr("Wt.Auth.updatepassword"), updatePasswordView);
    if (defaultUpdatePasswordWidget != null) {
      defaultUpdatePasswordWidget
          .updated()
          .addListener(
              this,
              () -> {
                AuthWidget.this.closeDialog();
              });
      defaultUpdatePasswordWidget
          .canceled()
          .addListener(
              this,
              () -> {
                AuthWidget.this.closeDialog();
              });
    }
  }
  /**
   * Lets the user &quot;recover&quot; a lost password.
   *
   * <p>This creates a view to let the user enter his email address, used to send an email
   * containing instructions to enter a new password.
   *
   * <p>The default implementation creates a new view using {@link
   * AuthWidget#getCreateLostPasswordView() getCreateLostPasswordView()} and shows it in a dialog
   * using {@link AuthWidget#showDialog(CharSequence title, WWidget contents) showDialog()}.
   */
  public void handleLostPassword() {
    this.showDialog(tr("Wt.Auth.lostpassword"), this.getCreateLostPasswordView());
  }
  /**
   * Creates a lost password view.
   *
   * <p>When email verification has been enabled, the user may indicate that he has lost his
   * password &ndash; then proof of controlling the same email address that had associated with his
   * account is sufficient to allow him to enter a new password.
   *
   * <p>This creates the widget used to let the user enter his email address. The default
   * implementation creates a new {@link LostPasswordWidget}.
   *
   * <p>
   *
   * @see AuthWidget#handleLostPassword()
   */
  public WWidget getCreateLostPasswordView() {
    return new LostPasswordWidget(
        this.model_.getUsers(), this.model_.getBaseAuth(), (WContainerWidget) null);
  }
  /**
   * Creates a registration view.
   *
   * <p>This creates a registration view, optionally using information already obtained from a third
   * party identification service (such as an OAuth provider).
   *
   * <p>The default implementation creates a new {@link RegistrationWidget} with a model created
   * using {@link AuthWidget#getCreateRegistrationModel() getCreateRegistrationModel()}.
   *
   * <p>
   *
   * @see AuthWidget#registerNewUser()
   */
  public WWidget createRegistrationView(final Identity id) {
    RegistrationModel model = this.getCreateRegistrationModel();
    if (id.isValid()) {
      model.registerIdentified(id);
    }
    RegistrationWidget w = new RegistrationWidget(this, (WContainerWidget) null);
    w.setModel(model);
    return w;
  }
  /**
   * Lets the user resend the verification email.
   *
   * <p>This creates a view to let the user resend the email to verify their email address.
   *
   * <p>The default implementation creates a new view using {@link
   * AuthWidget#getCreateResendEmailVerificationView() getCreateResendEmailVerificationView()} and
   * shows it in a dialog using {@link AuthWidget#showDialog(CharSequence title, WWidget contents)
   * showDialog()}.
   */
  public void letResendEmailVerification() {
    this.showDialog(
        tr("Wt.Auth.resend-verification-title"), this.getCreateResendEmailVerificationView());
  }
  /**
   * Creates a view to resend the email verification email.
   *
   * <p>If {@link AuthService#isEmailVerificationRequired()} is <code>true</code>, a button will be
   * shown next to the user name field to resend the verification email (if the email was not yet
   * verified). This button will show a dialog containing the widget returned by this method. The
   * default implementation instantiates a {@link ResendEmailVerificationWidget}.
   *
   * <p>This creates the widget used to let the user chose a new password. The default
   * implementation instantiates an {@link UpdatePasswordWidget}.
   *
   * <p>Note that if email verification is optional, the application should provide its own
   * mechanism to resend the verification email (e.g. in a user settings widget).
   */
  public WWidget getCreateResendEmailVerificationView() {
    String loginName = this.model_.valueText(AuthModel.LoginNameField);
    User user = this.model_.getUsers().findWithIdentity(Identity.LoginName, loginName);
    return new ResendEmailVerificationWidget(
        user, this.model_.getBaseAuth(), (WContainerWidget) null);
  }
  /**
   * Creates a view to update a user&apos;s password.
   *
   * <p>If <code>promptPassword</code> is <code>true</code>, the user has to enter his current
   * password in addition to a new password.
   *
   * <p>This creates the widget used to let the user chose a new password. The default
   * implementation instantiates an {@link UpdatePasswordWidget}.
   *
   * <p>
   *
   * @see AuthWidget#letUpdatePassword(User user, boolean promptPassword)
   */
  public WWidget createUpdatePasswordView(final User user, boolean promptPassword) {
    return new UpdatePasswordWidget(
        user,
        this.getCreateRegistrationModel(),
        promptPassword ? this.model_ : null,
        (WContainerWidget) null);
  }
  /**
   * Creates a password prompt dialog.
   *
   * <p>This creates a dialog password. The user is taken from the <code>login</code> object, which
   * also signals an eventual success using its {@link Login#changed()} signal.
   *
   * <p>The default implementation instantiates a {@link PasswordPromptDialog}.
   */
  public WDialog createPasswordPromptDialog(final Login login) {
    return new PasswordPromptDialog(login, this.model_);
  }
  /**
   * Create the MFA process.
   *
   * <p>When MFA is enabled ({@link AuthService#setMfaProvider(String provider)
   * AuthService#setMfaProvider()} is set), this will be called to create a specific MFA process.
   * This can be used by developers to provide their own implementation, and ensure that the right
   * widgets are shown to the user.
   *
   * <p>By default this will generate a {@link TotpProcess}.
   */
  public AbstractMfaProcess createMfaProcess() {
    TotpProcess mfaProcess =
        new TotpProcess(this.getModel().getBaseAuth(), this.getModel().getUsers(), this.getLogin());
    if (this.getModel().getBaseAuth().isMfaThrottleEnabled()) {
      mfaProcess.setMfaThrottle(new AuthThrottle());
    }
    return mfaProcess;
  }
  /**
   * Shows the MFA process in the UI.
   *
   * <p>This functionality manages how the MFA step is shown to the user. Developers can override
   * this to show the step in any way they see fit. This can be shown as part of the main view, as a
   * pop-up, ...
   *
   * <p>It will also need to decide whether the setup view ({@link
   * AbstractMfaProcess#createSetupView()}) or input view ({@link
   * AbstractMfaProcess#createInputView()}) is shown to the user.
   *
   * <p>By default this will show the process in the main view, replacing the normal login widget
   * with the right view on the MFA process.
   */
  public void createMfaView() {
    this.setTemplateText("<div>${mfa}</div>");
    if (!(this.mfaWidget_ != null)) {
      this.mfaWidget_ = this.createMfaProcess();
    }
    AbstractMfaProcess defaultMfaWidget = (AbstractMfaProcess) this.mfaWidget_;
    if (defaultMfaWidget != null) {
      final User user = this.login_.getUser();
      final WString mfaSecretKey = new WString(user.getIdentity(defaultMfaWidget.getProvider()));
      if ((mfaSecretKey.length() == 0)) {
        this.bindWidget("mfa", defaultMfaWidget.createSetupView());
      } else {
        defaultMfaWidget.processEnvironment();
        this.bindWidget("mfa", defaultMfaWidget.createInputView());
      }
    }
  }

  void attemptPasswordLogin() {
    this.updateModel(this.model_);
    if (this.model_.validate()) {
      if (!this.model_.login(this.login_)) {
        this.updatePasswordLoginView();
      }
    } else {
      this.updatePasswordLoginView();
    }
  }
  /**
   * Displays the error message.
   *
   * <p>This method display an dialog showing the error
   */
  public void displayError(final CharSequence m) {
    this.messageBox_ =
        new WMessageBox(tr("Wt.Auth.error"), m, Icon.None, EnumSet.of(StandardButton.Ok));
    this.messageBox_
        .buttonClicked()
        .addListener(
            this,
            (StandardButton e1) -> {
              AuthWidget.this.closeDialog();
            });
    this.messageBox_.show();
  }
  /**
   * Displays the info message.
   *
   * <p>This method display an dialog showing the info
   */
  public void displayInfo(final CharSequence m) {
    this.messageBox_ =
        new WMessageBox(tr("Wt.Auth.notice"), m, Icon.None, EnumSet.of(StandardButton.Ok));
    this.messageBox_
        .buttonClicked()
        .addListener(
            this,
            (StandardButton e1) -> {
              AuthWidget.this.closeDialog();
            });
    this.messageBox_.show();
  }
  /**
   * Creates the user-interface.
   *
   * <p>This method is called just before an initial rendering, and creates the initial view.
   *
   * <p>The default implementation calls {@link AuthWidget#createLoginView() createLoginView()} or
   * {@link AuthWidget#createLoggedInView() createLoggedInView()} depending on whether a user is
   * currently logged in.
   *
   * <p>If MFA is enabled ({@link AuthService#isMfaEnabled()}), this may call {@link
   * AuthWidget#createMfaView() createMfaView()}. This will be called if the user that is logging in
   * has this step enabled ({@link AuthModel#hasMfaStep(User user) AuthModel#hasMfaStep()}).
   */
  protected void create() {
    if (this.created_) {
      return;
    }
    this.onLoginChange();
    this.created_ = true;
  }
  /**
   * Creates the login view.
   *
   * <p>This creates a view that allows the user to login, and is shown when no user is current
   * logged in.
   *
   * <p>The default implementation renders the <code>&quot;Wt.Auth.template.login&quot;</code>
   * template, and binds fields using {@link AuthWidget#createPasswordLoginView()
   * createPasswordLoginView()} and {@link AuthWidget#createOAuthLoginView()
   * createOAuthLoginView()}.
   */
  protected void createLoginView() {
    this.setTemplateText(tr("Wt.Auth.template.login"));
    this.createPasswordLoginView();
    this.createOAuthLoginView();
  }
  /**
   * Creates the view shown when the user is logged in.
   *
   * <p>The default implementation renders the <code>&quot;Wt.Auth.template.logged-in&quot;</code>
   * template.
   */
  protected void createLoggedInView() {
    this.setTemplateText(tr("Wt.Auth.template.logged-in"));
    this.bindString("user-name", this.login_.getUser().getIdentity(Identity.LoginName));
    WPushButton logout = new WPushButton(tr("Wt.Auth.logout"));
    this.bindWidget("logout", logout);
    logout
        .clicked()
        .addListener(
            this,
            (WMouseEvent e1) -> {
              AuthWidget.this.logout();
            });
  }
  /**
   * Creates a password login view.
   *
   * <p>This is used by the default implementation of {@link AuthWidget#createLoginView()
   * createLoginView()} to prompt for the information needed for logging in using a username and
   * password. The default implementation implements a view guided by the {@link
   * AuthWidget#getModel() getModel()}.
   *
   * <p>
   *
   * @see AuthWidget#createLoginView()
   */
  protected void createPasswordLoginView() {
    this.updatePasswordLoginView();
  }
  /**
   * Creates a widget to login using OAuth.
   *
   * <p>The default implementation adds an icon for each OAuth service provider available. The icon
   * that will be used for each service is a PNG file with a path based on the {@link
   * OAuthService#getName()} of the service. If the name is is &quot;myService&quot;, then the icon
   * path will be &quot;css/oauth-myService.png&quot;. JWt does not bundle any icons by default, so
   * you should make sure that the icon is in place.
   *
   * <p>There&apos;s a lot to say about making a usable login mechanism for OAuth (and federated
   * login services in general), see <a
   * href="https://sites.google.com/site/oauthgoog/UXFedLogin">https://sites.google.com/site/oauthgoog/UXFedLogin</a>.
   *
   * <p>
   *
   * @see AuthWidget#createLoginView()
   */
  protected void createOAuthLoginView() {
    if (!this.model_.getOAuth().isEmpty()) {
      this.setCondition("if:oauth", true);
      WContainerWidget icons = new WContainerWidget();
      this.bindWidget("icons", icons);
      icons.setInline(this.isInline());
      for (int i = 0; i < this.model_.getOAuth().size(); ++i) {
        OAuthService service = this.model_.getOAuth().get(i);
        OAuthWidget w = new OAuthWidget(service);
        icons.addWidget(w);
        w.authenticated()
            .addListener(
                this,
                (OAuthProcess e1, Identity e2) -> {
                  AuthWidget.this.oAuthDone(e1, e2);
                });
      }
    }
  }
  /**
   * Shows a dialog.
   *
   * <p>This shows a dialog. The default method creates a standard {@link WDialog}, with the given
   * <code>title</code> and <code>contents</code> as central widget.
   *
   * <p>When the central widget is deleted, it deletes the dialog.
   */
  protected WDialog showDialog(final CharSequence title, WWidget contents) {
    if (contents != null) {
      this.dialog_ = new WDialog(title);
      this.dialog_.getContents().addWidget(contents);
      this.dialog_
          .getContents()
          .childrenChanged()
          .addListener(
              this,
              () -> {
                AuthWidget.this.closeDialog();
              });
      this.dialog_.getFooter().hide();
      if (!WApplication.getInstance().getEnvironment().hasAjax()) {
        this.dialog_.setMargin(new WLength("-21em"), EnumSet.of(Side.Left));
        this.dialog_.setMargin(new WLength("-200px"), EnumSet.of(Side.Top));
      }
      this.dialog_.show();
    }
    return this.dialog_;
  }
  /**
   * Creates a registration model.
   *
   * <p>This method creates a registration model. The default implementation creates a
   * RegistrationModel() but you may want to reimplement this function to return a specialized
   * registration model (complementing a specialized registration view).
   *
   * <p>
   *
   * @see AuthWidget#registerNewUser()
   */
  protected RegistrationModel getCreateRegistrationModel() {
    RegistrationModel result =
        new RegistrationModel(this.model_.getBaseAuth(), this.model_.getUsers(), this.login_);
    if (this.model_.getPasswordAuth() != null) {
      result.addPasswordAuth(this.model_.getPasswordAuth());
    }
    result.addOAuth(this.model_.getOAuth());
    return result;
  }

  protected WWidget createFormWidget(String field) {
    WFormWidget result = null;
    if (field == AuthModel.LoginNameField) {
      result = new WLineEdit();
      result.setFocus(true);
    } else {
      if (field == AuthModel.PasswordField) {
        WPasswordEdit p = new WPasswordEdit();
        p.enterPressed()
            .addListener(
                this,
                () -> {
                  AuthWidget.this.attemptPasswordLogin();
                });
        result = p;
      } else {
        if (field == AuthModel.RememberMeField) {
          result = new WCheckBox();
        }
      }
    }
    return result;
  }

  protected void render(EnumSet<RenderFlag> flags) {
    if (!this.created_) {
      this.create();
      this.created_ = true;
    }
    super.render(flags);
  }

  private AuthModel model_;
  private RegistrationModel registrationModel_;
  private final Login login_;
  private String basePath_;
  private boolean registrationEnabled_;
  private boolean created_;
  private WDialog dialog_;
  private WMessageBox messageBox_;
  private AbstractMfaProcess mfaWidget_;

  private void init() {
    this.setWidgetIdMode(TemplateWidgetIdMode.SetObjectName);
    this.registrationEnabled_ = false;
    this.created_ = false;
    WApplication app = WApplication.getInstance();
    app.internalPathChanged()
        .addListener(
            this,
            (String e1) -> {
              AuthWidget.this.onPathChange(e1);
            });
    app.getTheme().apply(this, this, WidgetThemeRole.AuthWidgets);
    this.login_
        .changed()
        .addListener(
            this,
            () -> {
              AuthWidget.this.onLoginChange();
            });
  }

  private void logout() {
    this.model_.logout(this.login_);
  }
  // private void loginThrottle(int delay) ;
  private void closeDialog() {
    if (this.dialog_ != null) {
      {
        WDialog toRemove = this.dialog_;
        if (toRemove != null) toRemove.remove();
      }

      this.dialog_ = null;
    } else {
      {
        WMessageBox toRemove = this.messageBox_;
        if (toRemove != null) toRemove.remove();
      }

      this.messageBox_ = null;
    }
    if (this.basePath_.length() != 0) {
      WApplication app = WApplication.getInstance();
      if (app.internalPathMatches(this.basePath_)) {
        String ap = app.internalSubPath(this.basePath_);
        if (ap.equals("register/")) {
          app.setInternalPath(this.basePath_, false);
        }
      }
    }
  }

  private void onLoginChange() {
    if (!(this.isRendered() || this.created_)) {
      return;
    }
    if (this.login_.isLoggedIn()
        || this.login_.getUser().isValid() && this.login_.getState() == LoginState.RequiresMfa) {
      if (this.login_.getState() == LoginState.RequiresMfa) {
        this.createMfaView();
      } else {
        this.createLoggedInView();
      }
    } else {
      if (this.login_.getState() != LoginState.Disabled) {
        if (this.model_.getBaseAuth().isAuthTokensEnabled()) {
          WApplication.getInstance()
              .removeCookie(this.model_.getBaseAuth().getAuthTokenCookieName());
        }
        this.model_.reset();
        this.createLoginView();
      } else {
        this.createLoginView();
      }
    }
  }

  private void onPathChange(final String path) {
    this.handleRegistrationPath(path);
  }

  private boolean handleRegistrationPath(final String path) {
    if (this.basePath_.length() != 0) {
      WApplication app = WApplication.getInstance();
      if (app.internalPathMatches(this.basePath_)) {
        String ap = app.internalSubPath(this.basePath_);
        if (ap.equals("register/")) {
          this.registerNewUser();
          return true;
        }
      }
    }
    return false;
  }
  // private void oAuthStateChange(OAuthProcess  process) ;
  private void oAuthDone(OAuthProcess oauth, final Identity identity) {
    if (identity.isValid()) {
      logger.warn(
          new StringWriter()
              .append("secure:")
              .append(oauth.getService().getName())
              .append(": identified: as ")
              .append(identity.getId())
              .append(", ")
              .append(identity.getName())
              .append(", ")
              .append(identity.getEmail())
              .toString());
      try (AbstractUserDatabase.Transaction t = this.model_.getUsers().startTransaction(); ) {
        User user = this.model_.getBaseAuth().identifyUser(identity, this.model_.getUsers());
        if (user.isValid()) {
          this.model_.loginUser(this.login_, user);
        } else {
          this.registerNewUser(identity);
        }
        if (t != null) {
          t.commit();
        }
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else {
      logger.warn(
          new StringWriter()
              .append("secure:")
              .append(oauth.getService().getName())
              .append(": error: ")
              .append(oauth.getError())
              .toString());
      this.displayError(oauth.getError());
    }
  }

  private void updatePasswordLoginView() {
    if (this.model_.getPasswordAuth() != null) {
      this.setCondition("if:passwords", true);
      this.updateView(this.model_);
      WInteractWidget login = (WInteractWidget) this.resolveWidget("login");
      if (!(login != null)) {
        login = new WPushButton(tr("Wt.Auth.login"));
        this.bindWidget("login", login);
        login
            .clicked()
            .addListener(
                this,
                (WMouseEvent e1) -> {
                  AuthWidget.this.attemptPasswordLogin();
                });
        this.model_.configureThrottling(login);
        if (this.model_.getBaseAuth().isEmailVerificationEnabled()) {
          WText text = new WText(tr("Wt.Auth.lost-password"));
          this.bindWidget("lost-password", text);
          text.clicked()
              .addListener(
                  this,
                  (WMouseEvent e1) -> {
                    AuthWidget.this.handleLostPassword();
                  });
        } else {
          this.bindEmpty("lost-password");
        }
        if (this.registrationEnabled_) {
          if (this.basePath_.length() != 0) {
            this.bindWidget(
                "register",
                new WAnchor(
                    new WLink(LinkType.InternalPath, this.basePath_ + "register"),
                    tr("Wt.Auth.register")));
          } else {
            WText t = new WText(tr("Wt.Auth.register"));
            this.bindWidget("register", t);
            t.clicked()
                .addListener(
                    this,
                    (WMouseEvent e1) -> {
                      AuthWidget.this.registerNewUser();
                    });
          }
        } else {
          this.bindEmpty("register");
        }
        if (this.model_.getBaseAuth().isEmailVerificationEnabled() && this.registrationEnabled_) {
          this.bindString("sep", " | ");
        } else {
          this.bindEmpty("sep");
        }
      }
      if (this.model_.isShowResendEmailVerification()) {
        WAnchor resendAnchor = (WAnchor) this.bindWidget("user-confirm-email", new WAnchor());
        resendAnchor.setText(WString.tr("Wt.Auth.resend-email-verification"));
        resendAnchor
            .clicked()
            .addListener(
                this,
                (WMouseEvent e1) -> {
                  AuthWidget.this.letResendEmailVerification();
                });
      } else {
        this.bindEmpty("user-confirm-email");
      }
      this.model_.updateThrottling(login);
    } else {
      this.bindEmpty("lost-password");
      this.bindEmpty("sep");
      this.bindEmpty("register");
      this.bindEmpty("login");
    }
  }
}
