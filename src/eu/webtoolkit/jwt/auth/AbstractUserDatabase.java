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
 * Abstract interface for an authentication user database.
 * <p>
 * 
 * This class defines the interface for managing user data related to
 * authentication. You need to implement this interface to allow the
 * authentication service classes ({@link AuthService}, {@link PasswordService},
 * and {@link OAuthService}) to locate and update user credentials. Except for
 * functions which do work on a single user, it is more convenient to use the
 * {@link User} API. Obviously, you may have more data associated with a user,
 * including roles for access control, other personal information, address
 * information. This information cannot be accessed through the {@link User}
 * class, but you should make it available through your own {@link User} class,
 * which is then als the basis of this user database implementation.
 * <p>
 * The only assumption made by the authentication system is that an id uniquely
 * defines the user. This is usually an internal identifier, for example an
 * auto-incrementing primary key.
 * <p>
 * With a user, one or more other identities may be associated. These could be a
 * login name (for password-based authentication), or id&apos;s used by third
 * party providers (such as OAuth or LDAP).
 * <p>
 * The database implements a simple data store and does not contain any logic.
 * The database can store data for different aspects of authentication, but most
 * data fields are only relevant for optional functionality, and thus
 * themeselves optional. The default implementation of these methods will log
 * errors.
 * <p>
 * The authentication views and model classes assume a private instance of the
 * database for each different session, and will try to wrap database access
 * within a transaction. {@link Transaction} support can thus be optionally
 * provided by a database implementation.
 * <p>
 * 
 * @see User
 */
public abstract class AbstractUserDatabase {
	private static Logger logger = LoggerFactory
			.getLogger(AbstractUserDatabase.class);

	/**
	 * An abstract transaction.
	 * <p>
	 * 
	 * An abstract transaction interface.
	 * <p>
	 * 
	 * @see AbstractUserDatabase#startTransaction()
	 */
	public static interface Transaction {
		/**
		 * Commits the transaction.
		 * <p>
		 * 
		 * @see AbstractUserDatabase.Transaction#rollback()
		 */
		public void commit();

		/**
		 * Rolls back the transaction.
		 * <p>
		 * 
		 * @see AbstractUserDatabase.Transaction#commit()
		 */
		public void rollback();
	}

	/**
	 * Creates a new database transaction.
	 * <p>
	 * If the underlying database does not support transactions, you can return
	 * <code>null</code>.
	 * <p>
	 * Ownership of the transaction is transferred, and the transaction must be
	 * deleted after it has been committed or rolled back.
	 * <p>
	 * The default implementation returns <code>null</code> (no transaction
	 * support).
	 */
	public AbstractUserDatabase.Transaction startTransaction() {
		return null;
	}

	/**
	 * Finds a user with a given id.
	 * <p>
	 * The id uniquely identifies a user.
	 * <p>
	 * This should find the user with the given <code>id</code>, or return an
	 * invalid user if no user with that id exists.
	 */
	public abstract User findWithId(String id);

	/**
	 * Finds a user with a given identity.
	 * <p>
	 * The <code>identity</code> uniquely identifies the user by the
	 * <code>provider</code>.
	 * <p>
	 * This should find the user with the given <code>identity</code>, or return
	 * an invalid user if no user with that identity exists.
	 */
	public abstract User findWithIdentity(String provider, String identity);

	/**
	 * Sets an identifier for the user.
	 * <p>
	 * This associates an identifier with the user.
	 * <p>
	 * You are free to support only one identity per user, e.g. if you only use
	 * password-based authentication. But you may also want to support more than
	 * one if you allow the user to login using multiple methods (e.g.
	 * name/password, OAuth from one or more providers, LDAP, ...).
	 */
	public abstract void addIdentity(User user, String provider, String id);

	/**
	 * Returns a user identity.
	 * <p>
	 * Returns a user identity for the given provider, or an empty string if the
	 * user has no identitfy set for this provider.
	 * <p>
	 * 
	 * @see AbstractUserDatabase#addIdentity(User user, String provider, String
	 *      id)
	 */
	public abstract String getIdentity(User user, String provider);

	public abstract void removeIdentity(User user, String provider);

	/**
	 * Registers a new user.
	 * <p>
	 * This adds a new user.
	 * <p>
	 * This method is only used by view classes involved with registration (
	 * {@link RegistrationWidget}).
	 */
	public User registerNew() {
		logger.error(new StringWriter().append(
				new Require("registerNew()", REGISTRATION).toString())
				.toString());
		return new User();
	}

