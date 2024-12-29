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

class ToggleButtonConfig {
  private static Logger logger = LoggerFactory.getLogger(ToggleButtonConfig.class);

  public ToggleButtonConfig(WWidget parent, final String styleClass) {
    this.states_ = new ArrayList<String>();
    this.toggleJS_ = null;
    this.styleClass_ = styleClass;
    this.toggleJS_ = new JSlot(parent);
  }

  public void addState(final String className) {
    this.states_.add(className);
  }

  public void generate() {
    WApplication app = WApplication.getInstance();
    StringWriter js = new StringWriter();
    js.append("function(s, e) {var states = new Array(");
    for (int i = 0; i < this.states_.size(); ++i) {
      if (i != 0) {
        js.append(',');
      }
      js.append('\'').append(this.states_.get(i)).append('\'');
    }
    js.append("), i, il;for (i=0; i<")
        .append(String.valueOf(this.states_.size()))
        .append("; ++i) {if (s.classList.contains(states[i])) {")
        .append(app.getJavaScriptClass())
        .append(
            ".emit(s, 't-'+states[i]);s.classList.remove(states[i]);s.classList.add(states[(i+1) % ")
        .append(String.valueOf(this.states_.size()))
        .append("]);break;}}}");
    this.toggleJS_.setJavaScript(js.toString());
  }

  public List<String> getStates() {
    return this.states_;
  }

  public String getStyleClass() {
    return this.styleClass_;
  }

  private List<String> states_;
  JSlot toggleJS_;
  private String styleClass_;
}
