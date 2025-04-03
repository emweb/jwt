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

class TopicTemplate extends BaseTemplate {
  private static Logger logger = LoggerFactory.getLogger(TopicTemplate.class);

  public TopicTemplate(String trKey, WContainerWidget parentContainer) {
    super(trKey, (WContainerWidget) null);
    this.namespaceToPackage = new HashMap<String, String>();
    this.bindString("doc-url", "//www.webtoolkit.eu/jwt/latest/doc/javadoc/eu/webtoolkit/jwt/");
    this.namespaceToPackage.put("Chart", "chart");
    this.namespaceToPackage.put("Render", "render");
    if (WApplication.getInstance().getTheme().getName().equals("bootstrap2")) {
      this.bindString("dl-class", "dl-horizontal");
      this.bindString("row", "row-fluid");
    } else {
      this.bindString("dl-class", "row");
      this.bindString("row", "row");
    }
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public TopicTemplate(String trKey) {
    this(trKey, (WContainerWidget) null);
  }

  public void resolveString(final String varName, final List<WString> args, final Writer result)
      throws IOException {
    if (varName.equals("doc-link")) {
      String className = args.get(0).toString();
      String type = "class";
      String title = "";
      for (int i = 1; i < args.size(); ++i) {
        String arg = args.get(i).toString();
        if (arg.startsWith("type=")) {
          type = arg.substring(5);
        } else {
          if (arg.startsWith("title=")) {
            title = arg.substring(6);
          }
        }
      }
      for (Iterator<Map.Entry<String, String>> it_it =
              this.namespaceToPackage.entrySet().iterator();
          it_it.hasNext(); ) {
        Map.Entry<String, String> it = it_it.next();
        className = StringUtils.replaceAll(className, it.getKey() + "-", it.getValue() + ".");
      }
      result
          .append("<a href=\"")
          .append(this.docUrl(type, className))
          .append("\" target=\"_blank\">");
      if (title.length() == 0) {
        title = className;
        for (Iterator<Map.Entry<String, String>> it_it =
                this.namespaceToPackage.entrySet().iterator();
            it_it.hasNext(); ) {
          Map.Entry<String, String> it = it_it.next();
          title = StringUtils.replaceAll(title, it.getValue() + ".", "");
        }
      }
      result.append(Utils.htmlEncode(title)).append("</a>");
    } else {
      if (varName.equals("src")) {
        String exampleName = args.get(0).toString();
        result
            .append("<fieldset class=\"src\">")
            .append("<legend>source</legend>")
            .append(tr("src-" + exampleName).toXhtml())
            .append("</fieldset>");
      } else {
        super.resolveString(varName, args, result);
      }
    }
  }

  private String docUrl(final String type, final String className) {
    StringBuilder ss = new StringBuilder();
    if (type.equals("namespace")) {
      ss.append(this.getString("doc-url"))
          .append(this.namespaceToPackage.get(className))
          .append("/package-summary.html");
    } else {
      String cn = className;
      cn = StringUtils.replaceAll(cn, ".", "/");
      ss.append(this.getString("doc-url")).append(cn).append(".html");
    }
    return ss.toString();
  }

  private String getString(final String varName) {
    try {
      StringWriter ss = new StringWriter();
      List<WString> args = new ArrayList<WString>();
      this.resolveString(varName, args, ss);
      return ss.toString();
    } catch (IOException ie) {
      logger.info("Ignoring exception {}", ie.getMessage(), ie);
      return null;
    }
  }

  private static String escape(final String name) {
    StringBuilder ss = new StringBuilder();
    for (int i = 0; i < name.length(); ++i) {
      if (name.charAt(i) != ':') {
        ss.append(name.charAt(i));
      } else {
        ss.append("_1");
      }
    }
    return ss.toString();
  }

  private Map<String, String> namespaceToPackage;
}