	/**
	 * Delete a user.
	 * <p>
	 * This deletes a user from the database.
	 */
	public void deleteUser(User user) {
		logger.error(new StringWriter().append(
				new Require("deleteUser()", REGISTRATION).toString())
				.toString());
	}

	/**
	 * Returns the status for a user.
	 * <p>
	 * If there is support for suspending accounts, then this method may be
	 * implemented to return whether a user account is disabled.
	 * <p>
	 * The default implementation always returns {@link User.Status#Normal}.
	 * <p>
	 */
	public User.Status getStatus(User user) {
		return User.Status.Normal;
	}

	/**
	 * Sets a new user password.
	 * <p>
	 * This updates the password for a user.
	 * <p>
	 * This is used only by {@link PasswordService}.
	 */
	public void setPassword(User user, PasswordHash password) {
		logger.error(new StringWriter().append(
				new Require("setPassword()", PASSWORDS).toString()).toString());
	}

	/**
	 * Returns a user password.
	 * <p>
	 * This returns the stored password for a user, or a default constructed
	 * password hash if the user does not yet have password credentials.
	 * <p>
	 * This is used only by {@link PasswordService}.
	 */
	public PasswordHash getPassword(User user) {
		logger.error(new StringWriter().append(
				new Require("password()", PASSWORDS).toString()).toString());
		return new PasswordHash();
	}

	/**
	 * Sets a user&apos;s email address.
	 * <p>
	 * This is used only when email verification is enabled, or as a result of a
	 * 3rd party {@link Identity} Provider based registration process, if the
	 * provider also provides email address information with the identiy.
	 * <p>
	 * Returns whether the user&apos;s email address could be set. This may fail
	 * when there is already a user registered that email address.
	 * <p>
	 * 
	 * @see AbstractUserDatabase#findWithEmail(String address)
	 */
	public boolean setEmail(User user, String address) {
		logger.error(new StringWriter().append(
				new Require("setEmail()", EMAIL_VERIFICATION).toString())
				.toString());
		return false;
	}

	/**
	 * Returns a user&apos;s email address.
	 * <p>
	 * This may be an unverified or verified email address, depending on whether
	 * email address verification is enabled in the model classes.
	 * <p>
	 * This is an optional method, and currently not used by any of the included
	 * models or views.
	 */
	public String getEmail(User user) {
		logger.error(new StringWriter().append(
				new Require("email()", EMAIL_VERIFICATION).toString())
				.toString());
		return "";
	}

	/**
	 * Sets a user&apos;s unverified email address.
	 * <p>
	 * This is only used when email verification is enabled. It holds the
	 * currently unverified email address, while a mail is being sent for the
	 * user to confirm this email address.
	 */
	public void setUnverifiedEmail(User user, String address) {
		logger.error(new StringWriter().append(
				new Require("setUnverifiedEmail()", EMAIL_VERIFICATION)
						.toString()).toString());
	}

	/**
	 * Returns a user&apos;s unverified email address.
	 * <p>
	 * This is an optional method, and currently not used by any of the included
	 * models or views.
	 */
	public String getUnverifiedEmail(User user) {
		logger.error(new StringWriter()
				.append(
						new Require("unverifiedEmail()", EMAIL_VERIFICATION)
								.toString()).toString());
		return "";
	}

	/**
	 * Finds a user with a given email address.
	 * <p>
	 * This is used to verify that a email addresses are unique, and to
	 * implement lost password functionality.
	 */
	public User findWithEmail(String address) {
		logger.error(new StringWriter().append(
				new Require("findWithEmail()", EMAIL_VERIFICATION).toString())
				.toString());
		return new User();
	}

	/**
	 * Sets a new email token for a user.
	 * <p>
	 * This is only used when email verification is enabled or for lost password
	 * functionality.
	 */
	public void setEmailToken(User user, Token token, User.EmailTokenRole role) {
		logger.error(new StringWriter().append(
				new Require("setEmailToken()", EMAIL_VERIFICATION).toString())
				.toString());
	}

	/**
	 * Returns an email token.
	 * <p>
	 * This is only used when email verification is enabled and for lost
	 * password functionality. It should return the email token previously set
	 * with
	 * {@link AbstractUserDatabase#setEmailToken(User user, Token token, User.EmailTokenRole role)
	 * setEmailToken()}
	 */
	public Token getEmailToken(User user) {
		logger.error(new StringWriter().append(
				new Require("emailToken()", EMAIL_VERIFICATION).toString())
				.toString());
		return new Token();
	}

