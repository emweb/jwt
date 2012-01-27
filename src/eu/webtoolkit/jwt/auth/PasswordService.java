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
 * Password authentication service.
 * <p>
 * 
 * This class implements password authentication.
 * <p>
 * Like all <b>service classes</b>, this class holds only configuration state.
 * Thus, once configured, it can be safely shared between multiple sessions
 * since its state (the configuration) is read-only.
 * <p>
 * Passwords are (usually) saved in the database using salted hash functions.
 * The process of computing new hashes, and verifying them is delegated to an
 * {@link AbstractVerifier}.
 * <p>
 * The authentication class may be configured to enable password attempt
 * throttling. This provides protection against brute force guessing of
 * passwords. When throttling is enabled, new password attempts are refused
 * until the throttling period is finished.
 * <p>
 * Password strength validation of a new user-chosen password may be implemented
 * by setting an AbstractStrengthValidator.
 */
public class PasswordService implements AbstractPasswordService {
	private static Logger logger = LoggerFactory
			.getLogger(PasswordService.class);

	/**
	 * Abstract password hash computation and verification class.
	 * <p>
	 * 
	 * This class defines the interface for verifying a passwords against
	 * password hashes, or computing a new password hash for a password.
	 * <p>
	 * 
	 * @see PasswordService#setVerifier(PasswordService.AbstractVerifier
	 *      verifier)
	 */
	public static interface AbstractVerifier {
		/**
		 * Returns whether a password hash needs to be updated (recomputed).
		 * <p>
		 * A <code>hash</code> may need to be updated if it has been computed
		 * with a cryptographic method that is being disfavoured.
		 */
		public boolean needsUpdate(PasswordHash hash);

		/**
		 * Computes the password hash for a clear text password.
		 * <p>
		 * This must return a hash that can later be used to verify the
		 * user&apos;s password, but which avoids compromising the user&apos;s
		 * password in case of loss.
		 */
		public PasswordHash hashPassword(CharSequence password);

		/**
		 * Verifies a password against a hash.
		 * <p>
		 * This returns whether the given password matches with the user&apos;s
		 * credentials stored in the hash.
		 */
		public boolean verify(CharSequence password, PasswordHash hash);
	}

	/**
	 * Constructor.
	 * <p>
	 * Creates a new password authentication service, which depends on the
	 * passed basic authentication service.
	 */
	public PasswordService(AuthService baseAuth) {
		super();
		this.baseAuth_ = baseAuth;
		this.verifier_ = null;
		this.validator_ = null;
		this.attemptThrottling_ = false;
	}

	/**
	 * Returns the basic authentication service.
	 */
	public AuthService getBaseAuth() {
		return this.baseAuth_;
	}

	/**
	 * Sets a password verifier which computes authorization checks.
	 * <p>
	 * The password verifier has as task to verify an entered password against a
	 * password hash stored in the database, and also to create or update a
	 * user&apos;s password hash.
	 * <p>
	 * The default password verifier is <code>null</code>.
	 * <p>
	 * The service takes ownership of the verifier.
	 * <p>
	 * 
	 * @see PasswordService#verifyPassword(User user, String password)
	 * @see PasswordService#updatePassword(User user, String password)
	 */
	public void setVerifier(PasswordService.AbstractVerifier verifier) {
		;
		this.verifier_ = verifier;
	}

	/**
	 * Returns the password verifier.
	 * <p>
	 * 
	 * @see PasswordService#setVerifier(PasswordService.AbstractVerifier
	 *      verifier)
	 */
	public PasswordService.AbstractVerifier getVerifier() {
		return this.verifier_;
	}

	/**
	 * Sets a validator which computes password strenght.
	 * <p>
	 * The default password strenght validator is <code>null</code>.
	 * <p>
	 * The service takes ownership of the validator.
	 */
	public void setStrengthValidator(
			AbstractPasswordService.AbstractStrengthValidator validator) {
		;
		this.validator_ = validator;
	}

	/**
	 * Returns the password strength validator.
	 * <p>
	 * 
	 * @see PasswordService#setStrengthValidator(AbstractPasswordService.AbstractStrengthValidator
	 *      validator)
	 */
	public AbstractPasswordService.AbstractStrengthValidator getStrengthValidator() {
		return this.validator_;
	}

