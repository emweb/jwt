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

class Grid {
  private static Logger logger = LoggerFactory.getLogger(Grid.class);

  public int horizontalSpacing_;
  public int verticalSpacing_;

  static class Section {
    private static Logger logger = LoggerFactory.getLogger(Section.class);

    public int stretch_;
    public boolean resizable_;
    public WLength initialSize_;

    public Section(int stretch) {
      this.stretch_ = stretch;
      this.resizable_ = false;
      this.initialSize_ = new WLength();
    }

    public Section() {
      this(0);
    }
  }

  static class Item {
    private static Logger logger = LoggerFactory.getLogger(Item.class);

    public WLayoutItem item_;
    public int rowSpan_;
    public int colSpan_;
    public boolean update_;
    public EnumSet<AlignmentFlag> alignment_;

    public Item(WLayoutItem item, EnumSet<AlignmentFlag> alignment) {
      this.item_ = item;
      this.rowSpan_ = 1;
      this.colSpan_ = 1;
      this.update_ = true;
      this.alignment_ = alignment;
    }

    public Item(WLayoutItem item, AlignmentFlag alignmen, AlignmentFlag... alignment) {
      this(item, EnumSet.of(alignmen, alignment));
    }

    public Item() {
      this(null, EnumSet.noneOf(AlignmentFlag.class));
    }

    public Item(WLayoutItem item) {
      this(item, EnumSet.noneOf(AlignmentFlag.class));
    }
    // public  Item(final Grid.Item other) ;
  }

  public List<Grid.Section> rows_;
  public List<Grid.Section> columns_;
  public List<List<Grid.Item>> items_;

  public Grid() {
    this.horizontalSpacing_ = 6;
    this.verticalSpacing_ = 6;
    this.rows_ = new ArrayList<Grid.Section>();
    this.columns_ = new ArrayList<Grid.Section>();
    this.items_ = new ArrayList<List<Grid.Item>>();
  }

  public void clear() {
    this.rows_.clear();
    this.columns_.clear();
    this.items_.clear();
  }
}
