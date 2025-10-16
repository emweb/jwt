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
 * An abstract base class storing information of a resource.
 *
 * <p>This is an abstract class which is meant to store/compute information about a resource/file.
 * Its primary use is to map URIs to file paths. This is to avoid confusion when rendering out these
 * resources, so that depending on the context the resources is created under, locating the file
 * correctly happens.
 */
public class WAbstractDataInfo {
  private static Logger logger = LoggerFactory.getLogger(WAbstractDataInfo.class);

  /**
   * Returns a path to a file containing the data.
   *
   * <p>This returns a path to a file containing the data. This should point to a path that exists
   * on the system.
   *
   * <p>By default this will throw an exception.
   *
   * <p>
   *
   * <p><i><b>Warning: </b>If you reimplement this function, you must also reimplement {@link
   * WAbstractDataInfo#hasFilePath() hasFilePath()} </i>
   *
   * @see WAbstractDataInfo#hasFilePath()
   */
  public String getFilePath() {
    throw new WException("WAbstractDataInfo::filePath(): missing file path.");
  }
  /**
   * Returns the URL of the data.
   *
   * <p>This returns the URL of the data. This can be both an absolute URL or a URL relative to the
   * application&apos;s base URL.
   *
   * <p>By default this will throw an exception.
   *
   * <p>
   *
   * <p><i><b>Warning: </b>If you reimplement this function, you must also reimplement {@link
   * WAbstractDataInfo#hasUrl() hasUrl()} </i>
   *
   * @see WAbstractDataInfo#hasUrl()
   */
  public String getUrl() {
    throw new WException("WAbstractDataInfo::url(): missing URL.");
  }
  /**
   * Returns the data in data URI format.
   *
   * <p>This returns the data in data URI format (see: <a
   * href="https://developer.mozilla.org/en-US/docs/Web/URI/Reference/Schemes/data">https://developer.mozilla.org/en-US/docs/Web/URI/Reference/Schemes/data</a>).
   *
   * <p>By default this will throw an exception.
   *
   * <p>
   *
   * <p><i><b>Warning: </b>If you reimplement this function, you must also reimplement {@link
   * WAbstractDataInfo#hasDataUri() hasDataUri()} </i>
   *
   * @see WAbstractDataInfo#hasDataUri()
   */
  public String getDataUri() {
    throw new WException("WAbstractDataInfo::dataUri(): missing data URI.");
  }
  /**
   * Returns the name of the Data.
   *
   * <p>This returns the name of the data. This is mainly use for error reporting.
   *
   * <p>By default this will return {@link WAbstractDataInfo#getUrl() getUrl()} if {@link
   * WAbstractDataInfo#hasUrl() hasUrl()} is <code>true</code>. In case it is <code>false</code>, it
   * will return {@link WAbstractDataInfo#getFilePath() getFilePath()} if {@link
   * WAbstractDataInfo#hasFilePath() hasFilePath()} is <code>true</code>, and it will return an
   * empty string otherwise.
   */
  public String getName() {
    if (this.hasUrl()) {
      return this.getUrl();
    } else {
      if (this.hasFilePath()) {
        return this.getFilePath();
      } else {
        if (this.hasDataUri()) {
          return this.getDataUri();
        }
      }
    }
    return "";
  }
  /**
   * Returns whether this contains a path to a file.
   *
   * <p>This returns whether filePath returns a path to a file containing the data.
   *
   * <p>By default this returns <code>false</code>.
   *
   * <p>
   *
   * @see WAbstractDataInfo#getFilePath()
   */
  public boolean hasFilePath() {
    return false;
  }
  /**
   * Returns whether this contains a URL.
   *
   * <p>This returns whether {@link WAbstractDataInfo#getUrl() getUrl()} returns a URL of the data.
   *
   * <p>By default this returns <code>false</code>.
   *
   * <p>
   *
   * @see WAbstractDataInfo#getUrl()
   */
  public boolean hasUrl() {
    return false;
  }
  /**
   * Returns whether this can return the data in data URI format.
   *
   * <p>This returns whether {@link WAbstractDataInfo#getDataUri() getDataUri()} returns the data in
   * data URI format.
   *
   * <p>By default this returns <code>false</code>.
   *
   * <p>
   *
   * @see WAbstractDataInfo#getDataUri()
   */
  public boolean hasDataUri() {
    return false;
  }
}
