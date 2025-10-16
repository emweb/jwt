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
 * A widget which allows a user to choose a new password.
 *
 * <p>This widget lets a user choose a new password.
 *
 * <p>The widget renders the <code>&quot;Wt.Auth.template.update-password&quot;</code> template.
 * Optionally, it asks for the current password, as well as a new password.
 *
 * <p>
 *
 * @see AuthWidget#createUpdatePasswordView(User user, boolean promptPassword)
 */
public class UpdatePasswordWidget extends WTemplateFormView {
  private static Logger logger = LoggerFactory.getLogger(UpdatePasswordWidget.class);

  /**
   * Constructor.
   *
   * <p>If <code>authModel</code> is not <code>null</code>, the user also has to authenticate first
   * using his current password.
   */
  public UpdatePasswordWidget(
      final User user,
      RegistrationModel registrationModel,
      final AuthModel authModel,
      WContainerWidget parentContainer) {
    super(tr("Wt.Auth.template.update-password"), (WContainerWidget) null);
    this.user_ = user;
    this.registrationModel_ = registrationModel;
    this.authModel_ = authModel;
    this.updated_ = new Signal();
    this.canceled_ = new Signal();
    this.registrationModel_.setValue(
        RegistrationModel.LoginNameField, user.getIdentity(Identity.LoginName));
    this.registrationModel_.setReadOnly(RegistrationModel.LoginNameField, true);
    if (user.getPassword().isEmpty()) {
      this.authModel_ = null;
    } else {
      if (this.authModel_ != null) {
        this.authModel_.reset();
      }
    }
    if (this.authModel_ != null && this.authModel_.getBaseAuth().isEmailVerificationEnabled()) {
      this.registrationModel_.setValue(
          RegistrationModel.EmailField, user.getEmail() + " " + user.getUnverifiedEmail());
    }
    this.registrationModel_.setVisible(RegistrationModel.EmailField, false);
    WPushButton okButton = new WPushButton(tr("Wt.WMessageBox.Ok"));
    this.bindWidget("ok-button", okButton);
    WPushButton cancelButton = new WPushButton(tr("Wt.WMessageBox.Cancel"));
    this.bindWidget("cancel-button", cancelButton);
    if (this.authModel_ != null) {
      this.authModel_.setValue(AuthModel.LoginNameField, user.getIdentity(Identity.LoginName));
      this.updateViewField(this.authModel_, AuthModel.PasswordField);
      this.authModel_.configureThrottling(okButton);
      WLineEdit password = (WLineEdit) this.resolveWidget(AuthModel.PasswordField);
      password.setFocus(true);
    }
    this.updateView(this.registrationModel_);
    WLineEdit password = (WLineEdit) this.resolveWidget(RegistrationModel.ChoosePasswordField);
    WLineEdit password2 = (WLineEdit) this.resolveWidget(RegistrationModel.RepeatPasswordField);
    WText password2Info =
        (WText) this.resolveWidget(RegistrationModel.RepeatPasswordField + "-info");
    this.registrationModel_.validatePasswordsMatchJS(password, password2, password2Info);
    if (!(this.authModel_ != null)) {
      password.setFocus(true);
    }
    okButton
        .clicked()
        .addListener(
            this,
            (WMouseEvent e1) -> {
              UpdatePasswordWidget.this.doUpdate();
            });
    cancelButton
        .clicked()
        .addListener(
            this,
            (WMouseEvent e1) -> {
              UpdatePasswordWidget.this.cancel();
            });
    if (parentContainer != null) parentContainer.addWidget(this);
  }
  /**
   * Constructor.
   *
   * <p>Calls {@link #UpdatePasswordWidget(User user, RegistrationModel registrationModel, AuthModel
   * authModel, WContainerWidget parentContainer) this(user, registrationModel, authModel,
   * (WContainerWidget)null)}
   */
  public UpdatePasswordWidget(
      final User user, RegistrationModel registrationModel, final AuthModel authModel) {
    this(user, registrationModel, authModel, (WContainerWidget) null);
  }
  /** {@link Signal} emitted when the password was updated. */
  public Signal updated() {
    return this.updated_;
  }
  /** {@link Signal} emitted when cancel clicked. */
  public Signal canceled() {
    return this.canceled_;
  }

  protected WWidget createFormWidget(String field) {
    WFormWidget result = null;
    if (field == RegistrationModel.LoginNameField) {
      result = new WLineEdit();
    } else {
      if (field == AuthModel.PasswordField) {
        WPasswordEdit p = new WPasswordEdit();
        result = p;
      } else {
        if (field == RegistrationModel.ChoosePasswordField) {
          WPasswordEdit p = new WPasswordEdit();
          p.keyWentUp()
              .addListener(
                  this,
                  (WKeyEvent e1) -> {
                    UpdatePasswordWidget.this.checkPassword();
                  });
          p.changed()
              .addListener(
                  this,
                  () -> {
                    UpdatePasswordWidget.this.checkPassword();
                  });
          result = p;
        } else {
          if (field == RegistrationModel.RepeatPasswordField) {
            WPasswordEdit p = new WPasswordEdit();
            p.changed()
                .addListener(
                    this,
                    () -> {
                      UpdatePasswordWidget.this.checkPassword2();
                    });
            result = p;
          }
        }
      }
    }
    return result;
  }

  private User user_;
  private RegistrationModel registrationModel_;
  private AuthModel authModel_;
  private Signal updated_;
  private Signal canceled_;

  private void checkPassword() {
    this.updateModelField(this.registrationModel_, RegistrationModel.ChoosePasswordField);
    this.registrationModel_.validateField(RegistrationModel.ChoosePasswordField);
    this.updateViewField(this.registrationModel_, RegistrationModel.ChoosePasswordField);
  }

  private void checkPassword2() {
    this.updateModelField(this.registrationModel_, RegistrationModel.RepeatPasswordField);
    this.registrationModel_.validateField(RegistrationModel.RepeatPasswordField);
    this.updateViewField(this.registrationModel_, RegistrationModel.RepeatPasswordField);
  }

  private boolean validate() {
    boolean valid = true;
    if (this.authModel_ != null) {
      this.updateModelField(this.authModel_, AuthModel.PasswordField);
      if (!this.authModel_.validate()) {
        this.updateViewField(this.authModel_, AuthModel.PasswordField);
        valid = false;
      }
    }
    this.registrationModel_.validateField(RegistrationModel.LoginNameField);
    this.checkPassword();
    this.checkPassword2();
    this.registrationModel_.validateField(RegistrationModel.EmailField);
    if (!this.registrationModel_.isValid()) {
      valid = false;
    }
    return valid;
  }

  private void doUpdate() {
    if (this.validate()) {
      String password = this.registrationModel_.valueText(RegistrationModel.ChoosePasswordField);
      this.registrationModel_.getPasswordAuth().updatePassword(this.user_, password);
      this.updated_.trigger();
    }
  }

  private void cancel() {
    this.canceled_.trigger();
  }
}
