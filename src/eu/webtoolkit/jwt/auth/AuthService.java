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
 * Basic authentication service.
 * <p>
 * 
 * This class presents an basic authentication service, which offers
 * authentication functionality that is not specific to an authentication
 * mechanism (such as password authentication or OAuth authentication).
 * <p>
 * Like all <b>service classes</b>, this class holds only configuration state.
 * Thus, once configured, it can be safely shared between multiple sessions
 * since its state (the configuration) is read-only.
 * <p>
 * The class provides the following services (and relevant configuration):
 * <p>
 * <ul>
 * <li>settings for generating random tokens:
 * <ul>
 * <li>{@link }</li>
 * </ul>
 * </li>
 * <li>authentication tokens, used by e.g. remember-me functionality:
 * <ul>
 * <li>{@link }</li>
 * <li>{@link }</li>
 * </ul>
 * </li>
 * <li>email tokens, for email verification and lost password functions:
 * <ul>
 * <li>{@link }</li>
 * <li>{@link }</li>
 * <li>{@link }</li>
 * <li>{@link }</li>
 * </ul>
 * </li>
 * </ul>
 */
public class AuthService {
	private static Logger logger = LoggerFactory.getLogger(AuthService.class);

	/**
	 * Constructor.
	 */
	public AuthService() {
		this.identityPolicy_ = IdentityPolicy.LoginNameIdentity;
		this.minimumLoginNameLength_ = 4;
		this.tokenHashFunction_ = new MD5HashFunction();
		this.tokenLength_ = 32;
		this.emailVerification_ = false;
		this.emailTokenValidity_ = 3 * 24 * 60;
		this.redirectInternalPath_ = "";
		this.authTokens_ = false;
		this.authTokenValidity_ = 14 * 24 * 60;
		this.authTokenCookieName_ = "";
		this.authTokenCookieDomain_ = "";
		this.redirectInternalPath_ = "/auth/mail/";
	}

	/**
	 * Sets the token length.
	 * <p>
	 * Configures the length used for random tokens. Random tokens are generated
	 * for authentication tokens, and email tokens.
	 * <p>
	 * The default length is 32 characters.
	 * <p>
	 */
	public void setRandomTokenLength(int length) {
		this.tokenLength_ = length;
	}

	/**
	 * Returns the token length.
	 * <p>
	 * 
	 * @see AuthService#setRandomTokenLength(int length)
	 */
	public int getRandomTokenLength() {
		return this.tokenLength_;
	}

	/**
	 * Configures the identity policy.
	 * <p>
	 * The identity policy has an impact on the login and registration
	 * procedure.
	 */
	public void setIdentityPolicy(IdentityPolicy identityPolicy) {
		this.identityPolicy_ = identityPolicy;
	}

	/**
	 * Returns the identity policy.
	 * <p>
	 * 
	 * @see AuthService#setIdentityPolicy(IdentityPolicy identityPolicy)
	 */
	public IdentityPolicy getIdentityPolicy() {
		return this.identityPolicy_;
	}

	/**
	 * Tries to match the identity to an existing user.
	 * <p>
	 * When authenticating using a 3rd party {@link Identity} Provider, the
	 * identity is matched against the existing users, based on the id (with
	 * {@link AbstractUserDatabase#findWithIdentity(String provider, String identity)
	 * AbstractUserDatabase#findWithIdentity()}), or if not matched, based on
	 * whether there is a user with the same verified email address as the one
	 * indicated by the identity.
	 */
	public User identifyUser(final Identity identity,
			final AbstractUserDatabase users) {
		AbstractUserDatabase.Transaction t = users.startTransaction();
		User user = users.findWithIdentity(identity.getProvider(), new WString(
				identity.getId()).toString());
		if (user.isValid()) {
			if (t != null) {
				t.commit();
			}
			return user;
		}
		if (identity.getEmail().length() != 0) {
			if (this.emailVerification_ && identity.isEmailVerified()) {
				user = users.findWithEmail(identity.getEmail());
				if (user.isValid()) {
					user.addIdentity(identity.getProvider(), identity.getId());
					if (t != null) {
						t.commit();
					}
					return user;
				}
			}
		}
		if (t != null) {
			t.commit();
		}
		return new User();
	}

