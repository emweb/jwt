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

/**
 * An abstract interface for a loading indicator.
 *
 * <p>The loading indicator displays a message while a response from the server is pending.
 *
 * <p>The widget will be shown and hidden using {@link WWidget#show()} and {@link WWidget#hide()}.
 * If you want to customize this behaviour, you should reimplement the {@link
 * WWidget#setHidden(boolean hidden, WAnimation animation) WWidget#setHidden()} method.
 *
 * <p>
 *
 * @see WApplication#setLoadingIndicator(WLoadingIndicator indicator)
 */
public abstract class WLoadingIndicator extends WCompositeWidget {
  private static Logger logger = LoggerFactory.getLogger(WLoadingIndicator.class);

  // public  WLoadingIndicator() ;
  /**
   * Destructor.
   *
   * <p>The destructor must delete the widget().
   */
  public void remove() {
    super.remove();
  }
  /**
   * Sets the message that you want to be displayed.
   *
   * <p>If the indicator is capable of displaying a text message, then you should reimplement this
   * method to allow this message to be modified.
   */
  public abstract void setMessage(final CharSequence text);
}
