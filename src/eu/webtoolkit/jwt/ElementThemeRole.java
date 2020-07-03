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
import java.time.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;

/**
 * Enumeration for the role of a DOM element (for theme support)
 *
 * <p>
 *
 * @see WTheme#apply(WWidget widget, WWidget child, int widgetRole)
 */
public class ElementThemeRole {
  public static final int MainElement = 0;
  public static final int ProgressBarBar = 100;
  public static final int ProgressBarLabel = 101;
}
