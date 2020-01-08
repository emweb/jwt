/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
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
 * Enumeration for a password verification result.
 * <p>
 * 
 * @see AbstractPasswordService#verifyPassword(User user, String password)
 */
public enum PasswordResult {
	/**
	 * The password is invalid.
	 */
	PasswordInvalid,
	/**
	 * The attempt was not processed because of throttling.
	 */
	LoginThrottling,
	/**
	 * The password is valid.
	 */
	PasswordValid;

	/**
	 * Returns the numerical representation of this enum.
	 */
	public int getValue() {
		return ordinal();
	}
}
