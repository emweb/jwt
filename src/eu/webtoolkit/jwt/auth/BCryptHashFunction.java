package eu.webtoolkit.jwt.auth;

/**
 *  A cryptograhpic hash function implemented using BCrypt.
 *
 * 	This hash function can be used for creating password hashes.
 */
public class BCryptHashFunction extends HashFunction {
	/** Returns the name for this hash function.
	 *
	 * Returns <tt>"BCrypt"</tt>.
	 */
	@Override
	public String getName() {
		return "BCrypt";
	}

	@Override
	public String compute(String msg, String salt) {
		return BCrypt.hashpw(msg, BCrypt.gensalt());
	}
	
	@Override
	public boolean verify(String msg, String salt, String hash) {
		return BCrypt.checkpw(msg, hash);
	}
}
