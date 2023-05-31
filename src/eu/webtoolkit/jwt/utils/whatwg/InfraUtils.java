/*
 * Copyright (C) 2023 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */

package eu.webtoolkit.jwt.utils.whatwg;

public class InfraUtils {

  private InfraUtils() {}

  // https://infra.spec.whatwg.org/#strip-newlines
  public static String stripNewlines(String s) {
    return s.replaceAll("[\r\n]", "");
  }

  // https://infra.spec.whatwg.org/#strip-leading-and-trailing-ascii-whitespace
  public static String trim(String s) {
    return s.replaceAll("(^[\r\n\f\t ]+)|([\r\n\f\t ]+$)", "");
  }
}
