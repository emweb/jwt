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

final class ContentsContainer extends WContainerWidget {
  private static Logger logger = LoggerFactory.getLogger(ContentsContainer.class);

  public ContentsContainer(WTreeView treeView) {
    super();
    this.treeView_ = treeView;
    this.setLayoutSizeAware(true);
  }

  protected void layoutSizeChanged(int width, int height) {
    this.treeView_.contentsSizeChanged(width, height);
  }

  private WTreeView treeView_;
}
