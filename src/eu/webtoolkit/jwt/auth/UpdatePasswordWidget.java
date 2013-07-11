/*
 * Copyright (C) 2009 Emweb bvba, Leuven, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.auth;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.lang.ref.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.*;
import javax.servlet.*;
import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.utils.*;
import eu.webtoolkit.jwt.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A widget which allows a user to choose a new password.
 * <p>
 * 
 * This widget lets a user choose a new password.
 * <p>
 * The widget renders the
 * <code>&quot;Wt.Auth.template.update-password&quot;</code> template.
 * Optionally, it asks for the current password, as well as a new password.
 * <p>
 * 
 * @see AuthWidget#createUpdatePasswordView(User user, boolean promptPassword)
 */
public class UpdatePasswordWidget extends WTemplateFormView {
	private static Logger logger = LoggerFactory
			.getLogger(UpdatePasswordWidget.class);

	/**
	 * Constructor.
	 * <p>
	 * If <code>authModel</code> is not <code>null</code>, the user also has to
	 * authenticate first using his current password.
	 */
	public UpdatePasswordWidget(User user, RegistrationModel registrationModel,
			AuthModel authModel, WContainerWidget parent) {
		super(tr("Wt.Auth.template.update-password"), parent);
		this.user_ = user;
		this.registrationModel_ = registrationModel;
		this.authModel_ = authModel;
		this.registrationModel_.setValue(RegistrationModel.LoginNameField, user
				.getIdentity(Identity.LoginName));
		this.registrationModel_.setReadOnly(RegistrationModel.LoginNameField,
				true);
		if (this.authModel_ != null
				&& this.authModel_.getBaseAuth().isEmailVerificationEnabled()) {
			this.registrationModel_.setValue(RegistrationModel.EmailField, user
					.getEmail()
					+ " " + user.getUnverifiedEmail());
		}
		this.registrationModel_.setVisible(RegistrationModel.EmailField, false);
		WPushButton okButton = new WPushButton(tr("Wt.WMessageBox.Ok"));
		WPushButton cancelButton = new WPushButton(tr("Wt.WMessageBox.Cancel"));
		if (this.authModel_ != null) {
			this.authModel_.setValue(AuthModel.LoginNameField, user
					.getIdentity(Identity.LoginName));
			this.updateViewField(this.authModel_, AuthModel.PasswordField);
			this.authModel_.configureThrottling(okButton);
			WLineEdit password = (WLineEdit) this
					.resolveWidget(AuthModel.PasswordField);
			password.setFocus();
		}
		this.updateView(this.registrationModel_);
		WLineEdit password = (WLineEdit) this
				.resolveWidget(RegistrationModel.ChoosePasswordField);
		WLineEdit password2 = (WLineEdit) this
				.resolveWidget(RegistrationModel.RepeatPasswordField);
		WText password2Info = (WText) this
				.resolveWidget(RegistrationModel.RepeatPasswordField + "-info");
		this.registrationModel_.validatePasswordsMatchJS(password, password2,
				password2Info);
		if (!(this.authModel_ != null)) {
			password.setFocus();
		}
		okButton.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						UpdatePasswordWidget.this.doUpdate();
					}
				});
		cancelButton.clicked().addListener(this,
				new Signal1.Listener<WMouseEvent>() {
					public void trigger(WMouseEvent e1) {
						UpdatePasswordWidget.this.close();
					}
				});
		this.bindWidget("ok-button", okButton);
		this.bindWidget("cancel-button", cancelButton);
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls
	 * {@link #UpdatePasswordWidget(User user, RegistrationModel registrationModel, AuthModel authModel, WContainerWidget parent)
	 * this(user, registrationModel, authModel, (WContainerWidget)null)}
	 */
	public UpdatePasswordWidget(User user, RegistrationModel registrationModel,
			AuthModel authModel) {
		this(user, registrationModel, authModel, (WContainerWidget) null);
	}

	protected WFormWidget createFormWidget(String field) {
		WFormWidget result = null;
		if (field == RegistrationModel.LoginNameField) {
			result = new WLineEdit();
		} else {
			if (field == AuthModel.PasswordField) {
				WLineEdit p = new WLineEdit();
				p.setEchoMode(WLineEdit.EchoMode.Password);
				result = p;
			} else {
				if (field == RegistrationModel.ChoosePasswordField) {
					WLineEdit p = new WLineEdit();
					p.setEchoMode(WLineEdit.EchoMode.Password);
					p.keyWentUp().addListener(this, new Signal.Listener() {
						public void trigger() {
							UpdatePasswordWidget.this.checkPassword();
						}
					});
					p.changed().addListener(this, new Signal.Listener() {
						public void trigger() {
							UpdatePasswordWidget.this.checkPassword();
						}
					});
					result = p;
				} else {
					if (field == RegistrationModel.RepeatPasswordField) {
						WLineEdit p = new WLineEdit();
						p.setEchoMode(WLineEdit.EchoMode.Password);
						p.changed().addListener(this, new Signal.Listener() {
							public void trigger() {
								UpdatePasswordWidget.this.checkPassword2();
							}
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

	private void checkPassword() {
		this.updateModelField(this.registrationModel_,
				RegistrationModel.ChoosePasswordField);
		this.registrationModel_
				.validateField(RegistrationModel.ChoosePasswordField);
		this.updateViewField(this.registrationModel_,
				RegistrationModel.ChoosePasswordField);
	}

	private void checkPassword2() {
		this.updateModelField(this.registrationModel_,
				RegistrationModel.RepeatPasswordField);
		this.registrationModel_
				.validateField(RegistrationModel.RepeatPasswordField);
		this.updateViewField(this.registrationModel_,
				RegistrationModel.RepeatPasswordField);
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
			String password = this.registrationModel_
					.valueText(RegistrationModel.ChoosePasswordField);
			this.registrationModel_.getPasswordAuth().updatePassword(
					this.user_, password);
			this.registrationModel_.getLogin().login(this.user_);
			this.close();
		}
	}

	private void close() {
		if (this != null)
			this.remove();
	}
}
