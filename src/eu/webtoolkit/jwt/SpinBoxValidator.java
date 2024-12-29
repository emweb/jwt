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

class SpinBoxValidator extends WValidator {
  private static Logger logger = LoggerFactory.getLogger(SpinBoxValidator.class);

  public SpinBoxValidator(WAbstractSpinBox spinBox) {
    super();
    this.spinBox_ = spinBox;
  }

  public WValidator.Result validate(final String input) {
    boolean valid = this.spinBox_.parseValue(input);
    if (valid) {
      return this.spinBox_.getValidateRange();
    } else {
      return new WValidator.Result(ValidationState.Invalid);
    }
  }

  public String getJavaScriptValidate() {
    return "new function() { this.validate = function(t) {return "
        + this.spinBox_.getJsRef()
        + ".wtObj.validate(t);};}";
  }

  private WAbstractSpinBox spinBox_;
}
