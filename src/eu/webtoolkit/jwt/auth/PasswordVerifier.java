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
 * Password hash computation and verification class.
 * <p>
 * 
 * This class implements the logic for comparing passwords against password
 * hashes, or computing a new password hash for a password.
 * <p>
 * One or more hash functions can be added, which allow you to introduce a new
 * &quot;preferred&quot; hash function while maintaining support for verifying
 * existing passwords hashes.
 */
public class PasswordVerifier implements PasswordService.AbstractVerifier {
	private static Logger logger = LoggerFactory
			.getLogger(PasswordVerifier.class);

	/**
	 * Constructor.
	 */
	public PasswordVerifier() {
		super();
		this.hashFunctions_ = new ArrayList<HashFunction>();
		this.saltLength_ = 12;
	}

	// public void setSaltLength(int words) ;
	// public int getSaltLength() ;
	/**
	 * Adds a hash function.
	 * <p>
	 * The first hash function added is the one that will be used for creating
	 * new password hashes, i.e. the &quot;preferred&quot; hash function. The
	 * other hash functions are used only for verifying existing hash passwords.
	 * This allows you to move to new hash functions as other ones are no longer
	 * deemed secure.
	 * <p>
	 * Each hash function has a unique name, which is annotated in the generated
	 * hash to identify the appropriate hash funtion to evaluate it.
	 * <p>
	 * Ownership of the hash functions is transferred.
	 * <p>
	 */
	public void addHashFunction(HashFunction function) {
		this.hashFunctions_.add(function);
	}

	/**
	 * Returns the list of hash functions.
	 * <p>
	 * This returns a list with references to hashfunctions that have been added
	 * with {@link PasswordVerifier#addHashFunction(HashFunction function)
	 * addHashFunction()}.
	 */
	public List<HashFunction> getHashFunctions() {
		return this.hashFunctions_;
	}

	public boolean needsUpdate(PasswordHash hash) {
		return !hash.getFunction().equals(
				this.getHashFunctions().get(0).getName());
	}

	/**
	 * Computes the password hash for a clear text password.
	 * <p>
	 * This creates new salt and applies the &quot;preferred&quot; hash function
	 * to the salt and clear text password to compute the hash.
	 * <p>
	 */
	public PasswordHash hashPassword(CharSequence password) {
		String msg = password.toString();
		String salt = AuthUtils.createSalt(this.saltLength_);
		salt = Utils.base64Encode(salt);
		HashFunction f = this.hashFunctions_.get(0);
		String hash = f.compute(msg, salt);
		return new PasswordHash(f.getName(), salt, hash);
	}

	/**
	 * Verifies a password against a hash.
	 * <p>
	 * This verifies whether the password matches the hash.
	 * <p>
	 * 
	 * @see PasswordVerifier#hashPassword(CharSequence password)
	 */
	public boolean verify(CharSequence password, PasswordHash hash) {
		for (int i = 0; i < this.hashFunctions_.size(); ++i) {
			HashFunction f = this.hashFunctions_.get(i);
			if (f.getName().equals(hash.getFunction())) {
				return f.verify(password.toString(), hash.getSalt(), hash
						.getValue());
			}
		}
		logger.error(new StringWriter().append(
				"verify() no hash configured for ").append(hash.getFunction())
				.toString());
		return false;
	}

	private List<HashFunction> hashFunctions_;
	private int saltLength_;
}
