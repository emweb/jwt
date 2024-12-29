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

class ColorUtils {
  private static Logger logger = LoggerFactory.getLogger(ColorUtils.class);

  private ColorUtils() {}

  static boolean ishex(char c) {
    return c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
  }

  static boolean tailIsHex(final String str) {
    for (int i = 1; i < str.length(); ++i) {
      if (!ishex(str.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  static int parseRgbArgument(final String argument) {
    String arg = argument.trim();
    try {
      if (arg.endsWith("%")) {
        return (int) (Double.parseDouble(arg.substring(0, 0 + arg.length() - 1)) * 255 / 100);
      } else {
        return Integer.parseInt(arg);
      }
    } catch (final RuntimeException e) {
      logger.error(new StringWriter().append("invalid color component: ").append(arg).toString());
      return 0;
    }
  }

  static int replicateHex(final String s) {
    int result = Utils.hexToInt(s);
    return result | result << 4;
  }

  public static WColor parseCssColor(final String name) {
    String n = name;
    n = n.trim();
    int red = 0;
    int green = 0;
    int blue = 0;
    int alpha = 255;
    if (n.startsWith("#")) {
      if (n.length() - 1 == 3 && tailIsHex(n)) {
        red = replicateHex(n.substring(1, 1 + 1));
        green = replicateHex(n.substring(2, 2 + 1));
        blue = replicateHex(n.substring(3, 3 + 1));
      } else {
        if (n.length() - 1 == 4 && tailIsHex(n)) {
          red = replicateHex(n.substring(1, 1 + 1));
          green = replicateHex(n.substring(2, 2 + 1));
          blue = replicateHex(n.substring(3, 3 + 1));
          alpha = replicateHex(n.substring(4, 4 + 1));
        } else {
          if (n.length() - 1 == 6 && tailIsHex(n)) {
            red = Utils.hexToInt(n.substring(1, 1 + 2));
            green = Utils.hexToInt(n.substring(3, 3 + 2));
            blue = Utils.hexToInt(n.substring(5, 5 + 2));
          } else {
            if (n.length() - 1 == 8 && tailIsHex(n)) {
              red = Utils.hexToInt(n.substring(1, 1 + 2));
              green = Utils.hexToInt(n.substring(3, 3 + 2));
              blue = Utils.hexToInt(n.substring(5, 5 + 2));
              alpha = Utils.hexToInt(n.substring(7, 7 + 2));
            } else {
              logger.error(
                  new StringWriter().append("could not parse rgb format: ").append(n).toString());
              red = green = blue = -1;
              return new WColor(red, green, blue, alpha);
            }
          }
        }
      }
    } else {
      if (n.startsWith("rgb")) {
        if (n.length() < 5) {
          logger.error(
              new StringWriter().append("could not parse rgb format: ").append(n).toString());
          return new WColor(red, green, blue, alpha);
        }
        boolean has_alpha = n.charAt(3) == 'a';
        int start_bracket = 3 + (has_alpha ? 1 : 0);
        if (n.charAt(start_bracket) != '(' || n.charAt(n.length() - 1) != ')') {
          logger.error(
              new StringWriter().append("could not parse rgb format: ").append(n).toString());
          return new WColor(red, green, blue, alpha);
        }
        String argumentsStr =
            n.substring(
                start_bracket + 1, start_bracket + 1 + n.length() - 1 - (start_bracket + 1));
        List<String> arguments = new ArrayList<String>();
        StringUtils.split(arguments, argumentsStr, ",", false);
        if (!has_alpha && arguments.size() != 3) {
          logger.error(
              new StringWriter().append("could not parse rgb format: ").append(n).toString());
          return new WColor(red, green, blue, alpha);
        }
        if (has_alpha && arguments.size() != 4) {
          logger.error(
              new StringWriter().append("could not parse rgb format: ").append(n).toString());
          return new WColor(red, green, blue, alpha);
        }
        red = parseRgbArgument(arguments.get(0));
        green = parseRgbArgument(arguments.get(1));
        blue = parseRgbArgument(arguments.get(2));
        if (has_alpha) {
          try {
            double alpha_d = Double.parseDouble(arguments.get(3).trim());
            if (alpha_d < 0.0 || alpha_d > 1.0) {
              throw new WException("parseCssColor: alpha value out of range 0.0 to 1.0");
            }
            alpha = (int) Math.round(alpha_d * 255.);
          } catch (final RuntimeException e) {
            logger.error(
                new StringWriter().append("could not parse rgb format: ").append(n).toString());
            alpha = 255;
            return new WColor(red, green, blue, alpha);
          }
        }
      }
    }
    return new WColor(red, green, blue, alpha);
  }
}
