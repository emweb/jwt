/*
 * Copyright (C) 2025 Emweb bv, Herent, Belgium.
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
 * A class that stores informations about a file in the WebRoot
 *
 * <p>This class stores the URL and the file path of a file inside of the
 *    WebRoot.
 *
 * <p>
 */
public class WWebRootDataInfo extends WAbstractDataInfo {
  private static Logger logger = LoggerFactory.getLogger(WWebRootDataInfo.class);

  /**
   * Creates a {@link WWebRootDataInfo}.
   *
   * <p>Creates a {@link WWebRootDataInfo} with the given <code>virtualPath</code>.
   */
  public WWebRootDataInfo(final String virtualPath) {
    super();
    this.virtualPath_ = virtualPath;
  }
  /** Sets the virtual path. */
  public void setVirtualPath(final String virtualPath) {
    this.virtualPath_ = virtualPath;
  }

  public String getFilePath() {
    if (this.hasFilePath()) {
      return WApplication.getInstance().getEnvironment().getServer().getServletContext().getRealPath(this.virtualPath_);
    }
    return super.getFilePath();
  }

  public String getUrl() {
    if (this.hasUrl()) {
      return this.virtualPath_;
    }
    return super.getUrl();
  }
  /**
   * Returns whether this contains a file path.
   *
   * <p>This returns whether {@link WWebRootDataInfo#getFilePath() getFilePath()} returns a non-empty
   * string.
   *
   * <p>
   *
   * @see WWebRootDataInfo#getFilePath()
   */
  public boolean hasFilePath() {
    return this.virtualPath_.length() != 0;
  }
  /**
   * Returns whether this contains a URL.
   *
   * <p>This returns whether {@link WWebRootDataInfo#getUrl() getUrl()} returns a non-empty string.
   *
   * <p>
   *
   * @see WWebRootDataInfo#getUrl()
   */
  public boolean hasUrl() {
    return this.virtualPath_.length() != 0;
  }

  private String virtualPath_;
}
