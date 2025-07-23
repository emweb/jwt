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
 * A registration widget.
 *
 * <p>This implements a widget which allows a new user to register. The widget renders the <code>
 * &quot;Wt.Auth.template.registration&quot;</code> template. and uses a {@link RegistrationModel}
 * for the actual registration logic.
 *
 * <p>Typically, you may want to specialize this widget to ask for other information.
 */
public class RegistrationWidget extends WTemplateFormView {
  private static Logger logger = LoggerFactory.getLogger(RegistrationWidget.class);

  /**
   * Constructor.
   *
   * <p>Creates a new authentication.
   */
  public RegistrationWidget(AuthWidget authWidget, WContainerWidget parentContainer) {
    super(tr("Wt.Auth.template.registration"), (WContainerWidget) null);
    this.authWidget_ = authWidget;
    this.model_ = null;
    this.created_ = false;
    this.confirmPasswordLogin_ = null;
    this.isYouDialog_ = null;
    this.setWidgetIdMode(TemplateWidgetIdMode.SetObjectName);
    WApplication app = WApplication.getInstance();
    app.getTheme().apply(this, this, WidgetThemeRole.AuthWidgets);
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #RegistrationWidget(AuthWidget authWidget, WContainerWidget parentContainer)
   * this((AuthWidget)null, (WContainerWidget)null)}
   */
  public RegistrationWidget() {
    this((AuthWidget) null, (WContainerWidget) null);
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #RegistrationWidget(AuthWidget authWidget, WContainerWidget parentContainer)
   * this(authWidget, (WContainerWidget)null)}
   */
  public RegistrationWidget(AuthWidget authWidget) {
    this(authWidget, (WContainerWidget) null);
  }

