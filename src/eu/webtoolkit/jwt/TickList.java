/*
 * Copyright (C) 2020 Emweb bv, Herent, Belgium.
 *
 * See the LICENSE file for terms of use.
 */
package eu.webtoolkit.jwt;

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

final class TickList extends WWebWidget {
  private static Logger logger = LoggerFactory.getLogger(TickList.class);

  public TickList(WSlider slider, WContainerWidget parentContainer) {
    super();
    this.slider_ = slider;
    if (parentContainer != null) parentContainer.addWidget(this);
  }

  public TickList(WSlider slider) {
    this(slider, (WContainerWidget) null);
  }

  public void doUpdateDom(final DomElement element, boolean all) {
    if (all) {
      DomElement list = DomElement.createNew(this.getDomElementType());
      list.setId(element.getId() + "dl");
      int tickInterval = this.slider_.getTickInterval();
      int range = this.slider_.getMaximum() - this.slider_.getMinimum();
      if (range == 0) {
        return;
      }
      if (tickInterval == 0) {
        tickInterval = range / 2;
      }
      int numTicks = range / tickInterval + 1;
      if (numTicks < 1) {
        return;
      }
      for (int i = 0; i < numTicks; ++i) {
        int value = this.slider_.getMinimum() + i * tickInterval;
        DomElement option = DomElement.createNew(DomElementType.OPTION);
        option.setProperty(Property.Value, String.valueOf(value));
        list.addChild(option);
      }
      element.addChild(list);
    }
  }

  DomElementType getDomElementType() {
    return DomElementType.DATALIST;
  }

  private WSlider slider_ = null;
}
