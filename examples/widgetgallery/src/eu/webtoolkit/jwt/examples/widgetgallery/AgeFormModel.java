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

class AgeFormModel extends WFormModel {
  private static Logger logger = LoggerFactory.getLogger(AgeFormModel.class);

  public static final String AgeField = "age";

  public AgeFormModel() {
    super();
    this.addField(AgeField);
    this.setValidator(AgeField, this.createAgeValidator());
    this.setValue(AgeField, "");
  }

  private WValidator createAgeValidator() {
    return new WIntValidator(0, 150);
  }
}
