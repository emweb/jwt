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
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dialog that prompts for the user password.
 *
 * <p>This is a simple dialog, useful for prompting the user to enter his password. This may be
 * convenient for example to let the user upgrade from a weak authentication to a strong
 * authentication.
 *
 * <p>The dialog uses a {@link Login} object to get the currently identified user, and also sets the
 * result of the login process by calling {@link Login#login(User user, LoginState state)
 * Login#login()} on this object.
 *
 * <p>The dialog renders the <code>&quot;Wt.Auth.template.password-prompt&quot;</code> template.
 */
public class PasswordPromptDialog extends WDialog {
  private static Logger logger = LoggerFactory.getLogger(PasswordPromptDialog.class);

  /**
   * Constructor.
   *
   * <p>From the passed <code>login</code> object, the dialog obtains the {@link User} for which a
   * valid password needs to be entered. The result, if successful, is signalled using {@link
   * Login#login(User user, LoginState state) Login#login()}.
   */
  public PasswordPromptDialog(final Login login, AuthModel model) {
    super(tr("Wt.Auth.enter-password"));
    this.login_ = login;
    this.model_ = model;
    this.impl_ = new WTemplateFormView(tr("Wt.Auth.template.password-prompt"));
    this.model_.reset();
    this.model_.setValue(
        AuthModel.LoginNameField, this.login_.getUser().getIdentity(Identity.LoginName));
    this.model_.setReadOnly(AuthModel.LoginNameField, true);
    WLineEdit nameEdit = new WLineEdit();
    this.impl_.bindWidget(AuthModel.LoginNameField, nameEdit);
    this.impl_.updateViewField(this.model_, AuthModel.LoginNameField);
    WLineEdit passwordEdit = new WLineEdit();
    passwordEdit.setEchoMode(WLineEdit.EchoMode.Password);
    passwordEdit.setFocus(true);
    this.impl_.bindWidget(AuthModel.PasswordField, passwordEdit);
    this.impl_.updateViewField(this.model_, AuthModel.PasswordField);
    WPushButton okButton = new WPushButton(tr("Wt.WMessageBox.Ok"));
    WPushButton cancelButton = new WPushButton(tr("Wt.WMessageBox.Cancel"));
    this.model_.configureThrottling(okButton);
    this.impl_.bindWidget("ok-button", okButton);
    this.impl_.bindWidget("cancel-button", cancelButton);
    okButton
        .clicked()
        .addListener(
            this,
            new Signal1.Listener<WMouseEvent>() {
              public void trigger(WMouseEvent e1) {
                PasswordPromptDialog.this.check();
              }
            });
    cancelButton
        .clicked()
        .addListener(
            this,
            new Signal1.Listener<WMouseEvent>() {
              public void trigger(WMouseEvent e1) {
                PasswordPromptDialog.this.reject();
              }
            });
    this.getContents().addWidget(this.impl_);
    if (!WApplication.getInstance().getEnvironment().hasAjax()) {
      this.setMargin(new WLength("-21em"), EnumSet.of(Side.Left));
      this.setMargin(new WLength("-200px"), EnumSet.of(Side.Top));
    }
  }

  protected final Login login_;
  protected AuthModel model_;
  protected WTemplateFormView impl_;

  protected void check() {
    this.impl_.updateModelField(this.model_, AuthModel.PasswordField);
    if (this.model_.validate()) {
      Login login = this.login_;
      this.accept();
      login.login(login.getUser(), LoginState.StrongLogin);
    } else {
      this.impl_.updateViewField(this.model_, AuthModel.PasswordField);
      WPushButton okButton = (WPushButton) this.impl_.resolveWidget("ok-button");
      this.model_.updateThrottling(okButton);
    }
  }
}
