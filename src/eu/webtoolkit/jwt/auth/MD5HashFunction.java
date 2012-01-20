package eu.webtoolkit.jwt.auth;

import eu.webtoolkit.jwt.Utils;

/**
 *  A cryptograhpic hash function implemented using MD5.
 *
 * This hash function is useful for creating token hashes, but
 * should not be used for password hashes.
 */
public class MD5HashFunction extends HashFunction {
	@Override
	public String compute(String msg, String salt) {
		return Utils.base64Encode(Utils.md5(salt + msg));		
	}

	/** Returns the name for this hash function.
	 *
	 * Returns <tt>"MD5"</tt>.
	 */
	@Override
	public String getName() {
		return "MD5";
	}
}
