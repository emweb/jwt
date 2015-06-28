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
 * The result of processing an authentication token.
 * <p>
 * 
 * An authentication token is usually taken from a browser cookie, and used to
 * identify (and possibly authenticate) a user across sessions.
 * <p>
 * 
 * @see AuthService#processAuthToken(String token, AbstractUserDatabase users)
 * @see AuthService#createAuthToken(User user)
 */
public class AuthTokenResult {
	private static Logger logger = LoggerFactory
			.getLogger(AuthTokenResult.class);

	/**
	 * Enumeration that describes the result.
	 */
	public enum Result {
		/**
		 * The presented auth token could be used to identify a user.
		 */
		Invalid,
		/**
		 * The presented auth token was invalid.
		 */
		Valid;

		/**
		 * Returns the numerical representation of this enum.
		 */
		public int getValue() {
			return ordinal();
		}
	}

	/**
	 * Constructor.
	 * <p>
	 * Creates an authentication token result.
	 */
	public AuthTokenResult(AuthTokenResult.Result result, final User user,
			final String newToken, int newTokenValidity) {
		this.result_ = result;
		this.user_ = user;
		this.newToken_ = newToken;
		this.newTokenValidity_ = newTokenValidity;
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls
	 * {@link #AuthTokenResult(AuthTokenResult.Result result, User user, String newToken, int newTokenValidity)
	 * this(result, new User(), "", - 1)}
	 */
	public AuthTokenResult(AuthTokenResult.Result result) {
		this(result, new User(), "", -1);
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls
	 * {@link #AuthTokenResult(AuthTokenResult.Result result, User user, String newToken, int newTokenValidity)
	 * this(result, user, "", - 1)}
	 */
	public AuthTokenResult(AuthTokenResult.Result result, final User user) {
		this(result, user, "", -1);
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls
	 * {@link #AuthTokenResult(AuthTokenResult.Result result, User user, String newToken, int newTokenValidity)
	 * this(result, user, newToken, - 1)}
	 */
	public AuthTokenResult(AuthTokenResult.Result result, final User user,
			final String newToken) {
		this(result, user, newToken, -1);
	}

	/**
	 * Returns the result.
	 */
	public AuthTokenResult.Result getResult() {
		return this.result_;
	}

	/**
	 * Returns the identified user.
	 * <p>
	 * The user is valid only if the the {@link AuthTokenResult#getResult()
	 * getResult()} == Valid.
	 */
	public User getUser() {
		if (this.user_.isValid()) {
			return this.user_;
		} else {
			throw new WException("AuthTokenResult::user() invalid");
		}
	}

	/**
	 * Returns a new token for this user.
	 * <p>
	 * An authentication token can be used only once, and needs to be replaced
	 * by a new token.
	 * <p>
	 * The returned token is valid only if the
	 * {@link AuthTokenResult#getResult() getResult()} == Valid.
	 */
	public String getNewToken() {
		if (this.user_.isValid()) {
			return this.newToken_;
		} else {
			throw new WException("AuthTokenResult::newToken() invalid");
		}
	}

	/**
	 * Returns the validity of the new token.
	 * <p>
	 * This returns the token validity in seconds.
	 * <p>
	 * 
	 * @see AuthTokenResult#getNewToken()
	 */
	public int getNewTokenValidity() {
		if (this.user_.isValid()) {
			return this.newTokenValidity_;
		} else {
			throw new WException("AuthTokenResult::newTokenValidity() invalid");
		}
	}

	private AuthTokenResult.Result result_;
	private User user_;
	private String newToken_;
	private int newTokenValidity_;
}