	/**
	 * Configures authentication token support.
	 * <p>
	 * This method allows you to configure whether authentication tokens are in
	 * use. Authentication tokens are used for the user to bypass a more
	 * elaborate authentication method, and are a secret shared with the
	 * user&apos;s user agent, usually in a cookie. They are typically presented
	 * in the user interfaces as a &quot;remember me&quot; option.
	 * <p>
	 * Whenever a valid authentication token is presented in processToken(), it
	 * is invalidated a new token is generated and stored for the user.
	 * <p>
	 * The tokens are generated and subsequently hashed using the token hash
	 * function. Only the hash values are stored in the user database so that a
	 * compromised user database does not compromise these tokens.
	 * <p>
	 * Authentication tokens are disabled by default.
	 * <p>
	 */
	public void setAuthTokensEnabled(boolean enabled, final String cookieName,
			final String cookieDomain) {
		this.authTokens_ = enabled;
		this.authTokenCookieName_ = cookieName;
		this.authTokenCookieDomain_ = cookieDomain;
	}

	/**
	 * Configures authentication token support.
	 * <p>
	 * Calls
	 * {@link #setAuthTokensEnabled(boolean enabled, String cookieName, String cookieDomain)
	 * setAuthTokensEnabled(enabled, "wtauth", "")}
	 */
	public final void setAuthTokensEnabled(boolean enabled) {
		setAuthTokensEnabled(enabled, "wtauth", "");
	}

	/**
	 * Configures authentication token support.
	 * <p>
	 * Calls
	 * {@link #setAuthTokensEnabled(boolean enabled, String cookieName, String cookieDomain)
	 * setAuthTokensEnabled(enabled, cookieName, "")}
	 */
	public final void setAuthTokensEnabled(boolean enabled,
			final String cookieName) {
		setAuthTokensEnabled(enabled, cookieName, "");
	}

	/**
	 * Returns whether authentication tokens are enabled.
	 * <p>
	 * 
	 * @see AuthService#setAuthTokensEnabled(boolean enabled, String cookieName,
	 *      String cookieDomain)
	 */
	public boolean isAuthTokensEnabled() {
		return this.authTokens_;
	}

	/**
	 * Returns the authentication token cookie name.
	 * <p>
	 * This is the default cookie name used for storing the authentication token
	 * in the user&apos;s browser.
	 * <p>
	 * 
	 * @see AuthService#setAuthTokensEnabled(boolean enabled, String cookieName,
	 *      String cookieDomain)
	 */
	public String getAuthTokenCookieName() {
		return this.authTokenCookieName_;
	}

	/**
	 * Returns the authentication token cookie domain.
	 * <p>
	 * This is the domain used for the authentication cookie. By default this is
	 * empty, which means that a cookie will be set for this application.
	 * <p>
	 * You may want to set a more general domain if you are sharing the
	 * authentication with multiple applications.
	 * <p>
	 * 
	 * @see AuthService#setAuthTokensEnabled(boolean enabled, String cookieName,
	 *      String cookieDomain)
	 */
	public String getAuthTokenCookieDomain() {
		return this.authTokenCookieDomain_;
	}

	/**
	 * Sets the token hash function.
	 * <p>
	 * Sets the hash function used to safely store authentication tokens in the
	 * database. Ownership of the hash function is transferred.
	 * <p>
	 * The default token hash function is an {@link MD5HashFunction}.
	 */
	public void setTokenHashFunction(HashFunction function) {
		;
		this.tokenHashFunction_ = function;
	}

	/**
	 * Returns the token hash function.
	 * <p>
	 * 
	 * @see AuthService#setTokenHashFunction(HashFunction function)
	 */
	public HashFunction getTokenHashFunction() {
		return this.tokenHashFunction_;
	}

	/**
	 * Creates and stores an authentication token for the user.
	 * <p>
	 * This creates and stores a new authentication token for the given user.
	 * <p>
	 * The returned value is the token that may be used to re-identify the user
	 * in {@link }.
	 */
	public String createAuthToken(final User user) {
		if (!user.isValid()) {
			throw new WException("Auth: createAuthToken(): user invalid");
		}
		AbstractUserDatabase.Transaction t = user.getDatabase()
				.startTransaction();
		String random = MathUtils.randomId(this.tokenLength_);
		String hash = this.getTokenHashFunction().compute(random, "");
		Token token = new Token(hash, WDate.getCurrentDate().addSeconds(
				this.authTokenValidity_ * 60));
		user.addAuthToken(token);
		if (t != null) {
			t.commit();
		}
		return random;
	}

