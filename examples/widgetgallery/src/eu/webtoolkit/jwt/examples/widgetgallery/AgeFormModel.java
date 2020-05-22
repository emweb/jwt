/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt.examples.widgetgallery;

import eu.webtoolkit.jwt.*;
import eu.webtoolkit.jwt.chart.*;
import eu.webtoolkit.jwt.servlet.*;
import eu.webtoolkit.jwt.utils.*;
import java.io.*;
import java.lang.ref.*;
import java.util.*;
import java.util.regex.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AgeFormModel extends WFormModel {
  private static Logger logger = LoggerFactory.getLogger(AgeFormModel.class);

  public static String AgeField = "age";

  public AgeFormModel(WObject parent) {
    super(parent);
    this.addField(AgeField);
    this.setValidator(AgeField, this.createAgeValidator());
    this.setValue(AgeField, "");
  }

  public AgeFormModel() {
    this((WObject) null);
  }

  private WValidator createAgeValidator() {
    WIntValidator v = new WIntValidator(0, 150);
    return v;
  }
}
