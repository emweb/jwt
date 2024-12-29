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

class DefaultPagingBar extends WContainerWidget {
  private static Logger logger = LoggerFactory.getLogger(DefaultPagingBar.class);

  public DefaultPagingBar(WAbstractItemView view) {
    super();
    this.view_ = view;
    this.prevButton_ = null;
    this.nextButton_ = null;
    this.firstButton_ = null;
    this.lastButton_ = null;
    this.current_ = null;
    this.view_.addStyleClass("Wt-itemview-paged");
    this.setStyleClass("Wt-pagingbar");
    this.firstButton_ = new WPushButton(tr("Wt.WAbstractItemView.PageBar.First"));
    this.addWidget(this.firstButton_);
    this.firstButton_
        .clicked()
        .addListener(
            this,
            (WMouseEvent e1) -> {
              DefaultPagingBar.this.showFirstPage();
            });
    this.prevButton_ = new WPushButton(tr("Wt.WAbstractItemView.PageBar.Previous"));
    this.addWidget(this.prevButton_);
    this.prevButton_
        .clicked()
        .addListener(
            this,
            (WMouseEvent e1) -> {
              DefaultPagingBar.this.showPreviousPage();
            });
    this.current_ = new WText();
    this.addWidget(this.current_);
    this.nextButton_ = new WPushButton(tr("Wt.WAbstractItemView.PageBar.Next"));
    this.addWidget(this.nextButton_);
    this.nextButton_
        .clicked()
        .addListener(
            this,
            (WMouseEvent e1) -> {
              DefaultPagingBar.this.showNextPage();
            });
    this.lastButton_ = new WPushButton(tr("Wt.WAbstractItemView.PageBar.Last"));
    this.addWidget(this.lastButton_);
    this.lastButton_
        .clicked()
        .addListener(
            this,
            (WMouseEvent e1) -> {
              DefaultPagingBar.this.showLastPage();
            });
    this.view_
        .pageChanged()
        .addListener(
            this,
            () -> {
              DefaultPagingBar.this.update();
            });
    this.update();
  }

  private WAbstractItemView view_;
  private WPushButton prevButton_;
  private WPushButton nextButton_;
  private WPushButton firstButton_;
  private WPushButton lastButton_;
  private WText current_;

  private void update() {
    this.firstButton_.setDisabled(this.view_.getCurrentPage() == 0);
    this.prevButton_.setDisabled(this.view_.getCurrentPage() == 0);
    this.nextButton_.setDisabled(this.view_.getCurrentPage() == this.view_.getPageCount() - 1);
    this.lastButton_.setDisabled(this.view_.getCurrentPage() == this.view_.getPageCount() - 1);
    this.current_.setText(
        WString.tr("Wt.WAbstractItemView.PageIOfN")
            .arg(this.view_.getCurrentPage() + 1)
            .arg(this.view_.getPageCount()));
  }

  private void showFirstPage() {
    this.view_.setCurrentPage(0);
  }

  private void showLastPage() {
    this.view_.setCurrentPage(this.view_.getPageCount() - 1);
  }

  private void showPreviousPage() {
    if (this.view_.getCurrentPage() > 0) {
      this.view_.setCurrentPage(this.view_.getCurrentPage() - 1);
    }
  }

  private void showNextPage() {
    if (this.view_.getCurrentPage() < this.view_.getPageCount() - 1) {
      this.view_.setCurrentPage(this.view_.getCurrentPage() + 1);
    }
  }
}