	/**
	 * Processes an authentication token.
	 * <p>
	 * This verifies an authentication token, and considers whether it matches
	 * with a token hash value stored in database. If it matches, the token is
	 * removed and a new token is created for the identified user.
	 */
	public AuthTokenResult processAuthToken(final String token,
			final AbstractUserDatabase users) {
		AbstractUserDatabase.Transaction t = users.startTransaction();
		String hash = this.getTokenHashFunction().compute(token, "");
		User user = users.findWithAuthToken(hash);
		if (user.isValid()) {
			String newToken = MathUtils.randomId(this.tokenLength_);
			String newHash = this.getTokenHashFunction().compute(newToken, "");
			int validity = user.updateAuthToken(hash, newHash);
			if (validity < 0) {
				user.removeAuthToken(hash);
				newToken = this.createAuthToken(user);
				validity = this.authTokenValidity_ * 60;
			}
			if (t != null) {
				t.commit();
			}
			return new AuthTokenResult(AuthTokenResult.Result.Valid, user,
					newToken, validity);
		} else {
			if (t != null) {
				t.commit();
			}
			return new AuthTokenResult(AuthTokenResult.Result.Invalid);
		}
	}

	/**
	 * Configures the duration for an authenticaton to remain valid.
	 * <p>
	 * The default duration is two weeks (14 * 24 * 60 minutes).
	 */
	public void setAuthTokenValidity(int minutes) {
		this.authTokenValidity_ = minutes;
	}

	/**
	 * Returns the authentication token validity.
	 * <p>
	 * 
	 * @see AuthService#setAuthTokenValidity(int minutes)
	 */
	public int getAuthTokenValidity() {
		return this.authTokenValidity_;
	}

	/**
	 * Configures email verification.
	 * <p>
	 * Email verification is useful for a user to recover a lost password, or to
	 * be able to confidently confirm other events with this user (such as order
	 * processing).
	 */
	public void setEmailVerificationEnabled(boolean enabled) {
		this.emailVerification_ = enabled;
		if (!enabled) {
			this.emailVerificationReq_ = false;
		}
	}

	/**
	 * Returns whether email verification is configured.
	 * <p>
	 * 
	 * @see AuthService#setEmailVerificationEnabled(boolean enabled)
	 */
	public boolean isEmailVerificationEnabled() {
		return this.emailVerification_;
	}

	/**
	 * Configure email verificiation to be required for login.
	 * <p>
	 * When enabled, a user will not be able to login if the email-address was
	 * not verified.
	 */
	public void setEmailVerificationRequired(boolean enabled) {
		this.emailVerificationReq_ = enabled;
		if (enabled) {
			this.emailVerification_ = true;
		}
	}

	/**
	 * <p>
	 * \ Returns whether email verification is required for login.
	 * <p>
	 * 
	 * @see AuthService#setEmailVerificationRequired(boolean enabled)
	 */
	public boolean isEmailVerificationRequired() {
		return this.emailVerificationReq_;
	}

	/**
	 * Sets the internal path used to present tokens in emails.
	 * <p>
	 * The default path is &quot;/auth/mail/&quot;.
	 */
	public void setEmailRedirectInternalPath(final String internalPath) {
		this.redirectInternalPath_ = internalPath;
	}

	/**
	 * Returns the internal path used for email tokens.
	 * <p>
	 * 
	 * @see AuthService#setEmailRedirectInternalPath(String internalPath)
	 */
	public String getEmailRedirectInternalPath() {
		return this.redirectInternalPath_;
	}

	/**
	 * Parses the emailtoken from an internal path.
	 * <p>
	 * This method parses an internal path and if it matches the email
	 * redirection path, it returns the token contained.
	 * <p>
	 * It returns an empty string if the internal path does not contain an email
	 * token.
	 */
	public String parseEmailToken(final String internalPath) {
		if (this.emailVerification_
				&& WApplication.pathMatches(internalPath,
						this.redirectInternalPath_)) {
			return internalPath.substring(this.redirectInternalPath_.length());
		} else {
			return "";
		}
	}

	/**
	 * Verifies an email address.
	 * <p>
	 * This registers a new email token with the user.
	 * <p>
	 * Then it sends an email to the user&apos;s unverified email address with
	 * instructions that redirect him to this site, using sendConfirmEmail().
	 * <p>
	 */
	public void verifyEmailAddress(final User user, final String address)
			throws javax.mail.MessagingException, UnsupportedEncodingException,
			IOException {
		user.setUnverifiedEmail(address);
		String random = MathUtils.randomId(this.tokenLength_);
		String hash = this.getTokenHashFunction().compute(random, "");
		Token t = new Token(hash, WDate.getCurrentDate().addSeconds(
				this.emailTokenValidity_ * 60));
		user.setEmailToken(t, User.EmailTokenRole.VerifyEmail);
		this.sendConfirmMail(address, user, random);
	}

