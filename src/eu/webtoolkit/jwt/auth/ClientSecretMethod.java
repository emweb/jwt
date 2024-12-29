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
 * Enumeration of the OAuth client authorization method.
 *
 * <p>Specifies how the OAuth client authorizes itself with the auth provider. I.e. how it passes
 * the client ID and secret to the provider.
 *
 * <p>
 *
 * @see OAuthService#getClientSecretMethod()
 * @see OAuthClient#getAuthMethod()
 */
public enum ClientSecretMethod {
  /** Pass the client ID and secret to the auth provider with a GET request with Basic auth. */
  HttpAuthorizationBasic,
  /**
   * Pass the client ID and secret to the auth provider as URL parameters of a GET request.
   *
   * <p>This is not part of the standard but this is what Facebook does.
   */
  PlainUrlParameter,
  /** Pass the client ID and secret to the auth provider as parameters of a POST request. */
  RequestBodyParameter;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
