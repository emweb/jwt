/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Enumeration that indicates what validation styles are to be applie.
 *
 * <p>
 *
 * @see WTheme#applyValidationStyle(WWidget widget, WValidator.Result validation, EnumSet flags)
 */
public enum ValidationStyleFlag {
  ValidationInvalidStyle,
  ValidationValidStyle;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
  /** Remove validation styles. */
  public static final EnumSet<ValidationStyleFlag> ValidationNoStyle =
      EnumSet.noneOf(ValidationStyleFlag.class);
  /** All validation styles. */
  public static final EnumSet<ValidationStyleFlag> ValidationAllStyles =
      EnumSet.of(
          ValidationStyleFlag.ValidationInvalidStyle, ValidationStyleFlag.ValidationValidStyle);
}
