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
 * A user.
 * <p>
 * 
 * This class represents a user. It is a value class that stores only the user
 * id and a reference to an {@link AbstractUserDatabase} to access its
 * properties.
 * <p>
 * An object can point to a valid user, or be invalid. Invalid users are
 * typically used as return value for database queries which did not match with
 * an existing user.
 * <p>
 * Not all methods are valid or applicable to your authentication system. See
 * {@link AbstractUserDatabase} for a discussion.
 * <p>
 * 
 * @see AbstractUserDatabase
 */
public class User {
	private static Logger logger = LoggerFactory.getLogger(User.class);

	/**
	 * Enumeration for a user&apos;s account status.
	 * <p>
	 * 
	 * @see User#getStatus()
	 */
	public enum Status {
		/**
		 * Successfully identified but not allowed to log in.
		 */
		Disabled,
		/**
		 * Normal status.
		 */
		Normal;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Enumeration for an email token stored for the user.
	 */
	public enum EmailTokenRole {
		/**
		 * {@link Token} is used to verify his email address.
		 */
		VerifyEmail,
		/**
		 * {@link Token} is used to allow the user to enter a new password.
		 */
		LostPassword;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Default constructor.
	 * <p>
	 * Creates an invalid user.
	 * <p>
	 * 
	 * @see User#isValid()
	 */
	public User() {
		this.id_ = "";
		this.db_ = null;
	}

	/**
	 * Constructor.
	 * <p>
	 * Creates a user with id <code>id</code>, and whose information is stored
	 * in the <code>database</code>.
	 */
	public User(String id, AbstractUserDatabase userDatabase) {
		this.id_ = id;
		this.db_ = userDatabase;
	}