	/**
	 * Implements lost password functionality.
	 * <p>
	 * If email address verification is enabled, then a user may recover his
	 * password (or rather, chose a new password) using a procedure which
	 * involves sending an email to a verified email address.
	 * <p>
	 * This method triggers this process, starting from an email address, if
	 * this email address corresponds to a verified email address in the
	 * database. The current password is not invalidated.
	 * <p>
	 */
	public void lostPassword(final String emailAddress,
			final AbstractUserDatabase users)
			throws javax.mail.MessagingException, UnsupportedEncodingException,
			IOException {
		User user = users.findWithEmail(emailAddress);
		if (user.isValid()) {
			String random = MathUtils.randomId(this.getRandomTokenLength());
			String hash = this.getTokenHashFunction().compute(random, "");
			WDate expires = WDate.getCurrentDate();
			expires = expires.addSeconds(this.getEmailTokenValidity() * 60);
			Token t = new Token(hash, expires);
			user.setEmailToken(t, User.EmailTokenRole.LostPassword);
			this.sendLostPasswordMail(emailAddress, user, random);
		}
	}

	/**
	 * Processes an email token.
	 * <p>
	 * This processes a token received through an email. If successful, the
	 * token is removed from the database.
	 * <p>
	 * This may return two successful results:
	 * <ul>
	 * <li>{@link }: a token was presented which proves that the user is tied to
	 * the email address.</li>
	 * <li>{@link }: a token was presented which requires the user to enter a new
	 * password.</li>
	 * </ul>
	 * <p>
	 * 
	 * @see AuthService#verifyEmailAddress(User user, String address)
	 * @see AuthService#lostPassword(String emailAddress, AbstractUserDatabase
	 *      users)
	 */
	public EmailTokenResult processEmailToken(final String token,
			final AbstractUserDatabase users) {
		AbstractUserDatabase.Transaction tr = users.startTransaction();
		String hash = this.getTokenHashFunction().compute(token, "");
		User user = users.findWithEmailToken(hash);
		if (user.isValid()) {
			Token t = user.getEmailToken();
			if (t.getExpirationTime().before(WDate.getCurrentDate())) {
				user.clearEmailToken();
				if (tr != null) {
					tr.commit();
				}
				return new EmailTokenResult(EmailTokenResult.Result.Expired);
			}
			switch (user.getEmailTokenRole()) {
			case LostPassword:
				user.clearEmailToken();
				if (tr != null) {
					tr.commit();
				}
				return new EmailTokenResult(
						EmailTokenResult.Result.UpdatePassword, user);
			case VerifyEmail:
				user.clearEmailToken();
				user.setEmail(user.getUnverifiedEmail());
				user.setUnverifiedEmail("");
				if (tr != null) {
					tr.commit();
				}
				return new EmailTokenResult(
						EmailTokenResult.Result.EmailConfirmed, user);
			default:
				if (tr != null) {
					tr.commit();
				}
				return new EmailTokenResult(EmailTokenResult.Result.Invalid);
			}
		} else {
			if (tr != null) {
				tr.commit();
			}
			return new EmailTokenResult(EmailTokenResult.Result.Invalid);
		}
	}

	/**
	 * Configures the duration for an email token to remain valid.
	 * <p>
	 * The default duration is three days (3 * 24 * 60 minutes). Three is a
	 * divine number.
	 */
	public void setEmailTokenValidity(int minutes) {
		this.emailTokenValidity_ = minutes;
	}

	/**
	 * Returns the duration for an email token to remain valid.
	 * <p>
	 * 
	 * @see AuthService#setEmailTokenValidity(int minutes)
	 */
	public int getEmailTokenValidity() {
		return this.emailTokenValidity_;
	}

	/**
	 * Sends an email.
	 * <p>
	 * Sends an email to the given address with subject and body.
	 * <p>
	 * The default implementation will consult configuration properties to add a
	 * sender address if it hasn&apos;t already been set:
	 * <ul>
	 * <li>&quot;auth-mail-sender-name&quot;: the sender name, with default
	 * value &quot;Wt Auth module&quot;</li>
	 * <li>&quot;auth-mail-sender-address&quot;: the sender email address, with
	 * default value &quot;noreply-auth@www.webtoolkit.eu&quot;</li>
	 * </ul>
	 * <p>
	 * Then it uses the JavaMail API to send the message, the SMTP settings are
	 * configured using the smtp.host and smpt.port JWt configuration variables
	 * (see {@link Configuration#setProperties(HashMap properties)}).
	 */
	public void sendMail(final javax.mail.Message message)
			throws javax.mail.MessagingException, UnsupportedEncodingException,
			IOException {
		javax.mail.Message m = message;
		if (MailUtils.isEmpty(m.getFrom())) {
			String senderName = "Wt Auth module";
			String senderAddress = "noreply-auth@www.webtoolkit.eu";
			senderName = WApplication.readConfigurationProperty(
					"auth-mail-sender-name", senderName);
			senderAddress = WApplication.readConfigurationProperty(
					"auth-mail-sender-address", senderAddress);
			m.setFrom(new javax.mail.internet.InternetAddress(senderAddress,
					senderName));
		}
		m.writeTo(System.out);
		MailUtils.sendMail(m);
	}

