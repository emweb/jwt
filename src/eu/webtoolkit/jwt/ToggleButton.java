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

class ToggleButton extends WText {
  private static Logger logger = LoggerFactory.getLogger(ToggleButton.class);

  public ToggleButton(ToggleButtonConfig config) {
    super();
    this.signals_ = new ArrayList<AbstractSignal>();
    this.config_ = config;
    this.setStyleClass(this.config_.getStyleClass());
    this.setInline(false);
    if (WApplication.getInstance().getEnvironment().hasAjax()) {
      this.clicked().addListener(this.config_.toggleJS_);
      this.clicked().preventPropagation();
      for (int i = 0; i < this.config_.getStates().size(); ++i) {
        this.signals_.add(new JSignal(this, "t-" + this.config_.getStates().get(i)));
      }
    } else {
      this.clicked()
          .addListener(
              this,
              (WMouseEvent e1) -> {
                ToggleButton.this.handleClick();
              });
      for (int i = 0; i < this.config_.getStates().size(); ++i) {
        this.signals_.add(new Signal());
      }
    }
  }

  public AbstractSignal signal(int i) {
    return this.signals_.get(i);
  }

  public void setState(int i) {
    this.setStyleClass(this.config_.getStyleClass() + this.config_.getStates().get(i));
  }

  private List<AbstractSignal> signals_;
  private ToggleButtonConfig config_;

  private void handleClick() {
    for (int i = 0; i < this.config_.getStates().size(); ++i) {
      if (this.getStyleClass().endsWith(this.config_.getStates().get(i))) {
        (ObjectUtils.cast(this.signals_.get(i), Signal.class)).trigger();
        break;
      }
    }
  }
}
