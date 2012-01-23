package eu.webtoolkit.jwt.auth.jpa;

import java.util.Date;

/**
 * A JPA implementation for an authentication token.
 * 
 * This class is used by {@link AuthInfo}, and stores authentication tokens.
 * 
 * @see AuthInfo
 */
public class AuthToken {
	/**
	 * Default constructor.
	 */
	public AuthToken() {
	}

	/**
	 * Constructor.
	 */
	public AuthToken(String value, Date expiryDate) {
		this.value = value;
		this.expiryDate = expiryDate;
	}

	/**
	 * Returns the token owner.
	 */
	public AuthInfo getAuthInfo() {
		return authInfo;
	}
	
	/**
	 * Set the token owner.
	 */
	public void setAuthInfo(AuthInfo info) {
		this.authInfo = info;
	}

	/**
	 * Returns the token value.
	 */
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Returns the token expiry date.
	 */
	public Date getExpiryDate() {
		return expiryDate;
	}
	
	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
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
	private AuthInfo authInfo;
	private String value;
	private Date expiryDate;
}