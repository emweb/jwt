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

class BaseTemplate extends WTemplate {
  private static Logger logger = LoggerFactory.getLogger(BaseTemplate.class);

  public BaseTemplate(String trKey, WContainerWidget parentContainer) {
    super(tr(trKey), (WContainerWidget) null);
    this.setInternalPathEncoding(true);
    this.addFunction("tr", Functions.tr);
    this.addFunction("block", Functions.block);
    this.setCondition("if:cpp", false);
    this.setCondition("if:java", true);
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public BaseTemplate(String trKey) {
    this(trKey, (WContainerWidget) null);
  }

  public void resolveString(final String varName, final List<WString> args, final Writer result)
      throws IOException {
    if (varName.equals("img")) {
      String src = "";
      String alt = "";
      String style = "";
      for (WString arg : args) {
        String argS = arg.toString();
        if (argS.startsWith("src=")) {
          src = argS.substring(4);
        } else {
          if (argS.startsWith("alt=")) {
            alt = argS.substring(4);
          } else {
            if (argS.startsWith("style=")) {
              style = argS.substring(6);
            }
          }
        }
      }
      WApplication app = WApplication.getInstance();
      result
          .append("<img src=\"")
          .append(Utils.htmlAttributeValue(app.resolveRelativeUrl(src)))
          .append("\" ");
      if (alt.length() != 0) {
        result.append("alt=\"").append(Utils.htmlAttributeValue(alt)).append("\" ");
      }
      if (style.length() != 0) {
        result.append("style=\"").append(Utils.htmlAttributeValue(style)).append("\" ");
      }
      result.append("/>");
    } else {
      super.resolveString(varName, args, result);
    }
  }
}
