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
 * A class that stores informations about data.
 *
 * <p>This is a barebone version of {@link WAbstractDataInfo}. It simply stores the information
 * given to it.
 *
 * <p>
 */
public class WDataInfo extends WAbstractDataInfo {
  private static Logger logger = LoggerFactory.getLogger(WDataInfo.class);

  /** Creates an empty {@link WDataInfo}. */
  public WDataInfo() {
    super();
    this.url_ = "";
    this.filePath_ = "";
    this.dataUri_ = "";
  }
  /**
   * Creates a {@link WDataInfo}.
   *
   * <p>Creates a {@link WDataInfo} with the given <code>url</code> and <code>filePath</code>.
   */
  public WDataInfo(final String url, final String filePath) {
    super();
    this.url_ = url;
    this.filePath_ = filePath;
    this.dataUri_ = "";
  }
  /** Sets the file path. */
  public void setFilePath(final String filePath) {
    this.filePath_ = filePath;
  }
  /**
   * Returns a path to a file containing the data.
   *
   * <p>Throws if the file path is set to an empty string.
   *
   * <p>
   *
   * @see WDataInfo#hasFilePath()
   */
  public String getFilePath() {
    if (this.hasFilePath()) {
      return this.filePath_;
    }
    return super.getFilePath();
  }
  /** Sets the URL. */
  public void setUrl(final String url) {
    this.url_ = url;
  }
  /**
   * Returns the URL of the data.
   *
   * <p>Throws if the URL is set to an empty string.
   *
   * <p>
   *
   * @see WDataInfo#hasUrl()
   */
  public String getUrl() {
    if (this.hasUrl()) {
      return this.url_;
    }
    return super.getUrl();
  }
  /** Sets the data formated as data URI. */
  public void setDataUri(final String dataUri) {
    this.dataUri_ = dataUri;
  }
  /**
   * Returns the data in data URI format.
   *
   * <p>Throws if the data URI is set to an empty string.
   *
   * <p>
   *
   * @see WDataInfo#hasDataUri()
   */
  public String getDataUri() {
    if (this.hasDataUri()) {
      return this.dataUri_;
    }
    return super.getDataUri();
  }
  /**
   * Returns whether this contains a file path.
   *
   * <p>This returns whether {@link WDataInfo#getFilePath() getFilePath()} returns a non-empty
   * string.
   *
   * <p>
   *
   * @see WDataInfo#getFilePath()
   */
  public boolean hasFilePath() {
    return this.filePath_.length() != 0;
  }
  /**
   * Returns whether this contains a url.
   *
   * <p>This returns whether {@link WDataInfo#getUrl() getUrl()} returns a non-empty string.
   *
   * <p>
   *
   * @see WDataInfo#getUrl()
   */
  public boolean hasUrl() {
    return this.url_.length() != 0;
  }
  /**
   * Returns whether this can return the data in data URI format.
   *
   * <p>This returns whether {@link WDataInfo#getDataUri() getDataUri()} returns a non-empty string.
   *
   * <p>
   *
   * @see WDataInfo#getDataUri()
   */
  public boolean hasDataUri() {
    return this.dataUri_.length() != 0;
  }

  private String url_;
  private String filePath_;
  private String dataUri_;
}
