/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.render;

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

class Match {
  private static Logger logger = LoggerFactory.getLogger(Match.class);

  public static boolean isMatch(Block block, final SimpleSelector s) {
    final DomElementType tag = block.getType();
    final String id = block.getId();
    final List<String> classes = block.getClasses();
    final List<String> requiredClasses = s.getClasses();
    if (s.getElementName().length() != 0
        && !s.getElementName().equals("*")
        && tag != s.getElementType()) {
      return false;
    }
    if (s.getHashId().length() != 0 && !id.equals(s.getHashId())) {
      return false;
    }
    for (int i = 0; i < requiredClasses.size(); ++i) {
      if (classes.indexOf(requiredClasses.get(i)) == -1) {
        return false;
      }
    }
    return true;
  }

  public static Specificity isMatch(Block block, final Selector selector) {
    if (!(selector.getSize() != 0)) {
      return new Specificity(false);
    }
    if (!isMatch(block, selector.at(selector.getSize() - 1))) {
      return new Specificity(false);
    }
    Block parent = block.getParent();
    for (int i = selector.getSize() - 2; i >= 0; --i) {
      boolean matchFound = false;
      while (parent != null) {
        matchFound = isMatch(parent, selector.at(i));
        parent = parent.getParent();
        if (matchFound) {
          break;
        }
      }
      if (!matchFound && !(parent != null)) {
        return new Specificity(false);
      }
    }
    return selector.getSpecificity();
  }
}