  public void remove() {
    super.remove();
  }
  /** Sets the registration model. */
  public void setModel(RegistrationModel model) {
    if (!(this.model_ != null) && model != null) {
      model
          .getLogin()
          .changed()
          .addListener(
              this,
              () -> {
                RegistrationWidget.this.close();
              });
    }
    this.model_ = model;
  }
  /**
   * Returns the registration model.
   *
   * <p>This returns the model that is used by the widget to do the actual registration.
   */
  public RegistrationModel getModel() {
    return this.model_;
  }
  /**
   * Updates the user-interface.
   *
   * <p>This updates the user-interface to reflect the current state of the model.
   */
  public void update() {
    if (this.model_.getPasswordAuth() != null) {
      this.bindString("password-description", tr("Wt.Auth.password-registration"));
    } else {
      this.bindEmpty("password-description");
    }
    this.updateView(this.model_);
    if (!this.created_) {
      WLineEdit password = (WLineEdit) this.resolveWidget(RegistrationModel.ChoosePasswordField);
      WLineEdit password2 = (WLineEdit) this.resolveWidget(RegistrationModel.RepeatPasswordField);
      WText password2Info =
          (WText) this.resolveWidget(RegistrationModel.RepeatPasswordField + "-info");
      if (password != null && password2 != null && password2Info != null) {
        this.model_.validatePasswordsMatchJS(password, password2, password2Info);
      } else {
        this.bindEmpty("password-description");
      }
    }
    WAnchor isYou = (WAnchor) this.resolveWidget("confirm-is-you");
    if (!(isYou != null)) {
      WAnchor newIsYou =
          isYou =
              new WAnchor(new WLink("#"), tr("Wt.Auth.confirm-is-you"), (WContainerWidget) null);
      newIsYou.hide();
      this.bindWidget("confirm-is-you", newIsYou);
    }
    if (this.model_.isConfirmUserButtonVisible()) {
      if (!isYou.clicked().isConnected()) {
        isYou
            .clicked()
            .addListener(
                this,
                (WMouseEvent e1) -> {
                  RegistrationWidget.this.confirmIsYou();
                });
      }
      isYou.show();
    } else {
      isYou.hide();
    }
    if (this.model_.isFederatedLoginVisible()) {
      if (!this.conditionValue("if:oauth")) {
        this.setCondition("if:oauth", true);
        if (this.model_.getPasswordAuth() != null) {
          this.bindString("oauth-description", tr("Wt.Auth.or-oauth-registration"));
        } else {
          this.bindString("oauth-description", tr("Wt.Auth.oauth-registration"));
        }
        WContainerWidget icons = new WContainerWidget();
        this.bindWidget("icons", icons);
        icons.addStyleClass("Wt-field");
        for (int i = 0; i < this.model_.getOAuth().size(); ++i) {
          OAuthService service = this.model_.getOAuth().get(i);
          OAuthWidget w = new OAuthWidget(service);
          icons.addWidget(w);
          w.authenticated()
              .addListener(
                  this,
                  (OAuthProcess e1, Identity e2) -> {
                    RegistrationWidget.this.oAuthDone(e1, e2);
                  });
        }
      }
    } else {
      this.setCondition("if:oauth", false);
      this.bindEmpty("icons");
    }
    if (!this.created_) {
      WPushButton okButton = new WPushButton(tr("Wt.Auth.register"));
      this.bindWidget("ok-button", okButton);
      WPushButton cancelButton = new WPushButton(tr("Wt.WMessageBox.Cancel"));
      this.bindWidget("cancel-button", cancelButton);
      okButton
          .clicked()
          .addListener(
              this,
              (WMouseEvent e1) -> {
                RegistrationWidget.this.doRegister();
              });
      cancelButton
          .clicked()
          .addListener(
              this,
              (WMouseEvent e1) -> {
                RegistrationWidget.this.close();
              });
      this.created_ = true;
    }
  }
  /**
   * Validates the current information.
   *
   * <p>The default implementation simply calls {@link WFormModel#validate()} on the model.
   *
   * <p>You may want to reimplement this method if you&apos;ve added other information to the
   * registration form that need validation.
   */
  protected boolean validate() {
    return this.model_.validate();
  }
  /**
   * Performs the registration.
   *
   * <p>The default implementation checks if the information is valid with {@link
   * RegistrationWidget#validate() validate()}, and then calls {@link
   * RegistrationModel#doRegister()}. If registration was successful, it calls {@link
   * RegistrationWidget#registerUserDetails(User user) registerUserDetails()} and subsequently logs
   * the user in.
   */
  protected void doRegister() {
    try (AbstractUserDatabase.Transaction t = this.model_.getUsers().startTransaction(); ) {
      this.updateModel(this.model_);
      if (this.validate()) {
        User user = this.model_.doRegister();
        if (user.isValid()) {
          this.registerUserDetails(user);
          if (!this.model_.getBaseAuth().isEmailVerificationRequired()
              || user.getUnverifiedEmail().length() == 0) {
            this.model_.loginUser(this.model_.getLogin(), user);
          } else {
            if (this.authWidget_ != null) {
              this.authWidget_.displayInfo(WString.tr("Wt.Auth.confirm-email-first"));
            }
            this.close();
          }
        } else {
          this.update();
        }
      } else {
        this.update();
      }
      if (t != null) {
        t.commit();
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  /**
   * Closes the registration widget.
   *
   * <p>The default implementation simply deletes the widget.
   */
  protected void close() {
    {
      WWidget toRemove = this.removeFromParent();
      if (toRemove != null) toRemove.remove();
    }
  }
  /**
   * Registers more user information.
   *
   * <p>This method is called when a new user has been successfully registered.
   *
   * <p>You may want to reimplement this method if you&apos;ve added other information to the
   * registration form which needs to be annotated to the user.
   */
  protected void registerUserDetails(final User user) {}

  protected void render(EnumSet<RenderFlag> flags) {
    if (!this.created_) {
      this.update();
      this.created_ = true;
    }
    super.render(flags);
  }

  protected WWidget createFormWidget(String field) {
    WFormWidget result = null;
    if (field == RegistrationModel.LoginNameField) {
      result = new WLineEdit();
      result
          .changed()
          .addListener(
              this,
              () -> {
                RegistrationWidget.this.checkLoginName();
              });
    } else {
      if (field == RegistrationModel.EmailField) {
        result = new WLineEdit();
      } else {
        if (field == RegistrationModel.ChoosePasswordField) {
          WPasswordEdit p = new WPasswordEdit();
          p.keyWentUp()
              .addListener(
                  this,
                  (WKeyEvent e1) -> {
                    RegistrationWidget.this.checkPassword();
                  });
          p.changed()
              .addListener(
                  this,
                  () -> {
                    RegistrationWidget.this.checkPassword();
                  });
          result = p;
        } else {
          if (field == RegistrationModel.RepeatPasswordField) {
            WPasswordEdit p = new WPasswordEdit();
            p.changed()
                .addListener(
                    this,
                    () -> {
                      RegistrationWidget.this.checkPassword2();
                    });
            result = p;
          }
        }
      }
    }
    return result;
  }

  private AuthWidget authWidget_;
  private RegistrationModel model_;
  private boolean created_;
  private Login confirmPasswordLogin_;
  private WDialog isYouDialog_;

  private void checkLoginName() {
    this.updateModelField(this.model_, RegistrationModel.LoginNameField);
    this.model_.validateField(RegistrationModel.LoginNameField);
    this.model_.setValidated(RegistrationModel.LoginNameField, false);
    this.update();
  }

  private void checkPassword() {
    this.updateModelField(this.model_, RegistrationModel.LoginNameField);
    this.updateModelField(this.model_, RegistrationModel.ChoosePasswordField);
    this.updateModelField(this.model_, RegistrationModel.EmailField);
    this.model_.validateField(RegistrationModel.ChoosePasswordField);
    this.model_.setValidated(RegistrationModel.ChoosePasswordField, false);
    this.update();
  }

  private void checkPassword2() {
    this.updateModelField(this.model_, RegistrationModel.ChoosePasswordField);
    this.updateModelField(this.model_, RegistrationModel.RepeatPasswordField);
    this.model_.validateField(RegistrationModel.RepeatPasswordField);
    this.model_.setValidated(RegistrationModel.RepeatPasswordField, false);
    this.update();
  }

  private void confirmIsYou() {
    this.updateModel(this.model_);
    switch (this.model_.getConfirmIsExistingUser()) {
      case ConfirmWithPassword:
        {
          this.confirmPasswordLogin_ = new Login();
          this.confirmPasswordLogin_.login(this.model_.getExistingUser(), LoginState.Weak);
          this.confirmPasswordLogin_
              .changed()
              .addListener(
                  this,
                  () -> {
                    RegistrationWidget.this.confirmedIsYou();
                  });
          this.isYouDialog_ =
              this.authWidget_.createPasswordPromptDialog(this.confirmPasswordLogin_);
          this.isYouDialog_
              .finished()
              .addListener(
                  this,
                  () -> {
                    {
                      WDialog toRemove = RegistrationWidget.this.isYouDialog_;
                      if (toRemove != null) toRemove.remove();
                    }

                    RegistrationWidget.this.isYouDialog_ = null;
                  });
          this.isYouDialog_.show();
        }
        break;
      case ConfirmWithEmail:
        logger.info(
            new StringWriter()
                .append("confirming a new identity to existing user not yet implemented")
                .toString());
        break;
      default:
        logger.error(new StringWriter().append("that's gone haywire.").toString());
    }
  }

  private void confirmedIsYou() {
    if (this.confirmPasswordLogin_.getState() == LoginState.Strong) {
      this.model_.existingUserConfirmed();
    } else {
      this.confirmPasswordLogin_ = null;
    }
  }

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
      if (!this.model_.registerIdentified(identity)) {
        this.update();
      }
    } else {
      if (this.authWidget_ != null) {
        this.authWidget_.displayError(oauth.getError());
      }
      logger.warn(
          new StringWriter()
              .append("secure:")
              .append(oauth.getService().getName())
              .append(": error: ")
              .append(oauth.getError())
              .toString());
    }
  }
}
