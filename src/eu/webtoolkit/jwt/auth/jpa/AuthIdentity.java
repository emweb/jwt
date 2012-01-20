package eu.webtoolkit.jwt.auth.jpa;


/**
 * A JPA implementation for an authentication identity
 * 
 * This class is used by {@link AuthInfo}, and stores identities.
 */
public class AuthIdentity {

	/**
	 * Default constructor.
	 */
	public AuthIdentity() {
	}

	/**
	 * Constructor.
	 */
	public AuthIdentity(String provider, String identity) {
		this.provider = provider;
		this.identity = identity;
	}

	/**
	 * Returns the identity owner.
	 */
	public AuthInfo getAuthInfo() {
		return authInfo;
	}
	
	/**
	 * Set the identity owner.
	 */
	public void setAuthInfo(AuthInfo info) {
		this.authInfo = info;
	}

	public String getProvider() {
		return provider;
	}
	
	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
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
	private String provider;
	private String identity;
}
