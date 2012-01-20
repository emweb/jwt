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
 * The result of processing an email-sent token.
 * <p>
 * 
 * An email token can be used for two purposes:
 * <p>
 * <ul>
 * <li>the user needs to verify his email address by returning a token sent to
 * his supplied email address.</li>
 * <li>the user indicates that he lost his email and wants to prove his identity
 * by acknowledging an email to a previously verified email account.</li>
 * </ul>
 * <p>
 * 
 * @see AuthService#processEmailToken(String token, AbstractUserDatabase users)
 * @see AuthService#verifyEmailAddress(User user, String address)
 * @see AuthService#lostPassword(String emailAddress, AbstractUserDatabase
 *      users)
 */
public class EmailTokenResult {
	private static Logger logger = LoggerFactory
			.getLogger(EmailTokenResult.class);

	/**
	 * Enumeration that describes the result.
	 */
	public enum Result {
		/**
		 * The token was invalid.
		 */
		Invalid,
		/**
		 * The token has expired.
		 */
		Expired,
		/**
		 * A token was presented which requires the user to enter a new
		 * password.
		 * <p>
		 * The presented token was a token sent by the
		 * {@link AuthService#lostPassword(String emailAddress, AbstractUserDatabase users)
		 * AuthService#lostPassword()} function. When this is returned as result
		 * of
		 * {@link AuthService#processEmailToken(String token, AbstractUserDatabase users)
		 * AuthService#processEmailToken()}, you should present the user with a
		 * dialog where he can enter a new password.
		 */
		UpdatePassword,
		/**
		 * A The token was presented which verifies the email address.
		 * <p>
		 * The presented token was a token sent by the
		 * {@link AuthService#verifyEmailAddress(User user, String address)
		 * AuthService#verifyEmailAddress()} function. When this is returned as
		 * result of processEmailToken(), you can indicate to the user that his
		 * email address is now confirmed.
		 */
		EmailConfirmed;

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
	 * Creates an email token result.
	 */
	public EmailTokenResult(EmailTokenResult.Result result, User user) {
		this.result_ = result;
		this.user_ = user;
	}

	/**
	 * Constructor.
	 * <p>
	 * Calls {@link #EmailTokenResult(EmailTokenResult.Result result, User user)
	 * this(result, new User())}
	 */
	public EmailTokenResult(EmailTokenResult.Result result) {
		this(result, new User());
	}

	/**
	 * Returns the result.
	 */
	public EmailTokenResult.Result getResult() {
		return this.result_;
	}

	/**
	 * Returns the user, if any.
	 * <p>
	 * The identified user is only valid when the token result is UpdatePassword
	 * or EmailConfirmed. In that case, you may login the user as strongly
	 * authenticated since he presented a random token that was sent to his own
	 * email address.
	 */
	public User getUser() {
		if (this.user_.isValid()) {
			return this.user_;
		} else {
			throw new WException("EmailTokenResult::user() invalid");
		}
	}

	private EmailTokenResult.Result result_;
	private User user_;
}
