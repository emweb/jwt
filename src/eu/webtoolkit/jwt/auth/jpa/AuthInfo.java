package eu.webtoolkit.jwt.auth.jpa;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.webtoolkit.jwt.auth.AuthService;
import eu.webtoolkit.jwt.auth.User;

/**
 * A default JPA implementation for authentication data
 * 
 * This class implements the requirements for use as a data type in
 * {@link UserDatabase}.
 * 
 * <ul>
 * It contains collections to two other types: 
 * <li>
 * - {@link #getAuthTokens()} references
 * a collection of authentication tokens (see
 * {@link AuthService#setAuthTokensEnabled(boolean enabled)})
 * </li> 
 * <li>
 * - {@link #getAuthIdentities()}
 * references a collection of identities, which represent all the authentication
 * identities this user has (e.g. a login name, but also an OAuth identity,
 * etc...)
 * </li>
 * </ul>
 * 
 * To use these classes, you need to map them to a database using JPA.
 * 
 * To add extra fields to a user's profile you need to extend {@link AbstractUser}.
 * 
 * @see UserDatabase
 */
public class AuthInfo {
	public AuthInfo() {
		status = User.Status.Normal;
		failedLoginAttempts = 0;
		emailTokenRole = User.EmailTokenRole.VerifyEmail;
		
		email = ""; 
		unverifiedEmail = "";
		emailToken = "";
	}

	/**
	 * Sets the user.
	 * 
	 * This sets the user that owns this authentication information.
	 * {@link AbstractUser} is an empty class by default, you can extend it to add extra field
	 * (e.g.: First name, Last name, ...).
	 */
	public void setUser(AbstractUser user) {
		this.user = user;
	}

	/**
	 * Returns a reference to the user.
	 * 
	 * @see #setUser(AbstractUser user)
	 */
	public AbstractUser getUser() {
		return user;
	}

	/**
	 * Sets a password.
	 */
	public void setPassword(String hash, String hashFunction, String hashSalt) {
		this.passwordHash = hash;
		this.passwordMethod = hashFunction;
		this.passwordSalt = hashSalt;
	}

	/**
	 * Returns the password hash.
	 * 
	 * @see #setPassword(String hash, String hashFunction, String hashSalt)
	 */
	public String getPasswordHash() {
		return passwordHash;
	}
	
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	/**
	 * Returns the password method.
	 * 
	 * @see #setPassword(String hash, String hashFunction, String hashSalt)
	 */
	public String getPasswordMethod() {
		return passwordMethod;
	}
	
	public void setPasswordMethod(String passwordMethod) {
		this.passwordMethod = passwordMethod;
	}

	/**
	 * Returns the password salt.
	 * 
	 * @see #setPassword(String hash, String hashFunction, String hashSalt)
	 */
	public String getPasswordSalt() {
		return passwordSalt;
	}
	
	public void setPasswordSalt(String passwordSalt) {
		this.passwordSalt = passwordSalt;
	}

	/**
	 * Sets the email address.
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Returns the email address.
	 * 
	 * @see #setEmail(String email)
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the unverified email address.
	 */
	public void setUnverifiedEmail(String email) {
		unverifiedEmail = email;
	}

	/**
	 * Returns the unverified email address.
	 * 
	 * @see #setUnverifiedEmail(String email)
	 */
	public String getUnverifiedEmail() {
		return unverifiedEmail;
	}

	/**
	 * Sets the email token.
	 */
	public void setEmailToken(String hash, Date expires,
			User.EmailTokenRole role) {
		emailToken = hash;
		emailTokenExpires = expires;
		emailTokenRole = role;
	}

	/**
	 * Returns the email token.
	 * 
	 * @see #setEmailToken(String hash, Date expires, User.EmailTokenRole role)
	 */
	public String getEmailToken() {
		return emailToken;
	}
	
	public void setEmailToken(String emailToken) {
		this.emailToken = emailToken;
	}

	/**
	 * Returns the email token expiration date.
	 * 
	 * @see #setEmailToken(String hash, Date expires, User.EmailTokenRole role)
	 */
	public Date getEmailTokenExpires() {
		return emailTokenExpires;
	}
	
	public void setEmailTokenExpires(Date emailTokenExpires) {
		this.emailTokenExpires = emailTokenExpires;
	}

	/**
	 * Returns the email token role.
	 * 
	 * @see #setEmailToken(String hash, Date expires, User.EmailTokenRole role)
	 */
	public User.EmailTokenRole getEmailTokenRole() {
		return emailTokenRole;
	}
	
	public void setEmailTokenRole(User.EmailTokenRole emailTokenRole) {
		this.emailTokenRole = emailTokenRole;
	}

	/**
	 * Sets the status.
	 */
	public void setStatus(User.Status status) {
		this.status = status;
	}

	/**
	 * Returns the status.
	 * 
	 * @see #setStatus(User.Status status)
	 */
	public User.Status getStatus() {
		return status;
	}

	/**
	 * Sets the number of failed login attempts.
	 */
	public void setFailedLoginAttempts(int count) {
		failedLoginAttempts = count;
	}

	/**
	 * Returns the number of failed login attempts.
	 * 
	 * @see #setFailedLoginAttempts(int count)
	 */
	public int getFailedLoginAttempts() {
		return failedLoginAttempts;
	}

	/**
	 * Sets the time of the last login attempt.
	 */
	public void setLastLoginAttempt(Date dt) {
		lastLoginAttempt = dt;
	}

	/**
	 * Returns the time of the last login attempt.
	 * 
	 * @see #setLastLoginAttempt(Date dt)
	 */
	public Date getLastLoginAttempt() {
		return lastLoginAttempt;
	}

	/**
	 * Returns the authentication tokens.
	 */
	public List<AuthToken> getAuthTokens() {
		return authTokens;
	}
	
	public void setAuthTokens(List<AuthToken> authTokens) {
		this.authTokens = authTokens;
	}

	/**
	 * Returns the authentication identities.
	 */
	public List<AuthIdentity> getAuthIdentities() {
		return authIdentities;
	}
	
	public void setAuthIdentities(List<AuthIdentity> authIdentities) {
		this.authIdentities = authIdentities;
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	private long id;
	private int version;
	
	private String passwordHash;
	private String passwordMethod;
	private String passwordSalt;

	private User.Status status;

	private int failedLoginAttempts;
	private Date lastLoginAttempt;

	private String email; 
	private String unverifiedEmail;
	private String emailToken;
	private Date emailTokenExpires;
	private User.EmailTokenRole emailTokenRole;

	private AbstractUser user;
	private List<AuthToken> authTokens = new ArrayList<AuthToken>();
	private List<AuthIdentity> authIdentities = new ArrayList<AuthIdentity>();
}
