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

/**
 * Enumeration for a password verification result.
 *
 * <p>
 *
 * @see AbstractPasswordService#verifyPassword(User user, String password)
 */
public enum PasswordResult {
  /** The password is invalid. */
  PasswordInvalid,
  /** The attempt was not processed because of throttling. */
  LoginThrottling,
  /** The password is valid. */
  PasswordValid;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