	/**
	 * Configures password attempt throttling.
	 * <p>
	 * When password throttling is enabled, new password verification attempts
	 * will be refused when the user has had too many unsuccessful
	 * authentication attempts in a row.
	 * <p>
	 * The exact back-off schema can be customized by specializing
	 * {@link PasswordService#getPasswordThrottle(int failedAttempts)
	 * getPasswordThrottle()}.
	 */
	public void setAttemptThrottlingEnabled(boolean enabled) {
		this.attemptThrottling_ = enabled;
	}

	/**
	 * Returns whether password attempt throttling is enabled.
	 * <p>
	 * 
	 * @see PasswordService#setAttemptThrottlingEnabled(boolean enabled)
	 */
	public boolean isAttemptThrottlingEnabled() {
		return this.attemptThrottling_;
	}

	/**
	 * Returns the delay for this user for a next authentication attempt.
	 * <p>
	 * If password attempt throttling is enabled, then this returns the number
	 * of seconds this user must wait for a new authentication attempt,
	 * presumably because of a number of failed attempts.
	 * <p>
	 * 
	 * @see AbstractPasswordService#isAttemptThrottlingEnabled() <p>
	 * @see AbstractPasswordService#isAttemptThrottlingEnabled()
	 * @see PasswordService#setAttemptThrottlingEnabled(boolean enabled)
	 * @see PasswordService#getPasswordThrottle(int failedAttempts)
	 */
	public int delayForNextAttempt(User user) {
		if (this.attemptThrottling_) {
			int throttlingNeeded = this.getPasswordThrottle(user
					.getFailedLoginAttempts());
			if (throttlingNeeded != 0) {
				WDate t = user.getLastLoginAttempt();
				int diff = t.getSecondsTo(new WDate(new Date()));
				if (diff < throttlingNeeded) {
					return throttlingNeeded - diff;
				} else {
					return 0;
				}
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	/**
	 * Verifies a password for a given user.
	 * <p>
	 * The supplied password is verified against the user&apos;s credentials
	 * stored in the database. If password account throttling is enabled, it may
	 * also refuse an authentication attempt.
	 * <p>
	 * 
	 * <p>
	 * 
	 * @see PasswordService#setVerifier(PasswordService.AbstractVerifier
	 *      verifier)
	 * @see PasswordService#setAttemptThrottlingEnabled(boolean enabled)
	 */
	public PasswordResult verifyPassword(User user, String password) {
		AbstractUserDatabase.Transaction t = user.getDatabase()
				.startTransaction();
		if (this.delayForNextAttempt(user) > 0) {
			return PasswordResult.LoginThrottling;
		}
		boolean valid = this.verifier_.verify(password, user.getPassword());
		if (this.attemptThrottling_) {
			user.setAuthenticated(valid);
		}
		if (valid) {
			if (this.verifier_.needsUpdate(user.getPassword())) {
				user.setPassword(this.verifier_.hashPassword(password));
			}
			if (t != null) {
				t.commit();
			}
			return PasswordResult.PasswordValid;
		} else {
			if (t != null) {
				t.commit();
			}
			return PasswordResult.PasswordInvalid;
		}
	}

	/**
	 * Sets a new password for the given user.
	 * <p>
	 * This stores a new password for the user in the database.
	 */
	public void updatePassword(User user, String password) {
		PasswordHash pwd = this.verifier_.hashPassword(password);
		user.setPassword(pwd);
	}

	/**
	 * Returns how much throttle should be given considering a number of failed
	 * authentication attempts.
	 * <p>
	 * The returned value is in seconds.
	 * <p>
	 * The default implementation returns the following:
	 * <ul>
	 * <li>failedAttempts == 0: 0</li>
	 * <li>failedAttempts == 1: 1</li>
	 * <li>failedAttempts == 2: 5</li>
	 * <li>failedAttempts == 3: 10</li>
	 * <li>failedAttempts &gt; 3: 25</li>
	 * </ul>
	 */
	protected int getPasswordThrottle(int failedAttempts) {
		switch (failedAttempts) {
		case 0:
			return 0;
		case 1:
			return 1;
		case 2:
			return 5;
		case 3:
			return 10;
		default:
			return 25;
		}
	}

	// private PasswordService(PasswordService anon1) ;
	private AuthService baseAuth_;
	private PasswordService.AbstractVerifier verifier_;
	private AbstractPasswordService.AbstractStrengthValidator validator_;
	private boolean attemptThrottling_;
}
