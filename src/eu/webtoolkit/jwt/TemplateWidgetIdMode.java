/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

import eu.webtoolkit.jwt.auth.*;
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

/** Enumeration that indicates how a widget&apos;s ID may be set. */
public enum TemplateWidgetIdMode {
  /** Do not set the widget ID. */
  None,
  /**
   * Use setObjectName() to add a &apos;data-object-name&apos; attribute. This is a safe choice
   * since it does not affect the ID.
   */
  SetObjectName,
  /**
   * Use setId() to set the ID as the varName.
   *
   * <p>
   *
   * <p><i><b>Warning: </b>You must be careful that there are no two widgets with the same ID in yor
   * application. </i>
   */
  SetId;

  /** Returns the numerical representation of this enum. */
  public int getValue() {
    return ordinal();
  }
}
