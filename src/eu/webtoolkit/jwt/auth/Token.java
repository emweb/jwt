/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.auth;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An authentication token hash.
 *
 * <p>An authentication token is a surrogate for identification or authentication. When a random
 * authentication token is generated, it is a good practice to hash it using a cryptographic hash
 * function, and only save this hash in the session or database for later verification. This avoids
 * that a compromised database would leak all the authentication tokens.
 *
 * <p>
 *
 * @see User#addAuthToken(Token token)
 * @see User#setEmailToken(Token token, User.EmailTokenRole role)
 */
public class Token {
  private static Logger logger = LoggerFactory.getLogger(Token.class);

  /**
   * Default constructor.
   *
   * <p>Creates an empty token.
   */
  public Token() {
    this.hash_ = "";
    this.expirationTime_ = null;
  }

  public Token(final String hash, final WDate expirationTime) {
    this.hash_ = hash;
    this.expirationTime_ = expirationTime;
  }
  // public  Token(final String hash, final WDate expirationTime, final String purpose, final String
  // scope, final String redirectUri) ;
  /**
   * Returns whether the token is empty.
   *
   * <p>An empty token is default constructed.
   */
  public boolean isEmpty() {
    return this.hash_.length() == 0;
  }
  /** Returns the hash. */
  public String getHash() {
    return this.hash_;
  }
  /** Returns the expiration time. */
  public WDate getExpirationTime() {
    return this.expirationTime_;
  }

  private String hash_;
  private WDate expirationTime_;
}
