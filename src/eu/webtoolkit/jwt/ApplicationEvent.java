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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ApplicationEvent {
  private static Logger logger = LoggerFactory.getLogger(ApplicationEvent.class);

  public ApplicationEvent(
      final String aSessionId, final Runnable aFunction, final Runnable aFallbackFunction) {
    this.sessionId = aSessionId;
    this.function = aFunction;
    this.fallbackFunction = aFallbackFunction;
  }

  public ApplicationEvent(final String aSessionId, final Runnable aFunction) {
    this(aSessionId, aFunction, (Runnable) null);
  }

  public String sessionId;
  public Runnable function;
  public Runnable fallbackFunction;
}
