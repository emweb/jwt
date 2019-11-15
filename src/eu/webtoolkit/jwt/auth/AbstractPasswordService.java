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
 * Abstract password authentication service.
 * <p>
 * 
 * This abstract class defines the interface for password authentication.
 * <p>
 * It provides methods to verify a password, to update a password, and to
 * throttle password verification attempts.
 * <p>
 * 
 * @see PasswordService
 */
public interface AbstractPasswordService {
	/**
	 * Result returned when validating password strength.
	 * <p>
	 * 
	 * This class contains information on the validity and the strength of the
	 * password together with possible messages. When the password is considered
	 * not strong enough, a message should be provided which helps the user pick
	 * a stronger password.
	 * <p>
	 * 
	 * @see AbstractPasswordService.AbstractStrengthValidator#evaluateStrength(String
	 *      password, String loginName, String email)
	 */
	public static class StrengthValidatorResult {
		private static Logger logger = LoggerFactory
				.getLogger(StrengthValidatorResult.class);

		/**
		 * Constructor.
		 */
		public StrengthValidatorResult(boolean valid,
				final CharSequence message, int strength) {
			this.valid_ = valid;
			this.message_ = WString.toWString(message);
			this.strength_ = strength;
		}

		/**
		 * Returns whether the password is considered strong enough.
		 */
		public boolean isValid() {
			return this.valid_;
		}

		/**
		 * Returns a message describing the password strength.
		 */
		public WString getMessage() {
			return this.message_;
		}

		/**
		 * Returns the password strength in a scale of 0 to 5.
		 */
		public int getStrength() {
			return this.strength_;
		}

		private boolean valid_;
		private WString message_;
		private int strength_;
	}

	/**
	 * Validator for password strength.
	 * <p>
	 * 
	 * This class defines a specialized validator interface for evaluating
	 * password strength. The implementation allows to evaluate strength in
	 * addition to the normal validator functionality of validating a password.
	 * <p>
	 * The
	 * {@link AbstractPasswordService.AbstractStrengthValidator#evaluateStrength(String password, String loginName, String email)
	 * evaluateStrength()} computes the strength and returns an instance of
	 * StrenghtValidatorResult which contains information on the validity and
	 * the strength of the password together with possible messages.
	 * <p>
	 * 
	 * @see AbstractPasswordService#getStrengthValidator()
	 */
	public static abstract class AbstractStrengthValidator extends WValidator {
		private static Logger logger = LoggerFactory
				.getLogger(AbstractStrengthValidator.class);

		/**
		 * Evaluates the strength of a password.
		 * <p>
		 * 
		 * The result is an instance of {@link StrengthValidatorResult} which
		 * contains information on the validity and the strength of the password
		 * together with possible messages.
		 * <p>
		 * The validator may take into account the user&apos;s login name and
		 * email address, to exclude passwords that are too similar to these.
		 */
		public abstract AbstractPasswordService.StrengthValidatorResult evaluateStrength(
				final String password, final String loginName,
				final String email);

		/**
		 * Validates a password.
		 * <p>
		 * 
		 * This uses
		 * {@link AbstractPasswordService.AbstractStrengthValidator#evaluateStrength(String password, String loginName, String email)
		 * evaluateStrength()}, isValid() and message() to return the result of
		 * password validation.
		 */
		public WValidator.Result validate(final String password,
				final String loginName, final String email) {
			AbstractPasswordService.StrengthValidatorResult result = this
					.evaluateStrength(password, loginName, email);
			return new WValidator.Result(
					result.isValid() ? WValidator.State.Valid
							: WValidator.State.Invalid, result.getMessage());
		}

		/**
		 * Validates a password.
		 * <p>
		 * 
		 * Calls validate(password, {@link WString#Empty}, &quot;&quot;);
		 */
		public WValidator.Result validate(final String password) {
			return this.validate(password, "", "");
		}
	}

	/**
	 * Returns the basic authentication service.
	 */
	public AuthService getBaseAuth();

	/**
	 * Returns whether password attempt throttling is enabled.
	 */
	public boolean isAttemptThrottlingEnabled();

	/**
	 * Returns a validator which checks that a password is strong enough.
	 */
	public AbstractPasswordService.AbstractStrengthValidator getStrengthValidator();

	/**
	 * Returns the delay for this user for a next authentication attempt.
	 * <p>
	 * 
	 * If password attempt throttling is enabled, then this returns the number
	 * of seconds this user must wait for a new authentication attempt,
	 * presumably because of a number of failed attempts.
	 * <p>
	 * 
	 * @see AbstractPasswordService#isAttemptThrottlingEnabled()
	 */
	public int delayForNextAttempt(final User user);

	/**
	 * Verifies a password for a given user.
	 * <p>
	 * 
	 * The supplied password is verified against the user&apos;s credentials
	 * stored in the database. If password account throttling is enabled, it may
	 * also refuse an authentication attempt.
	 * <p>
	 */
	public PasswordResult verifyPassword(final User user, final String password);

	/**
	 * Sets a new password for the given user.
	 * <p>
	 * 
	 * This stores a new password for the user in the database.
	 */
	public void updatePassword(final User user, final String password);
}