	/**
	 * Returns the user database.
	 * <p>
	 * This returns the user database passed in the constructor, or 0 if the
	 * user is invalid, and was constructed using the default constructor.
	 */
	public AbstractUserDatabase getDatabase() {
		return this.db_;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
	 * <p>
	 * Two users are equal if they have the same identity and the same database.
	 */
	public boolean equals(User other) {
		return this.id_.equals(other.id_) && this.db_ == other.db_;
	}

	/**
	 * Returns whether the user is valid.
	 * <p>
	 * A invalid user is a sentinel value returned by methods that query the
	 * database but could not identify a matching user.
	 */
	public boolean isValid() {
		return this.db_ != null;
	}

	/**
	 * Returns the user id.
	 * <p>
	 * This returns the id that uniquely identifies the user, and acts as a
	 * &quot;primary key&quot; to obtain other information for the user in the
	 * database.
	 * <p>
	 * 
	 * @see AbstractUserDatabase
	 */
	public String getId() {
		return this.id_;
	}

	/**
	 * Returns the user&apos;s identity.
	 */
	public String identity(String provider) {
		this.checkValid();
		return this.db_.getIdentity(this, provider);
	}

	/**
	 * Adds (or modifies) a user&apos;s identity.
	 */
	public void addIdentity(String provider, String identity) {
		this.checkValid();
		this.db_.addIdentity(this, provider, identity);
	}

	/**
	 * Sets a password.
	 * <p>
	 * 
	 * @see AbstractUserDatabase#setPassword(User user, PasswordHash password)
	 */
	public void setPassword(PasswordHash password) {
		this.checkValid();
		this.db_.setPassword(this, password);
	}

	/**
	 * Returns the password.
	 * <p>
	 * 
	 * @see AbstractUserDatabase#getPassword(User user)
	 */
	public PasswordHash getPassword() {
		this.checkValid();
		return this.db_.getPassword(this);
	}

	/**
	 * Sets the email address.
	 * <p>
	 * 
	 * @see AbstractUserDatabase#setEmail(User user, String address)
	 */
	public void setEmail(String address) {
		this.checkValid();
		this.db_.setEmail(this, address);
	}

	/**
	 * Returns the email address.
	 * <p>
	 * 
	 * @see AbstractUserDatabase#getEmail(User user)
	 */
	public String getEmail() {
		return this.db_.getEmail(this);
	}

	/**
	 * Sets the unverified email address.
	 * <p>
	 * 
	 * @see AbstractUserDatabase#setUnverifiedEmail(User user, String address)
	 */
	public void setUnverifiedEmail(String address) {
		this.checkValid();
		this.db_.setUnverifiedEmail(this, address);
	}

	/**
	 * Returns the unverified email address.
	 * <p>
	 * 
	 * @see AbstractUserDatabase#getUnverifiedEmail(User user)
	 */
	public String getUnverifiedEmail() {
		this.checkValid();
		return this.db_.getUnverifiedEmail(this);
	}

	/**
	 * Returns the account status.
	 * <p>
	 * 
	 * @see AbstractUserDatabase#getStatus(User user)
	 */
	public User.Status getStatus() {
		this.checkValid();
		return this.db_.getStatus(this);
	}

	/**
	 * Returns the email token.
	 * <p>
	 * 
	 * @see AbstractUserDatabase#getEmailToken(User user)
	 */
	public Token getEmailToken() {
		return this.db_.getEmailToken(this);
	}

	/**
	 * Returns the email token role.
	 * <p>
	 * 
	 * @see AbstractUserDatabase#getEmailTokenRole(User user)
	 */
	public User.EmailTokenRole getEmailTokenRole() {
		return this.db_.getEmailTokenRole(this);
	}

	/**
	 * Sets an email token.
	 * <p>
	 * 
	 * @see AbstractUserDatabase#setEmailToken(User user, Token token,
	 *      User.EmailTokenRole role)
	 */
	public void setEmailToken(Token token, User.EmailTokenRole role) {
		this.checkValid();
		this.db_.setEmailToken(this, token, role);
	}

	/**
	 * Clears the email token.
	 * <p>
	 * 
	 * @see User#setEmailToken(Token token, User.EmailTokenRole role)
	 */
	public void clearEmailToken() {
		this.checkValid();
		this.db_.setEmailToken(this, new Token(),
				User.EmailTokenRole.LostPassword);
	}

	/**
	 * Adds an authentication token.
	 * <p>
	 * 
	 * @see AbstractUserDatabase#addAuthToken(User user, Token token)
	 */
	public void addAuthToken(Token token) {
		this.checkValid();
		this.db_.addAuthToken(this, token);
	}

	/**
	 * Removes an authentication token.
	 * <p>
	 * 
	 * @see AbstractUserDatabase#removeAuthToken(User user, String hash)
	 */
	public void removeAuthToken(String token) {
		this.checkValid();
		this.db_.removeAuthToken(this, token);
	}

	/**
	 * Logs the result of an authentication attempt.
	 * <p>
	 * This changes the number of failed login attempts, and stores the current
	 * date as the last login attempt time.
	 * <p>
	 * 
	 * @see User#getFailedLoginAttempts()
	 * @see User#getLastLoginAttempt()
	 */
	public void setAuthenticated(boolean success) {
		this.checkValid();
		if (success) {
			this.db_.setFailedLoginAttempts(this, 0);
		} else {
			this.db_.setFailedLoginAttempts(this, this.db_
					.getFailedLoginAttempts(this) + 1);
		}
		this.db_.setLastLoginAttempt(this, WDate.getCurrentDate());
	}

	/**
	 * Returns the number of consecutive unsuccessful login attempts.
	 * <p>
	 * 
	 * @see User#setAuthenticated(boolean success)
	 */
	public int getFailedLoginAttempts() {
		return this.db_.getFailedLoginAttempts(this);
	}

	/**
	 * Returns the last login attempt.
	 * <p>
	 * 
	 * @see User#setAuthenticated(boolean success)
	 */
	public WDate getLastLoginAttempt() {
		return this.db_.getLastLoginAttempt(this);
	}

	private String id_;
	private AbstractUserDatabase db_;

	private void checkValid() {
		if (!(this.db_ != null)) {
			throw new WException("Method called on invalid Auth::User");
		}
	}
}
