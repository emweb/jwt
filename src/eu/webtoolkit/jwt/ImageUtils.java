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

class ImageUtils {
  private static Logger logger = LoggerFactory.getLogger(ImageUtils.class);

  public static String identifyMimeType(final List<Byte> header) {
    for (int i = 0; i < mimeTypeCount; ++i) {
      if (Utils.memcmp(header, imageHeaders[i], imageHeaderSize[i]) == 0) {
        return imageMimeTypes[i];
      }
    }
    return "";
  }

  public static String identifyMimeType(final String fileName) {
    List<Byte> header = FileUtils.fileHeader(fileName, 25);
    if (header.isEmpty()) {
      return "";
    } else {
      return identifyMimeType(header);
    }
  }

  private static final int mimeTypeCount = 12;
  private static String[] imageMimeTypes = {
    "image/png",
    "image/jpeg",
    "image/gif",
    "image/gif",
    "image/bmp",
    "image/bmp",
    "image/bmp",
    "image/bmp",
    "image/bmp",
    "image/bmp",
    "image/svg",
    "image/svg"
  };
  private static String[] imageHeaders = {
    "\211PNG\r\n\032\n",
    "\377\330\377",
    "GIF87a",
    "GIF89a",
    "BA",
    "BM",
    "CI",
    "CP",
    "IC",
    "PI",
    "<?xml",
    "<svg"
  };
  private static int[] imageHeaderSize = {8, 3, 6, 6, 2, 2, 2, 2, 2, 2, 5, 4};

  static int toUnsigned(int c) {
    int result = c;
    if (result < 0) {
      result += 256;
    }
    return result;
  }
}