	/**
	 * Returns the role of the current email token.
	 * <p>
	 * This is only used when email verification is enabled or for lost password
	 * functionality. It should return the role previously set with
	 * setEailToken().
	 */
	public User.EmailTokenRole getEmailTokenRole(User user) {
		logger.error(new StringWriter().append(
				new Require("emailTokenRole()", EMAIL_VERIFICATION).toString())
				.toString());
		return User.EmailTokenRole.VerifyEmail;
	}

	/**
	 * Finds a user with a given email token.
	 * <p>
	 * This is only used when email verification is enabled or for lost password
	 * functionality.
	 */
	public User findWithEmailToken(String hash) {
		logger.error(new StringWriter().append(
				new Require("findWithEmailToken()", EMAIL_VERIFICATION)
						.toString()).toString());
		return new User();
	}

	/**
	 * Adds an authentication token to a user.
	 * <p>
	 * Unless you want a user to only have remember-me support from a single
	 * computer at a time, you should support multiple authentication tokens per
	 * user.
	 */
	public void addAuthToken(User user, Token token) {
		logger.error(new StringWriter().append(
				new Require("addAuthToken()", AUTH_TOKEN).toString())
				.toString());
	}

	/**
	 * Deletes an authentication token.
	 * <p>
	 * Deletes an authentication token previously added with
	 * {@link AbstractUserDatabase#addAuthToken(User user, Token token)
	 * addAuthToken()}
	 */
	public void removeAuthToken(User user, String hash) {
		logger.error(new StringWriter().append(
				new Require("removeAuthToken()", AUTH_TOKEN).toString())
				.toString());
	}

	/**
	 * Finds a user with an authentication token.
	 * <p>
	 * Returns a user with an authentication token.
	 * <p>
	 * This should find the user associated with a particular token hash, or
	 * return an invalid user if no user with that token hash exists.
	 */
	public User findWithAuthToken(String hash) {
		logger.error(new StringWriter().append(
				new Require("findWithAuthToken()", AUTH_TOKEN).toString())
				.toString());
		return new User();
	}

	/**
	 * Sets the number of consecutive authentication failures.
	 * <p>
	 * This sets the number of consecutive authentication failures since the
	 * last valid login.
	 * <p>
	 * This is used by the throttling logic to determine how much time a user
	 * needs to wait before he can do a new login attempt.
	 */
	public void setFailedLoginAttempts(User user, int count) {
		logger.error(new StringWriter().append(
				new Require("setFailedLoginAttempts()", THROTTLING).toString())
				.toString());
	}

	/**
	 * Returns the number of consecutive authentication failures.
	 * <p>
	 * <i>
	 * {@link AbstractUserDatabase#setFailedLoginAttempts(User user, int count)
	 * setFailedLoginAttempts()}</i>
	 */
	public int getFailedLoginAttempts(User user) {
		logger.error(new StringWriter().append(
				new Require("failedLoginAttempts()", THROTTLING).toString())
				.toString());
		return 0;
	}

	/**
	 * Sets the time of the last login attempt.
	 * <p>
	 * This sets the time at which the user attempted to login.
	 */
	public void setLastLoginAttempt(User user, WDate t) {
		logger.error(new StringWriter().append(
				new Require("setLastLoginAttempt()", THROTTLING).toString())
				.toString());
	}

	/**
	 * Returns the time of the last login.
	 * <p>
	 * 
	 * @see AbstractUserDatabase#setLastLoginAttempt(User user, WDate t)
	 */
	public WDate getLastLoginAttempt(User user) {
		logger.error(new StringWriter().append(
				new Require("lastLoginAttempt()", THROTTLING).toString())
				.toString());
		return new WDate(1970, 1, 1);
	}

	public User addUser(User user) {
		logger.error(new StringWriter().append(
				new Require("registerNew()", REGISTRATION).toString())
				.toString());
		return new User();
	}

	protected AbstractUserDatabase() {
	}

	// private AbstractUserDatabase(AbstractUserDatabase anon1) ;
	static String EMAIL_VERIFICATION = "email verification";
	static String AUTH_TOKEN = "authentication tokens";
	static String PASSWORDS = "password handling";
	static String THROTTLING = "password attempt throttling";
	static String REGISTRATION = "user registration";
}
