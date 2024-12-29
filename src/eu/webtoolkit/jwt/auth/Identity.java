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
 * A class that represents a user identity.
 *
 * <p>The identity is the result of an authentication process. Although the most common
 * authentication method (password authentication) only returns a user name, other methods (such as
 * OAuth, client SSL certificates, or an authentication reverse proxy server) may return more
 * information.
 *
 * <p>At the very least, the user is identified using a unique ID, and it optionally also contains
 * name and email address information.
 *
 * <p>
 */
public class Identity {
  private static Logger logger = LoggerFactory.getLogger(Identity.class);

  /**
   * Default constructor.
   *
   * <p>Creates an invalid identity.
   */
  public Identity() {
    this.provider_ = "";
    this.id_ = "";
    this.email_ = "";
    this.name_ = "";
  }
  /** Constructor. */
  public Identity(
      final String provider,
      final String id,
      final String name,
      final String email,
      boolean emailVerified) {
    this.provider_ = provider;
    this.id_ = id;
    this.email_ = email;
    this.name_ = name;
    this.emailVerified_ = emailVerified;
  }
  /**
   * Returns whether the identity is valid.
   *
   * <p>An invalid identity is used to indicate for example that no identity information could be
   * obtained.
   */
  public boolean isValid() {
    return this.id_.length() != 0;
  }
  /**
   * Returns the provider name.
   *
   * <p>This is a unique id that names the source for this identity (e.g. &quot;google-oauth&quot;,
   * or &quot;LDAP&quot;, or &quot;user&quot; (for a user-chosen identity).
   */
  public String getProvider() {
    return this.provider_;
  }
  /**
   * Returns the id.
   *
   * <p>Returns a unique identifier for the user within the scope of this provider.
   */
  public String getId() {
    return this.id_;
  }
  /**
   * Returns the name.
   *
   * <p>Returns the user&apos;s name, or an empty string if not provided.
   */
  public String getName() {
    return this.name_;
  }
  /**
   * Returns an email address.
   *
   * <p>Returns the user&apos;s email address, or an empty string if not provided.
   *
   * <p>
   *
   * @see Identity#isEmailVerified()
   */
  public String getEmail() {
    return this.email_;
  }
  /**
   * Returns whether the email address has been verified.
   *
   * <p>The third party provider may be able to guarantee that the user indeed also control&apos;s
   * the given email address (e.g. because the third party hosts that email account for the user).
   *
   * <p>
   *
   * @see Identity#getEmail()
   */
  public boolean isEmailVerified() {
    return this.emailVerified_;
  }
  /**
   * An invalid identity constant.
   *
   * <p>This is an identity that is not {@link Identity#isValid() isValid()}.
   */
  public static final Identity Invalid = new Identity();
  /**
   * The login name identity.
   *
   * <p>This is a provider name for the (usually user-controlled) identity, used for example for
   * password-based authentication.
   */
  public static final String LoginName = "loginname";
  /**
   * The default multi-factor identity.
   *
   * <p>This is the name used for the default implementation of JWt for MFA. While the actual
   * implementation of any additional factor can be changed, the default implementation (using
   * TOTP), will use this identity name.
   *
   * <p>It will dictate what the identity entry in the database will be called. Which stores the
   * TOTP secret key in the database.
   *
   * <p>
   *
   * <p><i><b>Note: </b>This entry is not encrypted or obfuscated in any way. </i>
   */
  public static final String MultiFactor = "multifactor";

  private String provider_;
  private String id_;
  private String email_;
  private String name_;
  private boolean emailVerified_;
}
