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
 * Helper class for creating a form to let a user enter his password.
 * <p>
 * 
 * This class implements the process of letting the user enter his password. It
 * also implements attempt throttling.
 * <p>
 * 
 * @see AuthWidget
 * @see PasswordPromptDialog
 * @see UpdatePasswordWidget
 */
public class EnterPasswordFields extends WObject {
	private static Logger logger = LoggerFactory
			.getLogger(EnterPasswordFields.class);

	/**
	 * Constructor.
	 * <p>
	 * The object uses a password entry field, a feed-back text field (for error
	 * feed-back and/or help text), and optionally also manipulates the
	 * &quot;login&quot;/&quot;ok&quot; button to indicate that attempts are
	 * currently refused because of password attempt throttling.
	 */
	public EnterPasswordFields(AbstractPasswordService auth,
			WLineEdit password, WText passwordInfo, WPushButton okButton,
			WObject parent) {
		super(parent);
		this.auth_ = auth;
		this.password_ = password;
		this.okButton_ = okButton;
		this.passwordInfo_ = passwordInfo;
		this.init();
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls
	 * {@link #EnterPasswordFields(AbstractPasswordService auth, WLineEdit password, WText passwordInfo, WPushButton okButton, WObject parent)
	 * this(auth, password, passwordInfo, okButton, (WObject)null)}
	 */
	public EnterPasswordFields(AbstractPasswordService auth,
			WLineEdit password, WText passwordInfo, WPushButton okButton) {
		this(auth, password, passwordInfo, okButton, (WObject) null);
	}

	/**
	 * Validates the entered password.
	 * <p>
	 * This validates the entered password, and provides the necessary feed-back
	 * in the info field and ok button.
	 * <p>
	 * Returns whether the entered password is valid.
	 */
	public boolean validate(User user) {
		boolean result = false;
		int throttlingDelay = 0;
		if (user.isValid()) {
			PasswordResult r = this.auth_.verifyPassword(user, this.password_
					.getText());
			switch (r) {
			case PasswordInvalid:
				if (this.passwordInfo_ != null) {
					this.passwordInfo_.setText(WString
							.tr("Wt.Auth.password-invalid"));
					this.passwordInfo_.addStyleClass("Wt-error");
				}
				this.password_.removeStyleClass("Wt-valid", true);
				this.password_.addStyleClass("Wt-invalid", true);
				if (this.auth_.isAttemptThrottlingEnabled()) {
					throttlingDelay = this.auth_.delayForNextAttempt(user);
				}
				break;
			case LoginThrottling:
				this.password_.removeStyleClass("Wt-invalid", true);
				throttlingDelay = this.auth_.delayForNextAttempt(user);
				logger.warn(new StringWriter().append("secure:").append(
						"throttling: ").append(String.valueOf(throttlingDelay))
						.append(" seconds for ").append(
								user.identity(Identity.LoginName)).toString());
				break;
			case PasswordValid:
				this.password_.removeStyleClass("Wt-invalid", true);
				this.password_.addStyleClass("Wt-valid", true);
				result = true;
			}
		}
		if (this.auth_.isAttemptThrottlingEnabled()) {
			this.loginThrottle(throttlingDelay);
		}
		return result;
	}

	private AbstractPasswordService auth_;
	private WLineEdit password_;
	private WPushButton okButton_;
	private WText passwordInfo_;

	private void init() {
		this.password_.setEchoMode(WLineEdit.EchoMode.Password);
		if (this.passwordInfo_ != null) {
			this.passwordInfo_.setText(WString.tr("Wt.Auth.password-info"));
		}
		if (this.auth_.isAttemptThrottlingEnabled()) {
			this.enableThrottlingJS();
		}
	}

	private void enableThrottlingJS() {
		WApplication app = WApplication.getInstance();
		app.loadJavaScript("js/AuthEnterPasswordFields.js", wtjs1());
		this.okButton_.doJavaScript("new Wt3_2_0.AuthThrottle(Wt3_2_0,"
				+ this.okButton_.getJsRef()
				+ ","
				+ WString.toWString(WString.tr("Wt.Auth.throttle-retry"))
						.getJsStringLiteral() + ");");
	}

	private void loginThrottle(int delay) {
		StringBuilder s = new StringBuilder();
		s.append("jQuery.data(").append(this.okButton_.getJsRef()).append(
				", 'throttle').reset(").append(delay).append(");");
		this.okButton_.doJavaScript(s.toString());
	}

	static WJavaScriptPreamble wtjs1() {
		return new WJavaScriptPreamble(
				JavaScriptScope.WtClassScope,
				JavaScriptObjectType.JavaScriptConstructor,
				"AuthThrottle",
				"function(e,a,h){function f(){clearInterval(b);b=null;e.setHtml(a,d);a.disabled=false;d=null}function g(){if(c==0)f();else{e.setHtml(a,h.replace(\"{1}\",c));--c}}jQuery.data(a,\"throttle\",this);var b=null,d=null,c=0;this.reset=function(i){b&&f();d=a.innerHTML;if(c=i){b=setInterval(g,1E3);a.disabled=true;g()}}}");
	}
}
