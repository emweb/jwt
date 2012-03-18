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
 * A base model class for authentication-related forms.
 * <p>
 * 
 * This class manages the the auth services and the user database which an
 * authentication model will use to implement a form..
 */
public class FormBaseModel extends WFormModel {
	private static Logger logger = LoggerFactory.getLogger(FormBaseModel.class);

	/**
	 * Constructor.
	 */
	public FormBaseModel(AuthService baseAuth, AbstractUserDatabase users,
			WObject parent) {
		super();
		this.baseAuth_ = baseAuth;
		this.users_ = users;
		this.passwordAuth_ = null;
		this.oAuth_ = new ArrayList<OAuthService>();
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls
	 * {@link #FormBaseModel(AuthService baseAuth, AbstractUserDatabase users, WObject parent)
	 * this(baseAuth, users, (WObject)null)}
	 */
	public FormBaseModel(AuthService baseAuth, AbstractUserDatabase users) {
		this(baseAuth, users, (WObject) null);
	}

	/**
	 * Returns the authentication base service.
	 * <p>
	 * This returns the service passed through the constructor.
	 */
	public AuthService getBaseAuth() {
		return this.baseAuth_;
	}

	/**
	 * Returns the user database.
	 */
	public AbstractUserDatabase getUsers() {
		return this.users_;
	}

	/**
	 * Adds a password authentication service.
	 * <p>
	 * This enables password-based registration, including choosing a proper
	 * password.
	 * <p>
	 * Only one password authentication service can be configured.
	 * <p>
	 * 
	 * @see FormBaseModel#addOAuth(OAuthService auth)
	 */
	public void addPasswordAuth(AbstractPasswordService auth) {
		this.passwordAuth_ = auth;
	}

	/**
	 * Returns the password authentication service.
	 * <p>
	 * 
	 * @see FormBaseModel#addPasswordAuth(AbstractPasswordService auth)
	 */
	public AbstractPasswordService getPasswordAuth() {
		return this.passwordAuth_;
	}

	/**
	 * Adds an OAuth authentication service provider.
	 * <p>
	 * This enables OAuth-based registration. More than one OAuth authentication
	 * service can be configured: one for each supported third-party OAuth
	 * identity provider.
	 * <p>
	 * 
	 * @see FormBaseModel#addPasswordAuth(AbstractPasswordService auth)
	 */
	public void addOAuth(OAuthService auth) {
		this.oAuth_.add(auth);
	}

	/**
	 * Adds a list of OAuth authentication service providers.
	 * <p>
	 * 
	 * @see FormBaseModel#addOAuth(OAuthService auth)
	 */
	public void addOAuth(List<OAuthService> auth) {
		this.oAuth_.addAll(auth);
	}

	/**
	 * Returns the list of OAuth authentication service providers.
	 * <p>
	 * 
	 * @see FormBaseModel#addOAuth(OAuthService auth)
	 */
	public List<OAuthService> getOAuth() {
		return this.oAuth_;
	}

	public WString label(String field) {
		if (field == LoginNameField
				&& this.baseAuth_.getIdentityPolicy() == IdentityPolicy.EmailAddressIdentity) {
			field = "email";
		}
		return WString.tr("Wt.Auth." + field);
	}

	protected void setValid(String field) {
		this.setValidation(field, new WValidator.Result(WValidator.State.Valid,
				WString.tr("Wt.Auth.valid")));
	}

	private AuthService baseAuth_;
	private AbstractUserDatabase users_;
	private AbstractPasswordService passwordAuth_;
	private List<OAuthService> oAuth_;
	/**
	 * {@link Login} name field.
	 */
	public static final String LoginNameField = "user-name";
}
