/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

import eu.webtoolkit.jwt.*;
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

class Topic extends WObject {
  private static Logger logger = LoggerFactory.getLogger(Topic.class);

  public Topic() {
    super();
  }

  public void populateSubMenu(WMenu menu) {}

  public static WString reindent(final CharSequence text) {
    List<String> lines = new ArrayList<String>();
    String s = text.toString();
    StringUtils.split(lines, s, "\n", false);
    String result = "";
    int indent = -1;
    int newlines = 0;
    for (int i = 0; i < lines.size(); ++i) {
      final String line = lines.get(i);
      if (line.length() == 0) {
        ++newlines;
      } else {
        if (indent == -1) {
          indent = countSpaces(line);
        } else {
          for (int j = 0; j < newlines; ++j) {
            result += '\n';
          }
        }
        newlines = 0;
        if (result.length() != 0) {
          result += '\n';
        }
        result += skipSpaces(line, indent);
      }
    }
    return new WString(result);
  }

  static int countSpaces(final String line) {
    for (int pos = 0; pos < line.length(); ++pos) {
      if (line.charAt(pos) != ' ') {
        return pos;
      }
    }
    return line.length();
  }

  static String skipSpaces(final String line, int count) {
    if (line.length() >= count) {
      return line.substring(count);
    } else {
      return "";
    }
  }
}
