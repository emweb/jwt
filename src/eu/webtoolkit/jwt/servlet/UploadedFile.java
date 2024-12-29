/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.servlet;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.auth.*;
import eu.webtoolkit.jwt.auth.mfa.*;
import eu.webtoolkit.jwt.chart.*;
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
 * Details about a file uploaded with a request to a resource.
 *
 * <p>
 *
 * @see WResource#handleRequest(WebRequest request, WebResponse response)
 */
public class UploadedFile {
  private static Logger logger = LoggerFactory.getLogger(UploadedFile.class);

  public UploadedFile() {
    this.fileInfo_ = null;
  }

  UploadedFile(final String spoolName, final String clientFileName, final String contentType) {
    this.fileInfo_ = null;
    this.fileInfo_ = new UploadedFile.Impl();
    this.fileInfo_.spoolFileName = spoolName;
    this.fileInfo_.clientFileName = clientFileName;
    this.fileInfo_.contentType = contentType;
    this.fileInfo_.isStolen = false;
  }
  /**
   * Return the spool file name.
   *
   * <p>This is the location on the local (server) filesystem where the uploaded file is temporarily
   * stored. Unless you call {@link UploadedFile#stealSpoolFile() stealSpoolFile()}, this file is
   * deleted automatically.
   */
  public String getSpoolFileName() {
    return this.fileInfo_.spoolFileName;
  }
  /**
   * Returns the client file name.
   *
   * <p>This is the location that was indicated by the browser.
   *
   * <p>
   *
   * <p><i><b>Note: </b>Depending on the browser this is an absolute path or only the file name.
   * </i>
   */
  public String getClientFileName() {
    return this.fileInfo_.clientFileName;
  }
  /**
   * Returns the file content type.
   *
   * <p>Returns the content mime-type that was sent along with the uploaded file.
   */
  public String getContentType() {
    return this.fileInfo_.contentType;
  }
  /**
   * Steals the uploaded spool file.
   *
   * <p>By stealing the spooled file, it is no longer automatically deleted by JWt.
   */
  public void stealSpoolFile() {
    this.fileInfo_.isStolen = true;
  }

  static class Impl {
    private static Logger logger = LoggerFactory.getLogger(Impl.class);

    public String spoolFileName;
    public String clientFileName;
    public String contentType;
    public boolean isStolen;

    public void finalize() {
      if (!this.isStolen) {
        new File(this.spoolFileName).delete();
      }
    }
  }

  private UploadedFile.Impl fileInfo_;
  private static StringWriter emptyStream = new StringWriter();

  static String str(String s) {
    return s != null ? s : "";
  }
}
