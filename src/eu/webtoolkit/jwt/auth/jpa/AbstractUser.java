package eu.webtoolkit.jwt.auth.jpa;

/**
 * By extending this class, and using the extended implementation in {@link AuthInfo}, 
 * extra fields can be added to the {@link AuthInfo} object.
 */
public class AbstractUser {
	public AbstractUser() {
		
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
}