	/**
	 * Sends a confirmation email to the user to verify his email address.
	 * <p>
	 * Sends a confirmation email to the given address.
	 * <p>
	 * The email content is provided by the following string keys:
	 * <ul>
	 * <li>subject: tr(&quot;Wt.auth.verification-mail.subject&quot;)</li>
	 * <li>body: tr(&quot;Wt.auth.verification-mail.body&quot;) with {1} a place
	 * holder for the identity, and {2} a placeholder for the redirection URL.</li>
	 * <li>HTML body: tr(&quot;Wt.auth.verification-mail.htmlbody&quot;) with
	 * the same place holders.</li>
	 * </ul>
	 */
	protected void sendConfirmMail(final String address, final User user,
			final String token) throws javax.mail.MessagingException,
			UnsupportedEncodingException, IOException {
		javax.mail.Message message = new javax.mail.internet.MimeMessage(
				javax.mail.Session.getDefaultInstance(MailUtils
						.getDefaultProperties()));
		String url = this.createRedirectUrl(token);
		message.addRecipient(javax.mail.Message.RecipientType.TO,
				new javax.mail.internet.InternetAddress(address));
		message.setSubject(WString.tr("Wt.Auth.confirmmail.subject").toString());
		MailUtils.setBody(
				message,
				WString.tr("Wt.Auth.confirmmail.body")
						.arg(user.getIdentity(Identity.LoginName)).arg(token)
						.arg(url));
		MailUtils.addHtmlBody(
				message,
				WString.tr("Wt.Auth.confirmmail.htmlbody")
						.arg(user.getIdentity(Identity.LoginName)).arg(token)
						.arg(url));
		this.sendMail(message);
	}

	/**
	 * Sends an email to the user to enter a new password.
	 * <p>
	 * This sends a lost password email to the given <code>address</code>, with
	 * a given <code>token</code>.
	 * <p>
	 * The default implementation will call
	 * {@link AuthService#sendMail(javax.mail.Message message) sendMail()} with
	 * the following message:
	 * <ul>
	 * <li>tr(&quot;Wt.Auth.lost-password-mail.subject&quot;) as subject,</li>
	 * <li>tr(&quot;Wt.Auth.lost-password-mail.body&quot;) as body to which it
	 * passes user.identity() and token as arguments.</li>
	 * <li>tr(&quot;Wt.Auth.lost-password-mail.htmlbody&quot;) as HTML body to
	 * which it passes user.identity() and token as arguments.</li>
	 * </ul>
	 */
	protected void sendLostPasswordMail(final String address, final User user,
			final String token) throws javax.mail.MessagingException,
			UnsupportedEncodingException, IOException {
		javax.mail.Message message = new javax.mail.internet.MimeMessage(
				javax.mail.Session.getDefaultInstance(MailUtils
						.getDefaultProperties()));
		String url = this.createRedirectUrl(token);
		message.addRecipient(javax.mail.Message.RecipientType.TO,
				new javax.mail.internet.InternetAddress(address));
		message.setSubject(WString.tr("Wt.Auth.lostpasswordmail.subject")
				.toString());
		MailUtils.setBody(message, WString.tr("Wt.Auth.lostpasswordmail.body")
				.arg(user.getIdentity(Identity.LoginName)).arg(token).arg(url));
		MailUtils.addHtmlBody(
				message,
				WString.tr("Wt.Auth.lostpasswordmail.htmlbody")
						.arg(user.getIdentity(Identity.LoginName)).arg(token)
						.arg(url));
		this.sendMail(message);
	}

	protected String createRedirectUrl(final String token) {
		WApplication app = WApplication.getInstance();
		return app.makeAbsoluteUrl(app
				.getBookmarkUrl(this.redirectInternalPath_)) + token;
	}

	// private AuthService(final AuthService anon1) ;
	private IdentityPolicy identityPolicy_;
	private int minimumLoginNameLength_;
	private HashFunction tokenHashFunction_;
	private int tokenLength_;
	private boolean emailVerification_;
	private boolean emailVerificationReq_;
	private int emailTokenValidity_;
	private String redirectInternalPath_;
	private boolean authTokens_;
	private int authTokenValidity_;
	private String authTokenCookieName_;
	private String authTokenCookieDomain_;
}
