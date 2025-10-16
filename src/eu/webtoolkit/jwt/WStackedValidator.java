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

/**
 * A validator that combines multiple validators.
 *
 * <p>This validator allows you to combine multiple validators into a single one.
 *
 * <p>For a value to be considered valid by this validator, it must be valid for all of the
 * validators added to it. In case the value is invalid for multiple validators, the validator used
 * to generate the error message is the one with the lowest index.
 */
public class WStackedValidator extends WValidator {
  private static Logger logger = LoggerFactory.getLogger(WStackedValidator.class);

  /** Creates an empty stacked validator. */
  public WStackedValidator() {
    super();
    this.validators_ = new ArrayList<WValidator>();
    this.setMandatory(false);
  }
  /**
   * Adds a validator.
   *
   * <p>This adds a validator at the last index if the validator is not already present in the list
   * of validators.
   */
  public void addValidator(final WValidator validator) {
    if (CollectionUtils.add(this.validators_, validator)) {
      validator.addParentValidator(this);
      this.repaint();
    }
  }
  /**
   * Inserts a validator to the given index.
   *
   * <p>This inserts a validator at the given index, or at last index if the given index is bigger
   * than the number of validators. Does nothing if the validator is already present in the list of
   * validators.
   */
  public void insertValidator(int index, final WValidator validator) {
    if (index < 0) {
      throw new WException("Cannot insert validator at negative index");
    }
    if (this.validators_.indexOf(validator) == -1) {
      if (index > (int) this.validators_.size()) {
        index = (int) this.validators_.size();
      }
      this.validators_.add(0 + index, validator);
      validator.addParentValidator(this);
      this.repaint();
    }
  }
  /** Removes the given validator. */
  public void removeValidator(final WValidator validator) {
    if (this.validators_.remove(validator)) {
      validator.removeParentValidator(this);
      this.repaint();
    }
  }
  /** Returns the number of validators. */
  public int getSize() {
    return this.validators_.size();
  }
  /** Removes all the validators. */
  public void clear() {
    this.doClear();
    this.repaint();
  }

  public WValidator.Result validate(final String input) {
    for (int i = 0; i < this.validators_.size(); ++i) {
      WValidator.Result result = this.validators_.get(i).validate(input);
      if (result.getState() != ValidationState.Valid) {
        return result;
      }
    }
    return new WValidator.Result(ValidationState.Valid);
  }

  public String getJavaScriptValidate() {
    WApplication.getInstance().loadJavaScript("js/WStackedValidator.js", wtjs1());
    StringBuilder js = new StringBuilder();
    js.append("new Wt4_12_1.WStackedValidator([");
    for (int i = 0; i < this.validators_.size(); ++i) {
      if (i > 0) {
        js.append(",");
      }
      String validatorJs = this.validators_.get(i).getJavaScriptValidate();
      if (validatorJs.charAt(validatorJs.length() - 1) == ';') {
        validatorJs = validatorJs.substring(0, 0 + validatorJs.length() - 1);
      }
      js.append(validatorJs);
    }
    js.append("]);");
    return js.toString();
  }

  private List<WValidator> validators_;

  private void doClear() {
    for (int i = 0; i < this.validators_.size(); ++i) {
      this.validators_.get(i).removeParentValidator(this);
    }
    this.validators_.clear();
  }

  static WJavaScriptPreamble wtjs1() {
    return new WJavaScriptPreamble(
        JavaScriptScope.WtClassScope,
        JavaScriptObjectType.JavaScriptConstructor,
        "WStackedValidator",
        "(function(t){this.validate=function(a){for(let i=0;i<t.length;++i){const n=t[i].validate(a);if(!n.valid)return n}return{valid:!0}}})");
  }
}
