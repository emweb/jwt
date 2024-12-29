/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.auth;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract cryptographic hash function interface.
 *
 * <p>A cryptographic hash function computes a hash value from a message, for which it is hard to
 * guess another message that generates the same hash.
 *
 * <p>These hash functions are intended for short messages, typically passwords or random tokens,
 * and thus not suitable for computing the hash value of a large document.
 *
 * <p>When used for passwords, to avoid dictionary attacks, the hash functions accept also a random
 * salt which is hashed together with the password. Not all hash functions are adequate for
 * passwords hashes.
 */
public abstract class HashFunction {
  private static Logger logger = LoggerFactory.getLogger(HashFunction.class);

  /**
   * Returns the name for this hash function.
   *
   * <p>This should return a (short) name that uniquely identifies this hash function.
   */
  public abstract String getName();
  /**
   * Computes the hash of a message + salt.
   *
   * <p>The message is usually an ASCII or UTF-8 string.
   *
   * <p>The <code>salt</code> and the computed hash are encoded in printable characters. This is
   * usually ASCII-encoded or could be Base64-encoded.
   */
  public abstract String compute(final String msg, final String salt);
  /**
   * Verifies a message with the salted hash.
   *
   * <p>The base implementation will recompute the hash of the message with the given salt, and
   * compare it to the <code>hash</code>.
   *
   * <p>Some methods however store the salt and additional settings in the <code>hash</code>, and
   * this information is thus needed to verify the message hash.
   */
  public boolean verify(final String msg, final String salt, final String hash) {
    return this.compute(msg, salt).equals(hash);
  }
}
