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

/** Internal class. Necessary for wt-port. */
public class DataUri {
  private static Logger logger = LoggerFactory.getLogger(DataUri.class);

  public DataUri(final String uriString) {
    this.mimeType = "";
    this.data = new ArrayList<Byte>();
    this.parse(uriString);
  }

  public String mimeType;
  public List<Byte> data;

  public static boolean isDataUri(final String uriString) {
    return uriString.startsWith("data:");
  }

  private void parse(final String uriString) {
    try {
      int dataEndPos = uriString.indexOf("data:") + 5;
      int commaPos = uriString.indexOf(",");
      if (commaPos == -1) {
        commaPos = dataEndPos;
      }
      this.mimeType = uriString.substring(dataEndPos, dataEndPos + commaPos - dataEndPos);
      String d = uriString.substring(commaPos + 1);
      Utils.copyList(Utils.base64Decode(d), this.data);
      if (!this.mimeType.endsWith(";base64") || this.data.isEmpty()) {
        throw new WException("Ill formed data URI: " + uriString);
      } else {
        this.mimeType = this.mimeType.substring(0, 0 + this.mimeType.indexOf(";"));
      }
    } catch (IOException ie) {
      logger.info("Ignoring exception {}", ie.getMessage(), ie);
    }
  }
}
